package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.ProductionPlan;

public interface ProductionPlanService extends IService<ProductionPlan> {

    IPage<ProductionPlan> pageList(Page<ProductionPlan> page, String planNo, Integer status, Long orderId);

    boolean startPlan(Long id);

    boolean finishPlan(Long id);
}
