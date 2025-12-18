package com.zj.ink.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zj.data.R

/**
 * 增强版Markdown编辑器
 * 功能特性：
 * - 行号显示（可选）
 * - 当前行高亮（可选）
 * - 完全兼容原BasicTextField API
 * - 零性能损耗（不启用增强功能时）
 */
@Composable
fun EnhancedMarkdownEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    showLineNumbers: Boolean = true,
    highlightCurrentLine: Boolean = true,
) {
    // 如果不启用任何增强功能，直接使用原生BasicTextField，零性能损耗
    if (!showLineNumbers && !highlightCurrentLine) {
        Box(modifier = modifier) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = textStyle.copy(color = colorResource(R.color.text_color)),
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = maxLines,
                cursorBrush = SolidColor(colorResource(R.color.cursor_color)),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle.copy(color = Color.Gray),
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        return
    }

    // 启用增强功能时的实现
    val currentLine = remember(value.selection.start, value.text) {
        if (highlightCurrentLine) {
            value.text.substring(0, value.selection.start.coerceAtMost(value.text.length))
                .count { it == '\n' } + 1
        } else 0
    }

    val totalLines = remember(value.text) {
        if (showLineNumbers) value.text.count { it == '\n' } + 1 else 0
    }

    Row(modifier = modifier) {
        // 行号列
        if (showLineNumbers) {
            LineNumberColumn(
                currentLine = currentLine,
                totalLines = totalLines,
                highlightCurrentLine = highlightCurrentLine,
                textStyle = textStyle,
                modifier = Modifier
                    .fillMaxHeight()
                    .background(colorResource(R.color.line_number_background))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )

            // 分隔线
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(colorResource(R.color.divider))
            )
        }

        // 编辑器区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                textStyle = textStyle.copy(color = colorResource(R.color.text_color)),
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = maxLines,
                cursorBrush = SolidColor(colorResource(R.color.cursor_color)),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle.copy(color = Color.Gray),
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * 行号列组件
 * 采用轻量级实现，避免过度渲染
 */
@Composable
private fun LineNumberColumn(
    currentLine: Int,
    totalLines: Int,
    highlightCurrentLine: Boolean,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 限制最大显示行数，避免大文档性能问题
        val maxDisplayLines = minOf(totalLines, 10000)

        for (lineNumber in 1..maxDisplayLines) {
            val isCurrentLine = highlightCurrentLine && lineNumber == currentLine

            Text(
                text = lineNumber.toString(),
                style = textStyle.copy(
                    fontSize = (textStyle.fontSize.value * 0.85f).sp,
                    color = if (isCurrentLine) {
                        colorResource(R.color.line_number_current)
                    } else {
                        colorResource(R.color.line_number_default)
                    },
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .then(
                        if (isCurrentLine) {
                            Modifier
                                .background(
                                    colorResource(R.color.current_line_highlight),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        } else {
                            Modifier.padding(horizontal = 2.dp)
                        }
                    )
            )
        }

        // 如果超过限制，显示省略提示
        if (totalLines > maxDisplayLines) {
            Text(
                text = "...",
                style = textStyle.copy(
                    fontSize = (textStyle.fontSize.value * 0.85f).sp,
                    color = colorResource(R.color.line_number_default)
                )
            )
        }
    }
}
