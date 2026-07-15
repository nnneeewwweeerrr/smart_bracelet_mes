package com.mes.controller;

import com.mes.common.Result;
import com.mes.entity.EquipmentFault;
import com.mes.service.EquipmentFaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/equipment-fault")
public class EquipmentFaultController {

    @Autowired
    private EquipmentFaultService equipmentFaultService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                            @RequestParam(defaultValue = "10") Long pageSize,
                                            @RequestParam(required = false) String faultNo,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Long equipmentId) {
        return equipmentFaultService.getPage(pageNum, pageSize, faultNo, status, equipmentId);
    }

    @GetMapping("/{id}")
    public Result<EquipmentFault> detail(@PathVariable Long id) {
        return Result.success(equipmentFaultService.getById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody EquipmentFault fault) {
        equipmentFaultService.save(fault);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody EquipmentFault fault) {
        equipmentFaultService.updateById(fault);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentFaultService.removeById(id);
        return Result.success();
    }

    @PutMapping("/assign/{id}")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long assigneeId = Long.valueOf(params.get("assigneeId").toString());
        return equipmentFaultService.assignFault(id, assigneeId, "manual");
    }

    @PutMapping("/auto-assign/{id}")
    public Result<Void> autoAssign(@PathVariable Long id) {
        return equipmentFaultService.autoAssign(id);
    }

    @PutMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String handler = (String) params.get("handler");
        String solution = (String) params.get("solution");
        Integer status = Integer.valueOf(params.get("status").toString());
        Double repairHours = params.get("repairHours") != null ? Double.valueOf(params.get("repairHours").toString()) : null;
        return equipmentFaultService.handleFault(id, handler, solution, status, repairHours);
    }

    @GetMapping("/my-tasks")
    public Result<Map<String, Object>> myTasks(@RequestParam Long assigneeId,
                                               @RequestParam(required = false) Integer status) {
        return equipmentFaultService.getMyTasks(assigneeId, status);
    }
}