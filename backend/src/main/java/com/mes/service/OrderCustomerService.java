package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.OrderCustomer;

import java.util.List;

public interface OrderCustomerService extends IService<OrderCustomer> {

    IPage<OrderCustomer> pageList(Page<OrderCustomer> page, String orderNo, Integer status, Long customerId);

    List<OrderCustomer> listByStatus(Integer status);
}
