// ImageLoader.kt
package com.zj.ink.md

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.zj.data.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.graphics.scale

object ImageLoader {

    private const val TAG = "ImageLoader"
    private const val MAX_CACHE_SIZE = 10 * 1024 * 1024 // 10MB
    private val memoryCache = object : LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context
    }

    /**
     * 加载图片并返回 Bitmap（支持多种来源）
     * 支持：
     * - 网络 URL（https://xxx.jpg）
     * - 本地资源（R.drawable.xxx）
     * - 本地文件路径（/sdcard/Pictures/image.jpg）
     * - Uri（file://... / content://...）
     */
    fun loadBitmap(
        source: Any, width: Int = 400, height: Int = 200
    ): Bitmap? {
        val key = "$source-$width-$height"

        Log.d(TAG, "loadBitmap: source:$source")

        // 检查内存缓存
        val cached = memoryCache.get(key)
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        // 同步调用，内部使用协程 + 超时控制
        return runBlocking(Dispatchers.IO) {
            val bitmap = withTimeoutOrNull(3000) { // 3秒超时
                when (source) {
                    is String -> {
                        if (source.startsWith("http")) {
                            loadBitmapFromNetwork(source, width, height)
                        } else if (source.startsWith("file://")) {
                            loadBitmapFromFile(source.removePrefix("file://"), width, height)
                        } else {
                            loadBitmapFromFile(source, width, height)
                        }
                    }

                    is Int -> {
                        loadBitmapFromResource(source, width, height)
                    }

                    is File -> {
                        loadBitmapFromFile(source.absolutePath, width, height)
                    }

                    else -> {
                        null
                    }
                }
            }

            bitmap ?: BitmapFactory.decodeResource(
                appContext.resources, R.drawable.baseline_color_lens
            )
        }
    }

    // 网络图片加载（使用 HttpURLConnection）
    private fun loadBitmapFromNetwork(urlString: String, width: Int, height: Int): Bitmap? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.useCaches = true
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()
                val size =
                    calculateImageSize(bitmap.width, bitmap.height, width, height)
                bitmap?.scale(size.first, size.second)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "网络图片加载失败: $urlString", e)
            null
        }
    }

    // 本地文件加载
    private fun loadBitmapFromFile(filePath: String, width: Int, height: Int): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "文件不存在: $filePath")
            return null
        }

        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val size =
                calculateImageSize(bitmap.width, bitmap.height, width, height)
            bitmap?.scale(size.first, size.second)
        } catch (e: Exception) {
            Log.e(TAG, "文件加载失败: $filePath", e)
            null
        }
    }

    // 资源 ID 加载
    private fun loadBitmapFromResource(resId: Int, width: Int, height: Int): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeResource(appContext.resources, resId)
            val size =
                calculateImageSize(bitmap.width, bitmap.height, width, height)
            bitmap?.scale(size.first, size.second)
        } catch (e: Exception) {
            Log.e(TAG, "资源加载失败: $resId", e)
            null
        }
    }

    private fun calculateImageSize(
        width: Int, height: Int, reqWidth: Int, reqHeight: Int
    ): Pair<Int, Int> {
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfWidth = width / 2
            val halfHeight = height / 2

            while ((halfWidth / inSampleSize) >= reqWidth &&
                (halfHeight / inSampleSize) >= reqHeight
            ) {
                inSampleSize *= 2
            }
        }

        val scaledWidth = width / inSampleSize
        val scaledHeight = height / inSampleSize

        return Pair(scaledWidth, scaledHeight)
    }

}
