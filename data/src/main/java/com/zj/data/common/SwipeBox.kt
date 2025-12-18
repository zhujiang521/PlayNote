package com.zj.data.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeBox(
    modifier: Modifier = Modifier,
    itemId: String,
    control: SwipeBoxControl = rememberSwipeBoxControl(),
    actionWidth: Dp,
    endAction: List<@Composable BoxScope.() -> Unit> = listOf(),
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val actionWidthPx = with(density) {
        actionWidth.toPx()
    }
    val endWidth = actionWidthPx * endAction.size
    var contentWidth by remember { mutableFloatStateOf(0f) }
    var contentHeight by remember { mutableFloatStateOf(0f) }
    val state = remember(endWidth, contentWidth) {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                DragAnchors.Center at 0f
                DragAnchors.End at -endWidth
            }
        )
    }


    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state,
        positionalThreshold = { distance -> distance * 0.5f },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
    )

    // 监听状态变化
    LaunchedEffect(state.currentValue, itemId) {
        println("SwipeBox[$itemId] 状态变化: ${state.currentValue}")
        when (state.currentValue) {
            DragAnchors.End -> {
                println("SwipeBox[$itemId] 设置为展开状态")
                SwipeBoxGlobalState.setExpanded(itemId)
            }

            DragAnchors.Center -> {
                if (SwipeBoxGlobalState.currentExpandedId.value == itemId) {
                    println("SwipeBox[$itemId] 清除展开状态")
                    SwipeBoxGlobalState.clearExpanded()
                }
            }
        }
    }

    LaunchedEffect(control, state, itemId) {
        with(control) {
            handleControlEvents(
                onCenter = {
                    scope.launch {
                        state.animateTo(DragAnchors.Center)
                    }
                },
                onEnd = {
                    scope.launch {
                        state.animateTo(DragAnchors.End)
                    }
                }
            )
        }
    }
    Box(
        modifier = modifier
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal,
                flingBehavior = flingBehavior,
                enabled = SwipeBoxGlobalState.canStartSwipe(itemId)
            )
            .clipToBounds()
    ) {
        endAction.forEachIndexed { index, action ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(actionWidth)
                    .height(with(density) {
                        contentHeight.toDp()
                    })
                    .offset {
                        IntOffset(
                            x = (actionWidthPx * (index + 1) + state.offset).roundToInt(),
                            y = 0,
                        )
                    }
            ) {
                action()
            }
        }
        Box(
            modifier = Modifier
                .onSizeChanged {
                    contentWidth = it.width.toFloat()
                    contentHeight = it.height.toFloat()
                }
                .offset {
                    IntOffset(
                        x = state.offset.roundToInt(),
                        y = 0,
                    )
                }
        ) {
            content()
        }
    }
}

@Stable
class SwipeBoxControl(
    private val scope: CoroutineScope
) {
    private sealed interface ControlEvent {
        data object Center : ControlEvent
        data object End : ControlEvent
    }

    private val controlEvents: MutableSharedFlow<ControlEvent> = MutableSharedFlow()

    @OptIn(FlowPreview::class)
    internal suspend fun handleControlEvents(
        onCenter: () -> Unit = {},
        onEnd: () -> Unit = {},
    ) = withContext(Dispatchers.Main) {
        controlEvents.debounce(100).collect { event ->
            when (event) {
                ControlEvent.Center -> onCenter()
                ControlEvent.End -> onEnd()
            }
        }
    }

    fun center() {
        scope.launch { controlEvents.emit(ControlEvent.Center) }
    }

    fun end() {
        scope.launch { controlEvents.emit(ControlEvent.End) }
    }
}

@Composable
fun rememberSwipeBoxControl(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): SwipeBoxControl = remember(coroutineScope) { SwipeBoxControl(coroutineScope) }

enum class DragAnchors {
    Center,
    End,
}