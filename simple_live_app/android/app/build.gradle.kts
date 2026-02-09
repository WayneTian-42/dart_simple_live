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
        // ✅ 永远保证 debug signingConfig 可用（无需你提供任何东西）
        // Flutter/AGP 通常默认就有 debug，但显式写一下更稳
        getByName("debug")

        // ✅ release signing：只有当 key.properties 完整时才真正配置
        create("release") if (hasCompleteReleaseKeystore(keystoreProperties)) {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            isV1SigningEnabled = true
            isV2SigningEnabled = true
        }
    }

    buildTypes {
        release {
            // ✅ 你要的是：release 构建能跑，不上架也不想配 keystore
            // - 有 key.properties 且完整：用 release keystore
            // - 否则：自动回退到 debug keystore（Action 环境就不会炸）
            signingConfig = if (hasCompleteReleaseKeystore(keystoreProperties)) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }

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
