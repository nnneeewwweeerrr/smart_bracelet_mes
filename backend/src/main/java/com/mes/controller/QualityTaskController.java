package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.QualityTask;
import com.mes.service.QualityTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quality-task")
public class QualityTaskController {

    @Autowired
    private QualityTaskService qualityTaskService;

    @GetMapping("/list")
    public Result<IPage<QualityTask>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String taskNo,
                                           @RequestParam(required = false) String taskType,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) Long inspectorId) {
        Page<QualityTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<QualityTask> wrapper = new LambdaQueryWrapper<>();
        if (taskNo != null && !taskNo.isEmpty()) {
            wrapper.like(QualityTask::getTaskNo, taskNo);
        }
        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq(QualityTask::getTaskType, taskType);
        }
        if (status != null) {
            wrapper.eq(QualityTask::getStatus, status);
        }
        if (inspectorId != null) {
            wrapper.eq(QualityTask::getInspectorId, inspectorId);
        }
        wrapper.orderByDesc(QualityTask::getCreateTime);
        return Result.success(qualityTaskService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<QualityTask> detail(@PathVariable Long id) {
        return Result.success(qualityTaskService.getById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody QualityTask qualityTask) {
        qualityTaskService.save(qualityTask);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody QualityTask qualityTask) {
        qualityTaskService.updateById(qualityTask);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        qualityTaskService.removeById(id);
        return Result.success();
    }

    @PutMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        qualityTaskService.startTask(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        qualityTaskService.completeTask(id);
        return Result.success();
    }

    @PostMapping("/create-iqc/{stockInId}")
    public Result<QualityTask> createIqc(@PathVariable Long stockInId, @RequestParam(required = false) Long inspectorId) {
        return Result.success(qualityTaskService.createIqcTask(stockInId, inspectorId));
    }

    @PostMapping("/create-ipqc/{workOrderId}")
    public Result<QualityTask> createIpqc(@PathVariable Long workOrderId, @RequestParam(required = false) Long inspectorId) {
        return Result.success(qualityTaskService.createIpqcTask(workOrderId, inspectorId));
    }

    @PostMapping("/create-oqc/{orderId}")
    public Result<QualityTask> createOqc(@PathVariable Long orderId, @RequestParam(required = false) Long inspectorId) {
        return Result.success(qualityTaskService.createOqcTask(orderId, inspectorId));
    }
}
