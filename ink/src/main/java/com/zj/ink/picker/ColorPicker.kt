package com.zj.ink.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.data.R

@Composable
fun ColorPicker(
    selectedColor: MutableIntState,
    expanded: MutableState<Boolean>,
    onColorChange: (Int) -> Unit,
) {
    if (!expanded.value) return
    val currentArgb = selectedColor.intValue
    val composeColor = Color(currentArgb)

    Dialog(onDismissRequest = { expanded.value = false }) {
        ElevatedCard(
            modifier = Modifier
                .width(380.dp)
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部标题区域
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 颜色预览
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(composeColor)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.pen_color_select),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "#${
                                        String.format(
                                            "%06X",
                                            composeColor.toArgb() and 0xFFFFFF
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // 关闭按钮
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                        ) {
                            TextButton(
                                onClick = { expanded.value = false },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_close),
                                    contentDescription = stringResource(R.string.close),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // 颜色选择区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 预设颜色
                    ColorPresetsSection(
                        currentColor = composeColor,
                        onColorSelected = onColorChange
                    )

                    // RGB调节
                    RGBControlSection(
                        currentColor = composeColor,
                        onColorChange = onColorChange
                    )
                }

                // 底部操作按钮
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onColorChange(Color.Black.toArgb())
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_undo),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.reset))
                        }

                        Button(
                            onClick = { expanded.value = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_check),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.done))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPresetsSection(
    currentColor: Color,
    onColorSelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "预设颜色",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val presetColors = listOf(
            Color.Black, Color.Red, Color.Green, Color.Blue,
            Color.Yellow, Color.Magenta, Color.Cyan, Color.Gray,
            Color(0xFF8B4513), Color(0xFFFF4500), Color(0xFF32CD32), Color(0xFF4169E1),
            Color(0xFFFF1493), Color(0xFF00CED1), Color(0xFFFFD700), Color(0xFF9370DB)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(presetColors) { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color.toArgb() == currentColor.toArgb()) 3.dp else 1.dp,
                            color = if (color.toArgb() == currentColor.toArgb())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color.toArgb()) }
                )
            }
        }
    }
}

@Composable
private fun RGBControlSection(
    currentColor: Color,
    onColorChange: (Int) -> Unit
) {
    Column {
        Text(
            text = "RGB 调节",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val red = (currentColor.red * 255).toInt()
        val green = (currentColor.green * 255).toInt()
        val blue = (currentColor.blue * 255).toInt()

        // 红色滑块
        EnhancedColorSlider(
            label = "红色 (R)",
            value = red,
            color = Color.Red,
            onValueChange = { newRed ->
                val newColor = Color(
                    red = newRed / 255f,
                    green = green / 255f,
                    blue = blue / 255f,
                    alpha = 1f
                )
                onColorChange(newColor.toArgb())
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 绿色滑块
        EnhancedColorSlider(
            label = "绿色 (G)",
            value = green,
            color = Color.Green,
            onValueChange = { newGreen ->
                val newColor = Color(
                    red = red / 255f,
                    green = newGreen / 255f,
                    blue = blue / 255f,
                    alpha = 1f
                )
                onColorChange(newColor.toArgb())
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 蓝色滑块
        EnhancedColorSlider(
            label = "蓝色 (B)",
            value = blue,
            color = Color.Blue,
            onValueChange = { newBlue ->
                val newColor = Color(
                    red = red / 255f,
                    green = green / 255f,
                    blue = newBlue / 255f,
                    alpha = 1f
                )
                onColorChange(newColor.toArgb())
            }
        )
    }
}

@Composable
private fun EnhancedColorSlider(
    label: String,
    value: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}