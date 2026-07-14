package com.mes.mapper;

import com.mes.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

    Integer getTodayProduction();

    Integer getProcessingWorkOrders();

    Integer getPendingQualityTasks();

    Integer getStockWarningCount();

    List<TodoItemVO> getPendingOrders();

    List<TodoItemVO> getPendingPurchaseOrders();

    List<TodoItemVO> getRecentQualityTasks();

    List<TodoItemVO> getPendingMaintainTasks();

    List<TodoItemVO> getRecentCompletedWorkOrders();

    List<TrendDataVO> getProductionTrend();

    List<StatusCountVO> getWorkOrderStatusCount();

    List<PieDataVO> getProductPieData();

    QualityStatsVO getQualityStats();
}
