package com.zj.ink.widget

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
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle

@Composable
fun RenderMarkdown(content: String) {
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
                            onClick = androidx.glance.appwidget.action.actionStartActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    element.url.toUri()
                                )
                            )
                        )
                    )
                }

                is Image -> {
                    ImageLoader.loadBitmap(element.url)?.let {
                        Image(
                            provider = ImageProvider(it),
                            contentDescription = "Markdown 图片"
                        )
                    }
                }

                is Code -> {
                    Text(
                        text = element.text,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = textColor
                        ),
                        modifier = GlanceModifier.background(gray)
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
                        modifier = GlanceModifier.background(gray)
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
