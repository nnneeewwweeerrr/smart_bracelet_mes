package com.mes.controller;

import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.SysDict;
import com.mes.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private SysDictService sysDictService;

    @GetMapping("/list")
    public Result<List<SysDict>> list() {
        return Result.success(sysDictService.list());
    }

    @GetMapping("/type/{dictType}")
    public Result<List<SysDict>> listByType(@PathVariable String dictType) {
        return Result.success(sysDictService.listByType(dictType));
    }

    @PostMapping("/save")
    @OperationLog(module = "系统管理", type = "新增", description = "新增数据字典")
    public Result<Void> save(@RequestBody SysDict dict) {
        sysDictService.save(dict);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "系统管理", type = "修改", description = "修改数据字典")
    public Result<Void> update(@RequestBody SysDict dict) {
        sysDictService.updateById(dict);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "系统管理", type = "删除", description = "删除数据字典")
    public Result<Void> delete(@PathVariable Long id) {
        sysDictService.removeById(id);
        return Result.success();
    }
}
