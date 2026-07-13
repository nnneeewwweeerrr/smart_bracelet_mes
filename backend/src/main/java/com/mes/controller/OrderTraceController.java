package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.common.Result;
import com.mes.entity.*;
import com.mes.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单全链路追溯接口
 */
@RestController
@RequestMapping("/api/trace")
public class OrderTraceController {

    @Autowired
    private OrderCustomerService orderCustomerService;
    @Autowired
    private CustomerInfoService customerInfoService;
    @Autowired
    private ProductInfoService productInfoService;
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    @Autowired
    private SupplierInfoService supplierInfoService;
    @Autowired
    private MaterialInfoService materialInfoService;
    @Autowired
    private QualityTaskService qualityTaskService;
    @Autowired
    private QualityResultService qualityResultService;
    @Autowired
    private StockInService stockInService;
    @Autowired
    private StockOutService stockOutService;
    @Autowired
    private StockInfoService stockInfoService;
    @Autowired
    private ProductionPlanService productionPlanService;
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private ProductionReportService productionReportService;
    @Autowired
    private ProcessInfoService processInfoService;
    @Autowired
    private ExceptionRecordService exceptionRecordService;
    @Autowired
    private WarehouseInfoService warehouseInfoService;

    /**
     * 订单全链路追溯查询
     * @param orderNo 订单号
     * @return 完整链路节点数据
     */
    @GetMapping("/order")
    public Result<Map<String, Object>> traceOrder(@RequestParam String orderNo) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询订单基本信息
        OrderCustomer order = orderCustomerService.getOne(
                new LambdaQueryWrapper<OrderCustomer>().eq(OrderCustomer::getOrderNo, orderNo));
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 查询客户和产品信息
        CustomerInfo customer = customerInfoService.getById(order.getCustomerId());
        ProductInfo product = productInfoService.getById(order.getProductId());

        // 订单基本信息
        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("orderNo", order.getOrderNo());
        orderInfo.put("quantity", order.getQuantity());
        orderInfo.put("unitPrice", order.getUnitPrice());
        orderInfo.put("totalAmount", order.getTotalAmount());
        orderInfo.put("status", order.getStatus());
        orderInfo.put("statusText", getOrderStatusText(order.getStatus()));
        orderInfo.put("deliveryDate", order.getDeliveryDate());
        orderInfo.put("createTime", order.getCreateTime());
        orderInfo.put("customerName", customer != null ? customer.getCustomerName() : "");
        orderInfo.put("productName", product != null ? product.getProductName() : "");
        orderInfo.put("productSpec", product != null ? product.getSpecification() : "");
        result.put("orderInfo", orderInfo);

        // 2. 构建链路节点
        List<Map<String, Object>> nodes = new ArrayList<>();

        // 节点1：订单录入
        nodes.add(buildOrderNode(order));

        // 节点2：采购下单
        List<PurchaseOrder> purchases = purchaseOrderService.list(
                new LambdaQueryWrapper<PurchaseOrder>().orderByDesc(PurchaseOrder::getCreateTime));
        nodes.add(buildPurchaseNode(purchases, order));

        // 节点3：来料检验(IQC)
        List<QualityTask> iqcTasks = qualityTaskService.list(
                new LambdaQueryWrapper<QualityTask>()
                        .eq(QualityTask::getTaskType, "IQC")
                        .orderByDesc(QualityTask::getCreateTime));
        nodes.add(buildIqcNode(iqcTasks));

        // 节点4：物料入库
        List<StockIn> stockIns = stockInService.list(
                new LambdaQueryWrapper<StockIn>().orderByDesc(StockIn::getCreateTime));
        nodes.add(buildStockInNode(stockIns));

        // 节点5：生产执行
        List<ProductionPlan> plans = productionPlanService.list(
                new LambdaQueryWrapper<ProductionPlan>()
                        .eq(order.getId() != null, ProductionPlan::getOrderId, order.getId())
                        .orderByDesc(ProductionPlan::getCreateTime));
        List<WorkOrder> workOrders = new ArrayList<>();
        List<ProductionReport> reports = new ArrayList<>();
        if (!plans.isEmpty()) {
            List<Long> planIds = plans.stream().map(ProductionPlan::getId).collect(Collectors.toList());
            workOrders = workOrderService.list(
                    new LambdaQueryWrapper<WorkOrder>().in(WorkOrder::getPlanId, planIds));
            if (!workOrders.isEmpty()) {
                List<Long> woIds = workOrders.stream().map(WorkOrder::getId).collect(Collectors.toList());
                reports = productionReportService.list(
                        new LambdaQueryWrapper<ProductionReport>().in(ProductionReport::getWorkOrderId, woIds));
            }
        }
        nodes.add(buildProductionNode(plans, workOrders, reports, order));

        // 节点6：成品质检(OQC)
        List<QualityTask> oqcTasks = qualityTaskService.list(
                new LambdaQueryWrapper<QualityTask>()
                        .eq(QualityTask::getTaskType, "OQC")
                        .orderByDesc(QualityTask::getCreateTime));
        nodes.add(buildOqcNode(oqcTasks));

        // 节点7：成品入库
        List<StockIn> finishedStockIns = stockInService.list(
                new LambdaQueryWrapper<StockIn>()
                        .eq(StockIn::getSourceType, "生产")
                        .orderByDesc(StockIn::getCreateTime));
        nodes.add(buildFinishedStockNode(finishedStockIns, order));

        // 查询异常记录
        List<ExceptionRecord> exceptions = exceptionRecordService.list(
                new LambdaQueryWrapper<ExceptionRecord>().orderByDesc(ExceptionRecord::getReportTime));
        result.put("exceptions", exceptions);

        result.put("nodes", nodes);
        result.put("totalNodes", nodes.size());

        // 计算整体进度
        int completedNodes = (int) nodes.stream().filter(n -> "completed".equals(n.get("status"))).count();
        result.put("overallProgress", Math.round(completedNodes * 100.0 / nodes.size()));

        return Result.success(result);
    }

    /**
     * 构建订单录入节点
     */
    private Map<String, Object> buildOrderNode(OrderCustomer order) {
        Map<String, Object> node = new HashMap<>();
        node.put("index", 1);
        node.put("name", "订单录入");
        node.put("nameEn", "ORDER ENTRY");
        node.put("status", "completed");
        node.put("time", order.getCreateTime());
        node.put("icon", "document");

        Map<String, Object> detail = new HashMap<>();
        detail.put("orderNo", order.getOrderNo());
        detail.put("quantity", order.getQuantity());
        detail.put("totalAmount", order.getTotalAmount());
        detail.put("status", getOrderStatusText(order.getStatus()));
        node.put("detail", detail);

        return node;
    }

    /**
     * 构建采购下单节点
     */
    private Map<String, Object> buildPurchaseNode(List<PurchaseOrder> purchases, OrderCustomer order) {
        Map<String, Object> node = new HashMap<>();
        node.put("index", 2);
        node.put("name", "采购下单");
        node.put("nameEn", "PURCHASE ORDER");

        boolean hasPurchase = !purchases.isEmpty();
        boolean hasArrived = purchases.stream().anyMatch(p -> p.getStatus() != null && p.getStatus() >= 2);

        node.put("status", hasArrived ? "completed" : (hasPurchase ? "inProgress" : "pending"));
        node.put("time", hasPurchase ? purchases.get(0).getCreateTime() : null);
        node.put("icon", "shopping-cart");

        Map<String, Object> detail = new HashMap<>();
        detail.put("purchaseCount", purchases.size());
        detail.put("totalQuantity", purchases.stream().mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0).sum());

        List<Map<String, Object>> purchaseList = new ArrayList<>();
        for (PurchaseOrder po : purchases) {
            Map<String, Object> item = new HashMap<>();
            item.put("purchaseNo", po.getPurchaseNo());
            MaterialInfo mat = materialInfoService.getById(po.getMaterialId());
            SupplierInfo sup = supplierInfoService.getById(po.getSupplierId());
            item.put("materialName", mat != null ? mat.getMaterialName() : "");
            item.put("supplierName", sup != null ? sup.getSupplierName() : "");
            item.put("quantity", po.getQuantity());
            item.put("status", getPurchaseStatusText(po.getStatus()));
            item.put("createTime", po.getCreateTime());
            purchaseList.add(item);
        }
        detail.put("purchaseList", purchaseList);
        node.put("detail", detail);

        return node;
    }

    /**
     * 构建来料检验节点
     */
    private Map<String, Object> buildIqcNode(List<QualityTask> tasks) {
        Map<String, Object> node = new HashMap<>();
        node.put("index", 3);
        node.put("name", "来料检验");
        node.put("nameEn", "IQC INSPECTION");

        boolean hasTask = !tasks.isEmpty();
        boolean allCompleted = tasks.stream().allMatch(t -> t.getStatus() != null && t.getStatus() == 2);

        node.put("status", allCompleted ? "completed" : (hasTask ? "inProgress" : "pending"));
        node.put("time", hasTask ? tasks.get(0).getCreateTime() : null);
        node.put("icon", "check-circle");

        Map<String, Object> detail = new HashMap<>();
        detail.put("taskCount", tasks.size());

        long passCount = 0;
        long failCount = 0;
        for (QualityTask task : tasks) {
            List<QualityResult> results = qualityResultService.list(
                    new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getTaskId, task.getId()));
            passCount += results.stream().filter(r -> r.getResult() != null && r.getResult() == 1).count();
            failCount += results.stream().filter(r -> r.getResult() != null && r.getResult() == 0).count();
        }
        detail.put("passCount", passCount);
        detail.put("failCount", failCount);
        detail.put("passRate", (passCount + failCount) > 0 ? Math.round(passCount * 100.0 / (passCount + failCount)) : 100);

        List<Map<String, Object>> taskList = new ArrayList<>();
        for (QualityTask task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskNo", task.getTaskNo());
            item.put("status", getQualityStatusText(task.getStatus()));
            item.put("createTime", task.getCreateTime());
            taskList.add(item);
        }
        detail.put("taskList", taskList);
        node.put("detail", detail);

        return node;
    }

    /**
     * 构建物料入库节点
     */
    private Map<String, Object> buildStockInNode(List<StockIn> stockIns) {
        Map<String, Object> node = new HashMap<>();
        node.put("index", 4);
        node.put("name", "物料入库");
        node.put("nameEn", "MATERIAL STOCK-IN");

        boolean hasStockIn = !stockIns.isEmpty();
        node.put("status", hasStockIn ? "completed" : "pending");
        node.put("time", hasStockIn ? stockIns.get(0).getCreateTime() : null);
        node.put("icon", "bottom");

        Map<String, Object> detail = new HashMap<>();
        detail.put("stockInCount", stockIns.size());
        detail.put("totalQuantity", stockIns.stream().mapToInt(s -> s.getQuantity() != null ? s.getQuantity().intValue() : 0).sum());

        List<Map<String, Object>> stockInList = new ArrayList<>();
        for (StockIn si : stockIns) {
            Map<String, Object> item = new HashMap<>();
            item.put("inNo", si.getInNo());
            MaterialInfo mat = materialInfoService.getById(si.getMaterialId());
            WarehouseInfo wh = warehouseInfoService.getById(si.getWarehouseId());
            item.put("materialName", mat != null ? mat.getMaterialName() : "");
            item.put("warehouseName", wh != null ? wh.getWarehouseName() : "");
            item.put("quantity", si.getQuantity());
            item.put("sourceType", si.getSourceType());
            item.put("createTime", si.getCreateTime());
            stockInList.add(item);
        }
        detail.put("stockInList", stockInList);
        node.put("detail", detail);

        return node;
    }

    /**
     * 构建生产执行节点
     */
    private Map<String, Object> buildProductionNode(List<ProductionPlan> plans,
                                                    List<WorkOrder> workOrders,
                                                    List<ProductionReport> reports,
                                                    OrderCustomer order) {
        Map<String, Object> node = new HashMap<>();
        node.put("index", 5);
        node.put("name", "生产执行");
        node.put("nameEn", "PRODUCTION EXECUTION");

        boolean hasPlan = !plans.isEmpty();
        boolean hasWorkOrder = !workOrders.isEmpty();
        boolean allCompleted = workOrders.stream().allMatch(w -> w.getStatus() != null && w.getStatus() == 2);

        String status = "pending";
        if (allCompleted) {
            status = "completed";
        } else if (hasWorkOrder) {
            status = "inProgress";
        } else if (hasPlan) {
            status = "inProgress";
        }

        // 检查是否有异常
        List<ExceptionRecord> prodExceptions = exceptionRecordService.list(
                new LambdaQueryWrapper<ExceptionRecord>()
                        .in(!workOrders.isEmpty(), ExceptionRecord::getWorkOrderId,
                                workOrders.stream().map(WorkOrder::getId).collect(Collectors.toList()))
                        .eq(ExceptionRecord::getStatus, 0));
        if (!prodExceptions.isEmpty() && !"pending".equals(status)) {
            status = "abnormal";
        }

        node.put("status", status);
        node.put("time", hasPlan ? plans.get(0).getCreateTime() : null);
        node.put("icon", "tickets");

        // 计算生产进度
        int totalQty = order.getQuantity() != null ? order.getQuantity() : 0;
        int completedQty = reports.stream().mapToInt(r -> r.getGoodQuantity() != null ? r.getGoodQuantity() : 0).sum();
        int progress = totalQty > 0 ? Math.min(100, Math.round(completedQty * 100.0f / totalQty)) : 0;

        Map<String, Object> detail = new HashMap<>();
        detail.put("planCount", plans.size());
        detail.put("workOrderCount", workOrders.size());
        detail.put("reportCount", reports.size());
        detail.put("completedQty", completedQty);
        detail.put("totalQty", totalQty);
        detail.put("progress", progress);

        // 统计各工序进度
        List<Map<String, Object>> processProgress = new ArrayList<>();
        Map<Long, String> processMap = new HashMap<>();
        List<ProcessInfo> processes = processInfoService.list();
        for (ProcessInfo p : processes) {
            processMap.put(p.getId(), p.getProcessName());
        }

        Map<Long, Integer> processQtyMap = new HashMap<>();
        Map<Long, Integer> processBadMap = new HashMap<>();
        for (ProductionReport report : reports) {
            if (report.getProcessId() != null) {
                processQtyMap.merge(report.getProcessId(), report.getGoodQuantity() != null ? report.getGoodQuantity() : 0, Integer::sum);
                processBadMap.merge(report.getProcessId(), report.getBadQuantity() != null ? report.getBadQuantity() : 0, Integer::sum);
            }
        }

        for (ProcessInfo p : processes) {
            Map<String, Object> item = new HashMap<>();
            item.put("processName", p.getProcessName());
            int qty = processQtyMap.getOrDefault(p.getId(), 0);
            int bad = processBadMap.getOrDefault(p.getId(), 0);
            item.put("goodQty", qty);
            item.put("badQty", bad);
            item.put("progress", totalQty > 0 ? Math.min(100, Math.round(qty * 100.0f / totalQty)) : 0);
            processProgress.add(item);
        }
        detail.put("processProgress", processProgress);

        // 工单列表
        List<Map<String, Object>> woList = new ArrayList<>();
        for (WorkOrder wo : workOrders) {
            Map<String, Object> item = new HashMap<>();
            item.put("workNo", wo.getWorkNo());
            String procName = processMap.getOrDefault(wo.getProcessId(), "");
            item.put("processName", procName);
            item.put("quantity", wo.getQuantity());
            item.put("status", getWorkOrderStatusText(wo.getStatus()));
            woList.add(item);
        }
        detail.put("workOrderList", woList);

        node.put("detail", detail);
        return node;
    }

    /**
     * 构建成品质检节点
     */
    private Map<String, Object> buildOqcNode(List<QualityTask> tasks) {
        Map<String, Object> node = new HashMap<>();
        node.put("index", 6);
        node.put("name", "成品质检");
        node.put("nameEn", "OQC INSPECTION");

        boolean hasTask = !tasks.isEmpty();
        boolean allCompleted = tasks.stream().allMatch(t -> t.getStatus() != null && t.getStatus() == 2);

        node.put("status", allCompleted ? "completed" : (hasTask ? "inProgress" : "pending"));
        node.put("time", hasTask ? tasks.get(0).getCreateTime() : null);
        node.put("icon", "quality");

        Map<String, Object> detail = new HashMap<>();
        detail.put("taskCount", tasks.size());

        long passCount = 0;
        long failCount = 0;
        for (QualityTask task : tasks) {
            List<QualityResult> results = qualityResultService.list(
                    new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getTaskId, task.getId()));
            passCount += results.stream().filter(r -> r.getResult() != null && r.getResult() == 1).count();
            failCount += results.stream().filter(r -> r.getResult() != null && r.getResult() == 0).count();
        }
        detail.put("passCount", passCount);
        detail.put("failCount", failCount);
        detail.put("passRate", (passCount + failCount) > 0 ? Math.round(passCount * 100.0 / (passCount + failCount)) : 100);

        List<Map<String, Object>> taskList = new ArrayList<>();
        for (QualityTask task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskNo", task.getTaskNo());
            item.put("status", getQualityStatusText(task.getStatus()));
            item.put("createTime", task.getCreateTime());

            List<QualityResult> results = qualityResultService.list(
                    new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getTaskId, task.getId()));
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (QualityResult r : results) {
                Map<String, Object> ri = new HashMap<>();
                ri.put("checkItem", r.getCheckItem());
                ri.put("standardValue", r.getStandardValue());
                ri.put("actualValue", r.getActualValue());
                ri.put("result", r.getResult() != null && r.getResult() == 1 ? "合格" : "不合格");
                resultList.add(ri);
            }
            item.put("results", resultList);
            taskList.add(item);
        }
        detail.put("taskList", taskList);
        node.put("detail", detail);

        return node;
    }

    /**
     * 构建成品入库节点
     */
    private Map<String, Object> buildFinishedStockNode(List<StockIn> stockIns, OrderCustomer order) {
        Map<String, Object> node = new HashMap<>();
        node.put("index", 7);
        node.put("name", "成品入库");
        node.put("nameEn", "FINISHED STOCK-IN");

        boolean hasStockIn = !stockIns.isEmpty();
        node.put("status", hasStockIn ? "completed" : "pending");
        node.put("time", hasStockIn ? stockIns.get(0).getCreateTime() : null);
        node.put("icon", "warehouse");

        int totalInQty = stockIns.stream().mapToInt(s -> s.getQuantity() != null ? s.getQuantity().intValue() : 0).sum();
        int orderQty = order.getQuantity() != null ? order.getQuantity() : 0;
        int progress = orderQty > 0 ? Math.min(100, Math.round(totalInQty * 100.0f / orderQty)) : 0;

        Map<String, Object> detail = new HashMap<>();
        detail.put("stockInCount", stockIns.size());
        detail.put("totalInQty", totalInQty);
        detail.put("orderQty", orderQty);
        detail.put("progress", progress);

        List<Map<String, Object>> stockInList = new ArrayList<>();
        for (StockIn si : stockIns) {
            Map<String, Object> item = new HashMap<>();
            item.put("inNo", si.getInNo());
            MaterialInfo mat = materialInfoService.getById(si.getMaterialId());
            WarehouseInfo wh = warehouseInfoService.getById(si.getWarehouseId());
            item.put("materialName", mat != null ? mat.getMaterialName() : "成品");
            item.put("warehouseName", wh != null ? wh.getWarehouseName() : "");
            item.put("quantity", si.getQuantity());
            item.put("createTime", si.getCreateTime());
            stockInList.add(item);
        }
        detail.put("stockInList", stockInList);
        node.put("detail", detail);

        return node;
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已审批";
            case 2: return "生产中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 获取采购状态文本
     */
    private String getPurchaseStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已审批";
            case 2: return "已到货";
            case 3: return "已完成";
            default: return "未知";
        }
    }

    /**
     * 获取质检状态文本
     */
    private String getQualityStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "已完成";
            default: return "未知";
        }
    }

    /**
     * 获取工单状态文本
     */
    private String getWorkOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待派发";
            case 1: return "执行中";
            case 2: return "已完成";
            default: return "未知";
        }
    }

    /**
     * 获取订单列表（用于搜索建议）
     */
    @GetMapping("/order-list")
    public Result<List<Map<String, Object>>> getOrderList(@RequestParam(required = false) String keyword) {
        List<OrderCustomer> orders;
        if (keyword != null && !keyword.isEmpty()) {
            orders = orderCustomerService.list(
                    new LambdaQueryWrapper<OrderCustomer>()
                            .like(OrderCustomer::getOrderNo, keyword)
                            .orderByDesc(OrderCustomer::getCreateTime)
                            .last("LIMIT 20"));
        } else {
            orders = orderCustomerService.list(
                    new LambdaQueryWrapper<OrderCustomer>()
                            .orderByDesc(OrderCustomer::getCreateTime)
                            .last("LIMIT 20"));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderCustomer order : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("orderNo", order.getOrderNo());
            ProductInfo product = productInfoService.getById(order.getProductId());
            item.put("productName", product != null ? product.getProductName() : "");
            item.put("quantity", order.getQuantity());
            item.put("status", order.getStatus());
            item.put("statusText", getOrderStatusText(order.getStatus()));
            result.add(item);
        }
        return Result.success(result);
    }
}
