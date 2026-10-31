package io.github.sanamhub.nepdate.patro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sanamhub.nepdate.Lang;
import io.github.sanamhub.nepdate.NepDateException;
import io.github.sanamhub.nepdate.NepaliDate;
import io.github.sanamhub.nepdate.NepaliDateRange;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Calls every public symbol of nepdate-patro the way a Java user would (ADR-0004 gate 6).
 */
class JavaApiTest {
    private static final Map<String, String> SUMMARY = readSummary();

    /** The hub's patro counts, the same vendored file the Kotlin tests read. */
    private static Map<String, String> readSummary() {
        String shared = System.getProperty("nepdate.shared");
        if (shared == null) throw new IllegalStateException("nepdate.shared is not set; run the tests through Gradle");
        Path path = Paths.get(shared, "spec", "vectors", "patro-summary.json");
        Map<String, String> fields = new HashMap<>();
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String text = line.trim();
                if (text.endsWith(",")) text = text.substring(0, text.length() - 1);
                int colon = text.indexOf(':');
                if (colon < 0) continue;
                String key = text.substring(0, colon).trim().replace("\"", "");
                String value = text.substring(colon + 1).trim().replace("\"", "");
                fields.put(key, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("reading " + path + "; re-sync shared/ from the hub", e);
        }
        return Map.copyOf(fields);
    }

    private static int summaryNumber(String field) {
        String text = SUMMARY.get(field);
        if (text == null) throw new IllegalStateException("patro-summary.json has no " + field);
        return Integer.parseInt(text);
    }

    private static String summaryText(String field) {
        String text = SUMMARY.get(field);
        if (text == null) throw new IllegalStateException("patro-summary.json has no " + field);
        return text;
    }

    @Test
    void patroQueries() throws NepDateException {
        NepaliDate newYear = NepaliDate.of(2081, 1, 1);
        CalendarInfo info = Patro.info(newYear);
        assertSame(info, NepaliDatePatro.getCalendarInfo(newYear));

        Named tithi = info.getTithi();
        assertNotNull(tithi);
        assertEquals("Panchami", tithi.name());
        assertEquals("Panchami", tithi.name(Lang.ENGLISH));
        assertEquals("पञ्चमी", tithi.name(Lang.NEPALI));
        assertEquals("पञ्चमी", tithi.getNepali());
        assertEquals("Panchami", tithi.getEnglish());
        assertTrue(info.isPublicHoliday());

        List<Named> events = info.getEvents();
        assertEquals("New Year", events.get(0).getEnglish());
        assertThrows(UnsupportedOperationException.class, () -> events.add(tithi));
        assertEquals(info, Patro.info(NepaliDate.of(2081, 1, 1)));
        assertEquals(info.hashCode(), Patro.info(newYear).hashCode());
        assertTrue(info.toString().startsWith("CalendarInfo(tithi=Panchami"));
        assertEquals("Panchami (पञ्चमी)", tithi.toString());

        CalendarInfo empty = Patro.info(NepaliDate.MIN);
        assertNull(empty.getTithi());
        assertFalse(empty.isPublicHoliday());
        assertTrue(empty.getEvents().isEmpty());
    }

    @Test
    void coverageAndHolidays() throws NepDateException {
        NepaliDateRange coverage = Patro.getCoverage();
        assertEquals(NepaliDate.parse(summaryText("first_day")), coverage.getStart());
        assertEquals(NepaliDate.parse(summaryText("last_day")), coverage.getEndInclusive());
        assertEquals(summaryNumber("days"), coverage.size());
        List<NepaliDate> holidays = Patro.getPublicHolidays();
        assertEquals(summaryNumber("holidays"), holidays.size());
        assertFalse(holidays.isEmpty());
        assertTrue(holidays.stream().allMatch(date -> Patro.info(date).isPublicHoliday()));
        assertThrows(UnsupportedOperationException.class, () -> holidays.add(NepaliDate.MIN));
    }
}
