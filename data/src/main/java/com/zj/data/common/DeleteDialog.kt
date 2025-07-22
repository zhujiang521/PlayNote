package com.zj.data.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zj.data.R

@Composable
fun DeleteDialog(
    alertDialog: MutableState<Boolean>,
    title: String,
    confirm: () -> Unit = {}
) {
    DialogX(
        alertDialog = alertDialog,
        title = stringResource(R.string.delete),
        content = stringResource(R.string.delete_confirm, title),
        onConfirmListener = confirm
    )
}

@Preview
@Composable
private fun DeleteDialogPreview() {
    val showDialog = remember { mutableStateOf(true) }
    DeleteDialog(showDialog, "测试")
}