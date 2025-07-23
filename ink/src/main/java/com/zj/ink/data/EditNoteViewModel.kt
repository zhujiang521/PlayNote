package com.zj.ink.data

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.ink.brush.StockBrushes
import androidx.ink.strokes.Stroke
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zj.data.model.Note
import com.zj.data.utils.MarkdownExporter
import com.zj.data.R
import com.zj.ink.widget.NoteAppWidget
import com.zj.ink.widget.updateNoteWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    // 新增：使用 TextFieldValue 管理文本和光标位置
    private val _noteContent = mutableStateOf(TextFieldValue(""))
    val noteContent: MutableState<TextFieldValue> = _noteContent

    // 同步 Note 对象的 content
    private val _note = MutableStateFlow(Note(title = "", content = _noteContent.value.text))
    val note: StateFlow<Note> get() = _note

    // 将原来的 Note 列表改为 EditState 列表
    private val noteUndoStack = mutableListOf<EditState>()
    private val noteRedoStack = mutableListOf<EditState>()

    private val _undoEnabled = MutableStateFlow(false)
    val undoEnabled: StateFlow<Boolean> get() = _undoEnabled

    private val _redoEnabled = MutableStateFlow(false)
    val redoEnabled: StateFlow<Boolean> get() = _redoEnabled

    // 新增：标记是否有未保存的修改
    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> get() = _isDirty
    private val markdownExporter = MarkdownExporter(getApplication())

    // 绘图状态
    val finishedStrokes = mutableStateOf<Set<Stroke>>(emptySet())
    val selectedColor = mutableIntStateOf(Color.Black.toArgb())
    val selectedBrushFamily = mutableStateOf(StockBrushes.pressurePenLatest)

    @SuppressLint("MutableCollectionMutableState")
    val drawUndoStack = mutableStateOf(mutableListOf(finishedStrokes.value))

    @SuppressLint("MutableCollectionMutableState")
    val drawRedoStack = mutableStateOf(mutableListOf<Set<Stroke>>())
    val selectedBrushSize = mutableFloatStateOf(5f)
    val showDialog = mutableStateOf(false) // 控制对话框显示
    val showColorPicker = mutableStateOf(false)
    val showPenPicker = mutableStateOf(false)
    val saveBitmap = mutableStateOf(false)
    val showPenSizePicker = mutableStateOf(false)
    val showEraserSizePicker = mutableStateOf(false)
    val showClearDraw = mutableStateOf(false)
    val showPreview = mutableStateOf(true)

    var showTablePicker = mutableStateOf(false)

    val isEraserMode = mutableStateOf(false)

    // 橡皮擦半径（默认25）
    val eraserRadius = mutableFloatStateOf(25f)

    // 行数输入
    var rows = mutableStateOf("2")

    // 列数输入
    var cols = mutableStateOf("2")

    // 同步 noteContent 和 note 的 content
    init {
        viewModelScope.launch {
            snapshotFlow { _noteContent.value }
                .filter { newContent -> newContent.text != _note.value.content }
                .collect { newContent ->
                    _note.update { it.copy(content = newContent.text) }
                }
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            if (_note.value.title.isBlank()) {
                _note.value.title = getApplication<Application>().getString(R.string.note)
                updateNoteTitle(_note.value.title)
            }
            if (_note.value.id > 0) {
                noteRepository.updateNote(_note.value)
            } else {
                noteRepository.insertNote(_note.value)
            }
            _isDirty.value = false // 保存后标记为已保存
            updateNoteWidget(getApplication())
        }
    }

    // 更新方法
    fun updateNoteTitle(newTitle: String) {
        val currentState = _note.value // 记录修改前的状态
        noteUndoStack.add(EditState(currentState, newTitle.length))
        noteRedoStack.clear()
        _note.value = currentState.copy(title = newTitle)
        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _isDirty.value = true
    }

    fun updateNoteContent(newValue: TextFieldValue) {
        val currentState = _note.value
        val currentCursorPosition = newValue.selection.min // 获取当前光标位置
        noteUndoStack.add(EditState(currentState, currentCursorPosition))
        noteRedoStack.clear()

        _noteContent.value = newValue
        _note.value = currentState.copy(content = newValue.text)

        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _redoEnabled.value = noteRedoStack.isNotEmpty()
        _isDirty.value = true
    }


    // 修改后的 insertTemplate
    fun insertTemplate(template: String) {
        val current = _noteContent.value
        val cursor = current.selection.min
        val placeholderIndex = template.indexOf("%s")
        val newTemplate = template.replace("%s", "")
        val newContentText =
            current.text.substring(0, cursor) +
                    newTemplate +
                    current.text.substring(cursor)
        val newCursorPosition = if (placeholderIndex != -1) {
            cursor + placeholderIndex
        } else {
            cursor + newTemplate.length
        }

        // 通过 updateNoteContent 触发 undo/redo 记录
        updateNoteContent(
            TextFieldValue(newContentText, TextRange(newCursorPosition))
        )
    }

    fun undo() {
        if (noteUndoStack.isEmpty()) return
        val previousState = noteUndoStack.last()
        val currentState = _note.value
        val currentCursorPosition = _noteContent.value.selection.min

        noteRedoStack.add(EditState(currentState, currentCursorPosition))

        _note.value = previousState.note
        _noteContent.value = TextFieldValue(
            previousState.note.content,
            TextRange(previousState.cursorPosition)
        )

        noteUndoStack.removeAt(noteUndoStack.lastIndex)
        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _redoEnabled.value = noteRedoStack.isNotEmpty()
    }

    fun redo() {
        if (noteRedoStack.isEmpty()) return
        val nextState = noteRedoStack.last()
        val currentState = _note.value
        val currentCursorPosition = _noteContent.value.selection.min

        noteUndoStack.add(EditState(currentState, currentCursorPosition))

        _note.value = nextState.note
        _noteContent.value = TextFieldValue(
            nextState.note.content,
            TextRange(nextState.cursorPosition)
        )

        noteRedoStack.removeAt(noteRedoStack.lastIndex)
        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _redoEnabled.value = noteRedoStack.isNotEmpty()
    }

    // 在 getNoteById 中同步 noteContent
    fun getNoteById(id: Int) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(id)
            _note.value = note
            // 新增：同步到 noteContent
            _noteContent.value = TextFieldValue(note.content)
            _isDirty.value = false // 加载旧数据不算修改
        }
    }

    fun updateNoteContentImage(file: File) {
        val currentState = _note.value
        noteUndoStack.add(EditState(currentState, currentState.content.length))
        noteRedoStack.clear()

        val currentContent = _noteContent.value
        val cursor = currentContent.selection.min
        val imagePath = file.absolutePath
        val markdownImage = "\n![${file.name}]($imagePath)"

        val newContentText =
            currentContent.text.substring(0, cursor) +
                    markdownImage +
                    currentContent.text.substring(cursor)

        val newCursorPosition = cursor + markdownImage.length

        // 通过统一方法更新内容并记录 undo
        updateNoteContent(
            TextFieldValue(newContentText, TextRange(newCursorPosition))
        )
    }

    fun exportMarkdownToDocx() {
        viewModelScope.launch {
            markdownExporter.exportMarkdownToFile(
                markdownContent = _note.value.content,
                title = _note.value.title,
            )
        }
    }

    fun clearDrawState() {
        // 重置绘图状态
        finishedStrokes.value = emptySet()
        selectedColor.intValue = Color.Black.toArgb()
        selectedBrushFamily.value = StockBrushes.pressurePenLatest
        drawUndoStack.value.clear()
        drawRedoStack.value.clear()
        selectedBrushSize.floatValue = 5f
        eraserRadius.floatValue = 25f

        // 重置对话框显示状态
        showDialog.value = false
        showColorPicker.value = false
        showPenPicker.value = false
        saveBitmap.value = false
        showPenSizePicker.value = false
        showEraserSizePicker.value = false
        showClearDraw.value = false

        // 重置其他状态
        isEraserMode.value = false
    }

}

data class EditState(val note: Note, val cursorPosition: Int)