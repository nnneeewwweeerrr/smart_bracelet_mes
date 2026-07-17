package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.*;
import com.mes.mapper.ProductSnMapper;
import com.mes.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductSnServiceImpl extends ServiceImpl<ProductSnMapper, ProductSn> implements ProductSnService {

    @Autowired
    private SnMaterialTraceService snMaterialTraceService;

    @Autowired
    private SnProcessTraceService snProcessTraceService;

    @Autowired
    private SnQualityTraceService snQualityTraceService;

    /**
     * 逆向追溯：输入SN码，返回完整全链路档案
     * 包含：物料明细、生产过程、质检记录、归属信息
     */
    @Override
    public Map<String, Object> getReverseTrace(String snCode) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询SN基本信息
        LambdaQueryWrapper<ProductSn> snWrapper = new LambdaQueryWrapper<>();
        snWrapper.eq(ProductSn::getSnCode, snCode);
        ProductSn sn = getOne(snWrapper);

        if (sn == null) {
            result.put("exists", false);
            result.put("message", "SN码不存在：" + snCode);
            return result;
        }

        result.put("exists", true);
        result.put("snInfo", sn);

        // 2. 物料明细
        LambdaQueryWrapper<SnMaterialTrace> matWrapper = new LambdaQueryWrapper<>();
        matWrapper.eq(SnMaterialTrace::getSnCode, snCode)
                .orderByAsc(SnMaterialTrace::getProcessId);
        List<SnMaterialTrace> materialList = snMaterialTraceService.list(matWrapper);
        result.put("materialList", materialList);

        // 3. 生产过程
        LambdaQueryWrapper<SnProcessTrace> procWrapper = new LambdaQueryWrapper<>();
        procWrapper.eq(SnProcessTrace::getSnCode, snCode)
                .orderByAsc(SnProcessTrace::getSortOrder, SnProcessTrace::getStartTime);
        List<SnProcessTrace> processList = snProcessTraceService.list(procWrapper);
        result.put("processList", processList);

        // 4. 质检记录
        LambdaQueryWrapper<SnQualityTrace> qualWrapper = new LambdaQueryWrapper<>();
        qualWrapper.eq(SnQualityTrace::getSnCode, snCode)
                .orderByAsc(SnQualityTrace::getCheckTime);
        List<SnQualityTrace> qualityList = snQualityTraceService.list(qualWrapper);
        result.put("qualityList", qualityList);

        // 5. 统计信息
        int passCount = 0;
        int failCount = 0;
        for (SnQualityTrace q : qualityList) {
            if (q.getResult() != null && q.getResult() == 1) {
                passCount++;
            } else {
                failCount++;
            }
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("materialCount", materialList.size());
        stats.put("processCount", processList.size());
        stats.put("qualityCount", qualityList.size());
        stats.put("qualityPassCount", passCount);
        stats.put("qualityFailCount", failCount);
        stats.put("hasException", failCount > 0 || processList.stream().anyMatch(p -> p.getStatus() != null && p.getStatus() == 2));
        result.put("stats", stats);

        return result;
    }

    /**
     * 正向追溯：支持按物料批次号或工单号查询，返回所有相关的成品SN列表与对应工单
     */
    @Override
    public Map<String, Object> getForwardTrace(String key) {
        Map<String, Object> result = new HashMap<>();
        List<ProductSn> snList = new ArrayList<>();
        Set<String> snCodes = new HashSet<>();

        // 1. 优先按工单号查询
        LambdaQueryWrapper<ProductSn> woWrapper = new LambdaQueryWrapper<>();
        woWrapper.eq(ProductSn::getWorkOrderNo, key)
                .orderByDesc(ProductSn::getProductionDate);
        snList = list(woWrapper);

        // 2. 如果工单号查询为空，再按物料批次号查询
        if (snList == null || snList.isEmpty()) {
            LambdaQueryWrapper<SnMaterialTrace> matWrapper = new LambdaQueryWrapper<>();
            matWrapper.eq(SnMaterialTrace::getBatchNo, key)
                    .orderByDesc(SnMaterialTrace::getCreateTime);
            List<SnMaterialTrace> materialList = snMaterialTraceService.list(matWrapper);

            if (materialList == null || materialList.isEmpty()) {
                result.put("exists", false);
                result.put("message", "未找到使用该批次或工单的产品：" + key);
                return result;
            }

            snCodes = materialList.stream()
                    .map(SnMaterialTrace::getSnCode)
                    .collect(Collectors.toSet());

            LambdaQueryWrapper<ProductSn> snWrapper = new LambdaQueryWrapper<>();
            snWrapper.in(ProductSn::getSnCode, snCodes)
                    .orderByDesc(ProductSn::getProductionDate);
            snList = list(snWrapper);
        } else {
            snCodes = snList.stream()
                    .map(ProductSn::getSnCode)
                    .collect(Collectors.toSet());
        }

        if (snList == null || snList.isEmpty()) {
            result.put("exists", false);
            result.put("message", "未找到使用该批次或工单的产品：" + key);
            return result;
        }

        result.put("exists", true);
        result.put("snList", snList);

        // 3. 查询物料追溯信息
        LambdaQueryWrapper<SnMaterialTrace> matWrapper = new LambdaQueryWrapper<>();
        matWrapper.in(SnMaterialTrace::getSnCode, snCodes)
                .orderByAsc(SnMaterialTrace::getProcessId);
        List<SnMaterialTrace> materialList = snMaterialTraceService.list(matWrapper);
        result.put("materialList", materialList);

        // 4. 查询生产过程追溯信息
        LambdaQueryWrapper<SnProcessTrace> procWrapper = new LambdaQueryWrapper<>();
        procWrapper.in(SnProcessTrace::getSnCode, snCodes)
                .orderByAsc(SnProcessTrace::getSortOrder, SnProcessTrace::getStartTime);
        List<SnProcessTrace> processList = snProcessTraceService.list(procWrapper);
        result.put("processList", processList);

        // 5. 查询质检追溯信息
        LambdaQueryWrapper<SnQualityTrace> qualWrapper = new LambdaQueryWrapper<>();
        qualWrapper.in(SnQualityTrace::getSnCode, snCodes)
                .orderByAsc(SnQualityTrace::getCheckTime);
        List<SnQualityTrace> qualityList = snQualityTraceService.list(qualWrapper);
        result.put("qualityList", qualityList);

        // 6. 按工单分组统计
        Map<Long, List<ProductSn>> groupByOrder = snList.stream()
                .collect(Collectors.groupingBy(sn -> sn.getWorkOrderId() != null ? sn.getWorkOrderId() : 0L));

        List<Map<String, Object>> workOrderGroups = new ArrayList<>();
        for (Map.Entry<Long, List<ProductSn>> entry : groupByOrder.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            List<ProductSn> groupSns = entry.getValue();
            group.put("workOrderId", entry.getKey());
            group.put("workOrderNo", groupSns.get(0).getWorkOrderNo());
            group.put("productName", groupSns.get(0).getProductName());
            group.put("snCount", groupSns.size());
            group.put("snCodes", groupSns.stream().map(ProductSn::getSnCode).collect(Collectors.toList()));
            workOrderGroups.add(group);
        }
        result.put("workOrderGroups", workOrderGroups);

        return result;
    }
}
