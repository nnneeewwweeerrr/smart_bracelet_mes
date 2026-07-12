package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.common.Result;
import com.mes.entity.*;
import com.mes.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数字化车间大屏接口
 */
@RestController
@RequestMapping("/api/big-screen")
public class BigScreenController {

    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private ProductionReportService productionReportService;
    @Autowired
    private EquipmentInfoService equipmentInfoService;
    @Autowired
    private ProcessInfoService processInfoService;
    @Autowired
    private QualityResultService qualityResultService;

    /**
     * 获取大屏全部数据
     */
    @GetMapping("/data")
    public Result<Map<String, Object>> data() {
        Map<String, Object> result = new HashMap<>();
        result.put("production", getProductionData());
        result.put("yield", getYieldData());
        result.put("equipment", getEquipmentData());
        result.put("wip", getWipData());
        result.put("defects", getDefectData());
        return Result.success(result);
    }

    /**
     * 实时产量：今日计划产量 vs 实际完成产量
     */
    @GetMapping("/production")
    public Result<Map<String, Object>> production() {
        return Result.success(getProductionData());
    }

    /**
     * 产线良率：近7天良率/不良率趋势
     */
    @GetMapping("/yield")
    public Result<Map<String, Object>> yield() {
        return Result.success(getYieldData());
    }

    /**
     * 设备状态：各工位设备运行状态
     */
    @GetMapping("/equipment")
    public Result<Map<String, Object>> equipment() {
        return Result.success(getEquipmentData());
    }

    /**
     * 在制分布：各工序WIP数量
     */
    @GetMapping("/wip")
    public Result<Map<String, Object>> wip() {
        return Result.success(getWipData());
    }

    /**
     * TOP不良：TOP5不良类型
     */
    @GetMapping("/defects")
    public Result<Map<String, Object>> defects() {
        return Result.success(getDefectData());
    }

    /**
     * 实时产量数据
     */
    private Map<String, Object> getProductionData() {
        Map<String, Object> data = new HashMap<>();

        // 今日计划产量：所有工单的quantity总和
        List<WorkOrder> allOrders = workOrderService.list();
        int planned = allOrders.stream().mapToInt(o -> o.getQuantity() != null ? o.getQuantity() : 0).sum();

        // 今日实际完成产量：今日报工的goodQuantity总和
        LocalDate today = LocalDate.now();
        List<ProductionReport> todayReports = productionReportService.list(
                new LambdaQueryWrapper<ProductionReport>().eq(ProductionReport::getReportDate, today));
        int actual = todayReports.stream().mapToInt(r -> r.getGoodQuantity() != null ? r.getGoodQuantity() : 0).sum();

        // 进度百分比
        double progress = planned > 0 ? (actual * 100.0 / planned) : 0;
        if (progress > 100) progress = 100;

        // 近7天产量趋势
        List<String> dates = new ArrayList<>();
        List<Integer> actualTrend = new ArrayList<>();
        List<Integer> plannedTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            List<ProductionReport> dayReports = productionReportService.list(
                    new LambdaQueryWrapper<ProductionReport>().eq(ProductionReport::getReportDate, date));
            int dayGood = dayReports.stream().mapToInt(r -> r.getGoodQuantity() != null ? r.getGoodQuantity() : 0).sum();
            actualTrend.add(dayGood);
            plannedTrend.add(planned / 7);
        }

        data.put("planned", planned);
        data.put("actual", actual);
        data.put("progress", Math.round(progress * 10) / 10.0);
        data.put("dates", dates);
        data.put("actualTrend", actualTrend);
        data.put("plannedTrend", plannedTrend);
        
        // 数据为空时返回演示数据
        if (planned == 0 && actual == 0) {
            data.put("planned", 5000);
            data.put("actual", 3850);
            data.put("progress", 77.0);
            data.put("actualTrend", Arrays.asList(3200, 3500, 3100, 3800, 4200, 3900, 3850));
            data.put("plannedTrend", Arrays.asList(5000, 5000, 5000, 5000, 5000, 5000, 5000));
        }
        return data;
    }

    /**
     * 良率数据
     */
    private Map<String, Object> getYieldData() {
        Map<String, Object> data = new HashMap<>();

        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        List<Double> yieldRates = new ArrayList<>();
        List<Double> defectRates = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            List<ProductionReport> dayReports = productionReportService.list(
                    new LambdaQueryWrapper<ProductionReport>().eq(ProductionReport::getReportDate, date));
            int good = dayReports.stream().mapToInt(r -> r.getGoodQuantity() != null ? r.getGoodQuantity() : 0).sum();
            int bad = dayReports.stream().mapToInt(r -> r.getBadQuantity() != null ? r.getBadQuantity() : 0).sum();
            int total = good + bad;
            double yieldRate = total > 0 ? (good * 100.0 / total) : 100.0;
            double defectRate = total > 0 ? (bad * 100.0 / total) : 0.0;
            yieldRates.add(Math.round(yieldRate * 10) / 10.0);
            defectRates.add(Math.round(defectRate * 10) / 10.0);
        }

        // 当前良率
        double currentYield = yieldRates.isEmpty() ? 100.0 : yieldRates.get(yieldRates.size() - 1);
        double currentDefect = defectRates.isEmpty() ? 0.0 : defectRates.get(defectRates.size() - 1);

        data.put("dates", dates);
        data.put("yieldRates", yieldRates);
        data.put("defectRates", defectRates);
        data.put("currentYield", currentYield);
        data.put("currentDefect", currentDefect);
        data.put("threshold", 95.0); // 预警阈值
        
        // 数据为空时返回演示数据
        boolean hasData = yieldRates.stream().anyMatch(y -> y < 100.0);
        if (!hasData) {
            data.put("yieldRates", Arrays.asList(98.2, 97.5, 96.8, 97.9, 95.3, 94.1, 96.5));
            data.put("defectRates", Arrays.asList(1.8, 2.5, 3.2, 2.1, 4.7, 5.9, 3.5));
            data.put("currentYield", 96.5);
            data.put("currentDefect", 3.5);
        }
        return data;
    }

    /**
     * 设备状态数据
     */
    private Map<String, Object> getEquipmentData() {
        Map<String, Object> data = new HashMap<>();

        List<EquipmentInfo> equipments = equipmentInfoService.list(
                new LambdaQueryWrapper<EquipmentInfo>().orderByAsc(EquipmentInfo::getEquipmentCode));

        List<Map<String, Object>> deviceList = equipments.stream().map(eq -> {
            Map<String, Object> device = new HashMap<>();
            device.put("name", eq.getEquipmentName());
            device.put("code", eq.getEquipmentCode());
            device.put("type", eq.getEquipmentType());
            device.put("status", eq.getStatus()); // 0=停用, 1=运行, 2=维修
            device.put("location", eq.getLocation());
            device.put("runningHours", eq.getRunningHours());
            return device;
        }).collect(Collectors.toList());

        // 统计
        long running = equipments.stream().filter(e -> Integer.valueOf(1).equals(e.getStatus())).count();
        long standby = equipments.stream().filter(e -> Integer.valueOf(0).equals(e.getStatus())).count();
        long fault = equipments.stream().filter(e -> Integer.valueOf(2).equals(e.getStatus())).count();

        data.put("devices", deviceList);
        data.put("total", equipments.size());
        data.put("running", running);
        data.put("standby", standby);
        data.put("fault", fault);
        return data;
    }

    /**
     * 在制分布数据
     */
    private Map<String, Object> getWipData() {
        Map<String, Object> data = new HashMap<>();

        // 查询生产中的工单（status=1）
        List<WorkOrder> inProductionOrders = workOrderService.list(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 1));

        // 查询所有工艺路线
        List<ProcessInfo> processes = processInfoService.list();

        // 按工序分组统计WIP
        Map<Long, Integer> wipMap = inProductionOrders.stream()
                .filter(o -> o.getProcessId() != null)
                .collect(Collectors.groupingBy(WorkOrder::getProcessId,
                        Collectors.summingInt(o -> o.getQuantity() != null ? o.getQuantity() : 0)));

        List<String> processNames = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (ProcessInfo process : processes) {
            processNames.add(process.getProcessName());
            counts.add(wipMap.getOrDefault(process.getId(), 0));
        }

        // 如果没有工艺数据，使用默认演示数据
        if (processNames.isEmpty()) {
            processNames = Arrays.asList("贴片", "焊接", "组装", "测试", "包装");
            counts = Arrays.asList(120, 85, 200, 65, 30);
        }

        // 如果WIP全为0，使用演示数据展示
        int wipTotal = counts.stream().mapToInt(Integer::intValue).sum();
        if (wipTotal == 0) {
            processNames = Arrays.asList("贴片", "焊接", "组装", "测试", "包装");
            counts = Arrays.asList(120, 85, 200, 65, 30);
        }

        data.put("processes", processNames);
        data.put("counts", counts);
        return data;
    }

    /**
     * TOP不良数据
     */
    private Map<String, Object> getDefectData() {
        Map<String, Object> data = new HashMap<>();

        // 查询所有不良结果（result=0表示不合格）
        List<QualityResult> defects = qualityResultService.list(
                new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getResult, 0));

        // 按检查项分组统计
        Map<String, Long> defectCount = defects.stream()
                .filter(r -> r.getCheckItem() != null)
                .collect(Collectors.groupingBy(QualityResult::getCheckItem,
                        Collectors.counting()));

        // 排序取TOP5
        List<Map<String, Object>> topDefects = defectCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        // 如果没有不良数据，使用默认演示数据
        if (topDefects.isEmpty()) {
            topDefects = new ArrayList<>();
            topDefects.add(createDefect("屏幕划痕", 28));
            topDefects.add(createDefect("心率模块焊接不良", 19));
            topDefects.add(createDefect("蓝牙连接异常", 15));
            topDefects.add(createDefect("外壳缝隙过大", 12));
            topDefects.add(createDefect("电池续航不足", 8));
        }

        List<String> names = topDefects.stream().map(d -> (String) d.get("name")).collect(Collectors.toList());
        List<Integer> counts = topDefects.stream().map(d -> ((Number) d.get("count")).intValue()).collect(Collectors.toList());

        data.put("names", names);
        data.put("counts", counts);
        data.put("items", topDefects);
        return data;
    }

    private Map<String, Object> createDefect(String name, int count) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("count", count);
        return item;
    }
}
