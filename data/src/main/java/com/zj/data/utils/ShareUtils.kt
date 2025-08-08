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

/**
 * 分享一个文件。
 *
 * @param context 上下文对象，用于获取资源和启动Activity。
 * @param file 要分享的文件对象。
 * @param mimeType 文件的MIME类型，用于指定分享内容的类型。
 * @param description 可选参数，附加的文本描述信息。
 * @return 分享操作是否成功执行（不保证用户实际完成分享）。
 */
suspend fun shareFile(
    context: Context,
    file: File,
    mimeType: String,
    description: String? = null
): Boolean {
    return withContext(Dispatchers.IO) {
        // 检查文件是否存在且可读
        if (!file.exists() || !file.canRead()) {
            withContext(Dispatchers.Main) {
                ToastUtil.showToast(context, context.getString(R.string.file_not_found))
            }
            return@withContext false
        }

        // 使用FileProvider生成安全的Uri
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        withContext(Dispatchers.Main) {
            // 构造分享Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, description)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 创建选择器Intent并添加新任务标志
            val chooserIntent = Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_file_via)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

/**
 * 分享一张图片。
 *
 * @param context 上下文对象，用于获取缓存目录和启动Activity。
 * @param imageUrl 图片的原始URL，用于提取文件名。
 * @param bitmap 要分享的Bitmap对象。
 */
suspend fun shareImage(context: Context, imageUrl: String, bitmap: Bitmap) {
    // 将Bitmap保存为文件
    val file = withContext(Dispatchers.IO) {
        saveBitmapToFile(context, bitmap, imageUrl.extractFileNameFromUrl())
    }
    if (file == null) {
        withContext(Dispatchers.Main.immediate) {
            ToastUtil.showToast(context, context.getString(R.string.down_fail))
        }
        return
    }

    // 获取文件的Uri
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    // 构造分享图片的Intent
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    // 启动分享选择器
    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.share_image_via)
        )
    )
}

/**
 * 分享一段文本。
 *
 * @param context 上下文对象，用于启动Activity。
 * @param title 文本的标题。
 * @param content 要分享的文本内容。
 * @return 分享操作是否成功执行。
 */
suspend fun shareText(
    context: Context,
    title: String,
    content: String
): Boolean {
    return withContext(Dispatchers.Main) {
        try {
            // 构造分享文本的Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, content)
            }

            // 创建分享选择器Intent
            val chooserIntent = Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_file_via)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooserIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToast(context, context.getString(R.string.share_failed))
            false
        }
    }
}

/**
 * 将Bitmap保存为JPEG格式的文件。
 *
 * @param context 上下文对象，用于获取缓存目录。
 * @param bitmap 要保存的Bitmap对象。
 * @param displayName 保存文件的名称。
 * @return 保存成功的文件对象，失败则返回null。
 */
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
