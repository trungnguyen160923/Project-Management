package com.example.projectmanagement.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    // Formatter for outputting date-only strings (yyyy-MM-dd)
    private static final ThreadLocal<SimpleDateFormat> DATE_ONLY_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    );

    // Formatter for outputting date-time strings (yyyy-MM-dd'T'HH:mm:ss)
    private static final ThreadLocal<SimpleDateFormat> DATETIME_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    );

    // Formatter for ISO 8601 with milliseconds and offset (yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
    private static final ThreadLocal<SimpleDateFormat> ISO8601_FORMATTER = ThreadLocal.withInitial(() -> {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        sdf.setLenient(false);
        return sdf;
    });

    // Formatter for custom "HH:mm dd/MM/yyyy" format
    private static final ThreadLocal<SimpleDateFormat> CUSTOM_FORMATTER = ThreadLocal.withInitial(() -> {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        return sdf;
    });

    private ParseDateUtil() { /* Utility class */ }

    /**
     * Parses the input string into java.util.Date using multiple patterns.
     * Returns null if parsing fails for all patterns.
     */
    public static Date parseDate(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String s = input.trim();
        for (SimpleDateFormat sdf : FORMATTERS.get()) {
            try {
                Date d = sdf.parse(s);
                if (d != null) return d;
            } catch (ParseException ignored) {}
        }
        return null;
    }

    /**
     * Formats a Date into a string in ISO 8601 date-time format (yyyy-MM-dd'T'HH:mm:ss).
     * Returns empty string if date is null.
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATETIME_FORMATTER.get().format(date);
    }

    /**
     * Parses the input string into a Calendar using multiple date patterns.
     * Returns null if parsing fails.
     */
    public static Calendar parseDateToCalendar(String input) {
        Date date = parseDate(input);
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * Converts a Date to ISO-8601 string with milliseconds and timezone offset.
     * e.g. "2025-06-12T18:16:42.541+07:00".
     * Returns empty string if date is null.
     */
    public static String toIso8601(Date date) {
        if (date == null) return "";
        return ISO8601_FORMATTER.get().format(date);
    }

    /**
     * Parses an ISO-8601 string (with offset) into java.util.Date.
     * Supports pattern "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".
     * Returns null on parse error.
     */
    public static Date parseIso8601(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        try {
            return ISO8601_FORMATTER.get().parse(input);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Formats a Date into custom "HH:mm dd/MM/yyyy" string.
     * e.g. "14:30 30/06/2025".
     * Returns empty string if date is null.
     */
    public static String toCustomDateTime(Date date) {
        if (date == null) return "";
        return CUSTOM_FORMATTER.get().format(date);
    }

    /**
     * Parses a custom "HH:mm dd/MM/yyyy" string into java.util.Date.
     * Returns null on parse error.
     */
    public static Date parseCustomDateTime(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        try {
            return CUSTOM_FORMATTER.get().parse(input);
        } catch (ParseException e) {
            return null;
        }
    }
}
