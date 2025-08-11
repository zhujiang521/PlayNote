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
    private const val DEFAULT_SCALE = 2f
    private const val DEFAULT_MARGIN = 20
    private const val DEFAULT_IMAGE_QUALITY = 100

    /**
     * 从Markdown生成PDF文件
     *
     * @param context 上下文对象，用于加载自定义字体
     * @param markdownContent Markdown格式的字符串内容
     * @param pdfFile 要保存的PDF文件
     * @throws PdfGenerationException 如果生成PDF失败
     */
    suspend fun generatePdfFromMarkdown(
        context: Context,
        markdownContent: String,
        pdfFile: File,
    ) {
        if (markdownContent.isBlank()) {
            throw IllegalArgumentException("Markdown content cannot be null or empty")
        }
        withContext(Dispatchers.IO) {
            val htmlDocument = markdownContent.md2Html()

            try {
                val converterProperties = ConverterProperties().apply {
                    setFontProvider(createFontProvider(context))
                    setCharset("UTF-8")
                }

                PdfWriter(FileOutputStream(pdfFile)).use { pdfWriter ->
                    HtmlConverter.convertToPdf(htmlDocument, pdfWriter, converterProperties)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate PDF from Markdown", e)
                throw PdfGenerationException("Failed to generate PDF", e)
            }
        }
    }

    private fun createFontProvider(context: Context): FontProvider {
        return FontProvider().apply {
            addStandardPdfFonts()
            loadCustomFont(context)?.let { addFont(it, PdfEncodings.IDENTITY_H) }
                ?: addSystemFonts()
        }
    }

    private fun loadCustomFont(context: Context): FontProgram? {
        return synchronized(this) {
            cachedFontProgram ?: try {
                context.assets.open("HYQiHei-50S.otf").use { stream ->
                    FontProgramFactory.createFont(stream.readBytes()).also {
                        cachedFontProgram = it
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load custom font, using system fonts", e)
                null
            }
        }
    }

    /**
     * 将PDF文件转换为图片
     *
     * @param pdfFile 要转换的PDF文件
     * @param imageFile 保存转换后的图片文件
     * @param scale 渲染PDF页面的缩放比例，默认为2.0
     * @param margin 图片间的边距，默认为20像素
     * @param quality 图片保存质量，默认为100
     * @throws PdfConversionException 如果转换PDF到图片失败
     */
    suspend fun convertPdfToImage(
        pdfFile: File,
        imageFile: File,
        scale: Float = DEFAULT_SCALE,
        margin: Int = DEFAULT_MARGIN,
        quality: Int = DEFAULT_IMAGE_QUALITY
    ) {
        if (!pdfFile.exists() || !pdfFile.canRead()) {
            throw IllegalArgumentException("PDF file must be a valid, readable file")
        }
        withContext(Dispatchers.IO) {
            try {
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                    PdfRenderer(fd).use { renderer ->
                        if (renderer.pageCount > 0) {
                            val (bitmaps, totalSize) = renderPages(renderer, scale)
                            createFinalImage(bitmaps, totalSize, margin, imageFile, quality)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to convert PDF to image", e)
                throw PdfConversionException("Failed to convert PDF to image", e)
            }
        }
    }

    private fun renderPages(
        renderer: PdfRenderer,
        scale: Float
    ): Pair<List<Bitmap>, Pair<Int, Int>> {
        val bitmaps = mutableListOf<Bitmap>()
        var totalHeight = 0
        var maxWidth = 0

        for (i in 0 until renderer.pageCount) {
            renderer.openPage(i).use { page ->
                val (width, height) = calculateRenderSize(page, scale)
                maxWidth = maxOf(maxWidth, width)
                totalHeight += height

                createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                    renderPageToCanvas(page, scale, this)
                    bitmaps.add(this)
                }
            }
        }

        return Pair(bitmaps, Pair(maxWidth, totalHeight))
    }

    private fun calculateRenderSize(page: PdfRenderer.Page, scale: Float): Pair<Int, Int> {
        return Pair(
            (page.width * scale).toInt(),
            (page.height * scale).toInt()
        )
    }

    private fun renderPageToCanvas(page: PdfRenderer.Page, scale: Float, bitmap: Bitmap) {
        Canvas(bitmap).apply {
            drawColor(android.graphics.Color.WHITE)
            scale(scale, scale)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        }
    }

    private fun createFinalImage(
        bitmaps: List<Bitmap>,
        totalSize: Pair<Int, Int>,
        margin: Int,
        outputFile: File,
        quality: Int
    ) {
        val (maxWidth, totalHeight) = totalSize
        val finalSize = calculateFinalSize(maxWidth, totalHeight, margin, bitmaps.size)

        createBitmap(finalSize.first, finalSize.second, Bitmap.Config.ARGB_8888).apply {
            drawAllPages(this, bitmaps, maxWidth, margin)
            saveToFile(outputFile, quality)
            recycle()
        }
    }

    private fun calculateFinalSize(
        maxWidth: Int,
        totalHeight: Int,
        margin: Int,
        pageCount: Int
    ): Pair<Int, Int> {
        return Pair(
            maxWidth + margin * 2,
            totalHeight + pageCount * margin * 2
        )
    }

    private fun drawAllPages(
        finalBitmap: Bitmap,
        bitmaps: List<Bitmap>,
        maxWidth: Int,
        margin: Int
    ) {
        Canvas(finalBitmap).apply {
            drawColor(android.graphics.Color.WHITE)
            var currentY = margin

            bitmaps.forEach { bitmap ->
                val x = (maxWidth - bitmap.width) / 2
                drawBitmap(bitmap, x.toFloat(), currentY.toFloat(), null)
                currentY += bitmap.height + margin * 2
                bitmap.recycle()
            }
        }
    }

    private fun Bitmap.saveToFile(file: File, quality: Int) {
        FileOutputStream(file).use { fos ->
            compress(Bitmap.CompressFormat.PNG, quality, fos)
            fos.flush()
        }
    }

    class PdfGenerationException(message: String, cause: Throwable?) : Exception(message, cause)
    class PdfConversionException(message: String, cause: Throwable?) : Exception(message, cause)
}