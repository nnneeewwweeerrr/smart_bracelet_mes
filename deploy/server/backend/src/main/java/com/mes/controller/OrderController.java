package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.OrderCustomer;
import com.mes.service.OrderCustomerService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderCustomerService orderCustomerService;

    @GetMapping("/list")
    public Result<IPage<OrderCustomer>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long customerId) {
        Page<OrderCustomer> page = new Page<>(current, size);
        return Result.success(orderCustomerService.pageList(page, orderNo, status, customerId));
    }

    @GetMapping("/{id}")
    public Result<OrderCustomer> detail(@PathVariable Long id) {
        return Result.success(orderCustomerService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "订单管理", type = "新增", description = "新增客户订单")
    public Result<Void> save(@RequestBody OrderCustomer order) {
        order.setOrderNo(BarcodeUtil.generateOrderNo());
        calcTotalAmount(order);
        if (order.getStatus() == null) {
            order.setStatus(0);
        }
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderCustomerService.save(order);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "订单管理", type = "修改", description = "修改客户订单")
    public Result<Void> update(@RequestBody OrderCustomer order) {
        calcTotalAmount(order);
        order.setUpdateTime(LocalDateTime.now());
        orderCustomerService.updateById(order);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "订单管理", type = "删除", description = "删除客户订单")
    public Result<Void> delete(@PathVariable Long id) {
        orderCustomerService.removeById(id);
        return Result.success();
    }

    @PutMapping("/audit/{id}")
    @OperationLog(module = "订单管理", type = "审核", description = "审核客户订单")
    public Result<Void> audit(@PathVariable Long id) {
        OrderCustomer order = orderCustomerService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            return Result.error("订单状态不允许审核");
        }
        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        orderCustomerService.updateById(order);
        return Result.success();
    }

    @PutMapping("/dispatch/{id}")
    @OperationLog(module = "订单管理", type = "派工", description = "订单派工生产")
    public Result<Void> dispatch(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> productionInfo) {
        OrderCustomer order = orderCustomerService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            return Result.error("订单状态不允许派工");
        }
        order.setStatus(2);
        order.setUpdateTime(LocalDateTime.now());
        orderCustomerService.updateById(order);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    @OperationLog(module = "订单管理", type = "取消", description = "取消客户订单")
    public Result<Void> cancel(@PathVariable Long id) {
        OrderCustomer order = orderCustomerService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (Integer.valueOf(3).equals(order.getStatus()) || Integer.valueOf(4).equals(order.getStatus())) {
            return Result.error("订单状态不允许取消");
        }
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        orderCustomerService.updateById(order);
        return Result.success();
    }

    @PutMapping("/status/{id}/{status}")
    @OperationLog(module = "订单管理", type = "状态流转", description = "订单状态变更")
    public Result<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        OrderCustomer order = orderCustomerService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderCustomerService.updateById(order);
        return Result.success();
    }

    private void calcTotalAmount(OrderCustomer order) {
        if (order.getQuantity() != null && order.getUnitPrice() != null) {
            order.setTotalAmount(order.getUnitPrice().multiply(new BigDecimal(order.getQuantity())));
        }
    }
}
