package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.ProductionPlan;
import com.mes.mapper.ProductionPlanMapper;
import com.mes.service.ProductionPlanService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductionPlanServiceImpl extends ServiceImpl<ProductionPlanMapper, ProductionPlan> implements ProductionPlanService {

    @Override
    public IPage<ProductionPlan> pageList(Page<ProductionPlan> page, String planNo, Integer status, Long orderId) {
        LambdaQueryWrapper<ProductionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(planNo), ProductionPlan::getPlanNo, planNo)
                .eq(status != null, ProductionPlan::getStatus, status)
                .eq(orderId != null, ProductionPlan::getOrderId, orderId)
                .orderByDesc(ProductionPlan::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public boolean startPlan(Long id) {
        ProductionPlan plan = getById(id);
        if (plan == null) {
            return false;
        }
        if (!Integer.valueOf(0).equals(plan.getStatus())) {
            return false;
        }
        plan.setStatus(1);
        return updateById(plan);
    }

    @Override
    public boolean finishPlan(Long id) {
        ProductionPlan plan = getById(id);
        if (plan == null) {
            return false;
        }
        if (!Integer.valueOf(1).equals(plan.getStatus())) {
            return false;
        }
        plan.setStatus(2);
        return updateById(plan);
    }
}
