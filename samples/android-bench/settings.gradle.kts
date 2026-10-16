pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android-bench"

include(":app", ":microbenchmark", ":macrobenchmark")

// The root nepdate-kotlin build provides :nepdate and :nepdate-patro.
includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.sanamhub:nepdate")).using(project(":nepdate"))
        substitute(module("io.github.sanamhub:nepdate-patro")).using(project(":nepdate-patro"))
    }
}
