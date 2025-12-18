package com.zj.ink.md

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.zj.data.R
import com.zj.data.export.GlanceImageLoader
import com.zj.ink.widget.gray
import com.zj.ink.widget.textColor

/**
 * 计算表格列宽以实现基础对齐效果
 */
private fun calculateColumnWidths(headers: List<String>, rows: List<List<String>>): List<Int> {
    val columnCount = headers.size
    val columnWidths = mutableListOf<Int>()

    for (i in 0 until columnCount) {
        var maxWidth = headers.getOrNull(i)?.length ?: 0
        rows.forEach { row ->
            val cellLength = row.getOrNull(i)?.length ?: 0
            if (cellLength > maxWidth) {
                maxWidth = cellLength
            }
        }
        // 限制最大宽度以适应小组件，最小8个字符，最大16个字符
        columnWidths.add(maxWidth.coerceIn(8, 16))
    }

    return columnWidths
}

/**
 * 格式化单元格文本以实现对齐效果
 */
private fun formatCellText(text: String, width: Int, alignment: TableAlignment): String {
    return when (alignment) {
        TableAlignment.LEFT -> text.padEnd(width)
        TableAlignment.RIGHT -> text.padStart(width)
        TableAlignment.CENTER -> {
            val padding = width - text.length
            val leftPadding = padding / 2
            val rightPadding = padding - leftPadding
            " ".repeat(leftPadding) + text + " ".repeat(rightPadding)
        }
    }
}

/**
 * 在 Glance 中渲染代码块的 Composable 组件
 * 支持语言标签显示和等宽字体渲染，适应Glance环境限制
 */
@Composable
private fun CodeBlockRenderer(
    code: String,
    language: String
) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(gray)
            .padding(8.dp)
    ) {
        // 显示语言标签（如果有）
        if (language.isNotEmpty()) {
            Text(
                text = language.uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(
                        day = Color(0xFF666666),
                        night = Color(0xFF999999)
                    )
                ),
                modifier = GlanceModifier.padding(bottom = 4.dp)
            )
        }

        // 代码内容
        Text(
            text = code,
            style = TextStyle(
                fontSize = 11.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

/**
 * 在 Glance 组件中渲染 Markdown 内容，增强错误处理
 *
 * @param content 需要渲染的 Markdown 字符串内容
 */
@Composable
fun GlanceRenderMarkdown(content: String) {
    // 输入验证和边界检查
    if (content.isBlank()) {
        Text(
            text = "无内容",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(
                    day = Color(0xFF666666),
                    night = Color(0xFF999999)
                )
            ),
            modifier = GlanceModifier.padding(8.dp)
        )
        return
    }

    // 优化的Glance解析和渲染，严格控制内存和性能，增强错误处理
    val (elements, hasError) = remember(content) {
        try {
            // 对于Glance环境，严格限制解析内容大小，防止内存溢出
            val limitedContent = if (content.length > 5000) {
                content.take(5000) + "..."
            } else {
                content
            }

            // 解析并限制元素数量，确保小组件性能
            val parsed = MarkdownParser.parse(limitedContent)
                .take(10) // 限制Glance渲染的元素数量，防止内存溢出
                .filter { element ->
                    // 过滤掉在Glance中不适合显示的复杂元素
                    when (element) {
                        is Table -> element.rows.size <= 3 // 限制表格行数
                        is CodeBlock -> element.text.length <= 500 // 限制代码块长度
                        is Image -> true // Glance中暂时不渲染图片，避免网络和内存问题
                        else -> true
                    }
                }
            Pair(parsed, false)
        } catch (e: Exception) {
            // 解析失败时的降级处理
            println("GlanceRenderMarkdown: Parse error - ${e.message}")
            val fallbackElements = listOf(
                Paragraph("内容解析出现问题"),
                Paragraph(content.take(200) + if (content.length > 200) "..." else "")
            )
            Pair(fallbackElements, true)
        }
    }

    // 显示解析错误提示（如果有）
    if (hasError) {
        Column(modifier = GlanceModifier.padding(4.dp)) {
            Text(
                text = "⚠️ 解析错误",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(
                        day = Color(0xFFD32F2F),
                        night = Color(0xFFEF5350)
                    ),
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(bottom = 4.dp)
            )
        }
    }

    Column {
        elements.forEach { element ->
            RenderGlanceElement(element)
        }
    }
}

/**
 * 渲染单个Glance元素
 */
@Composable
private fun RenderGlanceElement(element: MarkdownElement) {
    when (element) {
        is Heading -> {
            // 标题渲染错误处理
            val safeText = element.text.takeIf { it.isNotBlank() } ?: "无标题内容"
            val safeLevel = element.level.coerceIn(1, 6)

            Text(
                text = safeText.take(100), // 限制标题长度
                style = TextStyle(
                    fontSize = when (safeLevel) {
                        1 -> 20.sp
                        2 -> 18.sp
                        3 -> 16.sp
                        4 -> 14.sp
                        5 -> 12.sp
                        6 -> 11.sp
                        else -> 16.sp
                    },
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        is Bold -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: ""
            if (safeText.isNotEmpty()) {
                Text(
                    text = safeText.take(200), // 限制文本长度
                    style = TextStyle(fontWeight = FontWeight.Bold, color = textColor)
                )
            }
        }

        is Italic -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: ""
            if (safeText.isNotEmpty()) {
                Text(
                    text = safeText.take(200), // 限制文本长度
                    style = TextStyle(fontStyle = FontStyle.Italic, color = textColor)
                )
            }
        }

        is Strikethrough -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: ""
            if (safeText.isNotEmpty()) {
                Text(
                    text = safeText.take(200), // 限制文本长度
                    style = TextStyle(
                        textDecoration = TextDecoration.LineThrough,
                        color = textColor
                    )
                )
            }
        }

        is Highlight -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: ""
            if (safeText.isNotEmpty()) {
                Text(
                    modifier = GlanceModifier.background(
                        ColorProvider(
                            day = Color(0xFFFFEB3B).copy(alpha = 0.4f),
                            night = Color(0xFF827717).copy(alpha = 0.4f)
                        )
                    ).padding(horizontal = 2.dp, vertical = 1.dp),
                    text = safeText.take(150), // 限制高亮文本长度
                    style = TextStyle(
                        color = textColor,
                    ),
                )
            }
        }

        is TaskList -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: "空任务"
            val safeLevel = element.level.coerceIn(0, 6) // 限制嵌套层级

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(start = (safeLevel * 16).dp)
                    .clickable(
                        onClick = actionStartActivity(
                            Intent().apply {
                                action = Intent.ACTION_MAIN
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                setPackage("com.zj.note")
                            }
                        )
                    )
            ) {
                Text(
                    text = if (element.isChecked) "☑" else "☐",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = if (element.isChecked)
                            ColorProvider(
                                day = Color(0xFF4CAF50),
                                night = Color(0xFF66BB6A)
                            ) else textColor
                    )
                )
                Text(
                    text = " ${safeText.take(50)}", // 限制任务文本长度
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (element.isChecked)
                            ColorProvider(
                                day = Color(0xFF757575),
                                night = Color(0xFF9E9E9E)
                            ) else textColor,
                        textDecoration = if (element.isChecked)
                            TextDecoration.LineThrough else null
                    )
                )
            }
        }

        is Footnote -> {
            Text(
                text = if (element.isReference) "[${element.id}]" else "${element.id}: ${element.text}",
                style = TextStyle(color = textColor, fontSize = 10.sp)
            )
        }

        is Superscript -> {
            Text(
                text = "^${element.text}",
                style = TextStyle(color = textColor, fontSize = 8.sp)
            )
        }

        is Subscript -> {
            Text(
                text = "_${element.text}",
                style = TextStyle(color = textColor, fontSize = 8.sp)
            )
        }

        is Math -> {
            Text(
                text = element.expression,
                style = TextStyle(color = textColor, fontFamily = FontFamily.Monospace)
            )
        }

        is NestedList -> {
            // 简化处理嵌套列表
            Text(
                text = "• Nested List",
                style = TextStyle(color = textColor)
            )
        }

        is Paragraph -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: ""
            if (safeText.isNotEmpty()) {
                Text(
                    text = safeText.take(300), // 限制段落长度
                    style = TextStyle(fontSize = 12.sp, color = textColor)
                )
            }
        }

        is Link -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: "链接"
            val safeUrl = element.url.takeIf { it.isNotBlank() } ?: "about:blank"

            Text(
                text = safeText.take(50), // 限制链接文本长度
                style = TextStyle(
                    color = ColorProvider(
                        day = Color.Blue,
                        night = Color.Blue
                    )
                ),
                modifier = GlanceModifier.clickable(
                    onClick = actionStartActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            safeUrl.toUri()
                        )
                    )
                )
            )
        }

        is Image -> {
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }

            // 使用 rememberCoroutineScope 启动协程加载图片
            LaunchedEffect(Unit) {
                bitmap = GlanceImageLoader.loadBitmap(element.url, roundedCorners = true)
            }

            // 当 bitmap 不为空时，显示图片
            if (bitmap != null) {
                Image(
                    provider = ImageProvider(bitmap!!),
                    contentDescription = "Markdown 图片",
                )
            }
        }

        is Code -> {
            val safeText = element.text.takeIf { it.isNotBlank() } ?: ""
            if (safeText.isNotEmpty()) {
                Text(
                    text = safeText.take(100), // 限制行内代码长度
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = textColor
                    ),
                    modifier = GlanceModifier.background(gray)
                        .padding(vertical = 5.dp)
                )
            }
        }

        is CodeBlock -> {
            val safeCode = element.text.takeIf { it.isNotBlank() } ?: ""
            val safeLanguage = element.language.take(20) // 限制语言标识符长度

            if (safeCode.isNotEmpty()) {
                CodeBlockRenderer(
                    code = safeCode.take(500), // 限制代码块长度
                    language = safeLanguage
                )
            }
        }

        is BlockQuote -> {
            // 使用字符模拟边框线效果，根据层级重复显示
            val borderPrefix = "┃".repeat(minOf(element.level, 6))
            val levelPadding = (element.level * 16).dp

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(start = levelPadding, top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = borderPrefix,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(
                            day = Color(0xFF757575),
                            night = Color(0xFF9E9E9E)
                        )
                    )
                )
                Text(
                    text = " ${element.text}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = ColorProvider(
                            day = Color(0xFF424242),
                            night = Color(0xFFBDBDBD)
                        )
                    ),
                    modifier = GlanceModifier.padding(start = 8.dp)
                )
            }
        }

        is UnorderedList -> {
            Column {
                element.items.forEach { item ->
                    // 获取层级标记符号，不同层级使用不同符号
                    val bulletSymbol = when (element.level % 6) {
                        0 -> "•"
                        1 -> "○"
                        2 -> "▪"
                        3 -> "▫"
                        4 -> "‣"
                        else -> "⁃"
                    }

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(start = (element.level * 16).dp)
                    ) {
                        Text(
                            text = bulletSymbol,
                            style = TextStyle(color = textColor)
                        )
                        Text(
                            text = " $item",
                            style = TextStyle(color = textColor)
                        )
                    }
                }
            }
        }

        is OrderedList -> {
            Column {
                element.items.forEachIndexed { index, item ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(start = (element.level * 16).dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = TextStyle(color = textColor)
                        )
                        Text(
                            text = " $item",
                            style = TextStyle(color = textColor)
                        )
                    }
                }
            }
        }

        is Table -> {
            // 简化表格渲染以适应小组件环境
            val tableCellPadding = 2.dp // 减小内边距以节省空间

            // 计算列宽以实现基础对齐效果
            val columnWidths = calculateColumnWidths(element.headers, element.rows)

            Column(
                modifier = GlanceModifier.fillMaxWidth()
                    .padding(vertical = 3.dp) // 减小外边距
            ) {
                // 表头 - 简化布局，减少嵌套
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    element.headers.forEachIndexed { index, header ->
                        val alignment =
                            if (index < element.alignments.size) element.alignments[index] else TableAlignment.LEFT
                        val alignedText =
                            formatCellText(header, columnWidths.getOrNull(index) ?: 12, alignment)

                        Text(
                            text = alignedText,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp, // 适合小组件的字体大小
                                color = textColor
                            ),
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(ImageProvider(R.drawable.ic_table_header_background))
                                .padding(tableCellPadding)
                        )
                    }
                }

                // 表格行 - 简化处理，最多显示前3行以适应小组件空间
                element.rows.take(3).forEach { row ->
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        row.forEachIndexed { index, cell ->
                            val alignment =
                                if (index < element.alignments.size) element.alignments[index] else TableAlignment.LEFT
                            val truncatedCell =
                                if (cell.length > 15) "${cell.take(12)}..." else cell
                            val alignedText = formatCellText(
                                truncatedCell,
                                columnWidths.getOrNull(index) ?: 12,
                                alignment
                            )

                            Text(
                                text = alignedText,
                                style = TextStyle(fontSize = 9.sp, color = textColor), // 更小的字体适应小组件
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .background(ImageProvider(R.drawable.ic_table_background))
                                    .padding(tableCellPadding)
                            )
                        }
                    }
                }

                // 如果有更多行，显示省略提示
                if (element.rows.size > 3) {
                    Text(
                        text = "... +${element.rows.size - 3} more rows",
                        style = TextStyle(
                            fontSize = 8.sp,
                            color = ColorProvider(
                                day = Color(0xFF757575),
                                night = Color(0xFF9E9E9E)
                            )
                        ),
                        modifier = GlanceModifier.padding(start = tableCellPadding)
                    )
                }
            }
        }

        is Divider -> {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(gray)
            ) {}
        }
    }
}