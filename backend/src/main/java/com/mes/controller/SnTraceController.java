package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.ProductSn;
import com.mes.service.ProductSnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SN唯一码全生命周期追溯接口
 * 支持正向追溯（物料批次→成品SN）和逆向追溯（成品SN→全链路）
 */
@RestController
@RequestMapping("/api/sn-trace")
public class SnTraceController {

    @Autowired
    private ProductSnService productSnService;

    /**
     * 逆向追溯：输入SN码，返回完整全链路档案
     * 包含：物料明细、生产过程、质检记录、归属信息
     */
    @GetMapping("/reverse/{snCode}")
    public Result<Map<String, Object>> reverseTrace(@PathVariable String snCode) {
        Map<String, Object> data = productSnService.getReverseTrace(snCode);
        return Result.success(data);
    }

    /**
     * 正向追溯：输入物料批次号，返回所有使用该批次的成品SN列表
     */
    @GetMapping("/forward/{batchNo}")
    public Result<Map<String, Object>> forwardTrace(@PathVariable String batchNo) {
        Map<String, Object> data = productSnService.getForwardTrace(batchNo);
        return Result.success(data);
    }

    /**
     * SN列表查询（分页）
     */
    @GetMapping("/list")
    public Result<IPage<ProductSn>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String snCode,
            @RequestParam(required = false) Long workOrderId,
            @RequestParam(required = false) Integer status) {
        Page<ProductSn> page = new Page<>(current, size);
        IPage<ProductSn> result = productSnService.lambdaQuery()
                .like(snCode != null && !snCode.isEmpty(), ProductSn::getSnCode, snCode)
                .eq(workOrderId != null, ProductSn::getWorkOrderId, workOrderId)
                .eq(status != null, ProductSn::getStatus, status)
                .orderByDesc(ProductSn::getCreateTime)
                .page(page);
        return Result.success(result);
    }

    /**
     * SN详情
     */
    @GetMapping("/{id}")
    public Result<ProductSn> detail(@PathVariable Long id) {
        return Result.success(productSnService.getById(id));
    }

    /**
     * 新增SN
     */
    @PostMapping("/save")
    @OperationLog(module = "SN追溯", type = "新增", description = "新增产品SN码")
    public Result<Void> save(@RequestBody ProductSn sn) {
        productSnService.save(sn);
        return Result.success();
    }

    /**
     * 更新SN
     */
    @PutMapping("/update")
    @OperationLog(module = "SN追溯", type = "修改", description = "修改产品SN码")
    public Result<Void> update(@RequestBody ProductSn sn) {
        productSnService.updateById(sn);
        return Result.success();
    }

    /**
     * 删除SN
     */
    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "SN追溯", type = "删除", description = "删除产品SN码")
    public Result<Void> delete(@PathVariable Long id) {
        productSnService.removeById(id);
        return Result.success();
    }
}
