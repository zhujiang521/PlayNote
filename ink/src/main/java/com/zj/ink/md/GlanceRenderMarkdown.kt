package com.zj.ink.md

import android.content.Intent
import androidx.compose.runtime.Composable
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
import com.zj.data.utils.GlanceImageLoader
import com.zj.ink.widget.gray
import com.zj.ink.widget.textColor

/**
 * 在 Glance 组件中渲染 Markdown 内容
 *
 * @param content 需要渲染的 Markdown 字符串内容
 */
@Composable
fun GlanceRenderMarkdown(content: String) {
    val elements = MarkdownParser.parse(content).take(10)

    Column {
        elements.forEach { element ->
            when (element) {
                is Heading -> {
                    Text(
                        text = element.text,
                        style = TextStyle(
                            fontSize = when (element.level) {
                                1 -> 20.sp
                                2 -> 18.sp
                                else -> 16.sp
                            },
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                is Bold -> {
                    Text(
                        text = element.text,
                        style = TextStyle(fontWeight = FontWeight.Bold, color = textColor)
                    )
                }

                is Italic -> {
                    Text(
                        text = element.text,
                        style = TextStyle(fontStyle = FontStyle.Italic, color = textColor)
                    )
                }

                is Strikethrough -> {
                    Text(
                        text = element.text,
                        style = TextStyle(
                            textDecoration = TextDecoration.LineThrough,
                            color = textColor,
                        )
                    )
                }

                is Paragraph -> {
                    Text(
                        text = element.text,
                        style = TextStyle(fontSize = 12.sp, color = textColor)
                    )
                }

                is Link -> {
                    Text(
                        text = element.text,
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
                                    element.url.toUri()
                                )
                            )
                        )
                    )
                }

                is Image -> {
                    Image(
                        provider = ImageProvider(GlanceImageLoader.loadBitmap(element.url)),
                        contentDescription = "Markdown 图片"
                    )
                }

                is Code -> {
                    Text(
                        text = element.text,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = textColor
                        ),
                        modifier = GlanceModifier.Companion.background(gray)
                            .padding(vertical = 5.dp)
                    )
                }

                is CodeBlock -> {
                    Text(
                        text = element.text,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = textColor,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = GlanceModifier.Companion.background(gray)
                            .padding(vertical = 5.dp)
                    )
                }

                is BlockQuote -> {
                    Text(
                        text = "“${element.text}”",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = gray
                        ),
                        modifier = GlanceModifier.padding(vertical = 5.dp)
                    )
                }

                is UnorderedList -> {
                    Column {
                        element.items.forEach { item ->
                            Row {
                                Text(
                                    text = "•",
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
                            Row {
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
                    val tableCellPadding = 4.dp
                    Column(
                        modifier = GlanceModifier.fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {

                        Box(modifier = GlanceModifier.height(5.dp)) { }

                        Column(
                            modifier = GlanceModifier.fillMaxWidth()
                                .background(ImageProvider(R.drawable.ic_table_header_background))
                                .padding(1.dp)
                        ) {

                            // 表头
                            Row(modifier = GlanceModifier.fillMaxWidth()) {
                                element.headers.forEach { header ->
                                    Text(
                                        text = header,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = textColor
                                        ),
                                        modifier = GlanceModifier
                                            .defaultWeight()
                                            .background(ImageProvider(R.drawable.ic_table_header_background))
                                            .padding(tableCellPadding)
                                    )
                                }
                            }

                            // 表格行
                            element.rows.forEach { row ->
                                Row(modifier = GlanceModifier.fillMaxWidth()) {
                                    row.forEach { cell ->
                                        Text(
                                            text = cell,
                                            style = TextStyle(fontSize = 12.sp, color = textColor),
                                            modifier = GlanceModifier
                                                .defaultWeight()
                                                .background(ImageProvider(R.drawable.ic_table_background))
                                                .padding(tableCellPadding)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Box(modifier = GlanceModifier.height(5.dp)) { }
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
    }
}
