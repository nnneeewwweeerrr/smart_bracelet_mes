package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.MaterialInfo;
import com.mes.service.MaterialInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private MaterialInfoService materialInfoService;

    @GetMapping("/list")
    public Result<IPage<MaterialInfo>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String materialName,
                                            @RequestParam(required = false) String materialNo,
                                            @RequestParam(required = false) String type,
                                            @RequestParam(required = false) String keyword) {
        Page<MaterialInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(MaterialInfo::getMaterialName, keyword)
                    .or()
                    .like(MaterialInfo::getMaterialNo, keyword);
        } else {
            wrapper.like(materialName != null && !materialName.isEmpty(), MaterialInfo::getMaterialName, materialName)
                    .like(materialNo != null && !materialNo.isEmpty(), MaterialInfo::getMaterialNo, materialNo);
        }
        wrapper.eq(type != null && !type.isEmpty(), MaterialInfo::getType, type);
        IPage<MaterialInfo> result = materialInfoService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<MaterialInfo> detail(@PathVariable Long id) {
        MaterialInfo info = materialInfoService.getById(id);
        return Result.success(info);
    }

    @GetMapping("/all")
    public Result<List<MaterialInfo>> listAll() {
        List<MaterialInfo> list = materialInfoService.list();
        return Result.success(list);
    }

    @GetMapping("/code/{code}")
    public Result<MaterialInfo> getByCode(@PathVariable String code) {
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialInfo::getMaterialNo, code);
        return Result.success(materialInfoService.getOne(wrapper));
    }

    @PostMapping("/save")
    @OperationLog(module = "物料档案", type = "新增", description = "新增物料")
    public Result<Void> save(@RequestBody MaterialInfo materialInfo) {
        if (materialInfo.getStatus() == null) {
            materialInfo.setStatus(1);
        }
        materialInfoService.save(materialInfo);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "物料档案", type = "修改", description = "修改物料")
    public Result<Void> update(@RequestBody MaterialInfo materialInfo) {
        materialInfoService.updateById(materialInfo);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "物料档案", type = "删除", description = "删除物料")
    public Result<Void> delete(@PathVariable Long id) {
        materialInfoService.removeById(id);
        return Result.success();
    }
}