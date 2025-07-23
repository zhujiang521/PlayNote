package com.zj.ink.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NoteAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NoteAppWidget()
}

suspend fun updateNoteWidget(context: Context) {
    val glanceIds =
        GlanceAppWidgetManager(context).getGlanceIds(NoteAppWidget::class.java)
    glanceIds.forEach {
        NoteAppWidget().update(context, it)
    }
}