import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

// 读取 key.properties（如果不存在就为空 Properties，不再强转 String）
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// 判断 release keystore 是否“完整可用”
fun hasCompleteReleaseKeystore(p: Properties): Boolean {
    val keyAlias = p.getProperty("keyAlias")
    val keyPassword = p.getProperty("keyPassword")
    val storeFile = p.getProperty("storeFile")
    val storePassword = p.getProperty("storePassword")
    return !keyAlias.isNullOrBlank()
            && !keyPassword.isNullOrBlank()
            && !storeFile.isNullOrBlank()
            && !storePassword.isNullOrBlank()
}

android {
    namespace = "com.xycz.simple_live"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    defaultConfig {
        applicationId = "com.xycz.simple_live"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    signingConfigs {
        // 保证 debug 存在（AGP 通常默认有，但显式获取更稳）
        getByName("debug")
    
        create("release") {
            // 只有当 key.properties 四个字段齐全时，才配置 release keystore
            val keyAliasVal = keystoreProperties.getProperty("keyAlias")
            val keyPasswordVal = keystoreProperties.getProperty("keyPassword")
            val storeFileVal = keystoreProperties.getProperty("storeFile")
            val storePasswordVal = keystoreProperties.getProperty("storePassword")
    
            val hasAll = !keyAliasVal.isNullOrBlank()
                    && !keyPasswordVal.isNullOrBlank()
                    && !storeFileVal.isNullOrBlank()
                    && !storePasswordVal.isNullOrBlank()
    
            if (hasAll) {
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
                storeFile = file(storeFileVal!!)
                storePassword = storePasswordVal
                isV1SigningEnabled = true
                isV2SigningEnabled = true
            }
            // 如果没有 hasAll：什么都不设置，后面 release 会 fallback 到 debug signing
        }
    }

    buildTypes {
        release {
            // ✅ release 构建仍然是 release（minify/shrink/proguard都照常）
            // 仅签名：有完整 release keystore -> 用 release；否则 -> 用 debug
            val rel = signingConfigs.getByName("release")
            signingConfig = if (rel.storeFile != null) rel else signingConfigs.getByName("debug")
    
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

flutter {
    source = "../.."
}
