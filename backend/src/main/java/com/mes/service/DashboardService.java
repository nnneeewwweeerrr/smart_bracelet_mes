package com.mes.service;

import com.mes.vo.*;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardVO getOverview();

    List<Map<String, Object>> getTodoList();

    List<TrendDataVO> getProductionTrend();

    List<StatusCountVO> getWorkOrderStatusCount();

    List<PieDataVO> getProductPieData();

    QualityStatsVO getQualityStats();
}
