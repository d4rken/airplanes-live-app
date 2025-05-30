plugins {
    `kotlin-dsl`
    `java-library`
}

repositories {
    google()
    mavenCentral()
}
dependencies {
    implementation("com.android.tools.build:gradle:8.10.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
    implementation("com.squareup:javapoet:1.13.0")
}