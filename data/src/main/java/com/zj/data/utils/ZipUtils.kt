package com.zj.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipUtils {

    private const val TAG = "ZipUtils"

    /**
     * 导出类型枚举
     */
    enum class ExportType {
        HTML,
        MD
    }

    /**
     * 通用的带图片Markdown导出方法
     *
     * @param title 文件名（不包含扩展名）
     * @param markdownContent Markdown格式的内容
     * @param exportType 导出类型（HTML或MD）
     */
    suspend fun exportMarkdownWithImages(
        context: Context,
        title: String,
        markdownContent: String,
        exportType: ExportType
    ) {
        withContext(Dispatchers.IO) {
            // 提取图片链接
            val imageUrls = extractImageUrls(markdownContent)

            // 创建临时目录
            val tempDirName = "export_${System.currentTimeMillis()}_${UUID.randomUUID()}"
            val tempDir = File(context.cacheDir, tempDirName)
            val exportFile = when (exportType) {
                ExportType.HTML -> File(tempDir, "$title.html")
                ExportType.MD -> File(tempDir, "$title.md")
            }
            val imageDir = File(tempDir, "images")

            try {
                // 复制图片到image目录
                if (imageUrls.isNotEmpty()) {
                    copyImagesToDirectory(imageUrls, imageDir)
                }

                // 更新Markdown中图片路径为相对路径
                var updatedMarkdown = markdownContent
                imageUrls.forEachIndexed { index, originalUrl ->
                    val newImagePath = "images/image_$index.${getFileExtension(originalUrl)}"
                    // 更精确地替换Markdown图片语法中的URL部分
                    updatedMarkdown = updatedMarkdown.replaceFirst(
                        originalUrl,
                        newImagePath
                    )
                }

                // 根据导出类型处理文件内容
                when (exportType) {
                    ExportType.HTML -> {
                        val htmlDocument = updatedMarkdown.md2Html()
                        exportFile.parentFile?.mkdirs()
                        OutputStreamWriter(
                            FileOutputStream(exportFile),
                            Charsets.UTF_8
                        ).use { writer ->
                            writer.write(htmlDocument)
                        }
                    }

                    ExportType.MD -> {
                        exportFile.parentFile?.mkdirs()
                        exportFile.writeText(updatedMarkdown)
                    }
                }

                // 打包为ZIP文件
                val zipFile = File(context.cacheDir, "$title.zip")
                zipDirectory(tempDir, zipFile)

                // 分享 ZIP 文件
                shareFile(context, zipFile, "application/zip")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to export Markdown file with images", e)
                throw RuntimeException("Failed to export Markdown file with images", e)
            } finally {
                // 清理临时目录
                tempDir.deleteRecursively()
            }
        }
    }

    /**
     * 提取Markdown中的图片链接
     */
    fun extractImageUrls(markdownContent: String): List<String> {
        val imageRegex = """!\[.*?]\((.*?)\)"""
        val pattern = java.util.regex.Pattern.compile(imageRegex)
        val matcher = pattern.matcher(markdownContent)
        val result = mutableListOf<String>()
        while (matcher.find()) {
            val image = matcher.group(1)
            image?.let {
                result.add(it)
            }
        }
        return result
    }

    /**
     * 下载或复制图片到指定目录
     */
    suspend fun copyImagesToDirectory(imageUrls: List<String>, imageDir: File) {
        withContext(Dispatchers.IO) {
            imageDir.mkdirs()
            imageUrls.forEachIndexed { index, url ->
                try {
                    val fileName = "image_$index.${getFileExtension(url)}"
                    val imageFile = File(imageDir, fileName)
                    imageFile.parentFile?.mkdirs()
                    downloadAndSaveImage(url, imageFile)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to copy image: $url", e)
                }
            }
        }
    }

    /**
     * 使用GlanceImageLoader下载并保存图片
     */
    fun downloadAndSaveImage(imageUrl: String, targetFile: File) {
        try {
            // 使用URL直接下载图片（如果GlanceImageLoader有特定方法，可以替换这部分）
            val bitmap = GlanceImageLoader.loadBitmap(imageUrl)
            saveBitmapToFile(bitmap, targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download and save image: $imageUrl", e)
            throw e
        }
    }

    /**
     * 将Bitmap保存为文件
     */
    fun saveBitmapToFile(bitmap: Bitmap, targetFile: File) {
        try {
            FileOutputStream(targetFile).use { fos ->
                val extension = targetFile.extension.lowercase()
                when (extension) {
                    "png" -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    "jpg", "jpeg" -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                    else -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                fos.flush()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bitmap to file: ${targetFile.absolutePath}", e)
            throw e
        }
    }

    fun getFileExtension(url: String): String {
        val urlWithoutQuery = url.substringBefore("?")
        val extension = urlWithoutQuery.substringAfterLast(".", "png")
        return when (extension.lowercase()) {
            "jpg", "jpeg", "png", "webp" -> extension
            else -> "png"
        }
    }

    /**
     * 压缩目录为ZIP文件
     */
    fun zipDirectory(sourceDir: File, zipFile: File) {
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                sourceDir.walkTopDown().forEach { file ->
                    val zipFileName = file.toRelativeString(sourceDir)
                    if (zipFileName.isNotEmpty()) {
                        val entry = ZipEntry(
                            if (file.isDirectory) "$zipFileName/" else zipFileName
                        )
                        zos.putNextEntry(entry)

                        if (file.isFile) {
                            file.inputStream().use { fis ->
                                fis.copyTo(zos)
                            }
                        }
                        zos.closeEntry()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to zip directory: ${sourceDir.absolutePath}", e)
            throw e
        }
    }

}