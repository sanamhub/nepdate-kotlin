plugins {
    kotlin("jvm") version "2.4.20"
    application
}

dependencies {
    implementation("io.github.sanamhub:nepdate:0.1.0")
    implementation("io.github.sanamhub:nepdate-patro:0.1.0")
}

application {
    mainClass.set("consumer.MainKt")
}

tasks.named<JavaExec>("run") {
    // Print the Devanagari strings correctly even on a Cp1252 Windows console.
    systemProperty("file.encoding", "UTF-8")
}

// Prints where the nepdate jars resolved from, so the run script can prove mavenLocal served them.
tasks.register("printNepdatePath") {
    doLast {
        configurations.runtimeClasspath.get().files
            .filter { it.name.startsWith("nepdate") && it.name.endsWith(".jar") }
            .forEach { println(it) }
    }
}
