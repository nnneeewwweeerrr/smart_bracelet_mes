package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoStockCheck;
import com.mes.entity.DemoStockCheckDetail;
import com.mes.entity.DemoStock;
import com.mes.service.DemoStockCheckService;
import com.mes.service.DemoStockCheckDetailService;
import com.mes.service.DemoStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/stock-check")
public class DemoStockCheckController {

    @Autowired
    private DemoStockCheckService demoStockCheckService;

    @Autowired
    private DemoStockCheckDetailService demoStockCheckDetailService;

    @Autowired
    private DemoStockService demoStockService;

    @GetMapping("/list")
    public Result<IPage<DemoStockCheck>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String checkNo,
                                               @RequestParam(required = false) String warehouseCode,
                                               @RequestParam(required = false) String warehouseName,
                                               @RequestParam(required = false) Integer status) {
        Page<DemoStockCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoStockCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(checkNo != null && !checkNo.isEmpty(), DemoStockCheck::getCheckNo, checkNo)
                .eq(warehouseCode != null && !warehouseCode.isEmpty(), DemoStockCheck::getWarehouseCode, warehouseCode)
                .like(warehouseName != null && !warehouseName.isEmpty(), DemoStockCheck::getWarehouseName, warehouseName)
                .eq(status != null, DemoStockCheck::getStatus, status)
                .orderByDesc(DemoStockCheck::getCheckDate);
        IPage<DemoStockCheck> result = demoStockCheckService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoStockCheck> detail(@PathVariable Long id) {
        DemoStockCheck check = demoStockCheckService.getById(id);
        return Result.success(check);
    }

    @GetMapping("/all")
    public Result<List<DemoStockCheck>> listAll() {
        List<DemoStockCheck> list = demoStockCheckService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoStockCheckService.count();
        long inProgress = demoStockCheckService.count(new LambdaQueryWrapper<DemoStockCheck>().eq(DemoStockCheck::getStatus, 0));
        long audited = demoStockCheckService.count(new LambdaQueryWrapper<DemoStockCheck>().eq(DemoStockCheck::getStatus, 1));
        long completed = demoStockCheckService.count(new LambdaQueryWrapper<DemoStockCheck>().eq(DemoStockCheck::getStatus, 2));

        stats.put("total", total);
        stats.put("inProgress", inProgress);
        stats.put("audited", audited);
        stats.put("completed", completed);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoStockCheck check) {
        if (check.getStatus() == null) {
            check.setStatus(0);
        }
        if (check.getCheckDate() == null) {
            check.setCheckDate(LocalDateTime.now());
        }
        if (check.getCheckNo() == null || check.getCheckNo().isEmpty()) {
            check.setCheckNo("PC" + System.currentTimeMillis());
        }
        demoStockCheckService.save(check);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoStockCheck check) {
        demoStockCheckService.updateById(check);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoStockCheckService.removeById(id);
        return Result.success();
    }

    @GetMapping("/details/{checkId}")
    public Result<List<DemoStockCheckDetail>> getDetails(@PathVariable Long checkId) {
        LambdaQueryWrapper<DemoStockCheckDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DemoStockCheckDetail::getCheckId, checkId);
        List<DemoStockCheckDetail> details = demoStockCheckDetailService.list(wrapper);
        return Result.success(details);
    }

    @PostMapping("/create-detail/{checkId}")
    public Result<Void> createDetail(@PathVariable Long checkId, @RequestBody List<DemoStockCheckDetail> details) {
        for (DemoStockCheckDetail detail : details) {
            detail.setCheckId(checkId);
            detail.setDiffQuantity(detail.getActualQuantity() - detail.getSystemQuantity());
            demoStockCheckDetailService.save(detail);
        }
        return Result.success();
    }

    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id) {
        DemoStockCheck check = demoStockCheckService.getById(id);
        if (check == null) {
            return Result.error("盘点单不存在");
        }

        check.setStatus(1);
        demoStockCheckService.updateById(check);
        return Result.success();
    }

    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        DemoStockCheck check = demoStockCheckService.getById(id);
        if (check == null) {
            return Result.error("盘点单不存在");
        }

        LambdaQueryWrapper<DemoStockCheckDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DemoStockCheckDetail::getCheckId, id);
        List<DemoStockCheckDetail> details = demoStockCheckDetailService.list(wrapper);

        for (DemoStockCheckDetail detail : details) {
            if (detail.getDiffQuantity() != 0) {
                LambdaQueryWrapper<DemoStock> stockWrapper = new LambdaQueryWrapper<>();
                stockWrapper.eq(DemoStock::getMaterialId, detail.getMaterialId())
                        .eq(DemoStock::getWarehouseId, check.getWarehouseId());
                DemoStock stock = demoStockService.getOne(stockWrapper);

                if (stock != null) {
                    stock.setQuantity(detail.getActualQuantity());
                    demoStockService.updateById(stock);
                }
            }
            detail.setHandleStatus(1);
            demoStockCheckDetailService.updateById(detail);
        }

        check.setStatus(2);
        demoStockCheckService.updateById(check);

        return Result.success();
    }
}