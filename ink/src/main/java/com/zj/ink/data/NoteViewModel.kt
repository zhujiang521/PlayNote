@file:OptIn(FlowPreview::class)

package com.zj.ink.data

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zj.data.R
import com.zj.data.model.Note
import com.zj.data.utils.DataStoreUtils
import com.zj.ink.widget.updateNoteWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val searchExpanded = mutableStateOf(false)

    // 在 NoteViewModel 中添加
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 同步 noteContent 和 note 的 content
    init {
        // 使用 debounce 优化搜索，避免频繁触发
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    loadNotes(query) // 根据搜索词加载数据
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

            // 默认加载所有笔记
            if (_searchQuery.value.isEmpty()) {
                loadNotes()
            }
        }
    }

    private fun loadNotes(query: String = "") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val flow = if (query.isEmpty()) {
                    noteRepository.getAllNotes()
                } else {
                    noteRepository.getNotesWithSearch(query)
                }

                flow.cachedIn(viewModelScope)
                    .collect { pagingData ->
                        _notes.value = pagingData
                        _isLoading.value = false
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
            updateNoteWidget(getApplication())
        }
    }

    fun refreshData() {
        loadNotes(_searchQuery.value)
    }

    companion object {
        private const val KEY_HAS_INSERTED = "has_inserted_default_note"
    }

}