package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.StockIn;
import com.mes.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-in")
public class StockInController {

    @Autowired
    private StockInService stockInService;

    @GetMapping("/list")
    public Result<IPage<StockIn>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StockIn::getCreateTime);
        return Result.success(stockInService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockIn> detail(@PathVariable Long id) {
        return Result.success(stockInService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "入库管理", type = "新增", description = "手动入库")
    public Result<Void> save(@RequestBody StockIn stockIn) {
        stockInService.save(stockIn);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "入库管理", type = "删除", description = "删除入库记录")
    public Result<Void> delete(@PathVariable Long id) {
        stockInService.removeById(id);
        return Result.success();
    }
}
