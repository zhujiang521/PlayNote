package com.zj.widget

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.Room
import com.zj.data.model.Note
import com.zj.data.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteWidgetRepository(private val context: Context) {

    private val noteDao = databaseBuilder().noteDao()

    fun getRecentNotes(): Flow<List<Note>> = noteDao.getRecentNotes()


    private fun databaseBuilder(): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            CONVERSATION_DATABASE
        ).build()
    }

    companion object {
        private const val CONVERSATION_DATABASE = "conversation-database"
    }

}