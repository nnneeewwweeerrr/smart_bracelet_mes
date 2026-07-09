package com.mes.controller;

import com.mes.common.Result;
import com.mes.entity.QualityResult;
import com.mes.service.QualityResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quality-result")
public class QualityResultController {

    @Autowired
    private QualityResultService qualityResultService;

    @GetMapping("/list")
    public Result<List<QualityResult>> list(@RequestParam Long taskId) {
        return Result.success(qualityResultService.listByTaskId(taskId));
    }

    @GetMapping("/{id}")
    public Result<QualityResult> detail(@PathVariable Long id) {
        return Result.success(qualityResultService.getById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody QualityResult qualityResult) {
        qualityResultService.save(qualityResult);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody QualityResult qualityResult) {
        qualityResultService.updateById(qualityResult);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        qualityResultService.removeById(id);
        return Result.success();
    }

    @PostMapping("/batch-save")
    public Result<Void> batchSave(@RequestBody List<QualityResult> results) {
        qualityResultService.batchSave(results);
        return Result.success();
    }

    @GetMapping("/report/{taskId}")
    public Result<Map<String, Object>> report(@PathVariable Long taskId) {
        return Result.success(qualityResultService.generateReport(taskId));
    }
}
