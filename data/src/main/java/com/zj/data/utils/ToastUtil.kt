package com.zj.data.utils

import android.content.Context
import android.widget.Toast

object ToastUtil {
    private var toast: Toast? = null

    fun showToast(context: Context?, message: String, duration: Int = Toast.LENGTH_SHORT) {
        toast?.cancel() // 取消之前的 Toast
        toast = Toast.makeText(context, message, duration)
        toast?.show()
    }
}
