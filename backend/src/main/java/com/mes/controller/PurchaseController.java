package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.PurchaseOrder;
import com.mes.service.PurchaseOrderService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping("/list")
    public Result<IPage<PurchaseOrder>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String purchaseNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long supplierId) {
        Page<PurchaseOrder> page = new Page<>(current, size);
        return Result.success(purchaseOrderService.pageList(page, purchaseNo, status, supplierId));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> detail(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "采购管理", type = "新增", description = "新增采购单")
    public Result<Void> save(@RequestBody PurchaseOrder purchase) {
        purchase.setPurchaseNo(BarcodeUtil.generatePurchaseNo());
        calcTotalAmount(purchase);
        if (purchase.getStatus() == null) {
            purchase.setStatus(0);
        }
        purchase.setCreateTime(LocalDateTime.now());
        purchaseOrderService.save(purchase);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "采购管理", type = "修改", description = "修改采购单")
    public Result<Void> update(@RequestBody PurchaseOrder purchase) {
        calcTotalAmount(purchase);
        purchaseOrderService.updateById(purchase);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "采购管理", type = "删除", description = "删除采购单")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.removeById(id);
        return Result.success();
    }

    @PutMapping("/audit/{id}")
    @OperationLog(module = "采购管理", type = "审核", description = "审核采购单")
    public Result<Void> audit(@PathVariable Long id) {
        PurchaseOrder purchase = purchaseOrderService.getById(id);
        if (purchase == null) {
            return Result.error("采购单不存在");
        }
        if (!Integer.valueOf(0).equals(purchase.getStatus())) {
            return Result.error("采购单状态不允许审核");
        }
        purchase.setStatus(1);
        purchaseOrderService.updateById(purchase);
        return Result.success();
    }

    @PutMapping("/arrive/{id}")
    @OperationLog(module = "采购管理", type = "到货登记", description = "采购到货入库")
    public Result<Void> arrive(@PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "1") Long warehouseId) {
        try {
            purchaseOrderService.arrive(id, warehouseId);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @OperationLog(module = "采购管理", type = "完成", description = "完成采购单")
    public Result<Void> complete(@PathVariable Long id) {
        PurchaseOrder purchase = purchaseOrderService.getById(id);
        if (purchase == null) {
            return Result.error("采购单不存在");
        }
        if (!Integer.valueOf(2).equals(purchase.getStatus())) {
            return Result.error("采购单状态不允许完成");
        }
        purchase.setStatus(3);
        purchaseOrderService.updateById(purchase);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    @OperationLog(module = "采购管理", type = "取消", description = "取消采购单")
    public Result<Void> cancel(@PathVariable Long id) {
        PurchaseOrder purchase = purchaseOrderService.getById(id);
        if (purchase == null) {
            return Result.error("采购单不存在");
        }
        if (!Integer.valueOf(0).equals(purchase.getStatus()) && !Integer.valueOf(1).equals(purchase.getStatus())) {
            return Result.error("采购单状态不允许取消");
        }
        purchase.setStatus(4);
        purchaseOrderService.updateById(purchase);
        return Result.success();
    }

    private void calcTotalAmount(PurchaseOrder purchase) {
        if (purchase.getQuantity() != null && purchase.getUnitPrice() != null) {
            purchase.setTotalAmount(purchase.getUnitPrice().multiply(new BigDecimal(purchase.getQuantity())));
        }
    }
}
