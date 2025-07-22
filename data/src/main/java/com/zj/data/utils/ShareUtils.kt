package com.zj.data.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.zj.data.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun shareFile(
    context: Context,
    file: File,
    mimeType: String,
    description: String? = null
): Boolean {
    return withContext(Dispatchers.IO) {
        if (!file.exists() || !file.canRead()) {
            withContext(Dispatchers.Main) {
                ToastUtil.showToast(context, context.getString(R.string.file_not_found))
            }
            return@withContext false
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        withContext(Dispatchers.Main) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, description)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_file_via)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // 为 chooser Intent 添加标志
            }

            try {
                context.startActivity(chooserIntent)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToast(context, context.getString(R.string.share_failed))
                false
            }
        }
    }
}

suspend fun shareImage(context: Context, imageUrl: String, bitmap: Bitmap) {
    val file = withContext(Dispatchers.IO) {
        saveBitmapToFile(context, bitmap, imageUrl.extractFileNameFromUrl())
    }
    if (file == null) {
        withContext(Dispatchers.Main.immediate) {
            ToastUtil.showToast(context, context.getString(R.string.down_fail))
        }
        return
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.share_image_via)
        )
    )
}

suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap, displayName: String): File? {
    return withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, displayName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}