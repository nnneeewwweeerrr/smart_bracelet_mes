package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.QualityResult;
import com.mes.entity.QualityTask;
import com.mes.exception.BusinessException;
import com.mes.mapper.QualityResultMapper;
import com.mes.service.QualityResultService;
import com.mes.service.QualityTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QualityResultServiceImpl extends ServiceImpl<QualityResultMapper, QualityResult> implements QualityResultService {

    @Autowired
    private QualityTaskService qualityTaskService;

    @Override
    public List<QualityResult> listByTaskId(Long taskId) {
        LambdaQueryWrapper<QualityResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QualityResult::getTaskId, taskId);
        wrapper.orderByAsc(QualityResult::getId);
        return list(wrapper);
    }

    @Override
    public boolean batchSave(List<QualityResult> results) {
        if (results == null || results.isEmpty()) {
            throw new BusinessException("检验结果不能为空");
        }
        return saveBatch(results);
    }

    @Override
    public Map<String, Object> generateReport(Long taskId) {
        QualityTask task = qualityTaskService.getById(taskId);
        if (task == null) {
            throw new BusinessException("质检任务不存在");
        }

        List<QualityResult> results = listByTaskId(taskId);

        Map<String, Object> report = new HashMap<>();
        report.put("taskId", taskId);
        report.put("taskNo", task.getTaskNo());
        report.put("taskType", task.getTaskType());
        report.put("status", task.getStatus());
        report.put("items", results);

        int total = results.size();
        int passCount = 0;
        for (QualityResult r : results) {
            if (r.getResult() != null && r.getResult() == 1) {
                passCount++;
            }
        }

        report.put("totalCount", total);
        report.put("passCount", passCount);
        report.put("failCount", total - passCount);

        BigDecimal passRate = BigDecimal.ZERO;
        if (total > 0) {
            passRate = BigDecimal.valueOf(passCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        }
        report.put("passRate", passRate + "%");

        boolean overallPass = total > 0 && passCount == total;
        report.put("overallResult", overallPass ? "合格" : "不合格");

        return report;
    }
}
