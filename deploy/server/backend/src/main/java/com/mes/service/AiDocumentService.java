package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.AiDocument;

import java.util.List;
import java.util.Map;

public interface AiDocumentService extends IService<AiDocument> {

    IPage<AiDocument> pageList(Page<AiDocument> page, String fileName, Integer status);

    void uploadDocument(String fileName, String content, String uploadedBy);

    void syncKnowledgeBase();

    List<Map<String, Object>> searchKnowledge(String query, int topK);

    void deleteDocument(Long id);
}
