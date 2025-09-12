pluginManagement {
    repositories {
        if (System.getProperty("useMirrors") != null) {
            maven{url = uri("https://maven.myket.ir")}
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
            maven { url = uri("https://maven-central-asia.storage-download.googleapis.com/maven2") }
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (System.getProperty("useMirrors") != null) {
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
            maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
            maven { url = uri("https://maven-central-asia.storage-download.googleapis.com/maven2") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "SimpleNote"
include(":app")
