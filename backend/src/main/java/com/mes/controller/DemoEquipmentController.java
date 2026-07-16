package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoEquipment;
import com.mes.service.DemoEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/equipment")
public class DemoEquipmentController {

    @Autowired
    private DemoEquipmentService demoEquipmentService;

    @GetMapping("/list")
    public Result<IPage<DemoEquipment>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String equipmentNo,
                                              @RequestParam(required = false) String equipmentName,
                                              @RequestParam(required = false) String equipmentType,
                                              @RequestParam(required = false) String workshop,
                                              @RequestParam(required = false) Integer status) {
        Page<DemoEquipment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(equipmentNo != null && !equipmentNo.isEmpty(), DemoEquipment::getEquipmentNo, equipmentNo)
                .like(equipmentName != null && !equipmentName.isEmpty(), DemoEquipment::getEquipmentName, equipmentName)
                .eq(equipmentType != null && !equipmentType.isEmpty(), DemoEquipment::getEquipmentType, equipmentType)
                .eq(workshop != null && !workshop.isEmpty(), DemoEquipment::getWorkshop, workshop)
                .eq(status != null, DemoEquipment::getStatus, status)
                .orderByDesc(DemoEquipment::getCreateTime);
        IPage<DemoEquipment> result = demoEquipmentService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoEquipment> detail(@PathVariable Long id) {
        DemoEquipment info = demoEquipmentService.getById(id);
        return Result.success(info);
    }

    @GetMapping("/all")
    public Result<List<DemoEquipment>> listAll() {
        List<DemoEquipment> list = demoEquipmentService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoEquipmentService.count();
        long running = demoEquipmentService.count(new LambdaQueryWrapper<DemoEquipment>().eq(DemoEquipment::getStatus, 1));
        long stopped = demoEquipmentService.count(new LambdaQueryWrapper<DemoEquipment>().eq(DemoEquipment::getStatus, 0));
        long repairing = demoEquipmentService.count(new LambdaQueryWrapper<DemoEquipment>().eq(DemoEquipment::getStatus, 2));
        long scrapped = demoEquipmentService.count(new LambdaQueryWrapper<DemoEquipment>().eq(DemoEquipment::getStatus, 3));

        long needMaintain = demoEquipmentService.count(new LambdaQueryWrapper<DemoEquipment>()
                .isNotNull(DemoEquipment::getNextMaintainDate)
                .le(DemoEquipment::getNextMaintainDate, LocalDateTime.now()));

        stats.put("total", total);
        stats.put("running", running);
        stats.put("stopped", stopped);
        stats.put("repairing", repairing);
        stats.put("scrapped", scrapped);
        stats.put("needMaintain", needMaintain);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoEquipment equipment) {
        if (equipment.getStatus() == null) {
            equipment.setStatus(1);
        }
        demoEquipmentService.save(equipment);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoEquipment equipment) {
        demoEquipmentService.updateById(equipment);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoEquipmentService.removeById(id);
        return Result.success();
    }
}