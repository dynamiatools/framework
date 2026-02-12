package tools.dynamia.commons;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class FormattersTest {

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testFormatInteger() {
        assertEquals("1,234", Formatters.formatInteger(1234));
        assertEquals("", Formatters.formatInteger(null));
    }

    @Test
    public void testFormatDecimal() {
        assertEquals("1,234.56", Formatters.formatDecimal(1234.56));
        assertEquals("", Formatters.formatDecimal(null));
    }

    @Test
    public void testFormatCurrency() {

        assertTrue(Formatters.formatCurrency(1234.56).contains("1,234.56"));
        assertEquals("", Formatters.formatCurrency(null));
    }

    @Test
    public void testFormatCurrencySimple() {
        assertTrue(Formatters.formatCurrencySimple(1234.56).contains("1,235"));
        assertEquals("", Formatters.formatCurrencySimple(null));
    }

    @Test
    public void testFormatPercent() {
        assertTrue(Formatters.formatPercent(0.25).contains("25"));
        assertEquals("", Formatters.formatPercent(null));
    }

    @Test
    public void testFormatDateWithDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse("2023-09-05");
        assertTrue(Formatters.formatDate(date).contains("2023"));
        assertEquals("", Formatters.formatDate((Date) null));
    }

    @Test
    public void testFormatDateWithLocalDate() {
        LocalDate date = LocalDate.of(2023, 9, 5);
        assertEquals("2023-09-05", Formatters.formatDate(date));
        assertEquals("", Formatters.formatDate((LocalDate) null));
    }

    @Test
    public void testFormatTimeWithLocalTime() {
        LocalTime time = LocalTime.of(14, 30, 15);
        assertEquals("14:30:15", Formatters.formatTime(time));
        assertEquals("", Formatters.formatTime((LocalTime) null));
    }

    @Test
    public void testFormatDateTimeWithLocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2023, 9, 5, 14, 30, 15);
        assertEquals("2023-09-05T14:30:15", Formatters.formatDateTime(dt));
        assertEquals("", Formatters.formatDateTime((LocalDateTime) null));
    }

    @Test
    public void testFormatDateWithLocalDateAndPattern() {
        LocalDate date = LocalDate.of(2023, 9, 5);
        assertEquals("05/09/2023", Formatters.formatDate(date, "dd/MM/yyyy"));
        assertEquals("", Formatters.formatDate((LocalDate) null, "dd/MM/yyyy"));
        assertEquals("", Formatters.formatDate(date, null));
    }

    @Test
    public void testFormatTimeWithLocalTimeAndPattern() {
        LocalTime time = LocalTime.of(14, 30, 15);
        assertEquals("14:30", Formatters.formatTime(time, "HH:mm"));
        assertEquals("", Formatters.formatTime(null, "HH:mm"));
        assertEquals("", Formatters.formatTime(time, null));
    }

    @Test
    public void testFormatDateTimeWithLocalDateTimeAndPattern() {
        LocalDateTime dt = LocalDateTime.of(2023, 9, 5, 14, 30, 15);
        assertEquals("05-09-2023 14:30", Formatters.formatDateTime(dt, "dd-MM-yyyy HH:mm"));
        assertEquals("", Formatters.formatDateTime(null, "dd-MM-yyyy HH:mm"));
        assertEquals("", Formatters.formatDateTime(dt, null));
    }

    @Test
    public void testFormatTemporal() {
        LocalDateTime dt = LocalDateTime.of(2023, 9, 5, 14, 30, 15);
        assertEquals("2023/09/05", Formatters.formatTemporal(dt, "yyyy/MM/dd"));
        assertEquals("", Formatters.formatTemporal(null, "yyyy/MM/dd"));
        assertEquals("", Formatters.formatTemporal(dt, null));
    }

    @Test
    public void testFormatDateWithDateAndPattern() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse("2023-09-05 14:30:15");
        assertTrue(Formatters.formatDate(date, "dd/MM/yyyy HH:mm").startsWith("05/09/2023 14:30"));
        assertEquals("", Formatters.formatDate((Date) null, "dd/MM/yyyy HH:mm"));
        assertEquals("", Formatters.formatDate(date, null));
    }
}
