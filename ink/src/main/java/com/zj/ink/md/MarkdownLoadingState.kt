package com.zj.ink.md

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

/**
 * Markdown 加载骨架屏组件
 *
 * 在 Markdown 内容解析期间显示，提供视觉反馈，减少用户感知的等待时间。
 * 使用 Shimmer 动画效果提升视觉体验。
 *
 * @param modifier 修饰符
 */
@Composable
fun MarkdownLoadingState(
    modifier: Modifier = Modifier
) {
    // Shimmer 动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val shimmerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 标题骨架
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(32.dp)
                .alpha(alpha)
                .background(
                    color = shimmerColor,
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 段落骨架 1
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .alpha(alpha)
                    .background(
                        color = shimmerColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 最后一行短一些
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .alpha(alpha)
                .background(
                    color = shimmerColor,
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 图片占位符
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .alpha(alpha)
                .background(
                    color = shimmerColor,
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 段落骨架 2
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .alpha(alpha)
                    .background(
                        color = shimmerColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(16.dp)
                .alpha(alpha)
                .background(
                    color = shimmerColor,
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}