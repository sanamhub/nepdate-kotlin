import com.vanniktech.maven.publish.MavenPublishBaseExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.animalsniffer) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    // The generated tables are long by design (ADR-0001 decision 6).
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude { it.file.name.endsWith(".generated.kt") }
        }
    }
    configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig.set(true)
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    }
    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        exclude("**/*.generated.kt")
    }
}

// The two published libraries share one setup (K4-01). Each module applies the plugins and sets its
// own artifact id, POM name and description, and dependencies.
val libraryProjects = setOf(":nepdate", ":nepdate-patro")

configure(subprojects.filter { it.path in libraryProjects }) {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmProjectExtension> {
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
            "testImplementation"(platform(libs.junit.bom))
            "testImplementation"(libs.junit.jupiter)
            "testImplementation"(kotlin("test-junit5"))
            "testImplementation"(libs.jqwik)
            "testRuntimeOnly"(libs.junit.platform.launcher)
        }

        // ADR-0004 gate 6: Java sources that call the public API as a Java user would (K1-07).
        val sourceSets = the<SourceSetContainer>()
        val main = sourceSets.getByName("main")
        val javaApiTest =
            sourceSets.create("javaApiTest") {
                compileClasspath += main.output
                runtimeClasspath += main.output
            }
        configurations[javaApiTest.implementationConfigurationName].extendsFrom(configurations["testImplementation"])
        configurations[javaApiTest.runtimeOnlyConfigurationName].extendsFrom(configurations["testRuntimeOnly"])

        val javaApiTestTask =
            tasks.register<Test>("javaApiTest") {
                description = "Calls every public symbol from Java."
                group = "verification"
                testClassesDirs = javaApiTest.output.classesDirs
                classpath = javaApiTest.runtimeClasspath
                useJUnitPlatform()
            }
        tasks.named("check") {
            dependsOn(javaApiTestTask)
        }

        tasks.named<Test>("test") {
            useJUnitPlatform()
            systemProperty("nepdate.shared", rootProject.file("shared").absolutePath)
            // CI also runs the tests on Java 11, the bytecode target: -PtestJavaVersion=11.
            providers.gradleProperty("testJavaVersion").orNull?.let { version ->
                val toolchains = project.extensions.getByType<JavaToolchainService>()
                javaLauncher.set(toolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(version)) })
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlinx.kover") {
        configure<KoverProjectExtension> {
            reports {
                verify {
                    rule {
                        // ADR-0004 gate 3; the org target is 80 %.
                        minBound(90)
                    }
                }
            }
        }
    }

    pluginManager.withPlugin("ru.vyarus.animalsniffer") {
        dependencies {
            // A call to an API missing on Android 5.0 fails the build (ADR-0004 gate 8).
            "signature"("${libs.animalsniffer.android.signature.get()}@signature")
        }
        configure<AnimalSnifferExtension> {
            sourceSets = listOf(the<SourceSetContainer>().getByName("main"))
        }
    }

    // Publishing itself is configured in K5 (ADR-0005); the POM is set now so it can be checked.
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            pom {
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
    }
}
