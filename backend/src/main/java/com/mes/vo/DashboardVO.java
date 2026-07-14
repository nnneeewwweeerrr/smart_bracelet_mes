package com.mes.vo;

import lombok.Data;

@Data
public class DashboardVO {

    private Integer todayProduction;

    private Integer processingWorkOrders;

    private Integer pendingQualityTasks;

    private Integer stockWarningCount;

    private String todayProductionTrend;
    private String processingWorkOrdersTrend;
    private String pendingQualityTasksTrend;
    private String stockWarningTrend;
}
