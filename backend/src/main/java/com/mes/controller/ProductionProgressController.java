package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.common.Result;
import com.mes.entity.*;
import com.mes.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/production-progress")
public class ProductionProgressController {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProductionReportService productionReportService;

    @Autowired
    private OrderCustomerService orderCustomerService;

    @Autowired
    private ProcessInfoService processInfoService;

    @Autowired
    private ProductionPlanService productionPlanService;

    @GetMapping("/list")
    public Result<List<ProductionProgressVO>> list() {
        List<WorkOrder> workOrders = workOrderService.list();
        List<ProductionProgressVO> result = new ArrayList<>();

        for (WorkOrder wo : workOrders) {
            ProductionProgressVO vo = new ProductionProgressVO();
            vo.setId(wo.getId());
            vo.setWorkOrderNo(wo.getWorkNo());
            vo.setQuantity(wo.getQuantity());
            vo.setStatus(wo.getStatus());

            ProductInfo product = productInfoService.getById(wo.getProductId());
            if (product != null) {
                vo.setProductName(product.getProductName());
                vo.setSpec(product.getSpecification());
            }

            int finishedQty = calculateFinishedQuantity(wo.getId());
            vo.setFinishedQuantity(finishedQty);

            LambdaQueryWrapper<ProductionReport> reportWrapper = new LambdaQueryWrapper<>();
            reportWrapper.eq(ProductionReport::getWorkOrderId, wo.getId());
            List<ProductionReport> reports = productionReportService.list(reportWrapper);
            
            List<ProcessProgress> processList = new ArrayList<>();
            if (!reports.isEmpty()) {
                for (ProductionReport report : reports) {
                    ProcessProgress pp = new ProcessProgress();
                    ProcessInfo process = processInfoService.getById(report.getProcessId());
                    if (process != null) {
                        pp.setProcessName(process.getProcessName());
                    }
                    pp.setFinishedQuantity(report.getGoodQuantity() != null ? report.getGoodQuantity() : 0);
                    pp.setQuantity(wo.getQuantity());
                    pp.setStatus("finished");
                    processList.add(pp);
                }
            }
            
            if (processList.isEmpty()) {
                ProcessProgress pp = new ProcessProgress();
                pp.setProcessName("待开始");
                pp.setFinishedQuantity(0);
                pp.setQuantity(wo.getQuantity());
                pp.setStatus("pending");
                processList.add(pp);
            }
            vo.setProcessList(processList);

            ProductionPlan plan = productionPlanService.getById(wo.getPlanId());
            if (plan != null) {
                vo.setPlanStartDate(plan.getStartDate() != null ? plan.getStartDate().toString().split("T")[0] : "");
                vo.setPlanEndDate(plan.getEndDate() != null ? plan.getEndDate().toString().split("T")[0] : "");
                OrderCustomer order = orderCustomerService.getById(plan.getOrderId());
                if (order != null) {
                    vo.setOrderNo(order.getOrderNo());
                }
            }

            vo.setManager("管理员");
            vo.setCreateTime(wo.getCreateTime() != null ? wo.getCreateTime().toString().replace("T", " ") : "");

            result.add(vo);
        }

        return Result.success(result);
    }

    private int calculateFinishedQuantity(Long workOrderId) {
        LambdaQueryWrapper<ProductionReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductionReport::getWorkOrderId, workOrderId);
        List<ProductionReport> reports = productionReportService.list(wrapper);
        
        int total = 0;
        for (ProductionReport report : reports) {
            if (report.getGoodQuantity() != null) {
                total += report.getGoodQuantity();
            }
            if (report.getBadQuantity() != null) {
                total += report.getBadQuantity();
            }
        }
        return total;
    }

    public static class ProductionProgressVO {
        private Long id;
        private String workOrderNo;
        private String orderNo;
        private String productName;
        private String spec;
        private Integer quantity;
        private Integer finishedQuantity;
        private Integer status;
        private String planStartDate;
        private String planEndDate;
        private String manager;
        private String createTime;
        private List<ProcessProgress> processList;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getWorkOrderNo() { return workOrderNo; }
        public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSpec() { return spec; }
        public void setSpec(String spec) { this.spec = spec; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getFinishedQuantity() { return finishedQuantity; }
        public void setFinishedQuantity(Integer finishedQuantity) { this.finishedQuantity = finishedQuantity; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getPlanStartDate() { return planStartDate; }
        public void setPlanStartDate(String planStartDate) { this.planStartDate = planStartDate; }
        public String getPlanEndDate() { return planEndDate; }
        public void setPlanEndDate(String planEndDate) { this.planEndDate = planEndDate; }
        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
        public List<ProcessProgress> getProcessList() { return processList; }
        public void setProcessList(List<ProcessProgress> processList) { this.processList = processList; }
    }

    public static class ProcessProgress {
        private String processName;
        private Integer finishedQuantity;
        private Integer quantity;
        private String status;

        public String getProcessName() { return processName; }
        public void setProcessName(String processName) { this.processName = processName; }
        public Integer getFinishedQuantity() { return finishedQuantity; }
        public void setFinishedQuantity(Integer finishedQuantity) { this.finishedQuantity = finishedQuantity; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}