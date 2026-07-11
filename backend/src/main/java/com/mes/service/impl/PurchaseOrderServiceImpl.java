package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.PurchaseOrder;
import com.mes.entity.StockIn;
import com.mes.mapper.PurchaseOrderMapper;
import com.mes.service.PurchaseOrderService;
import com.mes.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    @Autowired
    private StockInService stockInService;

    @Override
    public IPage<PurchaseOrder> pageList(Page<PurchaseOrder> page, String purchaseNo, Integer status, Long supplierId) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(purchaseNo), PurchaseOrder::getPurchaseNo, purchaseNo)
                .eq(status != null, PurchaseOrder::getStatus, status)
                .eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId)
                .orderByDesc(PurchaseOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arrive(Long id, Long warehouseId) {
        PurchaseOrder purchase = getById(id);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }
        if (!Integer.valueOf(1).equals(purchase.getStatus())) {
            throw new RuntimeException("采购单状态不允许到货登记");
        }
        Long targetWarehouseId = warehouseId == null ? 1L : warehouseId;

        // 创建入库单（stockInService.save 内部生成 inNo 并通过 addStock 更新/累加库存）
        StockIn stockIn = new StockIn();
        stockIn.setMaterialId(purchase.getMaterialId());
        stockIn.setWarehouseId(targetWarehouseId);
        stockIn.setQuantity(new BigDecimal(purchase.getQuantity()));
        stockIn.setSourceType("采购");
        stockIn.setSourceNo(purchase.getPurchaseNo());
        stockIn.setCreateTime(LocalDateTime.now());
        stockInService.save(stockIn);

        // 更新采购单状态为已到货
        purchase.setStatus(2);
        updateById(purchase);
    }
}
