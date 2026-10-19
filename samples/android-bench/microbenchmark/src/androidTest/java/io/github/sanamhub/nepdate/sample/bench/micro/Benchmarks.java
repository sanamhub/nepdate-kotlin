package io.github.sanamhub.nepdate.sample.bench.micro;

import androidx.benchmark.junit4.BenchmarkRule;
import androidx.benchmark.junit4.BenchmarkRuleKt;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.sanamhub.nepdate.Lang;
import io.github.sanamhub.nepdate.NepaliDate;
import io.github.sanamhub.nepdate.patro.CalendarInfo;
import io.github.sanamhub.nepdate.patro.Patro;
import kotlin.Unit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * B1, B2, B5, B6 and B7 on one input each (and the B1 64-input loop), measured with
 * androidx.benchmark. Runs on a device via :microbenchmark:connectedCheck. Results
 * are written to a volatile field so the JIT cannot remove the measured work.
 */
@RunWith(AndroidJUnit4.class)
public class Benchmarks {
    @Rule public final BenchmarkRule rule = new BenchmarkRule();
    @SuppressWarnings("unused") private volatile int sink;

    @Test
    public void b1() {
        BenchmarkRuleKt.measureRepeated(rule, scope -> {
            NepaliDate d = NepaliDate.of(2081, 4, 15);
            sink = d.getGregorianYear() + d.getGregorianMonth() + d.getGregorianDay();
            return Unit.INSTANCE;
        });
    }

    @Test
    public void b2() {
        BenchmarkRuleKt.measureRepeated(rule, scope -> {
            NepaliDate d = NepaliDate.ofGregorian(2024, 7, 30);
            sink = d.getYear() + d.getMonthValue() + d.getDayOfMonth();
            return Unit.INSTANCE;
        });
    }

    @Test
    public void b5() {
        BenchmarkRuleKt.measureRepeated(rule, scope -> {
            NepaliDate d = NepaliDate.of(2081, 4, 15);
            sink = d.toLongString(false, true, true, Lang.NEPALI).length();
            return Unit.INSTANCE;
        });
    }

    @Test
    public void b6() {
        BenchmarkRuleKt.measureRepeated(rule, scope -> {
            NepaliDate d = NepaliDate.parseOrNull("2080/05/15");
            sink = d.getYear() + d.getMonthValue() + d.getDayOfMonth();
            return Unit.INSTANCE;
        });
    }

    @Test
    public void b7() {
        BenchmarkRuleKt.measureRepeated(rule, scope -> {
            NepaliDate d = NepaliDate.of(2081, 4, 15);
            CalendarInfo info = Patro.info(d);
            sink = info.getEvents().size();
            return Unit.INSTANCE;
        });
    }

    @Test
    public void b1Loop64() {
        BenchmarkRuleKt.measureRepeated(rule, scope -> {
            int acc = 0;
            for (NepaliDate d : Dates.DATES) {
                acc += d.getGregorianYear() + d.getGregorianMonth() + d.getGregorianDay();
            }
            sink = acc;
            return Unit.INSTANCE;
        });
    }
}
