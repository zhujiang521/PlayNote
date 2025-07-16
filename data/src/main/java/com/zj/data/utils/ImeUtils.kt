package com.zj.data.utils

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.ime

/**
 * 隐藏ime
 */
fun Activity?.hideIme() {
    if (this == null || window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.hide(ime())
}

/**
 * 显示ime
 */
fun Activity?.showIme() {
    if (this == null || window == null) return
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.show(ime())
}
