package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.ProcessInfo;

import java.util.List;

public interface ProcessInfoService extends IService<ProcessInfo> {

    List<ProcessInfo> listByProductId(Long productId);
}
