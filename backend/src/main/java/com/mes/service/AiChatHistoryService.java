package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.AiChatHistory;

import java.util.List;

public interface AiChatHistoryService extends IService<AiChatHistory> {

    IPage<AiChatHistory> pageList(Page<AiChatHistory> page, String userId);

    List<AiChatHistory> getRecentHistory(String userId, Integer limit);
}
