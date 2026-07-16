package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.AiChatHistory;
import com.mes.service.AiChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatHistoryController {

    @Autowired
    private AiChatHistoryService aiChatHistoryService;

    @GetMapping("/list")
    public Result<IPage<AiChatHistory>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String sessionId,
                                              @RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) String userName) {
        Page<AiChatHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(sessionId != null && !sessionId.isEmpty(), AiChatHistory::getSessionId, sessionId)
                .eq(userId != null, AiChatHistory::getUserId, userId)
                .like(userName != null && !userName.isEmpty(), AiChatHistory::getUserName, userName)
                .orderByDesc(AiChatHistory::getChatTime);
        return Result.success(aiChatHistoryService.page(page, wrapper));
    }

    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> sessions() {
        List<Map<String, Object>> sessions = aiChatHistoryService.listObjs(
                new LambdaQueryWrapper<AiChatHistory>()
                        .select(AiChatHistory::getSessionId)
                        .groupBy(AiChatHistory::getSessionId)
                        .orderByDesc(AiChatHistory::getChatTime)
                        .last("LIMIT 50"), o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("sessionId", o);
                    return m;
                });
        return Result.success(sessions);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", aiChatHistoryService.count());
        stats.put("today", aiChatHistoryService.count(new LambdaQueryWrapper<AiChatHistory>()
                .ge(AiChatHistory::getChatTime, java.time.LocalDate.now().atStartOfDay())));
        long positiveFeedback = aiChatHistoryService.count(new LambdaQueryWrapper<AiChatHistory>().eq(AiChatHistory::getFeedback, 1));
        long totalFeedback = aiChatHistoryService.count(new LambdaQueryWrapper<AiChatHistory>().isNotNull(AiChatHistory::getFeedback));
        stats.put("positiveRate", totalFeedback > 0 ? String.format("%.1f", (double) positiveFeedback / totalFeedback * 100) : "0");
        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody AiChatHistory history) {
        aiChatHistoryService.save(history);
        return Result.success();
    }

    @PutMapping("/feedback/{id}")
    public Result<Void> feedback(@PathVariable Long id, @RequestParam Integer feedback) {
        AiChatHistory history = aiChatHistoryService.getById(id);
        if (history == null) return Result.error("记录不存在");
        history.setFeedback(feedback);
        aiChatHistoryService.updateById(history);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiChatHistoryService.removeById(id);
        return Result.success();
    }
}