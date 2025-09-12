pluginManagement {
    repositories {
        // --- میرورها (اختیاری: اگر گوگل اصلی کند/مسدود است) ---
        maven{url = uri("https://maven.myket.ir")}
        maven { url = uri("https://maven.aliyun.com/repository/google") }       // آینه‌ی Google Maven
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") } // آینه‌ی پلاگین‌ها
        maven { url = uri("https://maven-central-asia.storage-download.googleapis.com/maven2") } // آینه‌ی Maven Central (APAC)

        // --- منابع رسمی (همیشه نگه‌دار) ---
        google()
        mavenCentral()
        gradlePluginPortal()

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // --- میرورها (در صورت نیاز) ---
        maven { url = uri("https://maven.aliyun.com/repository/google") }        // فقط اگر Google اصلیت مشکل دارد
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://maven-central-asia.storage-download.googleapis.com/maven2") }

        // --- رسمی‌ها ---
        google()
        mavenCentral()
    }
}


rootProject.name = "SimpleNote"
include(":app")