package com.zj.widget

import android.content.Context
import androidx.datastore.core.DataStore
import com.zj.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteDataStore(context: Context) : DataStore<List<Note>> {

    private val noteWidgetRepository = NoteWidgetRepository(context)

    override val data: Flow<List<Note>>
        get() {
            val notes = noteWidgetRepository.getRecentNotes()
            return notes
        }

    override suspend fun updateData(transform: suspend (t: List<Note>) -> List<Note>): List<Note> {
        throw NotImplementedError("Not implemented in Favorite Data Store")
    }

}
