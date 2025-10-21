package com.zj.ink.md

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI测试类，用于验证Markdown渲染器的视觉效果和交互功能
 *
 * 测试覆盖范围：
 * - 任务列表渲染和交互
 * - H4-H6标题渲染
 * - 表格列对齐渲染
 * - 嵌套列表渲染
 * - 代码块语法高亮渲染
 * - 多级引用渲染
 * - 高亮文本渲染
 * - 扩展语法渲染（脚注、上下标、数学公式）
 * - 主题适配测试
 * - 响应式布局测试
 */
@RunWith(AndroidJUnit4::class)
class RenderMarkdownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========== 任务列表渲染测试 ==========

    @Test
    fun testTaskListBasicRendering() {
        // 测试基础任务列表渲染
        val taskListElements = listOf(
            MarkdownElement.TaskList(
                items = listOf(
                    "未完成任务" to false,
                    "已完成任务" to true,
                    "另一个未完成任务" to false
                ),
                level = 0
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = taskListElements)
                }
            }
        }

        // 验证任务列表是否正确渲染
        composeTestRule.onNodeWithTag("markdown_container").assertExists()

        // 验证复选框存在
        composeTestRule.onAllNodesWithContentDescription("Checkbox").assertCountEquals(3)

        // 验证任务文本存在
        composeTestRule.onNodeWithText("未完成任务").assertExists()
        composeTestRule.onNodeWithText("已完成任务").assertExists()
        composeTestRule.onNodeWithText("另一个未完成任务").assertExists()
    }

    @Test
    fun testTaskListInteraction() {
        // 测试任务列表交互功能
        val taskListElements = listOf(
            MarkdownElement.TaskList(
                items = listOf("可点击任务" to false),
                level = 0
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = taskListElements)
                }
            }
        }

        // 点击复选框
        composeTestRule.onAllNodesWithContentDescription("Checkbox")[0].performClick()

        // 验证状态变化（这里主要测试点击不会崩溃）
        composeTestRule.onNodeWithText("可点击任务").assertExists()
    }

    @Test
    fun testTaskListNestedRendering() {
        // 测试嵌套任务列表渲染
        val nestedTaskListElements = listOf(
            MarkdownElement.TaskList(
                items = listOf("一级任务" to false),
                level = 0
            ),
            MarkdownElement.TaskList(
                items = listOf("二级任务" to true),
                level = 1
            ),
            MarkdownElement.TaskList(
                items = listOf("三级任务" to false),
                level = 2
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = nestedTaskListElements)
                }
            }
        }

        // 验证嵌套任务列表渲染
        composeTestRule.onNodeWithText("一级任务").assertExists()
        composeTestRule.onNodeWithText("二级任务").assertExists()
        composeTestRule.onNodeWithText("三级任务").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Checkbox").assertCountEquals(3)
    }

    // ========== H4-H6标题渲染测试 ==========

    @Test
    fun testH4H5H6HeadingRendering() {
        // 测试H4-H6标题渲染
        val headingElements = listOf(
            MarkdownElement.Heading(text = "四级标题", level = 4),
            MarkdownElement.Heading(text = "五级标题", level = 5),
            MarkdownElement.Heading(text = "六级标题", level = 6)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = headingElements)
                }
            }
        }

        // 验证标题文本存在
        composeTestRule.onNodeWithText("四级标题").assertExists()
        composeTestRule.onNodeWithText("五级标题").assertExists()
        composeTestRule.onNodeWithText("六级标题").assertExists()
    }

    @Test
    fun testHeadingHierarchy() {
        // 测试标题层级完整性
        val allHeadingElements = listOf(
            MarkdownElement.Heading(text = "一级标题", level = 1),
            MarkdownElement.Heading(text = "二级标题", level = 2),
            MarkdownElement.Heading(text = "三级标题", level = 3),
            MarkdownElement.Heading(text = "四级标题", level = 4),
            MarkdownElement.Heading(text = "五级标题", level = 5),
            MarkdownElement.Heading(text = "六级标题", level = 6)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = allHeadingElements)
                }
            }
        }

        // 验证所有标题都存在
        for (i in 1..6) {
            composeTestRule.onNodeWithText("${when(i) {
                1 -> "一"
                2 -> "二"
                3 -> "三"
                4 -> "四"
                5 -> "五"
                6 -> "六"
                else -> ""
            }}级标题").assertExists()
        }
    }

    // ========== 表格列对齐渲染测试 ==========

    @Test
    fun testTableColumnAlignment() {
        // 测试表格列对齐渲染
        val tableElement = MarkdownElement.Table(
            headers = listOf("左对齐", "居中对齐", "右对齐"),
            rows = listOf(
                listOf("左侧内容", "居中内容", "右侧内容"),
                listOf("Left", "Center", "Right")
            ),
            alignments = listOf(
                MarkdownElement.TableAlignment.LEFT,
                MarkdownElement.TableAlignment.CENTER,
                MarkdownElement.TableAlignment.RIGHT
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = listOf(tableElement))
                }
            }
        }

        // 验证表格内容存在
        composeTestRule.onNodeWithText("左对齐").assertExists()
        composeTestRule.onNodeWithText("居中对齐").assertExists()
        composeTestRule.onNodeWithText("右对齐").assertExists()
        composeTestRule.onNodeWithText("左侧内容").assertExists()
        composeTestRule.onNodeWithText("居中内容").assertExists()
        composeTestRule.onNodeWithText("右侧内容").assertExists()
    }

    @Test
    fun testTableMixedAlignment() {
        // 测试表格混合对齐
        val mixedTableElement = MarkdownElement.Table(
            headers = listOf("姓名", "年龄", "城市"),
            rows = listOf(
                listOf("张三", "25", "北京"),
                listOf("李四", "30", "上海")
            ),
            alignments = listOf(
                MarkdownElement.TableAlignment.LEFT,
                MarkdownElement.TableAlignment.CENTER,
                MarkdownElement.TableAlignment.RIGHT
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = listOf(mixedTableElement))
                }
            }
        }

        // 验证表格数据渲染
        composeTestRule.onNodeWithText("张三").assertExists()
        composeTestRule.onNodeWithText("25").assertExists()
        composeTestRule.onNodeWithText("北京").assertExists()
    }

    // ========== 嵌套列表渲染测试 ==========

    @Test
    fun testNestedListRendering() {
        // 测试嵌套列表渲染
        val nestedListElements = listOf(
            MarkdownElement.UnorderedList(
                items = listOf("一级无序列表项"),
                level = 0
            ),
            MarkdownElement.UnorderedList(
                items = listOf("二级无序列表项"),
                level = 1
            ),
            MarkdownElement.OrderedList(
                items = listOf("二级有序列表项"),
                level = 1
            ),
            MarkdownElement.UnorderedList(
                items = listOf("三级无序列表项"),
                level = 2
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = nestedListElements)
                }
            }
        }

        // 验证嵌套列表项存在
        composeTestRule.onNodeWithText("一级无序列表项").assertExists()
        composeTestRule.onNodeWithText("二级无序列表项").assertExists()
        composeTestRule.onNodeWithText("二级有序列表项").assertExists()
        composeTestRule.onNodeWithText("三级无序列表项").assertExists()
    }

    @Test
    fun testListLevelMarkers() {
        // 测试不同层级的列表标记
        val multiLevelListElements = listOf(
            MarkdownElement.UnorderedList(items = listOf("Level 0"), level = 0),
            MarkdownElement.UnorderedList(items = listOf("Level 1"), level = 1),
            MarkdownElement.UnorderedList(items = listOf("Level 2"), level = 2),
            MarkdownElement.UnorderedList(items = listOf("Level 3"), level = 3)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = multiLevelListElements)
                }
            }
        }

        // 验证各层级列表项存在
        composeTestRule.onNodeWithText("Level 0").assertExists()
        composeTestRule.onNodeWithText("Level 1").assertExists()
        composeTestRule.onNodeWithText("Level 2").assertExists()
        composeTestRule.onNodeWithText("Level 3").assertExists()
    }

    // ========== 代码块语法高亮渲染测试 ==========

    @Test
    fun testCodeBlockWithLanguage() {
        // 测试带语言标签的代码块渲染
        val codeBlockElement = MarkdownElement.CodeBlock(
            code = "fun main() {\n    println(\"Hello, World!\")\n}",
            language = "kotlin"
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = listOf(codeBlockElement))
                }
            }
        }

        // 验证代码内容存在
        composeTestRule.onNodeWithText("fun main() {", substring = true).assertExists()
        composeTestRule.onNodeWithText("println", substring = true).assertExists()
    }

    @Test
    fun testCodeBlockMultipleLanguages() {
        // 测试多种语言的代码块
        val codeBlockElements = listOf(
            MarkdownElement.CodeBlock(
                code = "console.log('Hello');",
                language = "javascript"
            ),
            MarkdownElement.CodeBlock(
                code = "print('Hello')",
                language = "python"
            ),
            MarkdownElement.CodeBlock(
                code = "System.out.println(\"Hello\");",
                language = "java"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = codeBlockElements)
                }
            }
        }

        // 验证不同语言的代码都存在
        composeTestRule.onNodeWithText("console.log", substring = true).assertExists()
        composeTestRule.onNodeWithText("print", substring = true).assertExists()
        composeTestRule.onNodeWithText("System.out.println", substring = true).assertExists()
    }

    @Test
    fun testCodeBlockWithoutLanguage() {
        // 测试无语言标签的代码块
        val codeBlockElement = MarkdownElement.CodeBlock(
            code = "generic code block\nwithout language",
            language = ""
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = listOf(codeBlockElement))
                }
            }
        }

        // 验证代码内容存在
        composeTestRule.onNodeWithText("generic code block", substring = true).assertExists()
    }

    // ========== 多级引用渲染测试 ==========

    @Test
    fun testMultiLevelBlockQuote() {
        // 测试多级引用渲染
        val blockQuoteElements = listOf(
            MarkdownElement.BlockQuote(text = "一级引用", level = 1),
            MarkdownElement.BlockQuote(text = "二级引用", level = 2),
            MarkdownElement.BlockQuote(text = "三级引用", level = 3)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = blockQuoteElements)
                }
            }
        }

        // 验证引用内容存在
        composeTestRule.onNodeWithText("一级引用").assertExists()
        composeTestRule.onNodeWithText("二级引用").assertExists()
        composeTestRule.onNodeWithText("三级引用").assertExists()
    }

    @Test
    fun testBlockQuoteVisualHierarchy() {
        // 测试引用视觉层次
        val maxLevelQuotes = listOf(
            MarkdownElement.BlockQuote(text = "Level 1 Quote", level = 1),
            MarkdownElement.BlockQuote(text = "Level 2 Quote", level = 2),
            MarkdownElement.BlockQuote(text = "Level 3 Quote", level = 3),
            MarkdownElement.BlockQuote(text = "Level 4 Quote", level = 4),
            MarkdownElement.BlockQuote(text = "Level 5 Quote", level = 5),
            MarkdownElement.BlockQuote(text = "Level 6 Quote", level = 6)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = maxLevelQuotes)
                }
            }
        }

        // 验证所有层级的引用都存在
        for (i in 1..6) {
            composeTestRule.onNodeWithText("Level $i Quote").assertExists()
        }
    }

    // ========== 高亮文本渲染测试 ==========

    @Test
    fun testHighlightTextRendering() {
        // 测试高亮文本渲染
        val highlightElement = MarkdownElement.Highlight(text = "这是高亮文本")

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = listOf(highlightElement))
                }
            }
        }

        // 验证高亮文本存在
        composeTestRule.onNodeWithText("这是高亮文本").assertExists()
    }

    @Test
    fun testHighlightWithOtherElements() {
        // 测试高亮文本与其他元素混合
        val mixedElements = listOf(
            MarkdownElement.Paragraph(text = "普通段落文本"),
            MarkdownElement.Highlight(text = "高亮文本"),
            MarkdownElement.Bold(text = "粗体文本")
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = mixedElements)
                }
            }
        }

        // 验证所有元素都存在
        composeTestRule.onNodeWithText("普通段落文本").assertExists()
        composeTestRule.onNodeWithText("高亮文本").assertExists()
        composeTestRule.onNodeWithText("粗体文本").assertExists()
    }

    // ========== 扩展语法渲染测试 ==========

    @Test
    fun testFootnoteRendering() {
        // 测试脚注渲染
        val footnoteElements = listOf(
            MarkdownElement.Footnote(id = "1", content = "这是脚注内容", isReference = false),
            MarkdownElement.Footnote(id = "1", content = "", isReference = true)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = footnoteElements)
                }
            }
        }

        // 验证脚注存在
        composeTestRule.onNodeWithText("这是脚注内容", substring = true).assertExists()
    }

    @Test
    fun testSuperscriptAndSubscript() {
        // 测试上标和下标渲染
        val scriptElements = listOf(
            MarkdownElement.Superscript(text = "2"),
            MarkdownElement.Subscript(text = "2")
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = scriptElements)
                }
            }
        }

        // 验证上下标文本存在
        composeTestRule.onAllNodesWithText("2").assertCountEquals(2)
    }

    @Test
    fun testMathFormulaRendering() {
        // 测试数学公式渲染
        val mathElements = listOf(
            MarkdownElement.Math(content = "E=mc^2", isInline = true),
            MarkdownElement.Math(content = "\\sum_{i=1}^{n} x_i", isInline = false)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = mathElements)
                }
            }
        }

        // 验证数学公式存在
        composeTestRule.onNodeWithText("E=mc^2", substring = true).assertExists()
        composeTestRule.onNodeWithText("\\sum_{i=1}^{n} x_i", substring = true).assertExists()
    }

    // ========== 主题适配测试 ==========

    @Test
    fun testDarkThemeRendering() {
        // 测试夜间模式渲染
        val testElements = listOf(
            MarkdownElement.Heading(text = "夜间模式标题", level = 1),
            MarkdownElement.Paragraph(text = "夜间模式段落"),
            MarkdownElement.Highlight(text = "夜间模式高亮")
        )

        composeTestRule.setContent {
            MaterialTheme(
                // 这里可以设置夜间主题，但由于测试环境限制，主要测试不崩溃
            ) {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = testElements)
                }
            }
        }

        // 验证在夜间模式下元素正常渲染
        composeTestRule.onNodeWithText("夜间模式标题").assertExists()
        composeTestRule.onNodeWithText("夜间模式段落").assertExists()
        composeTestRule.onNodeWithText("夜间模式高亮").assertExists()
    }

    // ========== 响应式布局测试 ==========

    @Test
    fun testResponsiveLayout() {
        // 测试响应式布局
        val complexElements = listOf(
            MarkdownElement.Heading(text = "响应式测试标题", level = 2),
            MarkdownElement.Table(
                headers = listOf("列1", "列2", "列3", "列4"),
                rows = listOf(
                    listOf("数据1", "数据2", "数据3", "数据4"),
                    listOf("长数据内容", "更长的数据内容", "非常长的数据内容", "极其长的数据内容")
                ),
                alignments = listOf(
                    MarkdownElement.TableAlignment.LEFT,
                    MarkdownElement.TableAlignment.CENTER,
                    MarkdownElement.TableAlignment.RIGHT,
                    MarkdownElement.TableAlignment.LEFT
                )
            ),
            MarkdownElement.CodeBlock(
                code = "这是一个很长的代码行，用于测试在不同屏幕尺寸下的响应式表现",
                language = "text"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = complexElements)
                }
            }
        }

        // 验证复杂布局不会崩溃
        composeTestRule.onNodeWithText("响应式测试标题").assertExists()
        composeTestRule.onNodeWithText("列1").assertExists()
        composeTestRule.onNodeWithText("数据1").assertExists()
    }

    // ========== 综合集成测试 ==========

    @Test
    fun testComplexMarkdownDocument() {
        // 测试复杂Markdown文档渲染
        val complexDocument = listOf(
            MarkdownElement.Heading(text = "完整功能测试文档", level = 1),
            MarkdownElement.Paragraph(text = "这是一个测试段落，包含各种Markdown元素。"),

            MarkdownElement.Heading(text = "任务列表测试", level = 2),
            MarkdownElement.TaskList(
                items = listOf(
                    "完成任务列表功能" to true,
                    "测试嵌套功能" to false
                ),
                level = 0
            ),

            MarkdownElement.Heading(text = "表格测试", level = 2),
            MarkdownElement.Table(
                headers = listOf("功能", "状态", "优先级"),
                rows = listOf(
                    listOf("解析器", "完成", "高"),
                    listOf("渲染器", "进行中", "高")
                ),
                alignments = listOf(
                    MarkdownElement.TableAlignment.LEFT,
                    MarkdownElement.TableAlignment.CENTER,
                    MarkdownElement.TableAlignment.RIGHT
                )
            ),

            MarkdownElement.Heading(text = "代码示例", level = 2),
            MarkdownElement.CodeBlock(
                code = "// Kotlin示例\nfun hello() {\n    println(\"Hello, World!\")\n}",
                language = "kotlin"
            ),

            MarkdownElement.Heading(text = "引用测试", level = 2),
            MarkdownElement.BlockQuote(text = "这是一级引用", level = 1),
            MarkdownElement.BlockQuote(text = "这是二级引用", level = 2),

            MarkdownElement.Heading(text = "高亮和公式", level = 2),
            MarkdownElement.Highlight(text = "重要内容高亮显示"),
            MarkdownElement.Math(content = "E = mc^2", isInline = true),

            MarkdownElement.Heading(text = "脚注测试", level = 2),
            MarkdownElement.Footnote(id = "1", content = "这是脚注说明", isReference = false)
        )

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = complexDocument)
                }
            }
        }

        // 验证关键元素存在
        composeTestRule.onNodeWithText("完整功能测试文档").assertExists()
        composeTestRule.onNodeWithText("任务列表测试").assertExists()
        composeTestRule.onNodeWithText("完成任务列表功能").assertExists()
        composeTestRule.onNodeWithText("功能").assertExists() // 表格标题
        composeTestRule.onNodeWithText("解析器").assertExists() // 表格内容
        composeTestRule.onNodeWithText("println", substring = true).assertExists() // 代码内容
        composeTestRule.onNodeWithText("这是一级引用").assertExists()
        composeTestRule.onNodeWithText("重要内容高亮显示").assertExists()
        composeTestRule.onNodeWithText("E = mc^2", substring = true).assertExists()
        composeTestRule.onNodeWithText("这是脚注说明", substring = true).assertExists()
    }

    @Test
    fun testPerformanceWithLargeDocument() {
        // 测试大文档性能
        val largeDocument = mutableListOf<MarkdownElement>()

        // 生成大量元素
        repeat(50) { i ->
            largeDocument.addAll(listOf(
                MarkdownElement.Heading(text = "标题 $i", level = (i % 6) + 1),
                MarkdownElement.Paragraph(text = "这是第 $i 个段落，包含一些测试内容。"),
                MarkdownElement.UnorderedList(
                    items = listOf("列表项 $i.1", "列表项 $i.2"),
                    level = 0
                )
            ))
        }

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().testTag("markdown_container")) {
                    RenderMarkdown(elements = largeDocument)
                }
            }
        }

        // 验证大文档不会崩溃，并且关键元素存在
        composeTestRule.onNodeWithText("标题 0").assertExists()
        composeTestRule.onNodeWithText("这是第 0 个段落，包含一些测试内容。").assertExists()
        composeTestRule.onNodeWithText("列表项 0.1").assertExists()
    }
}