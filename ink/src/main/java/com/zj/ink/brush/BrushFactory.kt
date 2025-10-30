package com.zj.ink.brush

import androidx.ink.brush.Brush
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * 画笔工厂类
 * 负责根据画笔类型和属性创建具体的画笔实例
 */
object BrushFactory {

    /**
     * 根据画笔类型创建默认画笔
     */
    fun createBrush(brushType: BrushType): Brush {
        return createBrush(
            brushType = brushType,
            color = Color.Black,
            size = brushType.defaultSize,
            opacity = brushType.defaultOpacity
        )
    }

    /**
     * 根据画笔类型和自定义属性创建画笔
     */
    fun createBrush(
        brushType: BrushType,
        color: Color,
        size: Float = brushType.defaultSize,
        opacity: Float = brushType.defaultOpacity
    ): Brush {
        val adjustedColor = applyOpacity(color, opacity)
        val adjustedSize = adjustSize(size, brushType)
        val brushFamily = getBrushFamily(brushType)

        return Brush.createWithColorIntArgb(
            family = brushFamily,
            colorIntArgb = adjustedColor.toArgb(),
            size = adjustedSize,
            epsilon = getEpsilon(brushType)
        )
    }

    /**
     * 根据画笔类型获取对应的BrushFamily
     */
    private fun getBrushFamily(brushType: BrushType): BrushFamily {
        return when (brushType) {
            BrushType.PEN -> StockBrushes.pressurePen()
            BrushType.PENCIL -> createPencilBrush()
            BrushType.BRUSH -> createArtBrush()
            BrushType.HIGHLIGHTER -> StockBrushes.marker()
            BrushType.MARKER -> StockBrushes.marker()
            BrushType.WATERCOLOR -> createWatercolorBrush()
            BrushType.CHALK -> createChalkBrush()
            BrushType.CRAYON -> createCrayonBrush()
        }
    }

    /**
     * 创建铅笔画笔 - 具有纹理感的细线条
     */
    private fun createPencilBrush(): BrushFamily {
        // 使用pressure pen作为基础，后续可以扩展为自定义画笔
        return StockBrushes.pressurePen()
    }

    /**
     * 创建艺术画笔 - 柔软的笔触效果
     */
    private fun createArtBrush(): BrushFamily {
        return StockBrushes.pressurePen()
    }

    /**
     * 创建水彩画笔 - 流动的水彩效果
     */
    private fun createWatercolorBrush(): BrushFamily {
        return StockBrushes.pressurePen()
    }

    /**
     * 创建粉笔画笔 - 粗糙的纹理效果
     */
    private fun createChalkBrush(): BrushFamily {
        return StockBrushes.pressurePen()
    }

    /**
     * 创建蜡笔画笔 - 厚重的蜡质效果
     */
    private fun createCrayonBrush(): BrushFamily {
        return StockBrushes.pressurePen()
    }

    /**
     * 应用透明度到颜色
     */
    private fun applyOpacity(color: Color, opacity: Float): Color {
        return color.copy(alpha = (color.alpha * opacity.coerceIn(0f, 1f)))
    }

    /**
     * 根据画笔类型调整尺寸
     */
    private fun adjustSize(size: Float, brushType: BrushType): Float {
        val adjustedSize = size.coerceIn(0.5f, 50f) // 限制尺寸范围

        return when (brushType) {
            BrushType.PEN -> adjustedSize * 0.8f // 钢笔相对较细
            BrushType.PENCIL -> adjustedSize * 0.9f // 铅笔稍细
            BrushType.BRUSH -> adjustedSize * 1.2f // 毛笔相对较粗
            BrushType.HIGHLIGHTER -> adjustedSize * 1.5f // 荧光笔较粗
            BrushType.MARKER -> adjustedSize * 1.1f // 马克笔适中
            BrushType.WATERCOLOR -> adjustedSize * 1.3f // 水彩笔较粗
            BrushType.CHALK -> adjustedSize // 粉笔标准
            BrushType.CRAYON -> adjustedSize * 1.1f // 蜡笔稍粗
        }
    }

    /**
     * 根据画笔类型获取epsilon值（影响笔触平滑度）
     */
    private fun getEpsilon(brushType: BrushType): Float {
        return when (brushType) {
            BrushType.PEN -> 0.1f // 钢笔需要精确
            BrushType.PENCIL -> 0.15f // 铅笔稍微平滑
            BrushType.BRUSH -> 0.2f // 毛笔更平滑
            BrushType.HIGHLIGHTER -> 0.3f // 荧光笔平滑
            BrushType.MARKER -> 0.25f // 马克笔适中
            BrushType.WATERCOLOR -> 0.35f // 水彩最平滑
            BrushType.CHALK -> 0.2f // 粉笔适中
            BrushType.CRAYON -> 0.25f // 蜡笔适中
        }
    }

    /**
     * 检查画笔类型是否支持压感
     */
    fun supportsPressure(brushType: BrushType): Boolean {
        return brushType.supportsPressure
    }

    /**
     * 检查画笔类型是否支持纹理
     */
    fun supportsTexture(brushType: BrushType): Boolean {
        return brushType.supportsTexture
    }

    /**
     * 获取画笔类型的推荐颜色
     */
    fun getRecommendedColors(brushType: BrushType): List<Color> {
        return when (brushType) {
            BrushType.PEN -> listOf(
                Color.Black, Color.Blue, Color.Red,
                Color(0xFF2E7D32), Color(0xFF6A1B9A)
            )
            BrushType.PENCIL -> listOf(
                Color(0xFF424242), Color(0xFF616161), Color(0xFF757575),
                Color(0xFF9E9E9E), Color(0xFFBDBDBD)
            )
            BrushType.HIGHLIGHTER -> listOf(
                Color.Yellow, Color(0xFFFFEB3B), Color(0xFF4CAF50),
                Color(0xFF2196F3), Color(0xFFFF9800)
            )
            BrushType.WATERCOLOR -> listOf(
                Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFD32F2F),
                Color(0xFF7B1FA2), Color(0xFFFF8F00)
            )
            else -> listOf(
                Color.Black, Color.Red, Color.Blue,
                Color.Green, Color(0xFFFF9800)
            )
        }
    }
}