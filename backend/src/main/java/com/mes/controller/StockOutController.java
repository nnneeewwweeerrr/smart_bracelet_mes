package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.StockOut;
import com.mes.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-out")
public class StockOutController {

    @Autowired
    private StockOutService stockOutService;

    @GetMapping("/list")
    public Result<IPage<StockOut>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<StockOut> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StockOut::getCreateTime);
        return Result.success(stockOutService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockOut> detail(@PathVariable Long id) {
        return Result.success(stockOutService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "出库管理", type = "新增", description = "手动出库")
    public Result<Void> save(@RequestBody StockOut stockOut) {
        stockOutService.save(stockOut);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "出库管理", type = "删除", description = "删除出库记录")
    public Result<Void> delete(@PathVariable Long id) {
        stockOutService.removeById(id);
        return Result.success();
    }
}
