package io.github.sanamhub.nepdate.benchmark

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro
import java.io.File

/**
 * Cold first-use cost of the core and patro tables (ADR-0002 section 4). With no arguments it
 * starts 11 fresh JVMs per case and prints the median minus the noop median in microseconds. With a
 * case name it runs one fresh JVM and prints the microseconds from main entry to the result.
 */
fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        val start = System.nanoTime()
        when (args[0]) {
            "noop" -> Unit
            "core" -> NepaliDate.ofGregorian(2024, 7, 30)
            "patro" -> Patro.info(NepaliDate.ofGregorian(2024, 7, 30))
            else -> {
                println("unknown case ${args[0]}")
                return
            }
        }
        println((System.nanoTime() - start) / 1000)
        return
    }

    val java = File(System.getProperty("java.home"), "bin/java").absolutePath
    val classpath = System.getProperty("java.class.path")
    val cases = listOf("noop", "core", "patro")
    val medians = cases.associateWith { case -> median(runCase(java, classpath, case)) }
    val noop = medians.getValue("noop")
    println("ColdInit (median of 11 fresh JVMs, minus the noop median):")
    println("  noop  = $noop us")
    println("  core  = ${medians.getValue("core") - noop} us")
    println("  patro = ${medians.getValue("patro") - noop} us")
}

private fun runCase(
    java: String,
    classpath: String,
    case: String,
): List<Long> =
    (1..11).map {
        val process =
            ProcessBuilder(
                java,
                "-Xshare:auto",
                "-cp",
                classpath,
                "io.github.sanamhub.nepdate.benchmark.ColdInitKt",
                case,
            ).redirectErrorStream(true).start()
        val out =
            process.inputStream
                .bufferedReader()
                .readText()
                .trim()
        process.waitFor()
        out.toLong()
    }

private fun median(values: List<Long>): Long = values.sorted()[values.size / 2]
