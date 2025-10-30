package com.zj.ink.layer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke

/**
 * 图层渲染器
 * 负责将图层内容渲染到画布上，支持图层混合、透明度等效果
 */
class LayerRenderer {

    private val canvasStrokeRenderer = CanvasStrokeRenderer.create()
    private val layerPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    /**
     * 渲染单个图层到画布
     */
    fun renderLayer(
        layer: Layer,
        canvas: Canvas,
        transform: Matrix = Matrix(),
        strokes: Set<Stroke> = emptySet()
    ) {
        if (!layer.isVisible) return

        // 保存画布状态
        val saveCount = canvas.save()

        try {
            // 应用图层变换
            canvas.concat(transform)

            // 设置图层透明度和混合模式
            setupLayerPaint(layer)

            // 根据图层类型进行渲染
            when (layer.getLayerType()) {
                LayerType.DRAWING -> renderDrawingLayer(layer, canvas, strokes)
                LayerType.TEXT -> renderTextLayer(layer, canvas)
                LayerType.IMAGE -> renderImageLayer(layer, canvas)
                LayerType.BACKGROUND -> renderBackgroundLayer(layer, canvas)
            }

        } finally {
            // 恢复画布状态
            canvas.restoreToCount(saveCount)
        }
    }

    /**
     * 渲染多个图层（按z-order顺序）
     */
    fun renderLayers(
        layers: List<Layer>,
        canvas: Canvas,
        transform: Matrix = Matrix(),
        strokesMap: Map<String, Set<Stroke>> = emptyMap()
    ) {
        // 按z-order排序
        val sortedLayers = layers.sortedBy { it.zOrder }

        sortedLayers.forEach { layer ->
            val layerStrokes = strokesMap[layer.id] ?: emptySet()
            renderLayer(layer, canvas, transform, layerStrokes)
        }
    }

    /**
     * 渲染绘图图层
     */
    private fun renderDrawingLayer(
        layer: Layer,
        canvas: Canvas,
        strokes: Set<Stroke>
    ) {
        // 渲染笔迹数据
        strokes.forEach { stroke ->
            canvasStrokeRenderer.draw(
                stroke = stroke,
                canvas = canvas,
                strokeToScreenTransform = Matrix()
            )
        }
    }

    /**
     * 渲染文本图层
     */
    private fun renderTextLayer(
        layer: Layer,
        canvas: Canvas
    ) {
        if (layer.textContent.isBlank()) return

        val textPaint = Paint().apply {
            color = layer.textColor
            textSize = layer.textSize
            isAntiAlias = true
            alpha = (layer.opacity * 255).toInt()
        }

        // 简单的文本渲染（实际应用中可能需要更复杂的文本布局）
        val lines = layer.textContent.split('\n')
        val lineHeight = textPaint.textSize * 1.2f

        lines.forEachIndexed { index, line ->
            val y = (index + 1) * lineHeight
            canvas.drawText(line, 0f, y, textPaint)
        }
    }

    /**
     * 渲染图像图层
     */
    private fun renderImageLayer(
        layer: Layer,
        canvas: Canvas
    ) {
        // TODO: 实现图像渲染
        // 这里需要加载和渲染图像文件
        // 可能需要使用 Coil 或其他图像加载库
    }

    /**
     * 渲染背景图层
     */
    private fun renderBackgroundLayer(
        layer: Layer,
        canvas: Canvas
    ) {
        // 渲染背景色
        val backgroundColor = Color.White.toArgb()
        canvas.drawColor(backgroundColor)

        // 如果有背景图像，也在这里渲染
        if (layer.imagePath.isNotEmpty()) {
            renderImageLayer(layer, canvas)
        }
    }

    /**
     * 设置图层绘制属性
     */
    private fun setupLayerPaint(layer: Layer) {
        layerPaint.alpha = (layer.opacity * 255).toInt()
        layerPaint.xfermode = getBlendModeXfermode(layer.getBlendMode())
    }

    /**
     * 获取混合模式对应的Xfermode
     */
    private fun getBlendModeXfermode(blendMode: BlendMode): PorterDuffXfermode? {
        val porterDuffMode = when (blendMode) {
            BlendMode.NORMAL -> PorterDuff.Mode.SRC_OVER
            BlendMode.MULTIPLY -> PorterDuff.Mode.MULTIPLY
            BlendMode.SCREEN -> PorterDuff.Mode.SCREEN
            BlendMode.OVERLAY -> PorterDuff.Mode.OVERLAY
            // Android不支持这些模式，使用相近的替代
            BlendMode.SOFT_LIGHT -> PorterDuff.Mode.OVERLAY // 使用OVERLAY作为替代
            BlendMode.HARD_LIGHT -> PorterDuff.Mode.OVERLAY // 使用OVERLAY作为替代
            BlendMode.COLOR_DODGE -> PorterDuff.Mode.LIGHTEN // 使用LIGHTEN作为替代
            BlendMode.COLOR_BURN -> PorterDuff.Mode.DARKEN // 使用DARKEN作为替代
            BlendMode.DIFFERENCE -> PorterDuff.Mode.XOR // 使用XOR作为替代
            BlendMode.EXCLUSION -> PorterDuff.Mode.XOR // 使用XOR作为替代
        }

        return if (blendMode == BlendMode.NORMAL) null else PorterDuffXfermode(porterDuffMode)
    }

    /**
     * 创建图层缩略图
     */
    fun createLayerThumbnail(
        layer: Layer,
        width: Int,
        height: Int,
        strokes: Set<Stroke> = emptySet()
    ): android.graphics.Bitmap? {
        return try {
            val bitmap = android.graphics.Bitmap.createBitmap(
                width, height, android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)

            // 白色背景
            canvas.drawColor(Color.White.toArgb())

            // 渲染图层内容
            renderLayer(layer, canvas, Matrix(), strokes)

            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 合并多个图层为单个位图
     */
    fun mergeLayers(
        layers: List<Layer>,
        width: Int,
        height: Int,
        strokesMap: Map<String, Set<Stroke>> = emptyMap()
    ): android.graphics.Bitmap? {
        return try {
            val bitmap = android.graphics.Bitmap.createBitmap(
                width, height, android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)

            // 渲染所有图层
            renderLayers(layers, canvas, Matrix(), strokesMap)

            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查图层是否在指定区域内有内容
     */
    fun hasContentInRegion(
        layer: Layer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        strokes: Set<Stroke> = emptySet()
    ): Boolean {
        return when (layer.getLayerType()) {
            LayerType.DRAWING -> {
                // 检查笔迹是否与区域相交
                strokes.any { stroke ->
                    // TODO: 实现笔迹边界检测
                    true // 简化实现
                }
            }
            LayerType.TEXT -> {
                // 检查文本是否在区域内
                layer.textContent.isNotBlank()
            }
            LayerType.IMAGE -> {
                // 检查图像是否与区域相交
                layer.imagePath.isNotEmpty()
            }
            LayerType.BACKGROUND -> true // 背景总是有内容
        }
    }

    /**
     * 计算图层的边界框
     */
    fun calculateLayerBounds(
        layer: Layer,
        strokes: Set<Stroke> = emptySet()
    ): LayerBounds? {
        return when (layer.getLayerType()) {
            LayerType.DRAWING -> {
                if (strokes.isEmpty()) return null

                // TODO: 计算所有笔迹的边界框
                LayerBounds(0f, 0f, 100f, 100f) // 简化实现
            }
            LayerType.TEXT -> {
                if (layer.textContent.isBlank()) return null

                // TODO: 计算文本的边界框
                LayerBounds(0f, 0f, 200f, 50f) // 简化实现
            }
            LayerType.IMAGE -> {
                if (layer.imagePath.isEmpty()) return null

                // TODO: 从图像文件获取尺寸
                LayerBounds(0f, 0f, 300f, 200f) // 简化实现
            }
            LayerType.BACKGROUND -> null // 背景没有固定边界
        }
    }
}

/**
 * 图层边界框
 */
data class LayerBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    /**
     * 检查是否包含指定点
     */
    fun contains(x: Float, y: Float): Boolean {
        return x >= left && x <= right && y >= top && y <= bottom
    }

    /**
     * 检查是否与另一个边界框相交
     */
    fun intersects(other: LayerBounds): Boolean {
        return left < other.right && right > other.left &&
               top < other.bottom && bottom > other.top
    }

    /**
     * 扩展边界框
     */
    fun expand(padding: Float): LayerBounds {
        return LayerBounds(
            left - padding,
            top - padding,
            right + padding,
            bottom + padding
        )
    }
}