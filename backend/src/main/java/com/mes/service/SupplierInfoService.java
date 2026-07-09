package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.SupplierInfo;

import java.util.List;

public interface SupplierInfoService extends IService<SupplierInfo> {

    List<SupplierInfo> listByName(String supplierName);
}
