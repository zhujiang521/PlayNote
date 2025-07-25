package com.zj.ink.md


object MarkdownParser {
    fun parse(text: String): List<MarkdownElement> {
        val lines = text.split("\n")
        val result = mutableListOf<MarkdownElement>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trimStart()

            // 标题
            when {
                line.startsWith("### ") -> result.add(Heading(3, line.removePrefix("### ")))
                line.startsWith("## ") -> result.add(Heading(2, line.removePrefix("## ")))
                line.startsWith("# ") -> result.add(Heading(1, line.removePrefix("# ")))

                // 删除线
                line.contains("~~") -> {
                    result.add(Strikethrough(line.removePrefix("~~").removeSuffix("~~")))
                }

                // 加粗
                line.contains("**") -> {
                    result.add(Bold(line.removePrefix("**").removeSuffix("**")))
                }

                // 斜体
                line.contains("*") -> {
                    result.add(Italic(line.removePrefix("*").removeSuffix("*")))
                }

                // 图片
                line.startsWith("!") && line.contains("](") -> {
                    val url = line.substringAfter("(").substringBefore(")")
                    result.add(Image(url))
                }

                // 链接
                line.contains("](") -> {
                    val text = line.substringBefore("]").removePrefix("[")
                    val url = line.substringAfter("(").substringBefore(")")
                    result.add(Link(text, url))
                }

                // 代码块
                line.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].startsWith("```") && !lines[i].startsWith("`")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    result.add(
                        CodeBlock(
                            codeLines.joinToString("\n")
                        )
                    )
                }

                // 行内代码
                line.contains("`") -> {
                    result.add(Code(line.removePrefix("`").removeSuffix("`").replace("`", "")))
                }

                // 引用
                line.startsWith("> ") -> {
                    result.add(BlockQuote(line.removePrefix("> ")))
                }

                // 无序列表
                line.trimStart().startsWith("- ") -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && (lines[i].trimStart().startsWith("- "))) {
                        items.add(lines[i].trimStart().substring(2).trimStart())
                        i++
                    }
                    result.add(UnorderedList(items))
                }

                // 有序列表
                line.matches(Regex("""^\d+\. .*$""")) -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && lines[i].matches(Regex("""^\d+\. .*$"""))) {
                        items.add(lines[i].substringAfter(". ").trimStart())
                        i++
                    }
                    result.add(OrderedList(items))
                }

                // 分割线
                line == "---" -> {
                    result.add(Divider)
                }

                // 表格
                line.startsWith("|") && line.contains("|") -> {
                    val tableLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                        tableLines.add(lines[i].trim())
                        i++
                    }

                    if (tableLines.size >= 2) {
                        val headers = parseTableRow(tableLines[0])
                        val rows = tableLines.subList(2, tableLines.size).map { parseTableRow(it) }

                        result.add(Table(headers, rows))
                    }
                }

                // 普通段落
                line.isNotBlank() -> {
                    result.add(Paragraph(line))
                }
            }

            i++
        }

        return result
    }

    private fun parseTableRow(line: String): List<String> {
        return line
            .removePrefix("|")
            .removeSuffix("|")
            .split("|")
            .map { it.trim() }
    }

}
