// ShareOptionsDialog.kt
package com.zj.ink.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.data.R
import com.zj.data.model.Note
import com.zj.ink.data.BaseShareViewModel
import com.zj.ink.widget.LoadingDialog

@Composable
fun ShareOptionsDialog(
    viewModel: BaseShareViewModel,
    note: Note,
    onDismissRequest: () -> Unit
) {
    val isLoading by viewModel.isExporting.collectAsState()
    val loadingMessage by viewModel.exportMessage.collectAsState()

    if (isLoading) {
        LoadingDialog(message = loadingMessage)
    }

    Dialog(onDismissRequest = onDismissRequest) {
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
                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToImage(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_image),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToPdf(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_pdf),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToHtml(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_html),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToFile(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_md),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
