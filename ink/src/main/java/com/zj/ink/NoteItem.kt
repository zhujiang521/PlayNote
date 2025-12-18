@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zj.ink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import com.zj.data.common.AnimationConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.zj.ink.widget.LightweightMarkdownPreview

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
    var isPressed by remember { mutableStateOf(false) }

    // 按压动画：轻微缩放效果
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )

    // 入场动画
    var isVisible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 6 },
            animationSpec = AnimationConfig.tweenEnter()
        ) + fadeIn(
            animationSpec = AnimationConfig.tweenNormal()
        )
    ) {
        SwipeBox(
            itemId = note.id.toString(),
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
                        .background(
                            MaterialTheme.colorScheme.error,
                            shape = MaterialTheme.shapes.large
                        )
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
                    .scale(scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                tryAwaitRelease()
                            }
                        )
                    }
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // 使用IntrinsicSize实现自适应高度
                    .heightIn(min = MIN_HEIGHT, max = MAX_HEIGHT), // 限制高度范围
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
                            .clickable { onClick() }
                    )
                }
            }

        }
    }
    DeleteDialog(showDialog, note.title, onDelete)
}

@Preview
@Composable
private fun NoteItemPreview() {
    NoteItem(
        note = Note(title = "title", content = "content"),
    )
}