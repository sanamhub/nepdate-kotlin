package io.github.sanamhub.nepdate.patro;

import io.github.sanamhub.nepdate.Lang;
import io.github.sanamhub.nepdate.NepaliDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * K5-01: the Java snippet under `## Quick start` in README.md must be exactly the marked region
 * below, and the same snippet must compile and run (this file is the javaApiTest source set).
 */
class ReadmeExamples {
    @Test
    void javaExampleRuns() {
        // README-START
        NepaliDate date = NepaliDate.of(2081, 4, 15);
        NepaliDate fromAd = NepaliDate.ofGregorian(2024, 7, 30);
        String longForm = date.toLongString(false, true, true, Lang.NEPALI);
        NepaliDate parsed = NepaliDate.parseLenient("15 Shrawan 2080");
        Named tithi = Patro.info(date).getTithi();
        // README-END
        assertEquals(NepaliDate.of(2081, 4, 15), fromAd);
        assertEquals(NepaliDate.of(2080, 4, 15), parsed);
        assertTrue(longForm.length() > 0);
        assertNotNull(tithi);
    }
}
