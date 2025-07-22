package com.zj.note

import android.app.Application
import com.zj.data.utils.DataStoreUtils
import com.zj.ink.md.ImageLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DataStoreUtils.init(applicationContext)
        ImageLoader.init(applicationContext)
    }

}