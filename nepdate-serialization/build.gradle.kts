// Optional artifact (PARITY D-11): the kotlinx.serialization hook for NepaliDate. The core
// artifact stays dependency free, so a project that does not serialize Nepali dates never
// resolves kotlinx-serialization.
//
// The setup shared with :nepdate and :nepdate-patro (toolchain, explicit API, ABI, Kover,
// animalsniffer, javaApiTest, POM) is in the root build.gradle.kts.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.animalsniffer)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

dependencies {
    api(project(":nepdate"))
    api(libs.kotlinx.serialization.core)
    testImplementation(libs.kotlinx.serialization.json)
}

// This artifact has no property-based tests of its own, and the jqwik engine fails discovery on
// the serializer classes that the compiler generates for a @Serializable type declared inside a
// test, so only the JUnit Jupiter engine runs here.
tasks.named<Test>("test") {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

mavenPublishing {
    coordinates("io.github.sanamhub", "nepdate-serialization", version.toString())
    pom {
        name.set("nepdate-serialization")
        description.set("kotlinx.serialization support for nepdate's Bikram Sambat dates")
    }
}
