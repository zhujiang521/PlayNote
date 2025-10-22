package com.zj.ink.data

val markdownSample = """
# 📝 PlayNote Markdown 完整示例

---

## 1️⃣ 标题示例 (H1-H6)

# 一级标题 H1
## 二级标题 H2
### 三级标题 H3
#### 四级标题 H4
##### 五级标题 H5
###### 六级标题 H6

---

## 2️⃣ 文本格式示例

**这是加粗文本**

*这是斜体文本*

~~这是删除线文本~~

==这是高亮文本==

***这是加粗斜体文本***

**~~这是加粗删除线文本~~**

---

## 3️⃣ 列表示例

### 无序列表
- 第一项
- 第二项
- 第三项

### 有序列表
1. 第一步
2. 第二步
3. 第三步

### 无序列表嵌套（多级）
- 一级列表项
    - 二级列表项
        - 三级列表项
            - 四级列表项
    - 二级列表项2
- 一级列表项2

### 有序列表嵌套
1. 第一章
    1. 第一节
    2. 第二节
        1. 第一小节
        2. 第二小节
2. 第二章

### 混合嵌套列表
1. 有序列表项1
    - 无序子项1
    - 无序子项2
        1. 有序子项1
        2. 有序子项2
2. 有序列表项2

### 任务列表
- [x] 已完成的任务
- [ ] 待办任务1
- [ ] 待办任务2
    - [x] 子任务1
    - [ ] 子任务2
        - [x] 子子任务1

---

## 4️⃣ 链接与图片示例

### 链接
[访问百度](https://www.baidu.com)

### 图片
![示例图片](https://img1.baidu.com/it/u=3860791285,218631115&fm=253&fmt=auto&app=138&f=JPEG?w=799&h=500)

---

## 5️⃣ 表格示例

### 左对齐表格
| 姓名 | 年龄 | 城市 |
|:-----|:-----|:-----|
| 张三 | 25 | 北京 |
| 李四 | 30 | 上海 |
| 王五 | 28 | 深圳 |

### 居中对齐表格
| 姓名 | 年龄 | 城市 |
|:----:|:----:|:----:|
| 张三 | 25 | 北京 |
| 李四 | 30 | 上海 |
| 王五 | 28 | 深圳 |

### 右对齐表格
| 姓名 | 年龄 | 城市 |
|-----:|-----:|-----:|
| 张三 | 25 | 北京 |
| 李四 | 30 | 上海 |
| 王五 | 28 | 深圳 |

### 混合对齐表格
| 商品名称 | 数量 | 单价 | 总价 |
|:---------|:----:|-----:|-----:|
| 苹果 | 10 | 5.00 | 50.00 |
| 香蕉 | 20 | 3.50 | 70.00 |
| 橙子 | 15 | 4.00 | 60.00 |

---

## 6️⃣ 代码示例

### 行内代码
这是一个行内代码示例：`System.out.println("Hello World");`

### Kotlin 代码块
```kotlin
fun main() {
    println("Hello, PlayNote!")
    val list = listOf(1, 2, 3, 4, 5)
    list.forEach { println(it) }
}
```

### Java 代码块
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World");
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        numbers.forEach(System.out::println);
    }
}
```

### Python 代码块
```python
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

# 打印斐波那契数列
for i in range(10):
    print(fibonacci(i))
```

### JavaScript 代码块
```javascript
function greet(name) {
    return `Hello, $ name!`;
}

const numbers = [1, 2, 3, 4, 5];
numbers.forEach(num => console.log(num));
```

### HTML 代码块
```html
<!DOCTYPE html>
<html>
<head>
    <title>PlayNote</title>
</head>
<body>
    <h1>Hello World</h1>
    <p>This is a paragraph.</p>
</body>
</html>
```

### CSS 代码块
```css
body {
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 20px;
    background-color: #f5f5f5;
}

h1 {
    color: #333;
    text-align: center;
}
```

### JSON 代码块
```json
{
    "name": "PlayNote",
    "version": "1.1.0",
    "features": [
        "Markdown支持",
        "画图功能",
        "语法高亮"
    ],
    "isAwesome": true
}
```

---

## 7️⃣ 引用示例

### 单级引用
> 这是一级引用文本

### 二级引用
> 这是一级引用
>> 这是二级引用

### 三级引用
> 这是一级引用
>> 这是二级引用
>>> 这是三级引用

### 多级引用（4-6级）
> 这是一级引用
>> 这是二级引用
>>> 这是三级引用
>>>> 这是四级引用
>>>>> 这是五级引用
>>>>>> 这是六级引用

---

## 8️⃣ 扩展语法示例

### 脚注
这是一个带有脚注的文本[^1]，这里还有另一个脚注[^2]。

[^1]: 这是第一个脚注的内容，用于提供额外的说明信息。
[^2]: 这是第二个脚注的内容，可以包含更详细的解释。

### 上标
爱因斯坦质能方程：E = mc^2^

数学表达式：X^2^ + Y^2^ = Z^2^

### 下标
水的化学式：H~2~O

二氧化碳：CO~2~

### 内联数学公式
这是内联数学公式：$ E = mc^2$，质能方程。

勾股定理：$ a^2 + b^2 = c^2$

---

## 9️⃣ 其他元素

### 分割线

---

### 转义字符
\*这不是斜体\*

\#这不是标题

\[这不是链接\]

---

## 📋 支持的功能清单

此示例文件展示了PlayNote支持的所有Markdown语法：

✅ **标题** - H1到H6完整支持
✅ **文本格式** - 加粗、斜体、删除线、高亮、组合格式
✅ **列表** - 有序、无序、任务列表、复杂嵌套
✅ **链接与图片** - 超链接、图片展示
✅ **表格** - 支持左对齐、居中、右对齐、混合对齐
✅ **代码** - 行内代码、7种语言的语法高亮代码块
✅ **引用** - 支持1-6级嵌套引用
✅ **扩展语法** - 脚注、上下标、数学公式（LaTeX）
✅ **其他** - 分割线、转义字符

🎨 **性能优化** - LRU缓存、虚拟化渲染、分块处理
🌓 **主题适配** - 支持日间/夜间模式
📱 **小组件** - Jetpack Glance渲染器支持
""".trimIndent()
