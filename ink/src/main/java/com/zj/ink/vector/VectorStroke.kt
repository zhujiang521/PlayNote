package com.zj.ink.vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.ink.strokes.Stroke
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * 矢量笔迹
 * 表示一个完整的矢量化笔迹，包含路径、样式和属性
 */
@Serializable
data class VectorStroke(
    /** 笔迹唯一标识符 */
    val id: String = UUID.randomUUID().toString(),

    /** 笔迹路径点 */
    val pathPoints: List<VectorPoint>,

    /** 笔迹颜色 (ARGB) */
    val color: Int = Color.Black.toArgb(),

    /** 笔迹宽度 */
    val width: Float = 5f,

    /** 透明度 */
    val opacity: Float = 1f,

    /** 笔迹样式 */
    val style: String = VectorStrokeStyle.SOLID.name,

    /** 是否启用压感 */
    val pressureEnabled: Boolean = true,

    /** 平滑参数 */
    val smoothingFactor: Float = 0.3f,

    /** 简化容差 */
    val simplificationTolerance: Float = 2.0f,

    /** 创建时间戳 */
    val timestamp: Long = System.currentTimeMillis(),

    /** 笔迹边界框 */
    val bounds: String = "{}", // JSON格式存储边界框

    /** 额外属性 */
    val properties: Map<String, String> = emptyMap()
) {

    companion object {
        /**
         * 从原始点创建矢量笔迹
         */
        fun fromRawPoints(
            points: List<VectorPoint>,
            color: Color = Color.Black,
            width: Float = 5f,
            style: VectorStrokeStyle = VectorStrokeStyle.SOLID,
            enablePressure: Boolean = true
        ): VectorStroke {
            val bounds = calculateBounds(points)

            return VectorStroke(
                pathPoints = points,
                color = color.toArgb(),
                width = width,
                style = style.name,
                pressureEnabled = enablePressure,
                bounds = bounds?.let { Json.encodeToString(it) } ?: "{}"
            )
        }

        /**
         * 从传统Stroke转换
         */
        fun fromInkStroke(stroke: Stroke): VectorStroke {
            // TODO: 实现从Ink Stroke到VectorStroke的转换
            // 这需要提取stroke的路径点、颜色等属性
            return VectorStroke(
                pathPoints = emptyList(), // 简化实现
                color = Color.Black.toArgb(),
                width = 5f
            )
        }

        /**
         * 计算点集的边界框
         */
        private fun calculateBounds(points: List<VectorPoint>): VectorBounds? {
            if (points.isEmpty()) return null

            val minX = points.minOf { it.x }
            val maxX = points.maxOf { it.x }
            val minY = points.minOf { it.y }
            val maxY = points.maxOf { it.y }

            return VectorBounds(minX, minY, maxX, maxY)
        }
    }

    /**
     * 获取笔迹样式枚举
     */
    fun getStyle(): VectorStrokeStyle {
        return VectorStrokeStyle.fromName(style) ?: VectorStrokeStyle.SOLID
    }

    /**
     * 获取笔迹颜色
     */
    fun getColor(): Color = Color(color)

    /**
     * 获取边界框
     */
    fun getBounds(): VectorBounds? {
        return try {
            Json.decodeFromString<VectorBounds>(bounds)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 生成平滑路径
     */
    fun generateSmoothPath(): VectorPath {
        val vectorPath = VectorPath()
        vectorPath.addPoints(pathPoints)
        vectorPath.generateSmoothPath(smoothingFactor, simplificationTolerance)
        return vectorPath
    }

    /**
     * 转换为Compose Path
     */
    fun toComposePath(): Path {
        return generateSmoothPath().toComposePath()
    }

    /**
     * 应用平滑算法
     */
    fun applySmoothing(algorithm: SmoothingAlgorithm): VectorStroke {
        val smoothedPoints = when (algorithm) {
            SmoothingAlgorithm.CATMULL_ROM ->
                PathSmoothing.catmullRomSmooth(pathPoints, smoothingFactor)
            SmoothingAlgorithm.GAUSSIAN ->
                PathSmoothing.gaussianSmooth(pathPoints)
            SmoothingAlgorithm.MOVING_AVERAGE ->
                PathSmoothing.movingAverageSmooth(pathPoints)
            SmoothingAlgorithm.ADAPTIVE ->
                PathSmoothing.adaptiveSmooth(pathPoints)
            SmoothingAlgorithm.PRESSURE_AWARE ->
                PathSmoothing.pressureAwareSmooth(pathPoints)
        }

        return copy(
            pathPoints = smoothedPoints,
            bounds = calculateBounds(smoothedPoints)?.let { Json.encodeToString(it) } ?: "{}"
        )
    }

    /**
     * 简化路径
     */
    fun simplify(tolerance: Float = simplificationTolerance): VectorStroke {
        if (pathPoints.size <= 2) return this

        val vectorPath = VectorPath()
        vectorPath.addPoints(pathPoints)
        // 使用Douglas-Peucker算法简化
        val simplifiedPath = VectorPath()
        simplifiedPath.addPoints(pathPoints)
        simplifiedPath.generateSmoothPath(smoothingFactor, tolerance)

        return copy(
            pathPoints = simplifiedPath.getPathPoints(),
            simplificationTolerance = tolerance
        )
    }

    /**
     * 变换笔迹（平移、缩放、旋转）
     */
    fun transform(
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        rotation: Float = 0f,
        pivotX: Float = 0f,
        pivotY: Float = 0f
    ): VectorStroke {
        val transformedPoints = pathPoints.map { point ->
            var x = point.x
            var y = point.y

            // 平移到旋转中心
            x -= pivotX
            y -= pivotY

            // 缩放
            x *= scaleX
            y *= scaleY

            // 旋转
            if (rotation != 0f) {
                val cos = kotlin.math.cos(rotation)
                val sin = kotlin.math.sin(rotation)
                val newX = x * cos - y * sin
                val newY = x * sin + y * cos
                x = newX.toFloat()
                y = newY.toFloat()
            }

            // 平移回原位置并应用偏移
            x += pivotX + offsetX
            y += pivotY + offsetY

            point.copy(x = x, y = y)
        }

        return copy(
            pathPoints = transformedPoints,
            bounds = calculateBounds(transformedPoints)?.let { Json.encodeToString(it) } ?: "{}"
        )
    }

    /**
     * 裁剪笔迹到指定区域
     */
    fun clipToRegion(region: VectorBounds): VectorStroke? {
        val clippedPoints = pathPoints.filter { point ->
            region.contains(point.x, point.y)
        }

        if (clippedPoints.isEmpty()) return null

        return copy(
            pathPoints = clippedPoints,
            bounds = calculateBounds(clippedPoints)?.let { Json.encodeToString(it) } ?: "{}"
        )
    }

    /**
     * 检查笔迹是否与区域相交
     */
    fun intersects(region: VectorBounds): Boolean {
        val strokeBounds = getBounds() ?: return false
        return strokeBounds.intersects(region)
    }

    /**
     * 获取笔迹长度
     */
    fun getLength(): Float {
        if (pathPoints.size < 2) return 0f

        var length = 0f
        for (i in 1 until pathPoints.size) {
            length += pathPoints[i - 1].distanceTo(pathPoints[i])
        }
        return length
    }

    /**
     * 获取笔迹点数
     */
    fun getPointCount(): Int = pathPoints.size

    /**
     * 检查笔迹是否为空
     */
    fun isEmpty(): Boolean = pathPoints.isEmpty()

    /**
     * 更新笔迹属性
     */
    fun updateProperties(newProperties: Map<String, String>): VectorStroke {
        return copy(properties = properties + newProperties)
    }

    /**
     * 更新颜色
     */
    fun updateColor(newColor: Color): VectorStroke {
        return copy(color = newColor.toArgb())
    }

    /**
     * 更新宽度
     */
    fun updateWidth(newWidth: Float): VectorStroke {
        return copy(width = newWidth.coerceAtLeast(0.1f))
    }

    /**
     * 更新透明度
     */
    fun updateOpacity(newOpacity: Float): VectorStroke {
        return copy(opacity = newOpacity.coerceIn(0f, 1f))
    }

    /**
     * 转换为JSON字符串
     */
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    /**
     * 创建副本
     */
    fun duplicate(): VectorStroke {
        return copy(id = UUID.randomUUID().toString())
    }
}

/**
 * 矢量笔迹样式枚举
 */
enum class VectorStrokeStyle(val displayName: String) {
    /** 实线 */
    SOLID("实线"),

    /** 虚线 */
    DASHED("虚线"),

    /** 点线 */
    DOTTED("点线"),

    /** 点划线 */
    DASH_DOT("点划线"),

    /** 双点划线 */
    DASH_DOT_DOT("双点划线");

    companion object {
        fun fromName(name: String): VectorStrokeStyle? =
            values().find { it.name.equals(name, ignoreCase = true) }
    }
}

/**
 * 平滑算法枚举
 */
enum class SmoothingAlgorithm(val displayName: String) {
    /** Catmull-Rom样条 */
    CATMULL_ROM("Catmull-Rom样条"),

    /** 高斯平滑 */
    GAUSSIAN("高斯平滑"),

    /** 移动平均 */
    MOVING_AVERAGE("移动平均"),

    /** 自适应平滑 */
    ADAPTIVE("自适应平滑"),

    /** 压力感应平滑 */
    PRESSURE_AWARE("压力感应平滑");
}

/**
 * 矢量笔迹集合
 * 管理多个矢量笔迹
 */
class VectorStrokeCollection {
    private val strokes = mutableListOf<VectorStroke>()

    /**
     * 添加笔迹
     */
    fun addStroke(stroke: VectorStroke) {
        strokes.add(stroke)
    }

    /**
     * 移除笔迹
     */
    fun removeStroke(strokeId: String): Boolean {
        return strokes.removeAll { it.id == strokeId }
    }

    /**
     * 获取所有笔迹
     */
    fun getAllStrokes(): List<VectorStroke> = strokes.toList()

    /**
     * 根据ID查找笔迹
     */
    fun findStroke(strokeId: String): VectorStroke? {
        return strokes.find { it.id == strokeId }
    }

    /**
     * 获取指定区域内的笔迹
     */
    fun getStrokesInRegion(region: VectorBounds): List<VectorStroke> {
        return strokes.filter { it.intersects(region) }
    }

    /**
     * 清空所有笔迹
     */
    fun clear() {
        strokes.clear()
    }

    /**
     * 获取笔迹数量
     */
    fun getStrokeCount(): Int = strokes.size

    /**
     * 获取总边界框
     */
    fun getBounds(): VectorBounds? {
        val bounds = strokes.mapNotNull { it.getBounds() }
        if (bounds.isEmpty()) return null

        val minX = bounds.minOf { it.left }
        val maxX = bounds.maxOf { it.right }
        val minY = bounds.minOf { it.top }
        val maxY = bounds.maxOf { it.bottom }

        return VectorBounds(minX, minY, maxX, maxY)
    }
}