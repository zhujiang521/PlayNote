package com.zj.data.common

import androidx.compose.runtime.mutableStateOf

/**
 * 全局SwipeBox状态管理器
 * 确保同时只有一个SwipeBox能够展开
 */
object SwipeBoxGlobalState {

    // 当前展开的SwipeBox ID
    private val _currentExpandedId = mutableStateOf<String?>(null)
    val currentExpandedId = _currentExpandedId

    /**
     * 设置指定SwipeBox为展开状态
     * @param itemId SwipeBox ID
     */
    fun setExpanded(itemId: String) {
        _currentExpandedId.value = itemId
    }

    /**
     * 清除展开状态
     */
    fun clearExpanded() {
        _currentExpandedId.value = null
    }

    /**
     * 检查指定item是否可以开始滑动
     * @param itemId SwipeBox ID
     * @return 是否可以滑动
     */
    fun canStartSwipe(itemId: String): Boolean {
        val currentExpanded = _currentExpandedId.value
        val canSwipe = currentExpanded == null || currentExpanded == itemId
        println("SwipeBoxGlobalState: canStartSwipe[$itemId] = $canSwipe (当前展开: $currentExpanded)")
        return canSwipe
    }
}