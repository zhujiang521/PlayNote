package com.zj.ink.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zj.data.utils.saveImageToAppStorage
import com.zj.data.R
import com.zj.ink.picker.TablePicker

val shortcutMap = mapOf(
    Key.B to "**%s**",
    Key.I to "*%s*",
    Key.L to "- %s\n",
    Key.H to "# %s\n",
    Key.Q to "```\n%s\n```",
    Key.T to "|\t%s\t|\tHeader2\t|\n|---|---|\n|\tRow1\t|\tRow2\t|",
    Key.K to ">%s\n"
)

@Composable
internal fun MarkdownToolbar(
    modifier: Modifier = Modifier,
    rows: MutableState<String>,
    cols: MutableState<String>,
    showTablePicker: MutableState<Boolean>,
    onInsert: (String) -> Unit,
) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val savedUri = context.saveImageToAppStorage(it)
            onInsert("\n![%s]($savedUri)")
        }
    }
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            ToolbarButton(
                icon = R.drawable.baseline_format_bold,
                title = R.string.bold,
                template = "**%s**"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_format_italic,
                title = R.string.italic,
                template = "*%s*"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_format_list_bulleted,
                title = R.string.bullet_list,
                template = "- %s\n"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_format_size,
                title = R.string.header,
                template = "# %s\n"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_code,
                title = R.string.code,
                template = "```\n%s\n```"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_link,
                title = R.string.link,
                template = "[文本](%s)"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_quote,
                title = R.string.quote,
                template = ">%s\n"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_table_chart,
                title = R.string.table,
                template =
                    "|\t%s\t|\tHeader2\t|\n" +
                            "|---|---|\n" +
                            "|\tRow1\t|\tRow2\t|"
            ),
            ToolbarButton(
                icon = R.drawable.baseline_image,
                title = R.string.image,
                template = "\n![Image](%s)"
            ),
        ).forEach { button ->
            item {
                MarkdownButton(
                    icon = button.icon,
                    contentDescription = button.title,
                    onClick = {
                        when (button.title) {
                            R.string.image -> {
                                imagePicker.launch("image/*")
                            }
                            R.string.table -> {
                                showTablePicker.value = true
                            }
                            else -> {
                                onInsert(button.template)
                            }
                        }
                    }
                )
            }
        }
    }

    TablePicker(
        rows = rows,
        cols = cols,
        showDialog = showTablePicker,
        onConfirm = { tableTemplate ->
            onInsert(tableTemplate)
        }
    )
}

private data class ToolbarButton(
    val icon: Int,
    val title: Int,
    val template: String
)

@Composable
private fun MarkdownButton(
    icon: Int,
    contentDescription: Int,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = stringResource(contentDescription),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}