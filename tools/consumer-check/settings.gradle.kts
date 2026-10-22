pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// nepdateSource=local checks this checkout's publishToMavenLocal output: the nepdate artifacts come
// from mavenLocal only. nepdateSource=central checks a release: there is no mavenLocal, so a stale
// local copy cannot stand in for what Maven Central serves. Central supplies kotlin-stdlib either way.
val source = providers.gradleProperty("nepdateSource").getOrElse("local")
require(source == "local" || source == "central") { "nepdateSource must be local or central, not $source" }

dependencyResolutionManagement {
    repositories {
        if (source == "local") {
            exclusiveContent {
                forRepository { mavenLocal() }
                filter { includeGroup("io.github.sanamhub") }
            }
        }
        mavenCentral()
    }
}

rootProject.name = "kt-consumer"
