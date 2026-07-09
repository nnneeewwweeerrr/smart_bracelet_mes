package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.ExceptionRecord;
import com.mes.service.ExceptionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exception")
public class ExceptionRecordController {

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    @GetMapping("/list")
    public Result<IPage<ExceptionRecord>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String exceptionNo,
                                               @RequestParam(required = false) String exceptionType,
                                               @RequestParam(required = false) Integer status) {
        Page<ExceptionRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExceptionRecord> wrapper = new LambdaQueryWrapper<>();
        if (exceptionNo != null && !exceptionNo.isEmpty()) {
            wrapper.like(ExceptionRecord::getExceptionNo, exceptionNo);
        }
        if (exceptionType != null && !exceptionType.isEmpty()) {
            wrapper.eq(ExceptionRecord::getExceptionType, exceptionType);
        }
        if (status != null) {
            wrapper.eq(ExceptionRecord::getStatus, status);
        }
        wrapper.orderByDesc(ExceptionRecord::getReportTime);
        return Result.success(exceptionRecordService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<ExceptionRecord> detail(@PathVariable Long id) {
        return Result.success(exceptionRecordService.getById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody ExceptionRecord exceptionRecord) {
        exceptionRecordService.save(exceptionRecord);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody ExceptionRecord exceptionRecord) {
        exceptionRecordService.updateById(exceptionRecord);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        exceptionRecordService.removeById(id);
        return Result.success();
    }

    @PutMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable Long id) {
        exceptionRecordService.handleException(id);
        return Result.success();
    }

    @PutMapping("/resolve/{id}")
    public Result<Void> resolve(@PathVariable Long id) {
        exceptionRecordService.resolveException(id);
        return Result.success();
    }

    @GetMapping("/batch-trace/{workOrderId}")
    public Result<List<ExceptionRecord>> batchTrace(@PathVariable Long workOrderId) {
        return Result.success(exceptionRecordService.batchTrace(workOrderId));
    }
}
