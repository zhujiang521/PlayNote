package com.zj.data.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.zj.data.R
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun SearchTextField(
    visible: Boolean,
    searchQuery: StateFlow<String>,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val query by searchQuery.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current // 新增：获取键盘控制器
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(
            animationSpec = AnimationConfig.tweenNormal()
        ) + fadeIn(
            animationSpec = AnimationConfig.tweenNormal()
        ),
        exit = shrinkHorizontally(
            animationSpec = AnimationConfig.tweenFast()
        ) + fadeOut(
            animationSpec = AnimationConfig.tweenFast()
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = dimensionResource(id = R.dimen.screen_horizontal_margin)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InputTextField(
                value = query,
                onValueChange = onValueChange,
                placeholder = stringResource(R.string.search_hint),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = dimensionResource(id = R.dimen.image_screen_horizontal_margin))
                    .focusRequester(focusRequester),
            )

            IconButton(
                onClick = onClear,
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_close),
                    contentDescription = stringResource(id = R.string.close)
                )
            }
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            scope.launch {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }
}

