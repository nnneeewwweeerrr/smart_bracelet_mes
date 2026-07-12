package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.AiChatHistory;
import com.mes.mapper.AiChatHistoryMapper;
import com.mes.service.AiChatHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiChatHistoryServiceImpl extends ServiceImpl<AiChatHistoryMapper, AiChatHistory> implements AiChatHistoryService {

    @Override
    public IPage<AiChatHistory> pageList(Page<AiChatHistory> page, String userId) {
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null && !userId.isEmpty(), AiChatHistory::getUserId, userId)
                .orderByDesc(AiChatHistory::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<AiChatHistory> getRecentHistory(String userId, Integer limit) {
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null && !userId.isEmpty(), AiChatHistory::getUserId, userId)
                .orderByDesc(AiChatHistory::getCreateTime)
                .last("LIMIT " + (limit != null ? limit : 20));
        return baseMapper.selectList(wrapper);
    }
}
