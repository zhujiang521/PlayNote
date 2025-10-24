package com.zj.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zj.data.model.Note
import com.zj.data.model.NoteFts

@Database(
    entities = [Note::class, NoteFts::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建FTS表
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS note_fts USING fts4(
                        content='note',
                        docid='id',
                        title,
                        content
                    )
                """)
                // 从原表复制数据到FTS表
                database.execSQL("""
                    INSERT INTO note_fts(docid, title, content)
                    SELECT id, title, content FROM note
                """)
            }
        }
    }
}
