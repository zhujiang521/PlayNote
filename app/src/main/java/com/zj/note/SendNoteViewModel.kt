package com.zj.note

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.zj.data.model.Note
import com.zj.data.utils.saveImageToAppStorage
import com.zj.ink.data.NoteRepository
import com.zj.ink.widget.updateNoteWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class SendNoteViewModel @Inject constructor(
    private val application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    suspend fun insertNote(note: Note): Long {
        val id = noteRepository.insertNote(note)
        updateNoteWidget(getApplication())
        return id
    }

    suspend fun readAndSaveMarkdownFile(uri: Uri): Long {

        return try {
            val contentResolver: ContentResolver = application.contentResolver
            val title = getApplication<Application>().getString(com.zj.data.R.string.note)

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

    suspend fun readAndSaveImageFile(uri: Uri): Long {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            try {
                val title = context.getString(com.zj.data.R.string.share_image_via)
                // 保存图片到应用存储并获取路径
                val savedImagePath = context.saveImageToAppStorage(uri)

                // 创建包含图片的Markdown内容
                val imageMarkdown = "![${title}]($savedImagePath)"

                // 保存笔记并返回ID
                val note = Note(
                    id = 0,
                    title = title,
                    content = imageMarkdown,
                    timestamp = System.currentTimeMillis()
                )

                insertNote(note)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

}