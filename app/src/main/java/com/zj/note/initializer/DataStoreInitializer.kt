package com.zj.note.initializer

import android.content.Context
import androidx.startup.Initializer
import com.zj.data.utils.DataStoreUtils

/**
 * 用于初始化DataStore的Startup Initializer
 */
class DataStoreInitializer : Initializer<DataStoreUtils> {
    override fun create(context: Context): DataStoreUtils {
        DataStoreUtils.init(context)
        return DataStoreUtils
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}