@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zj.ink

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zj.data.R
import com.zj.data.common.DeleteDialog
import com.zj.data.common.HighlightedText
import com.zj.data.common.SwipeBox
import com.zj.data.common.SwipeBoxControl
import com.zj.data.common.rememberSwipeBoxControl
import com.zj.data.model.Note
import com.zj.data.utils.DateUtils
import com.zj.ink.md.RenderMarkdown


/**
 * 笔记列表项
 */
@Composable
fun NoteItem(
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    searchQuery: String = "",
    control: SwipeBoxControl = rememberSwipeBoxControl(),
    note: Note
) {
    val showDialog = remember { mutableStateOf(false) }
    SwipeBox(
        control = control,
        modifier = Modifier
            .padding(
                top = 8.dp,
                bottom = 8.dp,
                end = dimensionResource(R.dimen.screen_horizontal_margin)
            )
            .fillMaxWidth()
            .wrapContentHeight(),
        actionWidth = 100.dp,
        endAction = listOf {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(min = 100.dp, max = 300.dp) // 限制高度范围
                    .align(Alignment.Center)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.large)
                    .clickable {
                        showDialog.value = true
                        control.center()
                    }) {
                Text(
                    text = stringResource(R.string.delete),
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = dimensionResource(R.dimen.small_text).value.sp
                    )
                )
            }
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // 根据内容高度自动调整
                .heightIn(min = 100.dp, max = 300.dp), // 限制高度范围 ,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.item_background)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
                    .padding(dimensionResource(R.dimen.item_margin))
            ) {
                HighlightedText(
                    text = note.title,
                    highlight = searchQuery,
                    style = TextStyle(
                        fontSize = dimensionResource(R.dimen.title_text).value.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatTimestamp(note.timestamp),
                    fontSize = dimensionResource(R.dimen.subtitle_text).value.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(4.dp))
                RenderMarkdown(
                    markdown = note.content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp)
                        .clickable { onClick() }
                )
            }
        }
    }
    DeleteDialog(showDialog, note.title, onDelete)
}


@Preview
@Composable
private fun NoteItemPreview() {
    NoteItem(
        note = Note(title = "title", content = "content"),
    )
}
