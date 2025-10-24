package com.zj.ink.md

/**
 * PlayNote Markdown解析器
 *
 * 这是一个全功能的Markdown解析器，支持标准Markdown语法以及扩展语法。
 * 解析器采用高性能设计，包含错误处理、缓存机制和性能优化。
 *
 * 支持的Markdown语法包括：
 * - 标准语法：标题(H1-H6)、段落、加粗、斜体、删除线、链接、图片、代码、代码块、引用、表格、列表
 * - 扩展语法：任务列表、高亮文本、脚注、上下标、数学公式、嵌套列表、多级引用、表格对齐
 *
 * 性能特性：
 * - 预编译正则表达式避免重复编译
 * - LRU缓存机制提升重复解析性能
 * - 大文档分块处理防止内存溢出
 * - 错误降级机制确保解析器健壮性
 *
 * @author PlayNote开发团队
 * @since 1.0
 */
object MarkdownParser {

    /** 匹配脚注定义语法: [^id]: content */
    @JvmStatic
    private val FOOTNOTE_DEFINITION_REGEX = Regex("""^\[\^([^]]+)]:\s*(.*)$""")

    /** 匹配脚注引用语法: [^id] */
    @JvmStatic
    private val FOOTNOTE_REFERENCE_REGEX = Regex("""\[\^([^]]+)]""")

    /** 匹配内联数学公式语法: $formula$ */
    @JvmStatic
    private val MATH_INLINE_REGEX = Regex("""\$([^$]+)\$""")

    /** 匹配上标语法: ^text^ */
    @JvmStatic
    private val SUPERSCRIPT_REGEX = Regex("""\^([^^\s]+)\^""")

    /** 匹配下标语法: ~text~ */
    @JvmStatic
    private val SUBSCRIPT_REGEX = Regex("""~([^~\s]+)~""")

    /** 匹配任务列表语法: - [ ] 或 - [android.R.attr.x] */
    @JvmStatic
    private val TASK_LIST_REGEX = Regex("""^- \[([ x])] .*$""")

    /** 匹配有序列表语法: 1. item */
    @JvmStatic
    private val ORDERED_LIST_REGEX = Regex("""^\d+\. .*$""")

    /** 匹配多级引用语法: > >> >>> */
    @JvmStatic
    private val MULTI_QUOTE_REGEX = Regex("""^(>+)\s*(.*)$""")

    /** 匹配加粗语法: **text** */
    @JvmStatic
    private val BOLD_REGEX = Regex("""\*\*([^*]+)\*\*""")

    /** 匹配斜体语法: *text* */
    @JvmStatic
    private val ITALIC_REGEX = Regex("""\*([^*]+)\*""")

    /** 匹配删除线语法: ~~text~~ */
    @JvmStatic
    private val STRIKETHROUGH_REGEX = Regex("""~~([^~]+)~~""")

    /** 匹配高亮语法: ==text== */
    @JvmStatic
    private val HIGHLIGHT_REGEX = Regex("""==([^=]+)==""")

    // 转义字符映射表，提升字符串替换性能
    /** 转义字符映射表，将Markdown转义字符转换为实际字符 */
    private val ESCAPE_CHARS = mapOf(
        "\\*" to "*",
        "\\#" to "#",
        "\\[" to "[",
        "\\]" to "]",
        "\\(" to "(",
        "\\)" to ")",
        "\\{" to "{",
        "\\}" to "}",
        "\\_" to "_",
        "\\`" to "`",
        "\\~" to "~",
        "\\\\" to "\\"
    )

    // 错误处理相关常量
    /** 最大文本长度限制(1MB)，防止内存溢出 */
    private const val MAX_TEXT_LENGTH = 1_000_000 // 1MB文本限制

    /** 最大嵌套层级，防止过深嵌套影响性能 */
    private const val MAX_NESTING_LEVEL = 10 // 最大嵌套层级

    /** 最大表格列数，防止表格过大影响渲染 */
    private const val MAX_TABLE_COLUMNS = 50 // 最大表格列数

    /** 最大列表项数，防止列表过长影响性能 */
    private const val MAX_LIST_ITEMS = 1000 // 最大列表项数

    // 错误计数器，用于调试和监控
    private var parseErrorCount = 0
    private var lastErrorMessage: String? = null

    /**
     * 记录解析错误
     */
    private fun logParseError(message: String, exception: Throwable? = null) {
        parseErrorCount++
        lastErrorMessage = message
        // 在调试模式下可以添加更详细的日志
        if (exception != null) {
            println("MarkdownParser Error: $message - ${exception.message}")
        } else {
            println("MarkdownParser Warning: $message")
        }
    }

    /**
     * 处理转义字符，将反斜杠转义的Markdown语法字符转换为实际字符
     * 优化版本：使用预编译的映射表提升性能
     */
    private fun unescapeText(text: String): String {
        var result = text
        ESCAPE_CHARS.forEach { (escaped, actual) ->
            result = result.replace(escaped, actual)
        }
        return result
    }

    /**
     * 解析结果缓存，使用 LRU 策略避免重复解析相同内容
     * LinkedHashMap 的 accessOrder = true 实现 LRU 访问顺序
     */
    private val parseCache = object : LinkedHashMap<Int, List<MarkdownElement>>(
        MAX_CACHE_SIZE + 1,
        0.75f,
        true // accessOrder = true 启用访问顺序（LRU）
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, List<MarkdownElement>>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    private const val MAX_CACHE_SIZE = 50 // 限制缓存大小防止内存泄漏

    /**
     * 清理缓存，防止内存泄漏
     */
    fun clearCache() {
        synchronized(parseCache) {
            parseCache.clear()
        }
    }

    /**
     * 优化的解析函数，支持缓存和性能优化，增强错误处理
     */
    fun parse(text: String): List<MarkdownElement> {
        return try {
            // 输入验证和边界检查
            when {
                text.isEmpty() -> {
                    logParseError("Empty input text provided")
                    return emptyList()
                }

                text.length > MAX_TEXT_LENGTH -> {
                    logParseError("Text too long: ${text.length} characters (max: $MAX_TEXT_LENGTH)")
                    return listOf(Paragraph("文档过大，无法解析"))
                }
            }

            // 使用 hashCode 作为缓存 key，节省内存
            val cacheKey = text.hashCode()

            // 检查缓存（线程安全）
            synchronized(parseCache) {
                parseCache[cacheKey]?.let { return it }
            }

            // 处理大文档的分块解析
            val result = if (text.length > 50000) {
                parseInChunks(text)
            } else {
                parseInternal(text)
            }

            // 缓存结果（LRU 自动淘汰最少使用的条目）
            synchronized(parseCache) {
                parseCache[cacheKey] = result
            }

            return result
        } catch (e: OutOfMemoryError) {
            logParseError("Out of memory during parsing", e)
            clearCache() // 清理缓存释放内存
            listOf(Paragraph("内存不足，解析失败"))
        } catch (e: StackOverflowError) {
            logParseError("Stack overflow during parsing (possible infinite recursion)", e)
            listOf(Paragraph("解析层级过深，请简化文档结构"))
        } catch (e: Exception) {
            logParseError("Unexpected error during parsing", e)
            // 优雅降级：尝试简单解析
            trySimpleParse(text)
        }
    }

    /**
     * 简单解析模式，作为错误降级方案
     */
    private fun trySimpleParse(text: String): List<MarkdownElement> {
        return try {
            text.split("\n\n")
                .filter { it.isNotBlank() }
                .take(100) // 限制段落数量
                .map { paragraph ->
                    val trimmed = paragraph.trim()
                    when {
                        trimmed.startsWith("#") -> {
                            val level = trimmed.takeWhile { it == '#' }.length.coerceIn(1, 6)
                            val content = trimmed.dropWhile { it == '#' }.trim()
                            Heading(level, content.take(200)) // 限制标题长度
                        }

                        else -> Paragraph(trimmed.take(1000)) // 限制段落长度
                    }
                }
        } catch (e: Exception) {
            logParseError("Simple parse also failed", e)
            listOf(Paragraph("解析失败，请检查文档格式"))
        }
    }

    /**
     * 大文档分块解析，减少内存压力，增强错误处理
     */
    private fun parseInChunks(text: String): List<MarkdownElement> {
        return try {
            val chunks = text.split("\n\n") // 按段落分块
            val result = mutableListOf<MarkdownElement>()
            var processedChunks = 0
            val maxChunks = 500 // 限制处理的块数量

            chunks.forEachIndexed { index, chunk ->
                if (processedChunks >= maxChunks) {
                    logParseError("Too many chunks to process, stopping at $maxChunks")
                    result.add(Paragraph("文档过长，部分内容未解析"))
                    return@forEachIndexed
                }

                if (chunk.isNotBlank()) {
                    try {
                        val chunkResult = parseInternal(chunk)
                        result.addAll(chunkResult)
                        processedChunks++
                    } catch (e: Exception) {
                        logParseError("Error parsing chunk $index", e)
                        // 降级处理：将错误块作为普通段落
                        result.add(Paragraph(chunk.take(500)))
                    }
                }
            }

            result
        } catch (e: Exception) {
            logParseError("Error in chunk parsing", e)
            trySimpleParse(text)
        }
    }

    /**
     * 内部解析实现，优化性能，增强错误处理
     */
    private fun parseInternal(text: String): List<MarkdownElement> {
        return try {
            val lines = text.split("\n")
            val result = mutableListOf<MarkdownElement>()
            var i = 0
            var processedElements = 0
            val maxElements = 2000 // 限制解析的元素数量

            while (i < lines.size && processedElements < maxElements) {
                try {
                    val line = lines[i].trimStart()

                    // 跳过空行避免不必要的处理
                    if (line.isBlank()) {
                        i++
                        continue
                    }

                    // 标题解析（增强错误处理）
                    val headingResult = parseHeading(line)
                    if (headingResult != null) {
                        result.add(headingResult)
                        processedElements++
                        i++
                        continue
                    }

                    // 扩展语法解析（脚注、数学公式、上下标）
                    val extendedResult = parseExtendedSyntax(line)
                    if (extendedResult != null) {
                        result.add(extendedResult)
                        processedElements++
                        i++
                        continue
                    }

                    // 行内格式解析（高亮、删除线、加粗、斜体）
                    val inlineResult = parseInlineFormat(line)
                    if (inlineResult != null) {
                        result.add(inlineResult)
                        processedElements++
                        i++
                        continue
                    }

                    // 媒体元素解析（图片、链接）
                    val mediaResult = parseMediaElement(line)
                    if (mediaResult != null) {
                        result.add(mediaResult)
                        processedElements++
                        i++
                        continue
                    }

                    // 代码块解析（增强错误处理）
                    if (line.startsWith("```")) {
                        val (codeBlockResult, nextIndex) = parseCodeBlock(lines, i)
                        if (codeBlockResult != null) {
                            result.add(codeBlockResult)
                            processedElements++
                        }
                        i = nextIndex
                        continue
                    }

                    // 行内代码解析（增强错误处理）
                    if (line.contains("`")) {
                        val codeResult = parseInlineCode(line)
                        if (codeResult != null) {
                            result.add(codeResult)
                            processedElements++
                            i++
                            continue
                        }
                    }

                    // 多级引用解析（增强错误处理）
                    if (line.startsWith(">")) {
                        val quoteResult = parseBlockQuote(line)
                        if (quoteResult != null) {
                            result.add(quoteResult)
                            processedElements++
                            i++
                            continue
                        }
                    }

                    // 嵌套列表解析（增强错误处理）
                    if (isListItem(line)) {
                        val (nestedListItems, nextIndex) = parseNestedListSafe(lines, i)
                        result.addAll(nestedListItems)
                        processedElements += nestedListItems.size
                        i = nextIndex
                        continue
                    }

                    // 分割线解析
                    if (line == "---") {
                        result.add(Divider)
                        processedElements++
                        i++
                        continue
                    }

                    // 表格解析（增强错误处理）
                    if (line.startsWith("|") && line.contains("|")) {
                        val (tableResult, nextIndex) = parseTableSafe(lines, i)
                        if (tableResult != null) {
                            result.add(tableResult)
                            processedElements++
                        }
                        i = nextIndex
                        continue
                    }

                    // 普通段落
                    if (line.isNotBlank()) {
                        result.add(Paragraph(unescapeText(line.take(2000)))) // 限制段落长度
                        processedElements++
                    }

                    i++
                } catch (e: Exception) {
                    logParseError("Error parsing line $i: '${lines.getOrNull(i)?.take(50)}'", e)
                    // 降级处理：跳过错误行，继续处理下一行
                    i++
                }
            }

            if (processedElements >= maxElements) {
                logParseError("Max elements limit reached ($maxElements), stopping parse")
                result.add(Paragraph("文档过于复杂，部分内容未解析"))
            }

            result
        } catch (e: Exception) {
            logParseError("Critical error in parseInternal", e)
            listOf(Paragraph("解析出现严重错误"))
        }
    }

    /**
     * 安全的标题解析
     */
    private fun parseHeading(line: String): Heading? {
        return try {
            when {
                line.startsWith("###### ") && line.length > 7 ->
                    Heading(6, unescapeText(line.removePrefix("###### ").take(200)))

                line.startsWith("##### ") && line.length > 6 ->
                    Heading(5, unescapeText(line.removePrefix("##### ").take(200)))

                line.startsWith("#### ") && line.length > 5 ->
                    Heading(4, unescapeText(line.removePrefix("#### ").take(200)))

                line.startsWith("### ") && line.length > 4 ->
                    Heading(3, unescapeText(line.removePrefix("### ").take(200)))

                line.startsWith("## ") && line.length > 3 ->
                    Heading(2, unescapeText(line.removePrefix("## ").take(200)))

                line.startsWith("# ") && line.length > 2 ->
                    Heading(1, unescapeText(line.removePrefix("# ").take(200)))

                else -> null
            }
        } catch (e: Exception) {
            logParseError("Error parsing heading", e)
            null
        }
    }

    /**
     * 安全的扩展语法解析（脚注、数学公式、上下标）
     */
    private fun parseExtendedSyntax(line: String): MarkdownElement? {
        return try {
            when {
                // 脚注定义
                FOOTNOTE_DEFINITION_REGEX.matches(line) -> {
                    val matchResult = FOOTNOTE_DEFINITION_REGEX.find(line)
                    if (matchResult != null) {
                        val (id, content) = matchResult.destructured
                        Footnote(unescapeText(id.take(50)), unescapeText(content.take(500)), false)
                    } else null
                }

                // 脚注引用
                FOOTNOTE_REFERENCE_REGEX.containsMatchIn(line) -> {
                    val matchResult = FOOTNOTE_REFERENCE_REGEX.find(line)
                    if (matchResult != null) {
                        val id = matchResult.groupValues[1]
                        Footnote(unescapeText(id.take(50)), "", true)
                    } else null
                }

                // 数学公式块
                line.startsWith("$$") && line.endsWith("$$") && line.length > 4 -> {
                    val expression = line.substring(2, line.length - 2).trim()
                    if (expression.length <= 1000) {
                        Math(unescapeText(expression), false)
                    } else {
                        logParseError("Math expression too long: ${expression.length}")
                        null
                    }
                }

                // 内联数学公式
                line.contains("$") && !line.startsWith("$$") -> {
                    val matchResult = MATH_INLINE_REGEX.find(line)
                    if (matchResult != null) {
                        val expression = matchResult.groupValues[1]
                        if (expression.length <= 200) {
                            Math(unescapeText(expression), true)
                        } else {
                            logParseError("Inline math expression too long: ${expression.length}")
                            null
                        }
                    } else null
                }

                // 上标
                SUPERSCRIPT_REGEX.containsMatchIn(line) -> {
                    val matchResult = SUPERSCRIPT_REGEX.find(line)
                    if (matchResult != null) {
                        val text = matchResult.groupValues[1]
                        Superscript(unescapeText(text.take(100)))
                    } else null
                }

                // 下标
                SUBSCRIPT_REGEX.containsMatchIn(line) -> {
                    val matchResult = SUBSCRIPT_REGEX.find(line)
                    if (matchResult != null) {
                        val text = matchResult.groupValues[1]
                        Subscript(unescapeText(text.take(100)))
                    } else null
                }

                else -> null
            }
        } catch (e: Exception) {
            logParseError("Error parsing extended syntax", e)
            null
        }
    }

    /**
     * 安全的行内格式解析（高亮、删除线、加粗、斜体）
     */
    private fun parseInlineFormat(line: String): MarkdownElement? {
        return try {
            when {
                // 高亮文本
                HIGHLIGHT_REGEX.containsMatchIn(line) -> {
                    HIGHLIGHT_REGEX.find(line)?.let { match ->
                        Highlight(unescapeText(match.groupValues[1].take(500)))
                    }
                }

                // 删除线
                STRIKETHROUGH_REGEX.containsMatchIn(line) -> {
                    STRIKETHROUGH_REGEX.find(line)?.let { match ->
                        Strikethrough(unescapeText(match.groupValues[1].take(500)))
                    }
                }

                // 加粗
                BOLD_REGEX.containsMatchIn(line) -> {
                    BOLD_REGEX.find(line)?.let { match ->
                        Bold(unescapeText(match.groupValues[1].take(500)))
                    }
                }

                // 斜体
                ITALIC_REGEX.containsMatchIn(line) -> {
                    ITALIC_REGEX.find(line)?.let { match ->
                        Italic(unescapeText(match.groupValues[1].take(500)))
                    }
                }

                else -> null
            }
        } catch (e: Exception) {
            logParseError("Error parsing inline format", e)
            null
        }
    }

    /**
     * 安全的媒体元素解析（图片、链接）
     */
    private fun parseMediaElement(line: String): MarkdownElement? {
        return try {
            when {
                // 图片
                line.startsWith("!") && line.contains("](") -> {
                    val urlStart = line.indexOf("(")
                    val urlEnd = line.indexOf(")", urlStart)
                    if (urlStart != -1 && urlEnd != -1 && urlEnd > urlStart) {
                        val url = line.substring(urlStart + 1, urlEnd)
                        if (url.length <= 2000) {
                            Image(unescapeText(url))
                        } else {
                            logParseError("Image URL too long: ${url.length}")
                            null
                        }
                    } else null
                }

                // 链接
                line.contains("](") -> {
                    val textStart = line.indexOf("[")
                    val textEnd = line.indexOf("]", textStart)
                    val urlStart = line.indexOf("(", textEnd)
                    val urlEnd = line.indexOf(")", urlStart)

                    if (textStart != -1 && textEnd != -1 && urlStart != -1 && urlEnd != -1 &&
                        textEnd > textStart && urlStart == textEnd + 1 && urlEnd > urlStart
                    ) {
                        val text = line.substring(textStart + 1, textEnd)
                        val url = line.substring(urlStart + 1, urlEnd)

                        if (text.length <= 500 && url.length <= 2000) {
                            Link(unescapeText(text), unescapeText(url))
                        } else {
                            logParseError("Link text/URL too long: text=${text.length}, url=${url.length}")
                            null
                        }
                    } else null
                }

                else -> null
            }
        } catch (e: Exception) {
            logParseError("Error parsing media element", e)
            null
        }
    }

    /**
     * 安全的代码块解析
     */
    private fun parseCodeBlock(lines: List<String>, startIndex: Int): Pair<CodeBlock?, Int> {
        return try {
            val startLine = lines[startIndex]
            val language = startLine.removePrefix("```").trim().take(50) // 限制语言标识符长度
            val codeLines = mutableListOf<String>()
            var i = startIndex + 1
            var foundEnd = false

            while (i < lines.size && codeLines.size < 1000) { // 限制代码行数
                if (lines[i].trimStart().startsWith("```")) {
                    foundEnd = true
                    i++
                    break
                }
                codeLines.add(lines[i].take(500)) // 限制每行长度
                i++
            }

            if (!foundEnd) {
                logParseError("Code block not properly closed")
                return Pair(null, i)
            }

            val code = codeLines.joinToString("\n")
            if (code.length > 50000) {
                logParseError("Code block too large: ${code.length}")
                Pair(null, i)
            } else {
                Pair(CodeBlock(code, language), i)
            }
        } catch (e: Exception) {
            logParseError("Error parsing code block", e)
            Pair(null, startIndex + 1)
        }
    }

    /**
     * 安全的行内代码解析
     */
    private fun parseInlineCode(line: String): Code? {
        return try {
            val backtickCount = line.count { it == '`' }
            if (backtickCount >= 2) {
                val content = line.removePrefix("`").removeSuffix("`").replace("`", "")
                if (content.length <= 200) {
                    Code(unescapeText(content))
                } else {
                    logParseError("Inline code too long: ${content.length}")
                    null
                }
            } else null
        } catch (e: Exception) {
            logParseError("Error parsing inline code", e)
            null
        }
    }

    /**
     * 安全的引用解析
     */
    private fun parseBlockQuote(line: String): BlockQuote? {
        return try {
            val matchResult = MULTI_QUOTE_REGEX.find(line)
            if (matchResult != null) {
                val level = matchResult.groupValues[1].length
                val content = matchResult.groupValues[2]

                // 限制最大层级为6，超过部分作为普通文本处理
                if (level > MAX_NESTING_LEVEL) {
                    logParseError("Quote nesting too deep: $level")
                    null
                } else {
                    BlockQuote(unescapeText(content.take(1000)), level)
                }
            } else null
        } catch (e: Exception) {
            logParseError("Error parsing block quote", e)
            null
        }
    }

    /**
     * 安全的嵌套列表解析
     */
    private fun parseNestedListSafe(
        lines: List<String>,
        startIndex: Int
    ): Pair<List<MarkdownElement>, Int> {
        return try {
            val (elements, nextIndex) = parseNestedList(lines, startIndex)

            // 限制列表项数量
            if (elements.size > MAX_LIST_ITEMS) {
                logParseError("Too many list items: ${elements.size}")
                Pair(elements.take(MAX_LIST_ITEMS), nextIndex)
            } else {
                Pair(elements, nextIndex)
            }
        } catch (e: Exception) {
            logParseError("Error parsing nested list", e)
            Pair(emptyList(), startIndex + 1)
        }
    }

    /**
     * 安全的表格解析
     */
    private fun parseTableSafe(lines: List<String>, startIndex: Int): Pair<Table?, Int> {
        return try {
            val tableLines = mutableListOf<String>()
            var i = startIndex

            while (i < lines.size && lines[i].trimStart()
                    .startsWith("|") && tableLines.size < 100
            ) {
                tableLines.add(lines[i].trim().take(2000)) // 限制行长度
                i++
            }

            if (tableLines.size >= 2) {
                val headers = parseTableRowSafe(tableLines[0])
                val alignments = parseTableAlignmentSafe(tableLines[1])

                // 限制列数
                if (headers == null || headers.size > MAX_TABLE_COLUMNS) {
                    logParseError("Too many table columns: ${headers?.size}")
                    return Pair(null, i)
                }

                val rows =
                    tableLines.subList(2, tableLines.size).mapNotNull { parseTableRowSafe(it) }
                Pair(Table(headers, rows, alignments), i)
            } else {
                Pair(null, i)
            }
        } catch (e: Exception) {
            logParseError("Error parsing table", e)
            Pair(null, startIndex + 1)
        }
    }

    /**
     * 安全的表格行解析
     */
    private fun parseTableRowSafe(line: String): List<String>? {
        return try {
            val cells = line
                .removePrefix("|")
                .removeSuffix("|")
                .split("|")
                .map { it.trim().take(500) } // 限制单元格内容长度

            if (cells.size <= MAX_TABLE_COLUMNS) cells else null
        } catch (e: Exception) {
            logParseError("Error parsing table row", e)
            null
        }
    }

    /**
     * 安全的表格对齐解析
     */
    private fun parseTableAlignmentSafe(line: String): List<TableAlignment> {
        return try {
            line
                .removePrefix("|")
                .removeSuffix("|")
                .split("|")
                .take(MAX_TABLE_COLUMNS) // 限制列数
                .map { cell ->
                    val trimmed = cell.trim()
                    when {
                        trimmed.startsWith(":") && trimmed.endsWith(":") -> TableAlignment.CENTER
                        trimmed.startsWith(":") -> TableAlignment.LEFT
                        trimmed.endsWith(":") -> TableAlignment.RIGHT
                        else -> TableAlignment.LEFT // 默认左对齐
                    }
                }
        } catch (e: Exception) {
            logParseError("Error parsing table alignment", e)
            emptyList()
        }
    }

    /**
     * 判断行是否为列表项（任务列表、无序列表、有序列表）
     * 优化版本：使用预编译正则表达式提升性能
     */
    private fun isListItem(line: String): Boolean {
        val trimmed = line.trimStart()
        return TASK_LIST_REGEX.matches(trimmed) || // 任务列表
                trimmed.startsWith("- ") || // 无序列表
                ORDERED_LIST_REGEX.matches(trimmed) // 有序列表
    }

    /**
     * 解析嵌套列表结构
     * 返回 Pair(解析的元素列表, 下一个处理的行索引)
     */
    private fun parseNestedList(
        lines: List<String>,
        startIndex: Int
    ): Pair<List<MarkdownElement>, Int> {
        val result = mutableListOf<MarkdownElement>()
        var currentIndex = startIndex

        while (currentIndex < lines.size) {
            val line = lines[currentIndex]
            if (!isListItem(line)) break

            val indentLevel = getIndentLevel(line)
            val trimmedLine = line.trimStart()

            // 根据列表类型解析
            when {
                // 任务列表
                trimmedLine.matches(Regex("""^- \[([ x])] .*$""")) -> {
                    val (taskItems, nextIndex) = parseTaskListItems(
                        lines,
                        currentIndex,
                        indentLevel
                    )
                    result.addAll(taskItems)
                    currentIndex = nextIndex
                }

                // 无序列表
                trimmedLine.startsWith("- ") -> {
                    val (unorderedItems, nextIndex) = parseUnorderedListItems(
                        lines,
                        currentIndex,
                        indentLevel
                    )
                    result.addAll(unorderedItems)
                    currentIndex = nextIndex
                }

                // 有序列表
                trimmedLine.matches(Regex("""^\d+\. .*$""")) -> {
                    val (orderedItems, nextIndex) = parseOrderedListItems(
                        lines,
                        currentIndex,
                        indentLevel
                    )
                    result.addAll(orderedItems)
                    currentIndex = nextIndex
                }

                else -> break
            }
        }

        return Pair(result, currentIndex)
    }

    /**
     * 获取行的缩进级别（每4个空格为一级）
     */
    private fun getIndentLevel(line: String): Int {
        val leadingSpaces = line.length - line.trimStart().length
        return (leadingSpaces / 4) + 1 // 基础级别为1
    }

    /**
     * 解析任务列表项目
     */
    private fun parseTaskListItems(
        lines: List<String>,
        startIndex: Int,
        baseLevel: Int
    ): Pair<List<MarkdownElement>, Int> {
        val result = mutableListOf<MarkdownElement>()
        var currentIndex = startIndex

        while (currentIndex < lines.size) {
            val line = lines[currentIndex]
            if (!isListItem(line)) break

            val currentLevel = getIndentLevel(line)
            val trimmedLine = line.trimStart()

            when {
                // 同级别任务列表项
                currentLevel == baseLevel && trimmedLine.matches(Regex("""^- \[([ x])] .*$""")) -> {
                    val isChecked = trimmedLine.contains("[x]")
                    val content = unescapeText(trimmedLine.substring(6).trimStart())
                    result.add(TaskList(content, isChecked, baseLevel))
                    currentIndex++
                }

                // 更深层级的嵌套项
                currentLevel > baseLevel -> {
                    val (nestedElements, nextIndex) = parseNestedList(lines, currentIndex)
                    result.addAll(nestedElements)
                    currentIndex = nextIndex
                }

                // 更浅层级或不同类型，结束当前解析
                else -> break
            }
        }

        return Pair(result, currentIndex)
    }

    /**
     * 解析无序列表项目
     */
    private fun parseUnorderedListItems(
        lines: List<String>,
        startIndex: Int,
        baseLevel: Int
    ): Pair<List<MarkdownElement>, Int> {
        val items = mutableListOf<String>()
        val nestedItems = mutableListOf<MarkdownElement>()
        var currentIndex = startIndex

        while (currentIndex < lines.size) {
            val line = lines[currentIndex]
            if (!isListItem(line)) break

            val currentLevel = getIndentLevel(line)
            val trimmedLine = line.trimStart()

            when {
                // 同级别无序列表项
                currentLevel == baseLevel && trimmedLine.startsWith("- ") &&
                        !trimmedLine.matches(Regex("""^- \[([ x])] .*$""")) -> {
                    items.add(unescapeText(trimmedLine.substring(2).trimStart()))
                    currentIndex++
                }

                // 更深层级的嵌套项
                currentLevel > baseLevel -> {
                    val (nestedElements, nextIndex) = parseNestedList(lines, currentIndex)
                    nestedItems.addAll(nestedElements)
                    currentIndex = nextIndex
                }

                // 更浅层级或不同类型，结束当前解析
                else -> break
            }
        }

        val result = mutableListOf<MarkdownElement>()
        if (items.isNotEmpty()) {
            result.add(UnorderedList(items, baseLevel))
        }
        result.addAll(nestedItems)

        return Pair(result, currentIndex)
    }

    /**
     * 解析有序列表项目
     */
    private fun parseOrderedListItems(
        lines: List<String>,
        startIndex: Int,
        baseLevel: Int
    ): Pair<List<MarkdownElement>, Int> {
        val items = mutableListOf<String>()
        val nestedItems = mutableListOf<MarkdownElement>()
        var currentIndex = startIndex

        while (currentIndex < lines.size) {
            val line = lines[currentIndex]
            if (!isListItem(line)) break

            val currentLevel = getIndentLevel(line)
            val trimmedLine = line.trimStart()

            when {
                // 同级别有序列表项
                currentLevel == baseLevel && trimmedLine.matches(Regex("""^\d+\. .*$""")) -> {
                    items.add(unescapeText(trimmedLine.substringAfter(". ").trimStart()))
                    currentIndex++
                }

                // 更深层级的嵌套项
                currentLevel > baseLevel -> {
                    val (nestedElements, nextIndex) = parseNestedList(lines, currentIndex)
                    nestedItems.addAll(nestedElements)
                    currentIndex = nextIndex
                }

                // 更浅层级或不同类型，结束当前解析
                else -> break
            }
        }

        val result = mutableListOf<MarkdownElement>()
        if (items.isNotEmpty()) {
            result.add(OrderedList(items, baseLevel))
        }
        result.addAll(nestedItems)

        return Pair(result, currentIndex)
    }

}
