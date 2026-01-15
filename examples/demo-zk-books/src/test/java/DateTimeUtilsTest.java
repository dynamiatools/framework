import org.junit.jupiter.api.Test;
import tools.dynamia.commons.DateRange;
import tools.dynamia.commons.DateTimeUtils;

import java.util.Date;

public class DateTimeUtilsTest {


    @Test
    public void testTimeBetweens() {

        Date fecha1 = DateTimeUtils.createDate(2023, 9, 6, 5, 0, 0);
        Date fecha2 = DateTimeUtils.createDate(2023, 9, 6, 7, 0, 0);

        System.out.println("Days Between:" + DateTimeUtils.daysBetween(fecha1, fecha2));
        System.out.println("Hours Between:" + DateTimeUtils.hoursBetween(fecha1, fecha2));
        System.out.println("Minutes Between:" + DateTimeUtils.minutesBetween(fecha1, fecha2));
        System.out.println("Seconds Between:" + DateTimeUtils.secondsBetween(fecha1, fecha2));

        DateTimeUtils.getCurrentMonthRange();
        DateRange range = new DateRange(fecha1, fecha2);
        System.out.println(range.getYearsBetween());

    }

}
