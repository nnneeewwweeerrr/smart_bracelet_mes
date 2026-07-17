package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.ExceptionRecord;

import java.util.List;

public interface ExceptionRecordService extends IService<ExceptionRecord> {

    boolean handleException(Long id);

    boolean resolveException(Long id);

    List<ExceptionRecord> batchTrace(Long workOrderId);
}
