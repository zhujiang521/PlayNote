package com.zj.data.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.zj.data.R
import com.zj.data.lce.ErrorContent
import com.zj.data.lce.NoContent

/**
 * 在LazyList中展示Paging数据加载状态的函数
 *
 * 此函数根据Paging数据的加载状态，在列表中展示不同的UI组件
 * 它处理三种情况：
 * 1. 刷新数据时发生错误
 * 2. 加载更多数据时发生错误
 * 3. 没有数据且没有在加载数据
 *
 * @param items LazyPagingItems对象，用于获取Paging数据和加载状态
 */
fun <T : Any> LazyStaggeredGridScope.lazyPagingStates(items: LazyPagingItems<T>) {
    val loadStates = items.loadState
    when {
        loadStates.refresh is LoadState.Error -> {
            item {
                ErrorContent(modifier = Modifier.fillMaxWidth()) {
                    items.retry()
                }
            }
        }

        loadStates.append is LoadState.Error -> {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { items.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }

        items.itemCount == 0 && loadStates.refresh is LoadState.NotLoading -> {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.screen_horizontal_margin)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    NoContent()
                }
            }
        }
    }
}