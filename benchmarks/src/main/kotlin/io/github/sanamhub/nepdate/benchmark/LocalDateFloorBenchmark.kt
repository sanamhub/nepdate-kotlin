package io.github.sanamhub.nepdate.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Not a competitor: the java.time floor for B1 and B2, so a reader sees how close the conversions
 * are to the JDK. `LocalDate.of` is the absolute floor for any "construct a date" operation.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class LocalDateFloorBenchmark {
    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun localDateOfToEpochDay(): Long {
        var acc = 0L
        for ((y, m, d) in Inputs.ad) acc += LocalDate.of(y, m, d).toEpochDay()
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun localDateOfEpochDayFields(): Int {
        var acc = 0
        for (e in Inputs.epochDays) {
            val ld = LocalDate.ofEpochDay(e)
            acc += ld.year + ld.monthValue + ld.dayOfMonth
        }
        return acc
    }
}
