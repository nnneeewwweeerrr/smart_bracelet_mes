package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.ExceptionRecord;
import com.mes.service.ExceptionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                               @RequestParam(required = false) String exceptionLevel,
                                               @RequestParam(required = false) String title,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Integer priority) {
        Page<ExceptionRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExceptionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(exceptionNo != null && !exceptionNo.isEmpty(), ExceptionRecord::getExceptionNo, exceptionNo)
                .like(exceptionType != null && !exceptionType.isEmpty(), ExceptionRecord::getExceptionType, exceptionType)
                .eq(exceptionLevel != null && !exceptionLevel.isEmpty(), ExceptionRecord::getExceptionLevel, exceptionLevel)
                .like(title != null && !title.isEmpty(), ExceptionRecord::getTitle, title)
                .eq(status != null, ExceptionRecord::getStatus, status)
                .eq(priority != null, ExceptionRecord::getPriority, priority)
                .orderByAsc(ExceptionRecord::getPriority)
                .orderByDesc(ExceptionRecord::getReportTime);
        return Result.success(exceptionRecordService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<ExceptionRecord> detail(@PathVariable Long id) {
        return Result.success(exceptionRecordService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", exceptionRecordService.count());
        stats.put("pending", exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 0)));
        stats.put("processing", exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 1)));
        stats.put("resolved", exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 2)));
        stats.put("closed", exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 3)));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "异常处理", type = "新增", description = "新增异常")
    public Result<Void> save(@RequestBody ExceptionRecord exceptionRecord) {
        if (exceptionRecord.getStatus() == null) {
            exceptionRecord.setStatus(0);
        }
        if (exceptionRecord.getReportTime() == null) {
            exceptionRecord.setReportTime(LocalDateTime.now());
        }
        exceptionRecordService.save(exceptionRecord);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "异常处理", type = "修改", description = "修改异常")
    public Result<Void> update(@RequestBody ExceptionRecord exceptionRecord) {
        exceptionRecordService.updateById(exceptionRecord);
        return Result.success();
    }

    @PutMapping("/handle/{id}")
    @OperationLog(module = "异常处理", type = "处理", description = "处理异常")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ExceptionRecord record = exceptionRecordService.getById(id);
        if (record == null) {
            return Result.error("异常记录不存在");
        }
        record.setStatus(1);
        if (body != null) {
            if (body.containsKey("handler")) {
                record.setHandler(body.get("handler").toString());
            }
            if (body.containsKey("handleContent")) {
                record.setHandleContent(body.get("handleContent").toString());
            }
            if (body.containsKey("handleResult")) {
                record.setHandleResult(body.get("handleResult").toString());
            }
        }
        record.setHandleTime(LocalDateTime.now());
        exceptionRecordService.updateById(record);
        return Result.success();
    }

    @PutMapping("/resolve/{id}")
    @OperationLog(module = "异常处理", type = "解决", description = "解决异常")
    public Result<Void> resolve(@PathVariable Long id) {
        ExceptionRecord record = exceptionRecordService.getById(id);
        if (record == null) {
            return Result.error("异常记录不存在");
        }
        record.setStatus(2);
        record.setHandleTime(LocalDateTime.now());
        exceptionRecordService.updateById(record);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "异常处理", type = "删除", description = "删除异常")
    public Result<Void> delete(@PathVariable Long id) {
        exceptionRecordService.removeById(id);
        return Result.success();
    }
}
