// The setup shared with :nepdate (toolchain, explicit API, ABI, Kover, animalsniffer, javaApiTest,
// POM) is in the root build.gradle.kts.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.animalsniffer)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

dependencies {
    api(project(":nepdate"))
}

mavenPublishing {
    coordinates("io.github.sanamhub", "nepdate-patro", version.toString())
    pom {
        name.set("nepdate-patro")
        description.set("Patro (Nepali almanac: tithi, public holidays, events) for nepdate")
    }
}
