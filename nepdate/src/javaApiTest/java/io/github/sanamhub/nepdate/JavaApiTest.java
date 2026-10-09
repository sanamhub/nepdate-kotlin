package io.github.sanamhub.nepdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Calls every public symbol the way a Java user would (ADR-0004 gate 6). A missing
 * {@code @JvmStatic}, {@code @JvmField} or {@code @JvmOverloads} stops this file from compiling.
 * Every task that adds a public symbol extends it.
 */
class JavaApiTest {
    @Test
    void factoriesAndConstants() throws NepDateException {
        NepaliDate date = NepaliDate.of(2081, 4, 15);
        assertEquals(date, NepaliDate.of(2081, Month.SHRAWAN, 15));
        assertEquals(date, NepaliDate.ofOrNull(2081, 4, 15));
        assertNull(NepaliDate.ofOrNull(2081, 4, 33));
        assertEquals(date, NepaliDate.ofGregorian(2024, 7, 30));
        assertEquals(date, NepaliDate.ofEpochDay(date.toEpochDay()));
        assertEquals("1901/01/01", NepaliDate.MIN.toString());
        assertEquals("2199/12/30", NepaliDate.MAX.toString());
    }

    @Test
    void properties() {
        NepaliDate date = NepaliDate.of(2081, 4, 15);
        assertEquals(2081, date.getYear());
        assertEquals(4, date.getMonthValue());
        assertEquals(Month.SHRAWAN, date.getMonth());
        assertEquals(15, date.getDayOfMonth());
        assertEquals(Weekday.TUESDAY, date.getDayOfWeek());
        assertEquals(31 + 32 + 31 + 15, date.getDayOfYear());
        assertEquals(32, date.lengthOfMonth());
        assertEquals(NepaliDate.of(2081, 4, 1), date.firstDayOfMonth());
        assertEquals(NepaliDate.of(2081, 4, 32), date.lastDayOfMonth());
        assertEquals(2024, date.getGregorianYear());
        assertEquals(7, date.getGregorianMonth());
        assertEquals(30, date.getGregorianDay());
        assertTrue(date.compareTo(NepaliDate.MAX) < 0);
        assertEquals(date.hashCode(), NepaliDate.of(2081, 4, 15).hashCode());
    }

    @Test
    void dayArithmetic() throws NepDateException {
        NepaliDate date = NepaliDate.of(2081, 4, 15);
        assertEquals(NepaliDate.of(2081, 4, 16), date.plusDays(1));
        assertEquals(NepaliDate.of(2081, 4, 14), date.minusDays(1));
        assertEquals(1L, date.daysUntil(NepaliDate.of(2081, 4, 16)));
    }

    @Test
    void enumsAndErrors() {
        assertEquals(Month.SHRAWAN, Month.of(4));
        assertEquals(4, Month.SHRAWAN.getValue());
        assertEquals(Lang.NEPALI, Lang.valueOf("NEPALI"));
        assertEquals(Weekday.SUNDAY, Weekday.values()[0]);

        NepDateException month = assertThrows(NepDateException.class, () -> Month.of(13));
        assertEquals(NepDateException.Kind.INVALID_MONTH, month.getKind());

        NepDateException day = assertThrows(NepDateException.class, () -> NepaliDate.of(2081, 4, 33));
        assertEquals(NepDateException.Kind.INVALID_DAY, day.getKind());
        assertEquals(32, day.getMonthLength());

        IllegalArgumentException range =
                assertThrows(IllegalArgumentException.class, () -> NepaliDate.ofGregorian(1844, 4, 10));
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, ((NepDateException) range).getKind());
        assertThrows(NepDateException.class, () -> NepaliDate.ofEpochDay(Long.MAX_VALUE));
    }
}
