package com.zj.data.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 为任何可组合项添加按压反馈动画
 *
 * @param enabled 是否启用动画，默认为true
 * @param pressScale 按下时的缩放比例，默认为0.95f
 * @param onPress 按下时的回调
 * @param onRelease 释放时的回调
 *
 * 使用示例：
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .pressAnimation()
 *         .clickable { /* 点击事件 */ }
 * )
 * ```
 */
fun Modifier.pressAnimation(
    enabled: Boolean = true,
    pressScale: Float = AnimationConfig.BUTTON_PRESSED_SCALE,
    onPress: (() -> Unit)? = null,
    onRelease: (() -> Unit)? = null
): Modifier = composed {
    if (!enabled) return@composed this

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )

    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    onPress?.invoke()
                    tryAwaitRelease()
                    isPressed = false
                    onRelease?.invoke()
                }
            )
        }
}

/**
 * 为IconButton等按钮组件添加轻微的按压反馈
 *
 * 使用示例：
 * ```kotlin
 * IconButton(
 *     onClick = { },
 *     modifier = Modifier.buttonPressAnimation()
 * ) {
 *     Icon(...)
 * }
 * ```
 */
fun Modifier.buttonPressAnimation(
    enabled: Boolean = true
): Modifier = pressAnimation(
    enabled = enabled,
    pressScale = AnimationConfig.BUTTON_PRESSED_SCALE
)

/**
 * 为Card等卡片组件添加更明显的按压反馈
 *
 * 使用示例：
 * ```kotlin
 * Card(
 *     modifier = Modifier.cardPressAnimation()
 * ) {
 *     // 内容
 * }
 * ```
 */
fun Modifier.cardPressAnimation(
    enabled: Boolean = true
): Modifier = pressAnimation(
    enabled = enabled,
    pressScale = 0.98f
)