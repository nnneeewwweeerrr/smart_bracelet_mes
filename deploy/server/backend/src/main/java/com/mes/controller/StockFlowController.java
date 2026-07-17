package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.StockIn;
import com.mes.entity.StockOut;
import com.mes.service.StockInService;
import com.mes.service.StockOutService;
import com.mes.vo.StockFlowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 库存流水（联合查询入库和出库记录）
 */
@RestController
@RequestMapping("/api/stock-flow")
public class StockFlowController {

    @Autowired
    private StockInService stockInService;

    @Autowired
    private StockOutService stockOutService;

    @GetMapping("/list")
    public Result<IPage<StockFlowVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        List<StockFlowVO> flowList = new ArrayList<>();

        List<StockIn> stockInList = stockInService.list();
        for (StockIn stockIn : stockInList) {
            StockFlowVO vo = new StockFlowVO();
            vo.setFlowNo(stockIn.getInNo());
            vo.setFlowType("入库");
            vo.setMaterialId(stockIn.getMaterialId());
            vo.setWarehouseId(stockIn.getWarehouseId());
            vo.setQuantity(stockIn.getQuantity());
            vo.setCreateTime(stockIn.getCreateTime());
            flowList.add(vo);
        }

        List<StockOut> stockOutList = stockOutService.list();
        for (StockOut stockOut : stockOutList) {
            StockFlowVO vo = new StockFlowVO();
            vo.setFlowNo(stockOut.getOutNo());
            vo.setFlowType("出库");
            vo.setMaterialId(stockOut.getMaterialId());
            vo.setWarehouseId(stockOut.getWarehouseId());
            vo.setQuantity(stockOut.getQuantity());
            vo.setCreateTime(stockOut.getCreateTime());
            flowList.add(vo);
        }

        flowList.sort(Comparator.nullsLast(Comparator.comparing(StockFlowVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))));

        int total = flowList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<StockFlowVO> pageRecords = fromIndex < total ? flowList.subList(fromIndex, toIndex) : new ArrayList<>();

        Page<StockFlowVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(pageRecords);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<StockFlowVO> detail(@PathVariable Long id) {
        StockIn stockIn = stockInService.getById(id);
        if (stockIn != null) {
            StockFlowVO vo = new StockFlowVO();
            vo.setFlowNo(stockIn.getInNo());
            vo.setFlowType("入库");
            vo.setMaterialId(stockIn.getMaterialId());
            vo.setWarehouseId(stockIn.getWarehouseId());
            vo.setQuantity(stockIn.getQuantity());
            vo.setCreateTime(stockIn.getCreateTime());
            return Result.success(vo);
        }
        StockOut stockOut = stockOutService.getById(id);
        if (stockOut != null) {
            StockFlowVO vo = new StockFlowVO();
            vo.setFlowNo(stockOut.getOutNo());
            vo.setFlowType("出库");
            vo.setMaterialId(stockOut.getMaterialId());
            vo.setWarehouseId(stockOut.getWarehouseId());
            vo.setQuantity(stockOut.getQuantity());
            vo.setCreateTime(stockOut.getCreateTime());
            return Result.success(vo);
        }
        return Result.success(null);
    }
}
