package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.WorkOrder;

import java.util.List;

public interface WorkOrderService extends IService<WorkOrder> {

    IPage<WorkOrder> pageList(Page<WorkOrder> page, String workNo, Integer status, Long productId, Long planId);

    boolean dispatchWorkOrder(Long id);

    boolean completeWorkOrder(Long id);

    List<WorkOrder> generateFromPlan(Long planId);
}
