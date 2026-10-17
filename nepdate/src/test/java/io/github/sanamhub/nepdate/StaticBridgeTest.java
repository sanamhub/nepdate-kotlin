package io.github.sanamhub.nepdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Java calls reach the synthetic bridges that Kotlin tests skip: the static {@code @JvmStatic}
 * methods and the {@code List} methods with an {@code Object} parameter. PIT does not count a
 * bridge as covered until one of these runs.
 */
class StaticBridgeTest {
    @Test
    void ofOrNullAndParseOrNullReturnADate() {
        NepaliDate expected = NepaliDate.of(2081, 4, 15);
        assertEquals(expected, NepaliDate.ofOrNull(2081, 4, 15));
        assertEquals(expected, NepaliDate.parseOrNull("2081/04/15"));
    }

    @Test
    void rangeObjectBridgesUseTheTypedOverrides() {
        NepaliDateRange range = NepaliDateRange.ofMonth(2081, 4);
        List<?> list = range;
        NepaliDate day = NepaliDate.of(2081, 4, 15);
        NepaliDate outside = NepaliDate.of(2081, 5, 1);

        assertTrue(list.contains(day));
        assertFalse(list.contains(outside));
        assertFalse(list.contains("2081/04/15"));
        assertEquals(14, list.indexOf(day));
        assertEquals(-1, list.indexOf(outside));
        assertEquals(-1, list.indexOf("2081/04/15"));
        assertEquals(14, list.lastIndexOf(day));
        assertEquals(-1, list.lastIndexOf(outside));
        assertEquals(-1, list.lastIndexOf("2081/04/15"));
    }

    @Test
    void quarterMonthsObjectBridgesUseTheTypedOverrides() {
        List<Month> months = Quarter.Q1.getMonths();

        assertTrue(months.contains(Month.SHRAWAN));
        assertTrue(months.contains(Month.ASHOJ));
        assertFalse(months.contains(Month.MAGH));
        assertFalse(months.contains("Shrawan"));
        assertEquals(0, months.indexOf(Month.SHRAWAN));
        assertEquals(2, months.indexOf(Month.ASHOJ));
        assertEquals(-1, months.indexOf(Month.MAGH));
        assertEquals(-1, months.indexOf("Shrawan"));
        assertEquals(0, months.lastIndexOf(Month.SHRAWAN));
        assertEquals(2, months.lastIndexOf(Month.ASHOJ));
        assertEquals(-1, months.lastIndexOf(Month.MAGH));
        assertEquals(-1, months.lastIndexOf("Shrawan"));
    }
}
