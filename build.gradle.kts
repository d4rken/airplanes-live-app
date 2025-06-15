buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.10.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.1.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.6.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean").configure {
    delete("build")
}

//subprojects {
//    tasks.matching { it.name.contains("kapt") }.configureEach {
//        enabled = false
//    }
//}
