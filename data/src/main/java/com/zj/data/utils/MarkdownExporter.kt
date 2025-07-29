package com.zj.data.utils

import android.content.Context
import android.util.Log
import com.zj.data.utils.PdfUtils.convertPdfToImage
import com.zj.data.utils.PdfUtils.generatePdfFromMarkdown
import com.zj.data.utils.ZipUtils.exportMarkdownWithImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

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
     * 将Markdown内容导出为图片文件
     * 生成PNG格式的图片文件
     *
     * @param title 文件名（不包含扩展名）
     * @param markdownContent Markdown格式的内容
     */
    suspend fun exportMarkdownToImage(title: String, markdownContent: String) {
        withContext(Dispatchers.IO) {
            // 首先生成PDF，然后将PDF转换为图片
            val pdfFile = File(context.cacheDir, "$title.pdf")

            try {
                // 先生成PDF
                generatePdfFromMarkdown(context, markdownContent, pdfFile)

                // 将PDF转换为图片
                val imageFile = File(context.cacheDir, "$title.png")
                convertPdfToImage(pdfFile, imageFile)

                // 分享图片文件
                shareFile(context, imageFile, "image/png")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export Markdown to image", e)
                throw RuntimeException("Failed to export Markdown to image", e)
            } finally {
                // 清理临时PDF文件
                if (pdfFile.exists()) {
                    pdfFile.delete()
                }
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
            // 创建 PDF 文件
            val pdfFile = File(context.cacheDir, "$title.pdf")
            try {
                // 生成PDF文件
                generatePdfFromMarkdown(context, markdownContent, pdfFile)

                // 分享 PDF 文件
                shareFile(context, pdfFile, "application/pdf")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export Markdown to PDF", e)
                throw RuntimeException("Failed to export Markdown to PDF", e)
            }
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
        exportMarkdownWithImages(context, title, markdownContent, ZipUtils.ExportType.HTML)
    }

    /**
     * 将Markdown内容导出为原始MD文件（包含图片的ZIP包）
     *
     * @param title 文件名（不包含扩展名）
     * @param markdownContent Markdown格式的内容
     */
    suspend fun exportMarkdownToFile(title: String, markdownContent: String) {
        exportMarkdownWithImages(context, title, markdownContent, ZipUtils.ExportType.MD)
    }

}
