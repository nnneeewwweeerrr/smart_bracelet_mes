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

    @GetMapping("/production/sop/{id}")
    public Result<ProcessInfo> getSopDetail(@PathVariable Long id) {
        return Result.success(processInfoService.getById(id));
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