package com.zj.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zj.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNote(note: Note): Int // 返回类型改为 Int

    @Query("SELECT * FROM note ORDER BY timestamp DESC")
    fun getAllNotes(): PagingSource<Int, Note>

    @Query("DELETE FROM note WHERE id = :noteId")
    suspend fun deleteNote(noteId: Int)

    @Query("DELETE FROM note WHERE id IN (:noteIds)")
    suspend fun deleteNotes(noteIds: List<Int>)

    @Query("SELECT *  FROM note WHERE id = :id")
    fun getNoteById(id: Int): Note

    // 在 NoteDao 中添加模糊查询方法：
    @Query("""
        SELECT note.* FROM note 
        JOIN note_fts ON note.id = note_fts.docid 
        WHERE note_fts MATCH :query 
        ORDER BY timestamp DESC
    """)
    fun getNotesWithSearch(query: String): PagingSource<Int, Note>

    @Query("SELECT COUNT(*) FROM note")
    fun getNoteCount(): Int

    @Query("SELECT * FROM note ORDER BY timestamp DESC LIMIT 10")
    fun getRecentNotes(): Flow<List<Note>>

}