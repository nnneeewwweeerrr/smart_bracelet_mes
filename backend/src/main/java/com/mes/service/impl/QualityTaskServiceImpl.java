package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.QualityTask;
import com.mes.exception.BusinessException;
import com.mes.mapper.QualityTaskMapper;
import com.mes.service.QualityTaskService;
import com.mes.utils.BarcodeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QualityTaskServiceImpl extends ServiceImpl<QualityTaskMapper, QualityTask> implements QualityTaskService {

    @Override
    public boolean save(QualityTask qualityTask) {
        qualityTask.setTaskNo(BarcodeUtil.generateQualityTaskNo());
        if (qualityTask.getStatus() == null) {
            qualityTask.setStatus(0);
        }
        if (qualityTask.getCreateTime() == null) {
            qualityTask.setCreateTime(LocalDateTime.now());
        }
        return super.save(qualityTask);
    }

    @Override
    public boolean startTask(Long id) {
        QualityTask task = getById(id);
        if (task == null) {
            throw new BusinessException("质检任务不存在");
        }
        if (task.getStatus() != 0) {
            throw new BusinessException("只有待处理的任务才能开始检验");
        }
        task.setStatus(1);
        return updateById(task);
    }

    @Override
    public boolean completeTask(Long id) {
        QualityTask task = getById(id);
        if (task == null) {
            throw new BusinessException("质检任务不存在");
        }
        if (task.getStatus() != 1) {
            throw new BusinessException("只有处理中的任务才能完成检验");
        }
        task.setStatus(2);
        return updateById(task);
    }

    @Override
    public QualityTask createIqcTask(Long stockInId, Long inspectorId) {
        QualityTask task = new QualityTask();
        task.setTaskType("IQC");
        task.setSourceId(stockInId);
        task.setInspectorId(inspectorId);
        task.setStatus(0);
        task.setCreateTime(LocalDateTime.now());
        save(task);
        return task;
    }

    @Override
    public QualityTask createIpqcTask(Long workOrderId, Long inspectorId) {
        QualityTask task = new QualityTask();
        task.setTaskType("IPQC");
        task.setSourceId(workOrderId);
        task.setInspectorId(inspectorId);
        task.setStatus(0);
        task.setCreateTime(LocalDateTime.now());
        save(task);
        return task;
    }

    @Override
    public QualityTask createOqcTask(Long orderId, Long inspectorId) {
        QualityTask task = new QualityTask();
        task.setTaskType("OQC");
        task.setSourceId(orderId);
        task.setInspectorId(inspectorId);
        task.setStatus(0);
        task.setCreateTime(LocalDateTime.now());
        save(task);
        return task;
    }
}
