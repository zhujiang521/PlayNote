package com.zj.data.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing

/**
 * 应用统一动画配置
 * 提供现代化、流畅的动画效果
 */
object AnimationConfig {

    // ============ 动画时长 ============

    /** 快速动画：150ms - 用于简单的状态切换 */
    const val DURATION_FAST = 150

    /** 标准动画：250ms - 用于大部分UI交互 */
    const val DURATION_NORMAL = 250

    /** 中等动画：350ms - 用于页面转场 */
    const val DURATION_MEDIUM = 350

    /** 慢速动画：450ms - 用于复杂转场 */
    const val DURATION_SLOW = 450

    // ============ Spring弹性动画 ============

    /** 快速弹性：高阻尼，快速到位，几乎无弹跳 */
    val springFast = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    /** 标准弹性：适中阻尼，轻微弹性 */
    val springNormal = spring<Float>(
        dampingRatio = 0.8f,  // 自定义阻尼比，介于NoBouncy和LowBouncy之间
        stiffness = Spring.StiffnessMedium
    )

    /** 柔和弹性：低阻尼，柔和过渡 */
    val springSoft = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessLow
    )

    // ============ Tween缓动动画 ============

    /** 快速淡入淡出 - 用于透明度动画 */
    fun <T> tweenFast() = tween<T>(
        durationMillis = DURATION_FAST,
        easing = FastOutSlowInEasing
    )

    /** 标准缓动 - 用于通用动画 */
    fun <T> tweenNormal() = tween<T>(
        durationMillis = DURATION_NORMAL,
        easing = EaseInOutCubic
    )

    /** 流畅进入 - 用于进入动画 */
    fun <T> tweenEnter() = tween<T>(
        durationMillis = DURATION_MEDIUM,
        easing = EaseOutCubic
    )

    /** 快速退出 - 用于退出动画 */
    fun <T> tweenExit() = tween<T>(
        durationMillis = DURATION_FAST,
        easing = LinearEasing
    )

    // ============ 页面转场动画 ============

    /** 页面进入动画时长 */
    const val PAGE_ENTER_DURATION = DURATION_MEDIUM

    /** 页面退出动画时长 */
    const val PAGE_EXIT_DURATION = DURATION_FAST

    // ============ 弹窗动画 ============

    /** 弹窗出现动画时长 */
    const val DIALOG_ENTER_DURATION = DURATION_NORMAL

    /** 弹窗消失动画时长 */
    const val DIALOG_EXIT_DURATION = DURATION_FAST

    /** 弹窗初始缩放比例 */
    const val DIALOG_INITIAL_SCALE = 0.8f

    /** 弹窗初始透明度 */
    const val DIALOG_INITIAL_ALPHA = 0f

    // ============ 列表动画 ============

    /** 列表项动画时长 */
    const val LIST_ITEM_DURATION = DURATION_FAST

    /** 滑动删除动画时长 */
    const val SWIPE_DURATION = DURATION_NORMAL

    // ============ 按钮反馈 ============

    /** 按钮按压缩放比例 */
    const val BUTTON_PRESSED_SCALE = 0.95f

    /** 按钮反馈动画时长 */
    const val BUTTON_FEEDBACK_DURATION = 100
}