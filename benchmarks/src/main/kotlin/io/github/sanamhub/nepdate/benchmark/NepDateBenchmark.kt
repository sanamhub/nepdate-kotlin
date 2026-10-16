package io.github.sanamhub.nepdate.benchmark

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.CalendarInfo
import io.github.sanamhub.nepdate.patro.Patro
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * B1 to B8 and B2e of shared/spec/BENCHMARK.md section 3, for this library. Each method runs over
 * the 64 inputs; [OperationsPerInvocation] makes JMH report time and bytes per single operation.
 * Every result feeds the returned sum so the JIT can't drop the work.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class NepDateBenchmark {
    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b1BsToAd(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.bs) {
            val ad = NepaliDate.of(y, m, d)
            acc += ad.gregorianYear + ad.gregorianMonth + ad.gregorianDay
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b2AdToBs(): Int {
        var acc = 0
        for ((y, m, d) in Inputs.ad) {
            val bs = NepaliDate.ofGregorian(y, m, d)
            acc += bs.year + bs.monthValue + bs.dayOfMonth
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b3AddDays(): Int {
        var acc = 0
        for (date in Inputs.dates) {
            val r = date.plusDays(1000)
            acc += r.year + r.monthValue + r.dayOfMonth
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b4AddMonthsClamp(): Int {
        var acc = 0
        for (date in Inputs.dates) {
            val r = date.plusMonths(13)
            acc += r.year + r.monthValue + r.dayOfMonth
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b5LongNepali(): Int {
        var acc = 0
        for (date in Inputs.dates) acc += date.toLongString(weekday = true, lang = Lang.NEPALI).length
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b6StrictParse(): Int {
        var acc = 0
        for (s in Inputs.text) {
            val r = NepaliDate.parseOrNull(s)
            if (r != null) acc += r.year + r.monthValue + r.dayOfMonth
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b7PatroInfo(): Int {
        var acc = 0
        for (date in Inputs.patroDates) acc += consume(Patro.info(date))
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b8MonthGrid(): Int {
        var acc = 0
        for (date in Inputs.patroDates) {
            val y = date.year
            val m = date.monthValue
            for (d in 1..date.lengthOfMonth()) acc += consume(Patro.info(NepaliDate.of(y, m, d)))
        }
        return acc
    }

    @Benchmark
    @OperationsPerInvocation(Inputs.COUNT)
    fun b2eAdToBsEscapes(blackhole: Blackhole) {
        for ((y, m, d) in Inputs.ad) blackhole.consume(NepaliDate.ofGregorian(y, m, d))
    }

    companion object {
        /** The hub's Consume: read the tithi names, the event count and the holiday flag. */
        fun consume(info: CalendarInfo): Int {
            val tithi = info.tithi
            return (tithi?.nepali?.length ?: 0) + (tithi?.english?.length ?: 0) +
                info.events.size + if (info.isPublicHoliday) 1 else 0
        }
    }
}
