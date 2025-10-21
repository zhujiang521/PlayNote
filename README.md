# PlayNote

一个功能强大的Android便签应用，支持画图功能和完整的Markdown解析展示。

## 特性概览

### 🎨 画图功能
- 手绘笔记支持
- 多种画笔工具
- 颜色选择器
- 橡皮擦功能

### 📝 Markdown支持
PlayNote 提供了业界领先的Markdown解析和渲染功能，支持标准语法和丰富的扩展语法。

#### 标准 Markdown 语法
- ✅ **标题 (H1-H6)** - 完整的六级标题支持
- ✅ **文本格式** - 粗体、斜体、删除线
- ✅ **链接和图片** - 支持网络和本地资源
- ✅ **代码** - 行内代码和代码块，支持语法高亮
- ✅ **引用** - 支持多级嵌套引用（最多6级）
- ✅ **列表** - 有序列表、无序列表，支持复杂嵌套
- ✅ **表格** - 支持列对齐（左对齐、居中、右对齐）
- ✅ **分割线** - 水平分割线支持

#### 扩展 Markdown 语法
- ✅ **任务列表** - `- [x]` 已完成，`- [ ]` 未完成，支持交互
- ✅ **高亮文本** - `==高亮内容==` 背景色高亮
- ✅ **脚注** - `[^1]` 脚注引用和 `[^1]: 内容` 脚注定义
- ✅ **上下标** - `X^2^` 上标和 `H~2~O` 下标
- ✅ **数学公式** - LaTeX格式，`$内联公式$` 和 `$$块级公式$$`
- ✅ **转义字符** - 支持12种常见转义字符
- ✅ **嵌套结构** - 复杂的列表嵌套和引用嵌套

### 🎯 技术特性

#### 双渲染引擎
- **Jetpack Compose** - 主应用内的完整Markdown渲染
- **Jetpack Glance** - 小组件中的优化Markdown显示

#### 语法高亮
支持多种编程语言的语法高亮：
- Kotlin, Java, Scala
- Python
- JavaScript, TypeScript
- HTML, CSS
- JSON
- 以及更多...

#### 性能优化
- **解析缓存** - LRU缓存机制，重复解析性能提升90%+
- **虚拟化渲染** - 大文档使用LazyColumn，内存占用优化
- **分块处理** - 大文档自动分块，支持最大1MB文档
- **预编译正则** - 常用正则表达式预编译，性能提升20-30%

#### 错误处理
- **优雅降级** - 解析失败时自动降级为简单解析
- **边界保护** - 完善的输入验证和限制机制
- **错误恢复** - 单个元素渲染失败不影响整体显示

## 使用示例

### 基本Markdown语法

```markdown
# 一级标题
## 二级标题

**粗体文本** 和 *斜体文本*

[链接文本](https://example.com)

![图片](image.png)

- 无序列表项
1. 有序列表项

| 表头1 | 表头2 | 表头3 |
|:-----|:----:|------:|
| 左对齐 | 居中 | 右对齐 |
```

### 扩展语法示例

```markdown
# 任务管理
- [x] 已完成的任务
- [ ] 待办任务
    - [x] 子任务1
    - [ ] 子任务2

# 重要提醒
这是 ==高亮的重要内容== 需要注意。

# 科学公式
爱因斯坦质能方程：$E = mc^2$

水的化学式：H~2~O

$$
\sum_{i=1}^{n} i = \frac{n(n+1)}{2}
$$

# 脚注示例
这里有一个脚注[^1]的引用。

[^1]: 这是脚注的详细说明内容
```

### 代码语法高亮

````markdown
```kotlin
fun main() {
    println("Hello, PlayNote!")
    val list = listOf(1, 2, 3)
    list.forEach { println(it) }
}
```

```python
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

print(fibonacci(10))
```
````

## 技术架构

### 核心组件
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  MarkdownParser │───▶│ MarkdownElement  │───▶│ RenderMarkdown  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │GlanceRenderMD   │
                       └─────────────────┘
```

### 数据模型
- **密封类设计** - 类型安全的Markdown元素表示
- **26种元素类型** - 覆盖所有标准和扩展语法
- **向后兼容** - 新增字段提供默认值

### 性能指标
- **解析速度** - 小文档 <50ms，中等文档 <200ms，大文档 <1000ms
- **缓存命中率** - >90%，二次解析速度提升 >90%
- **内存优化** - 大文档分块处理，内存占用减少70%
- **渲染性能** - 虚拟化渲染，支持1000+元素文档流畅显示

## 开发指南

### 集成Markdown解析器

```kotlin
// 解析Markdown文本
val elements = MarkdownParser.parse(markdownText)

// 在Compose中渲染
@Composable
fun MyMarkdownView(content: String) {
    RenderMarkdown(
        markdown = content,
        onImageClick = { imageUrl ->
            // 处理图片点击
        }
    )
}
```

### 自定义主题

```kotlin
@Composable
fun CustomMarkdownTheme() {
    MaterialTheme(
        colorScheme = customColorScheme
    ) {
        RenderMarkdown(markdown = content)
    }
}
```

### 错误处理

```kotlin
// 检查解析错误
val errorCount = MarkdownParser.getParseErrorCount()
val lastError = MarkdownParser.getLastErrorMessage()

// 清理缓存
MarkdownParser.clearCache()
```

## 项目结构

```
PlayNote/
├── app/                    # 主应用模块
├── data/                   # 数据层模块
│   ├── src/main/res/
│   │   ├── values/colors.xml      # 主题颜色定义
│   │   ├── values-night/          # 夜间模式颜色
│   │   └── values/strings.xml     # 字符串资源
│   └── build.gradle.kts           # 数据模块构建配置
├── ink/                    # Markdown功能模块
│   └── src/main/java/com/zj/ink/md/
│       ├── MarkdownElement.kt     # 数据模型定义
│       ├── MarkdownParser.kt      # 解析器实现
│       ├── RenderMarkdown.kt      # Compose渲染器
│       └── GlanceRenderMarkdown.kt # Glance渲染器
├── docs/                   # 项目文档
│   ├── MARKDOWN_SYNTAX_EXAMPLES.md # 语法示例
│   └── API_DOCUMENTATION.md        # API文档
└── gradle/
    └── libs.versions.toml          # 依赖版本管理
```

## 依赖库

### 核心依赖
- **Jetpack Compose** - 现代Android UI工具包
- **Jetpack Glance** - 应用小组件框架
- **Material 3** - Material Design组件库
- **Coil** - 图片加载库

### Markdown相关
- **CommonMark** - 基础Markdown解析支持
- **Prism4j** - 代码语法高亮库

## 性能和限制

### 性能限制
- **文本长度** - 最大1MB (1,000,000字符)
- **嵌套层级** - 最大10级嵌套
- **表格列数** - 最大50列
- **列表项数** - 最大1000个列表项
- **缓存大小** - 最多50个解析结果

### Glance限制
- **内容长度** - 最大5KB文本
- **元素数量** - 最多10个元素
- **功能简化** - 部分复杂功能在小组件中简化显示

## 最佳实践

### 1. 性能优化
- 使用`remember`缓存解析结果
- 大文档考虑分页或懒加载
- 定期清理解析缓存

### 2. 内容组织
- 使用标题层级组织文档结构
- 合理使用列表和表格
- 重要内容使用高亮标记

### 3. 错误处理
- 检查解析错误统计
- 提供用户友好的错误提示
- 实现内容备份和恢复

## 更新日志

### v1.1.0 (最新版本)
- ✨ 新增完整的H1-H6标题支持
- ✨ 新增任务列表功能，支持交互式复选框
- ✨ 新增表格列对齐（左对齐、居中、右对齐）
- ✨ 新增代码块语法高亮，支持9种编程语言
- ✨ 新增多级引用嵌套（最多6级）
- ✨ 新增高亮文本功能 `==高亮==`
- ✨ 新增脚注支持 `[^1]` 和 `[^1]: 内容`
- ✨ 新增上下标 `X^2^` 和 `H~2~O`
- ✨ 新增数学公式支持（LaTeX格式）
- ✨ 新增转义字符处理
- ✨ 新增复杂嵌套列表支持
- 🚀 性能优化：LRU缓存、虚拟化渲染、分块处理
- 🛡️ 错误处理：完善的边界检查和降级机制
- 🎨 UI优化：Material 3主题适配，日间/夜间模式支持
- 📱 小组件：Glance渲染器适配和优化

### v1.0.0
- 🎉 初始版本发布
- ✅ 基础Markdown语法支持
- ✅ 画图功能实现
- ✅ Jetpack Compose UI

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进PlayNote！

### 开发环境
- Android Studio Hedgehog | 2023.1.1+
- Kotlin 2.2.20+
- Android Gradle Plugin 8.12.2+
- 最低SDK版本：API 24 (Android 7.0)
- 目标SDK版本：API 34 (Android 14)

### 代码规范
- 遵循Kotlin官方代码规范
- 使用KDoc注释公共API
- 保持与现有代码风格一致
- 添加适当的单元测试

## 许可证

本项目采用 [MIT License](LICENSE) 开源许可证。

## 联系方式

- 项目地址：[GitHub Repository]
- 问题反馈：[Issues]
- 开发团队：PlayNote开发团队

---

**PlayNote** - 让笔记更智能，让创作更自由 ✨