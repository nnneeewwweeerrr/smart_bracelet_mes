package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.SysDict;

import java.util.List;

public interface SysDictService extends IService<SysDict> {

    List<SysDict> listByType(String dictType);
}
