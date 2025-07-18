package com.zj.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.zj.data.R
import com.zj.data.model.Note
import java.io.File

/**
 * Implementation of App Widget functionality.
 */

class NoteAppWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<List<Note>>
        get() = object : GlanceStateDefinition<List<Note>> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<List<Note>> {
                return NoteDataStore(context)
            }

            override fun getLocation(context: Context, fileKey: String): File {
                throw NotImplementedError("Not implemented for Favorite App Widget State Definition")
            }
        }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            val recentNotes = currentState<List<Note>>()
            Column(
                modifier = GlanceModifier.fillMaxSize().background(R.color.item_background)
                    .padding(10.dp)
                    .cornerRadius(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = GlanceModifier.padding(bottom = 10.dp),
                    text = context.getString(R.string.note),
                    style = TextStyle(fontSize = 16.sp)
                )
                if (recentNotes.isEmpty()) {
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_no_data),
                            contentDescription = context.getString(R.string.no_content),
                        )
                        Text(
                            text = context.getString(R.string.no_content),
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = textColor
                            ),
                            modifier = GlanceModifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = GlanceModifier.fillMaxSize()
                    ) { items(recentNotes.size) { NoteItem(note = recentNotes[it]) } }
                }
            }
        }
    }

    @Composable
    fun NoteItem(note: Note) {
        Column {
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(R.color.background)
                    .padding(8.dp)
                    .cornerRadius(10.dp)
                    .appWidgetBackground()
            ) {
                Text(
                    text = note.title,
                    style = TextStyle(fontSize = 14.sp)
                )
                Text(
                    text = note.content.take(50) + "...",
                    style = TextStyle(fontSize = 12.sp)
                )
            }
            Box(modifier = GlanceModifier.height(10.dp)) { }
        }
    }

}

val textColor = ColorProvider(
    day = Color(25, 25, 25),
    night = Color(250, 250, 250)
)