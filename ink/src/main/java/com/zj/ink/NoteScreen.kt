@file:OptIn(ExperimentalMaterial3Api::class)

package com.zj.ink

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.zj.data.R
import com.zj.data.common.SearchTextField
import com.zj.data.common.buttonPressAnimation
import com.zj.data.common.lazyPagingStates
import com.zj.data.lce.LoadingContent
import com.zj.data.lce.NoContent
import com.zj.data.model.INVALID_ID
import com.zj.data.model.Note
import com.zj.ink.data.NoteViewModel

@Composable
fun NoteScreen(
    viewModel: NoteViewModel = hiltViewModel(),
    editNote: (Note?) -> Unit = {},
    previewNote: (Note) -> Unit = {},
) {
    val notes = viewModel.notes.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()
    val state: LazyStaggeredGridState = rememberLazyStaggeredGridState()

    // 监听 selectedNoteId 变化并滚动到对应项目
    LaunchedEffect(selectedNoteId) {
        if (selectedNoteId != INVALID_ID) {
            var index = INVALID_ID
            for (i in 0 until notes.itemCount) {
                if (notes[i]?.id == selectedNoteId) {
                    index = i
                }
            }
            if (index > INVALID_ID) {
                state.animateScrollToItem(index)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.note),
                        fontSize = dimensionResource(R.dimen.top_bar_title).value.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    if (!viewModel.searchExpanded.value) {
                        IconButton(
                            onClick = {
                                viewModel.searchExpanded.value = !viewModel.searchExpanded.value
                            },
                            modifier = Modifier.buttonPressAnimation()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_search),
                                contentDescription = stringResource(R.string.search)
                            )
                        }
                    }
                    SearchTextField(
                        visible = viewModel.searchExpanded.value,
                        searchQuery = viewModel.searchQuery,
                        onValueChange = {
                            viewModel.setSearchQuery(it)
                        },
                        onClear = {
                            if (viewModel.searchQuery.value.isBlank()) {
                                viewModel.searchExpanded.value = false
                            } else {
                                viewModel.setSearchQuery("")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                },
            )
        }, floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editNote(null)
                    viewModel.searchExpanded.value = false
                },
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.screen_horizontal_margin)),
                shape = CircleShape,
                contentColor = Color.White,
                containerColor = colorResource(R.color.primary) // 使用主题中的 secondary 颜色
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add),
                    contentDescription = stringResource(R.string.add_note)
                )
            }
        }) { paddingValues ->
        // 显示加载状态
        if (isLoading) {
            LoadingContent()
        } else if (notes.itemCount == 0) {
            NoContent()
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = dimensionResource(R.dimen.screen_horizontal_margin)),
                state = state,
                columns = StaggeredGridCells.Adaptive(dimensionResource(R.dimen.note_card_width)),
            ) {
                items(
                    count = notes.itemCount,
                    key = notes.itemKey { it.id }, // 使用唯一的ID作为key
                    contentType = notes.itemContentType()
                ) { index ->
                    val item = notes[index] ?: return@items
                    // 为每个NoteItem创建独立的SwipeBoxControl
                    NoteItem(
                        note = item,
                        onClick = {
                            viewModel.setSelectedNoteId(item.id)
                            previewNote(item)
                        },
                        searchQuery = viewModel.searchQuery.value,
                        onDelete = { viewModel.deleteNote(item) },
                        isSelected = selectedNoteId == item.id
                    )
                }
                lazyPagingStates(notes)
            }
        }
    }
}


@Preview
@Composable
private fun NoteScreenPreview() {
    NoteScreen()
}