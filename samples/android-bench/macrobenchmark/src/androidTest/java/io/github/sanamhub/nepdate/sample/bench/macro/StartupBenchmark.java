package io.github.sanamhub.nepdate.sample.bench.macro;

import androidx.benchmark.macro.BaselineProfileMode;
import androidx.benchmark.macro.CompilationMode;
import androidx.benchmark.macro.StartupTimingMetric;
import androidx.benchmark.macro.TraceSectionMetric;
import androidx.benchmark.macro.junit4.MacrobenchmarkRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import kotlin.Unit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Cold-start timing of the target app, with the nepdate-core-init and nepdate-patro-init trace
 * sections from SampleApp.onCreate. Runs on a device via :macrobenchmark:connectedCheck.
 */
@RunWith(AndroidJUnit4.class)
public class StartupBenchmark {
    @Rule public final MacrobenchmarkRule rule = new MacrobenchmarkRule();

    @Test
    public void startupNone() {
        rule.measureRepeated(
                "io.github.sanamhub.nepdate.sample.bench",
                Arrays.asList(
                        new StartupTimingMetric(),
                        new TraceSectionMetric("nepdate-core-init"),
                        new TraceSectionMetric("nepdate-patro-init")),
                new CompilationMode.None(),
                10,
                scope -> {
                    scope.startActivityAndWait();
                    return Unit.INSTANCE;
                });
    }

    @Test
    public void startupPartial() {
        rule.measureRepeated(
                "io.github.sanamhub.nepdate.sample.bench",
                Arrays.asList(
                        new StartupTimingMetric(),
                        new TraceSectionMetric("nepdate-core-init"),
                        new TraceSectionMetric("nepdate-patro-init")),
                new CompilationMode.Partial(BaselineProfileMode.Disable, 3),
                10,
                scope -> {
                    scope.startActivityAndWait();
                    return Unit.INSTANCE;
                });
    }
}
