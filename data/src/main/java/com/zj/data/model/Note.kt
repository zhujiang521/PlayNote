package com.zj.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
)

fun Note?.isValid(): Boolean {
    if (this == null) return false
    return id > 0
}

const val INVALID_ID = -1