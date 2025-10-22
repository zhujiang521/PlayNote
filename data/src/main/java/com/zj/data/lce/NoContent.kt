package com.zj.data.lce

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zj.data.R

@Composable
fun NoContent(
    modifier: Modifier = Modifier,
    @RawRes resId: Int = R.raw.no_data_anim,
    tip: String = stringResource(R.string.no_content)
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_no_data),
            modifier = Modifier.size(150.dp),
            contentDescription = "No Content"
        )
        Text(
            text = tip, modifier = Modifier
                .padding(10.dp)
                .width(300.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun NoContentPreview() {
    NoContent()
}