package api.example.shared.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String format(LocalDate date) {
        return date != null ? date.format(FORMATTER) : null;
    }

    public static LocalDate parse(String dateString) {
        return dateString != null ? LocalDate.parse(dateString, FORMATTER) : null;
    }
}