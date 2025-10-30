package com.zj.ink.layer

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.ink.strokes.Stroke
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * 图层数据模型
 * 表示绘图中的一个图层，包含所有相关属性和内容
 */
@Stable
@Serializable
data class Layer(
    /** 图层唯一标识符 */
    val id: String = UUID.randomUUID().toString(),

    /** 图层名称 */
    val name: String,

    /** 图层类型 */
    val layerType: String, // 使用字符串存储，避免序列化问题

    /** 是否可见 */
    val isVisible: Boolean = true,

    /** 是否锁定（锁定后不可编辑） */
    val isLocked: Boolean = false,

    /** 透明度 (0.0 - 1.0) */
    val opacity: Float = 1.0f,

    /** 混合模式 */
    val blendMode: String = BlendMode.NORMAL.name,

    /** 图层在堆栈中的顺序（越大越在上层） */
    val zOrder: Int = 0,

    /** 笔迹数据（序列化为字符串） */
    val strokesData: String = "[]",

    /** 文本内容（如果是文本图层） */
    val textContent: String = "",

    /** 文本颜色（ARGB格式） */
    val textColor: Int = Color.Black.toArgb(),

    /** 文本大小 */
    val textSize: Float = 16f,

    /** 图像路径（如果是图像图层） */
    val imagePath: String = "",

    /** 图像位置和尺寸信息 */
    val imageTransform: String = "{}",

    /** 创建时间戳 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 最后修改时间戳 */
    val lastModified: Long = System.currentTimeMillis(),

    /** 图层描述或备注 */
    val description: String = ""
) {

    companion object {
        /**
         * 创建默认绘图图层
         */
        fun createDrawingLayer(name: String = "绘图图层"): Layer {
            return Layer(
                name = name,
                layerType = LayerType.DRAWING.name,
                description = "用于手绘内容"
            )
        }

        /**
         * 创建文本图层
         */
        fun createTextLayer(
            name: String = "文本图层",
            content: String = "",
            color: Color = Color.Black,
            size: Float = 16f
        ): Layer {
            return Layer(
                name = name,
                layerType = LayerType.TEXT.name,
                textContent = content,
                textColor = color.toArgb(),
                textSize = size,
                description = "用于文字内容"
            )
        }

        /**
         * 创建图像图层
         */
        fun createImageLayer(
            name: String = "图像图层",
            imagePath: String = ""
        ): Layer {
            return Layer(
                name = name,
                layerType = LayerType.IMAGE.name,
                imagePath = imagePath,
                description = "用于图片内容"
            )
        }

        /**
         * 创建背景图层
         */
        fun createBackgroundLayer(name: String = "背景"): Layer {
            return Layer(
                name = name,
                layerType = LayerType.BACKGROUND.name,
                opacity = 1.0f,
                isLocked = false,
                zOrder = -1000, // 背景层始终在最底层
                description = "背景图层"
            )
        }

        /**
         * 从JSON字符串解析图层
         */
        fun fromJson(json: String): Layer? {
            return try {
                Json.decodeFromString<Layer>(json)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 获取图层类型枚举
     */
    fun getLayerType(): LayerType {
        return LayerType.fromName(layerType) ?: LayerType.DEFAULT
    }

    /**
     * 获取混合模式枚举
     */
    fun getBlendMode(): BlendMode {
        return BlendMode.fromName(blendMode) ?: BlendMode.DEFAULT
    }

    /**
     * 转换为JSON字符串
     */
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    /**
     * 更新笔迹数据
     */
    fun withStrokes(strokes: Set<Stroke>): Layer {
        // 这里应该实现笔迹的序列化，暂时使用简化版本
        val strokesJson = Json.encodeToString(strokes.size) // 简化存储
        return copy(
            strokesData = strokesJson,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新图层可见性
     */
    fun withVisibility(visible: Boolean): Layer {
        return copy(
            isVisible = visible,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新图层透明度
     */
    fun withOpacity(newOpacity: Float): Layer {
        val validOpacity = newOpacity.coerceIn(0f, 1f)
        return copy(
            opacity = validOpacity,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新图层混合模式
     */
    fun withBlendMode(mode: BlendMode): Layer {
        return copy(
            blendMode = mode.name,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新图层锁定状态
     */
    fun withLockState(locked: Boolean): Layer {
        return copy(
            isLocked = locked,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新图层名称
     */
    fun withName(newName: String): Layer {
        return copy(
            name = newName.take(50), // 限制名称长度
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新图层顺序
     */
    fun withZOrder(order: Int): Layer {
        return copy(
            zOrder = order,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新文本内容（仅文本图层）
     */
    fun withTextContent(content: String, color: Color? = null, size: Float? = null): Layer {
        if (getLayerType() != LayerType.TEXT) return this

        return copy(
            textContent = content,
            textColor = color?.toArgb() ?: textColor,
            textSize = size ?: textSize,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 更新图像路径（仅图像图层）
     */
    fun withImagePath(path: String): Layer {
        if (getLayerType() != LayerType.IMAGE) return this

        return copy(
            imagePath = path,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 创建图层副本
     */
    fun duplicate(newName: String? = null): Layer {
        return copy(
            id = UUID.randomUUID().toString(),
            name = newName ?: "${name}_副本",
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * 验证图层数据是否有效
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                opacity in 0f..1f &&
                LayerType.fromName(layerType) != null &&
                BlendMode.fromName(blendMode) != null
    }

    /**
     * 检查图层是否可编辑
     */
    fun isEditable(): Boolean {
        return !isLocked && isVisible
    }

    /**
     * 检查图层是否支持指定功能
     */
    fun supportsDrawing(): Boolean = getLayerType().supportsDrawing
    fun supportsText(): Boolean = getLayerType().supportsText
    fun supportsImages(): Boolean = getLayerType().supportsImages
    fun supportsTransparency(): Boolean = getLayerType().supportsTransparency
    fun supportsBlendModes(): Boolean = getLayerType().supportsBlendModes

    /**
     * 获取图层显示信息
     */
    fun getDisplayInfo(): String {
        val typeInfo = getLayerType().displayName
        val visibilityInfo = if (isVisible) "可见" else "隐藏"
        val lockInfo = if (isLocked) "已锁定" else "可编辑"
        val opacityInfo = "${(opacity * 100).toInt()}%"

        return "$typeInfo · $visibilityInfo · $lockInfo · $opacityInfo"
    }
}

/**
 * 图层变换信息
 * 用于存储图层的位置、缩放、旋转等变换信息
 */
@Serializable
data class LayerTransform(
    /** X轴偏移 */
    val offsetX: Float = 0f,

    /** Y轴偏移 */
    val offsetY: Float = 0f,

    /** X轴缩放 */
    val scaleX: Float = 1f,

    /** Y轴缩放 */
    val scaleY: Float = 1f,

    /** 旋转角度（度数） */
    val rotation: Float = 0f,

    /** 是否保持宽高比 */
    val maintainAspectRatio: Boolean = true
) {

    /**
     * 检查是否为默认变换（无变化）
     */
    fun isIdentity(): Boolean {
        return offsetX == 0f && offsetY == 0f &&
               scaleX == 1f && scaleY == 1f &&
               rotation == 0f
    }

    /**
     * 转换为JSON字符串
     */
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        /**
         * 从JSON字符串解析变换信息
         */
        fun fromJson(json: String): LayerTransform? {
            return try {
                Json.decodeFromString<LayerTransform>(json)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 默认变换（无变化）
         */
        fun identity(): LayerTransform = LayerTransform()
    }
}