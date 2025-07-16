package com.zj.data.utils

import android.app.Activity
import android.content.res.Configuration
import android.view.View

fun Activity.setStatusBarInv() {
    // 判断是否为深色模式
    val isDarkMode =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    // 设置状态栏反色
    val window = window
    val decorView = window.decorView

    if (isDarkMode) {
        decorView.systemUiVisibility =
            decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    } else {
        decorView.systemUiVisibility =
            decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}