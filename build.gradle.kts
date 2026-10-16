import com.vanniktech.maven.publish.MavenPublishBaseExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSniffer
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
        // JavaTime.kt is the only file that touches java.time and is documented as needing it
        // (ADR-0004 gate 8); checkCoreSplit still proves no other core class references java.time.
        tasks.withType<AnimalSniffer>().configureEach {
            exclude("**/NepaliDateJavaTime.class")
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

// ADR-0004 gate 7, K4-05: the core artifact carries no patro data and doesn't depend on the patro.
evaluationDependsOn(":nepdate")
evaluationDependsOn(":nepdate-patro")

val checkCoreSplit by tasks.registering {
    description = "Fails if the core jar or POM refers to the patro, or the core uses java.text or java.time outside JavaTime.kt."
    group = "verification"
    val coreJar = project(":nepdate").tasks.named<Jar>("jar").flatMap { it.archiveFile }
    val patroJar = project(":nepdate-patro").tasks.named<Jar>("jar").flatMap { it.archiveFile }
    val corePomTask = project(":nepdate").tasks.named<GenerateMavenPom>("generatePomFileForMavenPublication")
    val corePom = corePomTask.map { it.destination }
    dependsOn(":nepdate:jar", ":nepdate-patro:jar", corePomTask)
    inputs.file(coreJar)
    inputs.file(patroJar)
    inputs.file(corePom)
    doLast {
        val marker = "World Day Against Human Trafficking".toByteArray(Charsets.UTF_8)

        fun ByteArray.has(needle: ByteArray): Boolean =
            (0..size - needle.size).any { start -> needle.indices.all { this[start + it] == needle[it] } }

        fun entries(jar: File): Map<String, ByteArray> =
            java.util.zip.ZipFile(jar).use { zip ->
                zip.entries().toList().filter { !it.isDirectory }.associate { it.name to zip.getInputStream(it).readBytes() }
            }

        val problems = mutableListOf<String>()
        if ("nepdate-patro" in corePom.get().readText()) problems += "the core POM names nepdate-patro"
        val core = entries(coreJar.get().asFile)
        core.filterValues { it.has(marker) }.keys.forEach { problems += "core jar entry $it holds patro data" }
        if (entries(patroJar.get().asFile).values.none { it.has(marker) }) problems += "the patro jar lacks the patro data"
        val javaText = "java/text/".toByteArray()
        val javaTime = "java/time/".toByteArray()
        for ((name, bytes) in core.filterKeys { it.endsWith(".class") }) {
            if (bytes.has(javaText)) problems += "$name uses java.text"
            if (name != "io/github/sanamhub/nepdate/NepaliDateJavaTime.class" && bytes.has(javaTime)) {
                problems += "$name uses java.time outside JavaTime.kt"
            }
        }
        if (problems.isNotEmpty()) throw GradleException("checkCoreSplit:\n" + problems.joinToString("\n"))
    }
}
