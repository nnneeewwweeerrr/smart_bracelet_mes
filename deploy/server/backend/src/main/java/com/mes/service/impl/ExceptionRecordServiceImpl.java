package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.ExceptionRecord;
import com.mes.exception.BusinessException;
import com.mes.mapper.ExceptionRecordMapper;
import com.mes.service.ExceptionRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExceptionRecordServiceImpl extends ServiceImpl<ExceptionRecordMapper, ExceptionRecord> implements ExceptionRecordService {

    @Override
    public boolean save(ExceptionRecord exceptionRecord) {
        if (exceptionRecord.getStatus() == null) {
            exceptionRecord.setStatus(0);
        }
        if (exceptionRecord.getReportTime() == null) {
            exceptionRecord.setReportTime(LocalDateTime.now());
        }
        return super.save(exceptionRecord);
    }

    @Override
    public boolean handleException(Long id) {
        ExceptionRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("异常记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("只有待处理的异常才能开始处理");
        }
        record.setStatus(1);
        return updateById(record);
    }

    @Override
    public boolean resolveException(Long id) {
        ExceptionRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("异常记录不存在");
        }
        if (record.getStatus() != 1) {
            throw new BusinessException("只有处理中的异常才能解决闭环");
        }
        record.setStatus(2);
        record.setHandleTime(LocalDateTime.now());
        return updateById(record);
    }

    @Override
    public List<ExceptionRecord> batchTrace(Long workOrderId) {
        LambdaQueryWrapper<ExceptionRecord> wrapper = new LambdaQueryWrapper<>();
        // 由于已删除 workOrderId 字段，此方法保留但不使用该过滤
        wrapper.orderByDesc(ExceptionRecord::getReportTime);
        return list(wrapper);
    }
}
