package com.zj.ink.md

import android.app.Application
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zj.data.R
import com.zj.data.utils.ToastUtil
import com.zj.data.utils.extractFileNameFromUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * 图片的ViewModel，负责获取图片和处理与图片相关的操作。
 */
@HiltViewModel
class ImageViewModel @Inject constructor(private val application: Application) :
    AndroidViewModel(application) {

    /**
     * 下载图片到相册。
     *
     * @param imageUrl 图片的URL地址
     * @param bitmap 要下载的图片的Bitmap对象
     */
    fun downloadImage(imageUrl: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                addBitmapToAlbum(application, bitmap, imageUrl.extractFileNameFromUrl())
                showToast(R.string.down_success)
            } catch (_: Exception) {
                showToast(R.string.down_fail)
            }
        }
    }

    /**
     * 将图片设置为壁纸。
     *
     * @param bitmap 要设置为壁纸的图片的Bitmap对象
     */
    fun setAsWallpaper(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(application)
                wallpaperManager.setBitmap(bitmap)
                showToast(R.string.wallpaper_success)
            } catch (_: IOException) {
                showToast(R.string.wallpaper_fail)
            } catch (_: Exception) {
                showToast(R.string.down_fail)
            }
        }
    }

    private suspend fun showToast(resId: Int) {
        withContext(Dispatchers.Main.immediate) {
            ToastUtil.showToast(application, application.getString(resId))
        }
    }

    /**
     * 将Bitmap保存到相册。
     *
     * @param context 上下文对象
     * @param bitmap 要保存的图片的Bitmap对象
     * @param displayName 图片的显示名称
     * @param mimeType 图片的MIME类型，默认为"image/jpeg"
     * @param compressFormat 图片的压缩格式，默认为JPEG
     */
    private suspend fun addBitmapToAlbum(
        context: Context,
        bitmap: Bitmap,
        displayName: String,
        mimeType: String = "image/jpeg",
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ) {
        withContext(Dispatchers.IO) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                }
                
                val contentResolver = context.contentResolver
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(compressFormat, 100, outputStream)
                    }
                } ?: throw IOException("Failed to insert image into MediaStore")
            } catch (e: Exception) {
                throw IOException("Failed to save image to album", e)
            }
        }
    }
}