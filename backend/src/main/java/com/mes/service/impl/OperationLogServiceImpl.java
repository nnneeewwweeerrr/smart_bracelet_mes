package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.OperationLog;
import com.mes.mapper.OperationLogMapper;
import com.mes.service.OperationLogService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void recordLog(String module, String type, String desc, String ip) {
        OperationLog log = new OperationLog();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.setUserName(username);
        } catch (Exception ignored) {
            log.setUserName("anonymous");
        }
        log.setOperationModule(module);
        log.setOperationType(type);
        log.setOperationDesc(desc);
        log.setIpAddress(ip);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }
}
