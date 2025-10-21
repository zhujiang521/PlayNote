package com.zj.ink.md

import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Markdown兼容性集成测试
 * 端到端测试，确保整个Markdown处理流程的兼容性
 */
class MarkdownCompatibilityIntegrationTest {

    private val parser = MarkdownParser()

    // ================================
    // 1. 端到端兼容性测试
    // ================================

    @Test
    fun testEndToEndCompatibility() {
        // 完整的端到端测试文档
        val comprehensiveMarkdown = """
            # PlayNote Markdown兼容性测试文档

            这个文档用于测试PlayNote项目中Markdown解析和渲染的完整兼容性。

            ## 1. 基础语法兼容性

            ### 1.1 文本格式
            这是包含**加粗文本**、*斜体文本*、~~删除线文本~~和`内联代码`的段落。

            ### 1.2 标题层级测试
            #### H4级标题
            ##### H5级标题
            ###### H6级标题

            ### 1.3 链接和图片
            [外部链接](https://example.com) 和 ![示例图片](image.jpg "图片标题")

            ## 2. 列表功能兼容性

            ### 2.1 传统列表
            - 无序列表项1
            - 无序列表项2
                - 嵌套无序列表项1
                - 嵌套无序列表项2
                    1. 深层嵌套有序列表项1
                    2. 深层嵌套有序列表项2

            1. 有序列表项1
            2. 有序列表项2
                - 混合嵌套无序列表项
                - 另一个混合嵌套项

            ### 2.2 任务列表（新功能）
            - [x] 已完成的基础任务
            - [ ] 未完成的基础任务
            - [x] 已完成的复杂任务
                - [ ] 嵌套的未完成任务
                - [x] 嵌套的已完成任务
                    - [ ] 深层嵌套任务
                    - [x] 另一个深层嵌套任务

            ## 3. 表格功能兼容性

            ### 3.1 基础表格
            | 功能 | 状态 | 说明 |
            |------|------|------|
            | 基础解析 | ✅ | 完全支持 |
            | 渲染显示 | ✅ | 正常工作 |

            ### 3.2 列对齐表格（新功能）
            | 左对齐功能 | 居中对齐功能 | 右对齐功能 |
            |:-----------|:------------:|----------:|
            | 解析器支持 | ✅ 完全支持 | 100% |
            | 渲染器支持 | ✅ 完全支持 | 100% |
            | 兼容性 | ✅ 向后兼容 | 100% |

            ## 4. 代码功能兼容性

            ### 4.1 内联代码
            使用 `parser.parse()` 方法解析Markdown文本。

            ### 4.2 普通代码块
            ```
            // 不带语言标记的代码块
            function basicExample() {
                console.log("基础代码块测试");
            }
            ```

            ### 4.3 语法高亮代码块（新功能）
            ```kotlin
            // Kotlin语法高亮测试
            fun parseMarkdown(text: String): List<MarkdownElement> {
                return MarkdownParser().parse(text)
            }

            data class TestResult(
                val success: Boolean,
                val message: String
            )
            ```

            ```java
            // Java语法高亮测试
            public class MarkdownTest {
                public static void main(String[] args) {
                    System.out.println("Java语法高亮测试");
                }
            }
            ```

            ```javascript
            // JavaScript语法高亮测试
            function testCompatibility() {
                const result = {
                    parsing: true,
                    rendering: true,
                    compatibility: "100%"
                };
                return result;
            }
            ```

            ## 5. 引用功能兼容性

            ### 5.1 单级引用
            > 这是一个标准的引用文本，用于测试基础引用功能的兼容性。

            ### 5.2 多级引用（新功能）
            > 第一级引用
            >> 第二级引用包含**加粗文本**
            >>> 第三级引用包含==高亮文本==
            >>>> 第四级引用包含`内联代码`
            >>>>> 第五级引用测试
            >>>>>> 第六级引用测试

            ## 6. 扩展语法兼容性

            ### 6.1 高亮文本（新功能）
            这个段落包含==重要的高亮文本==和普通文本的混合。
            高亮功能应该==完全兼容==现有的文本格式。

            ### 6.2 数学和化学公式（新功能）

            #### 数学公式
            著名的质能方程：$E=mc^2$

            更复杂的数学表达式：$$\sum_{i=1}^{n} x_i = x_1 + x_2 + \cdots + x_n$$

            #### 化学公式
            - 水分子：H~2~O
            - 二氧化碳：CO~2~
            - 硫酸：H~2~SO~4~

            #### 数学指数
            - 平方：X^2^
            - 立方：Y^3^
            - 复杂指数：(a+b)^(n+1)^

            ### 6.3 脚注系统（新功能）

            这个文档包含多个脚注引用[^1]，用于测试脚注功能的兼容性[^compatibility]。
            脚注应该能够正确解析和渲染[^render]。

            [^1]: 这是第一个脚注，测试基础脚注功能
            [^compatibility]: 兼容性脚注，确保与现有功能不冲突
            [^render]: 渲染脚注，测试在不同渲染器中的表现

            ## 7. 转义字符兼容性

            ### 7.1 基础转义
            转义的Markdown语法字符：
            - 星号：\*不是加粗\*
            - 井号：\# 不是标题
            - 方括号：\[不是链接\]
            - 圆括号：\(不是链接\)
            - 下划线：\_不是斜体\_
            - 反引号：\`不是代码\`
            - 波浪号：\~不是删除线\~
            - 反斜杠：\\\ 显示反斜杠

            ### 7.2 复杂转义
            混合使用：**加粗中包含\*转义星号\***和*斜体中包含\_转义下划线\_*。

            ## 8. 复杂混合内容测试

            ### 8.1 表格中的复杂内容
            | 元素类型 | 语法示例 | 兼容性状态 |
            |:---------|:--------:|----------:|
            | **加粗** | `**文本**` | ✅ 完全兼容 |
            | ==高亮== | `==文本==` | ✅ 新功能 |
            | `代码` | `` `代码` `` | ✅ 完全兼容 |
            | 数学公式 | `$公式$` | ✅ 新功能 |

            ### 8.2 引用中的复杂内容
            > 这个引用包含多种格式：
            > - **加粗文本**
            > - *斜体文本*
            > - ==高亮文本==
            > - `内联代码`
            > - 数学公式：$a^2 + b^2 = c^2$
            > 
            >> 二级引用也支持复杂内容：
            >> - [x] 任务列表项
            >> - [ ] 未完成任务
            >> 
            >>> 三级引用中的表格：
            >>> 
            >>> | 项目 | 状态 |
            >>> |------|------|
            >>> | 解析 | ✅ |
            >>> | 渲染 | ✅ |

            ### 8.3 列表中的复杂内容
            - 第一项包含**加粗**和==高亮==
                - 嵌套项包含`代码`和数学公式：$E=mc^2$
                - [x] 嵌套任务：实现H~2~O分子式渲染
                - [ ] 另一个嵌套任务：支持X^2^指数显示

            1. 有序列表项包含链接：[GitHub](https://github.com)
            2. 包含脚注引用的项目[^complex]
            3. 包含转义字符的项目：\*不是加粗\*

            [^complex]: 复杂内容脚注，测试脚注在复杂环境中的表现

            ## 9. 性能兼容性验证

            这个文档包含了大量不同类型的Markdown元素，用于测试：
            - 解析性能：应该在合理时间内完成解析
            - 内存使用：不应该造成内存泄漏
            - 渲染性能：应该能够流畅渲染
            - 兼容性：新旧功能应该完全兼容

            ## 10. 总结

            ### 10.1 兼容性检查清单
            - [x] 基础Markdown语法：完全向后兼容
            - [x] H1-H6标题：扩展支持H4-H6
            - [x] 任务列表：新功能，支持嵌套
            - [x] 表格列对齐：新功能，向后兼容
            - [x] 代码语法高亮：新功能，向后兼容
            - [x] 多级引用：扩展支持，向后兼容
            - [x] 多级列表：扩展支持，向后兼容
            - [x] 高亮文本：新功能
            - [x] 数学公式：新功能
            - [x] 上下标：新功能
            - [x] 脚注：新功能
            - [x] 转义字符：增强支持

            ### 10.2 渲染器兼容性
            - [x] Compose渲染器：支持所有新功能
            - [x] Glance渲染器：适配小组件环境
            - [x] 主题兼容性：支持日间/夜间模式
            - [x] 屏幕适配：响应式布局

            ### 10.3 性能兼容性
            - [x] 解析性能：无回归，新功能高效
            - [x] 内存管理：优化内存使用
            - [x] 缓存机制：支持解析结果缓存
            - [x] 错误处理：优雅处理异常输入

            这个综合测试文档验证了PlayNote Markdown功能的完整兼容性。
            所有新增功能都保持了向后兼容，没有破坏性变更。
        """.trimIndent()

        // 执行端到端测试
        val parseTime = measureTimeMillis {
            val elements = parser.parse(comprehensiveMarkdown)

            // 验证解析结果完整性
            assertNotNull("综合文档应该能解析", elements)
            assertTrue("应该解析出大量元素", elements.size > 50)

            // 验证所有元素类型都存在
            val elementTypes = elements.map { it::class.java.simpleName }.toSet()

            val expectedTypes = setOf(
                "Heading", "Paragraph", "Bold", "Italic", "Strikethrough",
                "InlineCode", "Link", "Image", "UnorderedList", "OrderedList",
                "TaskList", "Table", "CodeBlock", "BlockQuote", "Highlight",
                "Math", "Superscript", "Subscript", "Footnote"
            )

            expectedTypes.forEach { expectedType ->
                assertTrue("综合文档应包含 $expectedType",
                           elementTypes.contains(expectedType))
            }

            // 验证关键功能的数据完整性

            // 验证H1-H6标题
            val headings = elements.filter { it is MarkdownElement.Heading }
                .map { it as MarkdownElement.Heading }
            val headingLevels = headings.map { it.level }.toSet()
            assertTrue("应该包含H1-H6所有层级",
                       headingLevels.containsAll(listOf(1, 2, 3, 4, 5, 6)))

            // 验证任务列表
            val taskLists = elements.filter { it is MarkdownElement.TaskList }
                .map { it as MarkdownElement.TaskList }
            assertTrue("应该有任务列表", taskLists.isNotEmpty())

            val hasCompletedTask = taskLists.any { taskList ->
                taskList.items.any { it.second }
            }
            val hasIncompleteTask = taskLists.any { taskList ->
                taskList.items.any { !it.second }
            }
            assertTrue("应该有已完成任务", hasCompletedTask)
            assertTrue("应该有未完成任务", hasIncompleteTask)

            // 验证表格对齐
            val tables = elements.filter { it is MarkdownElement.Table }
                .map { it as MarkdownElement.Table }
            assertTrue("应该有表格", tables.isNotEmpty())

            val hasAlignedTable = tables.any { table ->
                table.alignments.isNotEmpty() &&
                table.alignments.any { it != MarkdownElement.TableAlignment.LEFT }
            }
            assertTrue("应该有对齐表格", hasAlignedTable)

            // 验证代码块语言支持
            val codeBlocks = elements.filter { it is MarkdownElement.CodeBlock }
                .map { it as MarkdownElement.CodeBlock }
            assertTrue("应该有代码块", codeBlocks.isNotEmpty())

            val hasLanguageCode = codeBlocks.any { it.language.isNotEmpty() }
            assertTrue("应该有带语言标记的代码块", hasLanguageCode)

            // 验证多级引用
            val quotes = elements.filter { it is MarkdownElement.BlockQuote }
                .map { it as MarkdownElement.BlockQuote }
            assertTrue("应该有引用", quotes.isNotEmpty())

            val quoteLevels = quotes.map { it.level }.toSet()
            assertTrue("应该有多级引用", quoteLevels.size > 1)
            assertTrue("应该有深层引用", quoteLevels.any { it >= 3 })

            // 验证扩展语法
            assertTrue("应该有高亮文本",
                       elements.any { it is MarkdownElement.Highlight })
            assertTrue("应该有数学公式",
                       elements.any { it is MarkdownElement.Math })
            assertTrue("应该有上标",
                       elements.any { it is MarkdownElement.Superscript })
            assertTrue("应该有下标",
                       elements.any { it is MarkdownElement.Subscript })
            assertTrue("应该有脚注",
                       elements.any { it is MarkdownElement.Footnote })
        }

        // 验证性能
        assertTrue("综合文档解析时间应合理: ${parseTime}ms", parseTime < 2000)

        println("端到端兼容性测试完成: ${parseTime}ms")
    }

    @Test
    fun testBackwardCompatibilityRegression() {
        // 回归测试：确保所有原有功能仍然正常工作
        val legacyMarkdown = """
            # 传统Markdown文档

            ## 基础功能测试

            这是一个使用传统Markdown语法的文档，应该完全兼容。

            ### 文本格式
            包含**加粗**、*斜体*、~~删除线~~和`内联代码`。

            ### 列表
            - 无序列表项1
            - 无序列表项2

            1. 有序列表项1
            2. 有序列表项2

            ### 链接和图片
            [链接文本](https://example.com)
            ![图片](image.jpg)

            ### 代码块
            ```
            function example() {
                console.log("传统代码块");
            }
            ```

            ### 引用
            > 这是传统的引用文本

            ### 表格
            | 列1 | 列2 |
            |-----|-----|
            | 数据1 | 数据2 |
        """.trimIndent()

        val elements = parser.parse(legacyMarkdown)

        // 验证所有传统元素都正确解析
        assertNotNull("传统文档应该正确解析", elements)
        assertTrue("应该解析出元素", elements.isNotEmpty())

        // 验证基础元素类型
        val elementTypes = elements.map { it::class.java.simpleName }.toSet()

        val legacyTypes = setOf(
            "Heading", "Paragraph", "Bold", "Italic", "Strikethrough",
            "InlineCode", "Link", "Image", "UnorderedList", "OrderedList",
            "CodeBlock", "BlockQuote", "Table"
        )

        legacyTypes.forEach { legacyType ->
            assertTrue("传统功能 $legacyType 应该正常工作",
                       elementTypes.contains(legacyType))
        }

        // 验证数据结构兼容性
        val heading = elements.find { it is MarkdownElement.Heading } as? MarkdownElement.Heading
        if (heading != null) {
            assertTrue("标题级别应在1-3范围", heading.level in 1..3)
        }

        val table = elements.find { it is MarkdownElement.Table } as? MarkdownElement.Table
        if (table != null) {
            // 新增的alignments字段应该有默认值
            assertNotNull("表格应该有alignments字段", table.alignments)
        }

        val codeBlock = elements.find { it is MarkdownElement.CodeBlock } as? MarkdownElement.CodeBlock
        if (codeBlock != null) {
            // 新增的language字段应该有默认值
            assertNotNull("代码块应该有language字段", codeBlock.language)
        }
    }

    @Test
    fun testRealWorldCompatibility() {
        // 真实世界的兼容性测试：使用实际项目中可能出现的复杂文档
        val realWorldMarkdown = """
            # PlayNote 项目文档

            ## 概述

            PlayNote 是一个功能强大的Android便签应用，支持：
            - [x] Markdown解析和渲染
            - [x] 手绘功能
            - [ ] 云同步功能
            - [ ] 多媒体支持

            ## 技术架构

            ### 核心模块

            | 模块 | 功能 | 状态 |
            |:-----|:----:|-----:|
            | Parser | Markdown解析 | ✅ 完成 |
            | Renderer | UI渲染 | ✅ 完成 |
            | Storage | 数据存储 | ⏳ 开发中 |

            ### 代码示例

            #### Kotlin实现
            ```kotlin
            class MarkdownParser {
                fun parse(text: String): List<MarkdownElement> {
                    // 解析逻辑
                    return parseElements(text)
                }

                private fun parseElements(text: String): List<MarkdownElement> {
                    val elements = mutableListOf<MarkdownElement>()
                    // 具体实现...
                    return elements
                }
            }
            ```

            #### 使用示例
            ```kotlin
            val parser = MarkdownParser()
            val markdown = "# 标题\n\n- [x] 任务"
            val elements = parser.parse(markdown)

            elements.forEach { element ->
                when (element) {
                    is MarkdownElement.Heading -> renderHeading(element)
                    is MarkdownElement.TaskList -> renderTaskList(element)
                    else -> renderDefault(element)
                }
            }
            ```

            ## 功能详解

            ### 任务列表功能

            支持复杂的嵌套任务列表：

            - [x] 基础功能开发
                - [x] 数据模型设计
                - [x] 解析器实现
                - [ ] 渲染器实现
                    - [x] Compose渲染器
                    - [ ] Glance渲染器

            ### 数学公式支持

            #### 物理公式
            - 牛顿第二定律：$F = ma$
            - 爱因斯坦质能方程：$E = mc^2$
            - 动能公式：$E_k = \frac{1}{2}mv^2$

            #### 化学公式
            - 水的分子式：H~2~O
            - 葡萄糖：C~6~H~12~O~6~
            - 硫酸：H~2~SO~4~

            ### 高级引用

            > **重要提示**：这个项目使用了最新的Markdown扩展语法
            > 
            > 包括以下新功能：
            > - 任务列表
            > - 表格对齐
            > - 语法高亮
            > - 数学公式
            > 
            >> **技术细节**：
            >> 
            >> 所有新功能都保持==向后兼容==，不会破坏现有功能。
            >> 
            >>> **性能优化**：
            >>> 
            >>> 新的解析器采用了多项优化技术：
            >>> 1. 正则表达式预编译
            >>> 2. 结果缓存机制
            >>> 3. 内存优化策略

            ## API文档

            ### 核心接口

            ```kotlin
            interface MarkdownRenderer {
                fun render(elements: List<MarkdownElement>): Unit
            }

            class ComposeMarkdownRenderer : MarkdownRenderer {
                override fun render(elements: List<MarkdownElement>) {
                    elements.forEach { element ->
                        RenderElement(element)
                    }
                }
            }
            ```

            ### 扩展点

            开发者可以通过以下方式扩展功能：

            1. **自定义元素类型**
            ```kotlin
            sealed class CustomElement : MarkdownElement {
                data class Diagram(val content: String) : CustomElement()
                data class Chart(val data: List<Int>) : CustomElement()
            }
            ```

            2. **自定义渲染器**
            ```kotlin
            class CustomRenderer : MarkdownRenderer {
                // 自定义渲染逻辑
            }
            ```

            ## 测试策略

            ### 单元测试
            - [x] 解析器测试：覆盖率 >95%
            - [x] 渲染器测试：覆盖率 >90%
            - [ ] 集成测试：覆盖率 >85%

            ### 性能测试
            - [x] 小文档解析：<50ms
            - [x] 中等文档解析：<200ms
            - [x] 大文档解析：<1s

            ### 兼容性测试
            - [x] Android API 21+
            - [x] Kotlin 1.8+
            - [x] Compose 1.5+

            ## 发布说明

            ### 版本 2.0.0

            #### 新功能 ✨
            - 支持H4-H6标题
            - 任务列表功能
            - 表格列对齐
            - 代码语法高亮
            - 多级引用和列表
            - 高亮文本
            - 数学公式和上下标
            - 脚注系统

            #### 改进 🚀
            - 解析性能提升30%
            - 内存使用优化
            - 错误处理增强

            #### 修复 🐛
            - 修复嵌套列表解析问题
            - 修复表格边界情况
            - 修复特殊字符处理

            ### 迁移指南

            从1.x版本升级到2.0.0：

            1. **无破坏性变更**：所有现有代码继续工作
            2. **新功能可选**：可以渐进式采用新功能
            3. **性能提升**：自动获得性能改进

            ## 贡献指南

            ### 开发环境设置

            ```bash
            # 克隆项目
            git clone https://github.com/example/playnote.git
            cd playnote

            # 构建项目
            ./gradlew build

            # 运行测试
            ./gradlew test
            ```

            ### 代码规范

            - 遵循Kotlin官方代码风格
            - 使用有意义的变量和函数名
            - 添加适当的注释和文档
            - 编写单元测试

            ## 致谢

            感谢所有贡献者的努力工作！特别感谢：

            - **核心开发团队**：负责架构设计和核心功能实现
            - **测试团队**：确保代码质量和兼容性
            - **文档团队**：编写和维护项目文档
            - **社区贡献者**：提供反馈和建议

            ## 许可证

            本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

            ---

            **注意**：这个文档本身就是一个==兼容性测试==的例子，包含了所有支持的Markdown语法。
            如果你能正确看到这个文档的渲染效果，说明兼容性测试通过！✅

            更多信息请参考[^docs]和[^api]。

            [^docs]: 完整文档：https://docs.example.com
            [^api]: API参考：https://api.example.com
        """.trimIndent()

        val parseTime = measureTimeMillis {
            val elements = parser.parse(realWorldMarkdown)

            // 验证真实世界文档的完整解析
            assertNotNull("真实世界文档应该能解析", elements)
            assertTrue("应该解析出大量元素", elements.size > 100)

            // 验证包含所有类型的元素
            val elementTypes = elements.map { it::class.java.simpleName }.toSet()

            // 应该包含所有主要元素类型
            val majorTypes = listOf(
                "Heading", "Paragraph", "TaskList", "Table", "CodeBlock",
                "BlockQuote", "Math", "Superscript", "Subscript", "Footnote",
                "Highlight", "Bold", "Italic", "Link"
            )

            majorTypes.forEach { majorType ->
                assertTrue("真实文档应包含 $majorType",
                           elementTypes.contains(majorType))
            }
        }

        // 真实世界文档的解析时间应该合理
        assertTrue("真实世界文档解析时间应合理: ${parseTime}ms", parseTime < 3000)

        println("真实世界兼容性测试完成: ${parseTime}ms")
    }

    @Test
    fun testCompatibilityStressTest() {
        // 压力测试：大量重复内容的兼容性
        val stressMarkdown = buildString {
            append("# 兼容性压力测试\n\n")

            repeat(100) { i ->
                append("## 章节 $i\n\n")

                // 任务列表
                append("### 任务列表 $i\n")
                repeat(10) { j ->
                    val checked = if (j % 2 == 0) "x" else " "
                    append("- [$checked] 任务 $i.$j\n")
                }
                append("\n")

                // 表格
                append("### 数据表 $i\n")
                append("| 项目 | 值 | 状态 |\n")
                append("|:-----|:--:|-----:|\n")
                repeat(5) { j ->
                    append("| 项目$i.$j | 值$j | 状态$j |\n")
                }
                append("\n")

                // 代码块
                append("### 代码示例 $i\n")
                append("```kotlin\n")
                append("fun example$i() {\n")
                append("    println(\"示例 $i\")\n")
                append("}\n")
                append("```\n\n")

                // 复杂内容
                if (i % 10 == 0) {
                    append("#### 复杂混合内容\n")
                    append("包含==高亮文本==和数学公式$E=mc^2$的段落。\n")
                    append("化学式H~2~O和指数X^2^的混合使用。\n")
                    append("脚注引用[^stress$i]测试。\n\n")
                    append("[^stress$i]: 压力测试脚注 $i\n\n")
                }
            }
        }

        val parseTime = measureTimeMillis {
            val elements = parser.parse(stressMarkdown)

            assertNotNull("压力测试文档应该能解析", elements)
            assertTrue("应该解析出大量元素", elements.size > 1000)

            // 验证不同类型元素的数量合理
            val taskLists = elements.count { it is MarkdownElement.TaskList }
            val tables = elements.count { it is MarkdownElement.Table }
            val codeBlocks = elements.count { it is MarkdownElement.CodeBlock }

            assertTrue("应该有大量任务列表", taskLists >= 100)
            assertTrue("应该有大量表格", tables >= 100)
            assertTrue("应该有大量代码块", codeBlocks >= 100)
        }

        // 压力测试的解析时间应该在可接受范围内
        assertTrue("压力测试解析时间应合理: ${parseTime}ms", parseTime < 5000)

        println("兼容性压力测试完成: ${parseTime}ms")
    }
}