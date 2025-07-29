package com.zj.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.io.font.FontProgram
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.font.FontProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    private const val TAG = "PdfUtils"
    private var cachedFontProgram: FontProgram? = null

    /**
     * 从Markdown生成PDF文件
     */
    suspend fun generatePdfFromMarkdown(
        context: Context, markdownContent: String, pdfFile: File
    ) {
        withContext(Dispatchers.IO) {
            val htmlDocument = markdownContent.md2Html()

            try {
                val converterProperties = ConverterProperties()
                val fontProvider = FontProvider()
                fontProvider.addStandardPdfFonts()

                // 加载自定义字体（缓存）
                synchronized(this) {
                    if (cachedFontProgram == null) {
                        try {
                            val fontInputStream = context.assets.open("HYQiHei-50S.otf")
                            cachedFontProgram =
                                FontProgramFactory.createFont(fontInputStream.readBytes())
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to load custom font, using system fonts", e)
                            fontProvider.addSystemFonts()
                        }
                    }
                    cachedFontProgram?.let {
                        fontProvider.addFont(it, PdfEncodings.IDENTITY_H)
                    }
                }

                converterProperties.setFontProvider(fontProvider)
                converterProperties.setCharset("UTF-8")

                PdfWriter(FileOutputStream(pdfFile)).use { pdfWriter ->
                    HtmlConverter.convertToPdf(htmlDocument, pdfWriter, converterProperties)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate PDF from Markdown", e)
                throw e
            }
        }
    }

    /**
     * 将PDF文件转换为图片
     */
    fun convertPdfToImage(pdfFile: File, imageFile: File) {
        try {
            val fileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                // 收集所有页面的位图
                val pageBitmaps = mutableListOf<Bitmap>()
                var totalHeight = 0
                var maxWidth = 0 // 动态计算最大宽度
                val scale = 2f // 缩放因子以提高质量

                // 先渲染所有页面以计算总高度和最大宽度
                for (i in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(i)

                    // 计算渲染尺寸
                    val renderWidth = (page.width * scale).toInt()
                    val renderHeight = (page.height * scale).toInt()

                    // 更新最大宽度
                    maxWidth = maxOf(maxWidth, renderWidth)

                    // 创建位图
                    val bitmap = createBitmap(
                        renderWidth,
                        renderHeight,
                        Bitmap.Config.ARGB_8888
                    )

                    // 渲染页面到位图
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    canvas.scale(scale, scale)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    pageBitmaps.add(bitmap)
                    totalHeight += renderHeight

                    // 关闭页面
                    page.close()
                }

                // 添加一些边距
                val margin = 20
                maxWidth += margin * 2
                totalHeight += pageBitmaps.size * margin * 2 // 每页上下边距

                // 创建包含所有页面的长图
                val finalBitmap = createBitmap(
                    maxWidth,
                    totalHeight,
                    Bitmap.Config.ARGB_8888
                )

                // 在最终位图上绘制所有页面
                val finalCanvas = Canvas(finalBitmap)
                finalCanvas.drawColor(android.graphics.Color.WHITE)

                var currentY = margin
                pageBitmaps.forEach { pageBitmap ->
                    // 计算居中位置
                    val x = (maxWidth - pageBitmap.width) / 2
                    finalCanvas.drawBitmap(pageBitmap, x.toFloat(), currentY.toFloat(), null)
                    currentY += pageBitmap.height + margin * 2

                    // 回收页面位图
                    pageBitmap.recycle()
                }

                // 保存位图到文件
                FileOutputStream(imageFile).use { fos ->
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }

                // 回收最终位图
                finalBitmap.recycle()
            }

            // 关闭PDF渲染器
            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert PDF to image", e)
            throw e
        }
    }

}