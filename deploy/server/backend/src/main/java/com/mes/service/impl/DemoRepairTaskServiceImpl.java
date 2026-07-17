package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoRepairTask;
import com.mes.mapper.DemoRepairTaskMapper;
import com.mes.service.DemoRepairTaskService;
import org.springframework.stereotype.Service;

@Service
public class DemoRepairTaskServiceImpl extends ServiceImpl<DemoRepairTaskMapper, DemoRepairTask> implements DemoRepairTaskService {
}