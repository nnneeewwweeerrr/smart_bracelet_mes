package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.QualityResult;

import java.util.List;
import java.util.Map;

public interface QualityResultService extends IService<QualityResult> {

    List<QualityResult> listByTaskId(Long taskId);

    boolean batchSave(List<QualityResult> results);

    Map<String, Object> generateReport(Long taskId);
}
