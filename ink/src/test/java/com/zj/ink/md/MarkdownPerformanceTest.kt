package com.zj.ink.md

import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

/**
 * Markdown解析器和渲染器性能测试
 *
 * 用于验证任务31的性能优化效果，确保新功能不会造成性能回归
 */
class MarkdownPerformanceTest {

    companion object {
        // 性能基准阈值（毫秒）
        private const val SMALL_DOCUMENT_THRESHOLD = 50L
        private const val MEDIUM_DOCUMENT_THRESHOLD = 200L
        private const val LARGE_DOCUMENT_THRESHOLD = 1000L

        // 缓存性能阈值
        private const val CACHE_HIT_THRESHOLD = 10L
    }

    /**
     * 测试小文档解析性能
     */
    @Test
    fun testSmallDocumentPerformance() {
        val smallMarkdown = generateSmallMarkdown()

        val parseTime = measureTimeMillis {
            repeat(100) {
                MarkdownParser.parse(smallMarkdown)
            }
        }

        val averageTime = parseTime / 100
        println("小文档平均解析时间: ${averageTime}ms")

        assertTrue(
            averageTime < SMALL_DOCUMENT_THRESHOLD,
            "小文档解析时间 ${averageTime}ms 超过阈值 ${SMALL_DOCUMENT_THRESHOLD}ms"
        )
    }

    /**
     * 测试中等文档解析性能
     */
    @Test
    fun testMediumDocumentPerformance() {
        val mediumMarkdown = generateMediumMarkdown()

        val parseTime = measureTimeMillis {
            repeat(50) {
                MarkdownParser.parse(mediumMarkdown)
            }
        }

        val averageTime = parseTime / 50
        println("中等文档平均解析时间: ${averageTime}ms")

        assertTrue(
            averageTime < MEDIUM_DOCUMENT_THRESHOLD,
            "中等文档解析时间 ${averageTime}ms 超过阈值 ${MEDIUM_DOCUMENT_THRESHOLD}ms"
        )
    }

    /**
     * 测试大文档解析性能
     */
    @Test
    fun testLargeDocumentPerformance() {
        val largeMarkdown = generateLargeMarkdown()

        val parseTime = measureTimeMillis {
            repeat(10) {
                MarkdownParser.parse(largeMarkdown)
            }
        }

        val averageTime = parseTime / 10
        println("大文档平均解析时间: ${averageTime}ms")

        assertTrue(
            averageTime < LARGE_DOCUMENT_THRESHOLD,
            "大文档解析时间 ${averageTime}ms 超过阈值 ${LARGE_DOCUMENT_THRESHOLD}ms"
        )
    }

    /**
     * 测试缓存性能
     */
    @Test
    fun testCachePerformance() {
        val markdown = generateMediumMarkdown()

        // 第一次解析（冷启动）
        val firstParseTime = measureTimeMillis {
            MarkdownParser.parse(markdown)
        }

        // 第二次解析（缓存命中）
        val secondParseTime = measureTimeMillis {
            MarkdownParser.parse(markdown)
        }

        println("首次解析时间: ${firstParseTime}ms")
        println("缓存命中时间: ${secondParseTime}ms")
        println("缓存性能提升: ${((firstParseTime - secondParseTime).toFloat() / firstParseTime * 100).toInt()}%")

        assertTrue(
            secondParseTime < CACHE_HIT_THRESHOLD,
            "缓存命中时间 ${secondParseTime}ms 超过阈值 ${CACHE_HIT_THRESHOLD}ms"
        )

        assertTrue(
            secondParseTime < firstParseTime,
            "缓存性能没有提升"
        )
    }

    /**
     * 测试复杂嵌套结构性能
     */
    @Test
    fun testComplexNestedStructurePerformance() {
        val complexMarkdown = generateComplexNestedMarkdown()

        val parseTime = measureTimeMillis {
            repeat(20) {
                MarkdownParser.parse(complexMarkdown)
            }
        }

        val averageTime = parseTime / 20
        println("复杂嵌套结构平均解析时间: ${averageTime}ms")

        assertTrue(
            averageTime < MEDIUM_DOCUMENT_THRESHOLD,
            "复杂嵌套结构解析时间 ${averageTime}ms 超过阈值 ${MEDIUM_DOCUMENT_THRESHOLD}ms"
        )
    }

    /**
     * 测试正则表达式优化效果
     */
    @Test
    fun testRegexOptimizationPerformance() {
        val regexHeavyMarkdown = generateRegexHeavyMarkdown()

        val parseTime = measureTimeMillis {
            repeat(30) {
                MarkdownParser.parse(regexHeavyMarkdown)
            }
        }

        val averageTime = parseTime / 30
        println("正则表达式密集文档平均解析时间: ${averageTime}ms")

        assertTrue(
            averageTime < MEDIUM_DOCUMENT_THRESHOLD,
            "正则表达式密集文档解析时间 ${averageTime}ms 超过阈值 ${MEDIUM_DOCUMENT_THRESHOLD}ms"
        )
    }

    /**
     * 测试内存使用情况
     */
    @Test
    fun testMemoryUsage() {
        // 清理垃圾回收
        System.gc()
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        // 解析大量文档
        repeat(100) {
            val markdown = generateMediumMarkdown()
            MarkdownParser.parse(markdown)
        }

        // 再次检查内存
        System.gc()
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        val memoryIncrease = (finalMemory - initialMemory) / (1024 * 1024) // MB
        println("内存增长: ${memoryIncrease}MB")

        assertTrue(
            memoryIncrease < 50, // 限制内存增长在50MB以内
            "内存增长 ${memoryIncrease}MB 超过预期"
        )
    }

    /**
     * 测试缓存清理功能
     */
    @Test
    fun testCacheClearPerformance() {
        // 填充缓存
        repeat(60) { index ->
            val markdown = "# Test $index\nContent $index"
            MarkdownParser.parse(markdown)
        }

        val clearTime = measureTimeMillis {
            MarkdownParser.clearCache()
        }

        println("缓存清理时间: ${clearTime}ms")

        assertTrue(
            clearTime < 10L,
            "缓存清理时间 ${clearTime}ms 超过预期"
        )
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成小文档测试数据
     */
    private fun generateSmallMarkdown(): String {
        return """
            # 测试标题

            这是一个**加粗**文本和*斜体*文本。

            - [ ] 未完成任务
            - [x] 已完成任务

            `inline code` 和 ==高亮文本==
        """.trimIndent()
    }

    /**
     * 生成中等文档测试数据
     */
    private fun generateMediumMarkdown(): String {
        return buildString {
            // 标题层级测试
            append("# H1标题\n")
            append("## H2标题\n")
            append("### H3标题\n")
            append("#### H4标题\n")
            append("##### H5标题\n")
            append("###### H6标题\n\n")

            // 段落和格式化文本
            repeat(5) { i ->
                append("这是第${i + 1}个段落，包含**加粗**、*斜体*、~~删除线~~和==高亮==文本。\n\n")
            }

            // 任务列表
            append("## 任务列表\n")
            repeat(10) { i ->
                val checked = if (i % 2 == 0) "x" else " "
                append("- [$checked] 任务项目 $i\n")
            }
            append("\n")

            // 嵌套列表
            append("## 嵌套列表\n")
            append("1. 一级有序列表\n")
            append("    - 二级无序列表\n")
            append("        1. 三级有序列表\n")
            append("            - [ ] 四级任务列表\n")
            append("            - [x] 四级任务列表已完成\n\n")

            // 表格
            append("## 表格测试\n")
            append("| 左对齐 | 居中对齐 | 右对齐 |\n")
            append("| :--- | :---: | ---: |\n")
            repeat(5) { i ->
                append("| 数据${i + 1} | 数据${i + 1} | 数据${i + 1} |\n")
            }
            append("\n")

            // 代码块
            append("## 代码块\n")
            append("```kotlin\n")
            append("fun main() {\n")
            append("    println(\"Hello, World!\")\n")
            append("    val list = listOf(1, 2, 3)\n")
            append("    list.forEach { println(it) }\n")
            append("}\n")
            append("```\n\n")

            // 引用
            append("## 多级引用\n")
            append("> 一级引用\n")
            append(">> 二级引用\n")
            append(">>> 三级引用\n\n")

            // 扩展语法
            append("## 扩展语法\n")
            append("脚注引用[^1]和数学公式$E=mc^2$\n")
            append("上标X^2^和下标H~2~O\n\n")
            append("[^1]: 这是脚注内容\n")
        }
    }

    /**
     * 生成大文档测试数据
     */
    private fun generateLargeMarkdown(): String {
        return buildString {
            repeat(50) { section ->
                append("# 章节 ${section + 1}\n\n")

                repeat(20) { para ->
                    append("这是第${section + 1}章节的第${para + 1}个段落。")
                    append("包含**加粗**、*斜体*、~~删除线~~、==高亮==、`代码`等格式。")
                    append("还有链接[链接文本](https://example.com)和脚注[^${section}_${para}]。\n\n")
                }

                // 添加列表
                append("## 列表内容\n")
                repeat(15) { item ->
                    val indent = "    ".repeat(item % 3)
                    val marker = when (item % 3) {
                        0 -> "- [ ]"
                        1 -> "- [x]"
                        else -> "${item + 1}."
                    }
                    append("$indent$marker 列表项目 $item\n")
                }
                append("\n")

                // 添加表格
                if (section % 5 == 0) {
                    append("## 数据表格\n")
                    append("| 列1 | 列2 | 列3 | 列4 |\n")
                    append("| :--- | :---: | ---: | --- |\n")
                    repeat(10) { row ->
                        append("| 数据${row}1 | 数据${row}2 | 数据${row}3 | 数据${row}4 |\n")
                    }
                    append("\n")
                }

                // 添加脚注定义
                repeat(20) { para ->
                    append("[^${section}_${para}]: 这是第${section + 1}章节第${para + 1}段的脚注内容。\n")
                }
                append("\n")
            }
        }
    }

    /**
     * 生成复杂嵌套结构测试数据
     */
    private fun generateComplexNestedMarkdown(): String {
        return buildString {
            append("# 复杂嵌套结构测试\n\n")

            // 深层嵌套列表
            repeat(6) { level ->
                val indent = "    ".repeat(level)
                when (level % 3) {
                    0 -> append("${indent}- 无序列表层级 $level\n")
                    1 -> append("${indent}1. 有序列表层级 $level\n")
                    2 -> append("${indent}- [${if (level % 2 == 0) "x" else " "}] 任务列表层级 $level\n")
                }
            }
            append("\n")

            // 嵌套引用
            repeat(5) { level ->
                val prefix = ">".repeat(level + 1)
                append("$prefix 引用层级 ${level + 1}\n")
            }
            append("\n")

            // 表格中的复杂内容
            append("| 复杂内容 | 格式化文本 |\n")
            append("| :--- | :--- |\n")
            append("| **加粗** *斜体* | ==高亮== ~~删除~~ |\n")
            append("| `代码` [链接](url) | 上标^2^ 下标~2~ |\n")
            append("| 数学$E=mc^2$ | 脚注[^complex] |\n\n")

            append("[^complex]: 复杂表格脚注\n")
        }
    }

    /**
     * 生成正则表达式密集测试数据
     */
    private fun generateRegexHeavyMarkdown(): String {
        return buildString {
            append("# 正则表达式密集测试\n\n")

            // 大量脚注
            repeat(50) { i ->
                append("文本内容包含脚注[^$i]")
                if (i % 10 == 9) append("\n\n")
            }

            // 大量数学公式
            repeat(30) { i ->
                append("内联公式\$x^$i + y^$i = z^$i\$ ")
                if (i % 5 == 4) append("\n")
            }
            append("\n\n")

            // 大量上下标
            repeat(40) { i ->
                append("上标X^$i^ 下标H~$i~O ")
                if (i % 8 == 7) append("\n")
            }
            append("\n\n")

            // 脚注定义
            repeat(50) { i ->
                append("[^$i]: 脚注内容 $i\n")
            }
        }
    }
}