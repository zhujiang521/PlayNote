package com.zj.ink.vector

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 矢量路径点
 * 表示路径中的一个控制点
 */
@Serializable
data class VectorPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 计算到另一点的距离
     */
    fun distanceTo(other: VectorPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * 计算两点的中点
     */
    fun midPoint(other: VectorPoint): VectorPoint {
        return VectorPoint(
            x = (x + other.x) / 2f,
            y = (y + other.y) / 2f,
            pressure = (pressure + other.pressure) / 2f
        )
    }

    /**
     * 线性插值
     */
    fun lerp(other: VectorPoint, t: Float): VectorPoint {
        val clampedT = t.coerceIn(0f, 1f)
        return VectorPoint(
            x = x + (other.x - x) * clampedT,
            y = y + (other.y - y) * clampedT,
            pressure = pressure + (other.pressure - pressure) * clampedT
        )
    }
}

/**
 * 贝塞尔曲线段
 * 表示一个三次贝塞尔曲线
 */
@Serializable
data class BezierSegment(
    val start: VectorPoint,
    val control1: VectorPoint,
    val control2: VectorPoint,
    val end: VectorPoint
) {
    /**
     * 在曲线上获取指定参数t的点
     * @param t 参数，范围[0, 1]
     */
    fun getPointAt(t: Float): VectorPoint {
        val clampedT = t.coerceIn(0f, 1f)
        val oneMinusT = 1f - clampedT
        val t2 = clampedT * clampedT
        val t3 = t2 * clampedT
        val oneMinusT2 = oneMinusT * oneMinusT
        val oneMinusT3 = oneMinusT2 * oneMinusT

        // 三次贝塞尔曲线公式
        val x = oneMinusT3 * start.x +
                3 * oneMinusT2 * clampedT * control1.x +
                3 * oneMinusT * t2 * control2.x +
                t3 * end.x

        val y = oneMinusT3 * start.y +
                3 * oneMinusT2 * clampedT * control1.y +
                3 * oneMinusT * t2 * control2.y +
                t3 * end.y

        val pressure = oneMinusT3 * start.pressure +
                      3 * oneMinusT2 * clampedT * control1.pressure +
                      3 * oneMinusT * t2 * control2.pressure +
                      t3 * end.pressure

        return VectorPoint(x, y, pressure)
    }

    /**
     * 获取曲线在指定参数t处的切线方向
     */
    fun getTangentAt(t: Float): VectorPoint {
        val clampedT = t.coerceIn(0f, 1f)
        val oneMinusT = 1f - clampedT
        val t2 = clampedT * clampedT
        val oneMinusT2 = oneMinusT * oneMinusT

        // 三次贝塞尔曲线的导数
        val dx = -3 * oneMinusT2 * start.x +
                 3 * oneMinusT2 * control1.x -
                 6 * oneMinusT * clampedT * control1.x +
                 6 * oneMinusT * clampedT * control2.x -
                 3 * t2 * control2.x +
                 3 * t2 * end.x

        val dy = -3 * oneMinusT2 * start.y +
                 3 * oneMinusT2 * control1.y -
                 6 * oneMinusT * clampedT * control1.y +
                 6 * oneMinusT * clampedT * control2.y -
                 3 * t2 * control2.y +
                 3 * t2 * end.y

        return VectorPoint(dx, dy)
    }

    /**
     * 计算曲线长度（近似）
     */
    fun getLength(steps: Int = 10): Float {
        var length = 0f
        var prevPoint = start

        for (i in 1..steps) {
            val t = i.toFloat() / steps
            val currentPoint = getPointAt(t)
            length += prevPoint.distanceTo(currentPoint)
            prevPoint = currentPoint
        }

        return length
    }
}

/**
 * 矢量路径
 * 由多个贝塞尔曲线段组成的平滑路径
 */
class VectorPath {
    private val segments = mutableListOf<BezierSegment>()
    private val rawPoints = mutableListOf<VectorPoint>()

    /**
     * 添加原始点
     */
    fun addPoint(point: VectorPoint) {
        rawPoints.add(point)
    }

    /**
     * 添加多个原始点
     */
    fun addPoints(points: List<VectorPoint>) {
        rawPoints.addAll(points)
    }

    /**
     * 生成平滑的贝塞尔曲线路径
     */
    fun generateSmoothPath(
        smoothingFactor: Float = 0.3f,
        simplificationTolerance: Float = 2.0f
    ) {
        if (rawPoints.size < 2) return

        // 1. 简化路径点
        val simplifiedPoints = simplifyPath(rawPoints, simplificationTolerance)

        if (simplifiedPoints.size < 2) return

        // 2. 生成贝塞尔曲线段
        segments.clear()

        if (simplifiedPoints.size == 2) {
            // 只有两个点，创建直线段
            val start = simplifiedPoints[0]
            val end = simplifiedPoints[1]
            val control1 = start.lerp(end, 0.33f)
            val control2 = start.lerp(end, 0.67f)
            segments.add(BezierSegment(start, control1, control2, end))
            return
        }

        // 3. 为每两个相邻点创建贝塞尔曲线段
        for (i in 0 until simplifiedPoints.size - 1) {
            val p0 = if (i > 0) simplifiedPoints[i - 1] else simplifiedPoints[i]
            val p1 = simplifiedPoints[i]
            val p2 = simplifiedPoints[i + 1]
            val p3 = if (i + 2 < simplifiedPoints.size) simplifiedPoints[i + 2] else simplifiedPoints[i + 1]

            val controlPoints = calculateControlPoints(p0, p1, p2, p3, smoothingFactor)
            segments.add(BezierSegment(p1, controlPoints.first, controlPoints.second, p2))
        }
    }

    /**
     * 使用Douglas-Peucker算法简化路径
     */
    private fun simplifyPath(points: List<VectorPoint>, tolerance: Float): List<VectorPoint> {
        if (points.size <= 2) return points

        return douglasPeucker(points, tolerance)
    }

    /**
     * Douglas-Peucker算法实现
     */
    private fun douglasPeucker(points: List<VectorPoint>, tolerance: Float): List<VectorPoint> {
        if (points.size <= 2) return points

        var maxDistance = 0f
        var maxIndex = 0
        val start = points.first()
        val end = points.last()

        // 找到距离直线最远的点
        for (i in 1 until points.size - 1) {
            val distance = pointToLineDistance(points[i], start, end)
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        // 如果最大距离小于容差，简化为直线
        if (maxDistance < tolerance) {
            return listOf(start, end)
        }

        // 递归简化两段
        val leftPart = douglasPeucker(points.subList(0, maxIndex + 1), tolerance)
        val rightPart = douglasPeucker(points.subList(maxIndex, points.size), tolerance)

        return leftPart.dropLast(1) + rightPart
    }

    /**
     * 计算点到直线的距离
     */
    private fun pointToLineDistance(point: VectorPoint, lineStart: VectorPoint, lineEnd: VectorPoint): Float {
        val A = point.x - lineStart.x
        val B = point.y - lineStart.y
        val C = lineEnd.x - lineStart.x
        val D = lineEnd.y - lineStart.y

        val dot = A * C + B * D
        val lenSq = C * C + D * D

        if (lenSq == 0f) return point.distanceTo(lineStart)

        val param = dot / lenSq

        val closestPoint = when {
            param < 0 -> lineStart
            param > 1 -> lineEnd
            else -> VectorPoint(
                lineStart.x + param * C,
                lineStart.y + param * D
            )
        }

        return point.distanceTo(closestPoint)
    }

    /**
     * 计算贝塞尔曲线的控制点
     */
    private fun calculateControlPoints(
        p0: VectorPoint,
        p1: VectorPoint,
        p2: VectorPoint,
        p3: VectorPoint,
        smoothingFactor: Float
    ): Pair<VectorPoint, VectorPoint> {

        // 计算切线向量
        val d1x = p2.x - p0.x
        val d1y = p2.y - p0.y
        val d2x = p3.x - p1.x
        val d2y = p3.y - p1.y

        // 计算距离
        val distance = p1.distanceTo(p2)
        val scale = distance * smoothingFactor

        // 计算控制点
        val control1 = VectorPoint(
            x = p1.x + d1x * scale * 0.25f,
            y = p1.y + d1y * scale * 0.25f,
            pressure = p1.pressure
        )

        val control2 = VectorPoint(
            x = p2.x - d2x * scale * 0.25f,
            y = p2.y - d2y * scale * 0.25f,
            pressure = p2.pressure
        )

        return Pair(control1, control2)
    }

    /**
     * 转换为Compose Path对象
     */
    fun toComposePath(): Path {
        val path = Path()

        if (segments.isEmpty()) {
            // 如果没有贝塞尔段，使用原始点创建简单路径
            if (rawPoints.isNotEmpty()) {
                path.moveTo(rawPoints.first().x, rawPoints.first().y)
                rawPoints.drop(1).forEach { point ->
                    path.lineTo(point.x, point.y)
                }
            }
            return path
        }

        // 使用贝塞尔段创建路径
        val firstSegment = segments.first()
        path.moveTo(firstSegment.start.x, firstSegment.start.y)

        segments.forEach { segment ->
            path.cubicTo(
                segment.control1.x, segment.control1.y,
                segment.control2.x, segment.control2.y,
                segment.end.x, segment.end.y
            )
        }

        return path
    }

    /**
     * 获取路径上的点（用于渲染）
     */
    fun getPathPoints(density: Float = 1.0f): List<VectorPoint> {
        val points = mutableListOf<VectorPoint>()

        segments.forEach { segment ->
            val length = segment.getLength()
            val steps = (length * density).toInt().coerceAtLeast(2)

            for (i in 0..steps) {
                val t = i.toFloat() / steps
                points.add(segment.getPointAt(t))
            }
        }

        return points
    }

    /**
     * 获取路径边界框
     */
    fun getBounds(): VectorBounds? {
        if (rawPoints.isEmpty()) return null

        val minX = rawPoints.minOf { it.x }
        val maxX = rawPoints.maxOf { it.x }
        val minY = rawPoints.minOf { it.y }
        val maxY = rawPoints.maxOf { it.y }

        return VectorBounds(minX, minY, maxX, maxY)
    }

    /**
     * 清空路径
     */
    fun clear() {
        segments.clear()
        rawPoints.clear()
    }

    /**
     * 获取路径长度
     */
    fun getLength(): Float {
        return segments.sumOf { it.getLength().toDouble() }.toFloat()
    }

    /**
     * 检查路径是否为空
     */
    fun isEmpty(): Boolean {
        return segments.isEmpty() && rawPoints.isEmpty()
    }

    /**
     * 获取段数
     */
    fun getSegmentCount(): Int = segments.size

    /**
     * 获取原始点数
     */
    fun getRawPointCount(): Int = rawPoints.size
}

/**
 * 矢量边界框
 */
data class VectorBounds(
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
    fun intersects(other: VectorBounds): Boolean {
        return left < other.right && right > other.left &&
               top < other.bottom && bottom > other.top
    }
}