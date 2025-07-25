package com.zj.data.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.zj.data.R

/**
 * 显示带有高亮的文本
 *
 * @param modifier UI 修改器，用于定义组件的布局属性
 * @param text 要显示的原始文本
 * @param highlight 需要高亮的关键词
 * @param style 文本样式，默认为 MaterialTheme.typography.bodyMedium
 * @param maxLines 文本显示的最大行数，默认为 Int.MAX_VALUE
 */
@Composable
fun HighlightedText(
    modifier: Modifier = Modifier,
    text: String,
    highlight: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE
) {
    if (highlight.isEmpty()) {
        Text(text = text, style = style, modifier = modifier, maxLines = maxLines)
        return
    }

    val parts = text.split(highlight, ignoreCase = true)
    val indexes = mutableListOf<Int>()
    var index = text.indexOf(highlight, ignoreCase = true)

    while (index >= 0) {
        indexes.add(index)
        index = text.indexOf(highlight, index + 1, ignoreCase = true)
    }

    val annotatedString = buildAnnotatedString {
        append(parts[0])
        var previousIndex = 0

        for (i in indexes.indices) {
            val matchStart = indexes[i]
            val matchEnd = matchStart + highlight.length

            append(text.substring(previousIndex + parts[i].length, matchEnd))
            addStyle(
                style = SpanStyle(
                    color = colorResource(R.color.primary),
                    fontWeight = FontWeight.Bold
                ),
                start = length - highlight.length,
                end = length
            )
            previousIndex = matchEnd
            if (i + 1 < parts.size) {
                append(parts[i + 1])
            }
        }
    }

    Text(text = annotatedString, style = style, modifier = modifier, maxLines = maxLines)
}
