package com.zj.data.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 获取当前设备屏幕的宽度（以像素为单位）
 *
 * 此函数使用Compose的LocalDensity和LocalConfiguration来获取屏幕宽度信息
 * 通过将屏幕宽度从密度独立像素（dp）转换为像素（px），提供了一个与设备无关的屏幕宽度值
 *
 * @return Float 屏幕宽度（以像素为单位）
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun screenWidthHeightPX(): Pair<Float, Float> {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = density.run { configuration.screenWidthDp.dp.toPx() }
    val screenHeightDp = density.run { configuration.screenHeightDp.dp.toPx() }
    return Pair(screenWidthPx, screenHeightDp)
}

@Composable
fun Int.pixelsToDp(): Dp {
    return with(LocalDensity.current) { this@pixelsToDp.toDp() }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun isPad(): Boolean {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    return screenWidthDp > 480.dp
}