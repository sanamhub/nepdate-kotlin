package io.github.sanamhub.nepdate.benchmark

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.util.concurrent.TimeUnit

/**
 * The B1, B2 and B5 operations of `io.github.shivathapaa:nepali-date-picker-core` 3.3.0.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class ShivathapaaBenchmark {
    private lateinit var locale: NepaliDateLocale
    private lateinit var weekdays: IntArray

    @Setup
    fun setup() {
        locale =
            NepaliDateLocale(
                NepaliDatePickerLang.NEPALI,
                NepaliDateFormatStyle.FULL,
                NameFormat.FULL,
                NameFormat.FULL,
                DigitScript.DEVANAGARI,
            )
        weekdays = IntArray(Inputs.COUNT) { i -> Inputs.dates[i].dayOfWeek.ordinal + 1 }
    }

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

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b5LongNepali(): Int {
        var acc = 0
        for (i in Inputs.bs.indices) {
            val (y, m, d) = Inputs.bs[i]
            acc += NepaliDateConverter.formatNepaliDate(y, m, d, weekdays[i], locale).length
        }
        return acc
    }
}
