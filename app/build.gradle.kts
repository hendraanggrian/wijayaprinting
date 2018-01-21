import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.kotlin

import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

plugins {
    java
    kotlin("jvm")
    rsync
    buildconfig
    `junit-platform`
}

java.sourceSets {
    getByName("main") {
        java.srcDir("src")
        resources.srcDirs("res", "licenses")
    }
    getByName("test") {
        java.srcDir("tests/src")
        resources.srcDir("tests/res")
    }
}

rsync {
    srcDir("src")
    resDir("res")
    packageName("com.wijayaprinting")
    leadingSlash(true)
    debug(isDebug)
}

buildconfig {
    srcDir("src")
    packageName("com.wijayaprinting")
    artifactId("wijayaprinting")
    version(releaseVersion)
    debug(isDebug)
}

configure<JUnitPlatformExtension> {
    filters {
        engines {
            include("spek")
        }
    }
}

dependencies {
    implementation(project(":scene"))

    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("nosql-mongodb", nosqlMongodbVersion))

    implementation(rx("javafx", rxjavafxVersion))
    implementation(rx("kotlin", rxkotlinVersion))

    implementation(apache("commons", "lang3", commonsLangVersion))
    implementation(apache("commons", "math3", commonsMathVersion))
    implementation(apache("poi", "ooxml", poiVersion))

    implementation(guava)
    implementation(log4j12)

    testImplementation(kotlin("test", kotlinVersion))
    testImplementation(kotlin("reflect", kotlinVersion))
    testImplementation(spek("api", spekVersion)) {
        exclude("org.jetbrains.kotlin")
    }
    testRuntime(spek("junit-platform-engine", spekVersion)) {
        exclude("org.jetbrains.kotlin")
        exclude("org.junit.platform")
    }
    testImplementation(junitPlatform("runner", junitPlatformVersion))
}

fun JUnitPlatformExtension.filters(setup: FiltersExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(FiltersExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
}

fun FiltersExtension.engines(setup: EnginesExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(EnginesExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
}