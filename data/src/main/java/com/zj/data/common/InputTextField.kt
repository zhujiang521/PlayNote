package com.zj.data.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zj.data.R

/**
 * 输入文本框的自定义组件
 * 这块进行了自定义，官方的组件TextField有bug，高度最小都是56dp，无法满足需求
 *
 * @param value 当前文本框的值
 * @param onValueChange 文本变化时的回调
 * @param modifier 布局修饰符
 * @param visualTransformation 可视转换，用于密码隐藏等
 * @param enabled 是否启用文本框
 * @param singleLine 是否单行显示
 * @param keyboardOptions 键盘选项配置
 * @param keyboardActions 键盘操作配置
 * @param shape 文本框的形状
 * @param trailingIcon 后置图标
 * @param leadingIcon 前置图标
 * @param placeholder 占位符文本
 * @param contentPadding 内容内边距
 * @param colors 文本框颜色配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    textStyle: TextStyle = TextStyle(
        fontSize = dimensionResource(R.dimen.title_text).value.sp,
        color = colorResource(R.color.text_color)
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: String? = null,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    colors: TextFieldColors = textFieldColors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    ),
    maxLines: Int = 1,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
    ) { innerTextField ->

        TextFieldDefaults.DecorationBox(
            value = value,
            visualTransformation = visualTransformation,
            innerTextField = innerTextField,
            singleLine = singleLine,
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = contentPadding, // this is how you can remove the padding
            trailingIcon = trailingIcon,
            placeholder = {
                if (placeholder != null) {
                    Text(
                        placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = dimensionResource(R.dimen.title_text).value.sp
                    )
                }
            },
            leadingIcon = leadingIcon,
            shape = shape,
            colors = colors
        )
    }
}