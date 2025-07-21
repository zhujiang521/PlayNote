package com.zj.ink.widget


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

                // 分割线
                line == "---" -> {
                    result.add(Divider)
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
}
