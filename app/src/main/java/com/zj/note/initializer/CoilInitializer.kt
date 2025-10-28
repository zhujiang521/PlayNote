package com.zj.note.initializer

import android.content.Context
import androidx.startup.Initializer
import coil.ImageLoader
import coil.memory.MemoryCache
import coil.disk.DiskCache

/**
 * 用于初始化Coil图片加载库的Startup Initializer
 */
class CoilInitializer : Initializer<ImageLoader> {
    override fun create(context: Context): ImageLoader {
        val imageLoader = ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
        
        return imageLoader
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}