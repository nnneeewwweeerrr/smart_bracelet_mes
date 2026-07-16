package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.MaterialInfo;
import com.mes.entity.StockInfo;
import com.mes.entity.WarehouseInfo;
import com.mes.service.MaterialInfoService;
import com.mes.service.StockInfoService;
import com.mes.service.WarehouseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockInfoService stockInfoService;

    @Autowired
    private MaterialInfoService materialInfoService;

    @Autowired
    private WarehouseInfoService warehouseInfoService;

    @GetMapping("/list")
    public Result<IPage<StockInfo>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String materialId,
                                          @RequestParam(required = false) String warehouseId,
                                          @RequestParam(required = false) String materialNo,
                                          @RequestParam(required = false) String materialName,
                                          @RequestParam(required = false) String warehouseName) {
        Page<StockInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (materialId != null && !materialId.isEmpty()) {
            try {
                Long id = Long.parseLong(materialId);
                wrapper.eq(StockInfo::getMaterialId, id);
            } catch (NumberFormatException e) {
                MaterialInfo material = materialInfoService.getOne(new LambdaQueryWrapper<MaterialInfo>().eq(MaterialInfo::getMaterialNo, materialId));
                if (material != null) {
                    wrapper.eq(StockInfo::getMaterialId, material.getId());
                }
            }
        } else if (materialNo != null && !materialNo.isEmpty()) {
            MaterialInfo material = materialInfoService.getOne(new LambdaQueryWrapper<MaterialInfo>().eq(MaterialInfo::getMaterialNo, materialNo));
            if (material != null) {
                wrapper.eq(StockInfo::getMaterialId, material.getId());
            }
        } else if (materialName != null && !materialName.isEmpty()) {
            List<MaterialInfo> materials = materialInfoService.list(new LambdaQueryWrapper<MaterialInfo>().like(MaterialInfo::getMaterialName, materialName));
            if (!materials.isEmpty()) {
                wrapper.in(StockInfo::getMaterialId, materials.stream().map(MaterialInfo::getId).collect(Collectors.toList()));
            }
        }
        
        if (warehouseId != null && !warehouseId.isEmpty()) {
            try {
                Long id = Long.parseLong(warehouseId);
                wrapper.eq(StockInfo::getWarehouseId, id);
            } catch (NumberFormatException e) {
                WarehouseInfo warehouse = warehouseInfoService.getOne(new LambdaQueryWrapper<WarehouseInfo>().eq(WarehouseInfo::getWarehouseName, warehouseId));
                if (warehouse != null) {
                    wrapper.eq(StockInfo::getWarehouseId, warehouse.getId());
                }
            }
        } else if (warehouseName != null && !warehouseName.isEmpty()) {
            WarehouseInfo warehouse = warehouseInfoService.getOne(new LambdaQueryWrapper<WarehouseInfo>().eq(WarehouseInfo::getWarehouseName, warehouseName));
            if (warehouse != null) {
                wrapper.eq(StockInfo::getWarehouseId, warehouse.getId());
            }
        }
        
        IPage<StockInfo> result = stockInfoService.page(page, wrapper);
        result.getRecords().forEach(this::mapFields);
        return Result.success(result);
    }

    private void mapFields(StockInfo info) {
        MaterialInfo material = materialInfoService.getById(info.getMaterialId());
        WarehouseInfo warehouse = warehouseInfoService.getById(info.getWarehouseId());
        
        info.setMaterialNo(material != null ? material.getMaterialNo() : "");
        info.setMaterialName(material != null ? material.getMaterialName() : "");
        info.setType(material != null ? material.getType() : "");
        info.setSpec(material != null ? material.getMaterialNo() : "");
        info.setUnit(material != null ? material.getUnit() : "");
        info.setWarehouse(warehouse != null ? warehouse.getWarehouseName() : "");
        info.setSafetyStock(material != null ? material.getSafetyStock() : 0);
        info.setAvgPrice(material != null ? material.getPrice() : BigDecimal.ZERO);
        BigDecimal unitPrice = material != null ? material.getPrice() : BigDecimal.ZERO;
        info.setTotalValue(unitPrice.multiply(info.getQuantity() != null ? info.getQuantity() : BigDecimal.ZERO));
    }

    @GetMapping("/{id}")
    public Result<StockInfo> detail(@PathVariable Long id) {
        return Result.success(stockInfoService.getById(id));
    }

    @PostMapping("/take-stock")
    @OperationLog(module = "库存管理", type = "盘点", description = "库存盘点")
    public Result<Void> takeStock(@RequestBody Map<String, Object> body) {
        Long warehouseId = Long.valueOf(body.get("warehouseId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        for (Map<String, Object> item : items) {
            Long materialId = Long.valueOf(item.get("materialId").toString());
            BigDecimal quantity = new BigDecimal(item.get("quantity").toString());
            LambdaQueryWrapper<StockInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StockInfo::getMaterialId, materialId)
                    .eq(StockInfo::getWarehouseId, warehouseId);
            StockInfo stockInfo = stockInfoService.getOne(wrapper);
            if (stockInfo != null) {
                stockInfo.setQuantity(quantity);
                stockInfoService.updateById(stockInfo);
            } else {
                StockInfo newStock = new StockInfo();
                newStock.setMaterialId(materialId);
                newStock.setWarehouseId(warehouseId);
                newStock.setQuantity(quantity);
                stockInfoService.save(newStock);
            }
        }
        return Result.success();
    }
}
