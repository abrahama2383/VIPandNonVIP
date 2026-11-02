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
            autoStart {
                applicationId("0723d7db-6587-4356-ba64-a3082d0452db")
                beaconUrl("https://bf97052cfu.bf.dynatrace.com/mbeacon")
            }
            userOptIn(true)
            agentBehavior.startupLoadBalancing(true)
            sessionReplay.enabled(true)
        }
    }
}
