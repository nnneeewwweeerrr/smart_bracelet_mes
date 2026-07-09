package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.QualityTask;

public interface QualityTaskService extends IService<QualityTask> {

    boolean startTask(Long id);

    boolean completeTask(Long id);

    QualityTask createIqcTask(Long stockInId, Long inspectorId);

    QualityTask createIpqcTask(Long workOrderId, Long inspectorId);

    QualityTask createOqcTask(Long orderId, Long inspectorId);
}
