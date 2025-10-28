package com.zj.note

import android.app.Application
import com.zj.data.export.GlanceImageLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // DataStore和Coil的初始化已移至Startup Initializers
        GlanceImageLoader.init(applicationContext)
    }
}
