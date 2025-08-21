package com.zj.ink.widget

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.king.ultraswiperefresh.NestedScrollMode
import com.king.ultraswiperefresh.UltraSwipeRefresh
import com.king.ultraswiperefresh.rememberUltraSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自定义下拉刷新组件，基于 UltraSwipeRefresh 实现
 *
 * @param modifier 修饰符，用于设置组件的布局属性
 * @param lazyListState 滚动状态，用于控制列表的滚动行为
 * @param refresh 刷新回调函数，当下拉刷新时执行
 * @param content 内容组件，需要支持下拉刷新功能的列表内容
 */
@Composable
fun SwipeRefresh(
    modifier: Modifier = Modifier,
    lazyListState: ScrollableState = rememberLazyStaggeredGridState(),
    refresh: () -> Unit = {},
    content: @Composable () -> Unit
) {

    val state = rememberUltraSwipeRefreshState()
    val coroutineScope = rememberCoroutineScope()

    UltraSwipeRefresh(
        state = state,
        onRefresh = {
            coroutineScope.launch {
                state.isRefreshing = true
                refresh()
                delay(1200)
                state.isRefreshing = false
            }
        },
        onLoadMore = {
            coroutineScope.launch {
                state.isLoading = true
                state.isLoading = false
            }
        },
        loadMoreEnabled = false,
        modifier = modifier,
        headerScrollMode = NestedScrollMode.Translate,
        footerScrollMode = NestedScrollMode.Translate,
        onCollapseScroll = {
            // 小于0时表示：由下拉刷新收起时触发的，大于0时表示：由上拉加载收起时触发的
            if (it > 0) {
                // 指示器收起时滚动列表位置，消除视觉回弹
                lazyListState.animateScrollBy(it)
            }
        },
    ) {
        content()
    }
}
