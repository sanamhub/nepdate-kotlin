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

rootProject.name = "android-size"

include(":app")

// The root nepdate-kotlin build provides :nepdate and :nepdate-patro. Substitution makes the
// sample resolve io.github.sanamhub:nepdate(-patro) to those modules instead of Maven Central.
includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.sanamhub:nepdate")).using(project(":nepdate"))
        substitute(module("io.github.sanamhub:nepdate-patro")).using(project(":nepdate-patro"))
    }
}
