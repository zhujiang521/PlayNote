package com.zj.ink.md

/**
 * 任务列表状态切换工具类
 *
 * 提供任务列表项的精确定位和状态切换功能，通过直接修改 Markdown content 实现。
 *
 * 核心功能：
 * - 精确定位：基于任务索引 + 文本内容 + 缩进层级进行匹配
 * - 状态切换：在原始 content 中替换 `- [ ]` ↔ `- [x]`
 * - 边界保护：避免误修改代码块中的任务标记
 *
 * @author PlayNote开发团队
 * @since 1.1.0
 */
object TaskListHelper {

    /**
     * 切换任务列表项的完成状态
     *
     * @param content 原始 Markdown 文本内容
     * @param taskIndex 任务在所有 TaskList 元素中的索引（从0开始）
     * @param taskText 任务的文本内容（用于二次验证）
     * @param currentChecked 当前的选中状态
     * @return 修改后的 Markdown 文本内容
     */
    fun toggleTaskState(
        content: String,
        taskIndex: Int,
        taskText: String,
        currentChecked: Boolean
    ): String {
        // 输入验证
        if (content.isBlank()) return content
        if (taskIndex < 0) return content

        // 解析 content 获取所有元素
        val elements = try {
            MarkdownParser.parse(content)
        } catch (e: Exception) {
            println("TaskListHelper: Parse error - ${e.message}")
            return content
        }

        // 提取所有 TaskList 元素
        val taskLists = elements.filterIsInstance<TaskList>()

        // 验证索引有效性
        if (taskIndex >= taskLists.size) {
            println("TaskListHelper: Invalid taskIndex=$taskIndex, total tasks=${taskLists.size}")
            return content
        }

        // 获取目标任务
        val targetTask = taskLists[taskIndex]

        // 二次验证：文本内容和状态是否匹配
        if (targetTask.text != taskText || targetTask.isChecked != currentChecked) {
            println("TaskListHelper: Task mismatch - expected text='$taskText' checked=$currentChecked, " +
                    "actual text='${targetTask.text}' checked=${targetTask.isChecked}")
            return content
        }

        // 在原始 content 中定位并替换
        return replaceTaskInContent(content, taskIndex, targetTask, !currentChecked)
    }

    /**
     * 在原始 content 中定位并替换指定任务的状态
     *
     * 策略：
     * 1. 按行遍历 content
     * 2. 识别任务列表行（匹配正则 `^\\s*- \\[([ x])\\] .*$`）
     * 3. 计数到第 taskIndex 个任务时进行替换
     * 4. 保留原始缩进和格式
     *
     * @param content 原始内容
     * @param taskIndex 目标任务索引
     * @param targetTask 目标任务对象
     * @param newChecked 新的选中状态
     * @return 修改后的内容
     */
    private fun replaceTaskInContent(
        content: String,
        taskIndex: Int,
        targetTask: TaskList,
        newChecked: Boolean
    ): String {
        val lines = content.lines().toMutableList()
        var currentTaskIndex = 0
        var modified = false

        for (i in lines.indices) {
            val line = lines[i]

            // 匹配任务列表行：支持缩进
            val taskMatch = Regex("""^(\s*)- \[([ x])\] (.*)$""").find(line)

            if (taskMatch != null) {
                val (indent, checkMark, text) = taskMatch.destructured

                // 判断是否为目标任务
                if (currentTaskIndex == taskIndex) {
                    // 验证文本内容和层级
                    val actualLevel = indent.length / 2 + 1 // 每2个空格为1级
                    val actualText = text.trimStart()

                    if (actualText == targetTask.text && actualLevel == targetTask.level) {
                        // 替换状态标记
                        val newCheckMark = if (newChecked) "x" else " "
                        lines[i] = "$indent- [$newCheckMark] $text"
                        modified = true
                        break
                    }
                }

                currentTaskIndex++
            }
        }

        if (!modified) {
            println("TaskListHelper: Failed to locate task at index=$taskIndex in content")
        }

        return lines.joinToString("\n")
    }

    /**
     * 获取内容中所有任务列表项的信息（用于调试）
     *
     * @param content Markdown 文本内容
     * @return 任务列表信息的列表
     */
    fun getTaskListInfo(content: String): List<TaskInfo> {
        if (content.isBlank()) return emptyList()

        val elements = try {
            MarkdownParser.parse(content)
        } catch (e: Exception) {
            return emptyList()
        }

        return elements.filterIsInstance<TaskList>().mapIndexed { index, task ->
            TaskInfo(
                index = index,
                text = task.text,
                isChecked = task.isChecked,
                level = task.level
            )
        }
    }

    /**
     * 任务列表项信息数据类
     */
    data class TaskInfo(
        val index: Int,
        val text: String,
        val isChecked: Boolean,
        val level: Int
    )
}
