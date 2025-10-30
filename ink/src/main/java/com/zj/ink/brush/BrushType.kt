package com.zj.ink.brush

import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes

/**
 * 画笔类型枚举
 * 定义不同类型画笔的基本属性和特征
 */
enum class BrushType(
    val displayName: String,
    val defaultFamily: BrushFamily,
    val defaultSize: Float,
    val defaultOpacity: Float,
    val supportsPressure: Boolean,
    val supportsTexture: Boolean,
    val description: String
) {
    /** 钢笔 - 精确线条，适合书写和细节绘制 */
    PEN(
        displayName = "钢笔",
        defaultFamily = StockBrushes.pressurePen(),
        defaultSize = 3f,
        defaultOpacity = 1.0f,
        supportsPressure = true,
        supportsTexture = false,
        description = "精确线条，适合书写和细节绘制"
    ),

    /** 铅笔 - 可调硬度，适合素描和草图 */
    PENCIL(
        displayName = "铅笔",
        defaultFamily = StockBrushes.pressurePen(),
        defaultSize = 2f,
        defaultOpacity = 0.8f,
        supportsPressure = true,
        supportsTexture = true,
        description = "可调硬度，适合素描和草图"
    ),

    /** 毛笔 - 柔软笔触，适合艺术创作 */
    BRUSH(
        displayName = "毛笔",
        defaultFamily = StockBrushes.pressurePen(),
        defaultSize = 8f,
        defaultOpacity = 0.9f,
        supportsPressure = true,
        supportsTexture = true,
        description = "柔软笔触，适合艺术创作"
    ),

    /** 荧光笔 - 半透明高亮，适合标记重点 */
    HIGHLIGHTER(
        displayName = "荧光笔",
        defaultFamily = StockBrushes.marker(),
        defaultSize = 12f,
        defaultOpacity = 0.4f,
        supportsPressure = false,
        supportsTexture = false,
        description = "半透明高亮，适合标记重点"
    ),

    /** 马克笔 - 饱和色彩，适合涂色和填充 */
    MARKER(
        displayName = "马克笔",
        defaultFamily = StockBrushes.marker(),
        defaultSize = 10f,
        defaultOpacity = 0.85f,
        supportsPressure = false,
        supportsTexture = true,
        description = "饱和色彩，适合涂色和填充"
    ),

    /** 水彩笔 - 流动效果，适合艺术绘画 */
    WATERCOLOR(
        displayName = "水彩笔",
        defaultFamily = StockBrushes.pressurePen(),
        defaultSize = 15f,
        defaultOpacity = 0.6f,
        supportsPressure = true,
        supportsTexture = true,
        description = "流动效果，适合艺术绘画"
    ),

    /** 粉笔 - 粗糙质感，适合黑板效果 */
    CHALK(
        displayName = "粉笔",
        defaultFamily = StockBrushes.pressurePen(),
        defaultSize = 8f,
        defaultOpacity = 0.7f,
        supportsPressure = false,
        supportsTexture = true,
        description = "粗糙质感，适合黑板效果"
    ),

    /** 蜡笔 - 厚重质感，适合儿童绘画 */
    CRAYON(
        displayName = "蜡笔",
        defaultFamily = StockBrushes.pressurePen(),
        defaultSize = 12f,
        defaultOpacity = 0.9f,
        supportsPressure = false,
        supportsTexture = true,
        description = "厚重质感，适合儿童绘画"
    );

    companion object {
        /** 默认画笔类型 */
        val DEFAULT = PEN

        /** 获取所有画笔类型 */
        fun getAllTypes(): List<BrushType> = values().toList()

        /** 根据名称查找画笔类型 */
        fun fromName(name: String): BrushType? =
            values().find { it.name.equals(name, ignoreCase = true) }
    }
}