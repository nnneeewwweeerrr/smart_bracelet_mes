package com.mes.controller;

import com.mes.common.Result;
import com.mes.service.DashboardService;
import com.mes.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardVO> getOverview() {
        return Result.success(dashboardService.getOverview());
    }

    @GetMapping("/todo-list")
    public Result<List<Map<String, Object>>> getTodoList() {
        return Result.success(dashboardService.getTodoList());
    }

    @GetMapping("/trend-data")
    public Result<List<TrendDataVO>> getProductionTrend() {
        return Result.success(dashboardService.getProductionTrend());
    }

    @GetMapping("/status-count")
    public Result<List<StatusCountVO>> getWorkOrderStatusCount() {
        return Result.success(dashboardService.getWorkOrderStatusCount());
    }

    @GetMapping("/pie-data")
    public Result<List<PieDataVO>> getProductPieData() {
        return Result.success(dashboardService.getProductPieData());
    }

    @GetMapping("/quality-stats")
    public Result<QualityStatsVO> getQualityStats() {
        return Result.success(dashboardService.getQualityStats());
    }
}
