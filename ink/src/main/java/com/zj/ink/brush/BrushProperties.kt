package com.zj.ink.brush

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * 画笔属性数据类
 * 包含画笔的所有可配置属性
 */
@Stable
data class BrushProperties(
    /** 画笔类型 */
    val brushType: BrushType = BrushType.PEN,

    /** 画笔颜色 */
    val color: Color = Color.Black,

    /** 画笔大小 */
    val size: Float = 5f,

    /** 透明度 (0.0 - 1.0) */
    val opacity: Float = 1.0f,

    /** 流量 - 控制颜料流出量 (0.0 - 1.0) */
    val flow: Float = 1.0f,

    /** 硬度 - 控制笔触边缘的锐利程度 (0.0 - 1.0) */
    val hardness: Float = 1.0f,

    /** 散布 - 控制笔触的随机性 (0.0 - 1.0) */
    val scatter: Float = 0.0f,

    /** 压感灵敏度 (0.0 - 2.0) */
    val pressureSensitivity: Float = 1.0f,

    /** 纹理强度 (0.0 - 1.0) */
    val textureIntensity: Float = 0.0f,

    /** 是否启用压感 */
    val pressureEnabled: Boolean = true,

    /** 是否启用纹理 */
    val textureEnabled: Boolean = false
) {

    companion object {
        /**
         * 根据画笔类型创建默认属性
         */
        fun fromBrushType(brushType: BrushType): BrushProperties {
            return BrushProperties(
                brushType = brushType,
                color = Color.Black,
                size = brushType.defaultSize,
                opacity = brushType.defaultOpacity,
                flow = getDefaultFlow(brushType),
                hardness = getDefaultHardness(brushType),
                scatter = getDefaultScatter(brushType),
                pressureSensitivity = getDefaultPressureSensitivity(brushType),
                textureIntensity = getDefaultTextureIntensity(brushType),
                pressureEnabled = brushType.supportsPressure,
                textureEnabled = brushType.supportsTexture
            )
        }

        /**
         * 获取画笔类型的默认流量
         */
        private fun getDefaultFlow(brushType: BrushType): Float {
            return when (brushType) {
                BrushType.PEN -> 1.0f
                BrushType.PENCIL -> 0.8f
                BrushType.BRUSH -> 0.9f
                BrushType.HIGHLIGHTER -> 0.6f
                BrushType.MARKER -> 0.9f
                BrushType.WATERCOLOR -> 0.7f
                BrushType.CHALK -> 0.8f
                BrushType.CRAYON -> 0.9f
            }
        }

        /**
         * 获取画笔类型的默认硬度
         */
        private fun getDefaultHardness(brushType: BrushType): Float {
            return when (brushType) {
                BrushType.PEN -> 1.0f
                BrushType.PENCIL -> 0.7f
                BrushType.BRUSH -> 0.3f
                BrushType.HIGHLIGHTER -> 0.8f
                BrushType.MARKER -> 0.6f
                BrushType.WATERCOLOR -> 0.2f
                BrushType.CHALK -> 0.4f
                BrushType.CRAYON -> 0.5f
            }
        }

        /**
         * 获取画笔类型的默认散布
         */
        private fun getDefaultScatter(brushType: BrushType): Float {
            return when (brushType) {
                BrushType.PEN -> 0.0f
                BrushType.PENCIL -> 0.1f
                BrushType.BRUSH -> 0.2f
                BrushType.HIGHLIGHTER -> 0.0f
                BrushType.MARKER -> 0.05f
                BrushType.WATERCOLOR -> 0.3f
                BrushType.CHALK -> 0.4f
                BrushType.CRAYON -> 0.2f
            }
        }

        /**
         * 获取画笔类型的默认压感灵敏度
         */
        private fun getDefaultPressureSensitivity(brushType: BrushType): Float {
            return when (brushType) {
                BrushType.PEN -> 1.2f
                BrushType.PENCIL -> 1.5f
                BrushType.BRUSH -> 1.8f
                BrushType.HIGHLIGHTER -> 0.5f
                BrushType.MARKER -> 0.8f
                BrushType.WATERCOLOR -> 2.0f
                BrushType.CHALK -> 1.0f
                BrushType.CRAYON -> 1.0f
            }
        }

        /**
         * 获取画笔类型的默认纹理强度
         */
        private fun getDefaultTextureIntensity(brushType: BrushType): Float {
            return when (brushType) {
                BrushType.PEN -> 0.0f
                BrushType.PENCIL -> 0.3f
                BrushType.BRUSH -> 0.2f
                BrushType.HIGHLIGHTER -> 0.0f
                BrushType.MARKER -> 0.1f
                BrushType.WATERCOLOR -> 0.4f
                BrushType.CHALK -> 0.8f
                BrushType.CRAYON -> 0.6f
            }
        }
    }

    /**
     * 验证属性值是否在有效范围内
     */
    fun validate(): BrushProperties {
        return copy(
            size = size.coerceIn(0.5f, 50f),
            opacity = opacity.coerceIn(0f, 1f),
            flow = flow.coerceIn(0f, 1f),
            hardness = hardness.coerceIn(0f, 1f),
            scatter = scatter.coerceIn(0f, 1f),
            pressureSensitivity = pressureSensitivity.coerceIn(0f, 2f),
            textureIntensity = textureIntensity.coerceIn(0f, 1f)
        )
    }

    /**
     * 计算最终的画笔大小（考虑压感）
     */
    fun calculateFinalSize(pressure: Float = 1f): Float {
        val baseSizeWithPressure = if (pressureEnabled && brushType.supportsPressure) {
            size * (1f + (pressure - 1f) * pressureSensitivity)
        } else {
            size
        }
        return baseSizeWithPressure.coerceIn(0.5f, 50f)
    }

    /**
     * 计算最终的透明度（考虑流量）
     */
    fun calculateFinalOpacity(): Float {
        return (opacity * flow).coerceIn(0f, 1f)
    }

    /**
     * 检查是否为默认属性
     */
    fun isDefault(): Boolean {
        val defaultProps = fromBrushType(brushType)
        return this == defaultProps.copy(color = this.color)
    }
}

/**
 * 画笔属性扩展函数
 */

/**
 * 应用压感到画笔属性
 */
fun BrushProperties.withPressure(pressure: Float): BrushProperties {
    if (!pressureEnabled || !brushType.supportsPressure) return this

    val adjustedSize = calculateFinalSize(pressure)
    val adjustedOpacity = if (pressure < 0.5f) {
        calculateFinalOpacity() * (0.5f + pressure)
    } else {
        calculateFinalOpacity()
    }

    return copy(
        size = adjustedSize,
        opacity = adjustedOpacity.coerceIn(0f, 1f)
    )
}

/**
 * 应用纹理效果到画笔属性
 */
fun BrushProperties.withTexture(textureStrength: Float = textureIntensity): BrushProperties {
    if (!textureEnabled || !brushType.supportsTexture) return this

    return copy(textureIntensity = textureStrength.coerceIn(0f, 1f))
}