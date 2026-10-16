package io.github.sanamhub.nepdate.benchmark

import com.nepalidate.ADBS.NepaliDateConverter.AdBs
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * The B1 and B2 operations of `com.github.dhiraj-var.adbs:adbs-core` v4.0.0. Its public API is
 * string-based (`ddMMyyyy` in, `java.util.Date` out; `dd-MM-yyyy` in, `y-m-d` out), so B1 and B2
 * include the string work a user must do.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class AdbsBenchmark {
    private lateinit var bsStrings: Array<String>
    private lateinit var adStrings: Array<String>

    @Setup
    fun setup() {
        bsStrings =
            Array(Inputs.COUNT) { i ->
                val (y, m, d) = Inputs.bs[i]
                "%02d%02d%04d".format(d, m, y)
            }
        adStrings =
            Array(Inputs.COUNT) { i ->
                val (y, m, d) = Inputs.ad[i]
                "%02d-%02d-%04d".format(d, m, y)
            }
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b1BsToAd(): Int {
        val calendar = Calendar.getInstance()
        var acc = 0
        for (s in bsStrings) {
            calendar.time = AdBs().convertBsToAd(s)
            acc += calendar[Calendar.YEAR] + calendar[Calendar.MONTH] + 1 + calendar[Calendar.DAY_OF_MONTH]
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b2AdToBs(): Int {
        var acc = 0
        for (s in adStrings) {
            val out = AdBs().convertAdToBs(s)
            val first = out.indexOf('-')
            val second = out.indexOf('-', first + 1)
            acc += out.substring(0, first).toInt() + out.substring(first + 1, second).toInt() +
                out.substring(second + 1).toInt()
        }
        return acc
    }
}
