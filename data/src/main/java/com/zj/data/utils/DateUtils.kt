package com.zj.data.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    /**
     * 将毫秒值格式化为人类可读的时间字符串
     *
     * @param timestamp 毫秒值
     * @return 格式化后的时间字符串
     */
    fun formatTimestamp(timestamp: Long): String {
        val locale = Locale.getDefault()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
        return dateFormat.format(Date(timestamp))
    }
}
