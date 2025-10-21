package com.zj.ink.md

import org.junit.Assert.*
import org.junit.Test

/**
 * Markdown兼容性测试套件
 * 确保新功能与现有功能兼容，测试新旧Markdown语法混合使用，验证向后兼容性
 */
class MarkdownCompatibilityTest {

    private val parser = MarkdownParser()

    // ================================
    // 1. 新旧Markdown语法混合测试
    // ================================

    @Test
    fun testMixedSyntaxTaskListWithHeadings() {
        val markdown = """
            # 项目计划

            ## 任务清单
            - [x] 完成设计文档
            - [ ] 实现核心功能

            ### 详细说明
            这是一个混合了标题和任务列表的文档。
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证解析结果包含所有元素
        assertNotNull(elements)
        assertTrue(elements.size >= 5)

        // 验证标题解析
        val heading1 = elements.find { it is MarkdownElement.Heading && it.level == 1 }
        assertNotNull(heading1)
        assertEquals("项目计划", (heading1 as MarkdownElement.Heading).text)

        // 验证任务列表解析
        val taskList = elements.find { it is MarkdownElement.TaskList }
        assertNotNull(taskList)
        val tasks = (taskList as MarkdownElement.TaskList).items
        assertEquals(2, tasks.size)
        assertTrue(tasks[0].second) // 第一个任务已完成
        assertFalse(tasks[1].second) // 第二个任务未完成
    }

    @Test
    fun testMixedSyntaxTableWithCodeBlock() {
        val markdown = """
            ## 代码示例表格

            | 语言 | 示例 | 说明 |
            |:-----|:----:|-----:|
            | Kotlin | `fun test()` | 函数定义 |
            | Java | `public void test()` | 方法定义 |

            ```kotlin
            fun example() {
                println("Hello World")
            }
            ```
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证表格解析
        val table = elements.find { it is MarkdownElement.Table }
        assertNotNull(table)
        val tableElement = table as MarkdownElement.Table
        assertEquals(3, tableElement.headers.size)
        assertEquals(2, tableElement.rows.size)

        // 验证表格对齐
        assertEquals(3, tableElement.alignments.size)
        assertEquals(MarkdownElement.TableAlignment.LEFT, tableElement.alignments[0])
        assertEquals(MarkdownElement.TableAlignment.CENTER, tableElement.alignments[1])
        assertEquals(MarkdownElement.TableAlignment.RIGHT, tableElement.alignments[2])

        // 验证代码块解析
        val codeBlock = elements.find { it is MarkdownElement.CodeBlock }
        assertNotNull(codeBlock)
        assertEquals("kotlin", (codeBlock as MarkdownElement.CodeBlock).language)
    }

    @Test
    fun testMixedSyntaxNestedListsWithQuotes() {
        val markdown = """
            # 文档结构

            - 第一级列表
                - 第二级列表
                    1. 有序子项1
                    2. 有序子项2
                - [ ] 任务项目
                - [x] 完成的任务

            > 这是一级引用
            >> 这是二级引用
            >>> 包含**加粗**和*斜体*的三级引用
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证嵌套列表
        val unorderedList = elements.find { it is MarkdownElement.UnorderedList }
        assertNotNull(unorderedList)

        // 验证多级引用
        val quotes = elements.filter { it is MarkdownElement.BlockQuote }
        assertTrue(quotes.size >= 3)

        // 验证引用层级
        val quote1 = quotes.find { (it as MarkdownElement.BlockQuote).level == 1 }
        val quote2 = quotes.find { (it as MarkdownElement.BlockQuote).level == 2 }
        val quote3 = quotes.find { (it as MarkdownElement.BlockQuote).level == 3 }

        assertNotNull(quote1)
        assertNotNull(quote2)
        assertNotNull(quote3)
    }

    @Test
    fun testMixedSyntaxHighlightWithExtensions() {
        val markdown = """
            ## 扩展语法示例

            这是==高亮文本==和普通文本的混合。

            数学公式：$E=mc^2$和化学公式：H~2~O和X^2^。

            脚注引用[^1]和另一个脚注[^note]。

            [^1]: 这是第一个脚注
            [^note]: 这是命名脚注
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证高亮文本
        val highlight = elements.find { it is MarkdownElement.Highlight }
        assertNotNull(highlight)
        assertEquals("高亮文本", (highlight as MarkdownElement.Highlight).text)

        // 验证数学公式
        val math = elements.find { it is MarkdownElement.Math }
        assertNotNull(math)

        // 验证上下标
        val superscript = elements.find { it is MarkdownElement.Superscript }
        val subscript = elements.find { it is MarkdownElement.Subscript }
        assertNotNull(superscript)
        assertNotNull(subscript)

        // 验证脚注
        val footnotes = elements.filter { it is MarkdownElement.Footnote }
        assertTrue(footnotes.size >= 2)
    }

    // ================================
    // 2. 向后兼容性验证
    // ================================

    @Test
    fun testBackwardCompatibilityBasicElements() {
        val markdown = """
            # 标题1
            ## 标题2
            ### 标题3

            这是普通段落文本。

            **加粗文本**和*斜体文本*以及~~删除线~~。

            - 无序列表项1
            - 无序列表项2

            1. 有序列表项1
            2. 有序列表项2

            [链接文本](https://example.com)

            ![图片](image.jpg)

            `内联代码`

            ```
            代码块
            ```

            > 引用文本

            | 表头1 | 表头2 |
            |-------|-------|
            | 数据1 | 数据2 |
        """.trimIndent()

        val elements = parser.parse(markdown)
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 验证基础元素都能正确解析
        assertTrue(elements.any { it is MarkdownElement.Heading })
        assertTrue(elements.any { it is MarkdownElement.Paragraph })
        assertTrue(elements.any { it is MarkdownElement.Bold })
        assertTrue(elements.any { it is MarkdownElement.Italic })
        assertTrue(elements.any { it is MarkdownElement.Strikethrough })
        assertTrue(elements.any { it is MarkdownElement.UnorderedList })
        assertTrue(elements.any { it is MarkdownElement.OrderedList })
        assertTrue(elements.any { it is MarkdownElement.Link })
        assertTrue(elements.any { it is MarkdownElement.Image })
        assertTrue(elements.any { it is MarkdownElement.InlineCode })
        assertTrue(elements.any { it is MarkdownElement.CodeBlock })
        assertTrue(elements.any { it is MarkdownElement.BlockQuote })
        assertTrue(elements.any { it is MarkdownElement.Table })
    }

    @Test
    fun testBackwardCompatibilityDataStructure() {
        // 测试扩展后的数据结构仍然支持原有字段
        val heading = MarkdownElement.Heading(text = "测试", level = 1)
        assertEquals("测试", heading.text)
        assertEquals(1, heading.level)

        val table = MarkdownElement.Table(
            headers = listOf("头1", "头2"),
            rows = listOf(listOf("数据1", "数据2"))
        )
        assertEquals(2, table.headers.size)
        assertEquals(1, table.rows.size)
        // 新增的alignments字段应该有默认值
        assertNotNull(table.alignments)

        val codeBlock = MarkdownElement.CodeBlock(code = "test code")
        assertEquals("test code", codeBlock.code)
        // 新增的language字段应该有默认值
        assertNotNull(codeBlock.language)
    }

    @Test
    fun testBackwardCompatibilityEmptyInput() {
        // 测试空输入的处理
        val emptyResult = parser.parse("")
        assertNotNull(emptyResult)
        assertTrue(emptyResult.isEmpty())

        // 测试只有空白字符的输入
        val whitespaceResult = parser.parse("   \n\n   \t  ")
        assertNotNull(whitespaceResult)
        // 可能为空或只包含空段落
    }

    @Test
    fun testBackwardCompatibilitySpecialCharacters() {
        // 测试特殊字符的处理保持一致
        val markdown = """
            # 特殊字符测试

            包含特殊字符：@#$%^&*()[]{}|;':",./<>?

            Unicode字符：中文测试 🎉 emoji测试

            转义字符：\# \* \[ \] \( \)
        """.trimIndent()

        val elements = parser.parse(markdown)
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 验证特殊字符和Unicode字符能正确处理
        val paragraphs = elements.filter { it is MarkdownElement.Paragraph }
        assertTrue(paragraphs.isNotEmpty())
    }

    // ================================
    // 3. 边界情况和异常处理测试
    // ================================

    @Test
    fun testBoundaryConditionsNestedSyntax() {
        // 测试嵌套语法的边界情况
        val markdown = """
            - [ ] 任务中包含==高亮==文本
            - [x] 任务中包含`代码`和**加粗**

            > 引用中包含- [ ] 任务列表
            > 引用中包含```代码块```

            | 表格 | 包含 |
            |------|------|
            | ==高亮== | `代码` |
            | **加粗** | *斜体* |
        """.trimIndent()

        val elements = parser.parse(markdown)
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 验证复杂嵌套能正确解析
        assertTrue(elements.any { it is MarkdownElement.TaskList })
        assertTrue(elements.any { it is MarkdownElement.BlockQuote })
        assertTrue(elements.any { it is MarkdownElement.Table })
    }

    @Test
    fun testBoundaryConditionsMalformedSyntax() {
        // 测试格式错误的语法处理
        val markdown = """
            # 不完整的语法测试

            - [ 缺少右括号的任务
            - [x 缺少右括号的完成任务
            - [] 空括号任务

            ```kotlin
            没有结束标记的代码块

            | 不完整 | 的表格
            |-----
            | 缺少 |

            > 不完整的
            >> 多级引用

            ==没有结束标记的高亮

            [^不完整的脚注
        """.trimIndent()

        val elements = parser.parse(markdown)
        assertNotNull(elements)
        // 应该能优雅处理格式错误，不抛出异常
    }

    @Test
    fun testBoundaryConditionsMaxNesting() {
        // 测试最大嵌套深度
        val deepNesting = StringBuilder()
        deepNesting.append("# 深度嵌套测试\n\n")

        // 创建深度嵌套的列表
        for (i in 0..10) {
            val indent = "    ".repeat(i)
            deepNesting.append("${indent}- 第${i + 1}级列表\n")
        }

        // 创建深度嵌套的引用
        for (i in 1..10) {
            val prefix = ">".repeat(i)
            deepNesting.append("$prefix 第${i}级引用\n")
        }

        val elements = parser.parse(deepNesting.toString())
        assertNotNull(elements)
        // 应该能处理深度嵌套而不崩溃
    }

    // ================================
    // 4. 性能兼容性测试
    // ================================

    @Test
    fun testPerformanceCompatibilitySmallDocument() {
        val markdown = """
            # 小文档性能测试

            这是一个包含基础语法的小文档：
            - **加粗**
            - *斜体*
            - `代码`
            - [链接](http://example.com)
        """.trimIndent()

        val startTime = System.currentTimeMillis()
        val elements = parser.parse(markdown)
        val endTime = System.currentTimeMillis()

        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 小文档解析应该很快（<50ms）
        val parseTime = endTime - startTime
        assertTrue("小文档解析时间过长: ${parseTime}ms", parseTime < 50)
    }

    @Test
    fun testPerformanceCompatibilityMediumDocument() {
        // 创建中等大小的文档
        val markdown = StringBuilder()
        markdown.append("# 中等文档性能测试\n\n")

        for (i in 1..50) {
            markdown.append("## 章节 $i\n\n")
            markdown.append("这是第 $i 个章节的内容。包含**加粗**、*斜体*和`代码`。\n\n")
            markdown.append("- 列表项 1\n")
            markdown.append("- 列表项 2\n")
            markdown.append("- [ ] 任务项 1\n")
            markdown.append("- [x] 任务项 2\n\n")

            if (i % 10 == 0) {
                markdown.append("```kotlin\n")
                markdown.append("fun example$i() {\n")
                markdown.append("    println(\"示例 $i\")\n")
                markdown.append("}\n")
                markdown.append("```\n\n")
            }
        }

        val startTime = System.currentTimeMillis()
        val elements = parser.parse(markdown.toString())
        val endTime = System.currentTimeMillis()

        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 中等文档解析应该在合理时间内（<200ms）
        val parseTime = endTime - startTime
        assertTrue("中等文档解析时间过长: ${parseTime}ms", parseTime < 200)
    }

    // ================================
    // 5. 渲染器兼容性测试
    // ================================

    @Test
    fun testRenderCompatibilityBasicElements() {
        val markdown = "# 测试标题\n\n这是段落文本。"
        val elements = parser.parse(markdown)

        // 验证解析结果可以被渲染器处理
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 检查元素类型是否完整
        for (element in elements) {
            when (element) {
                is MarkdownElement.Heading -> {
                    assertNotNull(element.text)
                    assertTrue(element.level in 1..6)
                }
                is MarkdownElement.Paragraph -> {
                    assertNotNull(element.text)
                }
                is MarkdownElement.TaskList -> {
                    assertNotNull(element.items)
                    assertTrue(element.level >= 0)
                }
                is MarkdownElement.Table -> {
                    assertNotNull(element.headers)
                    assertNotNull(element.rows)
                    assertNotNull(element.alignments)
                }
                is MarkdownElement.CodeBlock -> {
                    assertNotNull(element.code)
                    assertNotNull(element.language)
                }
                is MarkdownElement.BlockQuote -> {
                    assertNotNull(element.text)
                    assertTrue(element.level >= 1)
                }
                // 其他元素类型的验证...
            }
        }
    }

    @Test
    fun testRenderCompatibilityNewElements() {
        val markdown = """
            - [x] 完成的任务
            - [ ] 未完成的任务

            #### H4标题
            ##### H5标题
            ###### H6标题

            ==高亮文本==

            上标：X^2^ 下标：H~2~O

            数学公式：$E=mc^2$

            脚注引用[^1]

            [^1]: 脚注内容
        """.trimIndent()

        val elements = parser.parse(markdown)
        assertNotNull(elements)

        // 验证新元素的必需字段
        val taskList = elements.find { it is MarkdownElement.TaskList }
        if (taskList != null) {
            val tasks = (taskList as MarkdownElement.TaskList).items
            assertTrue(tasks.isNotEmpty())
            // 每个任务都应该有文本和状态
            tasks.forEach { task ->
                assertNotNull(task.first) // 任务文本
                // task.second 是布尔值，不需要null检查
            }
        }

        val highlight = elements.find { it is MarkdownElement.Highlight }
        if (highlight != null) {
            assertNotNull((highlight as MarkdownElement.Highlight).text)
            assertTrue(highlight.text.isNotEmpty())
        }
    }

    // ================================
    // 6. 错误恢复和容错性测试
    // ================================

    @Test
    fun testErrorRecoveryInvalidSyntax() {
        val markdown = """
            # 正常标题

            这是正常段落。

            - [ 这是格式错误的任务列表
            - [x] 这是正常的任务列表

            正常段落应该继续解析。

            ```kotlin
            这是没有结束标记的代码块

            # 后续内容应该正常解析
        """.trimIndent()

        val elements = parser.parse(markdown)
        assertNotNull(elements)

        // 即使有错误语法，也应该解析出一些正确的元素
        assertTrue(elements.any { it is MarkdownElement.Heading })
        assertTrue(elements.any { it is MarkdownElement.Paragraph })
    }

    @Test
    fun testErrorRecoveryLargeInput() {
        // 创建一个很大的输入来测试内存管理
        val largeMarkdown = StringBuilder()
        for (i in 1..1000) {
            largeMarkdown.append("# 标题 $i\n")
            largeMarkdown.append("段落内容 $i ".repeat(100))
            largeMarkdown.append("\n\n")
        }

        try {
            val elements = parser.parse(largeMarkdown.toString())
            assertNotNull(elements)
            // 大输入应该能正常处理，不抛出OutOfMemoryError
        } catch (e: OutOfMemoryError) {
            fail("大输入导致内存溢出")
        }
    }

    // ================================
    // 7. 数据完整性测试
    // ================================

    @Test
    fun testDataIntegrityAfterParsing() {
        val markdown = """
            # 数据完整性测试

            | 列1 | 列2 | 列3 |
            |:----|:---:|----:|
            | 左对齐 | 居中 | 右对齐 |
            | 数据1 | 数据2 | 数据3 |
        """.trimIndent()

        val elements = parser.parse(markdown)
        val table = elements.find { it is MarkdownElement.Table } as? MarkdownElement.Table

        assertNotNull(table)

        // 验证表格数据完整性
        assertEquals(3, table!!.headers.size)
        assertEquals(2, table.rows.size)
        assertEquals(3, table.alignments.size)

        // 验证对齐信息正确
        assertEquals(MarkdownElement.TableAlignment.LEFT, table.alignments[0])
        assertEquals(MarkdownElement.TableAlignment.CENTER, table.alignments[1])
        assertEquals(MarkdownElement.TableAlignment.RIGHT, table.alignments[2])

        // 验证数据内容完整
        assertEquals("列1", table.headers[0])
        assertEquals("列2", table.headers[1])
        assertEquals("列3", table.headers[2])

        assertEquals("左对齐", table.rows[0][0])
        assertEquals("居中", table.rows[0][1])
        assertEquals("右对齐", table.rows[0][2])
    }

    // ================================
    // 8. 综合兼容性验证
    // ================================

    @Test
    fun testComprehensiveCompatibility() {
        val markdown = """
            # 综合兼容性测试文档

            ## 基础语法

            这是包含**加粗**、*斜体*、~~删除线~~和`内联代码`的段落。

            ### 列表混合

            #### 传统列表
            - 无序列表项1
            - 无序列表项2
                1. 嵌套有序列表
                2. 另一个有序项

            #### 新增任务列表
            - [x] 已完成的任务
            - [ ] 未完成的任务
                - [ ] 嵌套的未完成任务
                - [x] 嵌套的已完成任务

            ##### 表格功能

            | 功能 | 状态 | 说明 |
            |:-----|:----:|-----:|
            | 基础解析 | ✅ | 完全兼容 |
            | 新增功能 | ✅ | 正常工作 |
            | 性能 | ✅ | 无回归 |

            ###### 代码示例

            ```kotlin
            // 带语言标记的代码块
            fun example() {
                println("Hello World")
            }
            ```

            ```
            // 无语言标记的代码块
            普通代码内容
            ```

            ## 扩展语法

            ### 高亮和格式
            这段包含==高亮文本==和普通文本。

            ### 数学和化学
            数学公式：$E=mc^2$
            化学公式：H~2~O 和 CO~2~
            指数：2^10^ = 1024

            ### 引用层次
            > 一级引用
            >> 二级引用包含**加粗**
            >>> 三级引用包含==高亮==

            ### 脚注系统
            这里有一个脚注引用[^1]，还有另一个[^note]。

            [^1]: 第一个脚注的内容
            [^note]: 命名脚注的内容

            ## 转义字符测试

            转义的特殊字符：\* \# \[ \] \( \) \{ \} \_ \` \~ \\

            ## 总结

            这个文档测试了新旧语法的混合使用，确保完全向后兼容。
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证解析成功
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 验证包含所有类型的元素
        val elementTypes = elements.map { it::class.java.simpleName }.toSet()

        // 基础元素应该存在
        assertTrue("缺少Heading元素", elementTypes.contains("Heading"))
        assertTrue("缺少Paragraph元素", elementTypes.contains("Paragraph"))
        assertTrue("缺少UnorderedList元素", elementTypes.contains("UnorderedList"))
        assertTrue("缺少OrderedList元素", elementTypes.contains("OrderedList"))
        assertTrue("缺少Table元素", elementTypes.contains("Table"))
        assertTrue("缺少CodeBlock元素", elementTypes.contains("CodeBlock"))
        assertTrue("缺少BlockQuote元素", elementTypes.contains("BlockQuote"))

        // 新增元素应该存在
        assertTrue("缺少TaskList元素", elementTypes.contains("TaskList"))

        // 验证标题层级完整性（H1-H6）
        val headings = elements.filter { it is MarkdownElement.Heading }
            .map { it as MarkdownElement.Heading }

        val headingLevels = headings.map { it.level }.toSet()
        assertTrue("应该包含H1-H6所有层级", headingLevels.containsAll(listOf(1, 2, 3, 4, 5, 6)))

        // 验证表格对齐功能
        val table = elements.find { it is MarkdownElement.Table } as? MarkdownElement.Table
        if (table != null) {
            assertEquals(3, table.alignments.size)
            assertEquals(MarkdownElement.TableAlignment.LEFT, table.alignments[0])
            assertEquals(MarkdownElement.TableAlignment.CENTER, table.alignments[1])
            assertEquals(MarkdownElement.TableAlignment.RIGHT, table.alignments[2])
        }

        // 验证代码块语言支持
        val codeBlocks = elements.filter { it is MarkdownElement.CodeBlock }
            .map { it as MarkdownElement.CodeBlock }

        assertTrue("应该有带语言标记的代码块",
            codeBlocks.any { it.language == "kotlin" })
        assertTrue("应该有不带语言标记的代码块",
            codeBlocks.any { it.language.isEmpty() })
    }
}