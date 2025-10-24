package com.zj.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4(contentEntity = Note::class)
@Entity(tableName = "note_fts")
data class NoteFts(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    val title: String,
    val content: String
)