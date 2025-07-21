package com.zj.ink.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.zj.data.model.Note
import com.zj.data.room.NoteDao
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityRetainedScoped
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    fun getAllNotes(): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { noteDao.getAllNotes() }
        ).flow
    }

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note.id)
        }
    }

    suspend fun getNoteById(id: Int): Note {
        return withContext(Dispatchers.IO) {
            noteDao.getNoteById(id)
        }
    }

    fun getNotesWithSearch(query: String): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { noteDao.getNotesWithSearch(query) }
        ).flow
    }

    suspend fun getNoteCount(): Int {
        return withContext(Dispatchers.IO) {
            noteDao.getNoteCount()
        }
    }

    fun getRecentNotes(): Flow<List<Note>> = noteDao.getRecentNotes()

}