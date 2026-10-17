// The setup shared with :nepdate-patro (toolchain, explicit API, ABI, Kover, animalsniffer,
// javaApiTest, POM) is in the root build.gradle.kts.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.animalsniffer)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.pitest)
}

dependencies {
    testImplementation(libs.jazzer.junit)
}

// K7: PIT mutates the core only. The generated tables are inputs, not code under test; PIT
// otherwise spends most of its time on Calendar_generatedKt and the Kotlin when() mappings.
pitest {
    pitestVersion.set(libs.versions.pitestVersion.get())
    junit5PluginVersion.set(libs.versions.pitestJunit5Version.get())
    targetClasses.set(setOf("io.github.sanamhub.nepdate.*"))
    targetTests.set(setOf("io.github.sanamhub.nepdate.*", "io.github.sanamhub.nepdate.catalog.*"))
    excludedClasses.set(
        setOf(
            "io.github.sanamhub.nepdate.Calendar_generatedKt",
            "io.github.sanamhub.nepdate.*WhenMappings",
        ),
    )
    // The fuzz and random tests start a fuzzing runtime or generate 200,000 inputs. That is useful
    // once per CI run, but PIT would repeat it for every mutant. The concurrency test is about
    // class initialisation, not date behaviour. The deterministic tests still cover these paths.
    excludedTestClasses.set(
        setOf(
            "io.github.sanamhub.nepdate.ParseFuzzTest",
            "io.github.sanamhub.nepdate.ParseRandomTest",
            "io.github.sanamhub.nepdate.CalendarConcurrentInitTest",
        ),
    )
    // Kotlin emits Intrinsics null checks for non-null expressions. PIT's Kotlin plugin filters
    // them; the free plugin has no release, so the call filter does the same job here.
    avoidCallsTo.set(setOf("kotlin.jvm.internal.Intrinsics"))
    threads.set(4)
    outputFormats.set(setOf("HTML", "XML"))
    timestampedReports.set(false)
    // K7-03: one point below the measured score. The same properties drive scripts/mutation.sh.
    mutationThreshold.set(providers.gradleProperty("nepdateMutationThreshold").orNull?.toInt() ?: 92)
    maxSurviving.set(providers.gradleProperty("nepdateMaxSurviving").orNull?.toInt() ?: 49)
    // The test task passes these two properties (root build.gradle.kts). PIT starts its own JVMs,
    // so the vector tests need them here too or they fail before any mutation is applied.
    jvmArgs.set(
        listOf(
            "-Dnepdate.shared=${rootProject.file("shared").absolutePath}",
            "-Dnepdate.repoRoot=${rootProject.projectDir.absolutePath}",
        ),
    )
}

mavenPublishing {
    coordinates("io.github.sanamhub", "nepdate", version.toString())
    pom {
        name.set("nepdate")
        description.set("Bikram Sambat (Nepali calendar) dates for Kotlin, Java and Android.")
    }
}
