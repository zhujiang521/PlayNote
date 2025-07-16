package com.zj.ink.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.ink.R

@Composable
fun PenSizePicker(
    selectedSize: MutableState<Float>,
    expanded: MutableState<Boolean>,
) {
    if (!expanded.value) return

    Dialog(onDismissRequest = { expanded.value = false }) {
        Card(
            modifier = Modifier.size(300.dp, 150.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(com.zj.data.R.color.dialog_background)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.size_title))

                Slider(
                    value = selectedSize.value,
                    onValueChange = {
                        selectedSize.value = it
                    },
                    valueRange = 1f..50f,
                    steps = 49 // 1-50共50档
                )

                Text(stringResource(R.string.size_content, selectedSize.value.toInt()))
            }
        }
    }
}
