package com.zj.ink.edit

import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.zj.data.R
import com.zj.data.common.isPad
import com.zj.data.model.Note
import com.zj.data.model.isValid
import com.zj.ink.data.EditNoteViewModel

@ExperimentalMaterial3Api
@Composable
fun EditNoteTopBar(
    note: Note,
    isDirty: Boolean,
    exitDialogShown: MutableState<Boolean>,
    back: () -> Unit,
    viewModel: EditNoteViewModel,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    keyboardController: SoftwareKeyboardController?,
) {
    val expanded = remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                stringResource(id = if (note.isValid()) R.string.edit_note else R.string.add_note),
                fontSize = dimensionResource(R.dimen.top_bar_title).value.sp
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = {
                if (isDirty) {
                    exitDialogShown.value = true
                } else {
                    back()
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_back),
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
        actions = {
            IconButton(onClick = {
                viewModel.undo()
            }, enabled = undoEnabled) {
                Icon(
                    painter = painterResource(R.drawable.baseline_undo),
                    contentDescription = stringResource(R.string.undo)
                )
            }
            IconButton(onClick = {
                viewModel.redo()
            }, enabled = redoEnabled) {
                Icon(
                    painter = painterResource(R.drawable.baseline_redo),
                    contentDescription = stringResource(R.string.redo)
                )
            }
            if (isPad()) {
                IconButton(onClick = {
                    viewModel.showPreview.value = !viewModel.showPreview.value
                }) {
                    Icon(
                        painter = painterResource(
                            if (viewModel.showPreview.value) R.drawable.baseline_visibility
                            else R.drawable.baseline_visibility_off
                        ),
                        contentDescription = stringResource(R.string.preview),
                    )
                }
                IconButton(onClick = {
                    viewModel.showDialog.value = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_draw),
                        contentDescription = stringResource(R.string.draw)
                    )
                }
                if (isDirty) {
                    IconButton(onClick = {
                        viewModel.saveNote()
                        keyboardController?.hide()
                    }, enabled = note.title.isNotBlank() && note.content.isNotBlank()) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_save),
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                } else {
                    IconButton(onClick = {
                        viewModel.exportMarkdown()
                    }, enabled = note.title.isNotBlank() && note.content.isNotBlank()) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_share),
                            contentDescription = stringResource(R.string.share)
                        )
                    }
                }
            } else {
                IconButton(onClick = { expanded.value = true }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_more_vert),
                        contentDescription = stringResource(R.string.more)
                    )
                }
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.medium
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.preview),
                                color = if (viewModel.showPreview.value) colorResource(R.color.primary)
                                else colorResource(R.color.icon_color)
                            )
                        },
                        onClick = {
                            viewModel.showPreview.value = !viewModel.showPreview.value
                            expanded.value = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.draw)) },
                        onClick = {
                            viewModel.showDialog.value = true
                            expanded.value = false
                        },
                    )
                    if (isDirty) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.save)) },
                            onClick = {
                                viewModel.saveNote()
                                keyboardController?.hide()
                                expanded.value = false
                            },
                            enabled = note.title.isNotBlank() && note.content.isNotBlank()
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.share)) },
                            onClick = {
                                viewModel.exportMarkdown()
                                expanded.value = false
                            },
                            enabled = note.title.isNotBlank() && note.content.isNotBlank()
                        )
                    }
                }
            }

        })
}