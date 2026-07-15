package com.mes.controller;

import com.mes.common.Result;
import com.mes.entity.ExceptionRecord;
import com.mes.service.ExceptionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/exception-handle")
public class ExceptionHandleController {

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    @PutMapping("/start/{id}")
    public Result<Void> startHandle(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        ExceptionRecord record = exceptionRecordService.getById(id);
        if (record == null) {
            return Result.error("异常记录不存在");
        }
        if (record.getStatus() != 0) {
            return Result.error("只有待处理的异常才能开始处理");
        }
        record.setStatus(1);
        record.setHandleTime(LocalDateTime.now());
        if (body != null) {
            if (body.containsKey("handlerId")) {
                record.setHandlerId(Long.valueOf(body.get("handlerId").toString()));
            }
            if (body.containsKey("handlerName")) {
                record.setHandlerName(body.get("handlerName").toString());
            }
        }
        exceptionRecordService.updateById(record);
        return Result.success();
    }

    @PutMapping("/submit/{id}")
    public Result<Void> submitHandle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ExceptionRecord record = exceptionRecordService.getById(id);
        if (record == null) {
            return Result.error("异常记录不存在");
        }
        if (record.getStatus() != 1) {
            return Result.error("只有处理中的异常才能提交处理报告");
        }
        record.setStatus(2);
        record.setHandleTime(LocalDateTime.now());
        if (body.containsKey("handleReport")) {
            record.setHandleReport(body.get("handleReport").toString());
        }
        if (body.containsKey("handleResult")) {
            record.setHandleResult(body.get("handleResult").toString());
        }
        if (body.containsKey("exceptionLevel")) {
            record.setExceptionLevel(Integer.valueOf(body.get("exceptionLevel").toString()));
        }
        exceptionRecordService.updateById(record);
        return Result.success();
    }

    @PutMapping("/close/{id}")
    public Result<Void> closeException(@PathVariable Long id) {
        ExceptionRecord record = exceptionRecordService.getById(id);
        if (record == null) {
            return Result.error("异常记录不存在");
        }
        record.setStatus(3);
        record.setHandleTime(LocalDateTime.now());
        exceptionRecordService.updateById(record);
        return Result.success();
    }

    @GetMapping("/pending-list")
    public Result<?> getPendingList() {
        return Result.success(exceptionRecordService.list());
    }
}
