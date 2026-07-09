package com.mes.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期处理工具类
 */
public class DateUtil {

    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATETIME);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATE);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_TIME);

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    public static String format(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATETIME_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr == null || dateTimeStr.isEmpty() ? null
                : LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        return dateStr == null || dateStr.isEmpty() ? null
                : LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null
                : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null
                : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalDate().atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime == null ? null
                : LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MAX);
    }

    public static long durationSeconds(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).getSeconds();
    }
}
