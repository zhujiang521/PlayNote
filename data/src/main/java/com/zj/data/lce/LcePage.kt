package com.zj.data.lce

import androidx.compose.runtime.Composable
import com.zj.data.model.UiState

@Composable
fun <T> LcePage(
    uiState: UiState<T>,
    onErrorClick: () -> Unit = {},
    content: @Composable (data: T) -> Unit
) {
    when (uiState) {
        is UiState.Error -> ErrorContent(onErrorClick = onErrorClick)
        UiState.Initial, UiState.Loading -> LoadingContent()
        is UiState.Success<T> -> content(uiState.data)
    }
}