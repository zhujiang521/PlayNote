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
import com.zj.ink.md.RenderMarkdown

@Composable
fun NotePreview(
    viewModel: NotePreviewViewModel = hiltViewModel<NotePreviewViewModel>(),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onImageClick: (String) -> Unit = {}
) {
    val note by viewModel.note.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        note.title,
                        fontSize = dimensionResource(R.dimen.top_bar_title).value.sp
                    )
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.exportMarkdown()
                    }, enabled = note.title.isNotBlank() && note.content.isNotBlank()) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_share),
                            contentDescription = stringResource(R.string.share)
                        )
                    }
                },
            )
        }) { paddingValues ->

        RenderMarkdown(
            markdown = note.content,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_margin))
                .animateContentSize(),
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            onImageClick = onImageClick
        )
    }
}