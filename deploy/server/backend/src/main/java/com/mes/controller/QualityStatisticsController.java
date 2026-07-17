package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.common.Result;
import com.mes.entity.ExceptionRecord;
import com.mes.entity.QualityResult;
import com.mes.entity.QualityTask;
import com.mes.service.ExceptionRecordService;
import com.mes.service.QualityResultService;
import com.mes.service.QualityTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/quality-statistics")
public class QualityStatisticsController {

    @Autowired
    private QualityTaskService qualityTaskService;

    @Autowired
    private QualityResultService qualityResultService;

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();

        long totalTasks = qualityTaskService.count();
        long pendingTasks = qualityTaskService.count(new LambdaQueryWrapper<QualityTask>().eq(QualityTask::getStatus, 0));
        long processingTasks = qualityTaskService.count(new LambdaQueryWrapper<QualityTask>().eq(QualityTask::getStatus, 1));
        long completedTasks = qualityTaskService.count(new LambdaQueryWrapper<QualityTask>().eq(QualityTask::getStatus, 2));

        long totalResults = qualityResultService.count();
        long passResults = qualityResultService.count(new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getResult, 1));
        long failResults = qualityResultService.count(new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getResult, 0));

        long totalExceptions = exceptionRecordService.count();
        long pendingExceptions = exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 0));
        long processingExceptions = exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 1));

        double passRate = totalResults > 0 ? (double) passResults / totalResults * 100 : 100.0;

        data.put("totalTasks", totalTasks);
        data.put("pendingTasks", pendingTasks);
        data.put("processingTasks", processingTasks);
        data.put("completedTasks", completedTasks);
        data.put("totalResults", totalResults);
        data.put("passResults", passResults);
        data.put("failResults", failResults);
        data.put("passRate", String.format("%.1f", passRate));
        data.put("totalExceptions", totalExceptions);
        data.put("pendingExceptions", pendingExceptions);
        data.put("processingExceptions", processingExceptions);

        return Result.success(data);
    }

    @GetMapping("/avatar-report")
    public Result<Map<String, Object>> avatarReport() {
        Map<String, Object> data = new HashMap<>();

        long totalTasks = qualityTaskService.count();
        long pendingTasks = qualityTaskService.count(new LambdaQueryWrapper<QualityTask>().eq(QualityTask::getStatus, 0));
        long completedTasks = qualityTaskService.count(new LambdaQueryWrapper<QualityTask>().eq(QualityTask::getStatus, 2));

        long totalResults = qualityResultService.count();
        long passResults = qualityResultService.count(new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getResult, 1));
        long failResults = qualityResultService.count(new LambdaQueryWrapper<QualityResult>().eq(QualityResult::getResult, 0));

        long totalExceptions = exceptionRecordService.count();
        long pendingExceptions = exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 0));
        long processingExceptions = exceptionRecordService.count(new LambdaQueryWrapper<ExceptionRecord>().eq(ExceptionRecord::getStatus, 1));

        double passRate = totalResults > 0 ? (double) passResults / totalResults * 100 : 100.0;

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM月dd日"));

        List<String> reports = new ArrayList<>();

        reports.add("您好，我是质量管理助手小智。今天是" + today + "。");

        if (totalTasks == 0) {
            reports.add("当前系统中暂无质检任务数据。建议及时录入质检任务，以便我为您提供质量分析服务。");
        } else {
            reports.add("当前质检任务总数为" + totalTasks + "条，其中已完成" + completedTasks + "条，待处理" + pendingTasks + "条。");

            if (totalResults > 0) {
                reports.add("质检合格率" + String.format("%.1f", passRate) + "%，共检测" + totalResults + "项，合格" + passResults + "项，不合格" + failResults + "项。");
            }
        }

        if (totalExceptions > 0) {
            reports.add("质量异常记录共" + totalExceptions + "条，其中待处理" + pendingExceptions + "条，处理中" + processingExceptions + "条。");
        }

        List<String> alerts = new ArrayList<>();

        if (pendingTasks > 0) {
            alerts.add("有" + pendingTasks + "条质检任务待处理，请及时安排质检。");
        }

        if (failResults > 0) {
            alerts.add("存在" + failResults + "项不合格检测结果，请关注并分析原因。");
        }

        if (pendingExceptions > 0) {
            alerts.add("有" + pendingExceptions + "条质量异常待处理，请尽快分派处理。");
        }

        if (passRate < 95 && totalResults > 0) {
            alerts.add("质检合格率低于95%，建议加强过程质量控制，排查不合格原因。");
        }

        if (alerts.isEmpty()) {
            alerts.add("目前质量状况良好，请继续保持！");
        }

        List<Map<String, String>> recentExceptions = new ArrayList<>();
        List<ExceptionRecord> exceptionList = exceptionRecordService.list(
                new LambdaQueryWrapper<ExceptionRecord>()
                        .orderByDesc(ExceptionRecord::getReportTime)
                        .last("LIMIT 5")
        );
        for (ExceptionRecord ex : exceptionList) {
            Map<String, String> item = new HashMap<>();
            item.put("no", ex.getExceptionNo());
            item.put("type", ex.getExceptionType());
            item.put("title", ex.getTitle());
            item.put("status", getStatusText(ex.getStatus()));
            recentExceptions.add(item);
        }

        data.put("today", today);
        data.put("reports", reports);
        data.put("alerts", alerts);
        data.put("recentExceptions", recentExceptions);
        data.put("totalTasks", totalTasks);
        data.put("pendingTasks", pendingTasks);
        data.put("passRate", String.format("%.1f", passRate));
        data.put("failResults", failResults);
        data.put("pendingExceptions", pendingExceptions);

        return Result.success(data);
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        String[] arr = {"待处理", "处理中", "已处理", "已关闭", "已取消"};
        return status >= 0 && status < arr.length ? arr[status] : "未知";
    }
}
