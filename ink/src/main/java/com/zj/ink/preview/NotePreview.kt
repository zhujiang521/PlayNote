@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.zj.ink.preview

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zj.data.R
import com.zj.data.lce.ErrorContent
import com.zj.data.lce.LoadingContent
import com.zj.ink.md.RenderMarkdown
import com.zj.ink.widget.ShareIconButton

@Composable
fun NotePreview(
    viewModel: NotePreviewViewModel = hiltViewModel<NotePreviewViewModel>(),
    showBackButton: Boolean = true,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEditClick: (Int) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    back: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val note by viewModel.note.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState) {
                            is NotePreviewUiState.Success -> note.title
                            is NotePreviewUiState.Loading -> stringResource(R.string.loading)
                            is NotePreviewUiState.Error -> stringResource(R.string.error)
                        },
                        fontSize = dimensionResource(R.dimen.top_bar_title).value.sp
                    )
                },
                navigationIcon = {
                    if (!showBackButton) return@TopAppBar
                    IconButton(onClick = { back() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    // 只在成功加载时显示操作按钮
                    if (uiState is NotePreviewUiState.Success) {
                        IconButton(
                            onClick = { onEditClick(note.id) },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_draw),
                                contentDescription = stringResource(R.string.edit_note)
                            )
                        }
                        ShareIconButton(viewModel, note)
                    }
                },
            )
        }) { paddingValues ->

        // 根据UI状态显示不同内容
        when (uiState) {
            is NotePreviewUiState.Loading -> {
                // 显示加载状态，避免空白闪屏
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is NotePreviewUiState.Success -> {
                // 显示笔记内容
                RenderMarkdown(
                    markdown = note.content,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_margin))
                        .animateContentSize(),
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    onImageClick = onImageClick,
                    onTaskToggle = { taskIndex, taskText, currentChecked ->
                        viewModel.toggleTaskAndSave(taskIndex, taskText, currentChecked)
                    }
                )
            }

            is NotePreviewUiState.Error -> {
                // 显示错误状态
                ErrorContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}