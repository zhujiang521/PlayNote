package com.zj.ink.picker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zj.data.R
import com.zj.data.common.DialogX
import com.zj.data.common.InputTextField

@Composable
fun TablePicker(
    showDialog: MutableState<Boolean>,
    rows: MutableState<String>,
    cols: MutableState<String>,
    onConfirm: (String) -> Unit
) {
    if (!showDialog.value) return



    DialogX(
        alertDialog = showDialog,
        title = stringResource(R.string.table_picker_title),
        content = {

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_horizontal_margin)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_margin)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.table_picker_rows_name),
                    modifier = Modifier.width(60.dp)
                )

                InputTextField(
                    value = rows.value,
                    onValueChange = {
                        rows.value = it.filter { char -> char.isDigit() }
                    },
                    placeholder = stringResource(R.string.table_picker_rows),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_horizontal_margin)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_margin)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.table_picker_cols_name),
                    modifier = Modifier.width(60.dp)
                )
                InputTextField(
                    value = cols.value,
                    onValueChange = {
                        cols.value = it.filter { char -> char.isDigit() }
                    },
                    placeholder = stringResource(R.string.table_picker_cols),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_horizontal_margin)))

        },
    ) {
        val row = rows.value.toIntOrNull() ?: 2
        val column = cols.value.toIntOrNull() ?: 2
        if (row >= 1 && column >= 1) {
            val table = generateTableTemplate(row, column)
            onConfirm(table)
            showDialog.value = false
        }
    }
}

private fun generateTableTemplate(rows: Int, cols: Int): String {
    val headers = (1..cols).joinToString("|") { "Header $it" }
    val separator = "|${List(cols) { "-------" }.joinToString("|")}|"
    val content = (1..rows).joinToString("\n") {
        "| ${List(cols) { "Content" }.joinToString(" | ")} |"
    }
    return "|$headers|\n$separator\n$content"
}