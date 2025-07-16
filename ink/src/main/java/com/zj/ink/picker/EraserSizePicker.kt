package com.zj.ink.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.ink.geometry.ImmutableBox
import androidx.ink.geometry.Vec
import com.zj.ink.data.EditNoteViewModel
import com.zj.ink.edit.eraseWholeStrokes
import com.zj.data.R
import com.zj.data.common.DialogX

@Composable
fun EraserSizePicker(
    viewModel: EditNoteViewModel = hiltViewModel(),
) {
    if (!viewModel.showEraserSizePicker.value) return

    Dialog(onDismissRequest = { viewModel.showEraserSizePicker.value = false }) {
        Card(
            modifier = Modifier.size(300.dp, 160.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.dialog_background)
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.screen_horizontal_margin)),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Switch 组件
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.eraser_size_title))

                        Spacer(modifier = Modifier.width(10.dp))

                        Switch(
                            checked = viewModel.isEraserMode.value,
                            onCheckedChange = { isChecked ->
                                viewModel.isEraserMode.value = isChecked
                            },
                            colors = SwitchDefaults.colors(
                                uncheckedTrackColor = Color.Gray,
                                checkedTrackColor = colorResource(R.color.primary)
                            ),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // 清空按钮
                    IconButton(
                        onClick = { viewModel.showClearDraw.value = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_delete),
                            contentDescription = stringResource(R.string.clear_canvas),
                        )
                    }
                }

                Text(
                    stringResource(
                        R.string.eraser_size_content,
                        viewModel.eraserRadius.floatValue.toInt()
                    )
                )

                Slider(
                    value = viewModel.eraserRadius.floatValue,
                    onValueChange = {
                        viewModel.eraserRadius.floatValue = it
                    },
                    valueRange = 10f..100f, // 橡皮擦半径范围
                    steps = 9, // 10-100共10档
                    colors = SliderDefaults.colors(
                        activeTrackColor = colorResource(R.color.primary), // 主题色
                        inactiveTrackColor = Color.Gray
                    )
                )
            }
        }
    }

    DialogX(
        alertDialog = viewModel.showClearDraw,
        title = stringResource(R.string.clear_canvas_title),
        content = stringResource(R.string.clear_canvas_content),
    ) {
        val eraserBox = ImmutableBox.fromCenterAndDimensions(
            Vec.ORIGIN,
            Float.MAX_VALUE,
            Float.MAX_VALUE,
        )
        eraseWholeStrokes(
            eraserBox = eraserBox,
            finishedStrokesState = viewModel.finishedStrokes,
            undoStack = viewModel.drawUndoStack, // 传递 undoStack
            redoStack = viewModel.drawRedoStack // 传递 redoStack
        )
    }

}
