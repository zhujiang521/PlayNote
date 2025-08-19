package com.zj.note

import android.app.Application
import com.zj.data.utils.DataStoreUtils
import com.zj.data.export.GlanceImageLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DataStoreUtils.init(applicationContext)
        GlanceImageLoader.init(applicationContext)
    }

}