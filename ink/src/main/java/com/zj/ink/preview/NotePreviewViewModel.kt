package com.zj.ink.preview

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.zj.data.model.Note
import com.zj.data.export.MarkdownExporter
import com.zj.ink.data.BaseShareViewModel
import com.zj.ink.data.NoteRepository
import com.zj.ink.md.TaskListHelper
import com.zj.ink.widget.updateNoteWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotePreviewViewModel @Inject constructor(
    application: Application,
    private val noteRepository: NoteRepository
) : BaseShareViewModel(application) {

    private val _note = MutableStateFlow(Note(title = "", content = ""))
    val note: StateFlow<Note> get() = _note

    private val markdownExporter = MarkdownExporter(getApplication())

    /**
     * 根据ID加载笔记数据
     * 优化：使用IO线程加载数据，加载完成后立即发射，不等待解析
     */
    fun getNoteById(id: Int) {
        viewModelScope.launch {
            // 在IO线程加载数据
            val note = withContext(Dispatchers.IO) {
                noteRepository.getNoteById(id)
            }
            // 立即发射数据，UI层会异步解析
            _note.value = note
        }
    }

    /**
     * 切换任务列表项的完成状态并立即保存（预览页面专用）
     *
     * 直接修改数据库中的笔记内容，立即生效
     *
     * @param taskIndex 任务在所有 TaskList 元素中的索引
     * @param taskText 任务文本内容（用于验证）
     * @param currentChecked 当前的选中状态
     */
    fun toggleTaskAndSave(taskIndex: Int, taskText: String, currentChecked: Boolean) {
        viewModelScope.launch {
            val currentNote = _note.value

            // 使用 TaskListHelper 切换任务状态
            val newContent = TaskListHelper.toggleTaskState(
                content = currentNote.content,
                taskIndex = taskIndex,
                taskText = taskText,
                currentChecked = currentChecked
            )

            // 如果内容有变化，则更新数据库和本地状态
            if (newContent != currentNote.content) {
                val updatedNote = currentNote.copy(
                    content = newContent,
                    timestamp = System.currentTimeMillis() // 更新时间戳
                )
                noteRepository.updateNote(updatedNote)
                _note.value = updatedNote // 更新本地状态以刷新UI
                updateNoteWidget(getApplication())
            }
        }
    }

}