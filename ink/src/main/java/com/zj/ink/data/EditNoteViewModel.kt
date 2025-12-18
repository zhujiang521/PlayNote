package com.zj.ink.data

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes
import androidx.ink.strokes.Stroke
import androidx.lifecycle.viewModelScope
import com.zj.data.R
import com.zj.data.model.Note
import com.zj.ink.brush.BrushFactory
import com.zj.ink.brush.BrushPresetManager
import com.zj.ink.brush.BrushProperties
import com.zj.ink.brush.BrushType
import com.zj.ink.md.TaskListHelper
import com.zj.ink.widget.updateNoteWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    application: Application,
    private val noteRepository: NoteRepository
) : BaseShareViewModel(application) {

    // 新增：使用 TextFieldValue 管理标题和内容的文本及光标位置
    private val _noteTitle = mutableStateOf(TextFieldValue(""))
    val noteTitle: MutableState<TextFieldValue> = _noteTitle

    private val _noteContent = mutableStateOf(TextFieldValue(""))
    val noteContent: MutableState<TextFieldValue> = _noteContent

    // 同步 Note 对象
    private val _note = MutableStateFlow(Note(title = _noteTitle.value.text, content = _noteContent.value.text))
    val note: StateFlow<Note> = _note.asStateFlow()

    // 将原来的 Note 列表改为 EditState 列表
    private val noteUndoStack = mutableListOf<EditState>()
    private val noteRedoStack = mutableListOf<EditState>()

    private val _undoEnabled = MutableStateFlow(false)
    val undoEnabled: StateFlow<Boolean> = _undoEnabled.asStateFlow()

    private val _redoEnabled = MutableStateFlow(false)
    val redoEnabled: StateFlow<Boolean> = _redoEnabled.asStateFlow()

    // 新增：标记是否有未保存的修改
    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

    // 绘图状态
    private val _finishedStrokes = mutableStateOf<Set<Stroke>>(emptySet())
    val finishedStrokes: MutableState<Set<Stroke>> = _finishedStrokes

    private val _selectedColor = mutableIntStateOf(Color.Black.toArgb())
    val selectedColor: MutableIntState = _selectedColor

    private val _selectedBrushFamily = mutableStateOf(StockBrushes.pressurePen())
    val selectedBrushFamily: MutableState<BrushFamily> = _selectedBrushFamily

    // 新增画笔系统状态
    private val _selectedBrushType = mutableStateOf(BrushType.PEN)
    val selectedBrushType: MutableState<BrushType> = _selectedBrushType

    private val _currentBrushProperties = mutableStateOf(BrushProperties.fromBrushType(BrushType.PEN))
    val currentBrushProperties: MutableState<BrushProperties> = _currentBrushProperties

    @SuppressLint("MutableCollectionMutableState")
    private val _drawUndoStack = mutableStateOf(mutableListOf(_finishedStrokes.value))
    val drawUndoStack: MutableState<MutableList<Set<Stroke>>> = _drawUndoStack

    @SuppressLint("MutableCollectionMutableState")
    private val _drawRedoStack = mutableStateOf(mutableListOf<Set<Stroke>>())
    val drawRedoStack: MutableState<MutableList<Set<Stroke>>> = _drawRedoStack

    private val _selectedBrushSize = mutableFloatStateOf(5f)
    val selectedBrushSize: MutableFloatState = _selectedBrushSize

    // UI 状态控制
    private val _showDialog = mutableStateOf(false)
    val showDialog: MutableState<Boolean> = _showDialog

    private val _showColorPicker = mutableStateOf(false)
    val showColorPicker: MutableState<Boolean> = _showColorPicker

    private val _showPenPicker = mutableStateOf(false)
    val showPenPicker: MutableState<Boolean> = _showPenPicker

    private val _saveBitmap = mutableStateOf(false)
    val saveBitmap: MutableState<Boolean> = _saveBitmap

    private val _showPenSizePicker = mutableStateOf(false)
    val showPenSizePicker: MutableState<Boolean> = _showPenSizePicker

    private val _showEraserSizePicker = mutableStateOf(false)
    val showEraserSizePicker: MutableState<Boolean> = _showEraserSizePicker

    private val _showClearDraw = mutableStateOf(false)
    val showClearDraw: MutableState<Boolean> = _showClearDraw

    private val _showPreview = mutableStateOf(true)
    val showPreview: MutableState<Boolean> = _showPreview

    private val _showTablePicker = mutableStateOf(false)
    val showTablePicker: MutableState<Boolean> = _showTablePicker

    private val _showBrushPropertyPanel = mutableStateOf(false)
    val showBrushPropertyPanel: MutableState<Boolean> = _showBrushPropertyPanel

    // 画笔预设管理器
    val brushPresetManager = BrushPresetManager(getApplication())

    private val _isEraserMode = mutableStateOf(false)
    val isEraserMode: MutableState<Boolean> = _isEraserMode

    // 橡皮擦半径（默认25）
    private val _eraserRadius = mutableFloatStateOf(25f)
    val eraserRadius: MutableFloatState = _eraserRadius

    // 表格行列数输入
    private val _rows = mutableStateOf("2")
    val rows: MutableState<String> = _rows

    private val _cols = mutableStateOf("2")
    val cols: MutableState<String> = _cols

    // 同步 noteTitle 和 noteContent 到 note
    init {
        // 同步标题
        viewModelScope.launch {
            snapshotFlow { _noteTitle.value }
                .filter { newTitle -> newTitle.text != _note.value.title }
                .collect { newTitle ->
                    _note.update { it.copy(title = newTitle.text) }
                }
        }

        // 同步内容
        viewModelScope.launch {
            snapshotFlow { _noteContent.value }
                .filter { newContent -> newContent.text != _note.value.content }
                .collect { newContent ->
                    _note.update { it.copy(content = newContent.text) }
                }
        }
    }

    fun saveNote(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            // 从TextFieldValue获取最新的文本内容
            val currentTitle = _noteTitle.value.text
            val currentContent = _noteContent.value.text

            val noteToSave = if (currentTitle.isBlank() || currentContent.isBlank()) {
                val title = currentTitle.ifBlank {
                    getApplication<Application>().getString(R.string.note)
                }

                val content = currentContent.ifBlank {
                    title
                }

                _note.value.copy(title = title, content = content)
            } else {
                _note.value.copy(title = currentTitle, content = currentContent)
            }

            if (noteToSave.id > 0) {
                noteRepository.updateNote(noteToSave)
            } else {
                noteRepository.insertNote(noteToSave)
            }

            // 更新内部状态，如果标题被设置为默认值，同步到noteTitle
            _note.value = noteToSave
            if (currentTitle.isBlank() && noteToSave.title.isNotBlank()) {
                _noteTitle.value = TextFieldValue(
                    text = noteToSave.title,
                    selection = TextRange(noteToSave.title.length)
                )
            }

            _isDirty.value = false
            updateNoteWidget(getApplication())

            // 保存完成后的回调，用于清除焦点
            onSaved()
        }
    }

    // 更新标题 - 接收TextFieldValue保持光标位置
    fun updateNoteTitle(newValue: TextFieldValue) {
        val currentState = _note.value
        val currentCursorPosition = newValue.selection.min
        noteUndoStack.add(EditState(currentState, currentCursorPosition))
        noteRedoStack.clear()

        _noteTitle.value = newValue
        _note.value = currentState.copy(title = newValue.text)

        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _redoEnabled.value = noteRedoStack.isNotEmpty()
        _isDirty.value = true
    }

    // 兼容旧的String版本（如果有其他地方调用）
    @Deprecated("使用 updateNoteTitle(TextFieldValue) 以保持光标位置", ReplaceWith("updateNoteTitle(TextFieldValue(newTitle))"))
    fun updateNoteTitle(newTitle: String) {
        updateNoteTitle(TextFieldValue(
            text = newTitle,
            selection = TextRange(newTitle.length) // 光标放在末尾
        ))
    }

    fun updateNoteContent(newValue: TextFieldValue) {
        val currentState = _note.value
        val currentCursorPosition = newValue.selection.min
        noteUndoStack.add(EditState(currentState, currentCursorPosition))
        noteRedoStack.clear()

        _noteContent.value = newValue
        _note.value = currentState.copy(content = newValue.text)

        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _redoEnabled.value = noteRedoStack.isNotEmpty()
        _isDirty.value = true
    }

    /**
     * 切换任务列表项的完成状态（编辑页面专用）
     *
     * 仅修改内存中的内容，不立即保存到数据库
     * 支持撤销/重做功能
     *
     * @param taskIndex 任务在所有 TaskList 元素中的索引
     * @param taskText 任务文本内容（用于验证）
     * @param currentChecked 当前的选中状态
     */
    fun toggleTaskInContent(taskIndex: Int, taskText: String, currentChecked: Boolean) {
        val currentContent = _note.value.content

        // 使用 TaskListHelper 切换任务状态
        val newContent = TaskListHelper.toggleTaskState(
            content = currentContent,
            taskIndex = taskIndex,
            taskText = taskText,
            currentChecked = currentChecked
        )

        // 如果内容有变化，则更新
        if (newContent != currentContent) {
            // 通过 updateNoteContent 更新内容，自动记录到撤销栈
            updateNoteContent(
                TextFieldValue(
                    text = newContent,
                    selection = _noteContent.value.selection // 保持光标位置
                )
            )
        }
    }

    /**
     * 智能插入Markdown模板
     * - 有选中文本：用模板包裹选中内容
     * - 无选中文本：插入模板并定位光标到编辑位置
     */
    fun insertTemplate(template: String) {
        val current = _noteContent.value
        val selection = current.selection
        val hasSelection = selection.start != selection.end

        val newContentText: String
        val newCursorPosition: Int

        if (hasSelection) {
            // 有选中文本：包裹选中内容
            val selectedText = current.text.substring(selection.start, selection.end)
            val wrappedText = template.replace("%s", selectedText)

            newContentText = current.text.substring(0, selection.start) +
                    wrappedText +
                    current.text.substring(selection.end)

            // 光标定位到包裹后的文本末尾
            newCursorPosition = selection.start + wrappedText.length
        } else {
            // 无选中文本：插入模板
            val cursor = current.selection.min
            val placeholderIndex = template.indexOf("%s")
            val newTemplate = template.replace("%s", "")

            newContentText = current.text.substring(0, cursor) +
                    newTemplate +
                    current.text.substring(cursor)

            // 光标定位到占位符位置或模板末尾
            newCursorPosition = if (placeholderIndex != -1) {
                cursor + placeholderIndex
            } else {
                cursor + newTemplate.length
            }
        }

        // 通过 updateNoteContent 触发 undo/redo 记录
        updateNoteContent(
            TextFieldValue(newContentText, TextRange(newCursorPosition))
        )
    }

    @SuppressLint("NewApi")
    fun undo() {
        if (noteUndoStack.isEmpty()) return
        val previousState = noteUndoStack.removeLast()
        val currentState = _note.value
        val currentCursorPosition = _noteContent.value.selection.min

        noteRedoStack.add(EditState(currentState, currentCursorPosition))

        _note.value = previousState.note
        _noteContent.value = TextFieldValue(
            previousState.note.content,
            TextRange(previousState.cursorPosition)
        )

        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _redoEnabled.value = noteRedoStack.isNotEmpty()
    }

    @SuppressLint("NewApi")
    fun redo() {
        if (noteRedoStack.isEmpty()) return
        val nextState = noteRedoStack.removeLast()
        val currentState = _note.value
        val currentCursorPosition = _noteContent.value.selection.min

        noteUndoStack.add(EditState(currentState, currentCursorPosition))

        _note.value = nextState.note
        _noteContent.value = TextFieldValue(
            nextState.note.content,
            TextRange(nextState.cursorPosition)
        )

        _undoEnabled.value = noteUndoStack.isNotEmpty()
        _redoEnabled.value = noteRedoStack.isNotEmpty()
    }

    // 在 getNoteById 中同步 noteTitle 和 noteContent
    fun getNoteById(id: Int) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(id)
            _note.value = note
            // 同步标题和内容到 TextFieldValue，光标默认放在末尾
            _noteTitle.value = TextFieldValue(
                text = note.title,
                selection = TextRange(note.title.length)
            )
            _noteContent.value = TextFieldValue(
                text = note.content,
                selection = TextRange(note.content.length)
            )
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

    fun clearDrawState() {
        // 重置绘图状态
        _finishedStrokes.value = emptySet()
        _selectedColor.intValue = Color.Black.toArgb()
        _selectedBrushFamily.value = StockBrushes.pressurePen()
        _drawUndoStack.value.clear()
        _drawRedoStack.value.clear()
        _selectedBrushSize.floatValue = 5f
        _eraserRadius.floatValue = 25f

        // 重置对话框显示状态
        _showDialog.value = false
        _showColorPicker.value = false
        _showPenPicker.value = false
        _saveBitmap.value = false
        _showPenSizePicker.value = false
        _showEraserSizePicker.value = false
        _showClearDraw.value = false

        // 重置其他状态
        _isEraserMode.value = false
    }

    // UI 状态控制方法
    fun setShowDialog(show: Boolean) {
        _showDialog.value = show
    }

    fun setShowColorPicker(show: Boolean) {
        _showColorPicker.value = show
    }

    fun setShowPenPicker(show: Boolean) {
        _showPenPicker.value = show
    }

    fun setSaveBitmap(save: Boolean) {
        _saveBitmap.value = save
    }

    fun setShowPenSizePicker(show: Boolean) {
        _showPenSizePicker.value = show
    }

    fun setShowEraserSizePicker(show: Boolean) {
        _showEraserSizePicker.value = show
    }

    fun setShowPreview(show: Boolean) {
        _showPreview.value = show
    }

    fun setIsEraserMode(isEraser: Boolean) {
        _isEraserMode.value = isEraser
    }

    // 新增画笔系统相关方法

    /**
     * 切换画笔类型
     */
    fun setBrushType(brushType: BrushType) {
        _selectedBrushType.value = brushType
        _currentBrushProperties.value = BrushProperties.fromBrushType(brushType)
            .copy(color = Color(_selectedColor.intValue))

        // 更新传统画笔系统的兼容性
        _selectedBrushFamily.value = brushType.defaultFamily
        _selectedBrushSize.floatValue = brushType.defaultSize

        // 添加到最近使用的画笔预设
        viewModelScope.launch {
            val preset = com.zj.ink.brush.BrushPreset.fromBrushProperties(
                id = "${brushType.name}_recent",
                name = brushType.displayName,
                properties = _currentBrushProperties.value
            )
            brushPresetManager.addToRecentPresets(preset)
        }
    }

    /**
     * 更新画笔属性
     */
    fun updateBrushProperties(properties: BrushProperties) {
        _currentBrushProperties.value = properties.validate()

        // 同步到传统系统
        _selectedColor.intValue = properties.color.toArgb()
        _selectedBrushSize.floatValue = properties.size
        _selectedBrushFamily.value = properties.brushType.defaultFamily
    }

    /**
     * 设置画笔颜色
     */
    fun setBrushColor(color: Color) {
        _selectedColor.intValue = color.toArgb()
        _currentBrushProperties.value = _currentBrushProperties.value.copy(color = color)
    }

    /**
     * 设置画笔大小
     */
    fun setBrushSize(size: Float) {
        _selectedBrushSize.floatValue = size
        _currentBrushProperties.value = _currentBrushProperties.value.copy(size = size)
    }

    /**
     * 获取当前画笔实例
     */
    fun getCurrentBrush(): androidx.ink.brush.Brush {
        return BrushFactory.createBrush(_currentBrushProperties.value)
    }

    /**
     * 显示/隐藏画笔属性面板
     */
    fun setShowBrushPropertyPanel(show: Boolean) {
        _showBrushPropertyPanel.value = show
    }

    /**
     * 重置画笔到默认状态
     */
    fun resetBrushToDefault() {
        val defaultType = BrushType.PEN
        setBrushType(defaultType)
    }

    /**
     * 检查画笔是否支持压感
     */
    fun isBrushPressureEnabled(): Boolean {
        return _currentBrushProperties.value.pressureEnabled &&
               _selectedBrushType.value.supportsPressure
    }

    /**
     * 检查画笔是否支持纹理
     */
    fun isBrushTextureEnabled(): Boolean {
        return _currentBrushProperties.value.textureEnabled &&
               _selectedBrushType.value.supportsTexture
    }


    /**
     * 保存当前画笔设置为预设
     */
    fun saveCurrentBrushAsPreset(name: String) {
        viewModelScope.launch {
            val preset = com.zj.ink.brush.BrushPreset.fromBrushProperties(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                properties = _currentBrushProperties.value
            )
            brushPresetManager.saveUserPreset(preset)
        }
    }

    /**
     * 应用画笔预设
     */
    fun applyBrushPreset(presetId: String) {
        viewModelScope.launch {
            val preset = brushPresetManager.findPresetById(presetId)
            preset?.toBrushProperties()?.let { properties ->
                updateBrushProperties(properties)
                setBrushType(properties.brushType)

                // 添加到最近使用
                brushPresetManager.addToRecentPresets(preset)
            }
        }
    }

}

data class EditState(val note: Note, val cursorPosition: Int)