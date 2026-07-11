package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.PurchaseOrder;

public interface PurchaseOrderService extends IService<PurchaseOrder> {

    IPage<PurchaseOrder> pageList(Page<PurchaseOrder> page, String purchaseNo, Integer status, Long supplierId);

    /**
     * 到货登记：状态1→2，同时创建入库单并更新库存
     */
    void arrive(Long id, Long warehouseId);
}
