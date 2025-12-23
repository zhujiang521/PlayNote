import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.zj.note"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig {
        applicationId = "com.zj.note"
        targetSdk = libs.versions.target.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()
        versionCode = getVerCode()
        versionName = getVerName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 启用矢量 drawable 优化
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            
            // 启用 Kotlin 编译器优化选项
            freeCompilerArgs.addAll(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    packaging {
        resources {
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes.add("META-INF/DEPENDENCIES")
            
            // 优化 APK 大小
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
        }
        
        // 启用 JNI 库打包优化
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

androidComponents {
    onVariants { applicationVariant ->
        applicationVariant.outputs.mapNotNull { it as? VariantOutputImpl }
            .forEach { variantOutput ->
                val name = "${variantOutput.versionName.get()}-${applicationVariant.name}"
                variantOutput.versionName.set(name)
                variantOutput.outputFileName.set("PlayNote-$name.apk")
            }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(project(":ink"))

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    
    // 添加 startup runtime 用于优化应用启动
    implementation(libs.androidx.startup.runtime)
}

fun getVerCode(): Int {
    val majorVersion = libs.versions.major.version.get()
    val minorVersion = System.getProperty("auto_build_num") ?: libs.versions.minor.version.get()
    return "$majorVersion.$minorVersion".replace(".", "").toInt()
}

fun getVerName(): String {
    val majorVersion = libs.versions.major.version.get()
    val minorVersion = System.getProperty("auto_build_num") ?: libs.versions.minor.version.get()
    val versionName = "$majorVersion.$minorVersion"
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    return "$versionName-$date"
}