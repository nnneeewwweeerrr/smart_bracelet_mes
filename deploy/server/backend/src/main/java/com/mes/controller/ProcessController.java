package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.ProcessInfo;
import com.mes.service.ProcessInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    @Autowired
    private ProcessInfoService processInfoService;

    @GetMapping("/list")
    public Result<List<ProcessInfo>> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ProcessInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, ProcessInfo::getProductId, productId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ProcessInfo::getProcessName, keyword)
                    .or().like(ProcessInfo::getProcessCode, keyword));
        }
        wrapper.orderByAsc(ProcessInfo::getSortOrder);
        return Result.success(processInfoService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<ProcessInfo> detail(@PathVariable Long id) {
        return Result.success(processInfoService.getById(id));
    }

    @GetMapping("/product/{productId}")
    public Result<List<ProcessInfo>> getByProductId(@PathVariable Long productId) {
        return Result.success(processInfoService.listByProductId(productId));
    }

    @PostMapping("/save")
    @OperationLog(module = "工序管理", type = "新增", description = "新增工序")
    public Result<Void> save(@RequestBody ProcessInfo processInfo) {
        processInfoService.save(processInfo);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "工序管理", type = "修改", description = "修改工序")
    public Result<Void> update(@RequestBody ProcessInfo processInfo) {
        processInfoService.updateById(processInfo);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "工序管理", type = "删除", description = "删除工序")
    public Result<Void> delete(@PathVariable Long id) {
        processInfoService.removeById(id);
        return Result.success();
    }
}