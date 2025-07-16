package com.zj.data.lce

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zj.data.R

@Composable
fun InitContent(
    modifier: Modifier = Modifier,
    @DrawableRes resId: Int = R.drawable.ic_ai_content,
    @StringRes titleResId: Int = R.string.ai__content_title,
    @StringRes stringResId: Int = R.string.ai_tip,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(120.dp),
            painter = painterResource(resId),
            contentDescription = stringResource(stringResId)
        )
        Text(
            text = stringResource(titleResId), modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 15.dp),
            textAlign = TextAlign.Center,
            fontSize = dimensionResource(R.dimen.title_text).value.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(stringResId),
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 20.dp),
            textAlign = TextAlign.Center,
            fontSize = dimensionResource(R.dimen.subtitle_text).value.sp,
        )
    }
}

@Preview
@Composable
private fun InitContentPreview() {
    InitContent()
}