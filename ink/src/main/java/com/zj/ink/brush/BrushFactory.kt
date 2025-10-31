package com.zj.ink.brush

import androidx.compose.ui.graphics.toArgb
import androidx.ink.brush.Brush
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes

/**
 * 画笔工厂类
 * 负责根据画笔类型和属性创建具体的画笔实例
 */
object BrushFactory {

    /**
     * 根据BrushProperties创建画笔 - 应用所有画笔属性
     */
    fun createBrush(properties: BrushProperties): Brush {
        val brushFamily = getBrushFamily(properties.brushType)
        val adjustedSize = adjustSize(properties.size, properties.brushType)

        // 计算最终的透明度（结合opacity和flow）
        val finalOpacity = properties.calculateFinalOpacity()

        // 计算画笔颜色（应用透明度）
        val finalColor = properties.color.copy(alpha = finalOpacity)

        // 应用硬度调整（通过调整epsilon值）
        // 硬度越高，epsilon越小，笔触越锐利
        val baseEpsilon = getEpsilon(properties.brushType)
        val epsilon = baseEpsilon * (1f - properties.hardness * 0.7f)

        return Brush.createWithColorIntArgb(
            family = brushFamily,
            colorIntArgb = finalColor.toArgb(),
            size = adjustedSize,
            epsilon = epsilon.coerceIn(0.01f, 1.0f) // 确保epsilon在合理范围内
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

}