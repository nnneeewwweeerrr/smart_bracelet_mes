package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.ProductionReport;

public interface ProductionReportService extends IService<ProductionReport> {

    IPage<ProductionReport> pageList(Page<ProductionReport> page, Long workOrderId, Long workerId);

    boolean saveReport(ProductionReport report);

    /**
     * 带双重防错校验的报工保存
     * @param report 报工数据
     * @param materialCode 扫码物料编码（null则跳过物料校验）
     * @param processParams 实际工艺参数（null则跳过参数校验）
     * @return 校验/保存失败原因，null表示成功
     */
    String saveReportWithValidation(ProductionReport report, String materialCode, java.util.Map<String, Object> processParams);
}
