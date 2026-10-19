pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Provisions the JDK 17 toolchain (ADR-0001) on machines that only have a newer JDK.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        // JitPack hosts the two JitPack-only benchmark competitors. exclusiveContent
        // keeps every other dependency resolving from Maven Central only (ADR-0001 decision 4).
        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://jitpack.io")
                }
            }
            filter {
                includeGroup("com.github.dhiraj-var.adbs")
            }
        }
    }
}

rootProject.name = "nepdate-kotlin"

include(":nepdate", ":nepdate-patro", ":codegen", ":benchmarks")
