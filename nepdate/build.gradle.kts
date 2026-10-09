import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.animalsniffer)
}

kotlin {
    jvmToolchain(17)
    explicitApi()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        allWarningsAsErrors.set(true)
    }
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}

dependencies {
    // A call to an API missing on Android 5.0 fails the build (ADR-0004 gate 8).
    signature("${libs.animalsniffer.android.signature.get()}@signature")
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

animalsniffer {
    sourceSets = listOf(project.sourceSets.main.get())
}

tasks.test {
    useJUnitPlatform()
}

// Publishing itself is configured in K5 (ADR-0005); the POM is set now so K0-01 can check it.
mavenPublishing {
    coordinates("io.github.sanamhub", "nepdate", version.toString())
    pom {
        name.set("nepdate")
        description.set("Bikram Sambat (Nepali calendar) dates for Kotlin, Java and Android.")
        inceptionYear.set("2026")
        url.set("https://github.com/sanamhub/nepdate-kotlin")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("sanamhub")
                name.set("Sanam Pakuwal")
                url.set("https://github.com/sanamhub")
            }
        }
        scm {
            url.set("https://github.com/sanamhub/nepdate-kotlin")
            connection.set("scm:git:https://github.com/sanamhub/nepdate-kotlin.git")
            developerConnection.set("scm:git:ssh://git@github.com/sanamhub/nepdate-kotlin.git")
        }
    }
}
