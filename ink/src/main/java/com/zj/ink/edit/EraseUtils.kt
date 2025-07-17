package com.zj.ink.edit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.Snapshot
import androidx.ink.geometry.AffineTransform
import androidx.ink.geometry.ImmutableBox
import androidx.ink.geometry.ImmutableVec
import androidx.ink.geometry.Intersection.intersects
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.ink.strokes.Stroke
import androidx.ink.strokes.StrokeInput
import kotlin.collections.forEach

/**
 * 清空画布
 */
fun eraseWholeStrokes(
    eraserBox: ImmutableBox,
    finishedStrokesState: MutableState<Set<Stroke>>,
    undoStack: MutableState<MutableList<Set<Stroke>>>, // 新增参数
    redoStack: MutableState<MutableList<Set<Stroke>>>, // 新增参数
) {
    val threshold = 0.1f

    val strokesToErase = finishedStrokesState.value.filter { stroke ->
        stroke.shape.computeCoverageIsGreaterThan(
            box = eraserBox,
            coverageThreshold = threshold,
        )
    }
    if (strokesToErase.isNotEmpty()) {
        Snapshot.withMutableSnapshot {
            val newStrokes = finishedStrokesState.value - strokesToErase.toSet()
            finishedStrokesState.value = newStrokes

            // 更新历史栈
            val newUndoStack = undoStack.value.toMutableList().apply { add(newStrokes) }
            undoStack.value = newUndoStack

            redoStack.value = mutableListOf() // 清空 Redo 栈
        }
    }
}

/**
 * 橡皮擦
 */
fun erasePartialStrokes(
    eraserBox: ImmutableBox,
    finishedStrokesState: MutableState<Set<Stroke>>,
    undoStack: MutableState<MutableList<Set<Stroke>>>,
    redoStack: MutableState<MutableList<Set<Stroke>>>,
) {
    val updatedStrokes = mutableSetOf<Stroke>()

    finishedStrokesState.value.forEach { stroke ->
        val shape = stroke.shape
        val intersection = shape.intersects(
            eraserBox,
            meshToBox = AffineTransform.IDENTITY,
        )

        if (intersection) { // 存在交集，需要分割
            val segments = mutableListOf<List<StrokeInput>>()
            val inputs = stroke.inputs
            var currentSegment = mutableListOf<StrokeInput>()
            var inEraser = false

            for (index in 0 until inputs.size) {
                val input = inputs[index]
                val point = ImmutableVec(input.x, input.y)
                val isInside = eraserBox.contains(point)

                if (isInside) {
                    if (currentSegment.size >= 2) {
                        segments.add(currentSegment.toList())
                        currentSegment = mutableListOf()
                    }
                    inEraser = true
                } else {
                    if (inEraser) {
                        currentSegment = mutableListOf()
                        inEraser = false
                    }
                    currentSegment.add(input)
                }
            }
            if (currentSegment.isNotEmpty()) {
                segments.add(currentSegment.toList())
            }

            segments.forEach { segment ->
                if (segment.size >= 2) { // 至少两个点才能形成线段
                    val newInputs = MutableStrokeInputBatch()
                    segment.forEach { input -> newInputs.add(input) }
                    val newStroke = Stroke(stroke.brush, newInputs)
                    updatedStrokes.add(newStroke)
                }
            }
        } else { // 无交集，保留原笔触
            updatedStrokes.add(stroke)
        }
    }

    if (updatedStrokes != finishedStrokesState.value) {
        Snapshot.withMutableSnapshot {
            finishedStrokesState.value = updatedStrokes
            undoStack.value.add(finishedStrokesState.value)
            redoStack.value.clear()
        }
    }
}
