package com.zj.note

import android.app.Application
import coil.ImageLoader
import coil.memory.MemoryCache
import coil.disk.DiskCache
import com.zj.data.utils.DataStoreUtils
import com.zj.data.export.GlanceImageLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DataStoreUtils.init(applicationContext)
        GlanceImageLoader.init(applicationContext)
        configureImageLoader()
    }

    private fun configureImageLoader() {
        ImageLoader.Builder(this)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
}