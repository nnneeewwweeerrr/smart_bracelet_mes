package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.ProductionReport;
import com.mes.service.ProductionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production-report")
public class ProductionReportController {

    @Autowired
    private ProductionReportService productionReportService;

    @GetMapping("/list")
    public Result<IPage<ProductionReport>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long workOrderId,
            @RequestParam(required = false) Long workerId) {
        Page<ProductionReport> page = new Page<>(current, size);
        return Result.success(productionReportService.pageList(page, workOrderId, workerId));
    }

    @GetMapping("/{id}")
    public Result<ProductionReport> detail(@PathVariable Long id) {
        return Result.success(productionReportService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "报工管理", type = "新增", description = "新增报工记录")
    public Result<Void> save(@RequestBody ProductionReport productionReport) {
        boolean success = productionReportService.saveReport(productionReport);
        if (!success) {
            return Result.error("报工失败");
        }
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "报工管理", type = "删除", description = "删除报工记录")
    public Result<Void> delete(@PathVariable Long id) {
        productionReportService.removeById(id);
        return Result.success();
    }
}
