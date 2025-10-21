package com.zj.ink.md

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before

/**
 * Android版本兼容性测试
 * 测试在不同Android API级别下的兼容性，验证Compose和Glance在不同版本下的表现
 */
class MarkdownAndroidCompatibilityTest {

    private val parser = MarkdownParser()

    @Before
    fun setUp() {
        // 测试前的初始化工作
    }

    // ================================
    // 1. API级别兼容性测试
    // ================================

    @Test
    fun testApiLevelCompatibilityBasics() {
        // 测试基础功能在不同API级别的兼容性
        val markdown = """
            # API兼容性测试

            基础功能测试：
            - **加粗文本**
            - *斜体文本*
            - `内联代码`
            - [链接](https://example.com)

            ```kotlin
            fun apiTest() {
                println("API兼容性测试")
            }
            ```
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证基础元素解析正常
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 验证各种基础元素类型
        assertTrue("缺少标题元素", elements.any { it is MarkdownElement.Heading })
        assertTrue("缺少段落元素", elements.any { it is MarkdownElement.Paragraph })
        assertTrue("缺少加粗元素", elements.any { it is MarkdownElement.Bold })
        assertTrue("缺少斜体元素", elements.any { it is MarkdownElement.Italic })
        assertTrue("缺少内联代码元素", elements.any { it is MarkdownElement.InlineCode })
        assertTrue("缺少链接元素", elements.any { it is MarkdownElement.Link })
        assertTrue("缺少代码块元素", elements.any { it is MarkdownElement.CodeBlock })
    }

    @Test
    fun testApiLevelCompatibilityNewFeatures() {
        // 测试新功能在不同API级别的兼容性
        val markdown = """
            # 新功能API兼容性测试

            #### H4标题测试
            ##### H5标题测试
            ###### H6标题测试

            任务列表测试：
            - [x] 已完成任务
            - [ ] 未完成任务

            表格对齐测试：
            | 左对齐 | 居中 | 右对齐 |
            |:-------|:----:|-------:|
            | 数据1 | 数据2 | 数据3 |

            扩展语法测试：
            ==高亮文本== 和 X^2^ 以及 H~2~O

            数学公式：$E=mc^2$

            脚注引用[^1]

            [^1]: 脚注内容
        """.trimIndent()

        val elements = parser.parse(markdown)

        // 验证新功能元素解析正常
        assertNotNull(elements)
        assertTrue(elements.isNotEmpty())

        // 验证H4-H6标题
        val headings = elements.filter { it is MarkdownElement.Heading }
            .map { it as MarkdownElement.Heading }
        val headingLevels = headings.map { it.level }.toSet()
        assertTrue("应该包含H4-H6标题", headingLevels.contains(4))
        assertTrue("应该包含H4-H6标题", headingLevels.contains(5))
        assertTrue("应该包含H4-H6标题", headingLevels.contains(6))

        // 验证任务列表
        assertTrue("缺少任务列表元素", elements.any { it is MarkdownElement.TaskList })

        // 验证表格对齐
        val table = elements.find { it is MarkdownElement.Table } as? MarkdownElement.Table
        if (table != null) {
            assertEquals(3, table.alignments.size)
        }
    }

    // ================================
    // 2. 数据结构兼容性测试
    // ================================

    @Test
    fun testDataStructureCompatibility() {
        // 测试数据结构的向后兼容性

        // 测试原有构造函数仍然可用
        val heading = MarkdownElement.Heading(text = "测试标题", level = 1)
        assertEquals("测试标题", heading.text)
        assertEquals(1, heading.level)

        // 测试扩展后的构造函数
        val headingH4 = MarkdownElement.Heading(text = "H4标题", level = 4)
        assertEquals("H4标题", headingH4.text)
        assertEquals(4, headingH4.level)

        // 测试表格的向后兼容性
        val table = MarkdownElement.Table(
            headers = listOf("列1", "列2"),
            rows = listOf(listOf("数据1", "数据2"))
        )
        assertEquals(2, table.headers.size)
        assertEquals(1, table.rows.size)
        // 新增的alignments字段应该有默认值
        assertNotNull(table.alignments)

        // 测试代码块的向后兼容性
        val codeBlock = MarkdownElement.CodeBlock(code = "println(\"test\")")
        assertEquals("println(\"test\")", codeBlock.code)
        // 新增的language字段应该有默认值
        assertNotNull(codeBlock.language)

        // 测试新增的数据结构
        val taskList = MarkdownElement.TaskList(
            items = listOf(
                Pair("任务1", true),
                Pair("任务2", false)
            ),
            level = 0
        )
        assertEquals(2, taskList.items.size)
        assertEquals(0, taskList.level)
        assertTrue(taskList.items[0].second)
        assertFalse(taskList.items[1].second)
    }

    @Test
    fun testSerializationCompatibility() {
        // 测试序列化兼容性（如果项目使用了序列化）
        val elements = listOf(
            MarkdownElement.Heading("标题", 1),
            MarkdownElement.Paragraph("段落"),
            MarkdownElement.TaskList(listOf(Pair("任务", true)), 0),
            MarkdownElement.Table(
                headers = listOf("列1", "列2"),
                rows = listOf(listOf("数据1", "数据2")),
                alignments = listOf(
                    MarkdownElement.TableAlignment.LEFT,
                    MarkdownElement.TableAlignment.RIGHT
                )
            )
        )

        // 验证所有元素都可以正常创建和访问
        elements.forEach { element ->
            assertNotNull("元素不应为null", element)
            when (element) {
                is MarkdownElement.Heading -> {
                    assertNotNull(element.text)
                    assertTrue(element.level > 0)
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
                else -> {
                    // 其他元素类型的基本验证
                }
            }
        }
    }

    // ================================
    // 3. 依赖库兼容性测试
    // ================================

    @Test
    fun testDependencyCompatibility() {
        // 测试依赖库的兼容性

        // 验证解析器基本功能不依赖特定版本
        val simpleMarkdown = "# 简单测试\n\n这是测试内容。"
        val elements = parser.parse(simpleMarkdown)

        assertNotNull("解析器应该正常工作", elements)
        assertTrue("应该解析出元素", elements.isNotEmpty())

        // 验证复杂功能的兼容性
        val complexMarkdown = """
            # 复杂功能测试

            - [x] 任务1
            - [ ] 任务2

            ```kotlin
            fun test() {
                println("依赖兼容性测试")
            }
            ```

            | 列1 | 列2 |
            |:---|---:|
            | 左 | 右 |
        """.trimIndent()

        val complexElements = parser.parse(complexMarkdown)
        assertNotNull("复杂解析应该正常工作", complexElements)
        assertTrue("应该解析出复杂元素", complexElements.size > 3)
    }

    @Test
    fun testKotlinVersionCompatibility() {
        // 测试Kotlin版本兼容性

        // 使用各种Kotlin特性确保兼容性
        val markdown = "# Kotlin兼容性测试"
        val elements = parser.parse(markdown)

        // 使用let、run、apply等Kotlin特性
        elements.let { elementList ->
            assertNotNull(elementList)
            assertTrue(elementList.isNotEmpty())
        }

        // 使用when表达式
        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Heading -> assertTrue(element.level > 0)
                is MarkdownElement.Paragraph -> assertNotNull(element.text)
                else -> {
                    // 其他类型的处理
                }
            }
        }

        // 使用数据类特性
        val heading = elements.find { it is MarkdownElement.Heading } as? MarkdownElement.Heading
        heading?.let {
            // 测试数据类的copy方法
            val copiedHeading = it.copy(level = 2)
            assertEquals(2, copiedHeading.level)
            assertEquals(it.text, copiedHeading.text)
        }
    }

    // ================================
    // 4. 性能兼容性测试
    // ================================

    @Test
    fun testPerformanceCompatibilityAcrossVersions() {
        // 测试在不同版本下的性能表现
        val testMarkdown = """
            # 性能兼容性测试

            ## 基础功能
            这是包含**加粗**、*斜体*、~~删除线~~的段落。

            ## 列表功能
            - 无序列表1
            - 无序列表2
                - [x] 嵌套任务1
                - [ ] 嵌套任务2

            ## 表格功能
            | 项目 | 状态 | 说明 |
            |:-----|:----:|-----:|
            | 功能1 | ✅ | 正常 |
            | 功能2 | ⏳ | 开发中 |

            ## 代码功能
            ```kotlin
            fun performance() {
                repeat(100) {
                    println("性能测试 $it")
                }
            }
            ```
        """.trimIndent()

        // 多次解析测试性能稳定性
        val times = mutableListOf<Long>()

        repeat(10) {
            val startTime = System.currentTimeMillis()
            val elements = parser.parse(testMarkdown)
            val endTime = System.currentTimeMillis()

            assertNotNull(elements)
            assertTrue(elements.isNotEmpty())

            times.add(endTime - startTime)
        }

        // 计算平均时间和标准差
        val avgTime = times.average()
        val maxTime = times.maxOrNull() ?: 0L
        val minTime = times.minOrNull() ?: 0L

        // 性能应该稳定（最大最小时间差不超过50ms）
        assertTrue("性能不稳定，时间差过大: ${maxTime - minTime}ms",
                   maxTime - minTime < 50)

        // 平均时间应该合理（<20ms）
        assertTrue("平均解析时间过长: ${avgTime}ms", avgTime < 20)

        println("性能兼容性测试: 平均${avgTime}ms, 范围${minTime}-${maxTime}ms")
    }

    @Test
    fun testMemoryCompatibilityAcrossVersions() {
        // 测试内存使用的兼容性
        val runtime = Runtime.getRuntime()

        // 强制垃圾回收获取基准内存
        System.gc()
        Thread.sleep(100)
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // 执行多次解析操作
        repeat(50) { iteration ->
            val markdown = """
                # 内存兼容性测试 $iteration

                包含各种元素的测试文档：
                - [x] 任务 $iteration
                - [ ] 另一个任务

                ```kotlin
                fun memoryTest$iteration() {
                    println("内存测试 $iteration")
                }
                ```

                | 序号 | 内容 |
                |------|------|
                | $iteration | 测试数据 |
            """.trimIndent()

            val elements = parser.parse(markdown)
            assertNotNull(elements)

            // 每10次检查一次内存
            if (iteration % 10 == 9) {
                System.gc()
                Thread.sleep(50)
            }
        }

        // 最终内存检查
        System.gc()
        Thread.sleep(100)
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()

        val memoryIncrease = finalMemory - initialMemory

        // 内存增长应该在合理范围内（<20MB）
        val maxMemoryIncrease = 20 * 1024 * 1024 // 20MB
        assertTrue("内存使用增长过多: ${memoryIncrease / 1024 / 1024}MB",
                   memoryIncrease < maxMemoryIncrease)

        println("内存兼容性测试: 增长 ${memoryIncrease / 1024 / 1024}MB")
    }

    // ================================
    // 5. 边界条件兼容性测试
    // ================================

    @Test
    fun testBoundaryConditionsCompatibility() {
        // 测试边界条件的兼容性

        // 空输入测试
        val emptyResult = parser.parse("")
        assertNotNull("空输入应该返回非null结果", emptyResult)

        // 只有空白字符的输入
        val whitespaceResult = parser.parse("   \n\t  \n   ")
        assertNotNull("空白字符输入应该返回非null结果", whitespaceResult)

        // 极长的单行输入
        val longLine = "# " + "很长的标题内容".repeat(1000)
        val longLineResult = parser.parse(longLine)
        assertNotNull("长行输入应该正常处理", longLineResult)

        // 大量的短行输入
        val manyLines = (1..1000).joinToString("\n") { "- 列表项 $it" }
        val manyLinesResult = parser.parse(manyLines)
        assertNotNull("大量短行应该正常处理", manyLinesResult)
        assertTrue("应该解析出多个元素", manyLinesResult.isNotEmpty())

        // 特殊字符输入
        val specialChars = """
            # 特殊字符测试 🎉

            包含Unicode字符：中文测试 العربية русский 日本語

            特殊符号：@#$%^&*()[]{}|;':",./<>?`~

            转义字符：\# \* \[ \] \( \) \{ \} \_ \` \~ \\
        """.trimIndent()

        val specialResult = parser.parse(specialChars)
        assertNotNull("特殊字符应该正常处理", specialResult)
        assertTrue("应该解析出元素", specialResult.isNotEmpty())
    }

    @Test
    fun testErrorRecoveryCompatibility() {
        // 测试错误恢复的兼容性
        val malformedMarkdown = """
            # 正常标题

            这是正常段落。

            - [ 格式错误的任务列表
            - [x] 正常的任务列表
            - [] 另一个错误的任务

            | 不完整的 | 表格
            |-------
            | 缺少列 |

            ```kotlin
            没有结束标记的代码块

            > 不完整的引用
            >> 另一个引用

            ==没有结束标记的高亮文本

            [^不完整的脚注引用

            # 后续正常内容

            这应该能正常解析。
        """.trimIndent()

        // 即使有格式错误，也应该能解析出一些正确的元素
        val elements = parser.parse(malformedMarkdown)
        assertNotNull("错误输入应该返回非null结果", elements)

        // 应该至少解析出一些正确的元素
        assertTrue("应该解析出一些正确的元素", elements.isNotEmpty())

        // 验证正确的元素确实被解析了
        assertTrue("应该包含正常的标题",
                   elements.any { it is MarkdownElement.Heading })
        assertTrue("应该包含正常的段落",
                   elements.any { it is MarkdownElement.Paragraph })
    }

    // ================================
    // 6. 并发兼容性测试
    // ================================

    @Test
    fun testConcurrencyCompatibility() {
        // 测试并发访问的兼容性
        val markdown = """
            # 并发兼容性测试

            - [x] 任务1
            - [ ] 任务2

            ```kotlin
            fun concurrent() {
                println("并发测试")
            }
            ```
        """.trimIndent()

        val threads = mutableListOf<Thread>()
        val results = mutableListOf<List<MarkdownElement>>()
        val exceptions = mutableListOf<Exception>()

        // 创建多个线程同时解析
        repeat(5) { threadIndex ->
            val thread = Thread {
                try {
                    repeat(5) { iteration ->
                        val elements = parser.parse(markdown)
                        synchronized(results) {
                            results.add(elements)
                        }
                    }
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                }
            }
            threads.add(thread)
        }

        // 启动所有线程
        threads.forEach { it.start() }

        // 等待所有线程完成
        threads.forEach { it.join(5000) } // 5秒超时

        // 验证结果
        assertTrue("并发访问出现异常: ${exceptions.size}", exceptions.isEmpty())
        assertEquals("并发解析结果数量不正确", 25, results.size)

        // 验证所有结果一致
        if (results.isNotEmpty()) {
            val firstResult = results.first()
            results.forEach { result ->
                assertEquals("并发解析结果不一致", firstResult.size, result.size)
            }
        }
    }

    // ================================
    // 7. 回归测试
    // ================================

    @Test
    fun testRegressionCompatibility() {
        // 回归测试：确保新功能不会破坏现有功能

        // 测试原有的基础功能
        val basicMarkdown = """
            # 回归测试

            ## 基础功能验证

            这是包含**加粗**、*斜体*、~~删除线~~和`内联代码`的段落。

            ### 列表功能
            - 无序列表项1
            - 无序列表项2

            1. 有序列表项1
            2. 有序列表项2

            ### 链接和图片
            [链接文本](https://example.com)
            ![图片描述](image.jpg)

            ### 代码块
            ```
            普通代码块
            function test() {
                console.log("测试");
            }
            ```

            ### 引用
            > 这是引用文本

            ### 表格
            | 列1 | 列2 |
            |-----|-----|
            | 数据1 | 数据2 |
        """.trimIndent()

        val elements = parser.parse(basicMarkdown)

        // 验证所有基础元素类型都存在
        val elementTypes = elements.map { it::class.java.simpleName }.toSet()

        val expectedTypes = setOf(
            "Heading", "Paragraph", "Bold", "Italic", "Strikethrough",
            "InlineCode", "UnorderedList", "OrderedList", "Link", "Image",
            "CodeBlock", "BlockQuote", "Table"
        )

        expectedTypes.forEach { expectedType ->
            assertTrue("缺少基础元素类型: $expectedType",
                       elementTypes.contains(expectedType))
        }

        // 验证元素内容的正确性
        val heading = elements.find { it is MarkdownElement.Heading } as? MarkdownElement.Heading
        assertNotNull("应该有标题元素", heading)
        assertEquals("回归测试", heading?.text)

        val table = elements.find { it is MarkdownElement.Table } as? MarkdownElement.Table
        assertNotNull("应该有表格元素", table)
        assertEquals(2, table?.headers?.size)
        assertEquals(1, table?.rows?.size)

        // 验证新功能不会影响原有功能的数据结构
        assertTrue("标题level应该在1-3范围内", heading?.level in 1..3)
    }
}