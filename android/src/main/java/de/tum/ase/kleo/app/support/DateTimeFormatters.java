package de.tum.ase.kleo.app.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateTimeFormatters {

    // Example: 09:34
    private static final DateTimeFormatter simpleTimeFormatter
            = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    // Example: Sat, Jan 20 at 09:34
    private static final DateTimeFormatter simpleDateTimeFormatter
            = DateTimeFormatter.ofPattern("EEE, MMM d 'at' HH:mm", Locale.ENGLISH);

    private DateTimeFormatters() {
        throw new AssertionError("No instance for you");
    }

    public static String simpleTime(LocalTime localTime) {
        return localTime.format(simpleTimeFormatter);
    }

    public static String simpleTime(OffsetTime offsetTime) {
        return simpleTime(offsetTime.toLocalTime());
    }

    public static String simpleTime(LocalDateTime localDateTime) {
        return simpleTime(localDateTime.toLocalTime());
    }

    public static String simpleTime(OffsetDateTime offsetDateTime) {
        return simpleTime(offsetDateTime.toLocalTime());
    }

    public static String simpleDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(simpleDateTimeFormatter);
    }

    public static String simpleDateTime(OffsetDateTime offsetDateTime) {
        return simpleDateTime(offsetDateTime.toLocalDateTime());
    }
}
