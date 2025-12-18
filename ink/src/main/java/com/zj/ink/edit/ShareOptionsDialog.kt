// ShareOptionsDialog.kt
package com.zj.ink.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zj.data.R
import com.zj.data.common.AnimationConfig
import com.zj.data.common.buttonPressAnimation
import com.zj.data.model.Note
import com.zj.ink.data.BaseShareViewModel
import com.zj.ink.widget.LoadingDialog

/**
 * 分享选项对话框，提供多种导出格式选项
 *
 * @param viewModel 处理导出逻辑的 ViewModel
 * @param note 需要导出的笔记
 * @param onDismissRequest 关闭对话框的回调函数
 */
@Composable
fun ShareOptionsDialog(
    viewModel: BaseShareViewModel,
    note: Note,
    onDismissRequest: () -> Unit
) {
    val isLoading by viewModel.isExporting.collectAsState()
    val loadingMessage by viewModel.exportMessage.collectAsState()

    if (isLoading) {
        LoadingDialog(message = loadingMessage)
    }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismissRequest) {
        AnimatedVisibility(
            visible = isVisible,
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
            Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToText(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .buttonPressAnimation(),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_text),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToImage(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .buttonPressAnimation(),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_image),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToPdf(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .buttonPressAnimation(),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_pdf),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToHtml(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .buttonPressAnimation(),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_html),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.exportMarkdownToFile(note) {
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .buttonPressAnimation(),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.screen_horizontal_margin),
                        vertical = dimensionResource(R.dimen.image_screen_horizontal_margin),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_md),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            }
        }
    }
}