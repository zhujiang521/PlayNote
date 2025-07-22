@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zj.ink.md

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun RenderMarkdown(
    markdown: String,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    onImageClick: (String) -> Unit = {}
) {
    val elements = MarkdownParser.parse(markdown)

    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(elements) { element ->
            when (element) {
                is Heading -> {
                    Text(
                        text = element.text,
                        fontSize = when (element.level) {
                            1 -> 24.sp
                            2 -> 20.sp
                            else -> 18.sp
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

                is Paragraph -> {
                    Text(
                        text = element.text,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                is Link -> {
                    val uriHandler = LocalUriHandler.current
                    ClickableText(
                        text = buildAnnotatedString {
                            append(element.text)
                            addStyle(
                                style = SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                                ),
                                start = 0,
                                end = element.text.length
                            )
                        },
                        onClick = {
                            uriHandler.openUri(element.url)
                        }
                    )
                }

                is Image -> {
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            AsyncImage(
                                model = element.url,
                                contentDescription = "Markdown 图片",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .sharedElement(
                                        sharedTransitionScope.rememberSharedContentState(key = "image-${element.url}"),
                                        animatedVisibilityScope = animatedContentScope,
                                    )
                                    .clickable {
                                        onImageClick(element.url)
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        AsyncImage(
                            model = element.url,
                            contentDescription = "Markdown 图片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                is Code -> {
                    Text(
                        text = element.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp)
                    )
                }

                is CodeBlock -> {
                    Text(
                        text = element.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }

                is BlockQuote -> {
                    Text(
                        text = "“${element.text}”",
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                    )
                }

                is UnorderedList -> {
                    element.items.forEach { item ->
                        Row {
                            Text("• ", fontSize = 14.sp)
                            Text(item, fontSize = 14.sp)
                        }
                    }
                }

                is OrderedList -> {
                    element.items.forEachIndexed { index, item ->
                        Row {
                            Text("${index + 1}. ", fontSize = 14.sp)
                            Text(item, fontSize = 14.sp)
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, colorResource(com.zj.data.R.color.divider))
                            .padding(0.5.dp)
                    ) {
                        // 表头
                        Row(modifier = Modifier.fillMaxWidth()) {
                            element.headers.forEach { header ->
                                Text(
                                    text = header,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .weight(weight)
                                        .background(colorResource(com.zj.data.R.color.edit_background))
                                        .border(0.5.dp, colorResource(com.zj.data.R.color.divider))
                                        .padding(tableCellPadding)
                                )
                            }
                        }

                        // 表格行
                        element.rows.forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                row.forEach { cell ->
                                    Text(
                                        text = cell,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .weight(weight)
                                            .border(
                                                0.5.dp,
                                                colorResource(com.zj.data.R.color.divider)
                                            )
                                            .padding(tableCellPadding)
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
