package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.OrderCustomer;
import com.mes.entity.ProductInfo;
import com.mes.entity.ProductionPlan;
import com.mes.service.OrderCustomerService;
import com.mes.service.ProductInfoService;
import com.mes.service.ProductionPlanService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/production-plan")
public class ProductionPlanController {

    @Autowired
    private ProductionPlanService productionPlanService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private OrderCustomerService orderCustomerService;

    @GetMapping("/list")
    public Result<IPage<ProductionPlan>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String planNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long orderId) {
        Page<ProductionPlan> page = new Page<>(current, size);
        IPage<ProductionPlan> result = productionPlanService.pageList(page, planNo, status, orderId);
        result.getRecords().forEach(this::mapFields);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ProductionPlan> detail(@PathVariable Long id) {
        ProductionPlan plan = productionPlanService.getById(id);
        if (plan != null) {
            mapFields(plan);
        }
        return Result.success(plan);
    }

    @PostMapping("/save")
    @OperationLog(module = "生产计划", type = "新增", description = "新增生产计划")
    public Result<Void> save(@RequestBody ProductionPlan productionPlan) {
        productionPlan.setPlanNo(BarcodeUtil.generatePlanNo());
        if (productionPlan.getStatus() == null) {
            productionPlan.setStatus(0);
        }
        productionPlan.setCreateTime(LocalDateTime.now());
        productionPlanService.save(productionPlan);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "生产计划", type = "修改", description = "修改生产计划")
    public Result<Void> update(@RequestBody ProductionPlan productionPlan) {
        productionPlanService.updateById(productionPlan);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "生产计划", type = "删除", description = "删除生产计划")
    public Result<Void> delete(@PathVariable Long id) {
        productionPlanService.removeById(id);
        return Result.success();
    }

    @PutMapping("/start/{id}")
    @OperationLog(module = "生产计划", type = "启动", description = "启动生产计划")
    public Result<Void> start(@PathVariable Long id) {
        boolean success = productionPlanService.startPlan(id);
        if (!success) {
            return Result.error("生产计划状态不允许启动");
        }
        return Result.success();
    }

    @PutMapping("/finish/{id}")
    @OperationLog(module = "生产计划", type = "完成", description = "完成生产计划")
    public Result<Void> finish(@PathVariable Long id) {
        boolean success = productionPlanService.finishPlan(id);
        if (!success) {
            return Result.error("生产计划状态不允许完成");
        }
        return Result.success();
    }

    private void mapFields(ProductionPlan plan) {
        // 映射quantity = plannedQuantity
        plan.setQuantity(plan.getPlannedQuantity());

        // 查询产品名称
        if (plan.getProductId() != null) {
            ProductInfo product = productInfoService.getById(plan.getProductId());
            if (product != null) {
                plan.setProductName(product.getProductName());
            }
        }

        // 如果planName为空，自动生成
        if (plan.getPlanName() == null || plan.getPlanName().isEmpty()) {
            plan.setPlanName(plan.getPlanNo() + " 生产计划");
        }
    }
}
