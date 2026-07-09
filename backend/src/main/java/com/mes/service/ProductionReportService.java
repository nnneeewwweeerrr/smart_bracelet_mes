package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.ProductionReport;

public interface ProductionReportService extends IService<ProductionReport> {

    IPage<ProductionReport> pageList(Page<ProductionReport> page, Long workOrderId, Long workerId);

    boolean saveReport(ProductionReport report);
}
