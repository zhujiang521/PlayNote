package com.zj.data.utils

import java.net.URL
import kotlin.collections.lastOrNull
import kotlin.takeIf
import kotlin.text.isNotBlank
import kotlin.text.split

private const val DEFAULT_IMAGE_NAME = "play_ai_image"

fun String.extractFileNameFromUrl(): String {
    return try {
        val url = URL(this)
        val path = url.path
        val segments = path.split("/")
        segments.lastOrNull()?.takeIf { it.isNotBlank() } ?: DEFAULT_IMAGE_NAME
    } catch (e: Exception) {
        e.printStackTrace()
        DEFAULT_IMAGE_NAME
    }
}