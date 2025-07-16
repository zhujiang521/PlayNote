package com.zj.ink.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.ink.R

@Composable
fun ColorPicker(
    selectedColor: MutableIntState,
    expanded: MutableState<Boolean>,
    onColorChange: (Int) -> Unit,
) {
    if (!expanded.value) return
    val currentArgb = selectedColor.intValue
    val composeColor = Color(currentArgb)
    val red = (composeColor.red * 255).toInt()
    val green = (composeColor.green * 255).toInt()
    val blue = (composeColor.blue * 255).toInt()

    val sliderColors = SliderDefaults.colors(
        activeTrackColor = composeColor,          // 活动轨道颜色（当前颜色）
        inactiveTrackColor = Color.LightGray,     // 非活动轨道颜色
        thumbColor = MaterialTheme.colorScheme.primary,  // 滑块颜色（主题主色）
        activeTickColor = Color.Transparent,
    )

    Dialog(onDismissRequest = {
        expanded.value = false
    }) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .width(350.dp)
                .height(300.dp)
                .padding(dimensionResource(R.dimen.screen_horizontal_margin)),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(com.zj.data.R.color.dialog_background)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.screen_horizontal_margin))
            ) {

                // 选择画笔颜色
                Text(
                    stringResource(R.string.pen_color_select),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_horizontal_margin)))

                // 颜色预览块
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(composeColor, shape = MaterialTheme.shapes.medium)
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.image_screen_horizontal_margin)))

                // 红色滑块
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("R: $red")
                    Slider(
                        modifier = Modifier.width(220.dp),
                        value = red.toFloat(),
                        onValueChange = { newRed ->
                            val newColor = Color(
                                red = newRed.toInt(),
                                green = green,
                                blue = blue,
                                alpha = 255
                            )
                            onColorChange(newColor.toArgb())
                        },
                        colors = sliderColors,
                        valueRange = 0f..255f,
                        steps = 255
                    )
                }

                // 绿色滑块
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("G: $green")
                    Slider(
                        modifier = Modifier.width(220.dp),
                        value = green.toFloat(),
                        onValueChange = { newGreen ->
                            val newColor = Color(
                                red = red,
                                green = newGreen.toInt(),
                                blue = blue,
                                alpha = 255
                            )
                            onColorChange(newColor.toArgb())
                        },
                        colors = sliderColors,
                        valueRange = 0f..255f,
                        steps = 255
                    )
                }

                // 蓝色滑块
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("B: $blue")
                    Slider(
                        modifier = Modifier.width(220.dp),
                        value = blue.toFloat(),
                        onValueChange = { newBlue ->
                            val newColor = Color(
                                red = red,
                                green = green,
                                blue = newBlue.toInt(),
                                alpha = 255
                            )
                            onColorChange(newColor.toArgb())
                        },
                        colors = sliderColors,
                        valueRange = 0f..255f,
                        steps = 255,
                    )
                }
            }
        }
    }

}
