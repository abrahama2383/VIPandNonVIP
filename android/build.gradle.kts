buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.dynatrace.tools.android:gradle-plugin:8.+")
    }
}

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

apply(plugin = "com.dynatrace.instrumentation")

configure<com.dynatrace.tools.android.dsl.DynatraceExtension> {
    configurations {
        create("sampleConfig") {
            // v2: no auto-start. The app starts the agent itself, only for VIP users (MainActivity.kt).
            autoStart.enabled(false)
            userOptIn(true)
            sessionReplay.enabled(true)
        }
    }
}
