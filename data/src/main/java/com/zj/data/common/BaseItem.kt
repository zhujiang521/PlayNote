package com.zj.data.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zj.data.R
import com.zj.data.md.MarkdownText
import com.zj.data.utils.DateUtils

// 定义一个常量表示默认的无效图标资源ID
private const val DEFAULT_VALID_ICON = -1

/**
 * 基础列表项组件，支持滑动删除、点击和多选等功能
 *
 * @param showCheckbox 是否显示复选框
 * @param isSelected 列表项是否被选中
 * @param onClick 列表项点击事件处理
 * @param onDelete 列表项删除事件处理
 * @param onSelect 列表项选中状态变化事件处理
 * @param control 滑动组件的控制对象
 * @param cardHeight 卡片的高度
 * @param title 列表项标题的生成函数
 * @param subtitle 列表项副标题的生成函数
 * @param icon 列表项图标的生成函数，默认为无效图标
 */
@Composable
fun BaseItem(
    showCheckbox: Boolean = true,
    isSelected: Boolean = true,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onSelect: (Boolean) -> Unit = {},
    control: SwipeBoxControl = rememberSwipeBoxControl(),
    cardHeight: Dp = 100.dp,
    title: String,
    subtitle: String,
    time: String = "",
    icon: Int = DEFAULT_VALID_ICON
) {
    val showDialog = remember { mutableStateOf(false) }
    SwipeBox(
        control = control,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        actionWidth = 100.dp,
        endAction = listOf {
            Box(
                modifier = Modifier
                    .height(cardHeight)
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp,
                        bottom = 8.dp,
                        end = dimensionResource(R.dimen.screen_horizontal_margin)
                    )
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
                .height(cardHeight)
                .padding(
                    horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                    vertical = dimensionResource(R.dimen.image_screen_horizontal_margin)
                )
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        onSelect(true)
                    }, onTap = {
                        if (showCheckbox) {
                            onSelect(!isSelected)
                        } else {
                            onClick()
                        }
                    })
                },
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    colorResource(R.color.item_select_background)
                else colorResource(R.color.item_background)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.item_margin)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCheckbox) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect(it) },
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.screen_horizontal_margin))
                    )
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = dimensionResource(R.dimen.title_text).value.sp,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (time != "") {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = subtitle,
                                fontSize = dimensionResource(R.dimen.subtitle_text).value.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,  // 新增
                                softWrap = false,
                                textAlign = TextAlign.Start
                            )
                            MarkdownText(
                                markdown = time,
                                fontSize = dimensionResource(R.dimen.subtitle_text).value.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = subtitle,
                                fontSize = dimensionResource(R.dimen.subtitle_text).value.sp,
                                textAlign = TextAlign.Center
                            )
                            if (icon != DEFAULT_VALID_ICON) {
                                Image(
                                    painterResource(icon),
                                    contentDescription = stringResource(R.string.model_name)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    DeleteDialog(showDialog, title, onDelete)
}

@Composable
fun DeleteDialog(
    alertDialog: MutableState<Boolean>,
    title: String,
    confirm: () -> Unit = {}
) {
    DialogX(
        alertDialog = alertDialog,
        title = stringResource(R.string.delete),
        content = stringResource(R.string.delete_confirm, title),
        onConfirmListener = confirm
    )
}

@Preview
@Composable
private fun DeleteDialogPreview() {
    val showDialog = remember { mutableStateOf(true) }
    DeleteDialog(showDialog, "测试")
}

@Preview
@Composable
private fun BaseItemPreview() {
    BaseItem(
        title = "测试",
        subtitle = DateUtils.formatTimestamp(1737360597000),
    )
}
