package com.zj.data.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object BitmapUtils {
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // 读取文件内容
    fun readFileContent(context: Activity, uri: Uri, callback: (Bitmap) -> Unit) {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return
        callback(bitmap)
    }

    /**
     * 将Bitmap保存到相册。
     *
     * @param bitmap 要保存的图片的Bitmap对象
     * @param displayName 图片的显示名称
     * @param mimeType 图片的MIME类型，默认为"image/jpeg"
     * @param compressFormat 图片的压缩格式，默认为JPEG
     */
    suspend fun addBitmapToAlbum(
        context: Context,
        bitmap: Bitmap,
        displayName: String,
        mimeType: String = "image/jpeg",
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                } else {
                    put(
                        MediaStore.MediaColumns.DATA,
                        "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$displayName"
                    )
                }
            }
            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(compressFormat, 100, outputStream)
                }
            }
        }
    }


}
