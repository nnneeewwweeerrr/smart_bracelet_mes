package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.*;
import com.mes.service.*;
import com.mes.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mini")
public class MiniAppController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ProductionReportService productionReportService;

    @Autowired
    private StockInfoService stockInfoService;

    @Autowired
    private WarehouseInfoService warehouseInfoService;

    @Autowired
    private MaterialInfoService materialInfoService;

    @Autowired
    private StockInService stockInService;

    @Autowired
    private StockOutService stockOutService;

    @Autowired
    private QualityTaskService qualityTaskService;

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    @Autowired
    private ProcessInfoService processInfoService;

    @Autowired
    private ProcessSopService processSopService;

    @Autowired
    private ProcessParamStandardService processParamStandardService;

    @Autowired
    private ProcessMaterialBomService processMaterialBomService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String userName = params.get("userName");
        String password = params.get("password");
        
        SysUser user = sysUserService.getByUsername(userName);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }
        
        String token = jwtUtil.generateToken(userName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("userName", user.getUserName());
        userInfo.put("realName", user.getRealName());
        userInfo.put("roleId", user.getRoleId());
        result.put("user", userInfo);
        
        return Result.success(result);
    }

    @GetMapping("/worker/tasks")
    public Result<List<WorkOrder>> getWorkerTasks() {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkOrder::getStatus, 0, 1);
        wrapper.orderByDesc(WorkOrder::getCreateTime);
        return Result.success(workOrderService.list(wrapper));
    }

    @PostMapping("/production/report/scan")
    public Result<Map<String, Object>> scanProductionReport(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getWorkNo, code);
        WorkOrder wo = workOrderService.getOne(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        if (wo != null) {
            result.put("workOrderId", wo.getId());
            result.put("workNo", wo.getWorkNo());
            result.put("processId", wo.getProcessId());
        }
        return Result.success(result);
    }

    @PostMapping("/stock/in/scan")
    public Result<Map<String, Object>> scanStockIn(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialInfo::getMaterialCode, code);
        MaterialInfo material = materialInfoService.getOne(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        if (material != null) {
            result.put("materialId", material.getId());
            result.put("materialCode", material.getMaterialCode());
            result.put("materialName", material.getMaterialName());
        }
        return Result.success(result);
    }

    @PostMapping("/stock/out/scan")
    public Result<Map<String, Object>> scanStockOut(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialInfo::getMaterialCode, code);
        MaterialInfo material = materialInfoService.getOne(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        if (material != null) {
            result.put("materialId", material.getId());
            result.put("materialCode", material.getMaterialCode());
            result.put("materialName", material.getMaterialName());
        }
        return Result.success(result);
    }

    @GetMapping("/production/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        long totalOrders = workOrderService.count();
        long runningOrders = workOrderService.count(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 1));
        long completedOrders = workOrderService.count(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 2));
        long pendingExceptions = exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 0));
        
        dashboard.put("totalOrders", totalOrders);
        dashboard.put("runningOrders", runningOrders);
        dashboard.put("completedOrders", completedOrders);
        dashboard.put("pendingExceptions", pendingExceptions);
        
        return Result.success(dashboard);
    }

    @GetMapping("/stock/warning")
    public Result<List<Map<String, Object>>> getStockWarning() {
        List<StockInfo> allStock = stockInfoService.list();
        List<Map<String, Object>> warnings = new ArrayList<>();
        
        for (StockInfo si : allStock) {
            if (si.getQuantity() != null && si.getQuantity().compareTo(new java.math.BigDecimal("100")) < 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", si.getId());
                item.put("materialId", si.getMaterialId());
                item.put("quantity", si.getQuantity());
                warnings.add(item);
            }
        }
        return Result.success(warnings);
    }

    @GetMapping("/production/sop/list")
    public Result<List<ProcessInfo>> getSopList() {
        return Result.success(processInfoService.list(new LambdaQueryWrapper<ProcessInfo>().orderByAsc(ProcessInfo::getSortOrder)));
    }

    /**
     * 按工序ID查询完整SOP（含操作步骤、参数标准、物料BOM）
     */
    @GetMapping("/production/sop/{id}")
    public Result<Map<String, Object>> getSopDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        // 工序基本信息
        ProcessInfo process = processInfoService.getById(id);
        result.put("process", process);

        // SOP信息
        LambdaQueryWrapper<ProcessSop> sopWrapper = new LambdaQueryWrapper<>();
        sopWrapper.eq(ProcessSop::getProcessId, id)
                .eq(ProcessSop::getStatus, 1)
                .orderByDesc(ProcessSop::getCreateTime)
                .last("LIMIT 1");
        ProcessSop sop = processSopService.getOne(sopWrapper);
        result.put("sop", sop);

        // 工艺参数标准
        LambdaQueryWrapper<ProcessParamStandard> paramWrapper = new LambdaQueryWrapper<>();
        paramWrapper.eq(ProcessParamStandard::getProcessId, id)
                .orderByAsc(ProcessParamStandard::getSortOrder);
        List<ProcessParamStandard> params = processParamStandardService.list(paramWrapper);
        result.put("paramStandards", params);

        // 物料BOM（关联物料名称）
        LambdaQueryWrapper<ProcessMaterialBom> bomWrapper = new LambdaQueryWrapper<>();
        bomWrapper.eq(ProcessMaterialBom::getProcessId, id)
                .orderByAsc(ProcessMaterialBom::getSortOrder);
        List<ProcessMaterialBom> bomList = processMaterialBomService.list(bomWrapper);
        List<Map<String, Object>> bomDetailList = new ArrayList<>();
        for (ProcessMaterialBom bom : bomList) {
            Map<String, Object> item = new HashMap<>();
            item.put("materialId", bom.getMaterialId());
            item.put("quantity", bom.getQuantity());
            item.put("isRequired", bom.getIsRequired());
            MaterialInfo material = materialInfoService.getById(bom.getMaterialId());
            if (material != null) {
                item.put("materialCode", material.getMaterialCode());
                item.put("materialName", material.getMaterialName());
                item.put("unit", material.getUnit());
            }
            bomDetailList.add(item);
        }
        result.put("materialBom", bomDetailList);

        return Result.success(result);
    }

    /**
     * 按工单号查询SOP（扫码后自动获取当前工序SOP）
     */
    @GetMapping("/production/sop/work-order/{workOrderId}")
    public Result<Map<String, Object>> getSopByWorkOrder(@PathVariable Long workOrderId) {
        WorkOrder wo = workOrderService.getById(workOrderId);
        if (wo == null) {
            return Result.error("工单不存在");
        }
        if (wo.getProcessId() == null) {
            return Result.error("工单未关联工序");
        }
        return getSopDetail(wo.getProcessId());
    }

    /**
     * 物料防错校验（小程序扫码后调用）
     */
    @PostMapping("/production/verify/material")
    public Result<Map<String, Object>> verifyMaterial(@RequestBody Map<String, Object> params) {
        String materialCode = (String) params.get("materialCode");
        Long processId = params.get("processId") != null ? Long.valueOf(params.get("processId").toString()) : null;

        Map<String, Object> result = new HashMap<>();

        if (materialCode == null || processId == null) {
            result.put("pass", false);
            result.put("reason", "参数缺失");
            return Result.success(result);
        }

        LambdaQueryWrapper<MaterialInfo> matWrapper = new LambdaQueryWrapper<>();
        matWrapper.eq(MaterialInfo::getMaterialCode, materialCode);
        MaterialInfo material = materialInfoService.getOne(matWrapper);

        if (material == null) {
            result.put("pass", false);
            result.put("reason", "物料编码不存在：" + materialCode);
            result.put("materialCode", materialCode);
            return Result.success(result);
        }

        result.put("materialCode", material.getMaterialCode());
        result.put("materialName", material.getMaterialName());

        LambdaQueryWrapper<ProcessMaterialBom> bomWrapper = new LambdaQueryWrapper<>();
        bomWrapper.eq(ProcessMaterialBom::getProcessId, processId)
                .eq(ProcessMaterialBom::getMaterialId, material.getId());
        ProcessMaterialBom bom = processMaterialBomService.getOne(bomWrapper);

        if (bom == null) {
            result.put("pass", false);
            result.put("reason", "物料「" + material.getMaterialName() + "」不属于当前工序的BOM范围");
        } else {
            result.put("pass", true);
            result.put("reason", "物料校验通过");
        }
        return Result.success(result);
    }

    /**
     * 工艺参数校验（小程序录入参数后调用）
     */
    @PostMapping("/production/verify/params")
    public Result<Map<String, Object>> verifyParams(@RequestBody Map<String, Object> params) {
        Long processId = params.get("processId") != null ? Long.valueOf(params.get("processId").toString()) : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> actualParams = (Map<String, Object>) params.get("params");

        Map<String, Object> result = new HashMap<>();

        if (processId == null || actualParams == null) {
            result.put("pass", false);
            result.put("reason", "参数缺失");
            return Result.success(result);
        }

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

            Object actualVal = actualParams.get(std.getParamCode());
            if (actualVal == null || actualVal.toString().isEmpty()) {
                if (std.getIsRequired() != null && std.getIsRequired() == 1) {
                    check.put("pass", false);
                    check.put("reason", "必检参数未填写");
                    allPass = false;
                    if (failReason.length() > 0) failReason.append("；");
                    failReason.append(std.getParamName()).append("未填写");
                } else {
                    check.put("pass", true);
                    check.put("reason", "非必检，已跳过");
                }
            } else {
                try {
                    double actual = Double.parseDouble(actualVal.toString());
                    check.put("actualValue", actual);
                    double min = std.getMinValue() != null ? std.getMinValue().doubleValue() : Double.MIN_VALUE;
                    double max = std.getMaxValue() != null ? std.getMaxValue().doubleValue() : Double.MAX_VALUE;
                    String unit = std.getUnit() != null ? std.getUnit() : "";

                    if (actual < min || actual > max) {
                        check.put("pass", false);
                        check.put("reason", "超差：标准" + min + "~" + max + unit + "，实际" + actual + unit);
                        allPass = false;
                        if (failReason.length() > 0) failReason.append("；");
                        failReason.append(std.getParamName()).append("超差");
                    } else {
                        check.put("pass", true);
                        check.put("reason", "合格");
                    }
                } catch (NumberFormatException e) {
                    check.put("pass", false);
                    check.put("reason", "格式错误");
                    allPass = false;
                }
            }
            checkResults.add(check);
        }

        result.put("pass", allPass);
        result.put("reason", allPass ? "全部参数校验通过" : failReason.toString());
        result.put("details", checkResults);
        return Result.success(result);
    }

    @GetMapping("/quality/statistics")
    public Result<Map<String, Object>> getQualityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long total = qualityTaskService.count();
        long completed = qualityTaskService.count(new LambdaQueryWrapper<QualityTask>().eq(QualityTask::getStatus, 2));
        long pending = qualityTaskService.count(new LambdaQueryWrapper<QualityTask>().eq(QualityTask::getStatus, 0));
        
        stats.put("total", total);
        stats.put("passed", completed);
        stats.put("failed", 0);
        stats.put("pending", pending);
        return Result.success(stats);
    }
}