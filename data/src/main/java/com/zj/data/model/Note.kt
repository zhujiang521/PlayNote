package com.zj.data.model

import androidx.navigation3.runtime.NavKey
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "note",
    indices = [
        Index(value = ["timestamp"], name = "idx_note_timestamp"),
        Index(value = ["title", "content"], name = "idx_note_search")
    ]
)
@Serializable
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
) : NavKey {
    fun toFts(): NoteFts = NoteFts(id, title, content)
}

fun Note?.isValid(): Boolean {
    if (this == null) return false
    return id > 0
}

const val INVALID_ID = -1