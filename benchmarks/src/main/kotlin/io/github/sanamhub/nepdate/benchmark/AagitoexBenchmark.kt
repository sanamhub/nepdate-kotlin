package io.github.sanamhub.nepdate.benchmark

import io.github.aagitoex.nepdate.Converter
import io.github.aagitoex.nepdate.NepDate
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
 * The B1, B2, B3 and B4 operations of `io.github.aagitoex:nepdate` 0.0.1-beta01 (a java.time
 * wrapper). B2 necessarily pays for `LocalDate.of`; see the LocalDateFloorBenchmark floor.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class AagitoexBenchmark {
    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b1BsToAd(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.bs) {
            val ad = NepDate(y, m, d).ad
            acc += ad.year + ad.monthValue + ad.dayOfMonth
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b2AdToBs(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.ad) {
            val bs = Converter.fromADToBS(LocalDate.of(y, m, d))
            acc += bs.year + bs.month + bs.day
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b3AddDays(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.bs) {
            val r = NepDate(y, m, d).plusDays(1000)
            acc += r.year + r.month + r.day
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b4AddMonthsClamp(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.bs) {
            val r = NepDate(y, m, d).plusMonths(13)
            acc += r.year + r.month + r.day
        }
        return acc
    }
}
