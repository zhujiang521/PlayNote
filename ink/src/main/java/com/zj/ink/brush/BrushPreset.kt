package com.zj.ink.brush

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 画笔预设数据类
 * 包含完整的画笔配置信息，可序列化保存
 */
@Stable
@Serializable
data class BrushPreset(
    /** 预设ID */
    val id: String,

    /** 预设名称 */
    val name: String,

    /** 画笔类型名称 */
    val brushTypeName: String,

    /** 颜色值 (ARGB) */
    val colorArgb: Int,

    /** 画笔大小 */
    val size: Float,

    /** 透明度 */
    val opacity: Float,

    /** 流量 */
    val flow: Float,

    /** 硬度 */
    val hardness: Float,

    /** 散布 */
    val scatter: Float,

    /** 压感灵敏度 */
    val pressureSensitivity: Float,

    /** 纹理强度 */
    val textureIntensity: Float,

    /** 是否启用压感 */
    val pressureEnabled: Boolean,

    /** 是否启用纹理 */
    val textureEnabled: Boolean,

    /** 是否为系统预设 */
    val isSystemPreset: Boolean = false,

    /** 创建时间戳 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 描述信息 */
    val description: String = ""
) {

    companion object {
        /**
         * 从画笔属性创建预设
         */
        fun fromBrushProperties(
            id: String,
            name: String,
            properties: BrushProperties,
            isSystemPreset: Boolean = false,
            description: String = ""
        ): BrushPreset {
            return BrushPreset(
                id = id,
                name = name,
                brushTypeName = properties.brushType.name,
                colorArgb = properties.color.toArgb(),
                size = properties.size,
                opacity = properties.opacity,
                flow = properties.flow,
                hardness = properties.hardness,
                scatter = properties.scatter,
                pressureSensitivity = properties.pressureSensitivity,
                textureIntensity = properties.textureIntensity,
                pressureEnabled = properties.pressureEnabled,
                textureEnabled = properties.textureEnabled,
                isSystemPreset = isSystemPreset,
                description = description
            )
        }

        /**
         * 从JSON字符串解析预设
         */
        fun fromJson(json: String): BrushPreset? {
            return try {
                Json.decodeFromString<BrushPreset>(json)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 创建默认系统预设列表
         */
        fun createSystemPresets(): List<BrushPreset> {
            return listOf(
                // 钢笔预设
                createPreset("pen_fine", "细钢笔", BrushType.PEN, 2f, Color.Black, "精细线条，适合详细绘制"),
                createPreset("pen_medium", "中钢笔", BrushType.PEN, 4f, Color.Blue, "标准钢笔，日常书写"),
                createPreset("pen_bold", "粗钢笔", BrushType.PEN, 6f, Color.Red, "粗线条，强调重点"),

                // 铅笔预设
                createPreset("pencil_2h", "2H铅笔", BrushType.PENCIL, 1.5f, Color(0xFF757575), "硬铅笔，淡色细线"),
                createPreset("pencil_hb", "HB铅笔", BrushType.PENCIL, 2.5f, Color(0xFF424242), "标准铅笔，适合素描"),
                createPreset("pencil_2b", "2B铅笔", BrushType.PENCIL, 3.5f, Color(0xFF212121), "软铅笔，浓黑线条"),

                // 荧光笔预设
                createPreset("highlight_yellow", "黄色荧光", BrushType.HIGHLIGHTER, 12f, Color.Yellow, "经典黄色高亮"),
                createPreset("highlight_green", "绿色荧光", BrushType.HIGHLIGHTER, 12f, Color(0xFF4CAF50), "绿色重点标记"),
                createPreset("highlight_pink", "粉色荧光", BrushType.HIGHLIGHTER, 12f, Color(0xFFE91E63), "粉色温柔标记"),

                // 毛笔预设
                createPreset("brush_ink", "墨汁毛笔", BrushType.BRUSH, 8f, Color.Black, "传统水墨效果"),
                createPreset("brush_color", "彩色毛笔", BrushType.BRUSH, 10f, Color(0xFF1976D2), "彩色艺术创作"),

                // 马克笔预设
                createPreset("marker_red", "红色马克", BrushType.MARKER, 8f, Color.Red, "鲜艳红色填充"),
                createPreset("marker_blue", "蓝色马克", BrushType.MARKER, 8f, Color.Blue, "经典蓝色标记"),

                // 水彩预设
                createPreset("watercolor_blue", "蓝色水彩", BrushType.WATERCOLOR, 15f, Color(0xFF2196F3), "流动水彩效果"),
                createPreset("watercolor_green", "绿色水彩", BrushType.WATERCOLOR, 15f, Color(0xFF4CAF50), "自然绿色水彩")
            )
        }

        /**
         * 创建单个预设的辅助方法
         */
        private fun createPreset(
            id: String,
            name: String,
            brushType: BrushType,
            size: Float,
            color: Color,
            description: String
        ): BrushPreset {
            val properties = BrushProperties.fromBrushType(brushType).copy(
                size = size,
                color = color
            )
            return fromBrushProperties(id, name, properties, true, description)
        }
    }

    /**
     * 转换为画笔属性
     */
    fun toBrushProperties(): BrushProperties? {
        val brushType = BrushType.fromName(brushTypeName) ?: return null

        return BrushProperties(
            brushType = brushType,
            color = Color(colorArgb),
            size = size,
            opacity = opacity,
            flow = flow,
            hardness = hardness,
            scatter = scatter,
            pressureSensitivity = pressureSensitivity,
            textureIntensity = textureIntensity,
            pressureEnabled = pressureEnabled,
            textureEnabled = textureEnabled
        ).validate()
    }

    /**
     * 转换为JSON字符串
     */
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    /**
     * 创建副本并修改名称
     */
    fun copyWithName(newName: String, newId: String = "${id}_copy"): BrushPreset {
        return copy(
            id = newId,
            name = newName,
            isSystemPreset = false,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * 检查预设是否有效
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                BrushType.fromName(brushTypeName) != null &&
                size > 0 &&
                opacity in 0f..1f &&
                flow in 0f..1f &&
                hardness in 0f..1f &&
                scatter in 0f..1f &&
                pressureSensitivity >= 0f &&
                textureIntensity in 0f..1f
    }
}

/**
 * 预设分类枚举
 */
enum class PresetCategory(val displayName: String) {
    SYSTEM("系统预设"),
    USER("我的预设"),
    RECENT("最近使用"),
    FAVORITE("收藏夹");

    companion object {
        fun getAllCategories(): List<PresetCategory> = values().toList()
    }
}