package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.OperationLog;
import com.mes.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<IPage<OperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (module != null && !module.isEmpty()) {
            wrapper.like(OperationLog::getOperationModule, module);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, type);
        }
        if (userName != null && !userName.isEmpty()) {
            wrapper.like(OperationLog::getUserName, userName);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(endTime));
        }
        
        wrapper.orderByDesc(OperationLog::getCreateTime);
        
        return Result.success(operationLogService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/modules")
    public Result<List<String>> getModules() {
        List<String> modules = operationLogService.list(new LambdaQueryWrapper<OperationLog>()
                .select(OperationLog::getOperationModule)
                .groupBy(OperationLog::getOperationModule))
                .stream()
                .map(OperationLog::getOperationModule)
                .toList();
        return Result.success(modules);
    }

    @GetMapping("/{id}")
    public Result<OperationLog> detail(@PathVariable Long id) {
        return Result.success(operationLogService.getById(id));
    }
}