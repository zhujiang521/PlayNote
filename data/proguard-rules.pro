# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html


-dontwarn java.lang.invoke.StringConcatFactory

# 保留通义千问 SDK 的所有类和方法
-keep class com.alibaba.dashscope.** { *; }
-keep interface com.alibaba.dashscope.** { *; }

# 保留 com.zj.data.model 包下的所有类和方法
-keep class com.zj.data.model.** { *; }
-keep interface com.zj.data.model.** { *; }

# 保留 Gson 所需的类和方法
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Synthetic
-keepattributes SourceFile,LineNumberTable

# 防止混淆 Gson 所需的构造函数
-keepclassmembers class * {
    public <init>();
}

# 防止混淆 Gson 所需的字段和方法
-keepclassmembers class * {
    public <fields>;
    public <methods>;
}

# 防止混淆 Gson 所需的构造函数
-keepclassmembers class * {
    public <init>();
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Synthetic
-keepattributes SourceFile,LineNumberTable

-keep class org.jsoup.** { *; }
-keep class org.apache.** { *; }
-keep class com.vladsch.** { *; }

# 防止混淆 OkHttp 所需的构造函数
-keepclassmembers class com.alibaba.dashscope.* {
    public <init>();
}

# 防止混淆 OkHttp 所需的字段和方法
-keepclassmembers class com.alibaba.dashscope.* {
    public <fields>;
    public <methods>;
}

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# 保留 com.zj.data 包下的所有类和方法
-keep class com.zj.data.** { *; }
-dontwarn com.zj.data.**
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient

# 明确保留 DataStoreUtils 类及其成员
-keep class com.zj.data.utils.DataStoreUtils { *; }

-dontwarn java.awt.geom.Dimension2D
-dontwarn java.awt.geom.Rectangle2D
# --- Apache POI (poi.ooxml) 混淆规则 ---
# 保留所有 POI 相关类及方法
-keep class org.apache.poi.** { *; }
-keep interface org.apache.poi.** { *; }

# 防止混淆 POI 的内部字段和方法（如反射调用）
-keepclassmembers class org.apache.poi.** {
    <fields>;
    <methods>;
}

# 忽略 POI 的警告（如内部未使用的类）
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.**
-dontwarn schemasMicrosoftComOfficeOffice.**
-dontwarn schemasMicrosoftComOfficeExcel.**
-dontwarn schemasMicrosoftComVml.**
-dontwarn org.w3c.dom.**

-keep class org.apache.poi.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class schemasMicrosoftComOfficeOffice.** { *; }
-keep class schemasMicrosoftComOfficeExcel.** { *; }
-keep class schemasMicrosoftComVml.** { *; }
-keep class org.w3c.dom.** { *; }

# --- Jsoup (org.jsoup) 混淆规则 ---
# 保留所有 Jsoup 类及方法
-keep class org.jsoup.** { *; }
-keep interface org.jsoup.** { *; }

# 防止混淆 Jsoup 的内部字段和方法
-keepclassmembers class org.jsoup.** {
    <fields>;
    <methods>;
}

# 忽略 Jsoup 的警告（如第三方依赖）
-dontwarn org.jsoup.**
-dontwarn org.w3c.dom.**
-dontwarn javax.xml.parsers.**
-dontwarn org.xml.sax.**

-keep class org.jsoup.** { *; }
-keep class org.w3c.dom.** { *; }
-keep class javax.xml.parsers.** { *; }
-keep class org.xml.sax.** { *; }

-keep class javax.imageio.** { *; }
-keep class javax.swing.** { *; }
-keep class java.awt.** { *; }
-keep class coil.Coil.** { *; }
-keep class com.microsoft.** { *; }
-keep class org.jsoup.** { *; }
-keep class org.apache.** { *; }
-keep class com.vladsch.** { *; }
-keep class javax.xml.** { *; }
-keep class net.sf.** { *; }
-keep class org.apache.batik.** { *; }
-keep class org.osgi.** { *; }
-keep class org.w3c.** { *; }

-dontwarn javax.imageio.**
-dontwarn javax.swing.**
-dontwarn java.awt.**
-dontwarn coil.Coil.**
-dontwarn com.microsoft.**
-dontwarn org.jsoup.**
-dontwarn org.apache.**
-dontwarn com.vladsch.**
-dontwarn javax.xml.**
-dontwarn net.sf.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.**
-dontwarn org.w3c.**

# 保留模型类字段名
-keep class com.zj.data.model.BingImageModelItem { *; }

# 保留 Gson 注解
-keepattributes Signature
-keepattributes *Annotation*
