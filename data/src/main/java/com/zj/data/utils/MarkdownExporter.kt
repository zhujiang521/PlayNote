package com.zj.data.utils

import android.content.Context
import android.util.Log
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.font.FontProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

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
     * 将Markdown内容导出为原始MD文件
     *
     * @param title 文件名（不包含扩展名）
     * @param markdownContent Markdown格式的内容
     */
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
            Log.d(TAG, "exportMarkdownToPdf: htmlContent：$htmlDocument")
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
            val fullHtmlDocument = markdownContent.md2Html()
            // 创建 HTML 文件
            val htmlFile = File(context.cacheDir, "$title.html")
            try {
                // 写入 HTML 文件
                OutputStreamWriter(FileOutputStream(htmlFile), Charsets.UTF_8).use { writer ->
                    writer.write(fullHtmlDocument)
                }
                // 分享 HTML 文件
                shareFile(context, htmlFile, "text/html")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export Markdown to HTML", e)
                throw RuntimeException("Failed to export Markdown to HTML", e)
            }
        }
    }

}
