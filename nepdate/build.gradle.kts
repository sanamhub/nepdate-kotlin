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

kover {
    reports {
        verify {
            rule {
                // ADR-0004 gate 3; the org target is 80 %.
                minBound(90)
            }
        }
    }
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
    testImplementation(libs.jqwik)
    testImplementation(libs.jazzer.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
}

animalsniffer {
    sourceSets = listOf(project.sourceSets.main.get())
}

// ADR-0004 gate 6: Java sources that call the public API as a Java user would (K1-07).
val javaApiTest: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
configurations[javaApiTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[javaApiTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val javaApiTestTask =
    tasks.register<Test>("javaApiTest") {
        description = "Calls every public symbol from Java."
        group = "verification"
        testClassesDirs = javaApiTest.output.classesDirs
        classpath = javaApiTest.runtimeClasspath
        useJUnitPlatform()
    }

tasks.check {
    dependsOn(javaApiTestTask)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("nepdate.shared", rootProject.file("shared").absolutePath)
    // CI also runs the tests on Java 11, the bytecode target: -PtestJavaVersion=11.
    providers.gradleProperty("testJavaVersion").orNull?.let { version ->
        javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(version)) })
    }
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
