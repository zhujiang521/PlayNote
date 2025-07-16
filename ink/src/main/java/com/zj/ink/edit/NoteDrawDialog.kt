package com.zj.ink.edit

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.zj.ink.data.EditNoteViewModel
import com.zj.ink.picker.ColorPicker
import com.zj.ink.picker.EraserSizePicker
import com.zj.ink.picker.PenPicker
import com.zj.ink.picker.PenSizePicker
import com.zj.data.R
import java.io.File

private const val TAG = "NoteDrawDialog"

@SuppressLint("UnrememberedMutableState", "MutableCollectionMutableState", "NewApi")
@Composable
fun NoteDrawDialog(
    viewModel: EditNoteViewModel = hiltViewModel<EditNoteViewModel>(),
    onFinished: (File) -> Unit = {},
) {
    if (!viewModel.showDialog.value) return

    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val screenWidthDp = with(LocalDensity.current) { screenWidthPx.toDp() }
    val screenHeightPx = displayMetrics.heightPixels
    val screenHeightDp = with(LocalDensity.current) { screenHeightPx.toDp() }

    val dialogWidth = if (screenWidthDp < 600.dp) {
        screenWidthDp - 20.dp
    } else {
        screenWidthDp * 0.85f
    }

    val dialogHeight = screenHeightDp * 0.85f

    val finishedStrokesState = viewModel.finishedStrokes
    val selectedColor = viewModel.selectedColor
    val selectedBrushFamily = viewModel.selectedBrushFamily
    val undoStackState = viewModel.drawUndoStack
    val redoStackState = viewModel.drawRedoStack
    val selectedBrushSize = viewModel.selectedBrushSize
    val isUndoEnabled by remember { derivedStateOf { undoStackState.value.size > 1 } }
    val isRedoEnabled by remember { derivedStateOf { redoStackState.value.isNotEmpty() } }
    val eraserIconTint by remember {
        derivedStateOf {
            if (viewModel.isEraserMode.value) {
                R.color.primary
            } else {
                null
            }
        }
    }
    val canErase by remember { derivedStateOf { finishedStrokesState.value.isNotEmpty() } }

    Dialog(onDismissRequest = {
        viewModel.showDialog.value = false
    }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .width(dialogWidth)
                .height(dialogHeight)
                .padding(dimensionResource(R.dimen.screen_horizontal_margin)),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.dialog_background)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyRow(modifier = Modifier.weight(1f)) {
                        actionIconButton(
                            drawableRes = R.drawable.baseline_undo,
                            descriptionRes = R.string.undo,
                            enable = isUndoEnabled
                        ) {
                            if (undoStackState.value.size > 1) {
                                val currentState = undoStackState.value.last()
                                val newRedoStack = redoStackState.value.toMutableList()
                                    .apply { add(currentState) }
                                redoStackState.value = newRedoStack

                                val newUndoStack = undoStackState.value.subList(
                                    0,
                                    undoStackState.value.size - 1
                                )
                                undoStackState.value = newUndoStack.toMutableList()

                                finishedStrokesState.value = undoStackState.value.last()
                            }
                        }
                        actionIconButton(
                            drawableRes = R.drawable.baseline_redo,
                            descriptionRes = R.string.redo,
                            enable = isRedoEnabled
                        ) {
                            if (redoStackState.value.isNotEmpty()) {
                                // 1. 先获取目标状态
                                val nextState = redoStackState.value.last()

                                val currentState = finishedStrokesState.value
                                val newUndoStack = undoStackState.value.toMutableList()
                                    .apply { add(currentState) }
                                undoStackState.value = newUndoStack

                                // 2. 移除 Redo 栈的最后一个元素
                                val newRedoStack = redoStackState.value.toMutableList()
                                    .apply { removeLast() }
                                redoStackState.value = newRedoStack

                                // 3. 使用提前获取的 nextState 更新状态
                                finishedStrokesState.value = nextState
                            }
                        }
                        actionIconButton(
                            drawableRes = R.drawable.baseline_eraser,
                            descriptionRes = R.string.eraser_size_title,
                            tintRes = eraserIconTint,
                            enable = canErase || viewModel.isEraserMode.value
                        ) {
                            if (viewModel.isEraserMode.value) {
                                viewModel.isEraserMode.value = false
                            } else {
                                viewModel.showEraserSizePicker.value = true
                            }
                        }

                        actionIconButton(
                            drawableRes = R.drawable.baseline_draw,
                            descriptionRes = R.string.draw,
                        ) {
                            viewModel.showPenPicker.value = true
                        }

                        actionIconButton(
                            drawableRes = R.drawable.baseline_format_size,
                            descriptionRes = R.string.size,
                        ) {
                            viewModel.showPenSizePicker.value = true
                        }

                        actionIconButton(
                            drawableRes = R.drawable.baseline_color_lens,
                            descriptionRes = R.string.pen_color_select,
                            tintColor = Color(selectedColor.intValue)
                        ) {
                            viewModel.showColorPicker.value = true
                        }
                    }

                    IconButton(onClick = {
                        if (finishedStrokesState.value.isNotEmpty()) {
                            viewModel.saveBitmap.value = true
                        } else {
                            viewModel.clearDrawState()
                        }
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (finishedStrokesState.value.isNotEmpty())
                                    R.drawable.baseline_check // 成功图标
                                else
                                    R.drawable.baseline_close // 关闭图标
                            ),
                            contentDescription = stringResource(R.string.note),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(color = colorResource(R.color.dialog_background))
                ) {
                    DrawingSurface(
                        viewModel = viewModel,
                        onStrokeFinished = { newStrokes ->
                            val currentState = finishedStrokesState.value + newStrokes
                            finishedStrokesState.value = currentState

                            val newUndoStack =
                                undoStackState.value.toMutableList().apply { add(currentState) }
                            undoStackState.value = newUndoStack

                            redoStackState.value = mutableListOf() // 清空 Redo 栈
                        },
                        onSaveCompleted = { file ->
                            Log.d(TAG, "NoteDrawDialog: file:${file}")
                            viewModel.saveBitmap.value = false
                            onFinished(file)
                            viewModel.clearDrawState()
                        }
                    )
                }
            }
        }
    }
    // 添加笔类型选择器
    PenPicker(
        selectedBrushFamily = selectedBrushFamily,
        expanded = viewModel.showPenPicker,
        onBrushSelected = { newBrush ->
            selectedBrushFamily.value = newBrush
        }
    )
    // 添加颜色选择器
    ColorPicker(
        selectedColor = selectedColor,
        expanded = viewModel.showColorPicker,
        onColorChange = { newArgb ->
            selectedColor.intValue = newArgb
        }
    )
    // 添加笔迹大小选择器
    PenSizePicker(
        selectedSize = selectedBrushSize,
        expanded = viewModel.showPenSizePicker,
    )
    // 在 Dialog 结尾处添加 EraserSizePicker：
    EraserSizePicker(
        viewModel = viewModel,
    )
}


fun LazyListScope.actionIconButton(
    drawableRes: Int,
    descriptionRes: Int,
    tintRes: Int? = null,
    tintColor: Color? = null,
    enable: Boolean = true,
    onClick: () -> Unit
) {
    item {
        IconButton(onClick = { onClick() }, enabled = enable) {
            if (tintRes != null || tintColor != null) {
                Icon(
                    painter = painterResource(drawableRes),
                    contentDescription = stringResource(descriptionRes),
                    tint = if (tintRes != null) colorResource(tintRes) else tintColor!!
                )
            } else {
                Icon(
                    painter = painterResource(drawableRes),
                    contentDescription = stringResource(descriptionRes),
                )
            }
        }
    }
}