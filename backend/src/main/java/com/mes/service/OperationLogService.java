package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {

    void recordLog(String module, String type, String desc, String ip);
}
