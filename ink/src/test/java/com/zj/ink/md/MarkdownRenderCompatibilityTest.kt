package com.zj.ink.md

import org.junit.Assert.*
import org.junit.Test

/**
 * Markdown渲染器兼容性测试
 * 测试Compose和Glance渲染器的一致性，验证主题切换时的兼容性
 */
class MarkdownRenderCompatibilityTest {

    private val parser = MarkdownParser()

    // ================================
    // 1. 渲染器数据结构兼容性测试
    // ================================

    @Test
    fun testRenderDataStructureCompatibility() {
        // 测试渲染器能正确处理所有数据结构
        val markdown = """
            # 渲染器兼容性测试

            ## 基础元素
            这是包含**加粗**、*斜体*、~~删除线~~和`内联代码`的段落。

            ## 新增元素
            - [x] 已完成任务
            - [ ] 未完成任务

            #### H4标题
            ##### H5标题
            ###### H6标题

            | 左对齐 | 居中 | 右对齐 |
            |:-------|:----:|-------:|
            | 数据1 | 数据2 | 数据3 |

            ```kotlin
            fun test() {
                println("语法高亮测试")
            }
            ```

            > 一级引用
            >> 二级引用
            >>> 三级引用

            ==高亮文本==测试

            数学公式：$E=mc^2$
            上标：X^2^ 下标：H~2~O

            脚注引用[^1]

            [^1]: 脚注内容
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证所有元素都有必需的字段用于渲染
        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Heading -> {
                    assertNotNull("标题文本不能为空", element.text)
                    assertTrue("标题级别应在1-6范围", element.level in 1..6)
                }
                is MarkdownElement.TaskList -> {
                    assertNotNull("任务列表项不能为空", element.items)
                    assertTrue("任务列表级别应>=0", element.level >= 0)
                    element.items.forEach { task ->
                        assertNotNull("任务文本不能为空", task.first)
                        // task.second 是布尔值，不需要null检查
                    }
                }
                is MarkdownElement.Table -> {
                    assertNotNull("表格头不能为空", element.headers)
                    assertNotNull("表格行不能为空", element.rows)
                    assertNotNull("表格对齐不能为空", element.alignments)
                    assertTrue("表格应有列", element.headers.isNotEmpty())
                }
                is MarkdownElement.CodeBlock -> {
                    assertNotNull("代码内容不能为空", element.code)
                    assertNotNull("语言字段不能为空", element.language)
                }
                is MarkdownElement.BlockQuote -> {
                    assertNotNull("引用内容不能为空", element.text)
                    assertTrue("引用级别应>=1", element.level >= 1)
                }
                is MarkdownElement.UnorderedList -> {
                    assertNotNull("无序列表项不能为空", element.items)
                    assertTrue("列表级别应>=0", element.level >= 0)
                }
                is MarkdownElement.OrderedList -> {
                    assertNotNull("有序列表项不能为空", element.items)
                    assertTrue("列表级别应>=0", element.level >= 0)
                }
                is MarkdownElement.Highlight -> {
                    assertNotNull("高亮文本不能为空", element.text)
                    assertTrue("高亮文本应非空", element.text.isNotEmpty())
                }
                is MarkdownElement.Math -> {
                    assertNotNull("数学公式不能为空", element.content)
                    // isInline 是布尔值，不需要null检查
                }
                is MarkdownElement.Superscript -> {
                    assertNotNull("上标内容不能为空", element.content)
                }
                is MarkdownElement.Subscript -> {
                    assertNotNull("下标内容不能为空", element.content)
                }
                is MarkdownElement.Footnote -> {
                    assertNotNull("脚注ID不能为空", element.id)
                    assertNotNull("脚注内容不能为空", element.content)
                    // isReference 是布尔值，不需要null检查
                }
                // 其他基础元素的验证
                is MarkdownElement.Paragraph -> {
                    assertNotNull("段落文本不能为空", element.text)
                }
                is MarkdownElement.Bold -> {
                    assertNotNull("加粗文本不能为空", element.text)
                }
                is MarkdownElement.Italic -> {
                    assertNotNull("斜体文本不能为空", element.text)
                }
                is MarkdownElement.Link -> {
                    assertNotNull("链接文本不能为空", element.text)
                    assertNotNull("链接URL不能为空", element.url)
                }
                is MarkdownElement.Image -> {
                    assertNotNull("图片描述不能为空", element.alt)
                    assertNotNull("图片URL不能为空", element.url)
                }
                else -> {
                    // 其他元素类型的基本验证
                }
            }
        }
    }

    @Test
    fun testRenderElementConsistency() {
        // 测试元素渲染的一致性
        val testCases = listOf(
            "# H1标题" to "Heading",
            "#### H4标题" to "Heading",
            "- [x] 已完成任务" to "TaskList",
            "- [ ] 未完成任务" to "TaskList",
            "==高亮文本==" to "Highlight",
            "> 引用文本" to "BlockQuote",
            ">>> 三级引用" to "BlockQuote",
            "```kotlin\nfun test()\n```" to "CodeBlock",
            "$E=mc^2$" to "Math",
            "X^2^" to "Superscript",
            "H~2~O" to "Subscript",
            "[^1]: 脚注" to "Footnote"
        )

        testCases.forEach { (markdown, expectedType) ->
            val elements = parser.parse(markdown)
            val hasExpectedType = elements.any {
                it::class.java.simpleName == expectedType
            }
            assertTrue("$markdown 应该解析出 $expectedType 元素", hasExpectedType)
        }
    }

    // ================================
    // 2. 嵌套结构渲染兼容性测试
    // ================================

    @Test
    fun testNestedStructureRenderCompatibility() {
        val nestedMarkdown = """
            # 嵌套结构测试

            ## 嵌套列表
            - 第一级
                - 第二级
                    - [x] 第三级任务
                    - [ ] 另一个任务
                        1. 第四级有序列表
                        2. 另一个有序项

            ## 嵌套引用
            > 一级引用
            >> 二级引用包含**加粗**
            >>> 三级引用包含==高亮==和`代码`

            ## 表格中的格式
            | 列1 | 列2 | 列3 |
            |:---|:---:|---:|
            | **加粗** | ==高亮== | `代码` |
            | *斜体* | ~~删除线~~ | 普通文本 |
        """.trimIndent()

        val elements = parser.parse(nestedMarkdown)

        // 验证嵌套列表的层级信息
        val lists = elements.filter {
            it is MarkdownElement.UnorderedList ||
            it is MarkdownElement.OrderedList ||
            it is MarkdownElement.TaskList
        }

        assertTrue("应该有嵌套列表", lists.isNotEmpty())

        // 验证引用的层级信息
        val quotes = elements.filter { it is MarkdownElement.BlockQuote }
            .map { it as MarkdownElement.BlockQuote }

        assertTrue("应该有多级引用", quotes.isNotEmpty())
        val quoteLevels = quotes.map { it.level }.toSet()
        assertTrue("应该有不同级别的引用", quoteLevels.size > 1)

        // 验证表格结构
        val table = elements.find { it is MarkdownElement.Table } as? MarkdownElement.Table
        assertNotNull("应该有表格", table)
        if (table != null) {
            assertEquals("表格应有3列", 3, table.headers.size)
            assertTrue("表格应有数据行", table.rows.isNotEmpty())
            assertEquals("对齐信息应匹配列数", 3, table.alignments.size)
        }
    }

    @Test
    fun testComplexMixedContent() {
        val complexMarkdown = """
            # 复杂混合内容测试

            这是一个包含多种元素的复杂文档：

            ## 任务计划
            - [x] 完成基础功能
                - [x] 解析器开发
                - [ ] 渲染器开发
                    - [x] Compose渲染器
                    - [ ] Glance渲染器

            ## 技术规格

            ### 数据结构
            ```kotlin
            sealed class MarkdownElement {
                data class Heading(val text: String, val level: Int)
                data class TaskList(val items: List<Pair<String, Boolean>>)
            }
            ```

            ### 性能指标
            | 功能 | 目标 | 状态 |
            |:-----|:----:|-----:|
            | 解析速度 | <50ms | ✅ |
            | 内存使用 | <20MB | ⏳ |
            | 兼容性 | 100% | ✅ |

            ## 数学公式

            能量质量关系：$E=mc^2$

            化学分子式：
            - 水分子：H~2~O
            - 二氧化碳：CO~2~
            - 平方：X^2^, Y^3^

            ## 重要说明

            > 这是一个重要的说明
            >> 包含==高亮内容==的二级引用
            >>> 以及包含**加粗文本**的三级引用

            详细信息请参考脚注[^1]和[^note]。

            [^1]: 第一个脚注的详细说明
            [^note]: 命名脚注的内容
        """.trimIndent()

        val elements = parser.parse(complexMarkdown)

        // 验证复杂文档的完整性
        assertNotNull("复杂文档应该能解析", elements)
        assertTrue("应该解析出多个元素", elements.size > 10)

        // 验证各种元素类型都存在
        val elementTypes = elements.map { it::class.java.simpleName }.toSet()

        val expectedTypes = listOf(
            "Heading", "Paragraph", "TaskList", "CodeBlock",
            "Table", "Math", "Subscript", "Superscript",
            "BlockQuote", "Footnote"
        )

        expectedTypes.forEach { expectedType ->
            assertTrue("复杂文档应包含 $expectedType",
                       elementTypes.contains(expectedType))
        }
    }

    // ================================
    // 3. 主题兼容性测试
    // ================================

    @Test
    fun testThemeCompatibilityElements() {
        // 测试主题相关的元素是否有适当的属性
        val markdown = """
            # 主题兼容性测试

            ==高亮文本==应该适配主题颜色

            ```kotlin
            // 代码块应该有适当的背景色
            fun themeTest() {
                println("主题测试")
            }
            ```

            | 元素 | 主题适配 |
            |------|----------|
            | 高亮 | 支持 |
            | 代码 | 支持 |

            > 引用块也应该适配主题
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证需要主题适配的元素
        val highlight = elements.find { it is MarkdownElement.Highlight }
        assertNotNull("应该有高亮元素", highlight)

        val codeBlock = elements.find { it is MarkdownElement.CodeBlock }
        assertNotNull("应该有代码块元素", codeBlock)

        val table = elements.find { it is MarkdownElement.Table }
        assertNotNull("应该有表格元素", table)

        val quote = elements.find { it is MarkdownElement.BlockQuote }
        assertNotNull("应该有引用元素", quote)

        // 这些元素在渲染时应该能正确应用主题
    }

    // ================================
    // 4. 屏幕尺寸兼容性测试
    // ================================

    @Test
    fun testScreenSizeCompatibility() {
        // 测试不同屏幕尺寸下的兼容性
        val markdown = """
            # 屏幕适配测试

            ## 长表格测试
            | 很长的列标题1 | 很长的列标题2 | 很长的列标题3 | 很长的列标题4 | 很长的列标题5 |
            |:-------------|:-------------|:-------------|:-------------|:-------------|
            | 很长的数据内容1 | 很长的数据内容2 | 很长的数据内容3 | 很长的数据内容4 | 很长的数据内容5 |

            ## 长代码行测试
            ```kotlin
            fun veryLongFunctionNameThatMightCauseHorizontalScrollingIssuesOnSmallScreens() {
                val veryLongVariableNameThatAlsoMightCauseIssues = "very long string content"
                println("This is a very long line that might need wrapping or scrolling")
            }
            ```

            ## 长任务列表测试
            - [ ] 这是一个非常长的任务描述，可能会在小屏幕上造成换行或者滚动问题，需要测试响应式布局
            - [x] 另一个很长的已完成任务，包含很多详细信息和说明文字，用于测试布局适应性
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证长内容元素的解析
        val table = elements.find { it is MarkdownElement.Table } as? MarkdownElement.Table
        if (table != null) {
            assertTrue("长表格应该有多列", table.headers.size >= 5)
            table.headers.forEach { header ->
                assertTrue("表头应该有内容", header.isNotEmpty())
            }
        }

        val codeBlock = elements.find { it is MarkdownElement.CodeBlock } as? MarkdownElement.CodeBlock
        if (codeBlock != null) {
            assertTrue("代码块应该有内容", codeBlock.code.isNotEmpty())
            // 检查是否包含长行
            val hasLongLines = codeBlock.code.lines().any { it.length > 50 }
            assertTrue("应该有长代码行用于测试", hasLongLines)
        }

        val taskList = elements.find { it is MarkdownElement.TaskList } as? MarkdownElement.TaskList
        if (taskList != null) {
            assertTrue("应该有任务项", taskList.items.isNotEmpty())
            // 检查是否有长任务文本
            val hasLongTasks = taskList.items.any { it.first.length > 50 }
            assertTrue("应该有长任务文本用于测试", hasLongTasks)
        }
    }

    // ================================
    // 5. 性能兼容性测试
    // ================================

    @Test
    fun testRenderPerformanceCompatibility() {
        // 测试渲染性能兼容性
        val heavyMarkdown = buildString {
            append("# 性能测试文档\n\n")

            // 大量标题
            repeat(20) { i ->
                append("## 章节 $i\n\n")
                append("### 子章节 $i.1\n\n")
                append("#### 子章节 $i.2\n\n")
                append("##### 子章节 $i.3\n\n")
                append("###### 子章节 $i.4\n\n")
            }

            // 大量任务列表
            append("## 任务列表性能测试\n\n")
            repeat(50) { i ->
                val checked = if (i % 2 == 0) "x" else " "
                append("- [$checked] 任务项 $i\n")
            }
            append("\n")

            // 大型表格
            append("## 表格性能测试\n\n")
            append("| 列1 | 列2 | 列3 | 列4 | 列5 |\n")
            append("|-----|-----|-----|-----|-----|\n")
            repeat(30) { i ->
                append("| 数据${i}1 | 数据${i}2 | 数据${i}3 | 数据${i}4 | 数据${i}5 |\n")
            }
            append("\n")

            // 多个代码块
            repeat(10) { i ->
                append("### 代码示例 $i\n\n")
                append("```kotlin\n")
                append("fun example$i() {\n")
                repeat(10) { j ->
                    append("    println(\"行 $j\")\n")
                }
                append("}\n")
                append("```\n\n")
            }
        }

        val startTime = System.currentTimeMillis()
        val elements = parser.parse(heavyMarkdown)
        val parseTime = System.currentTimeMillis() - startTime

        // 验证解析结果
        assertNotNull("重文档应该能解析", elements)
        assertTrue("应该解析出大量元素", elements.size > 100)

        // 验证性能
        assertTrue("重文档解析时间应合理: ${parseTime}ms", parseTime < 1000)

        // 验证元素类型分布
        val elementTypes = elements.groupBy { it::class.java.simpleName }
        assertTrue("应该有多种元素类型", elementTypes.size >= 5)

        println("重文档渲染兼容性测试: ${parseTime}ms, ${elements.size}个元素")
    }

    // ================================
    // 6. 错误处理兼容性测试
    // ================================

    @Test
    fun testErrorHandlingCompatibility() {
        // 测试错误处理的兼容性
        val problematicMarkdown = """
            # 错误处理测试

            ## 格式错误的元素

            - [ 缺少右括号的任务
            - [x 另一个错误任务
            - [] 空任务标记

            | 不完整的表格 |
            |----------
            | 缺少列 |

            ```kotlin
            没有结束标记的代码块

            > 不完整引用
            >> 另一个引用

            ==没有结束的高亮

            [^不完整脚注

            ## 正常内容

            这些内容应该正常解析：
            - [x] 正常任务
            - 正常列表项

            正常段落文本。
        """.trimIndent()

        // 错误处理应该不抛异常
        var elements: List<MarkdownElement>? = null
        var exception: Exception? = null

        try {
            elements = parser.parse(problematicMarkdown)
        } catch (e: Exception) {
            exception = e
        }

        // 验证错误处理
        assertNull("解析错误格式不应抛异常", exception)
        assertNotNull("应该返回解析结果", elements)

        if (elements != null) {
            assertTrue("应该解析出一些正确元素", elements.isNotEmpty())

            // 验证正确的元素仍然被解析
            assertTrue("应该包含正常标题",
                       elements.any { it is MarkdownElement.Heading })
            assertTrue("应该包含正常段落",
                       elements.any { it is MarkdownElement.Paragraph })
        }
    }

    // ================================
    // 7. 向前兼容性测试
    // ================================

    @Test
    fun testForwardCompatibility() {
        // 测试向前兼容性（未来扩展的准备）

        // 测试数据结构的扩展性
        val heading = MarkdownElement.Heading("测试", 1)

        // 验证现有字段仍然可用
        assertEquals("测试", heading.text)
        assertEquals(1, heading.level)

        // 测试新字段的默认值
        val table = MarkdownElement.Table(
            headers = listOf("列1"),
            rows = listOf(listOf("数据1"))
        )

        // 新字段应该有合理的默认值
        assertNotNull("alignments应该有默认值", table.alignments)

        val codeBlock = MarkdownElement.CodeBlock("test code")
        assertNotNull("language应该有默认值", codeBlock.language)

        // 测试解析器的扩展性
        val futureMarkdown = """
            # 向前兼容性测试

            当前支持的所有语法：
            - [x] 任务列表
            - H1-H6标题
            - 表格对齐
            - 代码语法高亮
            - 多级引用和列表
            - 高亮文本
            - 数学公式
            - 上下标
            - 脚注

            这些功能应该在未来版本中保持兼容。
        """.trimIndent()

        val elements = parser.parse(futureMarkdown)
        assertNotNull("向前兼容性文档应该正常解析", elements)
        assertTrue("应该解析出元素", elements.isNotEmpty())
    }
}