package com.zj.ink.data

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zj.data.model.Note
import com.zj.data.utils.DataStoreUtils
import com.zj.data.R
import com.zj.ink.widget.NoteAppWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    private val _notes: MutableStateFlow<PagingData<Note>> =
        MutableStateFlow(PagingData.empty())

    val notes: StateFlow<PagingData<Note>> = _notes.asStateFlow()

    val searchExpanded = mutableStateOf(false)

    val recentNotes = noteRepository.getRecentNotes()

    // 在 NoteViewModel 中添加
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 同步 noteContent 和 note 的 content
    init {
        viewModelScope.launch {
            _searchQuery.collect { query ->
                loadSearchNotes(query) // 根据搜索词加载数据
            }
        }
        viewModelScope.launch {
            val hasInserted = DataStoreUtils.readBooleanData(KEY_HAS_INSERTED, false)
            if (!hasInserted) {
                val count = noteRepository.getNoteCount()
                if (count == 0) {
                    noteRepository.insertNote(
                        Note(
                            title = application.getString(R.string.markdown_default_title),
                            content = markdownSample
                        )
                    )
                    DataStoreUtils.saveBooleanData(KEY_HAS_INSERTED, true)
                }
            }
            noteRepository.getAllNotes()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _notes.value = pagingData
                }
        }
        viewModelScope.launch {
            recentNotes.collect {
                // 示例：在插入或更新 Note 后调用
                val glanceIds =
                    GlanceAppWidgetManager(application).getGlanceIds(NoteAppWidget::class.java)
                glanceIds.forEach {
                    NoteAppWidget().update(application, it)
                }
            }
        }
    }

    private fun loadSearchNotes(query: String) {
        viewModelScope.launch {
            noteRepository.getNotesWithSearch(query)
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _notes.value = pagingData
                }
        }
    }


    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    companion object {
        private const val KEY_HAS_INSERTED = "has_inserted_default_note"
    }

}