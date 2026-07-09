package com.mes;

import com.mes.utils.BarcodeUtil;
import com.mes.utils.DateUtil;
import com.mes.utils.SnowflakeIdGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void testSnowflakeIdUnique() {
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            long id = SnowflakeIdGenerator.generateId();
            assertTrue(id > 0, "ID必须为正数");
            assertTrue(ids.add(id), "ID必须唯一");
        }
    }

    @Test
    void testOrderNoFormat() {
        String orderNo = BarcodeUtil.generateOrderNo();
        assertNotNull(orderNo);
        assertTrue(orderNo.startsWith("OD"), "订单号必须以OD开头");
        assertEquals(16, orderNo.length(), "订单号长度必须为16");
    }

    @Test
    void testPurchaseNoFormat() {
        String purchaseNo = BarcodeUtil.generatePurchaseNo();
        assertNotNull(purchaseNo);
        assertTrue(purchaseNo.startsWith("PO"), "采购单号必须以PO开头");
    }

    @Test
    void testAllBarcodeTypes() {
        assertNotNull(BarcodeUtil.generateOrderNo());
        assertNotNull(BarcodeUtil.generatePurchaseNo());
        assertNotNull(BarcodeUtil.generateStockInNo());
        assertNotNull(BarcodeUtil.generateStockOutNo());
        assertNotNull(BarcodeUtil.generatePlanNo());
        assertNotNull(BarcodeUtil.generateWorkOrderNo());
        assertNotNull(BarcodeUtil.generateQualityTaskNo());
        assertNotNull(BarcodeUtil.generateExceptionNo());
    }

    @Test
    void testDateFormat() {
        LocalDateTime now = LocalDateTime.now();
        String formatted = DateUtil.format(now);
        assertNotNull(formatted);
        assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "日期格式必须为yyyy-MM-dd HH:mm:ss");
    }

    @Test
    void testDateParse() {
        String dateStr = "2026-07-09 12:30:45";
        LocalDateTime parsed = DateUtil.parseDateTime(dateStr);
        assertNotNull(parsed);
        assertEquals(2026, parsed.getYear());
        assertEquals(7, parsed.getMonthValue());
        assertEquals(9, parsed.getDayOfMonth());
    }

    @Test
    void testDateStartEndOfDay() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 9, 12, 30, 45);
        LocalDateTime start = DateUtil.startOfDay(now);
        LocalDateTime end = DateUtil.endOfDay(now);

        assertEquals(0, start.getHour());
        assertEquals(0, start.getMinute());
        assertEquals(23, end.getHour());
        assertEquals(59, end.getMinute());
    }

    @Test
    void testDurationSeconds() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 9, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 9, 10, 30, 0);
        long seconds = DateUtil.durationSeconds(start, end);
        assertEquals(1800, seconds);
    }
}
