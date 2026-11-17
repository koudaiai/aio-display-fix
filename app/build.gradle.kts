plugins {
    id("com.android.application")
}

android {
    namespace = "com.koudaiai.aiofix"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.koudaiai.aiofix"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // 只用于编译期，运行时由 LSPosed 提供
    compileOnly("de.robv.android.xposed:api:82")
}
