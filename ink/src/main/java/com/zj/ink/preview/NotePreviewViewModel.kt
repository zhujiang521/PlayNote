package com.zj.ink.preview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zj.data.model.Note
import com.zj.data.utils.MarkdownExporter
import com.zj.ink.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotePreviewViewModel @Inject constructor(
    application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    private val _note = MutableStateFlow(Note(title = "", content = ""))
    val note: StateFlow<Note> get() = _note

    private val markdownExporter = MarkdownExporter(getApplication())

    // 在 getNoteById 中同步 noteContent
    fun getNoteById(id: Int) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(id)
            _note.value = note
        }
    }

    fun exportMarkdown() {
        viewModelScope.launch {
            markdownExporter.exportMarkdownToFile(
                markdownContent = _note.value.content,
                title = _note.value.title,
            )
        }
    }

    fun exportMarkdownToPdf() {
        viewModelScope.launch {
            markdownExporter.exportMarkdownToPdf(
                markdownContent = _note.value.content,
                title = _note.value.title,
            )
        }
    }

    fun exportMarkdownToHtml() {
        viewModelScope.launch {
            markdownExporter.exportMarkdownToHtml(
                markdownContent = _note.value.content,
                title = _note.value.title,
            )
        }
    }

}