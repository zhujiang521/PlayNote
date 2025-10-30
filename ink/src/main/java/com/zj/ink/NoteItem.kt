@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zj.ink

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zj.data.R
import com.zj.data.common.DeleteDialog
import com.zj.data.common.HighlightedText
import com.zj.data.common.SwipeBox
import com.zj.data.common.SwipeBoxControl
import com.zj.data.common.rememberSwipeBoxControl
import com.zj.data.model.Note
import com.zj.data.utils.DateUtils

private val MIN_HEIGHT = 30.dp
private val MAX_HEIGHT = 300.dp
private val ACTION_WIDTH = 100.dp

/**
 * 笔记列表项
 */
@Composable
fun NoteItem(
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    searchQuery: String = "",
    control: SwipeBoxControl = rememberSwipeBoxControl(),
    note: Note
) {
    val showDialog = remember { mutableStateOf(false) }
    SwipeBox(
        control = control,
        modifier = Modifier
            .padding(
                top = dimensionResource(R.dimen.image_screen_horizontal_margin),
                bottom = dimensionResource(R.dimen.image_screen_horizontal_margin),
                end = dimensionResource(R.dimen.screen_horizontal_margin)
            )
            .fillMaxWidth()
            .wrapContentHeight(),
        actionWidth = ACTION_WIDTH,
        endAction = listOf {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .heightIn(min = MIN_HEIGHT, max = MAX_HEIGHT)
                    .padding(horizontal = dimensionResource(R.dimen.image_screen_horizontal_margin))
                    .background(MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.large)
                    .clickable {
                        showDialog.value = true
                        control.center()
                    }
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = dimensionResource(R.dimen.small_text).value.sp
                    )
                )
            }
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // 根据内容高度自动调整
                .heightIn(min = MIN_HEIGHT, max = MAX_HEIGHT), // 限制高度范围 ,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.item_background)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
                    .padding(dimensionResource(R.dimen.item_margin))
            ) {
                HighlightedText(
                    text = note.title,
                    highlight = searchQuery,
                    style = TextStyle(
                        fontSize = dimensionResource(R.dimen.title_text).value.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatTimestamp(note.timestamp),
                    fontSize = dimensionResource(R.dimen.subtitle_text).value.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(4.dp))
                LightweightMarkdownPreview(
                    content = note.content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MIN_HEIGHT, max = MAX_HEIGHT)
                        .clickable { onClick() }
                )
            }
        }
    }
    DeleteDialog(showDialog, note.title, onDelete)
}


@Composable
private fun LightweightMarkdownPreview(
    content: String,
    modifier: Modifier = Modifier
) {
    val annotatedText = remember(content) {
        buildAnnotatedString {
            val previewContent = content.take(600) // 减少预览长度以提高性能
            var index = 0

            while (index < previewContent.length) {
                when {
                    // 代码块：显示提示
                    previewContent.startsWith("```", index) -> {
                        val endIndex = previewContent.indexOf("```", index + 3)
                        if (endIndex != -1) {
                            append("💻 ")
                            index = endIndex + 3
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 图片：显示提示
                    previewContent.startsWith("![", index) -> {
                        val endIndex = previewContent.indexOf(")", index)
                        if (endIndex != -1) {
                            append("📷 ")
                            index = endIndex + 1
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 表格：显示提示
                    previewContent.startsWith("|", index) && index + 1 < previewContent.length -> {
                        // 查找表格行
                        val lineEnd = previewContent.indexOf("\n", index)
                        val tableRow = if (lineEnd != -1) {
                            previewContent.substring(index, lineEnd)
                        } else {
                            previewContent.substring(index)
                        }

                        // 确定是否为表格（包含至少一个|分隔符且不是列表）
                        if (tableRow.count { it == '|' } >= 2 && !tableRow.startsWith("|-")) {
                            append("📊 ")
                            // 跳过整行
                            index = if (lineEnd != -1) lineEnd + 1 else previewContent.length

                            // 检查并跳过表头分隔行（如 |-|-|）
                            if (index < previewContent.length && previewContent.startsWith(
                                    "|-",
                                    index
                                )
                            ) {
                                val separatorLineEnd = previewContent.indexOf("\n", index)
                                index =
                                    if (separatorLineEnd != -1) separatorLineEnd + 1 else previewContent.length
                            }

                            // 跳过剩余的表格行
                            while (index < previewContent.length && previewContent[index] == '|') {
                                val nextLineEnd = previewContent.indexOf("\n", index)
                                index =
                                    if (nextLineEnd != -1) nextLineEnd + 1 else previewContent.length
                            }
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 加粗
                    previewContent.startsWith("**", index) -> {
                        val endIndex = previewContent.indexOf("**", index + 2)
                        if (endIndex != -1) {
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append(previewContent.substring(index + 2, endIndex))
                            pop()
                            index = endIndex + 2
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 斜体（单星号或下划线）
                    (previewContent.startsWith("*", index) && !previewContent.startsWith(
                        "**",
                        index
                    )) ||
                            previewContent.startsWith("_", index) -> {
                        val delimiter = if (previewContent[index] == '*') "*" else "_"
                        val endIndex = previewContent.indexOf(delimiter, index + 1)
                        if (endIndex != -1) {
                            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            append(previewContent.substring(index + 1, endIndex))
                            pop()
                            index = endIndex + 1
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 删除线
                    previewContent.startsWith("~~", index) -> {
                        val endIndex = previewContent.indexOf("~~", index + 2)
                        if (endIndex != -1) {
                            pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                            append(previewContent.substring(index + 2, endIndex))
                            pop()
                            index = endIndex + 2
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 行内代码
                    previewContent.startsWith("`", index) -> {
                        val endIndex = previewContent.indexOf("`", index + 1)
                        if (endIndex != -1) {
                            pushStyle(
                                SpanStyle(
                                    fontFamily = FontFamily.Monospace,
                                    background = Color.LightGray.copy(alpha = 0.3f)
                                )
                            )
                            append(previewContent.substring(index + 1, endIndex))
                            pop()
                            index = endIndex + 1
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 链接
                    previewContent.startsWith("[", index) -> {
                        val textEnd = previewContent.indexOf("]", index)
                        val urlEnd = previewContent.indexOf(")", textEnd)
                        if (textEnd != -1 && urlEnd != -1 && previewContent[textEnd + 1] == '(') {
                            pushStyle(
                                SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            append(previewContent.substring(index + 1, textEnd))
                            pop()
                            index = urlEnd + 1
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 标题标记：移除
                    previewContent.startsWith(
                        "#",
                        index
                    ) && (index == 0 || previewContent[index - 1] == '\n') -> {
                        var hashCount = 0
                        while (index + hashCount < previewContent.length && previewContent[index + hashCount] == '#') {
                            hashCount++
                        }
                        index += hashCount
                        if (index < previewContent.length && previewContent[index] == ' ') {
                            index++
                        }
                    }
                    // 无序列表
                    (previewContent.startsWith("- ", index) || previewContent.startsWith(
                        "* ",
                        index
                    )) &&
                            (index == 0 || previewContent[index - 1] == '\n') -> {
                        append("• ")
                        index += 2
                    }
                    // 有序列表
                    previewContent[index].isDigit() && (index == 0 || previewContent[index - 1] == '\n') -> {
                        var numEnd = index
                        while (numEnd < previewContent.length && previewContent[numEnd].isDigit()) {
                            numEnd++
                        }
                        if (numEnd < previewContent.length && previewContent[numEnd] == '.' &&
                            numEnd + 1 < previewContent.length && previewContent[numEnd + 1] == ' '
                        ) {
                            append(previewContent.substring(index, numEnd + 1))
                            append(" ")
                            index = numEnd + 2
                        } else {
                            append(previewContent[index])
                            index++
                        }
                    }
                    // 引用块
                    previewContent.startsWith(
                        "> ",
                        index
                    ) && (index == 0 || previewContent[index - 1] == '\n') -> {
                        pushStyle(
                            SpanStyle(
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic
                            )
                        )
                        append("┃ ")
                        index += 2
                        // 读取到行尾
                        val lineEnd = previewContent.indexOf('\n', index)
                        if (lineEnd != -1) {
                            append(previewContent.substring(index, lineEnd))
                            pop()
                            index = lineEnd
                        } else {
                            append(previewContent.substring(index))
                            pop()
                            index = previewContent.length
                        }
                    }
                    // 普通字符
                    else -> {
                        append(previewContent[index])
                        index++
                    }
                }
            }
        }
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 6, // 减少最大行数以提高性能
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Preview
@Composable
private fun NoteItemPreview() {
    NoteItem(
        note = Note(title = "title", content = "content"),
    )
}