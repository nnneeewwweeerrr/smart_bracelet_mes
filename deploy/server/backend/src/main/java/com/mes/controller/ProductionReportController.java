package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.ProductionReport;
import com.mes.service.ProductionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * 报工提交（含双重防错校验）
     * 请求体可包含：
     * - productionReport: 报工基本数据
     * - materialCode: 扫码物料编码（可选，有则校验）
     * - processParams: 实际工艺参数Map（可选，有则校验）
     */
    @PostMapping("/save")
    @OperationLog(module = "报工管理", type = "新增", description = "新增报工记录")
    public Result<Void> save(@RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> reportData = (Map<String, Object>) requestBody.get("productionReport");
            if (reportData == null) {
                // 兼容直接传ProductionReport的场景
                reportData = requestBody;
            }

            ProductionReport report = new ProductionReport();
            if (reportData.get("workOrderId") != null) {
                report.setWorkOrderId(Long.valueOf(reportData.get("workOrderId").toString()));
            }
            if (reportData.get("workerId") != null) {
                report.setWorkerId(Long.valueOf(reportData.get("workerId").toString()));
            }
            if (reportData.get("processId") != null) {
                report.setProcessId(Long.valueOf(reportData.get("processId").toString()));
            }
            if (reportData.get("goodQuantity") != null) {
                report.setGoodQuantity(Integer.valueOf(reportData.get("goodQuantity").toString()));
            }
            if (reportData.get("badQuantity") != null) {
                report.setBadQuantity(Integer.valueOf(reportData.get("badQuantity").toString()));
            }

            // 提取校验参数
            String materialCode = (String) requestBody.get("materialCode");
            @SuppressWarnings("unchecked")
            Map<String, Object> processParams = (Map<String, Object>) requestBody.get("processParams");

            // 执行双重校验 + 保存
            String error = productionReportService.saveReportWithValidation(report, materialCode, processParams);
            if (error != null) {
                return Result.error(error);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("报工失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "报工管理", type = "删除", description = "删除报工记录")
    public Result<Void> delete(@PathVariable Long id) {
        productionReportService.removeById(id);
        return Result.success();
    }
}
