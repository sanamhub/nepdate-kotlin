// The setup shared with :nepdate-patro (toolchain, explicit API, ABI, Kover, animalsniffer,
// javaApiTest, POM) is in the root build.gradle.kts.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.animalsniffer)
}

dependencies {
    testImplementation(libs.jazzer.junit)
}

mavenPublishing {
    coordinates("io.github.sanamhub", "nepdate", version.toString())
    pom {
        name.set("nepdate")
        description.set("Bikram Sambat (Nepali calendar) dates for Kotlin, Java and Android.")
    }
}
