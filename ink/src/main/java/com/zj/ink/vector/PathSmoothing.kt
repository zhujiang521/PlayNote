package com.zj.ink.vector

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 路径平滑算法工具类
 * 提供多种路径平滑和优化算法
 */
object PathSmoothing {

    /**
     * Catmull-Rom样条平滑
     * 生成通过所有控制点的平滑曲线
     */
    fun catmullRomSmooth(
        points: List<VectorPoint>,
        tension: Float = 0.5f,
        segments: Int = 10
    ): List<VectorPoint> {
        if (points.size < 2) return points
        if (points.size == 2) return points

        val smoothedPoints = mutableListOf<VectorPoint>()

        // 为端点添加虚拟控制点
        val extendedPoints = mutableListOf<VectorPoint>()
        extendedPoints.add(points[0]) // 重复第一个点
        extendedPoints.addAll(points)
        extendedPoints.add(points.last()) // 重复最后一个点

        // 对每个段进行平滑
        for (i in 1 until extendedPoints.size - 2) {
            val p0 = extendedPoints[i - 1]
            val p1 = extendedPoints[i]
            val p2 = extendedPoints[i + 1]
            val p3 = extendedPoints[i + 2]

            smoothedPoints.add(p1) // 添加起始点

            // 在两点之间插入平滑点
            for (j in 1 until segments) {
                val t = j.toFloat() / segments
                val point = catmullRomInterpolation(p0, p1, p2, p3, t, tension)
                smoothedPoints.add(point)
            }
        }

        // 添加最后一个点
        smoothedPoints.add(points.last())

        return smoothedPoints
    }

    /**
     * Catmull-Rom插值计算
     */
    private fun catmullRomInterpolation(
        p0: VectorPoint,
        p1: VectorPoint,
        p2: VectorPoint,
        p3: VectorPoint,
        t: Float,
        tension: Float
    ): VectorPoint {
        val t2 = t * t
        val t3 = t2 * t

        // Catmull-Rom基函数
        val f1 = -tension * t + 2 * tension * t2 - tension * t3
        val f2 = 1 + (tension - 3) * t2 + (2 - tension) * t3
        val f3 = tension * t + (3 - 2 * tension) * t2 + (tension - 2) * t3
        val f4 = -tension * t2 + tension * t3

        val x = f1 * p0.x + f2 * p1.x + f3 * p2.x + f4 * p3.x
        val y = f1 * p0.y + f2 * p1.y + f3 * p2.y + f4 * p3.y
        val pressure = f1 * p0.pressure + f2 * p1.pressure + f3 * p2.pressure + f4 * p3.pressure

        return VectorPoint(x, y, pressure.coerceIn(0f, 1f))
    }

    /**
     * B样条平滑
     * 生成不一定通过控制点的平滑曲线
     */
    fun bSplineSmooth(
        points: List<VectorPoint>,
        degree: Int = 3,
        segments: Int = 10
    ): List<VectorPoint> {
        if (points.size < degree + 1) return points

        val smoothedPoints = mutableListOf<VectorPoint>()
        val n = points.size - 1
        val p = degree

        // 生成节点向量
        val knots = generateKnotVector(n, p)

        // 计算B样条曲线上的点
        for (i in 0..segments * (n - p + 1)) {
            val u = i.toFloat() / (segments * (n - p + 1)) * (knots[n + 1] - knots[p]) + knots[p]
            val point = evaluateBSpline(points, knots, p, u)
            smoothedPoints.add(point)
        }

        return smoothedPoints
    }

    /**
     * 生成B样条节点向量
     */
    private fun generateKnotVector(n: Int, p: Int): FloatArray {
        val m = n + p + 1
        val knots = FloatArray(m + 1)

        // 钳位节点向量
        for (i in 0..p) knots[i] = 0f
        for (i in m - p..m) knots[i] = 1f

        for (i in p + 1 until m - p) {
            knots[i] = (i - p).toFloat() / (n - p + 1)
        }

        return knots
    }

    /**
     * 计算B样条曲线上的点
     */
    private fun evaluateBSpline(
        points: List<VectorPoint>,
        knots: FloatArray,
        degree: Int,
        u: Float
    ): VectorPoint {
        val n = points.size - 1
        var x = 0f
        var y = 0f
        var pressure = 0f

        for (i in 0..n) {
            val basis = basisFunction(i, degree, u, knots)
            x += basis * points[i].x
            y += basis * points[i].y
            pressure += basis * points[i].pressure
        }

        return VectorPoint(x, y, pressure.coerceIn(0f, 1f))
    }

    /**
     * B样条基函数
     */
    private fun basisFunction(i: Int, p: Int, u: Float, knots: FloatArray): Float {
        if (p == 0) {
            return if (u >= knots[i] && u < knots[i + 1]) 1f else 0f
        }

        var left = 0f
        var right = 0f

        if (knots[i + p] != knots[i]) {
            left = (u - knots[i]) / (knots[i + p] - knots[i]) * basisFunction(i, p - 1, u, knots)
        }

        if (knots[i + p + 1] != knots[i + 1]) {
            right = (knots[i + p + 1] - u) / (knots[i + p + 1] - knots[i + 1]) * basisFunction(i + 1, p - 1, u, knots)
        }

        return left + right
    }

    /**
     * 高斯平滑滤波
     * 对路径点进行高斯模糊处理
     */
    fun gaussianSmooth(
        points: List<VectorPoint>,
        sigma: Float = 1.0f,
        kernelSize: Int = 5
    ): List<VectorPoint> {
        if (points.size < kernelSize) return points

        val kernel = generateGaussianKernel(kernelSize, sigma)
        val smoothedPoints = mutableListOf<VectorPoint>()
        val halfKernel = kernelSize / 2

        for (i in points.indices) {
            var x = 0f
            var y = 0f
            var pressure = 0f
            var weightSum = 0f

            for (j in -halfKernel..halfKernel) {
                val index = (i + j).coerceIn(0, points.size - 1)
                val weight = kernel[j + halfKernel]

                x += points[index].x * weight
                y += points[index].y * weight
                pressure += points[index].pressure * weight
                weightSum += weight
            }

            smoothedPoints.add(VectorPoint(
                x / weightSum,
                y / weightSum,
                (pressure / weightSum).coerceIn(0f, 1f),
                points[i].timestamp
            ))
        }

        return smoothedPoints
    }

    /**
     * 生成高斯核
     */
    private fun generateGaussianKernel(size: Int, sigma: Float): FloatArray {
        val kernel = FloatArray(size)
        val halfSize = size / 2
        var sum = 0f

        for (i in 0 until size) {
            val x = i - halfSize
            val value = (1f / (sigma * sqrt(2 * Math.PI))).toFloat() *
                       kotlin.math.exp(-(x * x) / (2 * sigma * sigma))
            kernel[i] = value
            sum += value
        }

        // 归一化
        for (i in 0 until size) {
            kernel[i] /= sum
        }

        return kernel
    }

    /**
     * 移动平均平滑
     * 简单的滑动窗口平均
     */
    fun movingAverageSmooth(
        points: List<VectorPoint>,
        windowSize: Int = 3
    ): List<VectorPoint> {
        if (points.size < windowSize) return points

        val smoothedPoints = mutableListOf<VectorPoint>()
        val halfWindow = windowSize / 2

        for (i in points.indices) {
            var x = 0f
            var y = 0f
            var pressure = 0f
            var count = 0

            for (j in -halfWindow..halfWindow) {
                val index = i + j
                if (index in points.indices) {
                    x += points[index].x
                    y += points[index].y
                    pressure += points[index].pressure
                    count++
                }
            }

            smoothedPoints.add(VectorPoint(
                x / count,
                y / count,
                (pressure / count).coerceIn(0f, 1f),
                points[i].timestamp
            ))
        }

        return smoothedPoints
    }

    /**
     * 自适应平滑
     * 根据曲率自动调整平滑强度
     */
    fun adaptiveSmooth(
        points: List<VectorPoint>,
        maxSmoothingStrength: Float = 0.5f,
        curvatureThreshold: Float = 0.1f
    ): List<VectorPoint> {
        if (points.size < 3) return points

        val smoothedPoints = mutableListOf<VectorPoint>()
        smoothedPoints.add(points.first()) // 保持第一个点不变

        for (i in 1 until points.size - 1) {
            val prev = points[i - 1]
            val current = points[i]
            val next = points[i + 1]

            // 计算曲率
            val curvature = calculateCurvature(prev, current, next)

            // 根据曲率调整平滑强度
            val smoothingStrength = if (curvature > curvatureThreshold) {
                maxSmoothingStrength * (curvature / (curvature + curvatureThreshold))
            } else {
                0f
            }

            // 应用平滑
            val smoothedX = current.x + (prev.x + next.x - 2 * current.x) * smoothingStrength * 0.5f
            val smoothedY = current.y + (prev.y + next.y - 2 * current.y) * smoothingStrength * 0.5f
            val smoothedPressure = current.pressure +
                                  (prev.pressure + next.pressure - 2 * current.pressure) * smoothingStrength * 0.5f

            smoothedPoints.add(VectorPoint(
                smoothedX,
                smoothedY,
                smoothedPressure.coerceIn(0f, 1f),
                current.timestamp
            ))
        }

        smoothedPoints.add(points.last()) // 保持最后一个点不变
        return smoothedPoints
    }

    /**
     * 计算三点间的曲率
     */
    private fun calculateCurvature(p1: VectorPoint, p2: VectorPoint, p3: VectorPoint): Float {
        val dx1 = p2.x - p1.x
        val dy1 = p2.y - p1.y
        val dx2 = p3.x - p2.x
        val dy2 = p3.y - p2.y

        // 计算角度变化
        val angle1 = kotlin.math.atan2(dy1, dx1)
        val angle2 = kotlin.math.atan2(dy2, dx2)

        var deltaAngle = angle2 - angle1

        // 标准化角度到 [-π, π]
        while (deltaAngle > Math.PI.toFloat()) deltaAngle -= 2 * Math.PI.toFloat()
        while (deltaAngle < -Math.PI.toFloat()) deltaAngle += 2 * Math.PI.toFloat()

        return abs(deltaAngle)
    }

    /**
     * 压力感应平滑
     * 根据压力值调整平滑程度
     */
    fun pressureAwareSmooth(
        points: List<VectorPoint>,
        baseSmoothingFactor: Float = 0.3f
    ): List<VectorPoint> {
        if (points.size < 3) return points

        val smoothedPoints = mutableListOf<VectorPoint>()
        smoothedPoints.add(points.first())

        for (i in 1 until points.size - 1) {
            val prev = points[i - 1]
            val current = points[i]
            val next = points[i + 1]

            // 根据压力调整平滑因子
            val pressureFactor = 1f - current.pressure // 压力越大，平滑越少
            val smoothingFactor = baseSmoothingFactor * pressureFactor

            // 应用加权平均
            val weight = smoothingFactor
            val x = current.x * (1 - weight) + (prev.x + next.x) * weight * 0.5f
            val y = current.y * (1 - weight) + (prev.y + next.y) * weight * 0.5f

            smoothedPoints.add(VectorPoint(x, y, current.pressure, current.timestamp))
        }

        smoothedPoints.add(points.last())
        return smoothedPoints
    }
}