@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.zj.ink.md

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.size.Size.Companion.ORIGINAL
import coil3.toBitmap
import com.zj.data.R
import com.zj.data.common.pixelsToDp
import com.zj.data.utils.ToastUtil
import com.zj.data.utils.shareImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ImagePreview(
    viewModel: ImageViewModel = hiltViewModel(),
    imageUrl: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    back: () -> Unit = {},
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState()
    )
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState, sheetContent = {
            BottomSheet(viewModel, imageUrl, bitmap, bottomSheetScaffoldState)
        }, sheetPeekHeight = 0.dp
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = Color.Black,
        ) {
            with(sharedTransitionScope) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .size(ORIGINAL)
                        .build(),
                    contentDescription = stringResource(R.string.image),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.size(3412.pixelsToDp(), 1920.pixelsToDp()),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp))
                        }
                    },
                    error = {
                        Image(
                            painterResource(R.drawable.ic_placeholder_big),
                            modifier = Modifier.size(3412.pixelsToDp(), 1920.pixelsToDp()),
                            contentDescription = stringResource(R.string.down_fail)
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "image-${imageUrl}"),
                            animatedVisibilityScope = animatedContentScope
                        )
                        .zoomable(
                            zoomState = rememberZoomState(),
                            onTap = {
                                if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                                    }
                                } else {
                                    back()
                                }
                            },
                            onLongPress = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                }
                            }),
                    onSuccess = { drawable ->
                        bitmap = drawable.result.image.toBitmap()
                    })
            }
        }
    }
}

@Composable
private fun BottomSheet(
    viewModel: ImageViewModel,
    imageUrl: String,
    bitmap: Bitmap?,
    bottomSheetScaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current

    Row(modifier = Modifier.padding(dimensionResource(R.dimen.screen_horizontal_margin))) {

        BottomSheetItem(
            bitmap, bottomSheetScaffoldState, R.string.download, R.drawable.baseline_download
        ) {
            bitmap?.let { viewModel.downloadImage(imageUrl, it) }
        }

        BottomSheetItem(
            bitmap, bottomSheetScaffoldState, R.string.share, R.drawable.baseline_share
        ) {
            launch {
                bitmap?.let { shareImage(context, imageUrl, it) }
            }
        }

        BottomSheetItem(
            bitmap, bottomSheetScaffoldState, R.string.set_wallpaper, R.drawable.baseline_image
        ) {
            bitmap?.let { viewModel.setAsWallpaper(imageUrl, it) }
        }

    }
}

@Composable
fun BottomSheetItem(
    bitmap: Bitmap?,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    @StringRes textRes: Int,
    @DrawableRes iconRes: Int,
    onClick: CoroutineScope.() -> Unit = {}
) {
    val imageToast = stringResource(R.string.image_toast)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            modifier = Modifier
                .background(
                    color = colorResource(R.color.item_background),
                    shape = MaterialTheme.shapes.large
                )
                .size(60.dp), onClick = {
                if (bitmap == null) {
                    ToastUtil.showToast(context, imageToast)
                    return@IconButton
                }
                coroutineScope.launch(Dispatchers.IO) {
                    bottomSheetScaffoldState.bottomSheetState.partialExpand()
                    onClick()
                }
            }) {
            Icon(
                painter = painterResource(iconRes), contentDescription = stringResource(textRes)
            )
        }
        Text(
            stringResource(textRes),
            modifier = Modifier.padding(vertical = 5.dp),
            fontSize = dimensionResource(R.dimen.subtitle_text).value.sp
        )
    }

    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.screen_horizontal_margin)))
}