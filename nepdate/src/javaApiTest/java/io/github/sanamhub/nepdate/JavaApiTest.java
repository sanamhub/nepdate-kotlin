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

        NepaliDate end = NepaliDate.of(2081, 4, 32);
        assertEquals(NepaliDate.of(2081, 6, 30), end.plusMonths(2));
        assertEquals(NepaliDate.of(2081, 7, 2), end.plusMonths(2, MonthOverflow.SPILL));
        assertEquals(NepaliDate.of(2082, 4, 31), end.plusYears(1));
        assertEquals(NepaliDate.of(2082, 5, 1), end.plusYears(1, MonthOverflow.SPILL));

        DateDiff diff = NepaliDate.of(2080, 1, 1).until(date);
        assertEquals(false, diff.isNegative());
        assertEquals(1, diff.getYears());
        assertEquals(3, diff.getMonths());
        assertEquals(14, diff.getDays());
        assertEquals(473L, diff.getTotalDays());
    }

    @Test
    void fiscalYears() throws NepDateException {
        NepaliDate date = NepaliDate.of(2081, 4, 15);
        FiscalYear fy = FiscalYear.containing(date);
        assertEquals(fy, date.getFiscalYear());
        assertEquals(new FiscalYear(2081), fy);
        assertEquals(2081, fy.getStartYear());
        assertEquals(NepaliDate.of(2081, 4, 1), fy.start());
        assertEquals(NepaliDate.of(2082, 3, 32), fy.end());
        assertEquals(fy.start(), fy.range().getStart());
        assertEquals(Quarter.Q1, date.getQuarter());
        assertEquals(Month.SHRAWAN, Quarter.Q1.getMonths().get(0));
        assertEquals(NepaliDate.of(2081, 6, 30), fy.range(Quarter.Q1).getEndInclusive());
        assertEquals("2081/82", fy.label());
        assertEquals("२०८१/८२", fy.label(Lang.NEPALI));
        assertTrue(fy.compareTo(new FiscalYear(2082)) < 0);
        assertThrows(UnsupportedOperationException.class, () -> Quarter.Q1.getMonths().set(0, Month.MAGH));
    }

    @Test
    void ranges() throws NepDateException {
        NepaliDateRange month = NepaliDateRange.ofMonth(2081, 4);
        assertEquals(32, month.size());
        assertEquals(NepaliDate.of(2081, 4, 1), month.getStart());
        assertEquals(NepaliDate.of(2081, 4, 32), month.getEndInclusive());
        assertEquals(NepaliDate.of(2081, 4, 15), month.get(14));
        assertTrue(month.contains(NepaliDate.of(2081, 4, 15)));
        assertEquals(366, NepaliDateRange.ofYear(2081).size());
        java.util.List<NepaliDate> days = NepaliDateRange.of(NepaliDate.MIN, NepaliDate.MIN.plusDays(2));
        assertEquals(3, days.size());
        assertThrows(NepDateException.class, () -> NepaliDateRange.of(NepaliDate.MAX, NepaliDate.MIN));
    }

    @Test
    void namesAndDigits() {
        assertEquals("Shrawan", Month.SHRAWAN.displayName());
        assertEquals("साउन", Month.SHRAWAN.displayName(Lang.NEPALI));
        assertEquals("Asa", Month.ASHAD.shortName());
        assertEquals("असार", Month.ASHAD.shortName(Lang.NEPALI));
        assertEquals("Tuesday", Weekday.TUESDAY.displayName());
        assertEquals("मङ्गलवार", Weekday.TUESDAY.displayName(Lang.NEPALI));
        assertEquals("Tue", Weekday.TUESDAY.shortName());
        assertEquals("मङ्गल", Weekday.TUESDAY.shortName(Lang.NEPALI));
        assertEquals("२०८१", NepaliDigits.nepaliDigits(2081));
        assertEquals("०५", NepaliDigits.nepaliDigits(5, 2));
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
