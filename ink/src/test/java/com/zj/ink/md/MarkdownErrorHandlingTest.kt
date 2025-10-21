package com.zj.ink.md

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Markdown解析器和渲染器错误处理和边界情况测试
 * 验证任务33的实现：错误处理和边界情况
 */
class MarkdownErrorHandlingTest {

    @Before
    fun setUp() {
        // 重置错误统计
        MarkdownParser.resetErrorStats()
    }

    @Test
    fun testEmptyInput() {
        // 测试空输入
        val result = MarkdownParser.parse("")
        assertTrue("空输入应返回空列表", result.isEmpty())
        assertEquals("空输入不应产生错误", 0, MarkdownParser.getParseErrorCount())
    }

    @Test
    fun testNullAndBlankInput() {
        // 测试空白输入
        val blankResult = MarkdownParser.parse("   \n\t  \n   ")
        assertTrue("空白输入应返回空列表", blankResult.isEmpty())

        // 测试只有换行的输入
        val newlineResult = MarkdownParser.parse("\n\n\n")
        assertTrue("只有换行的输入应返回空列表", newlineResult.isEmpty())
    }

    @Test
    fun testVeryLongText() {
        // 测试超长文本（超过1MB）
        val longText = "a".repeat(1_500_000) // 1.5MB
        val result = MarkdownParser.parse(longText)

        assertTrue("超长文本应有错误处理", result.isNotEmpty())
        assertTrue("应该返回错误提示", result.any { it is Paragraph && it.text.contains("过大") })
        assertTrue("应该记录错误", MarkdownParser.getParseErrorCount() > 0)
    }

    @Test
    fun testMalformedTaskList() {
        // 测试格式错误的任务列表
        val malformedTasks = """
            - [y] 错误状态
            - [] 空状态
            - [xx] 多字符状态
            - [ 未闭合状态
        """.trimIndent()

        val result = MarkdownParser.parse(malformedTasks)

        // 应该优雅处理，不崩溃
        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含一些解析结果", result.isNotEmpty())

        // 错误的任务列表应该被处理为普通列表或段落
        val hasValidElements = result.any { element ->
            when (element) {
                is TaskList -> element.text.isNotEmpty()
                is UnorderedList -> element.items.isNotEmpty()
                is Paragraph -> element.text.isNotEmpty()
                else -> false
            }
        }
        assertTrue("应该包含有效的解析元素", hasValidElements)
    }

    @Test
    fun testMalformedTable() {
        // 测试格式错误的表格
        val malformedTable = """
            | 标题1 | 标题2
            |---|
            | 内容1 | 内容2 | 多余内容
            | 缺少内容
            |
        """.trimIndent()

        val result = MarkdownParser.parse(malformedTable)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 检查是否有表格或降级为段落
        val hasTableOrParagraph = result.any { element ->
            element is Table || element is Paragraph
        }
        assertTrue("应该包含表格或段落", hasTableOrParagraph)
    }

    @Test
    fun testUnclosedCodeBlock() {
        // 测试未正确闭合的代码块
        val unclosedCode = """
            ```kotlin
            fun test() {
                println("Hello")
            // 缺少结束的```
        """.trimIndent()

        val result = MarkdownParser.parse(unclosedCode)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 应该优雅处理未闭合的代码块
        val hasCodeOrParagraph = result.any { element ->
            element is CodeBlock || element is Paragraph
        }
        assertTrue("应该包含代码块或段落", hasCodeOrParagraph)
    }

    @Test
    fun testExcessiveNesting() {
        // 测试过深的嵌套层级
        val deepNesting = buildString {
            repeat(15) { level ->
                append("    ".repeat(level)) // 每级4个空格
                append("- 第${level + 1}级列表\n")
            }
        }

        val result = MarkdownParser.parse(deepNesting)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 检查是否限制了嵌套层级
        val listElements = result.filterIsInstance<UnorderedList>()
        if (listElements.isNotEmpty()) {
            val maxLevel = listElements.maxOf { it.level }
            assertTrue("应该限制最大嵌套层级", maxLevel <= 10)
        }
    }

    @Test
    fun testExcessiveQuoteNesting() {
        // 测试过深的引用嵌套
        val deepQuotes = buildString {
            repeat(12) { level ->
                append(">".repeat(level + 1))
                append(" 第${level + 1}级引用\n")
            }
        }

        val result = MarkdownParser.parse(deepQuotes)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 检查引用层级限制
        val quoteElements = result.filterIsInstance<BlockQuote>()
        if (quoteElements.isNotEmpty()) {
            val maxLevel = quoteElements.maxOf { it.level }
            assertTrue("应该限制最大引用层级", maxLevel <= 10)
        }
    }

    @Test
    fun testSpecialCharacters() {
        // 测试特殊字符和Unicode字符
        val specialText = """
            # 标题 with 🎉 emoji
            - 列表项 with ñ ü ö special chars
            | 表格 | 包含 | 中文 | 和 | Русский |
            |---|---|---|---|---|
            | 数据 | with | 日本語 | and | العربية |

            ```javascript
            // 代码块 with special chars: ñ, ü, ö, 中文
            console.log("Hello 世界! 🌍");
            ```
        """.trimIndent()

        val result = MarkdownParser.parse(specialText)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 验证特殊字符被正确处理
        val hasHeading = result.any { it is Heading && it.text.contains("🎉") }
        assertTrue("应该正确处理emoji", hasHeading)

        val hasList = result.any { it is UnorderedList && it.items.any { item -> item.contains("ñ") } }
        assertTrue("应该正确处理特殊字符", hasList)
    }

    @Test
    fun testMalformedEscapeCharacters() {
        // 测试转义字符异常
        val malformedEscape = """
            \* 正常转义
            \\ 双反斜杠
            \xyz 无效转义
            \ 单独的反斜杠
            \
        """.trimIndent()

        val result = MarkdownParser.parse(malformedEscape)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 应该优雅处理无效转义
        val paragraphs = result.filterIsInstance<Paragraph>()
        assertTrue("应该包含段落", paragraphs.isNotEmpty())
    }

    @Test
    fun testLargeTable() {
        // 测试大表格（超过列数限制）
        val largeTable = buildString {
            // 创建超过50列的表格头
            append("|")
            repeat(60) { index ->
                append(" 列$index |")
            }
            append("\n|")
            repeat(60) {
                append("---|")
            }
            append("\n|")
            repeat(60) { index ->
                append(" 数据$index |")
            }
        }

        val result = MarkdownParser.parse(largeTable)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 检查是否限制了表格列数
        val tableElements = result.filterIsInstance<Table>()
        if (tableElements.isNotEmpty()) {
            val table = tableElements.first()
            assertTrue("应该限制表格列数", table.headers.size <= 50)
        }
    }

    @Test
    fun testExcessiveListItems() {
        // 测试过多的列表项
        val excessiveList = buildString {
            repeat(1500) { index ->
                append("- 列表项 $index\n")
            }
        }

        val result = MarkdownParser.parse(excessiveList)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 检查是否限制了列表项数量
        val listElements = result.filterIsInstance<UnorderedList>()
        if (listElements.isNotEmpty()) {
            val totalItems = listElements.sumOf { it.items.size }
            assertTrue("应该限制列表项总数", totalItems <= 1000)
        }
    }

    @Test
    fun testMixedMalformedContent() {
        // 测试混合的格式错误内容
        val mixedContent = """
            # 正常标题

            - [x] 正常任务
            - [y] 错误任务

            | 正常 | 表格 |
            |---|---|
            | 数据 | 内容 |
            | 错误行 | 缺少 |

            ```
            未闭合代码块

            > 正常引用
            >> 嵌套引用
            >>>>>>>>>>>>>>>>>>> 过深引用

            **正常加粗**
            ==高亮文本==
            ~~~错误删除线~~~
        """.trimIndent()

        val result = MarkdownParser.parse(mixedContent)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 验证正常内容被正确解析
        val hasHeading = result.any { it is Heading }
        assertTrue("应该包含标题", hasHeading)

        val hasTask = result.any { it is TaskList }
        assertTrue("应该包含任务列表", hasTask)

        // 验证错误内容被优雅处理
        assertTrue("解析不应崩溃", result.isNotEmpty())
    }

    @Test
    fun testPerformanceWithLargeDocument() {
        // 测试大文档性能
        val largeDocument = buildString {
            repeat(100) { section ->
                append("# 第${section + 1}节\n\n")
                append("这是第${section + 1}节的内容。".repeat(10))
                append("\n\n")

                append("## 子标题\n\n")
                repeat(20) { item ->
                    append("- 列表项 $item\n")
                }
                append("\n")

                append("| 列1 | 列2 | 列3 |\n")
                append("|---|---|---|\n")
                repeat(10) { row ->
                    append("| 数据${row}1 | 数据${row}2 | 数据${row}3 |\n")
                }
                append("\n")
            }
        }

        val startTime = System.currentTimeMillis()
        val result = MarkdownParser.parse(largeDocument)
        val endTime = System.currentTimeMillis()

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        val parseTime = endTime - startTime
        assertTrue("解析时间应该合理 (< 5秒)", parseTime < 5000)

        // 验证缓存机制
        val startTime2 = System.currentTimeMillis()
        val result2 = MarkdownParser.parse(largeDocument)
        val endTime2 = System.currentTimeMillis()

        val parseTime2 = endTime2 - startTime2
        assertTrue("缓存解析应该更快", parseTime2 < parseTime)
        assertEquals("缓存结果应该一致", result.size, result2.size)
    }

    @Test
    fun testErrorRecovery() {
        // 测试错误恢复机制
        val errorProneContent = """
            # 正常标题

            ```未闭合代码块
            代码内容

            | 错误 | 表格
            |---|
            | 数据 | 过多 | 列 |

            - [错误] 任务状态

            > 正常引用

            **正常加粗**
        """.trimIndent()

        val result = MarkdownParser.parse(errorProneContent)

        assertNotNull("解析结果不应为null", result)
        assertTrue("应该包含解析结果", result.isNotEmpty())

        // 验证正常内容仍然被正确解析
        val hasHeading = result.any { it is Heading && it.text == "正常标题" }
        assertTrue("正常标题应该被解析", hasHeading)

        val hasQuote = result.any { it is BlockQuote && it.text == "正常引用" }
        assertTrue("正常引用应该被解析", hasQuote)

        val hasBold = result.any { it is Bold && it.text == "正常加粗" }
        assertTrue("正常加粗应该被解析", hasBold)

        // 验证错误内容被优雅处理（不崩溃）
        assertTrue("错误内容应该被优雅处理", result.isNotEmpty())
    }

    @Test
    fun testCacheMemoryManagement() {
        // 测试缓存内存管理
        val initialErrorCount = MarkdownParser.getParseErrorCount()

        // 创建多个不同的文档来填充缓存
        repeat(60) { index ->
            val content = "# 文档 $index\n\n内容 $index"
            MarkdownParser.parse(content)
        }

        // 缓存应该被限制在合理范围内
        // 这里我们主要验证没有内存泄漏异常
        assertNotNull("缓存管理不应导致异常", MarkdownParser.parse("测试"))

        // 清理缓存
        MarkdownParser.clearCache()

        val content = "# 测试标题"
        val result = MarkdownParser.parse(content)
        assertTrue("清理缓存后应该正常工作", result.isNotEmpty())
    }
}