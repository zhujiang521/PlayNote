package com.zj.ink.widget

sealed class MarkdownElement

data class Heading(val level: Int, val text: String) : MarkdownElement()
data class Bold(val text: String) : MarkdownElement()
data class Italic(val text: String) : MarkdownElement()
data class Strikethrough(val text: String) : MarkdownElement()
data class Paragraph(val text: String) : MarkdownElement()
data class Link(val text: String, val url: String) : MarkdownElement()
data class Image(val url: String) : MarkdownElement()
data class Code(val text: String) : MarkdownElement()
data class CodeBlock(val text: String) : MarkdownElement()
data class BlockQuote(val text: String) : MarkdownElement()
data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownElement()
object Divider : MarkdownElement()