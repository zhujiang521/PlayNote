package com.zj.ink.md

import org.junit.Test
import org.junit.Assert.*

/**
 * MarkdownParser单元测试类
 *
 * 测试覆盖所有新增的Markdown解析功能，包括：
 * - 任务列表解析测试
 * - H4-H6标题解析测试
 * - 表格列对齐解析测试
 * - 嵌套列表解析测试
 * - 代码块语法高亮解析测试
 * - 多级引用解析测试
 * - 高亮文本解析测试
 * - 转义字符解析测试
 * - 扩展语法解析测试（脚注、上下标、数学公式）
 */
class MarkdownParserTest {

    // ========== 任务列表解析测试 ==========

    @Test
    fun testTaskListBasicParsing() {
        val input = """
            - [ ] 未完成任务
            - [x] 已完成任务
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(2, result.size)

        val uncompletedTask = result[0] as TaskList
        assertEquals("未完成任务", uncompletedTask.text)
        assertFalse(uncompletedTask.isChecked)
        assertEquals(1, uncompletedTask.level)

        val completedTask = result[1] as TaskList
        assertEquals("已完成任务", completedTask.text)
        assertTrue(completedTask.isChecked)
        assertEquals(1, completedTask.level)
    }

    @Test
    fun testTaskListNestedParsing() {
        val input = """
            - [ ] 主任务
                - [x] 子任务1
                - [ ] 子任务2
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(3, result.size)

        val mainTask = result[0] as TaskList
        assertEquals("主任务", mainTask.text)
        assertFalse(mainTask.isChecked)
        assertEquals(1, mainTask.level)

        val subTask1 = result[1] as TaskList
        assertEquals("子任务1", subTask1.text)
        assertTrue(subTask1.isChecked)
        assertEquals(2, subTask1.level)

        val subTask2 = result[2] as TaskList
        assertEquals("子任务2", subTask2.text)
        assertFalse(subTask2.isChecked)
        assertEquals(2, subTask2.level)
    }

    @Test
    fun testTaskListWithSpecialCharacters() {
        val input = """
            - [ ] 任务包含特殊字符：*bold* and `code`
            - [x] 完成的任务 #标签
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(2, result.size)

        val task1 = result[0] as TaskList
        assertEquals("任务包含特殊字符：*bold* and `code`", task1.text)
        assertFalse(task1.isChecked)

        val task2 = result[1] as TaskList
        assertEquals("完成的任务 #标签", task2.text)
        assertTrue(task2.isChecked)
    }

    @Test
    fun testTaskListEmptyContent() {
        val input = """
            - [ ] 
            - [x] 
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(2, result.size)

        val task1 = result[0] as TaskList
        assertEquals("", task1.text)
        assertFalse(task1.isChecked)

        val task2 = result[1] as TaskList
        assertEquals("", task2.text)
        assertTrue(task2.isChecked)
    }

    @Test
    fun testTaskListInvalidFormat() {
        val input = """
            - [y] 无效格式
            - [] 缺少状态
            -[ ] 缺少空格
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        // 无效格式应该被解析为普通列表或段落
        assertFalse(result.any { it is TaskList })
    }

    @Test
    fun testTaskListMixedWithOtherLists() {
        val input = """
            - [ ] 任务项
            - 普通列表项
            1. 有序列表项
            - [x] 另一个任务项
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertTrue(result.any { it is TaskList })
        assertTrue(result.any { it is UnorderedList })
        assertTrue(result.any { it is OrderedList })
    }

    // ========== H4-H6标题解析测试 ==========

    @Test
    fun testH4H5H6HeadingParsing() {
        val input = """
            #### H4标题
            ##### H5标题
            ###### H6标题
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(3, result.size)

        val h4 = result[0] as Heading
        assertEquals(4, h4.level)
        assertEquals("H4标题", h4.text)

        val h5 = result[1] as Heading
        assertEquals(5, h5.level)
        assertEquals("H5标题", h5.text)

        val h6 = result[2] as Heading
        assertEquals(6, h6.level)
        assertEquals("H6标题", h6.text)
    }

    @Test
    fun testCompleteHeadingHierarchy() {
        val input = """
            # H1标题
            ## H2标题
            ### H3标题
            #### H4标题
            ##### H5标题
            ###### H6标题
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(6, result.size)

        for (i in 0 until 6) {
            val heading = result[i] as Heading
            assertEquals(i + 1, heading.level)
            assertEquals("H${i + 1}标题", heading.text)
        }
    }

    @Test
    fun testHeadingWithSpecialCharacters() {
        val input = """
            #### 标题包含特殊字符：*bold* and `code`
            ##### 标题 #标签
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(2, result.size)

        val h4 = result[0] as Heading
        assertEquals(4, h4.level)
        assertEquals("标题包含特殊字符：*bold* and `code`", h4.text)

        val h5 = result[1] as Heading
        assertEquals(5, h5.level)
        assertEquals("标题 #标签", h5.text)
    }

    @Test
    fun testHeadingInvalidFormat() {
        val input = """
            ####### 七级标题（无效）
            ####标题缺少空格
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        // 无效格式应该被解析为段落
        assertFalse(result.any { it is Heading && (it as Heading).level > 6 })
    }

    // ========== 表格列对齐解析测试 ==========

    @Test
    fun testTableColumnAlignmentParsing() {
        val input = """
            | 左对齐 | 居中对齐 | 右对齐 |
            | :--- | :---: | ---: |
            | 内容1 | 内容2 | 内容3 |
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val table = result[0] as Table
        assertEquals(3, table.alignments.size)
        assertEquals(TableAlignment.LEFT, table.alignments[0])
        assertEquals(TableAlignment.CENTER, table.alignments[1])
        assertEquals(TableAlignment.RIGHT, table.alignments[2])

        assertEquals(listOf("左对齐", "居中对齐", "右对齐"), table.headers)
        assertEquals(1, table.rows.size)
        assertEquals(listOf("内容1", "内容2", "内容3"), table.rows[0])
    }

    @Test
    fun testTableWithoutAlignment() {
        val input = """
            | 列1 | 列2 | 列3 |
            | --- | --- | --- |
            | 值1 | 值2 | 值3 |
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val table = result[0] as Table
        assertEquals(3, table.alignments.size)
        // 默认应该是左对齐
        assertEquals(TableAlignment.LEFT, table.alignments[0])
        assertEquals(TableAlignment.LEFT, table.alignments[1])
        assertEquals(TableAlignment.LEFT, table.alignments[2])
    }

    @Test
    fun testTableMixedAlignment() {
        val input = """
            | 名称 | 价格 | 数量 | 备注 |
            | :--- | ---: | :---: | --- |
            | 商品A | 100.00 | 5 | 优质 |
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val table = result[0] as Table
        assertEquals(4, table.alignments.size)
        assertEquals(TableAlignment.LEFT, table.alignments[0])
        assertEquals(TableAlignment.RIGHT, table.alignments[1])
        assertEquals(TableAlignment.CENTER, table.alignments[2])
        assertEquals(TableAlignment.LEFT, table.alignments[3])
    }

    @Test
    fun testTableInvalidAlignmentFormat() {
        val input = """
            | 列1 | 列2 |
            | :-- | --: |
            | 值1 | 值2 |
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val table = result[0] as Table
        // 无效格式应该默认为左对齐
        assertEquals(TableAlignment.LEFT, table.alignments[0])
        assertEquals(TableAlignment.LEFT, table.alignments[1])
    }

    @Test
    fun testTableEmptyAlignment() {
        val input = """
            | 列1 | 列2 |
            |  |  |
            | 值1 | 值2 |
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val table = result[0] as Table
        assertEquals(2, table.alignments.size)
        assertEquals(TableAlignment.LEFT, table.alignments[0])
        assertEquals(TableAlignment.LEFT, table.alignments[1])
    }

    // ========== 嵌套列表解析测试 ==========

    @Test
    fun testNestedUnorderedListParsing() {
        val input = """
            - 一级项目1
                - 二级项目1
                - 二级项目2
                    - 三级项目1
            - 一级项目2
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertTrue(result.any { it is UnorderedList && (it as UnorderedList).level == 1 })
        assertTrue(result.any { it is UnorderedList && (it as UnorderedList).level == 2 })
        assertTrue(result.any { it is UnorderedList && (it as UnorderedList).level == 3 })
    }

    @Test
    fun testNestedOrderedListParsing() {
        val input = """
            1. 第一项
                1. 子项目1
                2. 子项目2
            2. 第二项
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertTrue(result.any { it is OrderedList && (it as OrderedList).level == 1 })
        assertTrue(result.any { it is OrderedList && (it as OrderedList).level == 2 })
    }

    @Test
    fun testMixedNestedListParsing() {
        val input = """
            - 无序列表项
                1. 嵌套有序列表1
                2. 嵌套有序列表2
                    - 深层无序列表
            1. 有序列表项
                - 嵌套无序列表
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertTrue(result.any { it is UnorderedList })
        assertTrue(result.any { it is OrderedList })

        // 检查层级
        val unorderedLists = result.filterIsInstance<UnorderedList>()
        val orderedLists = result.filterIsInstance<OrderedList>()

        assertTrue(unorderedLists.any { it.level == 1 })
        assertTrue(unorderedLists.any { it.level == 2 })
        assertTrue(orderedLists.any { it.level == 1 })
        assertTrue(orderedLists.any { it.level == 2 })
    }

    @Test
    fun testTaskListNestedWithOtherLists() {
        val input = """
            - [ ] 任务项
                - 普通列表项
                1. 有序列表项
                    - [x] 深层任务项
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertTrue(result.any { it is TaskList })
        assertTrue(result.any { it is UnorderedList })
        assertTrue(result.any { it is OrderedList })

        val taskLists = result.filterIsInstance<TaskList>()
        assertTrue(taskLists.any { it.level == 1 })
        assertTrue(taskLists.any { it.level == 3 })
    }

    @Test
    fun testDeepNestedListParsing() {
        val input = """
            - 一级
                - 二级
                    - 三级
                        - 四级
                            - 五级
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        val unorderedLists = result.filterIsInstance<UnorderedList>()
        assertTrue(unorderedLists.any { it.level == 1 })
        assertTrue(unorderedLists.any { it.level == 2 })
        assertTrue(unorderedLists.any { it.level == 3 })
        assertTrue(unorderedLists.any { it.level == 4 })
        assertTrue(unorderedLists.any { it.level == 5 })
    }

    @Test
    fun testInconsistentIndentationHandling() {
        val input = """
            - 项目1
              - 不规则缩进（2空格）
                - 正常缩进（4空格）
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        // 应该能处理不规则缩进
        assertTrue(result.filterIsInstance<UnorderedList>().isNotEmpty())
    }

    // ========== 代码块语法高亮解析测试 ==========

    @Test
    fun testCodeBlockWithLanguageParsing() {
        val input = """
            ```kotlin
            fun main() {
                println("Hello, World!")
            }
            ```
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val codeBlock = result[0] as CodeBlock
        assertEquals("kotlin", codeBlock.language)
        assertTrue(codeBlock.code.contains("fun main()"))
        assertTrue(codeBlock.code.contains("println"))
    }

    @Test
    fun testCodeBlockWithoutLanguage() {
        val input = """
            ```
            echo "Hello, World!"
            ```
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val codeBlock = result[0] as CodeBlock
        assertEquals("", codeBlock.language)
        assertEquals("echo \"Hello, World!\"", codeBlock.code)
    }

    @Test
    fun testMultipleLanguageCodeBlocks() {
        val input = """
            ```java
            System.out.println("Java");
            ```

            ```python
            print("Python")
            ```

            ```javascript
            console.log("JavaScript");
            ```
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        val codeBlocks = result.filterIsInstance<CodeBlock>()
        assertEquals(3, codeBlocks.size)

        assertEquals("java", codeBlocks[0].language)
        assertEquals("python", codeBlocks[1].language)
        assertEquals("javascript", codeBlocks[2].language)

        assertTrue(codeBlocks[0].code.contains("System.out.println"))
        assertTrue(codeBlocks[1].code.contains("print"))
        assertTrue(codeBlocks[2].code.contains("console.log"))
    }

    @Test
    fun testCodeBlockWithComplexContent() {
        val input = """
            ```html
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test</title>
            </head>
            <body>
                <h1>Hello</h1>
            </body>
            </html>
            ```
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val codeBlock = result[0] as CodeBlock
        assertEquals("html", codeBlock.language)
        assertTrue(codeBlock.code.contains("<!DOCTYPE html>"))
        assertTrue(codeBlock.code.contains("<title>Test</title>"))
    }

    // ========== 多级引用解析测试 ==========

    @Test
    fun testMultiLevelBlockQuoteParsing() {
        val input = """
            > 一级引用
            >> 二级引用
            >>> 三级引用
            >>>> 四级引用
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(4, result.size)

        val quote1 = result[0] as BlockQuote
        assertEquals(1, quote1.level)
        assertEquals("一级引用", quote1.text)

        val quote2 = result[1] as BlockQuote
        assertEquals(2, quote2.level)
        assertEquals("二级引用", quote2.text)

        val quote3 = result[2] as BlockQuote
        assertEquals(3, quote3.level)
        assertEquals("三级引用", quote3.text)

        val quote4 = result[3] as BlockQuote
        assertEquals(4, quote4.level)
        assertEquals("四级引用", quote4.text)
    }

    @Test
    fun testMaximumQuoteLevel() {
        val input = """
            > 一级
            >> 二级
            >>> 三级
            >>>> 四级
            >>>>> 五级
            >>>>>> 六级
            >>>>>>> 七级（超过最大级别）
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        val quotes = result.filterIsInstance<BlockQuote>()

        // 检查1-6级正常解析
        assertTrue(quotes.any { it.level == 1 })
        assertTrue(quotes.any { it.level == 2 })
        assertTrue(quotes.any { it.level == 3 })
        assertTrue(quotes.any { it.level == 4 })
        assertTrue(quotes.any { it.level == 5 })
        assertTrue(quotes.any { it.level == 6 })

        // 超过6级的应该被处理为普通段落或限制为6级
        assertFalse(quotes.any { it.level > 6 })
    }

    @Test
    fun testQuoteWithContent() {
        val input = """
            > 这是一个引用，包含**加粗**和*斜体*文本
            >> 嵌套引用包含`代码`
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(2, result.size)

        val quote1 = result[0] as BlockQuote
        assertEquals(1, quote1.level)
        assertTrue(quote1.text.contains("加粗"))
        assertTrue(quote1.text.contains("斜体"))

        val quote2 = result[1] as BlockQuote
        assertEquals(2, quote2.level)
        assertTrue(quote2.text.contains("代码"))
    }

    @Test
    fun testQuoteWithoutSpace() {
        val input = """
            >引用没有空格
            >> 引用有空格
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        val quotes = result.filterIsInstance<BlockQuote>()
        assertTrue(quotes.isNotEmpty())

        // 应该能处理有无空格的情况
        assertTrue(quotes.any { it.text == "引用没有空格" })
        assertTrue(quotes.any { it.text == "引用有空格" })
    }

    // ========== 高亮文本解析测试 ==========

    @Test
    fun testHighlightTextParsing() {
        val input = """
            ==高亮文本==
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val highlight = result[0] as Highlight
        assertEquals("高亮文本", highlight.text)
    }

    @Test
    fun testHighlightWithSpecialCharacters() {
        val input = """
            ==包含特殊字符的高亮：*bold* and `code`==
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val highlight = result[0] as Highlight
        assertEquals("包含特殊字符的高亮：*bold* and `code`", highlight.text)
    }

    @Test
    fun testHighlightInvalidFormat() {
        val input = """
            =单个等号=
            ===三个等号===
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        // 无效格式不应该被解析为高亮
        assertFalse(result.any { it is Highlight })
    }

    // ========== 转义字符解析测试 ==========

    @Test
    fun testBasicEscapeCharacters() {
        val input = """
            \*不是加粗\*
            \#不是标题
            \[不是链接\]
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(3, result.size)

        val para1 = result[0] as Paragraph
        assertEquals("*不是加粗*", para1.text)

        val para2 = result[1] as Paragraph
        assertEquals("#不是标题", para2.text)

        val para3 = result[2] as Paragraph
        assertEquals("[不是链接]", para3.text)
    }

    @Test
    fun testAllSupportedEscapeCharacters() {
        val input = """
            转义字符测试：\* \# \[ \] \( \) \{ \} \_ \` \~ \\
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val paragraph = result[0] as Paragraph
        val expected = "转义字符测试：* # [ ] ( ) { } _ ` ~ \\"
        assertEquals(expected, paragraph.text)
    }

    @Test
    fun testEscapeInHeadings() {
        val input = """
            ## 标题包含转义字符：\*不加粗\*
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val heading = result[0] as Heading
        assertEquals(2, heading.level)
        assertEquals("标题包含转义字符：*不加粗*", heading.text)
    }

    @Test
    fun testEscapeInLists() {
        val input = """
            - 列表项包含转义：\*不加粗\*
            1. 有序列表：\#不是标题
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        val unorderedList = result.filterIsInstance<UnorderedList>().firstOrNull()
        assertNotNull(unorderedList)
        assertTrue(unorderedList!!.items.any { it.contains("*不加粗*") })

        val orderedList = result.filterIsInstance<OrderedList>().firstOrNull()
        assertNotNull(orderedList)
        assertTrue(orderedList!!.items.any { it.contains("#不是标题") })
    }

    @Test
    fun testInvalidEscapeCharacters() {
        val input = """
            \z不支持的转义
            \1数字转义
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(2, result.size)

        // 不支持的转义字符应该保持原样
        val para1 = result[0] as Paragraph
        assertEquals("\\z不支持的转义", para1.text)

        val para2 = result[1] as Paragraph
        assertEquals("\\1数字转义", para2.text)
    }

    // ========== 扩展语法解析测试（脚注、上下标、数学公式） ==========

    @Test
    fun testFootnoteDefinitionParsing() {
        val input = """
            [^1]: 这是脚注内容
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val footnote = result[0] as Footnote
        assertEquals("1", footnote.id)
        assertEquals("这是脚注内容", footnote.content)
        assertFalse(footnote.isReference)
    }

    @Test
    fun testFootnoteReferenceParsing() {
        val input = """
            文本包含脚注引用[^1]
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val footnote = result[0] as Footnote
        assertEquals("1", footnote.id)
        assertEquals("", footnote.content)
        assertTrue(footnote.isReference)
    }

    @Test
    fun testSuperscriptParsing() {
        val input = """
            E=mc^2^
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val superscript = result[0] as Superscript
        assertEquals("2", superscript.text)
    }

    @Test
    fun testSubscriptParsing() {
        val input = """
            H~2~O
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val subscript = result[0] as Subscript
        assertEquals("2", subscript.text)
    }

    @Test
    fun testInlineMathParsing() {
        val input = """
            内联数学公式：$E=mc^2$
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val math = result[0] as Math
        assertEquals("E=mc^2", math.expression)
        assertTrue(math.isInline)
    }

    @Test
    fun testBlockMathParsing() {
        val input = """
            $$\sum_{i=1}^{n} x_i = 0$$
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)

        val math = result[0] as Math
        assertEquals("\\sum_{i=1}^{n} x_i = 0", math.expression)
        assertFalse(math.isInline)
    }

    @Test
    fun testComplexFootnoteIds() {
        val input = """
            [^note1]: 脚注1
            [^note-2]: 脚注2
            [^note_3]: 脚注3
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertEquals(3, result.size)

        val footnotes = result.filterIsInstance<Footnote>()
        assertTrue(footnotes.any { it.id == "note1" })
        assertTrue(footnotes.any { it.id == "note-2" })
        assertTrue(footnotes.any { it.id == "note_3" })
    }

    @Test
    fun testSuperscriptSubscriptWithComplexContent() {
        val input = """
            x^{n+1}^
            H~{2}SO{4}~
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        val superscripts = result.filterIsInstance<Superscript>()
        val subscripts = result.filterIsInstance<Subscript>()

        assertTrue(superscripts.any { it.text.contains("n+1") })
        assertTrue(subscripts.any { it.text.contains("2") })
    }

    // ========== 边界情况和异常输入测试 ==========

    @Test
    fun testEmptyInput() {
        val input = ""

        val result = MarkdownParser.parse(input)

        assertTrue(result.isEmpty())
    }

    @Test
    fun testWhitespaceOnlyInput() {
        val input = "   \n\t\n   "

        val result = MarkdownParser.parse(input)

        assertTrue(result.isEmpty() || result.all { it is Paragraph && (it as Paragraph).text.isBlank() })
    }

    @Test
    fun testMixedSyntaxParsing() {
        val input = """
            # 标题
            - [ ] 任务项
            > 引用
            ```kotlin
            fun test() {}
            ```
            ==高亮文本==
            $$E=mc^2$$
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        assertTrue(result.any { it is Heading })
        assertTrue(result.any { it is TaskList })
        assertTrue(result.any { it is BlockQuote })
        assertTrue(result.any { it is CodeBlock })
        assertTrue(result.any { it is Highlight })
        assertTrue(result.any { it is Math })
    }

    @Test
    fun testLargeInput() {
        val largeContent = StringBuilder()
        for (i in 1..1000) {
            largeContent.appendLine("# 标题 $i")
            largeContent.appendLine("内容 $i")
        }

        val result = MarkdownParser.parse(largeContent.toString())

        // 应该能处理大文档而不崩溃
        assertTrue(result.size > 1000)
    }

    @Test
    fun testSpecialUnicodeCharacters() {
        val input = """
            # 标题包含Unicode：🎉 测试 🚀
            - [ ] 任务包含emoji：✅ 完成
            > 引用包含特殊字符：«引用内容»
        """.trimIndent()

        val result = MarkdownParser.parse(input)

        val heading = result.filterIsInstance<Heading>().firstOrNull()
        assertNotNull(heading)
        assertTrue(heading!!.text.contains("🎉"))
        assertTrue(heading.text.contains("🚀"))

        val task = result.filterIsInstance<TaskList>().firstOrNull()
        assertNotNull(task)
        assertTrue(task!!.text.contains("✅"))

        val quote = result.filterIsInstance<BlockQuote>().firstOrNull()
        assertNotNull(quote)
        assertTrue(quote!!.text.contains("«"))
        assertTrue(quote.text.contains("»"))
    }
}