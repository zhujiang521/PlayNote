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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.data.R
import com.zj.ink.layer.Layer
import com.zj.ink.layer.LayerManager
import com.zj.ink.layer.LayerType
import com.zj.ink.layer.BlendMode
import kotlin.math.roundToInt

/**
 * 图层控制面板
 * 提供图层管理的完整界面
 */
@Composable
fun LayerPanel(
    layerManager: LayerManager,
    visible: MutableState<Boolean>,
    onLayerSelected: (String) -> Unit = {}
) {
    if (!visible.value) return

    val layers = remember { layerManager.layers }
    val activeLayerId by layerManager.activeLayerId.collectAsState()
    val selectedLayerIds by layerManager.selectedLayerIds.collectAsState()

    Dialog(onDismissRequest = { visible.value = false }) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .width(350.dp)
                .height(500.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.dialog_background)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题栏
                LayerPanelHeader(
                    onAddLayer = { layerManager.createDrawingLayer() },
                    onClose = { visible.value = false }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 图层列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(layers.reversed()) { layer -> // 反转显示，顶层在上
                        LayerItem(
                            layer = layer,
                            isActive = layer.id == activeLayerId,
                            isSelected = layer.id in selectedLayerIds,
                            onLayerClick = {
                                layerManager.setActiveLayer(layer.id)
                                onLayerSelected(layer.id)
                            },
                            onVisibilityToggle = {
                                layerManager.updateLayer(layer.id) {
                                    it.withVisibility(!it.isVisible)
                                }
                            },
                            onLockToggle = {
                                layerManager.updateLayer(layer.id) {
                                    it.withLockState(!it.isLocked)
                                }
                            },
                            onOpacityChange = { opacity ->
                                layerManager.updateLayer(layer.id) {
                                    it.withOpacity(opacity)
                                }
                            },
                            onDelete = {
                                layerManager.deleteLayer(layer.id)
                            },
                            onDuplicate = {
                                layerManager.duplicateLayer(layer.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 底部操作栏
                LayerPanelFooter(
                    layerManager = layerManager,
                    activeLayerId = activeLayerId
                )
            }
        }
    }
}

@Composable
private fun LayerPanelHeader(
    onAddLayer: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "图层",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row {
            IconButton(onClick = onAddLayer) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add),
                    contentDescription = "添加图层"
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.baseline_close),
                    contentDescription = "关闭"
                )
            }
        }
    }
}

@Composable
private fun LayerItem(
    layer: Layer,
    isActive: Boolean,
    isSelected: Boolean,
    onLayerClick: () -> Unit,
    onVisibilityToggle: () -> Unit,
    onLockToggle: () -> Unit,
    onOpacityChange: (Float) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var showOpacitySlider by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLayerClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isActive -> MaterialTheme.colorScheme.primaryContainer
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图层信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LayerTypeIcon(layer.getLayerType())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = layer.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${(layer.opacity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 控制按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 可见性切换
                    IconButton(
                        onClick = onVisibilityToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (layer.isVisible) R.drawable.baseline_visibility
                                else R.drawable.baseline_visibility_off
                            ),
                            contentDescription = if (layer.isVisible) "隐藏" else "显示",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 锁定切换
                    IconButton(
                        onClick = onLockToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (layer.isLocked) R.drawable.baseline_check // 用check表示锁定
                                else R.drawable.baseline_draw // 用draw表示可编辑
                            ),
                            contentDescription = if (layer.isLocked) "解锁" else "锁定",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 更多选项
                    LayerOptionsMenu(
                        layer = layer,
                        onOpacityClick = { showOpacitySlider = !showOpacitySlider },
                        onDelete = onDelete,
                        onDuplicate = onDuplicate
                    )
                }
            }

            // 透明度滑块
            if (showOpacitySlider && layer.supportsTransparency()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "透明度",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(60.dp)
                    )
                    Slider(
                        value = layer.opacity,
                        onValueChange = onOpacityChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(layer.opacity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerTypeIcon(layerType: LayerType) {
    val iconRes = when (layerType) {
        LayerType.DRAWING -> R.drawable.baseline_draw
        LayerType.TEXT -> R.drawable.baseline_format_size
        LayerType.IMAGE -> R.drawable.baseline_image
        LayerType.BACKGROUND -> R.drawable.baseline_color_lens
    }

    val iconColor = when (layerType) {
        LayerType.DRAWING -> Color(0xFF2196F3)
        LayerType.TEXT -> Color(0xFF4CAF50)
        LayerType.IMAGE -> Color(0xFFFF9800)
        LayerType.BACKGROUND -> Color(0xFF9C27B0)
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(iconColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = layerType.displayName,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun LayerOptionsMenu(
    layer: Layer,
    onOpacityClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { showMenu = !showMenu },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_more_vert),
                contentDescription = "更多选项",
                modifier = Modifier.size(16.dp)
            )
        }

        if (showMenu) {
            Card(
                modifier = Modifier.padding(top = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (layer.supportsTransparency()) {
                        TextButton(
                            onClick = {
                                onOpacityClick()
                                showMenu = false
                            }
                        ) {
                            Text("调整透明度")
                        }
                    }

                    TextButton(
                        onClick = {
                            onDuplicate()
                            showMenu = false
                        }
                    ) {
                        Text("复制图层")
                    }

                    if (layer.getLayerType() != LayerType.BACKGROUND) {
                        TextButton(
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        ) {
                            Text("删除图层", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerPanelFooter(
    layerManager: LayerManager,
    activeLayerId: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 向上移动
        IconButton(
            onClick = {
                activeLayerId?.let { layerManager.moveLayerUp(it) }
            },
            enabled = activeLayerId != null
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_arrow_back), // 旋转90度表示向上
                contentDescription = "上移图层"
            )
        }

        // 向下移动
        IconButton(
            onClick = {
                activeLayerId?.let { layerManager.moveLayerDown(it) }
            },
            enabled = activeLayerId != null
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_arrow_back), // 旋转-90度表示向下
                contentDescription = "下移图层"
            )
        }

        // 添加文本图层
        IconButton(
            onClick = { layerManager.createTextLayer() }
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_format_size),
                contentDescription = "添加文本图层"
            )
        }

        // 添加绘图图层
        IconButton(
            onClick = { layerManager.createDrawingLayer() }
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_draw),
                contentDescription = "添加绘图图层"
            )
        }
    }
}