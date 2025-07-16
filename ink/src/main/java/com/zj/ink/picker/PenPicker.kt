package com.zj.ink.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes
import com.zj.ink.R

@Composable
fun PenPicker(
    selectedBrushFamily: MutableState<BrushFamily>,
    expanded: MutableState<Boolean>,
    onBrushSelected: (BrushFamily) -> Unit
) {
    if (!expanded.value) return

    Dialog(onDismissRequest = { expanded.value = false }) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .width(300.dp)
                .height(320.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(com.zj.data.R.color.dialog_background)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.image_screen_horizontal_margin))
            ) {
                // 画笔类型标题
                Text(
                    stringResource(R.string.pen_type_select),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // 列出所有可用画笔类型
                val brushOptions = listOf(
                    StockBrushes.markerLatest,
                    StockBrushes.pressurePenLatest,
                    StockBrushes.highlighterLatest,
                    StockBrushes.dashedLineLatest
                )

                brushOptions.forEach { brush ->
                    BrushOptionItem(
                        brushFamily = brush,
                        isSelected = selectedBrushFamily.value == brush,
                        onClick = {
                            selectedBrushFamily.value = brush
                            onBrushSelected(brush)
                            expanded.value = false // 选择后关闭弹窗
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BrushOptionItem(
    brushFamily: BrushFamily,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = when (brushFamily) {
                StockBrushes.markerLatest -> stringResource(R.string.pen_marker)
                StockBrushes.pressurePenLatest -> stringResource(R.string.pen_pressure)
                StockBrushes.highlighterLatest -> stringResource(R.string.pen_highlighter)
                StockBrushes.dashedLineLatest -> stringResource(R.string.pen_dashed_line)
                else -> stringResource(R.string.pen_unknown)
            },
            modifier = Modifier.padding(start = dimensionResource(R.dimen.screen_horizontal_margin)),
            style = MaterialTheme.typography.bodyMedium
        )

        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.baseline_check),
                contentDescription = stringResource(R.string.pen_select),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(end = dimensionResource(R.dimen.screen_horizontal_margin))
            )
        }
    }
}
