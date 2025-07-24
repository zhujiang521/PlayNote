package com.zj.data.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/**
 * 为HTML内容添加样式
 *
 * 此函数将原始HTML内容包装在完整的HTML文档结构中，
 * 并添加CSS样式以确保内容在各种导出格式中正确显示
 *
 * @return 完整的带样式的HTML文档
 */
fun String.styledHtmlDocument() = """
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body {
                font-family: "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
                font-size: 14px;
                line-height: 1.5;
                margin: 20px;
            }
            h1, h2, h3, h4, h5, h6 {
                margin-top: 20px;
                margin-bottom: 10px;
                font-weight: bold;
                font-family: "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            }
            h1 { font-size: 2em; }
            h2 { font-size: 1.5em; }
            h3 { font-size: 1.25em; }
            p {
                margin-top: 0;
                margin-bottom: 10px;
                font-family: "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            }
            strong, b { font-weight: bold; }
            em, i { font-style: italic; }
            del { text-decoration: line-through; }
            ol, ul {
                margin-top: 0;
                margin-bottom: 10px;
                padding-left: 20px;
            }
            li {
                margin-bottom: 5px;
                font-family: "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            }
            a {
                color: #0000ff;
                text-decoration: underline;
            }
            img {
                max-width: 100%;
                height: auto;
            }
            table {
                border-collapse: collapse;
                width: 100%;
                margin-bottom: 10px;
                font-family: "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            }
            th, td {
                border: 1px solid #000;
                padding: 8px;
                text-align: left;
            }
            th {
                background-color: #f0f0f0;
                font-weight: bold;
            }
            code {
                font-family: "Courier New", "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", monospace;
                background-color: #f0f0f0;
                padding: 2px 4px;
                border-radius: 3px;
            }
            pre {
                font-family: "Courier New", "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", monospace;
                background-color: #f0f0f0;
                padding: 10px;
                border-radius: 5px;
                overflow-x: auto;
            }
            blockquote {
                margin: 0 0 10px 0;
                padding: 10px 20px;
                border-left: 4px solid #ccc;
                background-color: #f9f9f9;
                font-family: "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            }
            hr {
                margin: 20px 0;
                border: none;
                border-top: 1px solid #ccc;
            }
        </style>
    </head>
    <body>
        $this
    </body>
    </html>
""".trimIndent()

/**
 * 将Markdown字符串转换为带样式的HTML文档
 *
 * 使用CommonMark解析器和扩展来处理Markdown内容，
 * 支持表格、脚注、标题锚点、删除线、任务列表和自动链接等特性
 *
 * @return 完整的带样式的HTML文档字符串
 */
suspend fun String.md2Html(): String {
    return withContext(Dispatchers.IO) {
        val extensions = listOf(
            TablesExtension.create(),              // 表格
            FootnotesExtension.create(),            // 脚注
            HeadingAnchorExtension.create(),       // 标题锚点
            StrikethroughExtension.create(),       // 删除线
            TaskListItemsExtension.create(),       // 任务列表
            AutolinkExtension.create(),            // 自动链接
        )

        // 解析 Markdown 为 HTML
        val parser = Parser.builder().extensions(extensions).build()
        val document = parser.parse(this@md2Html)
        val htmlRenderer = HtmlRenderer.builder().extensions(extensions).build()
        val htmlContent = htmlRenderer.render(document)

        // 创建完整的 HTML 文档
        val fullHtmlDocument = htmlContent.styledHtmlDocument()
        fullHtmlDocument
    }
}
