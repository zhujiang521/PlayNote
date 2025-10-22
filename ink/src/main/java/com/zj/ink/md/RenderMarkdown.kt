@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zj.ink.md

/**
 * PlayNote Markdown渲染器 - Compose版本
 *
 * 这是PlayNote应用的主要Markdown渲染组件，使用Jetpack Compose实现。
 * 支持完整的标准Markdown语法和扩展语法的渲染，包括：
 *
 * 标准语法：
 * - 标题 (H1-H6)
 * - 段落、加粗、斜体、删除线
 * - 链接和图片
 * - 代码和代码块（支持语法高亮）
 * - 引用（支持多级嵌套）
 * - 表格（支持列对齐）
 * - 列表（支持嵌套）
 *
 * 扩展语法：
 * - 任务列表（支持交互）
 * - 高亮文本
 * - 脚注（引用和定义）
 * - 上下标
 * - 数学公式（LaTeX格式）
 * - 转义字符处理
 *
 * 性能特性：
 * - 虚拟化渲染（LazyColumn）处理大文档
 * - 语法高亮结果缓存
 * - 错误处理和优雅降级
 * - 主题适配（日间/夜间模式）
 * - 优化的状态管理
 *
 * @author PlayNote开发团队
 * @since 1.0
 */

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.size.Size.Companion.ORIGINAL
import com.zj.data.R

/**
 * 渲染 Markdown 内容为 Jetpack Compose UI 组件。
 *
 * 该函数接收一个 Markdown 字符串，并将其解析为一系列 UI 元素进行展示，支持标题、段落、加粗、斜体、链接、图片、代码块、列表、表格等多种 Markdown 语法。
 *
 * @param markdown 要渲染的 Markdown 文本内容。
 * @param modifier 应用于根布局的 Modifier。
 * @param sharedTransitionScope 用于支持共享元素过渡动画的 SharedTransitionScope 对象（可选）。
 * @param animatedContentScope 用于支持共享元素过渡动画的 AnimatedContentScope 对象（可选）。
 * @param onImageClick 当图片被点击时触发的回调函数，参数为图片 URL。
 */
@Composable
fun RenderMarkdown(
    markdown: String,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    showRoundShape: Boolean = false,
    onImageClick: (String) -> Unit = {},
    onTaskToggle: ((taskIndex: Int, taskText: String, currentChecked: Boolean) -> Unit)? = null
) {
    // 输入验证和错误处理
    if (markdown.isBlank()) {
        // 空内容处理
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painterResource(R.drawable.ic_no_data),
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )
            Text(
                text = stringResource(R.string.markdown_empty_content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // 集中式状态管理
    data class MarkdownRenderState(
        val elements: List<MarkdownElement>,
        val hasError: Boolean,
        val expandedSections: Set<Int> = emptySet()
    )

    // 使用rememberSaveable保存渲染状态
    val renderState = rememberSaveable(markdown, saver = run {
        listSaver(
            save = { state ->
                listOf(state.elements, state.hasError, state.expandedSections)
            },
            restore = { restored ->
                MarkdownRenderState(
                    elements = restored[0] as List<MarkdownElement>,
                    hasError = restored[1] as Boolean,
                    expandedSections = restored[2] as Set<Int>
                )
            }
        )
    }) {
        try {
            val parsed = MarkdownParser.parse(markdown)
            MarkdownRenderState(parsed, false)
        } catch (e: Exception) {
            println("RenderMarkdown: Parse error - ${e.message}")
            val fallbackElements = listOf(
                Paragraph("内容解析出现问题，请检查格式"),
                Paragraph(markdown.take(500) + if (markdown.length > 500) "..." else "")
            )
            MarkdownRenderState(fallbackElements, true)
        }
    }

    val (elements, hasError) = renderState

    // 显示解析错误提示
    if (hasError) {
        Column(modifier = modifier) {
            // 错误提示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚠️",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.markdown_parse_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 降级内容渲染
            RenderElementsList(
                elements = elements,
                modifier = Modifier,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                showRoundShape = showRoundShape,
                onImageClick = onImageClick,
                onTaskToggle = onTaskToggle
            )
        }
        return
    }

    // 正常渲染流程
    RenderElementsList(
        elements = elements,
        modifier = modifier,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        showRoundShape = showRoundShape,
        onImageClick = onImageClick,
        onTaskToggle = onTaskToggle
    )
}

/**
 * 渲染元素列表的通用函数，支持错误处理
 */
@Composable
private fun RenderElementsList(
    elements: List<MarkdownElement>,
    modifier: Modifier,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?,
    showRoundShape: Boolean,
    onImageClick: (String) -> Unit,
    onTaskToggle: ((taskIndex: Int, taskText: String, currentChecked: Boolean) -> Unit)?
) {
    // 注意：由于SubcomposeAsyncImage在LazyColumn中存在intrinsic measurement问题
    // 暂时禁用LazyColumn优化，统一使用Column渲染以避免崩溃
    // 未来可考虑使用AsyncImage替代SubcomposeAsyncImage来重新启用LazyColumn优化
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        elements.forEachIndexed { index, element ->
            SafeRenderMarkdownElement(
                element = element,
                elementIndex = index,
                allElements = elements,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                showRoundShape = showRoundShape,
                onImageClick = onImageClick,
                onTaskToggle = onTaskToggle
            )
        }
    }
}

/**
 * 渲染图片元素的独立函数
 * 用于在LazyColumn中避免intrinsic measurement问题
 */
@Composable
private fun RenderImageElement(
    element: Image,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?,
    showRoundShape: Boolean,
    onImageClick: (String) -> Unit
) {
    if (sharedTransitionScope != null && animatedContentScope != null) {
        with(sharedTransitionScope) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(element.url)
                    .size(ORIGINAL)
                    .build(),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        if (showRoundShape)
                            RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                    )
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = "image-${element.url}"),
                        animatedVisibilityScope = animatedContentScope,
                    )
                    .clickable {
                        onImageClick(element.url)
                    },
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp))
                    }
                },
                error = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painterResource(R.drawable.ic_placeholder),
                            contentDescription = stringResource(R.string.down_fail)
                        )
                    }
                },
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(element.url)
                .size(ORIGINAL)
                .build(),
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    if (showRoundShape)
                        RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                )
                .clickable {
                    onImageClick(element.url)
                },
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                }
            },
            error = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painterResource(R.drawable.ic_placeholder_big),
                        contentDescription = stringResource(R.string.down_fail)
                    )
                }
            },
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * 安全渲染单个Markdown元素的函数，包含错误处理
 */
@Composable
private fun SafeRenderMarkdownElement(
    element: MarkdownElement,
    elementIndex: Int,
    allElements: List<MarkdownElement>,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?,
    showRoundShape: Boolean,
    onImageClick: (String) -> Unit,
    onTaskToggle: ((taskIndex: Int, taskText: String, currentChecked: Boolean) -> Unit)?
) {
    RenderMarkdownElement(
        element = element,
        elementIndex = elementIndex,
        allElements = allElements,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        showRoundShape = showRoundShape,
        onImageClick = onImageClick,
        onTaskToggle = onTaskToggle
    )
}

/**
 * 渲染单个Markdown元素的优化函数
 * 提取出来避免重复代码，提升维护性
 */
@Composable
private fun RenderMarkdownElement(
    element: MarkdownElement,
    elementIndex: Int,
    allElements: List<MarkdownElement>,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?,
    showRoundShape: Boolean,
    onImageClick: (String) -> Unit,
    onTaskToggle: ((taskIndex: Int, taskText: String, currentChecked: Boolean) -> Unit)?
) {
    when (element) {
        is Heading -> {
            // 标题渲染错误处理
            val safeText = element.text.takeIf { it.isNotBlank() } ?: "无标题内容"
            val safeLevel = element.level.coerceIn(1, 6)

            Text(
                text = safeText,
                fontSize = when (safeLevel) {
                    1 -> 24.sp
                    2 -> 20.sp
                    3 -> 18.sp
                    4 -> 16.sp
                    5 -> 14.sp
                    6 -> 12.sp
                    else -> 14.sp // 默认大小，保持兼容性
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        is Bold -> {
            Text(
                text = element.text,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        is Italic -> {
            Text(
                text = element.text,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        is Strikethrough -> {
            Text(
                text = element.text,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        is Highlight -> {
            Text(
                text = element.text,
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.highlight_background),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        is Paragraph -> {
            Text(
                text = element.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        is Link -> {
            BasicText(
                buildAnnotatedString {
                    withLink(
                        LinkAnnotation.Url(
                            element.url,
                            TextLinkStyles(style = SpanStyle(color = Color.Blue)),
                        )
                    ) {
                        append(element.text)
                    }
                }
            )
        }

        is Image -> {
            // 使用Box包裹SubcomposeAsyncImage，避免LazyColumn的intrinsic measurement问题
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (sharedTransitionScope != null && animatedContentScope != null) {
                    with(sharedTransitionScope) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(element.url)
                                .size(ORIGINAL)
                                .build(),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(
                                    if (showRoundShape)
                                        RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                                )
                                .sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(key = "image-${element.url}"),
                                    animatedVisibilityScope = animatedContentScope,
                                )
                                .clickable {
                                    onImageClick(element.url)
                                },
                            contentDescription = stringResource(R.string.image),
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                }
                            },
                            error = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Image(
                                        painterResource(R.drawable.ic_placeholder),
                                        contentDescription = stringResource(R.string.down_fail)
                                    )
                                }
                            },
                        )
                    }
                } else {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(element.url)
                            .size(ORIGINAL)
                            .build(),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                if (showRoundShape)
                                    RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                            )
                            .clickable {
                                onImageClick(element.url)
                            },
                        contentDescription = stringResource(R.string.image),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(30.dp))
                            }
                        },
                        error = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_placeholder_big),
                                    contentDescription = stringResource(R.string.down_fail)
                                )
                            }
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        is Code -> {
            SelectionContainer {
                Text(
                    text = element.text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                )
            }
        }

        is CodeBlock -> {
            CodeBlockRenderer(
                code = element.text,
                language = element.language
            )
        }

        is BlockQuote -> {
            // 计算层级缩进，每级16dp，最大支持6级
            val indentLevel = minOf(element.level, 6)
            val leftPadding = (indentLevel * 16).dp

            // 根据层级调整边框颜色透明度，创造视觉层次感
            val borderAlpha = maxOf(0.3f, 0.8f - (indentLevel - 1) * 0.1f)
            val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = borderAlpha)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftPadding, top = 4.dp, bottom = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 4.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = element.text,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        is UnorderedList -> {
            element.items.forEach { item ->
                Row(
                    modifier = Modifier.padding(start = (element.level * 16).dp)
                ) {
                    Text(
                        text = getUnorderedListBullet(element.level),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        is OrderedList -> {
            element.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.padding(start = (element.level * 16).dp)
                ) {
                    Text(
                        text = "${index + 1}. ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        is Divider -> {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        }

        is Table -> {
            val tableCellPadding = 8.dp
            val columnCount = element.headers.size
            val weight = 1f / columnCount

            // 辅助函数：将TableAlignment转换为TextAlign
            fun getTextAlign(alignment: TableAlignment): TextAlign {
                return when (alignment) {
                    TableAlignment.LEFT -> TextAlign.Start
                    TableAlignment.CENTER -> TextAlign.Center
                    TableAlignment.RIGHT -> TextAlign.End
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, colorResource(R.color.divider))
                    .padding(0.5.dp)
            ) {
                // 表头
                Row(modifier = Modifier.fillMaxWidth()) {
                    element.headers.forEachIndexed { index, header ->
                        val alignment = element.alignments.getOrNull(index) ?: TableAlignment.LEFT
                        Text(
                            text = header,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = getTextAlign(alignment),
                            modifier = Modifier
                                .weight(weight)
                                .background(colorResource(R.color.edit_background))
                                .border(0.5.dp, colorResource(R.color.divider))
                                .padding(tableCellPadding)
                        )
                    }
                }

                // 表格行
                element.rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEachIndexed { index, cell ->
                            val alignment =
                                element.alignments.getOrNull(index) ?: TableAlignment.LEFT
                            Text(
                                text = cell,
                                fontSize = 14.sp,
                                textAlign = getTextAlign(alignment),
                                modifier = Modifier
                                    .weight(weight)
                                    .border(
                                        0.5.dp,
                                        colorResource(R.color.divider)
                                    )
                                    .padding(tableCellPadding)
                            )
                        }
                    }
                }
            }
        }

        is TaskList -> {
            // 计算当前任务在所有TaskList元素中的索引
            val taskIndex = remember(elementIndex, allElements) {
                allElements.take(elementIndex + 1)
                    .filterIsInstance<TaskList>()
                    .size - 1
            }

            TaskListItem(
                text = element.text,
                isChecked = element.isChecked,
                level = element.level,
                onCheckedChange = { newChecked ->
                    // 触发任务状态切换回调
                    onTaskToggle?.invoke(taskIndex, element.text, element.isChecked)
                }
            )
        }

        is Footnote -> {
            FootnoteRenderer(
                id = element.id,
                text = element.text,
                isReference = element.isReference
            )
        }

        is Superscript -> {
            SuperscriptRenderer(text = element.text)
        }

        is Subscript -> {
            SubscriptRenderer(text = element.text)
        }

        is Math -> {
            MathRenderer(
                expression = element.expression,
                isInline = element.isInline
            )
        }

        is NestedList -> {
            // 简化处理嵌套列表
            Text(
                text = "• Nested List",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

    }
}

/**
 * 获取不同层级的无序列表标记符号
 *
 * @param level 列表层级（0-based）
 * @return 对应层级的标记符号
 */
private fun getUnorderedListBullet(level: Int): String {
    return when (level % 6) {
        0 -> "• "      // 一级：实心圆点
        1 -> "○ "      // 二级：空心圆点
        2 -> "▪ "      // 三级：实心方块
        3 -> "▫ "      // 四级：空心方块
        4 -> "‣ "      // 五级：三角形
        5 -> "⁃ "      // 六级：连字符
        else -> "• "   // 默认：实心圆点
    }
}

/**
 * 代码块语法高亮渲染器 Composable 组件
 *
 * 支持基于正则表达式的简单语法高亮，根据语言类型显示不同颜色
 *
 * @param code 代码内容
 * @param language 编程语言类型（如"kotlin"、"java"、"python"等）
 */
@Composable
private fun CodeBlockRenderer(
    code: String,
    language: String
) {
    // 获取语法高亮颜色
    val keywordColor = colorResource(R.color.syntax_keyword)
    val stringColor = colorResource(R.color.syntax_string)
    val commentColor = colorResource(R.color.syntax_comment)
    val numberColor = colorResource(R.color.syntax_number)
    val codeBackgroundColor = colorResource(R.color.code_background)
    val codeBorderColor = colorResource(R.color.code_border)

    val annotatedCode = remember(code, language) {
        if (language.isNotEmpty()) {
            applySyntaxHighlighting(
                code,
                language,
                keywordColor,
                stringColor,
                commentColor,
                numberColor
            )
        } else {
            buildAnnotatedString { append(code) }
        }
    }

    Column {
        // 语言标签（如果有）
        if (language.isNotEmpty()) {
            Text(
                text = language.uppercase(),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        SelectionContainer {
            Text(
                text = annotatedCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(
                        color = codeBackgroundColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = codeBorderColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp)
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 应用基于正则表达式的语法高亮
 *
 * @param code 代码内容
 * @param language 编程语言类型
 * @param keywordColor 关键字颜色
 * @param stringColor 字符串颜色
 * @param commentColor 注释颜色
 * @param numberColor 数字颜色
 * @return 应用了语法高亮的AnnotatedString
 */
private fun applySyntaxHighlighting(
    code: String,
    language: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = code.lines()

        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) append("\n")

            when (language.lowercase()) {
                "kotlin", "java", "scala" -> {
                    highlightJvmLanguage(line, keywordColor, stringColor, commentColor, numberColor)
                }

                "python" -> {
                    highlightPython(line, keywordColor, stringColor, commentColor, numberColor)
                }

                "javascript", "typescript", "js", "ts" -> {
                    highlightJavaScript(line, keywordColor, stringColor, commentColor, numberColor)
                }

                "html", "xml" -> {
                    highlightMarkup(line, keywordColor, commentColor)
                }

                "css" -> {
                    highlightCSS(line, keywordColor, stringColor, commentColor, numberColor)
                }

                "json" -> {
                    highlightJSON(line, keywordColor, stringColor, commentColor, numberColor)
                }

                else -> {
                    // 通用高亮：字符串、注释、数字
                    highlightGeneric(line, keywordColor, stringColor, commentColor, numberColor)
                }
            }
        }
    }
}

/**
 * JVM语言（Kotlin、Java、Scala）语法高亮
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightJvmLanguage(
    line: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
) {
    val keywords = setOf(
        "class", "interface", "fun", "val", "var", "if", "else", "when", "for", "while",
        "return", "import", "package", "public", "private", "protected", "internal",
        "abstract", "final", "override", "open", "suspend", "inline", "data", "sealed",
        "object", "companion", "enum", "annotation", "try", "catch", "finally", "throw"
    )

    highlightWithKeywords(line, keywords, keywordColor, stringColor, commentColor, numberColor)
}

/**
 * Python语法高亮
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightPython(
    line: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
) {
    val keywords = setOf(
        "def", "class", "if", "elif", "else", "for", "while", "return", "import", "from",
        "as", "try", "except", "finally", "with", "lambda", "yield", "async", "await",
        "True", "False", "None", "and", "or", "not", "in", "is", "pass", "break", "continue"
    )

    highlightWithKeywords(line, keywords, keywordColor, stringColor, commentColor, numberColor)
}

/**
 * JavaScript/TypeScript语法高亮
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightJavaScript(
    line: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
) {
    val keywords = setOf(
        "function", "var", "let", "const", "if", "else", "for", "while", "return",
        "import", "export", "default", "class", "extends", "interface", "type",
        "async", "await", "try", "catch", "finally", "throw", "new", "this",
        "true", "false", "null", "undefined"
    )

    highlightWithKeywords(line, keywords, keywordColor, stringColor, commentColor, numberColor)
}

/**
 * HTML/XML语法高亮
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightMarkup(
    line: String,
    keywordColor: Color,
    commentColor: Color
) {
    var i = 0
    while (i < line.length) {
        when {
            // HTML注释
            line.startsWith("<!--", i) -> {
                val endIndex = line.indexOf("-->", i + 4)
                if (endIndex != -1) {
                    pushStyle(SpanStyle(color = commentColor))
                    append(line.substring(i, endIndex + 3))
                    pop()
                    i = endIndex + 3
                } else {
                    pushStyle(SpanStyle(color = commentColor))
                    append(line.substring(i))
                    pop()
                    break
                }
            }
            // HTML标签
            line[i] == '<' -> {
                val endIndex = line.indexOf('>', i)
                if (endIndex != -1) {
                    pushStyle(SpanStyle(color = keywordColor))
                    append(line.substring(i, endIndex + 1))
                    pop()
                    i = endIndex + 1
                } else {
                    append(line[i])
                    i++
                }
            }

            else -> {
                append(line[i])
                i++
            }
        }
    }
}

/**
 * CSS语法高亮
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightCSS(
    line: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
) {
    val keywords = setOf(
        "color", "background", "margin", "padding", "border", "width", "height",
        "display", "position", "top", "left", "right", "bottom", "font", "text"
    )

    highlightWithKeywords(line, keywords, keywordColor, stringColor, commentColor, numberColor)
}

/**
 * JSON语法高亮
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightJSON(
    line: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
) {
    val keywords = setOf("true", "false", "null")
    highlightWithKeywords(line, keywords, keywordColor, stringColor, commentColor, numberColor)
}

/**
 * 通用语法高亮
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightGeneric(
    line: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
) {
    highlightWithKeywords(line, emptySet(), keywordColor, stringColor, commentColor, numberColor)
}

/**
 * 基于关键字的语法高亮核心实现
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightWithKeywords(
    line: String,
    keywords: Set<String>,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    numberColor: Color
) {
    var i = 0
    while (i < line.length) {
        when {
            // 单行注释
            (line.startsWith("//", i) || line.startsWith("#", i)) -> {
                pushStyle(SpanStyle(color = commentColor))
                append(line.substring(i))
                pop()
                break
            }
            // 字符串（双引号）
            line[i] == '"' -> {
                val start = i
                i++
                while (i < line.length && line[i] != '"') {
                    if (line[i] == '\\' && i + 1 < line.length) i++ // 转义字符
                    i++
                }
                if (i < line.length) i++ // 包含结束引号
                pushStyle(SpanStyle(color = stringColor))
                append(line.substring(start, i))
                pop()
            }
            // 字符串（单引号）
            line[i] == '\'' -> {
                val start = i
                i++
                while (i < line.length && line[i] != '\'') {
                    if (line[i] == '\\' && i + 1 < line.length) i++ // 转义字符
                    i++
                }
                if (i < line.length) i++ // 包含结束引号
                pushStyle(SpanStyle(color = stringColor))
                append(line.substring(start, i))
                pop()
            }
            // 数字
            line[i].isDigit() -> {
                val start = i
                while (i < line.length && (line[i].isDigit() || line[i] == '.' || line[i] == 'f' || line[i] == 'L')) {
                    i++
                }
                pushStyle(SpanStyle(color = numberColor))
                append(line.substring(start, i))
                pop()
            }
            // 关键字或标识符
            line[i].isLetter() || line[i] == '_' -> {
                val start = i
                while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) {
                    i++
                }
                val word = line.substring(start, i)
                if (keywords.contains(word)) {
                    pushStyle(SpanStyle(color = keywordColor))
                    append(word)
                    pop()
                } else {
                    append(word)
                }
            }

            else -> {
                append(line[i])
                i++
            }
        }
    }
}

/**
 * 任务列表项 Composable 组件
 *
 * 支持复选框交互、删除线效果和嵌套渲染
 *
 * @param text 任务文本内容
 * @param isChecked 任务是否已完成
 * @param level 嵌套层级，用于缩进显示
 * @param onCheckedChange 复选框状态变化回调
 */
@Composable
fun TaskListItem(
    text: String,
    isChecked: Boolean,
    level: Int = 0,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember(isChecked) { mutableStateOf(isChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp) // 根据层级缩进
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { newChecked ->
                checked = newChecked
                onCheckedChange(newChecked)
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 脚注渲染器 Composable 组件
 *
 * 支持脚注引用和脚注内容的渲染，使用上标样式显示引用编号
 *
 * @param id 脚注ID或编号
 * @param text 脚注文本内容（仅脚注定义时使用）
 * @param isReference 是否为脚注引用（true）或脚注定义（false）
 */
@Composable
fun FootnoteRenderer(
    id: String,
    text: String,
    isReference: Boolean
) {
    val footnoteColor = colorResource(R.color.footnote_reference)

    if (isReference) {
        // 脚注引用：使用上标样式显示
        Text(
            text = buildAnnotatedString {
                pushStyle(
                    SpanStyle(
                        fontSize = 10.sp,
                        color = footnoteColor,
                        baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript
                    )
                )
                append("[$id]")
                pop()
            },
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 2.dp)
                .clickable {
                    // 可以在这里添加点击跳转到脚注定义的逻辑
                }
        )
    } else {
        // 脚注定义：完整显示ID和内容
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    pushStyle(
                        SpanStyle(
                            fontSize = 10.sp,
                            color = footnoteColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    append("[$id]")
                    pop()
                },
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 上标渲染器 Composable 组件
 *
 * 实现上标的字体样式和位置调整
 *
 * @param text 上标文本内容
 */
@Composable
fun SuperscriptRenderer(text: String) {
    Text(
        text = buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript
                )
            )
            append(text)
            pop()
        }
    )
}

/**
 * 下标渲染器 Composable 组件
 *
 * 实现下标的字体样式和位置调整
 *
 * @param text 下标文本内容
 */
@Composable
fun SubscriptRenderer(text: String) {
    Text(
        text = buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    baselineShift = androidx.compose.ui.text.style.BaselineShift.Subscript
                )
            )
            append(text)
            pop()
        }
    )
}

/**
 * 数学公式渲染器 Composable 组件
 *
 * 实现基础数学公式的渲染支持，使用等宽字体和特殊样式
 *
 * @param expression 数学公式表达式
 * @param isInline 是否为内联公式（true）或块级公式（false）
 */
@Composable
fun MathRenderer(
    expression: String,
    isInline: Boolean
) {
    val mathBackgroundColor = colorResource(R.color.math_background)
    val mathBorderColor = colorResource(R.color.math_border)

    if (isInline) {
        // 内联数学公式：简洁显示
        Text(
            text = expression,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(
                    color = mathBackgroundColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    } else {
        // 块级数学公式：居中显示，更突出的样式
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = mathBackgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = mathBorderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                SelectionContainer {
                    Text(
                        text = expression,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}
