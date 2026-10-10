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
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Calls every public symbol of nepdate-patro the way a Java user would (ADR-0004 gate 6).
 */
class JavaApiTest {
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
        assertEquals(NepaliDate.of(2077, 1, 1), coverage.getStart());
        assertEquals(NepaliDate.of(2084, 8, 20), coverage.getEndInclusive());
        List<NepaliDate> holidays = Patro.getPublicHolidays();
        assertEquals(294, holidays.size());
        assertThrows(UnsupportedOperationException.class, () -> holidays.add(NepaliDate.MIN));
    }
}
