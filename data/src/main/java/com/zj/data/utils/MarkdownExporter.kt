package com.zj.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.font.FontProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Markdown导出工具类
 * 支持将Markdown内容导出为PDF、HTML和原始MD文件格式
 *
 * @param context Android上下文，用于访问assets和文件系统
 */
class MarkdownExporter(private val context: Context) {

    companion object {
        private const val TAG = "MarkdownExporter"
    }

    /**
     * 将Markdown内容导出为原始MD文件（包含图片的ZIP包）
     *
     * @param title 文件名（不包含扩展名）
     * @param markdownContent Markdown格式的内容
     */
    suspend fun exportMarkdownToFile(title: String, markdownContent: String) {
        withContext(Dispatchers.IO) {
            // 提取图片链接
            val imageUrls = extractImageUrls(markdownContent)

            // 创建临时目录
            val tempDir = File(context.cacheDir, "export_${System.currentTimeMillis()}")
            val mdFile = File(tempDir, "$title.md")
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
                    // 更精确地替换Markdown图片语法中的URL部分，保留alt文本
                    updatedMarkdown = updatedMarkdown.replace(
                        originalUrl,
                        newImagePath
                    )
                }

                // 写入 Markdown 文件
                mdFile.parentFile?.mkdirs()
                mdFile.writeText(updatedMarkdown)

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
     * 将Markdown内容导出为PDF文件
     * 支持中文显示，使用HYQiHei字体确保中文字体正确渲染
     *
     * @param title 文件名（不包含扩展名）
     * @param markdownContent Markdown格式的内容
     */
    suspend fun exportMarkdownToPdf(title: String, markdownContent: String) {
        withContext(Dispatchers.IO) {
            val htmlDocument = markdownContent.md2Html()

            // 创建 PDF 文件
            val pdfFile = File(context.cacheDir, "$title.pdf")
            try {
                val converterProperties = ConverterProperties()
                // 设置字体支持中文
                val fontProvider = FontProvider()
                // 添加标准字体集
                fontProvider.addStandardPdfFonts()
                try {
                    val fontInputStream = context.assets.open("HYQiHei-50S.otf")
                    val fontProgram = FontProgramFactory.createFont(
                        fontInputStream.readBytes(),
                    )
                    fontProvider.addFont(fontProgram, PdfEncodings.IDENTITY_H)
                } catch (e: Exception) {
                    Log.w("MarkdownExporter", "Failed to load custom font, using system fonts", e)
                    // 如果自定义字体加载失败，尝试使用系统字体
                    fontProvider.addSystemFonts()
                }

                // 添加Android系统默认字体支持中文
                converterProperties.setFontProvider(fontProvider)
                converterProperties.setCharset("UTF-8")

                //输出地址
                val pdfWriter =
                    PdfWriter(FileOutputStream(pdfFile))

                //开始转换
                HtmlConverter.convertToPdf(htmlDocument, pdfWriter, converterProperties)

                // 分享 PDF 文件
                shareFile(context, pdfFile, "application/pdf")
            } catch (e: Exception) {
                Log.e("MarkdownExporter", "Failed to export Markdown to PDF", e)
                throw RuntimeException("Failed to export Markdown to PDF", e)
            }

            // 分享 PDF 文件
            shareFile(context, pdfFile, "application/pdf")
        }
    }

    /**
     * 将Markdown内容导出为HTML文件
     * 生成完整的HTML文档，包含CSS样式和字体设置
     *
     * @param title 文件名（不包含扩展名）
     * @param markdownContent Markdown格式的内容
     */
    suspend fun exportMarkdownToHtml(title: String, markdownContent: String) {
        withContext(Dispatchers.IO) {
            // 提取图片链接
            val imageUrls = extractImageUrls(markdownContent)

            // 创建临时目录
            val tempDir = File(context.cacheDir, "export_${System.currentTimeMillis()}")
            val htmlFile = File(tempDir, "$title.html")
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
                    // 替换Markdown中的图片链接格式 ![alt](originalUrl) -> ![alt](newImagePath)
                    Log.d(
                        TAG,
                        "exportMarkdownToHtml: originalUrl:$originalUrl newImagePath：$newImagePath"
                    )
                    updatedMarkdown = updatedMarkdown.replace(
                        originalUrl,
                        newImagePath
                    )
                }

                // 写入 Markdown 文件
                val fullHtmlDocument = updatedMarkdown.md2Html()
                try {
                    // 写入 HTML 文件
                    OutputStreamWriter(FileOutputStream(htmlFile), Charsets.UTF_8).use { writer ->
                        writer.write(fullHtmlDocument)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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

    // 在 MarkdownExporter 类中添加以下方法和属性

    /**
     * 提取Markdown中的图片链接
     */
    private fun extractImageUrls(markdownContent: String): List<String> {
        // 使用原始字符串避免转义问题
        val imageRegex = """!\[.*?]\((.*?)\)"""
        val pattern = java.util.regex.Pattern.compile(imageRegex)
        val matcher = pattern.matcher(markdownContent)
        val result = mutableListOf<String>()
        Log.d(TAG, "extractImageUrls: matcher:$matcher")
        while (matcher.find()) {
            val image = matcher.group(1)
            Log.d(TAG, "extractImageUrls: image:$image")
            image?.let {
                result.add(it)
            }
        }

        return result
    }

    /**
     * 下载或复制图片到指定目录
     */
    private suspend fun copyImagesToDirectory(imageUrls: List<String>, imageDir: File) {
        withContext(Dispatchers.IO) {
            imageDir.mkdirs()
            imageUrls.forEachIndexed { index, url ->
                try {
                    val fileName = "image_$index.${getFileExtension(url)}"
                    val imageFile = File(imageDir, fileName)

                    // 确保目标文件的父目录存在
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
    private fun downloadAndSaveImage(imageUrl: String, targetFile: File) {
        try {
            // 使用URL直接下载图片（如果GlanceImageLoader有特定方法，可以替换这部分）
            val bitmap = GlanceImageLoader.loadBitmap(imageUrl)
            // 保存Bitmap到文件
            saveBitmapToFile(bitmap, targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download and save image: $imageUrl", e)
            throw e
        }
    }

    /**
     * 将Bitmap保存为文件
     */
    private fun saveBitmapToFile(bitmap: Bitmap, targetFile: File) {
        try {
            FileOutputStream(targetFile).use { fos ->
                // 根据文件扩展名决定压缩格式
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
        } finally {
            // bitmap.recycle() // 注意：只有在确定不再使用bitmap时才调用
        }
    }

    private fun getFileExtension(url: String): String {
        // 先去除查询参数
        val urlWithoutQuery = url.substringBefore("?")
        val extension = urlWithoutQuery.substringAfterLast(".", "png")
        // 确保扩展名有效
        return when (extension.lowercase()) {
            "jpg", "jpeg", "png", "webp" -> extension
            else -> "png" // 默认使用png格式
        }
    }

    /**
     * 压缩目录为ZIP文件
     */
    private fun zipDirectory(sourceDir: File, zipFile: File) {
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
