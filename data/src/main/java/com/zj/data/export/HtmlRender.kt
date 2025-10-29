package com.zj.data.export

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
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body {
                font-family: "NotoSansCJKsc", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
                font-size: 14px;
                line-height: 1.5;
                margin: 20px;
                color: #333;
                background-color: #fff;
            }
            h1, h2, h3, h4, h5, h6 {
                margin-top: 24px;
                margin-bottom: 16px;
                font-weight: 600;
                line-height: 1.25;
                color: #1a1a1a;
            }
            h1 { 
                font-size: 2em; 
                padding-bottom: 0.3em;
                border-bottom: 1px solid #eaecef;
            }
            h2 { 
                font-size: 1.5em; 
                padding-bottom: 0.3em;
                border-bottom: 1px solid #eaecef;
            }
            h3 { font-size: 1.25em; }
            h4 { font-size: 1em; }
            h5 { font-size: 0.875em; }
            h6 { font-size: 0.85em; color: #6a737d; }
            
            p {
                margin-top: 0;
                margin-bottom: 16px;
            }
            
            strong, b { font-weight: 600; }
            em, i { font-style: italic; }
            del, s { text-decoration: line-through; }
            ins { text-decoration: underline; }
            
            ol, ul {
                margin-top: 0;
                margin-bottom: 16px;
                padding-left: 2em;
            }
            
            li {
                margin-bottom: 8px;
            }
            
            li > p {
                margin-top: 16px;
            }
            
            li + li {
                margin-top: 0.25em;
            }
            
            a {
                color: #0366d6;
                text-decoration: none;
            }
            
            a:hover {
                text-decoration: underline;
            }
            
            img {
                max-width: 100%;
                box-sizing: content-box;
                background-color: #fff;
                border-style: none;
            }
            
            img[align="right"] {
                padding-left: 20px;
            }
            
            img[align="left"] {
                padding-right: 20px;
            }
            
            table {
                border-spacing: 0;
                border-collapse: collapse;
                width: 100%;
                margin-bottom: 16px;
                font-size: 0.9em;
            }
            
            th, td {
                padding: 6px 13px;
                border: 1px solid #dfe2e5;
            }
            
            th {
                font-weight: 600;
                background-color: #f6f8fa;
            }
            
            tr:nth-child(2n) {
                background-color: #f6f8fa;
            }
            
            code {
                font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
                font-size: 0.85em;
                padding: 0.2em 0.4em;
                margin: 0;
                background-color: rgba(27,31,35,0.05);
                border-radius: 3px;
            }
            
            pre {
                font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
                font-size: 0.85em;
                margin-top: 0;
                margin-bottom: 16px;
                padding: 16px;
                overflow: auto;
                line-height: 1.45;
                background-color: #f6f8fa;
                border-radius: 3px;
            }
            
            pre > code {
                padding: 0;
                margin: 0;
                font-size: 100%;
                word-break: normal;
                white-space: pre;
                background: transparent;
                border: 0;
            }
            
            blockquote {
                margin: 0;
                padding: 0 1em;
                color: #6a737d;
                border-left: 0.25em solid #dfe2e5;
            }
            
            blockquote > p {
                margin: 0;
                padding: 0;
            }
            
            blockquote > :first-child {
                margin-top: 0;
            }
            
            blockquote > :last-child {
                margin-bottom: 0;
            }
            
            hr {
                margin: 24px 0;
                padding: 0;
                height: 0.25em;
                background-color: #e1e4e8;
                border: 0;
            }
            
            kbd {
                display: inline-block;
                padding: 3px 5px;
                font: 11px "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
                line-height: 10px;
                color: #444d56;
                vertical-align: middle;
                background-color: #fafbfc;
                border: solid 1px #d1d5da;
                border-bottom-color: #c6cbd1;
                border-radius: 3px;
                box-shadow: inset 0 -1px 0 #c6cbd1;
            }
            
            .task-list-item {
                list-style-type: none;
            }
            
            .task-list-item-checkbox {
                margin: 0 0.2em 0.25em -1.6em;
                vertical-align: middle;
            }
            
            .footnote-ref {
                font-size: 0.8em;
                vertical-align: super;
            }
            
            .footnote-backref {
                text-decoration: none;
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
 * 支持表格、脚注、标题锚点、删除线、任务列表、自动链接等多种特性
 *
 * @return 完整的带样式的HTML文档字符串
 */
suspend fun String.md2Html(): String {
    return withContext(Dispatchers.IO) {
        val extensions = listOf(
            TablesExtension.create(),              // 表格
            FootnotesExtension.create(),           // 脚注
            HeadingAnchorExtension.create(),       // 标题锚点
            StrikethroughExtension.create(),       // 删除线
            TaskListItemsExtension.create(),       // 任务列表
            AutolinkExtension.create(),            // 自动链接
        )

        // 解析 Markdown 为 HTML
        val parser = Parser.builder()
            .extensions(extensions)
            .build()
        val document = parser.parse(this@md2Html)
        
        val htmlRenderer = HtmlRenderer.builder()
            .extensions(extensions)
            .build()
        val htmlContent = htmlRenderer.render(document)

        // 创建完整的 HTML 文档
        val fullHtmlDocument = htmlContent.styledHtmlDocument()
        fullHtmlDocument
    }
}