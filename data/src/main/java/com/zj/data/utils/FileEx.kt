package com.zj.data.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.UUID

fun String.getMimeType(): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl(this)
    return MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension.lowercase(Locale.getDefault()))
}

fun Context.saveImageToAppStorage(uri: Uri): String {
    val appStorageDir = getExternalStorageDir()
    if (!appStorageDir.exists()) {
        appStorageDir.mkdirs()
    }
    val imageFile = File(appStorageDir, uri.getImageName())

    val inputStream: InputStream? = contentResolver.openInputStream(uri)
    inputStream?.use { input ->
        FileOutputStream(imageFile).use { output ->
            input.copyTo(output)
        }
    }

    inputStream?.close()
    return imageFile.path.toString()
}

// Function to get the external storage directory
fun Context.getExternalStorageDir(): File {
    return getExternalFilesDir(null)
        ?: throw IllegalStateException("External storage directory not found")
}

fun Uri?.getImageName(): String {
    // 使用UUID确保每个文件名都是唯一的
    val timestamp = System.currentTimeMillis()
    val uniqueId = UUID.randomUUID().toString().take(8)

    return if (this != null) {
        try {
            // 尝试获取原始扩展名
            val originalFileName = this.lastPathSegment?.substringAfterLast("/", "")
            val extension =
                if (!originalFileName.isNullOrEmpty() && originalFileName.contains(".")) {
                    originalFileName.substringAfterLast(".")
                } else {
                    // 默认扩展名
                    "jpg"
                }

            // 确保扩展名是有效的
            val validExtension = when (extension.lowercase()) {
                "jpg", "jpeg", "png", "gif", "webp" -> extension
                else -> "jpg"
            }

            "image_${timestamp}_${uniqueId}.${validExtension}"
        } catch (_: Exception) {
            "image_${timestamp}_${uniqueId}.jpg"
        }
    } else {
        "image_${timestamp}_${uniqueId}.jpg"
    }
}