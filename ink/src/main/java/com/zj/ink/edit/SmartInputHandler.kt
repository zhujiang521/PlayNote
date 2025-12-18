package com.zj.ink.edit

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 智能输入处理器
 * 提供自动配对、智能缩进等编辑增强功能
 */
object SmartInputHandler {

    // 自动配对的字符映射
    private val autoPairMap = mapOf(
        '(' to ')',
        '[' to ']',
        '{' to '}',
        '"' to '"',
        '\'' to '\'',
        '`' to '`',
        '*' to '*',
        '_' to '_'
    )

    /**
     * 处理字符输入，实现自动配对
     * @param current 当前TextFieldValue
     * @param input 输入的字符
     * @return 处理后的TextFieldValue，如果不需要处理返回null
     */
    fun handleCharInput(current: TextFieldValue, input: Char): TextFieldValue? {
        val closingChar = autoPairMap[input] ?: return null

        val selection = current.selection

        // 如果有选中文本，用配对字符包裹
        if (selection.start != selection.end) {
            val selectedText = current.text.substring(selection.start, selection.end)
            val wrappedText = "$input$selectedText$closingChar"

            val newText = current.text.substring(0, selection.start) +
                    wrappedText +
                    current.text.substring(selection.end)

            // 光标定位到包裹后的文本末尾
            val newCursorPosition = selection.start + wrappedText.length

            return TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPosition)
            )
        }

        // 无选中文本，插入配对字符
        val cursor = selection.start

        // 检查是否是对称字符（如引号），且光标右侧已有该字符
        if (input == closingChar && cursor < current.text.length &&
            current.text[cursor] == closingChar) {
            // 直接跳过，不重复插入
            return TextFieldValue(
                text = current.text,
                selection = TextRange(cursor + 1)
            )
        }

        val newText = current.text.substring(0, cursor) +
                "$input$closingChar" +
                current.text.substring(cursor)

        // 光标定位到两个字符中间
        return TextFieldValue(
            text = newText,
            selection = TextRange(cursor + 1)
        )
    }

    /**
     * 处理退格键，智能删除配对字符
     * @param current 当前TextFieldValue
     * @return 处理后的TextFieldValue，如果不需要处理返回null
     */
    fun handleBackspace(current: TextFieldValue): TextFieldValue? {
        val selection = current.selection
        if (selection.start != selection.end) return null // 有选中文本，正常删除

        val cursor = selection.start
        if (cursor == 0) return null // 已在开头

        val prevChar = current.text[cursor - 1]
        val closingChar = autoPairMap[prevChar] ?: return null

        // 检查光标后是否是配对的闭合字符
        if (cursor < current.text.length && current.text[cursor] == closingChar) {
            // 同时删除开闭字符
            val newText = current.text.substring(0, cursor - 1) +
                    current.text.substring(cursor + 1)

            return TextFieldValue(
                text = newText,
                selection = TextRange(cursor - 1)
            )
        }

        return null
    }

    /**
     * 处理换行，实现智能缩进
     * @param current 当前TextFieldValue
     * @return 处理后的TextFieldValue
     */
    fun handleNewLine(current: TextFieldValue): TextFieldValue {
        val cursor = current.selection.start
        val text = current.text

        // 找到当前行的起始位置
        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val currentLine = text.substring(lineStart, cursor)

        // 计算当前行的缩进
        val indent = currentLine.takeWhile { it.isWhitespace() }

        // 检查是否在列表项中
        val listPrefix = when {
            currentLine.trimStart().startsWith("- ") -> "- "
            currentLine.trimStart().startsWith("* ") -> "* "
            currentLine.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                val num = currentLine.trimStart().takeWhile { it.isDigit() }.toIntOrNull() ?: 1
                "${num + 1}. "
            }
            currentLine.trimStart().startsWith("- [ ] ") -> "- [ ] "
            currentLine.trimStart().startsWith("- [x] ") -> "- [ ] "
            else -> null
        }

        val newLineContent = if (listPrefix != null) {
            "\n$indent$listPrefix"
        } else {
            "\n$indent"
        }

        val newText = text.take(cursor) +
                newLineContent +
                text.substring(cursor)

        return TextFieldValue(
            text = newText,
            selection = TextRange(cursor + newLineContent.length)
        )
    }

    /**
     * 处理Tab键，插入4个空格或增加缩进
     * @param current 当前TextFieldValue
     * @param shift 是否按下Shift（Shift+Tab减少缩进）
     * @return 处理后的TextFieldValue
     */
    fun handleTab(current: TextFieldValue, shift: Boolean = false): TextFieldValue {
        val selection = current.selection

        if (shift) {
            // Shift+Tab：减少缩进
            return decreaseIndent(current)
        }

        // 普通Tab：增加缩进
        if (selection.start != selection.end) {
            // 有选中文本：对选中行增加缩进
            return increaseIndent(current)
        }

        // 无选中文本：插入4个空格
        val cursor = selection.start
        val spaces = "    "
        val newText = current.text.substring(0, cursor) +
                spaces +
                current.text.substring(cursor)

        return TextFieldValue(
            text = newText,
            selection = TextRange(cursor + spaces.length)
        )
    }

    /**
     * 增加选中行的缩进
     */
    private fun increaseIndent(current: TextFieldValue): TextFieldValue {
        val selection = current.selection
        val text = current.text

        // 找到选中区域的行范围
        val startLineBegin = text.lastIndexOf('\n', selection.start - 1) + 1
        val endLineEnd = text.indexOf('\n', selection.end).let {
            if (it == -1) text.length else it
        }

        val selectedLines = text.substring(startLineBegin, endLineEnd)
        val indentedLines = selectedLines.split('\n').joinToString("\n") { "    $it" }

        val newText = text.take(startLineBegin) +
                indentedLines +
                text.substring(endLineEnd)

        return TextFieldValue(
            text = newText,
            selection = TextRange(
                selection.start + 4,
                selection.end + (indentedLines.length - selectedLines.length)
            )
        )
    }

    /**
     * 减少选中行的缩进
     */
    private fun decreaseIndent(current: TextFieldValue): TextFieldValue {
        val selection = current.selection
        val text = current.text

        // 找到选中区域的行范围
        val startLineBegin = text.lastIndexOf('\n', selection.start - 1) + 1
        val endLineEnd = text.indexOf('\n', selection.end).let {
            if (it == -1) text.length else it
        }

        val selectedLines = text.substring(startLineBegin, endLineEnd)
        val dedentedLines = selectedLines.split('\n').joinToString("\n") { line ->
            when {
                line.startsWith("    ") -> line.substring(4)
                line.startsWith("\t") -> line.substring(1)
                else -> line
            }
        }

        val newText = text.take(startLineBegin) +
                dedentedLines +
                text.substring(endLineEnd)

        val lengthDiff = selectedLines.length - dedentedLines.length

        return TextFieldValue(
            text = newText,
            selection = TextRange(
                (selection.start - 4).coerceAtLeast(startLineBegin),
                selection.end - lengthDiff
            )
        )
    }
}