package com.zj.ink.md

import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Markdown性能兼容性测试
 * 确保新功能不会造成性能回归，测试大文档处理能力
 */
class MarkdownPerformanceCompatibilityTest {

    private val parser = MarkdownParser()

    @Test
    fun testPerformanceRegressionBasicSyntax() {
        // 测试基础语法的性能回归
        val basicMarkdown = """
            # 标题测试
            ## 二级标题
            ### 三级标题

            这是包含**加粗**、*斜体*、~~删除线~~和`内联代码`的段落。

            - 无序列表项1
            - 无序列表项2
            - 无序列表项3

            1. 有序列表项1
            2. 有序列表项2
            3. 有序列表项3

            [链接文本](https://example.com)

            ```kotlin
            fun example() {
                println("Hello World")
            }
            ```

            > 这是引用文本

            | 表头1 | 表头2 | 表头3 |
            |-------|-------|-------|
            | 数据1 | 数据2 | 数据3 |
        """.trimIndent()

        var totalTime = 0L
        val iterations = 100

        // 预热
        repeat(10) { parser.parse(basicMarkdown) }

        // 性能测试
        repeat(iterations) {
            totalTime += measureTimeMillis {
                parser.parse(basicMarkdown)
            }
        }

        val averageTime = totalTime / iterations

        // 基础语法平均解析时间应该小于10ms
        assertTrue("基础语法解析性能回归，平均时间: ${averageTime}ms", averageTime < 10)
    }

    @Test
    fun testPerformanceRegressionNewSyntax() {
        // 测试新语法的性能
        val newSyntaxMarkdown = """
            # 新语法性能测试

            #### H4标题
            ##### H5标题
            ###### H6标题

            - [x] 已完成任务
            - [ ] 未完成任务
                - [ ] 嵌套任务1
                - [x] 嵌套任务2

            | 左对齐 | 居中 | 右对齐 |
            |:-------|:----:|-------:|
            | 数据1 | 数据2 | 数据3 |
            | 测试1 | 测试2 | 测试3 |

            ```kotlin
            // 带语言标记的代码块
            fun test() {
                println("测试")
            }
            ```

            > 一级引用
            >> 二级引用
            >>> 三级引用

            这是包含==高亮文本==的段落。

            数学公式：$E=mc^2$ 和化学公式：H~2~O

            上标：X^2^ 下标：CO~2~

            脚注引用[^1]和[^note]

            [^1]: 脚注内容1
            [^note]: 脚注内容2
        """.trimIndent()

        var totalTime = 0L
        val iterations = 50

        // 预热
        repeat(5) { parser.parse(newSyntaxMarkdown) }

        // 性能测试
        repeat(iterations) {
            totalTime += measureTimeMillis {
                parser.parse(newSyntaxMarkdown)
            }
        }

        val averageTime = totalTime / iterations

        // 新语法平均解析时间应该小于20ms
        assertTrue("新语法解析性能不达标，平均时间: ${averageTime}ms", averageTime < 20)
    }

    @Test
    fun testLargeDocumentPerformance() {
        // 测试大文档性能
        val largeMarkdown = buildString {
            append("# 大文档性能测试\n\n")

            repeat(100) { section ->
                append("## 章节 $section\n\n")
                append("这是第 $section 个章节的内容。")
                append("包含**加粗**、*斜体*、~~删除线~~和`内联代码`。\n\n")

                // 添加各种列表
                append("### 传统列表\n")
                repeat(5) { item ->
                    append("- 无序列表项 $item\n")
                }
                append("\n")

                repeat(3) { item ->
                    append("${item + 1}. 有序列表项 ${item + 1}\n")
                }
                append("\n")

                // 添加任务列表
                append("### 任务列表\n")
                repeat(3) { task ->
                    val checked = if (task % 2 == 0) "x" else " "
                    append("- [$checked] 任务 $task\n")
                }
                append("\n")

                // 添加表格
                if (section % 10 == 0) {
                    append("### 数据表格\n")
                    append("| 项目 | 状态 | 说明 |\n")
                    append("|:-----|:----:|-----:|\n")
                    repeat(3) { row ->
                        append("| 项目$row | 完成 | 说明$row |\n")
                    }
                    append("\n")
                }

                // 添加代码块
                if (section % 15 == 0) {
                    append("### 代码示例\n")
                    append("```kotlin\n")
                    append("fun example$section() {\n")
                    append("    println(\"章节 $section\")\n")
                    append("    return \"完成\"\n")
                    append("}\n")
                    append("```\n\n")
                }

                // 添加引用
                if (section % 20 == 0) {
                    append("> 这是章节 $section 的重要引用\n")
                    append(">> 这是二级引用\n")
                    append(">>> 这是三级引用\n\n")
                }
            }
        }

        val parseTime = measureTimeMillis {
            val elements = parser.parse(largeMarkdown)
            assertNotNull(elements)
            assertTrue("大文档解析结果为空", elements.isNotEmpty())
        }

        // 大文档（约10万字符）解析时间应该小于1秒
        assertTrue("大文档解析时间过长: ${parseTime}ms", parseTime < 1000)

        println("大文档解析性能: ${parseTime}ms (文档大小: ${largeMarkdown.length} 字符)")
    }

    @Test
    fun testMemoryUsageCompatibility() {
        // 测试内存使用兼容性
        val runtime = Runtime.getRuntime()

        // 强制垃圾回收
        System.gc()
        Thread.sleep(100)

        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // 解析多个中等大小的文档
        repeat(50) { iteration ->
            val markdown = buildString {
                append("# 内存测试文档 $iteration\n\n")

                repeat(20) { section ->
                    append("## 章节 $section\n\n")
                    append("内容".repeat(100))
                    append("\n\n")

                    append("- [x] 任务 1\n")
                    append("- [ ] 任务 2\n")
                    append("- [x] 任务 3\n\n")

                    append("| 列1 | 列2 | 列3 |\n")
                    append("|-----|-----|-----|\n")
                    append("| 数据1 | 数据2 | 数据3 |\n\n")

                    append("```kotlin\n")
                    append("fun test$section() { println(\"test\") }\n")
                    append("```\n\n")
                }
            }

            val elements = parser.parse(markdown)
            assertNotNull(elements)
        }

        // 强制垃圾回收
        System.gc()
        Thread.sleep(100)

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = finalMemory - initialMemory

        // 内存增长应该控制在合理范围内（小于50MB）
        val maxMemoryIncrease = 50 * 1024 * 1024 // 50MB
        assertTrue("内存使用增长过多: ${memoryIncrease / 1024 / 1024}MB",
                   memoryIncrease < maxMemoryIncrease)

        println("内存使用测试: 增长 ${memoryIncrease / 1024 / 1024}MB")
    }

    @Test
    fun testComplexNestedStructurePerformance() {
        // 测试复杂嵌套结构的性能
        val complexMarkdown = buildString {
            append("# 复杂嵌套结构性能测试\n\n")

            // 深度嵌套列表
            append("## 深度嵌套列表\n\n")
            repeat(6) { level ->
                val indent = "    ".repeat(level)
                append("${indent}- 第${level + 1}级列表\n")
                append("${indent}    - [x] 嵌套任务 1\n")
                append("${indent}    - [ ] 嵌套任务 2\n")
                repeat(3) { item ->
                    append("${indent}        ${item + 1}. 有序子项 ${item + 1}\n")
                }
            }
            append("\n")

            // 多层引用
            append("## 多层引用\n\n")
            repeat(6) { level ->
                val prefix = ">".repeat(level + 1)
                append("$prefix 第${level + 1}级引用包含**加粗**和==高亮==\n")
            }
            append("\n")

            // 复杂表格
            append("## 复杂表格\n\n")
            append("| 项目 | 类型 | 状态 | 优先级 | 说明 |\n")
            append("|:-----|:----:|:----:|:------:|-----:|\n")
            repeat(20) { row ->
                append("| 项目$row | 类型$row | 完成 | 高 | 说明内容$row |\n")
            }
            append("\n")

            // 混合语法
            append("## 混合语法测试\n\n")
            repeat(10) { section ->
                append("### 子章节 $section\n\n")
                append("包含==高亮文本==和普通文本。\n\n")
                append("数学公式：$E=mc^2$ 化学式：H~2~O 指数：2^10^\n\n")
                append("脚注引用[^$section]\n\n")
                append("[^$section]: 脚注内容 $section\n\n")
            }
        }

        val parseTime = measureTimeMillis {
            val elements = parser.parse(complexMarkdown)
            assertNotNull(elements)
            assertTrue("复杂结构解析结果为空", elements.isNotEmpty())
        }

        // 复杂嵌套结构解析时间应该小于500ms
        assertTrue("复杂嵌套结构解析时间过长: ${parseTime}ms", parseTime < 500)

        println("复杂嵌套结构解析性能: ${parseTime}ms")
    }

    @Test
    fun testParseResultCacheCompatibility() {
        // 测试解析结果缓存兼容性
        val markdown = """
            # 缓存测试

            这是用于测试解析缓存的文档。

            - [x] 缓存功能
            - [ ] 性能优化

            ```kotlin
            fun cacheTest() {
                println("缓存测试")
            }
            ```
        """.trimIndent()

        // 第一次解析（冷启动）
        val firstParseTime = measureTimeMillis {
            val elements1 = parser.parse(markdown)
            assertNotNull(elements1)
        }

        // 后续解析（可能命中缓存）
        var totalSubsequentTime = 0L
        val subsequentIterations = 10

        repeat(subsequentIterations) {
            totalSubsequentTime += measureTimeMillis {
                val elements = parser.parse(markdown)
                assertNotNull(elements)
            }
        }

        val avgSubsequentTime = totalSubsequentTime / subsequentIterations

        println("解析缓存测试: 首次${firstParseTime}ms, 后续平均${avgSubsequentTime}ms")

        // 后续解析应该更快（如果有缓存）或至少不会更慢
        assertTrue("缓存机制可能存在问题", avgSubsequentTime <= firstParseTime + 5)
    }

    @Test
    fun testErrorHandlingPerformance() {
        // 测试错误处理的性能影响
        val malformedMarkdown = """
            # 格式错误测试

            - [ 缺少右括号的任务
            - [x 另一个错误任务
            - [] 空任务

            ```kotlin
            没有结束标记的代码块

            | 不完整 | 表格
            |------
            | 缺少 |

            > 不完整引用
            >> 另一个

            ==没有结束的高亮

            [^不完整脚注

            正常内容应该继续解析。

            ## 正常标题

            正常段落文本。
        """.trimIndent()

        val parseTime = measureTimeMillis {
            val elements = parser.parse(malformedMarkdown)
            assertNotNull(elements)
            // 即使有错误，也应该解析出一些正确的元素
            assertTrue("错误处理后没有解析出任何元素", elements.isNotEmpty())
        }

        // 错误处理不应该显著影响性能
        assertTrue("错误处理影响性能过大: ${parseTime}ms", parseTime < 100)

        println("错误处理性能: ${parseTime}ms")
    }

    @Test
    fun testConcurrentParsingCompatibility() {
        // 测试并发解析的兼容性
        val markdown = """
            # 并发测试

            ## 章节内容

            - [x] 任务1
            - [ ] 任务2

            ```kotlin
            fun concurrent() {
                println("并发测试")
            }
            ```

            | 列1 | 列2 |
            |-----|-----|
            | 数据1 | 数据2 |
        """.trimIndent()

        val threads = mutableListOf<Thread>()
        val results = mutableListOf<List<MarkdownElement>>()
        val errors = mutableListOf<Exception>()

        // 创建多个线程同时解析
        repeat(10) { threadId ->
            val thread = Thread {
                try {
                    repeat(10) {
                        val elements = parser.parse(markdown)
                        synchronized(results) {
                            results.add(elements)
                        }
                    }
                } catch (e: Exception) {
                    synchronized(errors) {
                        errors.add(e)
                    }
                }
            }
            threads.add(thread)
        }

        // 启动所有线程
        val startTime = System.currentTimeMillis()
        threads.forEach { it.start() }

        // 等待所有线程完成
        threads.forEach { it.join() }
        val endTime = System.currentTimeMillis()

        // 验证结果
        assertTrue("并发解析出现错误: ${errors.size}", errors.isEmpty())
        assertEquals("并发解析结果数量不正确", 100, results.size)

        // 验证所有结果一致
        val firstResult = results.first()
        results.forEach { result ->
            assertEquals("并发解析结果不一致", firstResult.size, result.size)
        }

        val totalTime = endTime - startTime
        println("并发解析测试: ${totalTime}ms, ${results.size}次解析")

        // 并发解析不应该比串行解析慢太多
        assertTrue("并发解析性能异常: ${totalTime}ms", totalTime < 5000)
    }
}