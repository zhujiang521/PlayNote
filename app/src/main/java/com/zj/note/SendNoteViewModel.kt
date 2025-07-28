package com.zj.note

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zj.data.model.Note
import com.zj.ink.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class SendNoteViewModel @Inject constructor(
    private val application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    suspend fun insertNote(note: Note): Long {
        return noteRepository.insertNote(note)
    }

    suspend fun readAndSaveMarkdownFile(uri: Uri): Long {

        return try {
            val contentResolver: ContentResolver = application.contentResolver
            var title = "Imported Markdown"

            // 尝试从Uri获取文件名作为标题
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex("_display_name")
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        val fileName = cursor.getString(nameIndex)
                        title = fileName.substringBeforeLast(".", fileName)
                    }
                }
            }

            // 读取文件内容
            val markdownContent = contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: ""

            insertNote(Note(title = title, content = markdownContent))
        } catch (e: Exception) {
            e.printStackTrace()
            DEFAULT_INVALID_ID.toLong()
        }
    }

}