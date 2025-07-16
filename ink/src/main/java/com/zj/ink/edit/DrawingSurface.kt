package com.zj.ink.edit

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Picture
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.ink.authoring.InProgressStrokeId
import androidx.ink.authoring.InProgressStrokesFinishedListener
import androidx.ink.authoring.InProgressStrokesView
import androidx.ink.brush.Brush
import androidx.ink.geometry.ImmutableBox
import androidx.ink.geometry.ImmutableVec
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke
import androidx.input.motionprediction.MotionEventPredictor
import com.zj.data.utils.saveBitmapToFile
import com.zj.ink.data.EditNoteViewModel
import com.zj.data.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@SuppressLint("ClickableViewAccessibility", "RestrictedApi", "NewApi")
@Composable
fun DrawingSurface(
    viewModel: EditNoteViewModel = hiltViewModel<EditNoteViewModel>(),
    onStrokeFinished: (Set<Stroke>) -> Unit, // 新增回调
    onSaveCompleted: (File) -> Unit, // 新增保存完成回调
) {
    val finishedStrokesState = viewModel.finishedStrokes
    val selectedColor = viewModel.selectedColor
    val selectedBrushFamily = viewModel.selectedBrushFamily
    val selectedBrushSize = viewModel.selectedBrushSize

    // 在 DrawingSurface 的 touchListener 中添加坐标记录
    var currentTouchX by remember { mutableFloatStateOf(0f) }
    var currentTouchY by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    val rootViewState = remember { mutableStateOf<FrameLayout?>(null) }
    val scope = rememberCoroutineScope()
    var inProgressStrokesView by remember { mutableStateOf<InProgressStrokesView?>(null) }
    val canvasStrokeRenderer = CanvasStrokeRenderer.create()
    val currentPointerId = remember { mutableStateOf<Int?>(null) }
    val currentStrokeId = remember { mutableStateOf<InProgressStrokeId?>(null) }
    val defaultBrush = remember {
        mutableStateOf<Brush>(
            Brush.createWithColorIntArgb(
                family = selectedBrushFamily.value,
                colorIntArgb = selectedColor.intValue,
                size = selectedBrushSize.floatValue,
                epsilon = 0.1F
            )
        )
    }
    defaultBrush.value = defaultBrush.value.copyWithColorIntArgb(
        family = selectedBrushFamily.value, colorIntArgb = selectedColor.intValue,
        size = selectedBrushSize.floatValue
    )

    val color = colorResource(R.color.dialog_background)
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val rootView = FrameLayout(context)
                rootViewState.value = rootView // 保存 root view 引用
                InProgressStrokesView(context).apply {
                    eagerInit()
                    addFinishedStrokesListener(object : InProgressStrokesFinishedListener {
                        override fun onStrokesFinished(strokes: Map<InProgressStrokeId, Stroke>) {
                            finishedStrokesState.value += strokes.values
                            inProgressStrokesView?.removeFinishedStrokes(strokes.keys)
                            // Caller must recompose from callback strokes, cannot wait until a later frame.
                            removeFinishedStrokes(strokes.keys)
                            // 触发回调更新历史记录
                            onStrokeFinished(finishedStrokesState.value)
                        }
                    })
                    inProgressStrokesView = this
                }
                val predictor = MotionEventPredictor.newInstance(rootView)
                val touchListener = View.OnTouchListener { view, event ->
                    predictor.record(event)
                    val predictedEvent = predictor.predict()

                    try {
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                if (viewModel.isEraserMode.value) {
                                    // 橡皮擦模式：记录擦除起始点
                                    val eraserRadius = viewModel.eraserRadius.floatValue
                                    val eraserCenter = ImmutableVec(event.x, event.y)
                                    val eraserBox = ImmutableBox.fromCenterAndDimensions(
                                        eraserCenter,
                                        eraserRadius * 2,
                                        eraserRadius * 2
                                    )
                                    // 触发初始擦除
                                    erasePartialStrokes(
                                        eraserBox,
                                        finishedStrokesState,
                                        viewModel.drawUndoStack,
                                        viewModel.drawRedoStack
                                    )
                                    true
                                } else {
                                    if (viewModel.isEraserMode.value) {
                                        // 橡皮擦模式：无需特殊处理，直接返回 true
                                        true
                                    } else {
                                        // First pointer - treat it as inking.
                                        view.requestUnbufferedDispatch(event)
                                        val pointerIndex = event.actionIndex
                                        val pointerId = event.getPointerId(pointerIndex)
                                        currentPointerId.value = pointerId
                                        currentStrokeId.value = inProgressStrokesView?.startStroke(
                                            event = event,
                                            pointerId = pointerId,
                                            brush = defaultBrush.value
                                        )
                                        true
                                    }
                                }
                            }

                            MotionEvent.ACTION_MOVE -> {
                                // 在 MotionEvent.ACTION_MOVE 中更新坐标：
                                if (viewModel.isEraserMode.value) {
                                    val eraserRadius = viewModel.eraserRadius.floatValue
                                    val eraserCenter = ImmutableVec(event.x, event.y)
                                    val eraserBox = ImmutableBox.fromCenterAndDimensions(
                                        eraserCenter,
                                        eraserRadius * 2,
                                        eraserRadius * 2
                                    )
                                    // 调用擦除函数（需通过 ViewModel 触发）
                                    erasePartialStrokes(
                                        eraserBox,
                                        finishedStrokesState,
                                        viewModel.drawUndoStack,
                                        viewModel.drawRedoStack
                                    )
                                    true
                                } else {
                                    currentTouchX = event.x
                                    currentTouchY = event.y
                                    val pointerId = checkNotNull(currentPointerId.value)
                                    val strokeId = checkNotNull(currentStrokeId.value)

                                    for (pointerIndex in 0 until event.pointerCount) {
                                        if (event.getPointerId(pointerIndex) != pointerId) continue
                                        inProgressStrokesView?.addToStroke(
                                            event, pointerId, strokeId, predictedEvent
                                        )
                                    }
                                    true
                                }


                            }

                            MotionEvent.ACTION_UP -> {
                                val pointerIndex = event.actionIndex
                                val pointerId = event.getPointerId(pointerIndex)
                                check(pointerId == currentPointerId.value)
                                val currentStrokeId = checkNotNull(currentStrokeId.value)
                                inProgressStrokesView?.finishStroke(
                                    event, pointerId, currentStrokeId
                                )
                                view.performClick()
                                true
                            }

                            MotionEvent.ACTION_CANCEL -> {
                                val pointerIndex = event.actionIndex
                                val pointerId = event.getPointerId(pointerIndex)
                                check(pointerId == currentPointerId.value)

                                val currentStrokeId = checkNotNull(currentStrokeId.value)
                                inProgressStrokesView?.cancelStroke(currentStrokeId, event)
                                true
                            }

                            else -> false
                        }
                    } finally {
                        predictedEvent?.recycle()
                    }

                }
                rootView.setOnTouchListener(touchListener)
                rootView.addView(inProgressStrokesView)
                rootView
            },
        )

        Canvas(modifier = Modifier) {
            val canvasTransform = Matrix()
            drawContext.canvas.nativeCanvas.concat(canvasTransform)
            val canvas = drawContext.canvas.nativeCanvas

            finishedStrokesState.value.forEach { stroke ->
                canvasStrokeRenderer.draw(
                    stroke = stroke, canvas = canvas, strokeToScreenTransform = canvasTransform
                )
            }
        }

        if (viewModel.saveBitmap.value) {
            LaunchedEffect(Unit) {
                if (finishedStrokesState.value.isNotEmpty()) {
                    // 获取 root view 的实际尺寸
                    val rootView = rootViewState.value
                    val width = rootView?.width ?: 1000 // 默认值防崩溃
                    val height = rootView?.height ?: 1000

                    recordCanvasToBitmap(
                        strokes = finishedStrokesState.value.toList(),
                        canvasStrokeRenderer = canvasStrokeRenderer,
                        canvasTransform = Matrix(),
                        width = width,
                        height = height,
                        color = color,
                        onBitmap = { bitmap ->
                            scope.launch {
                                val file = saveBitmapToFile(
                                    context, bitmap, "${System.currentTimeMillis()}"
                                )
                                // 保存完成后触发回调
                                file?.let { onSaveCompleted(it) }
                            }
                        })
                }
            }
        }
    }
}

// 修改 recordCanvasToBitmap 函数：
@RequiresApi(Build.VERSION_CODES.P)
suspend fun recordCanvasToBitmap(
    strokes: List<Stroke>,
    canvasStrokeRenderer: CanvasStrokeRenderer,
    canvasTransform: Matrix?,
    width: Int, // 新增参数：动态宽度
    height: Int, // 新增参数：动态高度
    onBitmap: (Bitmap) -> Unit,
    color: androidx.compose.ui.graphics.Color,
) = withContext(Dispatchers.Default) {
    val picture = Picture()
    val canvas = picture.beginRecording(width, height) // 使用动态尺寸
    // 设置白色背景
    canvas.drawColor(color.toArgb())
    // 应用变换并绘制
    canvas.concat(canvasTransform ?: Matrix())
    strokes.forEach { stroke ->
        canvasStrokeRenderer.draw(
            stroke = stroke, canvas = canvas, strokeToScreenTransform = canvasTransform ?: Matrix()
        )
    }
    picture.endRecording()
    val bitmap = Bitmap.createBitmap(picture)
    onBitmap(bitmap)
}
