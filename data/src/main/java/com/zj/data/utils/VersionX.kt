package com.zj.data.utils

import android.content.Context

private const val DEFAULT_VERSION_NAME = "1.0.0"

fun Context.versionName(): String {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: DEFAULT_VERSION_NAME
    } catch (e: Exception) {
        DEFAULT_VERSION_NAME
    }
}