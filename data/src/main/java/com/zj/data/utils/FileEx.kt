package com.zj.data.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale

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
    return if (this != null) {
        lastPathSegment?.filter { it.isDigit() }?.takeLast(10) ?: (1000..99999).random()
            .toString()
    } else {
        (1000..99999).random().toString()
    } + ".jpg"
}