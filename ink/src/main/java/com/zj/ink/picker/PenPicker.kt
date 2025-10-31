package com.zj.ink.picker

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.ink.brush.BrushFamily
import com.zj.data.R
import com.zj.ink.brush.BrushProperties
import com.zj.ink.brush.BrushType

@Composable
fun PenPicker(
    selectedBrushFamily: MutableState<BrushFamily>,
    expanded: MutableState<Boolean>,
    onBrushSelected: (BrushFamily) -> Unit,
    // 新增参数支持画笔属性
    selectedBrushType: MutableState<BrushType> = mutableStateOf(BrushType.PEN),
    onBrushTypeSelected: (BrushType) -> Unit = {},
    onBrushPropertiesChanged: (BrushProperties) -> Unit = {},
    // 新增画笔预设管理器参数
    brushPresetManager: com.zj.ink.brush.BrushPresetManager? = null
) {
    if (!expanded.value) return

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = if (brushPresetManager != null) {
        listOf(
            stringResource(R.string.brush_type),
            stringResource(R.string.quick_presets),
            stringResource(R.string.my_presets)
        )
    } else {
        listOf(
            stringResource(R.string.brush_type),
            stringResource(R.string.quick_presets)
        )
    }
    Dialog(onDismissRequest = { expanded.value = false }) {
        ElevatedCard(
            modifier = Modifier
                .width(400.dp)
                .height(520.dp)
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
                            // 画笔图标
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_draw),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.pen_type_select),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = selectedBrushType.value.displayName,
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

                // Tab选项卡
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f)
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // Tab内容
                when (selectedTabIndex) {
                    0 -> BrushTypeTab(
                        selectedBrushType = selectedBrushType,
                        onBrushTypeSelected = { brushType ->
                            selectedBrushType.value = brushType
                            selectedBrushFamily.value = brushType.defaultFamily
                            onBrushTypeSelected(brushType)
                            onBrushSelected(brushType.defaultFamily)
                            // 触发画笔属性更新
                            val newProperties = BrushProperties.fromBrushType(brushType)
                            onBrushPropertiesChanged(newProperties)
                        },
                        onDismiss = { expanded.value = false }
                    )

                    1 -> QuickPresetsTab(
                        onPresetSelected = { brushType ->
                            selectedBrushType.value = brushType
                            selectedBrushFamily.value = brushType.defaultFamily
                            onBrushTypeSelected(brushType)
                            onBrushSelected(brushType.defaultFamily)
                            // 触发画笔属性更新
                            val newProperties = BrushProperties.fromBrushType(brushType)
                            onBrushPropertiesChanged(newProperties)
                            expanded.value = false
                        }
                    )

                    2 -> if (brushPresetManager != null) {
                        UserPresetsTab(
                            brushPresetManager = brushPresetManager,
                            onPresetSelected = { preset ->
                                preset.toBrushProperties()?.let { properties ->
                                    selectedBrushType.value = properties.brushType
                                    selectedBrushFamily.value = properties.brushType.defaultFamily
                                    onBrushTypeSelected(properties.brushType)
                                    onBrushSelected(properties.brushType.defaultFamily)
                                    onBrushPropertiesChanged(properties)
                                    expanded.value = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrushTypeTab(
    selectedBrushType: MutableState<BrushType>,
    onBrushTypeSelected: (BrushType) -> Unit,
    onDismiss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(BrushType.getAllTypes()) { brushType ->
            BrushTypeItem(
                brushType = brushType,
                isSelected = selectedBrushType.value == brushType,
                onClick = {
                    onBrushTypeSelected(brushType)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun QuickPresetsTab(
    onPresetSelected: (BrushType) -> Unit
) {
    val quickPresets = listOf(
        BrushType.PEN to stringResource(R.string.daily_writing),
        BrushType.HIGHLIGHTER to stringResource(R.string.key_marking),
        BrushType.PENCIL to stringResource(R.string.sketch_drawing),
        BrushType.BRUSH to stringResource(R.string.artistic_creation),
        BrushType.MARKER to stringResource(R.string.fill_coloring)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickPresets) { (brushType, description) ->
            QuickPresetItem(
                brushType = brushType,
                description = description,
                onClick = { onPresetSelected(brushType) }
            )
        }
    }
}

@Composable
private fun UserPresetsTab(
    brushPresetManager: com.zj.ink.brush.BrushPresetManager,
    onPresetSelected: (com.zj.ink.brush.BrushPreset) -> Unit
) {
    val systemPresets = remember { brushPresetManager.getSystemPresets() }
    val userPresets by brushPresetManager.getUserPresets().collectAsState(initial = emptyList())
    val recentPresets by brushPresetManager.getRecentPresets().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 最近使用
        if (recentPresets.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.recent_used),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(recentPresets.take(3)) { preset ->
                PresetItem(
                    preset = preset,
                    onClick = { onPresetSelected(preset) }
                )
            }
        }

        // 系统预设
        item {
            Text(
                text = stringResource(R.string.system_presets),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        items(systemPresets.take(5)) { preset ->
            PresetItem(
                preset = preset,
                onClick = { onPresetSelected(preset) }
            )
        }

        // 用户预设
        if (userPresets.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.my_presets),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(userPresets) { preset ->
                PresetItem(
                    preset = preset,
                    onClick = { onPresetSelected(preset) }
                )
            }
        }
    }
}

@Composable
private fun PresetItem(
    preset: com.zj.ink.brush.BrushPreset,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // 增加高度以容纳两行文本
            .clickable(onClick = onClick)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 预设颜色指示器
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(preset.colorArgb))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (preset.description.isNotBlank()) {
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        // 预设标识
        if (preset.isSystemPreset) {
            Icon(
                painter = painterResource(R.drawable.baseline_check),
                contentDescription = stringResource(R.string.system_presets),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BrushTypeItem(
    brushType: BrushType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 画笔类型图标
        BrushTypeIcon(brushType = brushType)

        Spacer(modifier = Modifier.width(12.dp))

        // 画笔信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = brushType.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = brushType.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 选中状态指示
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.baseline_check),
                contentDescription = stringResource(R.string.pen_select),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun QuickPresetItem(
    brushType: BrushType,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrushTypeIcon(brushType = brushType)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = brushType.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BrushTypeIcon(
    brushType: BrushType,
    modifier: Modifier = Modifier
) {
    val iconColor = when (brushType) {
        BrushType.PEN -> Color.Black
        BrushType.PENCIL -> Color.Gray
        BrushType.BRUSH -> Color(0xFF8B4513)
        BrushType.HIGHLIGHTER -> Color.Yellow
        BrushType.MARKER -> Color.Red
        BrushType.WATERCOLOR -> Color.Blue
        BrushType.CHALK -> Color.White
        BrushType.CRAYON -> Color(0xFF9C27B0)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(iconColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(
                when (brushType) {
                    BrushType.PEN -> R.drawable.baseline_draw
                    BrushType.PENCIL -> R.drawable.baseline_draw
                    BrushType.BRUSH -> R.drawable.baseline_draw
                    BrushType.HIGHLIGHTER -> R.drawable.baseline_draw
                    BrushType.MARKER -> R.drawable.baseline_draw
                    else -> R.drawable.baseline_draw
                }
            ),
            contentDescription = brushType.displayName,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
