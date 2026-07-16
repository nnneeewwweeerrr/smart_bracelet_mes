package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoEquipmentMaintain;
import com.mes.service.DemoEquipmentMaintainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/equipment-maintain")
public class DemoEquipmentMaintainController {

    @Autowired
    private DemoEquipmentMaintainService demoEquipmentMaintainService;

    @GetMapping("/list")
    public Result<IPage<DemoEquipmentMaintain>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) String maintainNo,
                                                      @RequestParam(required = false) String equipmentNo,
                                                      @RequestParam(required = false) String equipmentName,
                                                      @RequestParam(required = false) String maintainType,
                                                      @RequestParam(required = false) Integer status) {
        Page<DemoEquipmentMaintain> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoEquipmentMaintain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(maintainNo != null && !maintainNo.isEmpty(), DemoEquipmentMaintain::getMaintainNo, maintainNo)
                .like(equipmentNo != null && !equipmentNo.isEmpty(), DemoEquipmentMaintain::getEquipmentNo, equipmentNo)
                .like(equipmentName != null && !equipmentName.isEmpty(), DemoEquipmentMaintain::getEquipmentName, equipmentName)
                .eq(maintainType != null && !maintainType.isEmpty(), DemoEquipmentMaintain::getMaintainType, maintainType)
                .eq(status != null, DemoEquipmentMaintain::getStatus, status)
                .orderByDesc(DemoEquipmentMaintain::getActualDate);
        IPage<DemoEquipmentMaintain> result = demoEquipmentMaintainService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoEquipmentMaintain> detail(@PathVariable Long id) {
        DemoEquipmentMaintain maintain = demoEquipmentMaintainService.getById(id);
        return Result.success(maintain);
    }

    @GetMapping("/all")
    public Result<List<DemoEquipmentMaintain>> listAll() {
        List<DemoEquipmentMaintain> list = demoEquipmentMaintainService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoEquipmentMaintainService.count();
        long pending = demoEquipmentMaintainService.count(new LambdaQueryWrapper<DemoEquipmentMaintain>().eq(DemoEquipmentMaintain::getStatus, 0));
        long inProgress = demoEquipmentMaintainService.count(new LambdaQueryWrapper<DemoEquipmentMaintain>().eq(DemoEquipmentMaintain::getStatus, 1));
        long completed = demoEquipmentMaintainService.count(new LambdaQueryWrapper<DemoEquipmentMaintain>().eq(DemoEquipmentMaintain::getStatus, 2));

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoEquipmentMaintain maintain) {
        if (maintain.getStatus() == null) {
            maintain.setStatus(0);
        }
        demoEquipmentMaintainService.save(maintain);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoEquipmentMaintain maintain) {
        demoEquipmentMaintainService.updateById(maintain);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoEquipmentMaintainService.removeById(id);
        return Result.success();
    }

    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        DemoEquipmentMaintain maintain = demoEquipmentMaintainService.getById(id);
        if (maintain == null) {
            return Result.error("维保记录不存在");
        }

        maintain.setStatus(1);
        demoEquipmentMaintainService.updateById(maintain);
        return Result.success();
    }

    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        DemoEquipmentMaintain maintain = demoEquipmentMaintainService.getById(id);
        if (maintain == null) {
            return Result.error("维保记录不存在");
        }

        maintain.setActualDate(LocalDateTime.now());
        maintain.setMaintainContent((String) params.get("maintainContent"));
        maintain.setMaintainResult((String) params.get("maintainResult"));
        if (params.get("cost") != null) {
            maintain.setCost(new java.math.BigDecimal(params.get("cost").toString()));
        }
        if (params.get("durationHours") != null) {
            maintain.setDurationHours(new java.math.BigDecimal(params.get("durationHours").toString()));
        }
        maintain.setStatus(2);
        demoEquipmentMaintainService.updateById(maintain);

        return Result.success();
    }
}