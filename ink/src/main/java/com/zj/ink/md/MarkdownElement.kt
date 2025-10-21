package com.zj.ink.md

/**
 * Sealed class representing all possible Markdown elements that can be parsed and rendered.
 * This serves as the base class for all Markdown syntax elements in the PlayNote application.
 */
sealed class MarkdownElement

/**
 * Represents a heading element (H1-H6).
 * @param level The heading level from 1 to 6 (H1-H6)
 * @param text The text content of the heading
 */
data class Heading(val level: Int, val text: String) : MarkdownElement()

/**
 * Represents bold text formatting.
 * @param text The text content to be displayed in bold
 */
data class Bold(val text: String) : MarkdownElement()

/**
 * Represents italic text formatting.
 * @param text The text content to be displayed in italics
 */
data class Italic(val text: String) : MarkdownElement()

/**
 * Represents strikethrough text formatting.
 * @param text The text content to be displayed with strikethrough
 */
data class Strikethrough(val text: String) : MarkdownElement()

/**
 * Represents a regular paragraph of text.
 * @param text The paragraph content
 */
data class Paragraph(val text: String) : MarkdownElement()

/**
 * Represents a hyperlink.
 * @param text The display text of the link
 * @param url The URL the link points to
 */
data class Link(val text: String, val url: String) : MarkdownElement()

/**
 * Represents an embedded image.
 * @param url The URL or path to the image
 */
data class Image(val url: String) : MarkdownElement()

/**
 * Represents inline code formatting.
 * @param text The code content
 */
data class Code(val text: String) : MarkdownElement()

/**
 * Represents a code block with optional language specification.
 * @param text The code content
 * @param language The programming language for syntax highlighting (optional)
 */
data class CodeBlock(val text: String, val language: String = "") : MarkdownElement()

/**
 * Represents a block quote element.
 * @param text The quoted text content
 * @param level The nesting level of the quote (default: 1)
 */
data class BlockQuote(val text: String, val level: Int = 1) : MarkdownElement()

/**
 * Enum representing table column alignment options.
 */
enum class TableAlignment {
    LEFT, CENTER, RIGHT
}

/**
 * Represents a table element with headers, rows, and column alignment.
 * @param headers The table header row
 * @param rows The table data rows
 * @param alignments The alignment for each column (optional, defaults to left alignment)
 */
data class Table(
    val headers: List<String>,
    val rows: List<List<String>>,
    val alignments: List<TableAlignment> = emptyList()
) : MarkdownElement()

/**
 * Represents an unordered (bulleted) list.
 * @param items The list items
 * @param level The nesting level (default: 1)
 */
data class UnorderedList(val items: List<String>, val level: Int = 1) : MarkdownElement()

/**
 * Represents an ordered (numbered) list.
 * @param items The list items
 * @param level The nesting level (default: 1)
 */
data class OrderedList(val items: List<String>, val level: Int = 1) : MarkdownElement()

/**
 * Represents a task list item with checkbox functionality.
 * @param text The task description text
 * @param isChecked Whether the task is completed
 * @param level The nesting level (default: 1)
 */
data class TaskList(val text: String, val isChecked: Boolean, val level: Int = 1) : MarkdownElement()

/**
 * Represents highlighted text (==text==).
 * @param text The text content to be highlighted
 */
data class Highlight(val text: String) : MarkdownElement()

/**
 * Represents a footnote reference or definition.
 * @param id The footnote identifier
 * @param text The footnote content (empty for references)
 * @param isReference True if this is a footnote reference, false if it's the definition
 */
data class Footnote(val id: String, val text: String = "", val isReference: Boolean = true) : MarkdownElement()

/**
 * Represents superscript text (X^2^).
 * @param text The text content to be displayed as superscript
 */
data class Superscript(val text: String) : MarkdownElement()

/**
 * Represents subscript text (H~2~O).
 * @param text The text content to be displayed as subscript
 */
data class Subscript(val text: String) : MarkdownElement()

/**
 * Represents mathematical expressions in LaTeX format.
 * @param expression The LaTeX mathematical expression
 * @param isInline True for inline math, false for display math blocks
 */
data class Math(val expression: String, val isInline: Boolean = true) : MarkdownElement()

/**
 * Represents a nested list structure for complex hierarchical lists.
 * @param items The nested list items, can contain other MarkdownElements
 * @param isOrdered True for ordered lists, false for unordered
 * @param level The nesting level
 */
data class NestedList(
    val items: List<MarkdownElement>,
    val isOrdered: Boolean = false,
    val level: Int = 1
) : MarkdownElement()

/**
 * Represents a horizontal divider/separator.
 */
object Divider : MarkdownElement()