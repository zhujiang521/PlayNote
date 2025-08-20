@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zj.ink.md

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    onImageClick: (String) -> Unit = {}
) {
    val elements = remember(markdown) {
        MarkdownParser.parse(markdown)
    }
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()) // 添加垂直滚动
    ) {
        elements.forEach { element ->
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
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(element.url)
                                    .size(ORIGINAL)
                                    .build(),
                                modifier = Modifier
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
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                    }
                                },
                                error = {
                                    Image(
                                        painterResource(R.drawable.ic_placeholder),
                                        contentDescription = stringResource(R.string.down_fail)
                                    )
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
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                }
                            },
                            error = {
                                Image(
                                    painterResource(R.drawable.ic_placeholder_big),
                                    contentDescription = stringResource(R.string.down_fail)
                                )
                            },
                        )

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
                    SelectionContainer {
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
                            .border(0.5.dp, colorResource(R.color.divider))
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
                                        .background(colorResource(R.color.edit_background))
                                        .border(0.5.dp, colorResource(R.color.divider))
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
                                                colorResource(R.color.divider)
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
