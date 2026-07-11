package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.OrderCustomer;
import com.mes.mapper.OrderCustomerMapper;
import com.mes.service.OrderCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class OrderCustomerServiceImpl extends ServiceImpl<OrderCustomerMapper, OrderCustomer> implements OrderCustomerService {

    @Override
    public IPage<OrderCustomer> pageList(Page<OrderCustomer> page, String orderNo, Integer status, Long customerId) {
        LambdaQueryWrapper<OrderCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), OrderCustomer::getOrderNo, orderNo)
                .eq(status != null, OrderCustomer::getStatus, status)
                .eq(customerId != null, OrderCustomer::getCustomerId, customerId)
                .orderByDesc(OrderCustomer::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public List<OrderCustomer> listByStatus(Integer status) {
        LambdaQueryWrapper<OrderCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, OrderCustomer::getStatus, status)
                .orderByDesc(OrderCustomer::getCreateTime);
        return list(wrapper);
    }
}
