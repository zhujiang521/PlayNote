package com.zj.data.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zj.data.R

@Composable
fun DialogX(
    alertDialog: MutableState<Boolean>,
    title: String,
    content: String,
    cancelString: String = stringResource(R.string.cancel),
    confirmString: String = stringResource(R.string.confirm),
    buttonHeight: Dp = 45.dp,
    onCancelListener: () -> Unit = {},
    onConfirmListener: () -> Unit = {}
) {
    DialogX(
        alertDialog = alertDialog,
        title = title,
        content = {
            Text(
                text = content,
                fontSize = dimensionResource(R.dimen.title_text).value.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier
                    .padding(
                        top = 25.dp, bottom = 30.dp, start = 20.dp, end = 20.dp
                    )
                    .align(Alignment.CenterHorizontally),
                maxLines = 3,
                color = colorResource(R.color.text_color)
            )
        },
        cancelString = cancelString,
        confirmString = confirmString,
        buttonHeight = buttonHeight,
        onCancelListener = onCancelListener,
        onConfirmListener = onConfirmListener
    )
}

@Composable
fun DialogX(
    alertDialog: MutableState<Boolean>,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    cancelString: String = stringResource(R.string.cancel),
    confirmString: String = stringResource(R.string.confirm),
    buttonHeight: Dp = 45.dp,
    onCancelListener: () -> Unit = {},
    onConfirmListener: () -> Unit = {}
) {
    if (!alertDialog.value) return
    Dialog(onDismissRequest = {
        alertDialog.value = false
    }) {
        AnimatedVisibility(
            visible = alertDialog.value,
            enter = scaleIn(
                initialScale = 0.9f,
                animationSpec = AnimationConfig.tweenNormal()
            ) + fadeIn(
                animationSpec = AnimationConfig.tweenNormal()
            ),
            exit = scaleOut(
                targetScale = 0.95f,
                animationSpec = AnimationConfig.tweenFast()
            ) + fadeOut(
                animationSpec = AnimationConfig.tweenFast()
            )
        ) {
            Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .width(400.dp)
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.dialog_background)
            )
        ) {
            Column(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.item_margin))
            ) {
                Text(
                    text = title,
                    fontSize = dimensionResource(R.dimen.title_text).value.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_color),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.item_margin)),
                    color = colorResource(R.color.divider)
                )
                content()
                HorizontalDivider(color = colorResource(R.color.divider))
                Row {
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        shape = MaterialTheme.shapes.extraSmall,
                        onClick = {
                            alertDialog.value = false
                            onCancelListener()
                        }) {
                        Text(
                            text = cancelString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(buttonHeight),
                        color = colorResource(R.color.divider)
                    )
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        shape = MaterialTheme.shapes.extraSmall,
                        onClick = {
                            alertDialog.value = false
                            onConfirmListener()
                        }) {
                        Text(
                            text = confirmString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            }
        }
    }
}


@Preview(showBackground = false, name = "对话框")
@Composable
fun ShowDialogPreview() {
    val alertDialog = remember { mutableStateOf(true) }
    DialogX(
        alertDialog = alertDialog,
        title = "标题",
        content = "内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容",
    )
}
