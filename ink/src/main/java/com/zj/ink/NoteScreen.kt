@file:OptIn(ExperimentalMaterial3Api::class)

package com.zj.ink

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import com.zj.data.model.INVALID_ID
import com.zj.ink.data.NoteViewModel
import com.zj.data.common.SearchTextField
import com.zj.data.common.lazyPagingStates
import com.zj.data.lce.NoContent

@Composable
fun NoteScreen(
    viewModel: NoteViewModel = hiltViewModel(),
    editNote: (Int) -> Unit = {},
) {
    val notes = viewModel.notes.collectAsLazyPagingItems()
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
                        IconButton(onClick = {
                            viewModel.searchExpanded.value = !viewModel.searchExpanded.value
                        }) {
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
                    editNote(INVALID_ID)
                    viewModel.searchExpanded.value = false
                },
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.screen_horizontal_margin)),
                shape = CircleShape,
                contentColor = Color.White,
                containerColor = colorResource(com.zj.data.R.color.primary) // 使用主题中的 secondary 颜色
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add),
                    contentDescription = stringResource(R.string.add_note)
                )
            }
        }) { paddingValues ->
        if (notes.itemCount == 0) {
            NoContent()
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_margin)),
                columns = StaggeredGridCells.Adaptive(dimensionResource(R.dimen.note_card_width)),
            ) {
                items(
                    count = notes.itemCount,
                    key = notes.itemKey(),
                    contentType = notes.itemContentType()
                ) { index ->
                    val item = notes[index] ?: return@items
                    NoteItem(
                        note = item,
                        onClick = {
                            editNote(item.id)
                        },
                        onDelete = { viewModel.deleteNote(item) },
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
