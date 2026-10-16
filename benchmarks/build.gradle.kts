// :benchmarks is a standalone JVM module (ADR-0001 decision 3: never published). It benchmarks this
// library against the competitors in docs/competitors.md and provides two runnable mains:
// ColdInit (desktop first-use timing, ADR-0002 section 4) and AgreementCheck (data agreement).
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.allopen)
    alias(libs.plugins.kotlinx.benchmark)
}

kotlin {
    jvmToolchain(17)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

dependencies {
    implementation(project(":nepdate"))
    implementation(project(":nepdate-patro"))
    implementation(libs.nepali.date.picker.core) {
        // The Compose annotations in the published jar are not needed at runtime; the desktop
        // Compose runtime would otherwise drag in androidx.compose (Google Maven).
        exclude(group = "org.jetbrains.compose.runtime")
        exclude(group = "androidx.compose.runtime")
    }
    implementation(libs.adbs.core)
    implementation(libs.aagitoex.nepdate)
}

benchmark {
    targets {
        register("main") {
            this as kotlinx.benchmark.gradle.JvmBenchmarkTarget
            jmhVersion = libs.versions.jmh.get()
        }
    }
}

// ColdInit: starts 11 fresh JVMs per case and prints the median first-touch cost minus noop, in us.
val coldInit by tasks.registering(JavaExec::class) {
    description = "Measures core and patro class-initialisation cost in fresh JVMs."
    group = "benchmark"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.sanamhub.nepdate.benchmark.ColdInitKt")
}

// AgreementCheck: converts the 64 BENCHMARK section 2 dates with each competitor and prints yes/no.
val agreement by tasks.registering(JavaExec::class) {
    description = "Checks each competitor's conversions against shared/data."
    group = "benchmark"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.sanamhub.nepdate.benchmark.AgreementCheckKt")
}
