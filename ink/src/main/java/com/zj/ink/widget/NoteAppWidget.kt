package com.zj.ink.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.makeMainActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.zj.data.R
import com.zj.data.model.Note
import com.zj.ink.md.GlanceRenderMarkdown
import java.io.File

/**
 * Implementation of App Widget functionality.
 */

class NoteAppWidget : GlanceAppWidget() {

    companion object {
        private const val NOTE_ID_ARG = "noteId"
        const val NOTE_FROM_ARG = "noteFromArg"
        const val NOTE_FROM_VALUE = "widget"
    }

    override val stateDefinition: GlanceStateDefinition<List<Note>>
        get() = object : GlanceStateDefinition<List<Note>> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<List<Note>> {
                return NoteDataStore(context)
            }

            override fun getLocation(context: Context, fileKey: String): File {
                throw NotImplementedError("Not implemented for Notes App Widget State Definition")
            }
        }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            NoteWidget(
                recentNotes = currentState(),
                title = context.getString(R.string.note),
                noContent = context.getString(R.string.no_content)
            )
        }
    }

    @Composable
    fun NoteWidget(recentNotes: List<Note>, title: String, noContent: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .background(R.color.item_background)
                .padding(10.dp)
                .cornerRadius(16.dp)
                .appWidgetBackground(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = GlanceModifier.padding(bottom = 10.dp),
                text = title,
                style = TextStyle(fontSize = 16.sp, color = textColor)
            )
            if (recentNotes.isEmpty()) {
                NoDataContent(noContent)
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) { items(recentNotes.size) { NoteItem(note = recentNotes[it]) } }
            }
        }
    }

    @Composable
    private fun ColumnScope.NoDataContent(noContent: String) {
        Column(
            modifier = GlanceModifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_no_data),
                contentDescription = noContent,
            )
            Text(
                text = noContent,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = textColor
                ),
                modifier = GlanceModifier.padding(16.dp)
            )
        }
    }

    @Composable
    fun NoteItem(note: Note) {
        val intent =
            makeMainActivity(ComponentName("com.zj.note", "com.zj.note.MainActivity")).apply {
                action = Intent.ACTION_VIEW
                putExtra(NOTE_ID_ARG, note.id)
                putExtra(NOTE_FROM_ARG, NOTE_FROM_VALUE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        Column {
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(R.color.background)
                    .padding(8.dp)
                    .cornerRadius(10.dp)
                    .clickable(onClick = actionStartActivity(intent))
            ) {
                Text(
                    text = note.title,
                    style = TextStyle(fontSize = 16.sp, color = textColor),
                    modifier = GlanceModifier.padding(bottom = 8.dp),

                    )
                GlanceRenderMarkdown(note.content)
            }
            Box(modifier = GlanceModifier.height(10.dp)) { }
        }
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 200, heightDp = 150)
    @Composable
    private fun NoteWidgetPreview() {
        NoteWidget(
            listOf(
                Note(id = 1, title = "Note1", content = "Note 1"),
                Note(id = 2, title = "Note2", content = "Note 2"),
                Note(id = 3, title = "Note3", content = "Note 2"),
            ), "便签", "当前无内容"
        )
    }

}
