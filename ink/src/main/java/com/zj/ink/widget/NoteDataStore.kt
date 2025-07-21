package com.zj.ink.widget

import android.content.Context
import androidx.datastore.core.DataStore
import com.zj.data.model.Note
import com.zj.ink.data.NoteRepository
import com.zj.ink.data.databaseBuilder
import kotlinx.coroutines.flow.Flow

class NoteDataStore(private val context: Context) : DataStore<List<Note>> {

    private val notesRepository by lazy {
        val room = databaseBuilder(context)
        NoteRepository(room.noteDao())
    }

    override val data: Flow<List<Note>>
        get() = notesRepository.getRecentNotes()


    override suspend fun updateData(transform: suspend (t: List<Note>) -> List<Note>): List<Note> {
        throw NotImplementedError("Not implemented in Favorite Data Store")
    }

}
