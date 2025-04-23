package com.example.projectmanagement.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for parsing and formatting date strings into java.util.Date and back.
 * Supports multiple input formats and is thread-safe on pre-API26 devices.
 */
public final class ParseDateUtil {

    // Supported date patterns for parsing
    private static final String[] PATTERNS = {
            "dd/MM/yyyy HH:mm",
            "dd-MM-yyyy HH:mm",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm",
            "dd/MM/yyyy",
            "yyyy-MM-dd"
    };

    // ThreadLocal array of SimpleDateFormat for each pattern
    private static final ThreadLocal<SimpleDateFormat[]> FORMATTERS = ThreadLocal.withInitial(() -> {
        SimpleDateFormat[] fmts = new SimpleDateFormat[PATTERNS.length];
        for (int i = 0; i < PATTERNS.length; i++) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERNS[i], Locale.getDefault());
            sdf.setLenient(false);
            fmts[i] = sdf;
        }
        return fmts;
    });

    // Formatter for outputting date-only strings (dd/MM/yyyy)
    private static final ThreadLocal<SimpleDateFormat> DATE_ONLY_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    );

    // Formatter for outputting date-time strings (dd/MM/yyyy HH:mm)
    private static final ThreadLocal<SimpleDateFormat> DATETIME_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    );

    private ParseDateUtil() { /* Utility class */ }

    /**
     * Parses the input string into java.util.Date using multiple patterns.
     * Returns null if parsing fails for all patterns.
     */
    public static Date parseDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String s = input.trim();
        for (SimpleDateFormat sdf : FORMATTERS.get()) {
            try {
                Date d = sdf.parse(s);
                if (d != null) {
                    return d;
                }
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    /**
     * Formats a Date into a string. If the time component is zero (00:00),
     * returns "dd/MM/yyyy"; otherwise returns "dd/MM/yyyy HH:mm".
     * Returns empty string if date is null.
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        if (hour == 0 && minute == 0) {
            return DATE_ONLY_FORMATTER.get().format(date);
        } else {
            return DATETIME_FORMATTER.get().format(date);
        }
    }
}