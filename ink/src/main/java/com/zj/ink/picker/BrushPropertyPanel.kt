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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.data.R
import com.zj.ink.brush.BrushProperties
import com.zj.ink.brush.BrushType
import kotlin.math.roundToInt

/**
 * 高级画笔属性控制面板
 * 提供详细的画笔属性调节功能
 */
@Composable
fun BrushPropertyPanel(
    brushProperties: BrushProperties,
    visible: MutableState<Boolean>,
    onPropertiesChanged: (BrushProperties) -> Unit
) {
    if (!visible.value) return

    var currentProperties by remember { mutableStateOf(brushProperties) }

    Dialog(onDismissRequest = { visible.value = false }) {
        ElevatedCard(
            modifier = Modifier
                .width(420.dp)
                .height(600.dp)
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
                            // 画笔类型图标
                            BrushTypeIndicator(currentProperties.brushType)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentProperties.brushType.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.brush_properties),
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
                                onClick = { visible.value = false },
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

                // 属性控制区域
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 基础属性
                    item {
                        PropertySection(title = "基础属性") {
                            EnhancedPropertySlider(
                                label = stringResource(R.string.size),
                                value = currentProperties.size,
                                range = 0.5f..50f,
                                onValueChange = { value ->
                                    currentProperties = currentProperties.copy(size = value)
                                    onPropertiesChanged(currentProperties)
                                },
                                valueFormatter = { "${it.roundToInt()}px" },
                                icon = R.drawable.baseline_format_size
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            EnhancedPropertySlider(
                                label = stringResource(R.string.opacity),
                                value = currentProperties.opacity,
                                range = 0f..1f,
                                onValueChange = { value ->
                                    currentProperties = currentProperties.copy(opacity = value)
                                    onPropertiesChanged(currentProperties)
                                },
                                valueFormatter = { "${(it * 100).roundToInt()}%" },
                                icon = R.drawable.baseline_visibility
                            )
                        }
                    }

                    // 高级属性
                    if (currentProperties.brushType != BrushType.HIGHLIGHTER) {
                        item {
                            PropertySection(title = "高级属性") {
                                EnhancedPropertySlider(
                                    label = stringResource(R.string.flow),
                                    value = currentProperties.flow,
                                    range = 0f..1f,
                                    onValueChange = { value ->
                                        currentProperties = currentProperties.copy(flow = value)
                                        onPropertiesChanged(currentProperties)
                                    },
                                    valueFormatter = { "${(it * 100).roundToInt()}%" },
                                    icon = R.drawable.baseline_draw
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                EnhancedPropertySlider(
                                    label = stringResource(R.string.hardness),
                                    value = currentProperties.hardness,
                                    range = 0f..1f,
                                    onValueChange = { value ->
                                        currentProperties = currentProperties.copy(hardness = value)
                                        onPropertiesChanged(currentProperties)
                                    },
                                    valueFormatter = { "${(it * 100).roundToInt()}%" },
                                    icon = R.drawable.baseline_color_lens
                                )
                            }
                        }
                    }

                    // 艺术效果属性
                    if (currentProperties.brushType in listOf(
                        BrushType.BRUSH, BrushType.WATERCOLOR, BrushType.CHALK, BrushType.CRAYON
                    )) {
                        item {
                            PropertySection(title = "艺术效果") {
                                EnhancedPropertySlider(
                                    label = stringResource(R.string.scatter),
                                    value = currentProperties.scatter,
                                    range = 0f..1f,
                                    onValueChange = { value ->
                                        currentProperties = currentProperties.copy(scatter = value)
                                        onPropertiesChanged(currentProperties)
                                    },
                                    valueFormatter = { "${(it * 100).roundToInt()}%" },
                                    icon = R.drawable.baseline_settings
                                )
                            }
                        }
                    }

                    // 压感控制
                    if (currentProperties.brushType.supportsPressure) {
                        item {
                            PropertySection(title = "压感设置") {
                                EnhancedPropertySwitch(
                                    label = stringResource(R.string.enable_pressure),
                                    checked = currentProperties.pressureEnabled,
                                    onCheckedChange = { enabled ->
                                        currentProperties = currentProperties.copy(pressureEnabled = enabled)
                                        onPropertiesChanged(currentProperties)
                                    },
                                    icon = R.drawable.baseline_settings
                                )

                                if (currentProperties.pressureEnabled) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    EnhancedPropertySlider(
                                        label = stringResource(R.string.pressure_sensitivity),
                                        value = currentProperties.pressureSensitivity,
                                        range = 0f..2f,
                                        onValueChange = { value ->
                                            currentProperties = currentProperties.copy(pressureSensitivity = value)
                                            onPropertiesChanged(currentProperties)
                                        },
                                        valueFormatter = { "${(it * 100).roundToInt()}%" },
                                        icon = R.drawable.baseline_settings
                                    )
                                }
                            }
                        }
                    }

                    // 纹理控制
                    if (currentProperties.brushType.supportsTexture) {
                        item {
                            PropertySection(title = "纹理效果") {
                                EnhancedPropertySwitch(
                                    label = stringResource(R.string.enable_texture),
                                    checked = currentProperties.textureEnabled,
                                    onCheckedChange = { enabled ->
                                        currentProperties = currentProperties.copy(textureEnabled = enabled)
                                        onPropertiesChanged(currentProperties)
                                    },
                                    icon = R.drawable.baseline_image
                                )

                                if (currentProperties.textureEnabled) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    EnhancedPropertySlider(
                                        label = stringResource(R.string.texture_intensity),
                                        value = currentProperties.textureIntensity,
                                        range = 0f..1f,
                                        onValueChange = { value ->
                                            currentProperties = currentProperties.copy(textureIntensity = value)
                                            onPropertiesChanged(currentProperties)
                                        },
                                        valueFormatter = { "${(it * 100).roundToInt()}%" },
                                        icon = R.drawable.baseline_image
                                    )
                                }
                            }
                        }
                    }
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
                                currentProperties = BrushProperties.fromBrushType(currentProperties.brushType)
                                    .copy(color = currentProperties.color)
                                onPropertiesChanged(currentProperties)
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
                            onClick = { visible.value = false },
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
private fun PropertySection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun BrushTypeIndicator(brushType: BrushType) {
    val iconColor = when (brushType) {
        BrushType.PEN -> Color(0xFF2196F3)
        BrushType.PENCIL -> Color(0xFF757575)
        BrushType.BRUSH -> Color(0xFF8B4513)
        BrushType.HIGHLIGHTER -> Color(0xFFFFEB3B)
        BrushType.MARKER -> Color(0xFFE91E63)
        BrushType.WATERCOLOR -> Color(0xFF03DAC6)
        BrushType.CHALK -> Color(0xFFFFFFFF)
        BrushType.CRAYON -> Color(0xFF9C27B0)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(iconColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_draw),
            contentDescription = brushType.displayName,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun EnhancedPropertySlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueFormatter: (Float) -> String = { it.toString() },
    icon: Int
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
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
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
                    text = valueFormatter(value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                activeTickColor = MaterialTheme.colorScheme.onPrimary,
                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun EnhancedPropertySwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}