package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.AiKnowledgeDocument;
import com.mes.service.AiKnowledgeDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-knowledge")
public class AiKnowledgeDocumentController {

    @Autowired
    private AiKnowledgeDocumentService aiKnowledgeDocumentService;

    @GetMapping("/list")
    public Result<IPage<AiKnowledgeDocument>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) String docNo,
                                                    @RequestParam(required = false) String docName,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam(required = false) String docType,
                                                    @RequestParam(required = false) Integer status) {
        Page<AiKnowledgeDocument> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(docNo != null && !docNo.isEmpty(), AiKnowledgeDocument::getDocNo, docNo)
                .like(docName != null && !docName.isEmpty(), AiKnowledgeDocument::getDocName, docName)
                .eq(category != null && !category.isEmpty(), AiKnowledgeDocument::getCategory, category)
                .eq(docType != null && !docType.isEmpty(), AiKnowledgeDocument::getDocType, docType)
                .eq(status != null, AiKnowledgeDocument::getStatus, status)
                .orderByDesc(AiKnowledgeDocument::getUploadTime);
        return Result.success(aiKnowledgeDocumentService.page(page, wrapper));
    }

    @GetMapping("/all")
    public Result<List<AiKnowledgeDocument>> listAll() {
        return Result.success(aiKnowledgeDocumentService.list(
                new LambdaQueryWrapper<AiKnowledgeDocument>().eq(AiKnowledgeDocument::getStatus, 1).orderByDesc(AiKnowledgeDocument::getUploadTime)));
    }

    @GetMapping("/{id}")
    public Result<AiKnowledgeDocument> detail(@PathVariable Long id) {
        return Result.success(aiKnowledgeDocumentService.getById(id));
    }

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(aiKnowledgeDocumentService.listObjs(
                new LambdaQueryWrapper<AiKnowledgeDocument>()
                        .select(AiKnowledgeDocument::getCategory)
                        .eq(AiKnowledgeDocument::getStatus, 1)
                        .isNotNull(AiKnowledgeDocument::getCategory)
                        .groupBy(AiKnowledgeDocument::getCategory), o -> (String) o));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", aiKnowledgeDocumentService.count());
        stats.put("enabled", aiKnowledgeDocumentService.count(new LambdaQueryWrapper<AiKnowledgeDocument>().eq(AiKnowledgeDocument::getStatus, 1)));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "知识库管理", type = "新增", description = "新增文档")
    public Result<Void> save(@RequestBody AiKnowledgeDocument doc) {
        if (doc.getStatus() == null) doc.setStatus(1);
        if (doc.getUploadTime() == null) doc.setUploadTime(LocalDateTime.now());
        aiKnowledgeDocumentService.save(doc);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "知识库管理", type = "修改", description = "修改文档")
    public Result<Void> update(@RequestBody AiKnowledgeDocument doc) {
        aiKnowledgeDocumentService.updateById(doc);
        return Result.success();
    }

    @PutMapping("/toggle/{id}")
    @OperationLog(module = "知识库管理", type = "启用/禁用", description = "切换状态")
    public Result<Void> toggle(@PathVariable Long id) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentService.getById(id);
        if (doc == null) return Result.error("文档不存在");
        doc.setStatus(doc.getStatus() == 1 ? 0 : 1);
        aiKnowledgeDocumentService.updateById(doc);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "知识库管理", type = "删除", description = "删除文档")
    public Result<Void> delete(@PathVariable Long id) {
        aiKnowledgeDocumentService.removeById(id);
        return Result.success();
    }

    @PostMapping("/sync")
    @OperationLog(module = "知识库管理", type = "同步", description = "同步知识库")
    public Result<Void> sync() {
        // 知识库同步逻辑（可扩展为调用AI服务进行向量化等）
        return Result.success();
    }
}