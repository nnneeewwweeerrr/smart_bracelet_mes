package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.common.Result;
import com.mes.entity.*;
import com.mes.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生产全流程联动接口
 * 展示工单从创建→生产→质检→入库的完整链路
 */
@RestController
@RequestMapping("/api/process-flow")
public class ProcessFlowController {

    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private ProductionReportService productionReportService;
    @Autowired
    private QualityResultService qualityResultService;
    @Autowired
    private QualityTaskService qualityTaskService;
    @Autowired
    private StockInService stockInService;
    @Autowired
    private ProductSnService productSnService;

    /**
     * 全流程概览：统计各状态工单数量
     * 返回：待生产/生产中/质检中/已完成 数量
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> result = new HashMap<>();

        // 查询全部工单
        List<WorkOrder> workOrders = workOrderService.list(
                new LambdaQueryWrapper<WorkOrder>().orderByDesc(WorkOrder::getCreateTime));

        int pending = 0;     // 待生产（status=0）
        int producing = 0;   // 生产中（status=1）
        int completed = 0;   // 已完成（status=2）

        if (workOrders != null) {
            for (WorkOrder wo : workOrders) {
                if (wo.getStatus() == null) continue;
                switch (wo.getStatus()) {
                    case 0: pending++; break;
                    case 1: producing++; break;
                    case 2: completed++; break;
                }
            }
        }

        // 质检中：关联工单且状态为待处理/处理中的质检任务数量
        List<Long> woIds = workOrders != null
                ? workOrders.stream().map(WorkOrder::getId).collect(Collectors.toList())
                : Collections.emptyList();
        long inspecting = 0;
        if (!woIds.isEmpty()) {
            List<QualityTask> tasks = qualityTaskService.list(
                    new LambdaQueryWrapper<QualityTask>()
                            .in(QualityTask::getSourceId, woIds)
                            .in(QualityTask::getStatus, 0, 1));
            inspecting = tasks != null ? tasks.size() : 0;
        }

        result.put("pending", pending);
        result.put("producing", producing);
        result.put("inspecting", inspecting);
        result.put("completed", completed);
        result.put("total", workOrders != null ? workOrders.size() : 0);
        result.put("workOrders", workOrders != null ? workOrders : Collections.emptyList());

        return Result.success(result);
    }

    /**
     * 工单全流程详情：返回工单信息、关联生产报工、质检记录、入库记录、SN列表
     */
    @GetMapping("/detail/{workOrderId}")
    public Result<Map<String, Object>> detail(@PathVariable Long workOrderId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 工单基本信息
        WorkOrder workOrder = workOrderService.getById(workOrderId);
        if (workOrder == null) {
            return Result.error("工单不存在");
        }
        result.put("workOrder", workOrder);

        // 2. 生产报工记录
        List<ProductionReport> reports = productionReportService.list(
                new LambdaQueryWrapper<ProductionReport>()
                        .eq(ProductionReport::getWorkOrderId, workOrderId)
                        .orderByDesc(ProductionReport::getReportDate));
        result.put("productionReports", reports != null ? reports : Collections.emptyList());

        // 3. 质检记录：通过质检任务关联（sourceId = workOrderId）
        List<QualityTask> qualityTasks = qualityTaskService.list(
                new LambdaQueryWrapper<QualityTask>()
                        .eq(QualityTask::getSourceId, workOrderId)
                        .orderByDesc(QualityTask::getCreateTime));
        List<Map<String, Object>> qualityList = new ArrayList<>();
        if (qualityTasks != null) {
            for (QualityTask task : qualityTasks) {
                List<QualityResult> qResults = qualityResultService.list(
                        new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getTaskId, task.getId()));
                Map<String, Object> item = new HashMap<>();
                item.put("task", task);
                item.put("results", qResults != null ? qResults : Collections.emptyList());
                qualityList.add(item);
            }
        }
        result.put("qualityRecords", qualityList);

        // 4. 入库记录：通过来源单号关联（sourceNo = workNo）
        List<StockIn> stockIns = stockInService.list(
                new LambdaQueryWrapper<StockIn>()
                        .eq(StockIn::getSourceNo, workOrder.getWorkNo())
                        .orderByDesc(StockIn::getCreateTime));
        result.put("stockIns", stockIns != null ? stockIns : Collections.emptyList());

        // 5. 产品SN列表
        List<ProductSn> sns = productSnService.list(
                new LambdaQueryWrapper<ProductSn>()
                        .eq(ProductSn::getWorkOrderId, workOrderId)
                        .orderByDesc(ProductSn::getCreateTime));
        result.put("productSns", sns != null ? sns : Collections.emptyList());

        return Result.success(result);
    }

    /**
     * 工单时间线：按时间排序的关键事件列表
     * 包含：工单创建、各工序报工、质检通过/不通过、入库完成、SN生成
     */
    @GetMapping("/timeline/{workOrderId}")
    public Result<List<Map<String, Object>>> timeline(@PathVariable Long workOrderId) {
        List<Map<String, Object>> events = new ArrayList<>();

        WorkOrder workOrder = workOrderService.getById(workOrderId);
        if (workOrder == null) {
            return Result.error("工单不存在");
        }

        // 事件1：工单创建
        if (workOrder.getCreateTime() != null) {
            Map<String, Object> e = new HashMap<>();
            e.put("time", workOrder.getCreateTime());
            e.put("type", "create");
            e.put("icon", "Document");
            e.put("title", "工单创建");
            e.put("description", "工单号 " + workOrder.getWorkNo() + " 已创建，计划数量 " + workOrder.getQuantity());
            events.add(e);
        }

        // 事件2：生产报工
        List<ProductionReport> reports = productionReportService.list(
                new LambdaQueryWrapper<ProductionReport>()
                        .eq(ProductionReport::getWorkOrderId, workOrderId)
                        .orderByAsc(ProductionReport::getReportDate));
        if (reports != null) {
            for (ProductionReport r : reports) {
                if (r.getReportDate() == null) continue;
                Map<String, Object> e = new HashMap<>();
                e.put("time", r.getReportDate().atStartOfDay());
                e.put("type", "report");
                e.put("icon", "Tools");
                int good = r.getGoodQuantity() != null ? r.getGoodQuantity() : 0;
                int bad = r.getBadQuantity() != null ? r.getBadQuantity() : 0;
                e.put("title", "工序报工");
                e.put("description", "合格 " + good + " 件，不合格 " + bad + " 件");
                events.add(e);
            }
        }

        // 事件3：质检任务及结果
        List<QualityTask> qualityTasks = qualityTaskService.list(
                new LambdaQueryWrapper<QualityTask>()
                        .eq(QualityTask::getSourceId, workOrderId)
                        .orderByAsc(QualityTask::getCreateTime));
        if (qualityTasks != null) {
            for (QualityTask task : qualityTasks) {
                List<QualityResult> qResults = qualityResultService.list(
                        new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getTaskId, task.getId()));
                if (task.getCreateTime() != null) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("time", task.getCreateTime());
                    e.put("type", "quality");
                    e.put("icon", "CircleCheck");
                    e.put("title", "质检任务创建");
                    e.put("description", "任务编号 " + task.getTaskNo() + "（" + getTaskTypeText(task.getTaskType()) + "）");
                    events.add(e);
                }
                if (qResults != null) {
                    for (QualityResult qr : qResults) {
                        boolean pass = qr.getResult() != null && qr.getResult() == 1;
                        Map<String, Object> e = new HashMap<>();
                        e.put("time", task.getCreateTime());
                        e.put("type", pass ? "qualityPass" : "qualityFail");
                        e.put("icon", pass ? "CircleCheckFilled" : "CircleCloseFilled");
                        e.put("title", pass ? "质检通过" : "质检不通过");
                        String desc = "检验项：" + (qr.getCheckItem() != null ? qr.getCheckItem() : "-");
                        if (qr.getActualValue() != null) {
                            desc += "，实际值：" + qr.getActualValue();
                        }
                        e.put("description", desc);
                        events.add(e);
                    }
                }
            }
        }

        // 事件4：产品SN生成
        List<ProductSn> sns = productSnService.list(
                new LambdaQueryWrapper<ProductSn>()
                        .eq(ProductSn::getWorkOrderId, workOrderId)
                        .orderByAsc(ProductSn::getCreateTime));
        if (sns != null) {
            for (ProductSn sn : sns) {
                if (sn.getCreateTime() == null) continue;
                Map<String, Object> e = new HashMap<>();
                e.put("time", sn.getCreateTime());
                e.put("type", "sn");
                e.put("icon", "Key");
                e.put("title", "产品SN生成");
                e.put("description", "SN码：" + sn.getSnCode());
                events.add(e);
            }
        }

        // 事件5：入库完成
        List<StockIn> stockIns = stockInService.list(
                new LambdaQueryWrapper<StockIn>()
                        .eq(StockIn::getSourceNo, workOrder.getWorkNo())
                        .orderByAsc(StockIn::getCreateTime));
        if (stockIns != null) {
            for (StockIn si : stockIns) {
                if (si.getCreateTime() == null) continue;
                Map<String, Object> e = new HashMap<>();
                e.put("time", si.getCreateTime());
                e.put("type", "stockIn");
                e.put("icon", "Bottom");
                e.put("title", "入库完成");
                e.put("description", "入库单号 " + si.getInNo() + "，数量 " + si.getQuantity());
                events.add(e);
            }
        }

        // 按时间升序排序
        events.sort(Comparator.comparing(m -> (LocalDateTime) m.get("time"),
                Comparator.nullsLast(Comparator.naturalOrder())));

        return Result.success(events);
    }

    private String getTaskTypeText(String taskType) {
        if (taskType == null) return "质检";
        switch (taskType) {
            case "IQC": return "来料检验";
            case "IPQC": return "过程检验";
            case "OQC": return "成品检验";
            default: return taskType;
        }
    }
}
