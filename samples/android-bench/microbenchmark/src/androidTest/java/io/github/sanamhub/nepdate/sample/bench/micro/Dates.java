package io.github.sanamhub.nepdate.sample.bench.micro;

import io.github.sanamhub.nepdate.NepaliDate;

/** The 64 BENCHMARK section 2 dates, for the 64-input loop benchmark. */
public final class Dates {
    public static final NepaliDate[] DATES = build();

    private Dates() {}

    private static NepaliDate[] build() {
        long s0 = NepaliDate.of(2000, 1, 1).toEpochDay();
        long s1 = NepaliDate.of(2090, 12, 30).toEpochDay();
        long step = (s1 - s0) / 63;
        NepaliDate[] dates = new NepaliDate[64];
        for (int i = 0; i < 64; i++) {
            dates[i] = NepaliDate.ofEpochDay(s0 + i * step);
        }
        return dates;
    }
}
