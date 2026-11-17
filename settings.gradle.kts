pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 旧版 Xposed API 只发布在这个仓库
        maven { url = uri("https://api.xposed.info/") }
    }
}
rootProject.name = "aio-display-fix"
include(":app")
