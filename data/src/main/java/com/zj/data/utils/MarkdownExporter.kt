package com.zj.data.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MarkdownExporter(private val context: Context) {

    suspend fun exportMarkdownToFile(title: String, markdownContent: String) {
        withContext(Dispatchers.IO) {
            // 创建最终的MD文件
            val mdFile = File(context.cacheDir, "$title.md")
            // 写入文件
            mdFile.writeText(markdownContent)
            shareFile(
                context,
                mdFile,
                "text/markdown"
            )
        }
    }

}
