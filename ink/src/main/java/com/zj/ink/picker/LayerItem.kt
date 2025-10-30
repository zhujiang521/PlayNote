package com.zj.ink.picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zj.data.R
import com.zj.ink.layer.Layer
import com.zj.ink.layer.LayerType
import com.zj.ink.layer.BlendMode
import kotlin.math.roundToInt

/**
 * 增强版图层项组件
 * 支持拖拽重排序、编辑名称、缩略图预览等功能
 */
@Composable
fun EnhancedLayerItem(
    layer: Layer,
    isActive: Boolean,
    isSelected: Boolean,
    isDragging: Boolean = false,
    onLayerClick: () -> Unit,
    onLayerDoubleClick: () -> Unit = {},
    onVisibilityToggle: () -> Unit,
    onLockToggle: () -> Unit,
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(layer.name) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() }
                ) { _, _ -> }
            }
            .clickable {
                if (!isEditingName) onLayerClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDragging -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                isActive -> MaterialTheme.colorScheme.primaryContainer
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图层缩略图
            LayerThumbnail(
                layer = layer,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 图层信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 图层名称（可编辑）
                if (isEditingName) {
                    BasicTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    )
                } else {
                    Text(
                        text = layer.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            isEditingName = true
                            tempName = layer.name
                        }
                    )
                }

                // 图层详细信息
                LayerInfoRow(layer = layer)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 控制按钮
            LayerControlButtons(
                layer = layer,
                isEditingName = isEditingName,
                onVisibilityToggle = onVisibilityToggle,
                onLockToggle = onLockToggle,
                onEditConfirm = {
                    onNameChange(tempName)
                    isEditingName = false
                },
                onEditCancel = {
                    isEditingName = false
                    tempName = layer.name
                },
                onDelete = onDelete,
                onDuplicate = onDuplicate
            )
        }
    }
}

@Composable
private fun LayerThumbnail(
    layer: Layer,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp)
            )
    ) {
        when (layer.getLayerType()) {
            LayerType.DRAWING -> DrawingLayerThumbnail(layer)
            LayerType.TEXT -> TextLayerThumbnail(layer)
            LayerType.IMAGE -> ImageLayerThumbnail(layer)
            LayerType.BACKGROUND -> BackgroundLayerThumbnail(layer)
        }

        // 透明度遮罩
        if (layer.opacity < 1.0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Color.White.copy(alpha = 1.0f - layer.opacity),
                        RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

@Composable
private fun DrawingLayerThumbnail(layer: Layer) {
    Canvas(
        modifier = Modifier.fillMaxWidth().height(40.dp)
    ) {
        // 绘制简化的笔迹预览
        drawDrawingPreview(this, layer)
    }
}

@Composable
private fun TextLayerThumbnail(layer: Layer) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_format_size),
            contentDescription = "文本图层",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ImageLayerThumbnail(layer: Layer) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFF0F0F0)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_image),
            contentDescription = "图像图层",
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BackgroundLayerThumbnail(layer: Layer) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_color_lens),
            contentDescription = "背景图层",
            tint = Color(0xFF9C27B0),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun drawDrawingPreview(drawScope: DrawScope, layer: Layer) {
    with(drawScope) {
        // 绘制简化的笔迹预览
        val width = size.width
        val height = size.height

        // 绘制几条示例线条
        drawLine(
            color = Color.Black,
            start = Offset(width * 0.1f, height * 0.3f),
            end = Offset(width * 0.9f, height * 0.7f),
            strokeWidth = 2.dp.toPx()
        )

        drawLine(
            color = Color.Black,
            start = Offset(width * 0.2f, height * 0.6f),
            end = Offset(width * 0.8f, height * 0.4f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
private fun LayerInfoRow(layer: Layer) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 图层类型标签
        LayerTypeChip(layer.getLayerType())

        // 混合模式（如果不是普通模式）
        if (layer.getBlendMode() != BlendMode.NORMAL && layer.supportsBlendModes()) {
            BlendModeChip(layer.getBlendMode())
        }

        // 透明度信息
        if (layer.opacity < 1.0f) {
            Text(
                text = "${(layer.opacity * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LayerTypeChip(layerType: LayerType) {
    Text(
        text = layerType.displayName,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun BlendModeChip(blendMode: BlendMode) {
    Text(
        text = blendMode.displayName,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun LayerControlButtons(
    layer: Layer,
    isEditingName: Boolean,
    onVisibilityToggle: () -> Unit,
    onLockToggle: () -> Unit,
    onEditConfirm: () -> Unit,
    onEditCancel: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditingName) {
            // 编辑模式按钮
            IconButton(
                onClick = onEditConfirm,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_check),
                    contentDescription = "确认",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onEditCancel,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_close),
                    contentDescription = "取消",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        } else {
            // 普通模式按钮
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
                    modifier = Modifier.size(16.dp),
                    tint = if (layer.isVisible) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(
                onClick = onLockToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (layer.isLocked) R.drawable.baseline_check
                        else R.drawable.baseline_draw
                    ),
                    contentDescription = if (layer.isLocked) "解锁" else "锁定",
                    modifier = Modifier.size(16.dp),
                    tint = if (layer.isLocked) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 图层拖拽指示器
 */
@Composable
fun LayerDragIndicator(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(20.dp)
    ) {
        val dotSize = 2.dp.toPx()
        val spacing = 4.dp.toPx()
        val color = Color.Gray

        // 绘制拖拽点阵
        for (row in 0..2) {
            for (col in 0..1) {
                val x = col * spacing + dotSize
                val y = row * spacing + dotSize
                drawCircle(
                    color = color,
                    radius = dotSize / 2,
                    center = Offset(x, y)
                )
            }
        }
    }
}