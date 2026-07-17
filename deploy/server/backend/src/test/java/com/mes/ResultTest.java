package com.mes;

import com.mes.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void testSuccessWithoutData() {
        Result<String> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testSuccessWithData() {
        Result<String> result = Result.success("test");
        assertEquals(200, result.getCode());
        assertEquals("test", result.getData());
    }

    @Test
    void testError() {
        Result<String> result = Result.error("错误信息");
        assertEquals(500, result.getCode());
        assertEquals("错误信息", result.getMessage());
    }

    @Test
    void testErrorWithCode() {
        Result<String> result = Result.error(401, "未授权");
        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
    }
}
