package com.mes.service.impl;

import com.mes.mapper.DashboardMapper;
import com.mes.service.DashboardService;
import com.mes.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardVO getOverview() {
        DashboardVO vo = new DashboardVO();
        vo.setTodayProduction(dashboardMapper.getTodayProduction() != null ? dashboardMapper.getTodayProduction() : 0);
        vo.setProcessingWorkOrders(dashboardMapper.getProcessingWorkOrders() != null ? dashboardMapper.getProcessingWorkOrders() : 0);
        vo.setPendingQualityTasks(dashboardMapper.getPendingQualityTasks() != null ? dashboardMapper.getPendingQualityTasks() : 0);
        vo.setStockWarningCount(dashboardMapper.getStockWarningCount() != null ? dashboardMapper.getStockWarningCount() : 0);
        vo.setTodayProductionTrend("+12.5%");
        vo.setProcessingWorkOrdersTrend("+2");
        vo.setPendingQualityTasksTrend("-15%");
        vo.setStockWarningTrend("0%");
        return vo;
    }

    @Override
    public List<Map<String, Object>> getTodoList() {
        List<Map<String, Object>> result = new ArrayList<>();

        addTodoItems(result, dashboardMapper.getPendingOrders());
        addTodoItems(result, dashboardMapper.getPendingPurchaseOrders());
        addTodoItems(result, dashboardMapper.getRecentQualityTasks());
        addTodoItems(result, dashboardMapper.getPendingMaintainTasks());
        addTodoItems(result, dashboardMapper.getRecentCompletedWorkOrders());

        result.sort((a, b) -> {
            String timeA = (String) a.get("time");
            String timeB = (String) b.get("time");
            return timeB.compareTo(timeA);
        });

        return result.stream().limit(10).collect(Collectors.toList());
    }

    private void addTodoItems(List<Map<String, Object>> result, List<TodoItemVO> items) {
        if (items == null || items.isEmpty()) return;
        for (TodoItemVO item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", item.getTitle());
            map.put("status", translateStatus(item.getStatus()));
            map.put("time", formatTimeAgo(item.getCreateTime()));
            map.put("type", translateType(item.getType()));
            result.add(map);
        }
    }

    private String translateType(String type) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("order_approval", "订单审批");
        typeMap.put("purchase_arrival", "采购到货");
        typeMap.put("quality_task", "质检任务");
        typeMap.put("equipment_maintain", "设备维护");
        typeMap.put("work_order_complete", "工单完成");
        return typeMap.getOrDefault(type, type);
    }

    private String translateStatus(String status) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("pending", "待处理");
        statusMap.put("completed", "已完成");
        return statusMap.getOrDefault(status, status);
    }

    private String formatTimeAgo(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return "未知时间";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime createTime = LocalDateTime.parse(dateTimeStr, formatter);
            LocalDateTime now = LocalDateTime.now();
            
            long minutes = java.time.Duration.between(createTime, now).toMinutes();
            if (minutes < 60) {
                return minutes + "分钟前";
            }
            long hours = minutes / 60;
            if (hours < 24) {
                return hours + "小时前";
            }
            long days = hours / 24;
            return days + "天前";
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    @Override
    public List<TrendDataVO> getProductionTrend() {
        return dashboardMapper.getProductionTrend();
    }

    @Override
    public List<StatusCountVO> getWorkOrderStatusCount() {
        return dashboardMapper.getWorkOrderStatusCount();
    }

    @Override
    public List<PieDataVO> getProductPieData() {
        return dashboardMapper.getProductPieData();
    }

    @Override
    public QualityStatsVO getQualityStats() {
        return dashboardMapper.getQualityStats();
    }
}
