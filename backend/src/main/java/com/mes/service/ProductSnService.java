package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.ProductSn;

import java.util.Map;

public interface ProductSnService extends IService<ProductSn> {

    Map<String, Object> getReverseTrace(String snCode);

    Map<String, Object> getForwardTrace(String batchNo);
}
