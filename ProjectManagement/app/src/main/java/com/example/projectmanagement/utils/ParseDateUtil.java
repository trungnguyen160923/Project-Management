package com.example.projectmanagement.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import android.os.Build;
import android.util.Log;

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
        for (SimpleDateFormat sdf : Objects.requireNonNull(FORMATTERS.get())) {
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
    public static Date parseFlexibleIsoDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            Log.d("ParseDateUtil", "Input is null or empty");
            return null;
        }
        String trimmed = input.trim();
        Log.d("ParseDateUtil", "Parsing date: " + trimmed);
        
        // Try parsing with milliseconds first
        if (trimmed.contains(".")) {
            int dotIndex = trimmed.indexOf(".");
            if (trimmed.length() >= dotIndex + 4) {
                trimmed = trimmed.substring(0, dotIndex + 4);
            } else {
                trimmed = trimmed.substring(0, dotIndex);
            }
            Log.d("ParseDateUtil", "After trimming milliseconds: " + trimmed);
            
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date result = sdf.parse(trimmed);
                Log.d("ParseDateUtil", "Successfully parsed date with milliseconds: " + result);
                return result;
            } catch (ParseException e) {
                Log.e("ParseDateUtil", "Error parsing date with milliseconds: " + e.getMessage());
            }
        }
        
        // Try parsing without milliseconds
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date result = sdf.parse(trimmed);
            Log.d("ParseDateUtil", "Successfully parsed date without milliseconds: " + result);
            return result;
        } catch (ParseException e) {
            Log.e("ParseDateUtil", "Error parsing date without milliseconds: " + e.getMessage());
            return null;
        }
    }
    /**
     * Chuyển ISO date-time string (vd. "2025-06-13T00:00:00.000+07:00")
     * về format "yyyy-MM-dd" (vd. "2025-06-13").
     */
    public static String isoToDate(String isoDateTime) {
        // 1) Parse thành OffsetDateTime
        OffsetDateTime odt = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            odt = OffsetDateTime.parse(isoDateTime);
        }
        // 2) Lấy LocalDate (bỏ giờ, phút, giây, timezone)
        LocalDate date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = odt.toLocalDate();
        }
        // 3) Format về chuỗi "yyyy-MM-dd"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return isoDateTime;
    }
    /**
     * Chuyển java.util.Date về chuỗi "yyyy-MM-dd".
     */
    public static String dateToIsoDate(Date date) {
        if (date == null) return null;
        // 1) Tạo Instant từ Date
        Instant instant = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            instant = date.toInstant();
        }
        // 2) Áp zone mặc định (GMT+07:00 hoặc theo hệ thống)
        LocalDate localDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        }
        // 3) Format
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return "";
    }

}
