plugins {
    kotlin("jvm") version "2.4.20"
    application
}

// run.sh passes the version under test, so the check follows gradle.properties or the released tag
// instead of naming one.
val nepdateVersion = providers.gradleProperty("nepdateVersion").get()

dependencies {
    implementation("io.github.sanamhub:nepdate:$nepdateVersion")
    implementation("io.github.sanamhub:nepdate-patro:$nepdateVersion") // only for tithi, holidays, events
}

application {
    mainClass.set("consumer.MainKt")
}

tasks.named<JavaExec>("run") {
    // Print the Devanagari strings correctly even on a Cp1252 Windows console.
    systemProperty("file.encoding", "UTF-8")
}

// Prints where the nepdate jars resolved from, so run.sh can prove which repository served them.
tasks.register("printNepdatePath") {
    doLast {
        configurations.runtimeClasspath.get().files
            .filter { it.name.startsWith("nepdate") && it.name.endsWith(".jar") }
            .forEach { println(it) }
    }
}
