package com.zj.data.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.scale
import com.zj.data.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.graphics.createBitmap

/**
 * 图片加载器，用于在 Glance 组件中加载和显示图片
 *
 * 支持多种图片来源：
 * - 网络图片 (URL)
 * - 本地资源 (R.drawable.xxx)
 * - 本地文件 (文件路径)
 *
 * 特性：
 * - 内存缓存 (LruCache)
 * - 图片尺寸压缩优化
 * - 网络状态检测
 * - 超时控制
 * - 错误处理和占位图
 * - 圆角处理
 */
object GlanceImageLoader {

    private const val TAG = "GlanceImageLoader"
    private const val MAX_CACHE_SIZE = 10 * 1024 * 1024 // 10MB
    private val memoryCache = object : LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.allocationByteCount
        }
    }

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * 加载图片并返回 Bitmap（支持多种来源）
     * 支持：
     * - 网络 URL（https://xxx.jpg）
     * - 本地资源（R.drawable.xxx）
     * - 本地文件路径（/sdcard/Pictures/image.jpg）
     * - Uri（file://... / content://...）
     *
     * @param source 图片来源
     * @param width 目标宽度
     * @param height 目标高度
     * @param roundedCorners 是否添加圆角
     */
    suspend fun loadBitmap(
        source: Any,
        width: Int = 400,
        height: Int = 200,
        roundedCorners: Boolean = false
    ): Bitmap {
        val key = "$source-$width-$height-$roundedCorners"

        Log.d(TAG, "loadBitmap: source:$source, roundedCorners:$roundedCorners")

        // 检查内存缓存
        val cached = memoryCache.get(key)
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        // 同步调用，内部使用协程 + 超时控制
        return withContext(Dispatchers.IO) {
            try {
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

                // 处理加载结果
                val resultBitmap = bitmap ?: loadPlaceholderBitmap()

                // 如果需要圆角处理，则应用圆角
                if (roundedCorners && resultBitmap != loadPlaceholderBitmap()) {
                    getRoundedCornerBitmap(resultBitmap) // 8dp 圆角
                } else {
                    resultBitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载图片时发生异常", e)
                loadPlaceholderBitmap()
            }
        }.also { loadedBitmap ->
            // 只有成功加载的图片才放入缓存
            if (loadedBitmap != loadPlaceholderBitmap() && !loadedBitmap.isRecycled) {
                memoryCache.put(key, loadedBitmap)
            }
        }
    }

    // 网络图片加载（使用 HttpURLConnection）
    private fun loadBitmapFromNetwork(urlString: String, width: Int, height: Int): Bitmap? {
        if (!isNetworkAvailable()) {
            return null
        }
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
                doInput = true
                useCaches = true
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    val size = calculateImageSize(bitmap.width, bitmap.height, width, height)
                    bitmap.scale(size.first, size.second, filter = true)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "网络图片加载失败: $urlString", e)
            null
        } finally {
            connection?.disconnect()
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
            val options = BitmapFactory.Options()
            // 先获取图片尺寸信息
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, options)

            // 计算缩放比例
            options.inSampleSize = calculateInSampleSize(options, width, height)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            val size = calculateImageSize(bitmap.width, bitmap.height, width, height)
            bitmap.scale(size.first, size.second, filter = true)
        } catch (e: Exception) {
            Log.e(TAG, "文件加载失败: $filePath", e)
            null
        }
    }

    // 资源 ID 加载
    private fun loadBitmapFromResource(resId: Int, width: Int, height: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            // 先获取图片尺寸信息
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(appContext.resources, resId, options)

            // 计算缩放比例
            options.inSampleSize = calculateInSampleSize(options, width, height)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeResource(appContext.resources, resId, options)
            val size = calculateImageSize(bitmap.width, bitmap.height, width, height)
            bitmap.scale(size.first, size.second, filter = true)
        } catch (e: Exception) {
            Log.e(TAG, "资源加载失败: $resId", e)
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun calculateImageSize(
        width: Int, height: Int, reqWidth: Int, reqHeight: Int
    ): Pair<Int, Int> {
        // 如果原图尺寸小于等于目标尺寸，则不需要缩放
        if (width <= reqWidth && height <= reqHeight) {
            return Pair(width, height)
        }

        // 保持宽高比的缩放
        val widthRatio = width.toFloat() / reqWidth
        val heightRatio = height.toFloat() / reqHeight
        val scaleRatio = maxOf(widthRatio, heightRatio)

        val scaledWidth = (width / scaleRatio).toInt()
        val scaledHeight = (height / scaleRatio).toInt()

        return Pair(scaledWidth, scaledHeight)
    }

    // 检查网络是否可用
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // 加载占位图
    private fun loadPlaceholderBitmap(): Bitmap {
        return BitmapFactory.decodeResource(appContext.resources, R.drawable.ic_placeholder)
    }

    // 为 Bitmap 添加圆角
    private fun getRoundedCornerBitmap(bitmap: Bitmap): Bitmap {
        val cornerRadius = 10f
        val output = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = -0xbdbdbe // 设置为灰色
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

}