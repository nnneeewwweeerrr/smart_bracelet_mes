package com.mes.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 条码/单据号生成工具
 */
public class BarcodeUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String generateOrderNo() {
        return "OD" + LocalDateTime.now().format(DATE_FORMAT)
                + String.format("%06d", SnowflakeIdGenerator.generateId() % 1000000);
    }

    public static String generatePurchaseNo() {
        return "PO" + LocalDateTime.now().format(DATETIME_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public static String generateStockInNo() {
        return "IN" + LocalDateTime.now().format(DATETIME_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public static String generateStockOutNo() {
        return "OUT" + LocalDateTime.now().format(DATETIME_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public static String generatePlanNo() {
        return "PP" + LocalDateTime.now().format(DATE_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public static String generateWorkOrderNo() {
        return "WO" + LocalDateTime.now().format(DATE_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public static String generateQualityTaskNo() {
        return "QT" + LocalDateTime.now().format(DATETIME_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public static String generateExceptionNo() {
        return "EX" + LocalDateTime.now().format(DATETIME_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public static String generateMaintainNo() {
        return "MT" + LocalDateTime.now().format(DATETIME_FORMAT)
                + String.format("%04d", (int) (Math.random() * 10000));
    }
}
