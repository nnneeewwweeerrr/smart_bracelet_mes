package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.*;
import com.mes.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SOP电子看板 + 防错校验管理接口
 * 提供工序SOP、工艺参数标准、物料BOM的完整管理能力
 */
@RestController
@RequestMapping("/api/sop")
public class SopController {

    @Autowired
    private ProcessSopService processSopService;

    @Autowired
    private ProcessParamStandardService processParamStandardService;

    @Autowired
    private ProcessMaterialBomService processMaterialBomService;

    @Autowired
    private ProcessInfoService processInfoService;

    @Autowired
    private MaterialInfoService materialInfoService;

    // ==================== SOP管理 ====================

    /**
     * 按工序ID查询SOP详情
     */
    @GetMapping("/process/{processId}")
    public Result<Map<String, Object>> getSopByProcessId(@PathVariable Long processId) {
        Map<String, Object> result = new HashMap<>();

        // 工序基本信息
        ProcessInfo process = processInfoService.getById(processId);
        result.put("process", process);

        // SOP信息
        LambdaQueryWrapper<ProcessSop> sopWrapper = new LambdaQueryWrapper<>();
        sopWrapper.eq(ProcessSop::getProcessId, processId)
                .eq(ProcessSop::getStatus, 1)
                .orderByDesc(ProcessSop::getCreateTime)
                .last("LIMIT 1");
        ProcessSop sop = processSopService.getOne(sopWrapper);
        result.put("sop", sop);

        // 工艺参数标准
        LambdaQueryWrapper<ProcessParamStandard> paramWrapper = new LambdaQueryWrapper<>();
        paramWrapper.eq(ProcessParamStandard::getProcessId, processId)
                .orderByAsc(ProcessParamStandard::getSortOrder);
        List<ProcessParamStandard> params = processParamStandardService.list(paramWrapper);
        result.put("paramStandards", params);

        // 物料BOM（关联物料名称）
        LambdaQueryWrapper<ProcessMaterialBom> bomWrapper = new LambdaQueryWrapper<>();
        bomWrapper.eq(ProcessMaterialBom::getProcessId, processId)
                .orderByAsc(ProcessMaterialBom::getSortOrder);
        List<ProcessMaterialBom> bomList = processMaterialBomService.list(bomWrapper);
        List<Map<String, Object>> bomDetailList = new ArrayList<>();
        for (ProcessMaterialBom bom : bomList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", bom.getId());
            item.put("processId", bom.getProcessId());
            item.put("materialId", bom.getMaterialId());
            item.put("quantity", bom.getQuantity());
            item.put("isRequired", bom.getIsRequired());
            item.put("sortOrder", bom.getSortOrder());
            MaterialInfo material = materialInfoService.getById(bom.getMaterialId());
            if (material != null) {
                item.put("materialCode", material.getMaterialNo());
                item.put("materialName", material.getMaterialName());
                item.put("unit", material.getUnit());
            }
            bomDetailList.add(item);
        }
        result.put("materialBom", bomDetailList);

        return Result.success(result);
    }

    /**
     * 按工单ID查询SOP（工单关联工序，自动返回对应SOP）
     */
    @GetMapping("/work-order/{workOrderId}")
    public Result<Map<String, Object>> getSopByWorkOrderId(
            @PathVariable Long workOrderId,
            @RequestParam(required = false) Long processId) {
        // 这里通过前端传入processId，或者从工单中获取
        // 前端已有工单详情，直接传processId过来更简洁
        if (processId == null) {
            return Result.error("请指定工序ID");
        }
        return getSopByProcessId(processId);
    }

    /**
     * 保存SOP
     */
    @PostMapping("/save")
    @OperationLog(module = "SOP管理", type = "新增", description = "新增工序SOP")
    public Result<Void> saveSop(@RequestBody ProcessSop sop) {
        processSopService.saveOrUpdate(sop);
        return Result.success();
    }

    /**
     * 更新SOP
     */
    @PutMapping("/update")
    @OperationLog(module = "SOP管理", type = "修改", description = "修改工序SOP")
    public Result<Void> updateSop(@RequestBody ProcessSop sop) {
        processSopService.updateById(sop);
        return Result.success();
    }

    /**
     * 删除SOP
     */
    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "SOP管理", type = "删除", description = "删除工序SOP")
    public Result<Void> deleteSop(@PathVariable Long id) {
        processSopService.removeById(id);
        return Result.success();
    }

    // ==================== SOP版本管理 ====================

    /**
     * 查询工序的所有SOP版本列表
     */
    @GetMapping("/versions/{processId}")
    public Result<List<Map<String, Object>>> getSopVersions(@PathVariable Long processId) {
        LambdaQueryWrapper<ProcessSop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessSop::getProcessId, processId)
                .orderByDesc(ProcessSop::getCreateTime);
        List<ProcessSop> sopList = processSopService.list(wrapper);
        List<Map<String, Object>> versions = sopList.stream().map(sop -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", sop.getId());
            item.put("sopName", sop.getSopName());
            item.put("sopVersion", sop.getSopVersion());
            item.put("status", sop.getStatus());
            item.put("createTime", sop.getCreateTime());
            return item;
        }).collect(Collectors.toList());
        return Result.success(versions);
    }

    /**
     * 按工序ID和版本号查询SOP详情
     */
    @GetMapping("/process/{processId}/version/{version}")
    public Result<Map<String, Object>> getSopByVersion(
            @PathVariable Long processId, @PathVariable String version) {
        Map<String, Object> result = new HashMap<>();
        ProcessInfo process = processInfoService.getById(processId);
        result.put("process", process);

        LambdaQueryWrapper<ProcessSop> sopWrapper = new LambdaQueryWrapper<>();
        sopWrapper.eq(ProcessSop::getProcessId, processId)
                .eq(ProcessSop::getSopVersion, version)
                .orderByDesc(ProcessSop::getCreateTime)
                .last("LIMIT 1");
        ProcessSop sop = processSopService.getOne(sopWrapper);
        result.put("sop", sop);

        LambdaQueryWrapper<ProcessParamStandard> paramWrapper = new LambdaQueryWrapper<>();
        paramWrapper.eq(ProcessParamStandard::getProcessId, processId)
                .orderByAsc(ProcessParamStandard::getSortOrder);
        result.put("paramStandards", processParamStandardService.list(paramWrapper));

        LambdaQueryWrapper<ProcessMaterialBom> bomWrapper = new LambdaQueryWrapper<>();
        bomWrapper.eq(ProcessMaterialBom::getProcessId, processId)
                .orderByAsc(ProcessMaterialBom::getSortOrder);
        List<ProcessMaterialBom> bomList = processMaterialBomService.list(bomWrapper);
        List<Map<String, Object>> bomDetailList = new ArrayList<>();
        for (ProcessMaterialBom bom : bomList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", bom.getId());
            item.put("processId", bom.getProcessId());
            item.put("materialId", bom.getMaterialId());
            item.put("quantity", bom.getQuantity());
            item.put("isRequired", bom.getIsRequired());
            item.put("sortOrder", bom.getSortOrder());
            MaterialInfo material = materialInfoService.getById(bom.getMaterialId());
            if (material != null) {
                item.put("materialCode", material.getMaterialNo());
                item.put("materialName", material.getMaterialName());
                item.put("unit", material.getUnit());
            }
            bomDetailList.add(item);
        }
        result.put("materialBom", bomDetailList);
        return Result.success(result);
    }

    // ==================== 工艺参数标准管理 ====================

    /**
     * 查询工序的参数标准列表
     */
    @GetMapping("/param/list/{processId}")
    public Result<List<ProcessParamStandard>> paramList(@PathVariable Long processId) {
        LambdaQueryWrapper<ProcessParamStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessParamStandard::getProcessId, processId)
                .orderByAsc(ProcessParamStandard::getSortOrder);
        return Result.success(processParamStandardService.list(wrapper));
    }

    /**
     * 保存参数标准
     */
    @PostMapping("/param/save")
    @OperationLog(module = "SOP管理", type = "新增", description = "新增工艺参数标准")
    public Result<Void> saveParam(@RequestBody ProcessParamStandard param) {
        processParamStandardService.saveOrUpdate(param);
        return Result.success();
    }

    /**
     * 删除参数标准
     */
    @DeleteMapping("/param/delete/{id}")
    @OperationLog(module = "SOP管理", type = "删除", description = "删除工艺参数标准")
    public Result<Void> deleteParam(@PathVariable Long id) {
        processParamStandardService.removeById(id);
        return Result.success();
    }

    // ==================== 物料BOM管理 ====================

    /**
     * 查询工序的物料BOM列表
     */
    @GetMapping("/bom/list/{processId}")
    public Result<List<Map<String, Object>>> bomList(@PathVariable Long processId) {
        LambdaQueryWrapper<ProcessMaterialBom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessMaterialBom::getProcessId, processId)
                .orderByAsc(ProcessMaterialBom::getSortOrder);
        List<ProcessMaterialBom> bomList = processMaterialBomService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessMaterialBom bom : bomList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", bom.getId());
            item.put("processId", bom.getProcessId());
            item.put("materialId", bom.getMaterialId());
            item.put("quantity", bom.getQuantity());
            item.put("isRequired", bom.getIsRequired());
            item.put("sortOrder", bom.getSortOrder());
            MaterialInfo material = materialInfoService.getById(bom.getMaterialId());
            if (material != null) {
                item.put("materialCode", material.getMaterialNo());
                item.put("materialName", material.getMaterialName());
                item.put("unit", material.getUnit());
            }
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 保存物料BOM
     */
    @PostMapping("/bom/save")
    @OperationLog(module = "SOP管理", type = "新增", description = "新增工序物料BOM")
    public Result<Void> saveBom(@RequestBody ProcessMaterialBom bom) {
        processMaterialBomService.saveOrUpdate(bom);
        return Result.success();
    }

    /**
     * 删除物料BOM
     */
    @DeleteMapping("/bom/delete/{id}")
    @OperationLog(module = "SOP管理", type = "删除", description = "删除工序物料BOM")
    public Result<Void> deleteBom(@PathVariable Long id) {
        processMaterialBomService.removeById(id);
        return Result.success();
    }

    // ==================== 防错校验 ====================

    /**
     * 物料防错校验：核对扫码物料是否属于当前工序BOM
     */
    @PostMapping("/verify/material")
    public Result<Map<String, Object>> verifyMaterial(@RequestBody Map<String, Object> params) {
        String materialCode = (String) params.get("materialCode");
        Long processId = params.get("processId") != null ? Long.valueOf(params.get("processId").toString()) : null;

        Map<String, Object> result = new HashMap<>();

        if (materialCode == null || processId == null) {
            result.put("pass", false);
            result.put("reason", "参数缺失：需要物料编码和工序ID");
            return Result.success(result);
        }

        // 查询物料
        LambdaQueryWrapper<MaterialInfo> matWrapper = new LambdaQueryWrapper<>();
        matWrapper.eq(MaterialInfo::getMaterialNo, materialCode);
        MaterialInfo material = materialInfoService.getOne(matWrapper);

        if (material == null) {
            result.put("pass", false);
            result.put("reason", "物料编码不存在：" + materialCode);
            result.put("materialCode", materialCode);
            return Result.success(result);
        }

        result.put("materialCode", material.getMaterialNo());
        result.put("materialName", material.getMaterialName());

        // 查询工序BOM
        LambdaQueryWrapper<ProcessMaterialBom> bomWrapper = new LambdaQueryWrapper<>();
        bomWrapper.eq(ProcessMaterialBom::getProcessId, processId)
                .eq(ProcessMaterialBom::getMaterialId, material.getId());
        ProcessMaterialBom bom = processMaterialBomService.getOne(bomWrapper);

        if (bom == null) {
            result.put("pass", false);
            result.put("reason", "物料「" + material.getMaterialName() + "」不属于当前工序的BOM范围，请确认物料是否正确");
            return Result.success(result);
        }

        result.put("pass", true);
        result.put("reason", "物料校验通过");
        result.put("quantity", bom.getQuantity());
        return Result.success(result);
    }

    /**
     * 工艺参数防错校验：核对实际参数是否在标准阈值内
     */
    @PostMapping("/verify/params")
    public Result<Map<String, Object>> verifyParams(@RequestBody Map<String, Object> params) {
        Long processId = params.get("processId") != null ? Long.valueOf(params.get("processId").toString()) : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> actualParams = (Map<String, Object>) params.get("params");

        Map<String, Object> result = new HashMap<>();

        if (processId == null || actualParams == null) {
            result.put("pass", false);
            result.put("reason", "参数缺失：需要工序ID和实际参数");
            return Result.success(result);
        }

        // 查询参数标准
        LambdaQueryWrapper<ProcessParamStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessParamStandard::getProcessId, processId)
                .orderByAsc(ProcessParamStandard::getSortOrder);
        List<ProcessParamStandard> standards = processParamStandardService.list(wrapper);

        List<Map<String, Object>> checkResults = new ArrayList<>();
        boolean allPass = true;
        StringBuilder failReason = new StringBuilder();

        for (ProcessParamStandard std : standards) {
            Map<String, Object> check = new HashMap<>();
            check.put("paramName", std.getParamName());
            check.put("paramCode", std.getParamCode());
            check.put("unit", std.getUnit());
            check.put("minValue", std.getMinValue());
            check.put("maxValue", std.getMaxValue());
            check.put("targetValue", std.getTargetValue());

            Object actualVal = actualParams.get(std.getParamCode());
            if (actualVal == null || actualVal.toString().isEmpty()) {
                if (std.getIsRequired() != null && std.getIsRequired() == 1) {
                    check.put("pass", false);
                    check.put("reason", "必检参数未填写");
                    check.put("actualValue", null);
                    allPass = false;
                    if (failReason.length() > 0) failReason.append("；");
                    failReason.append(std.getParamName()).append("未填写");
                } else {
                    check.put("pass", true);
                    check.put("reason", "非必检参数，已跳过");
                    check.put("actualValue", null);
                }
            } else {
                try {
                    double actual = Double.parseDouble(actualVal.toString());
                    check.put("actualValue", actual);
                    double min = std.getMinValue() != null ? std.getMinValue().doubleValue() : Double.MIN_VALUE;
                    double max = std.getMaxValue() != null ? std.getMaxValue().doubleValue() : Double.MAX_VALUE;

                    if (actual < min || actual > max) {
                        check.put("pass", false);
                        check.put("reason", String.format("%s超差：标准范围%.2f~%.2f%s，实际值%.2f%s",
                                std.getParamName(), min, max, std.getUnit() != null ? std.getUnit() : "",
                                actual, std.getUnit() != null ? std.getUnit() : ""));
                        allPass = false;
                        if (failReason.length() > 0) failReason.append("；");
                        failReason.append(std.getParamName()).append("超差（")
                                .append(actual).append(std.getUnit() != null ? std.getUnit() : "")
                                .append("，标准").append(min).append("~").append(max)
                                .append(std.getUnit() != null ? std.getUnit() : "").append("）");
                    } else {
                        check.put("pass", true);
                        check.put("reason", "合格");
                    }
                } catch (NumberFormatException e) {
                    check.put("pass", false);
                    check.put("reason", "参数格式错误：" + actualVal);
                    check.put("actualValue", actualVal);
                    allPass = false;
                    if (failReason.length() > 0) failReason.append("；");
                    failReason.append(std.getParamName()).append("格式错误");
                }
            }
            checkResults.add(check);
        }

        result.put("pass", allPass);
        result.put("reason", allPass ? "全部参数校验通过" : failReason.toString());
        result.put("details", checkResults);
        return Result.success(result);
    }
}
