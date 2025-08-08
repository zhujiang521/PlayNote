package com.zj.ink.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.data.R
import com.zj.data.model.Note
import com.zj.ink.data.BaseShareViewModel

@Composable
fun ShareIconButton(viewModel: BaseShareViewModel, note: Note) {
    var expanded by remember { mutableStateOf(false) }
    // 监听 ViewModel 中的加载状态
    val isLoading by viewModel.isExporting.collectAsState()
    val loadingMessage by viewModel.exportMessage.collectAsState()
    IconButton(
        onClick = { expanded = true },
        enabled = note.title.isNotBlank() && note.content.isNotBlank()
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_share),
            contentDescription = stringResource(R.string.share)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(
                -dimensionResource(R.dimen.image_screen_horizontal_margin),
                0.dp
            ),
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.export_text)) },
                onClick = {
                    viewModel.exportMarkdownToText(note)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.export_image)) },
                onClick = {
                    viewModel.exportMarkdownToImage(note)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.export_pdf)) },
                onClick = {
                    viewModel.exportMarkdownToPdf(note)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.export_html)) },
                onClick = {
                    viewModel.exportMarkdownToHtml(note)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.export_md)) },
                onClick = {
                    viewModel.exportMarkdownToFile(note)
                    expanded = false
                }
            )
        }
    }
    // 显示加载对话框
    if (isLoading) {
        LoadingDialog(loadingMessage)
    }
}

@Composable
fun LoadingDialog(message: String) {
    Dialog(onDismissRequest = { }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}