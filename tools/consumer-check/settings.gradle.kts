pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// The nepdate artifacts come from mavenLocal (the publishToMavenLocal output under test);
// Maven Central only supplies kotlin-stdlib and the other transitive dependencies.
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "kt-consumer"
