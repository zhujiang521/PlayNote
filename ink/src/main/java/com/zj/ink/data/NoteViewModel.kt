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
import com.zj.ink.md.TaskListHelper
import com.zj.ink.widget.updateNoteWidget
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
            updateNoteWidget(getApplication())
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            noteRepository.getAllNotes()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _notes.value = pagingData
                }
        }
    }

    /**
     * 切换任务列表项的完成状态并立即保存（列表页面专用）
     *
     * 直接修改数据库中的笔记内容，立即生效
     *
     * @param note 要修改的笔记对象
     * @param taskIndex 任务在所有 TaskList 元素中的索引
     * @param taskText 任务文本内容（用于验证）
     * @param currentChecked 当前的选中状态
     */
    fun toggleTaskAndSave(note: Note, taskIndex: Int, taskText: String, currentChecked: Boolean) {
        viewModelScope.launch {
            // 使用 TaskListHelper 切换任务状态
            val newContent = TaskListHelper.toggleTaskState(
                content = note.content,
                taskIndex = taskIndex,
                taskText = taskText,
                currentChecked = currentChecked
            )

            // 如果内容有变化，则更新数据库
            if (newContent != note.content) {
                val updatedNote = note.copy(
                    content = newContent,
                    timestamp = System.currentTimeMillis() // 更新时间戳
                )
                noteRepository.updateNote(updatedNote)
                updateNoteWidget(getApplication())
            }
        }
    }

    companion object {
        private const val KEY_HAS_INSERTED = "has_inserted_default_note"
    }

}