package tools.dynamia.commons;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * Common duration units enum
 */
public enum DurationUnit {
    NANOS(ChronoUnit.NANOS),
    MILLIS(ChronoUnit.MILLIS),
    SECONDS(ChronoUnit.SECONDS),
    MINUTES(ChronoUnit.MINUTES),
    HOURS(ChronoUnit.HOURS),
    DAYS(ChronoUnit.DAYS),
    WEEKS(ChronoUnit.WEEKS),
    MONTHS(ChronoUnit.MONTHS),
    YEARS(ChronoUnit.YEARS);

    private final TemporalUnit temporalUnit;

    DurationUnit(TemporalUnit temporalUnit) {
        this.temporalUnit = temporalUnit;
    }

    public TemporalUnit getTemporalUnit() {
        return temporalUnit;
    }

    public Duration getDuration() {
        return getTemporalUnit().getDuration();
    }

}
