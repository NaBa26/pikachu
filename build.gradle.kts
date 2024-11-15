// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
}
//dependencies {
//    classpath("com.android.tools.build:gradle:7.2.1") // Parentheses are required
//}