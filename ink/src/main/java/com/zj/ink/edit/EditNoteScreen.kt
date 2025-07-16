@file:OptIn(ExperimentalMaterial3Api::class)

package com.zj.ink.edit

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zj.data.model.Note
import com.zj.data.model.isValid
import com.zj.ink.data.EditNoteViewModel
import com.zj.data.R
import com.zj.data.common.DialogX
import com.zj.data.common.InputTextField
import com.zj.data.common.isPad
import com.zj.data.md.MarkdownText

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun EditNoteScreen(
    viewModel: EditNoteViewModel = hiltViewModel<EditNoteViewModel>(),
    back: () -> Unit = {},
) {
    val note by viewModel.note.collectAsState()
    val undoEnabled by viewModel.undoEnabled.collectAsState()
    val redoEnabled by viewModel.redoEnabled.collectAsState()
    val isDirty by viewModel.isDirty.collectAsState()
    val exitDialogShown = remember { mutableStateOf(false) }
    // 在EditNoteScreen开头添加键盘控制器声明
    val keyboardController = LocalSoftwareKeyboardController.current
    // 创建下拉菜单包裹低优先级按钮
    var expanded by remember { mutableStateOf(false) }

    // 返回键监听
    BackHandler(enabled = true) {
        if (isDirty) {
            exitDialogShown.value = true
        } else {
            back()
        }
    }
    Scaffold(
        topBar = {
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
                                viewModel.exportMarkdownToDocx()
                            }, enabled = note.title.isNotBlank() && note.content.isNotBlank()) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_share),
                                    contentDescription = stringResource(R.string.share)
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_more_vert),
                                contentDescription = stringResource(R.string.more)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
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
                                    expanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.draw)) },
                                onClick = {
                                    viewModel.showDialog.value = true
                                    expanded = false
                                },
                            )
                            if (isDirty) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.save)) },
                                    onClick = {
                                        viewModel.saveNote()
                                        keyboardController?.hide()
                                        expanded = false
                                    },
                                    enabled = note.title.isNotBlank() && note.content.isNotBlank()
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.share)) },
                                    onClick = {
                                        viewModel.exportMarkdownToDocx()
                                        expanded = false
                                    },
                                    enabled = note.title.isNotBlank() && note.content.isNotBlank()
                                )
                            }
                        }
                    }

                })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_margin))
                .background(MaterialTheme.colorScheme.background)
        ) {
            InputTextField(
                value = note.title,
                onValueChange = { viewModel.updateNoteTitle(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .animateContentSize(),
                placeholder = stringResource(R.string.title_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.image_screen_horizontal_margin)))
            val isPad = isPad()
            if (isPad) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.image_screen_horizontal_margin))
                ) {
                    NoteEditView(Modifier.weight(1f), viewModel)

                    NotePreview(Modifier.weight(1f), viewModel, note, true)
                }
            } else {
                NoteEditView(Modifier.weight(1f), viewModel)

                NotePreview(Modifier.weight(1f), viewModel, note, false)
            }
            NoteDrawDialog(viewModel) {
                viewModel.updateNoteContentImage(it)
            }
        }
    }
    DialogX(
        alertDialog = exitDialogShown,
        title = stringResource(R.string.save_dialog_title),
        content = stringResource(R.string.save_dialog_content),
    ) {
        back()
    }
}

@Composable
private fun NotePreview(
    modifier: Modifier = Modifier,
    viewModel: EditNoteViewModel,
    note: Note,
    isPad: Boolean
) {
    if (viewModel.showPreview.value) {
        if (isPad) {
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 3.dp)
                    .animateContentSize(),
                color = colorResource(R.color.divider)
            )
        } else {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .animateContentSize(),
                color = colorResource(R.color.divider)
            )
        }
        MarkdownText(
            markdown = note.content,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(R.dimen.image_screen_horizontal_margin))
                .animateContentSize()
        )
    }
}

@Composable
private fun NoteEditView(modifier: Modifier = Modifier, viewModel: EditNoteViewModel) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        MarkdownToolbar(
            rows = viewModel.rows,
            cols = viewModel.cols,
            showTablePicker = viewModel.showTablePicker,
            onInsert = { markdownTemplate ->
                viewModel.insertTemplate(markdownTemplate)
            },
        )

        BasicTextField(
            value = viewModel.noteContent.value,
            onValueChange = { viewModel.updateNoteContent(it) },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(R.dimen.image_screen_horizontal_margin))
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                        shortcutMap[keyEvent.key]?.let { template ->
                            viewModel.insertTemplate(template)
                            true
                        } ?: run {
                            false
                        }
                    } else {
                        false
                    }
                }
                .animateContentSize(),
            textStyle = TextStyle(
                fontSize = dimensionResource(R.dimen.subtitle_text).value.sp,
                textAlign = TextAlign.Start,
                color = colorResource(R.color.text_color)
            ),
            singleLine = false,
            maxLines = Int.MAX_VALUE,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (viewModel.noteContent.value.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.content_placeholder),
                            style = LocalTextStyle.current.copy(color = Color.Gray),
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                    innerTextField()
                }
            })

    }
}