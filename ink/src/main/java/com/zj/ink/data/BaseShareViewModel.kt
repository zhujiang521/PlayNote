package com.zj.ink.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zj.data.R
import com.zj.data.model.Note
import com.zj.data.utils.MarkdownExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 基础分享 ViewModel 类，提供导出笔记为不同格式的功能
 *
 * 该类负责处理笔记导出操作的状态管理，包括显示加载状态和进度消息
 */
open class BaseShareViewModel(private val application: Application) :
    AndroidViewModel(application) {

    // 导出状态管理 - 控制是否正在导出
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    // 导出消息状态管理 - 显示当前导出操作的描述信息
    private val _exportMessage = MutableStateFlow("")
    val exportMessage: StateFlow<String> = _exportMessage.asStateFlow()

    // Markdown 导出工具实例
    private val markdownExporter = MarkdownExporter(getApplication())

    /**
     * 将笔记导出为 PDF 格式
     *
     * @param note 要导出的笔记对象
     */
    fun exportMarkdownToPdf(note: Note) {
        exportMarkdownToPdf(note) {}
    }

    /**
     * 将笔记导出为 PDF 格式
     *
     * @param note 要导出的笔记对象
     * @param onFinishedListener 导出完成后的回调函数
     */
    fun exportMarkdownToPdf(note: Note, onFinishedListener: () -> Unit) {
        viewModelScope.launch {
            // 设置导出状态为正在进行，并显示相应消息
            _isExporting.value = true
            _exportMessage.value = application.getString(R.string.exporting_pdf)
            try {
                // 执行实际的 PDF 导出操作
                markdownExporter.exportMarkdownToPdf(
                    markdownContent = note.content,
                    title = note.title,
                )
            } finally {
                // 无论导出成功与否，都重置导出状态
                _isExporting.value = false
                _exportMessage.value = ""
                onFinishedListener()
            }
        }
    }

    /**
     * 将笔记导出为 HTML 格式
     *
     * @param note 要导出的笔记对象
     */
    fun exportMarkdownToHtml(note: Note) {
        exportMarkdownToHtml(note) {}
    }

    /**
     * 将笔记导出为 HTML 格式
     *
     * @param note 要导出的笔记对象
     */
    fun exportMarkdownToHtml(note: Note, onFinishedListener: () -> Unit) {
        viewModelScope.launch {
            // 设置导出状态为正在进行，并显示相应消息
            _isExporting.value = true
            _exportMessage.value = application.getString(R.string.exporting_html)
            try {
                // 执行实际的 HTML 导出操作
                markdownExporter.exportMarkdownToHtml(
                    markdownContent = note.content,
                    title = note.title,
                )
            } finally {
                // 无论导出成功与否，都重置导出状态
                _isExporting.value = false
                _exportMessage.value = ""
                onFinishedListener()
            }
        }
    }

    /**
     * 将笔记导出为 Markdown 格式文件
     *
     * @param note 要导出的笔记对象
     */
    fun exportMarkdownToFile(note: Note) {
        exportMarkdownToFile(note) {}
    }

    /**
     * 将笔记导出为 Markdown 格式文件
     *
     * @param note 要导出的笔记对象
     */
    fun exportMarkdownToFile(note: Note, onFinishedListener: () -> Unit) {
        viewModelScope.launch {
            // 设置导出状态为正在进行，并显示相应消息
            _isExporting.value = true
            _exportMessage.value = application.getString(R.string.exporting_md)
            try {
                // 执行实际的 Markdown 文件导出操作
                markdownExporter.exportMarkdownToFile(
                    markdownContent = note.content,
                    title = note.title,
                )
            } finally {
                // 无论导出成功与否，都重置导出状态
                _isExporting.value = false
                _exportMessage.value = ""
                onFinishedListener()
            }
        }
    }

}
