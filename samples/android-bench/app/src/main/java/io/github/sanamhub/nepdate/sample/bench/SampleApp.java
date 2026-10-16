package io.github.sanamhub.nepdate.sample.bench;

import android.app.Application;
import android.os.Trace;
import io.github.sanamhub.nepdate.NepaliDate;
import io.github.sanamhub.nepdate.patro.Patro;

/** Marks the first core and patro touches so the macrobenchmark can time them separately. */
public class SampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Trace.beginSection("nepdate-core-init");
        NepaliDate date = NepaliDate.ofGregorian(2024, 7, 30);
        Trace.endSection();
        Trace.beginSection("nepdate-patro-init");
        Patro.info(date);
        Trace.endSection();
    }
}
