# ============ 基础 Android 规则 ============
# 保留注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations

# 保留默认构造函数
-keepclassmembers class * {
    public <init>(...);
}

# 保留 Parcelable 实现类
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable 实现类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============ Kotlin 相关规则 ============
# 保留 Kotlin 内部类和方法
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    <fields>;
}
-keepclassmembers class kotlin.jvm.internal.Intrinsics {
    *** checkParameterIsNotNull(...);
    *** checkNotNull(...);
    *** checkExpressionValueIsNotNull(...);
    *** checkFieldIsNotNull(...);
    *** checkReturnedValueIsNotNull(...);
    *** throwUninitializedPropertyAccessException(...);
}

# 保留 Kotlin 协程相关类
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.jvm.functions.** { *; }

# ============ Jetpack Compose 规则 ============
# Compose 编译器生成的类
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }

# 保留 Compose 相关注解和类
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# 保留 Navigation Compose 相关类
-keep class androidx.navigation.compose.** { *; }

# 保留 ViewModel 相关类
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.ViewModelProvider { *; }
-keep class androidx.lifecycle.viewmodel.compose.** { *; }

# ============ Room 数据库规则 ============
# 保留 Room 相关类和注解
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keep @interface androidx.room.** { *; }

# 保留数据库实体类
-keep class com.zj.data.room.** { *; }
-keep class com.zj.data.model.** { *; }

# 保留 DAO 接口
-keep interface com.zj.data.room.** { *; }

# ============ Hilt/Dagger 规则 ============
# 保留 Hilt 注解
-keep class dagger.** { *; }
-keep interface dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class javax.annotation.** { *; }

# 保留 Hilt 生成的类
-keep class hilt_aggregated_deps.** { *; }
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }

# 保留 Hilt 组件
-keep class *_HiltComponents { *; }
-keep class *_HiltModules { *; }
-keep class *_Factory { *; }
-keep class *_MembersInjector { *; }

# 保留 Hilt ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ============ CommonMark Markdown 解析器 ============
# 保留 CommonMark 相关类
-keep class org.commonmark.** { *; }
-keep interface org.commonmark.** { *; }

# 保留 CommonMark 扩展
-keep class org.commonmark.ext.** { *; }
-keep interface org.commonmark.ext.** { *; }

# ============ iText HTML to PDF ============
# 保留 iText 相关类
-keep class com.itextpdf.** { *; }
-keep interface com.itextpdf.** { *; }

# ============ Coil 图片加载库 ============
# 保留 Coil 相关类
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ============ DataStore ============
# 保留 DataStore 相关类
-keep class androidx.datastore.** { *; }
-keep interface androidx.datastore.** { *; }

# ============ Lottie 动画 ============
# 保留 Lottie 相关类
-keep class com.airbnb.lottie.** { *; }
-keep interface com.airbnb.lottie.** { *; }

# ============ Paging 3 ============
# 保留 Paging 相关类
-keep class androidx.paging.** { *; }
-keep interface androidx.paging.** { *; }

# ============ Glance Widgets ============
# 保留 Glance 相关类
-keep class androidx.glance.** { *; }
-keep interface androidx.glance.** { *; }

# ============ 自定义项目规则 ============
# 保留应用包下的所有类（可根据需要调整）
-keep class com.zj.note.** { *; }
-keep class com.zj.data.** { *; }
-keep class com.zj.ink.** { *; }

# 保留 ViewModel 类
-keep class * extends androidx.lifecycle.ViewModel { *; }

# 保留数据模型类
-keep class com.zj.data.model.** { *; }

# 保留数据库相关类
-keep class com.zj.data.room.** { *; }

# 保留工具类
-keep class com.zj.data.utils.** { *; }

# 保留 ViewModels
-keep class com.zj.ink.data.**ViewModel { *; }
-keep class com.zj.data.**ViewModel { *; }

# 保留 Hilt 模块
-keep class com.zj.ink.data.**Module { *; }
-keep class com.zj.data.**Module { *; }

# 保留导出相关的类
-keep class com.zj.data.utils.MarkdownExporter { *; }

# ============ 其他第三方库 ============
# 保留 Zoomable 相关类
-keep class net.engawapg.lib.zoomable.** { *; }

# 保留 AndroidX Ink 相关类
-keep class androidx.ink.** { *; }

# ============ 优化规则 ============
# 移除日志输出
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# 允许访问修饰符优化
-allowaccessmodification

# 优化时允许更激进的策略
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 优化级别
-optimizationpasses 5
