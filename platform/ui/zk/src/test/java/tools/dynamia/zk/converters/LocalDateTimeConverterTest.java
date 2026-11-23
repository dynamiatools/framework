package tools.dynamia.zk.converters;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LocalDateTimeConverterTest {

    @Test
    void localDateIsDelegatedToTheLocalDateConverter() {
        var date = LocalDate.of(2026, 11, 23);
        var expected = new tools.dynamia.zk.converters.LocalDate().coerceToUi(date, null, null);

        assertNotNull(expected);
        assertEquals(expected, new tools.dynamia.zk.converters.LocalDateTime().coerceToUi(date, null, null));
    }

    @Test
    void localDateTimeStillFormatsWithTime() {
        assertNotNull(new tools.dynamia.zk.converters.LocalDateTime().coerceToUi(LocalDateTime.of(2026, 11, 23, 10, 30), null, null));
    }
}
