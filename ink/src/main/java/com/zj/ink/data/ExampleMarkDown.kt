package com.zj.ink.data

val markdownSample = """
# 标题1
## 标题2
### 标题3

**加粗文本**
*斜体文本*
~~删除线文本~~

1. 有序列表项1
2. 有序列表项2

   - 子列表项1
   - 子列表项2

[链接文本](https://www.baidu.com)

![示例图片](https://img1.baidu.com/it/u=3860791285,218631115&fm=253&fmt=auto&app=138&f=JPEG?w=799&h=500)

| 表头1 | 表头2 | 表头3 |
|-------|-------|-------|
| 内容A | 内容B | 内容C |
| 内容D | 内容E | 内容F |

代码块（行内）：`System.out.println("Hello World");`

代码块（多行）：
```
public class Demo {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```

> 引用文本

---

此内容包含：
- 标题（# 到 ###）
- 文本样式（加粗、斜体、删除线）
- 列表（有序、无序）
- 超链接
- 图片
- 表格
- 代码块（行内和多行）
- 引用
- 分割线
""".trimIndent()
