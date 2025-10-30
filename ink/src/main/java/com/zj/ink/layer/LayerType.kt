package com.zj.ink.layer

/**
 * 图层类型枚举
 * 定义不同类型图层的特性和功能
 */
enum class LayerType(
    val displayName: String,
    val description: String,
    val supportsDrawing: Boolean,
    val supportsText: Boolean,
    val supportsImages: Boolean,
    val supportsTransparency: Boolean,
    val supportsBlendModes: Boolean
) {
    /** 绘图图层 - 主要用于手绘内容 */
    DRAWING(
        displayName = "绘图图层",
        description = "用于手绘和绘图内容",
        supportsDrawing = true,
        supportsText = false,
        supportsImages = false,
        supportsTransparency = true,
        supportsBlendModes = true
    ),

    /** 文本图层 - 用于文字内容 */
    TEXT(
        displayName = "文本图层",
        description = "用于文字和标注",
        supportsDrawing = false,
        supportsText = true,
        supportsImages = false,
        supportsTransparency = true,
        supportsBlendModes = false
    ),

    /** 图像图层 - 用于插入的图片 */
    IMAGE(
        displayName = "图像图层",
        description = "用于插入的图片内容",
        supportsDrawing = false,
        supportsText = false,
        supportsImages = true,
        supportsTransparency = true,
        supportsBlendModes = true
    ),

    /** 背景图层 - 特殊的背景层 */
    BACKGROUND(
        displayName = "背景图层",
        description = "背景内容，不支持透明度",
        supportsDrawing = true,
        supportsText = true,
        supportsImages = true,
        supportsTransparency = false,
        supportsBlendModes = false
    );

    companion object {
        /** 默认图层类型 */
        val DEFAULT = DRAWING

        /** 获取所有图层类型 */
        fun getAllTypes(): List<LayerType> = values().toList()

        /** 根据名称查找图层类型 */
        fun fromName(name: String): LayerType? =
            values().find { it.name.equals(name, ignoreCase = true) }
    }
}

/**
 * 图层混合模式枚举
 * 定义图层之间的混合效果
 */
enum class BlendMode(
    val displayName: String,
    val description: String
) {
    /** 正常模式 - 默认混合 */
    NORMAL("正常", "标准的图层覆盖"),

    /** 叠加模式 - 增强对比度 */
    OVERLAY("叠加", "增强对比度和饱和度"),

    /** 正片叠底 - 变暗效果 */
    MULTIPLY("正片叠底", "颜色相乘，产生变暗效果"),

    /** 滤色模式 - 变亮效果 */
    SCREEN("滤色", "颜色相加，产生变亮效果"),

    /** 柔光模式 - 柔和的对比度增强 */
    SOFT_LIGHT("柔光", "柔和的对比度增强"),

    /** 强光模式 - 强烈的对比度增强 */
    HARD_LIGHT("强光", "强烈的对比度增强"),

    /** 颜色减淡 - 提亮效果 */
    COLOR_DODGE("颜色减淡", "提亮底层颜色"),

    /** 颜色加深 - 加深效果 */
    COLOR_BURN("颜色加深", "加深底层颜色"),

    /** 差值模式 - 颜色反转效果 */
    DIFFERENCE("差值", "颜色差值计算"),

    /** 排除模式 - 类似差值但更柔和 */
    EXCLUSION("排除", "类似差值但对比度更低");

    companion object {
        /** 默认混合模式 */
        val DEFAULT = NORMAL

        /** 获取所有混合模式 */
        fun getAllModes(): List<BlendMode> = values().toList()

        /** 根据名称查找混合模式 */
        fun fromName(name: String): BlendMode? =
            values().find { it.name.equals(name, ignoreCase = true) }
    }
}