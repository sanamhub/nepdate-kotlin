package io.github.sanamhub.nepdate.benchmark

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.util.concurrent.TimeUnit

/**
 * The B1 and B2 operations of `io.github.shivathapaa:nepali-date-picker-core` 3.3.0.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class ShivathapaaBenchmark {
    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b1BsToAd(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.bs) {
            val ad = NepaliDateConverter.convertNepaliToEnglish(y, m, d)
            acc += ad.year + ad.month + ad.dayOfMonth
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b2AdToBs(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.ad) {
            val bs = NepaliDateConverter.convertEnglishToNepali(y, m, d)
            acc += bs.year + bs.month + bs.dayOfMonth
        }
        return acc
    }
}
