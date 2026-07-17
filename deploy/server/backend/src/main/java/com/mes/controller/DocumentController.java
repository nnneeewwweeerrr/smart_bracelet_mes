package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.AiDocument;
import com.mes.service.AiDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/document")
public class DocumentController {

    @Autowired
    private AiDocumentService documentService;

    @GetMapping("/list")
    public Result<IPage<AiDocument>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) Integer status) {
        Page<AiDocument> page = new Page<>(current, size);
        return Result.success(documentService.pageList(page, fileName, status));
    }

    @GetMapping("/{id}")
    public Result<AiDocument> detail(@PathVariable Long id) {
        return Result.success(documentService.getById(id));
    }

    @PostMapping("/upload")
    public Result<Void> upload(@RequestParam("file") MultipartFile file,
                               @RequestParam("uploadedBy") String uploadedBy) {
        String fileName = file.getOriginalFilename();
        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            documentService.uploadDocument(fileName, content.toString(), uploadedBy);
            return Result.success();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<Void> create(@RequestBody Map<String, Object> params) {
        String fileName = params.get("fileName").toString();
        String content = params.get("content").toString();
        String uploadedBy = params.getOrDefault("uploadedBy", "admin").toString();
        documentService.uploadDocument(fileName, content, uploadedBy);
        return Result.success();
    }

    @PostMapping("/sync")
    public Result<Void> sync() {
        documentService.syncKnowledgeBase();
        return Result.success();
    }

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") Integer topK) {
        return Result.success(documentService.searchKnowledge(query, topK));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }

    @PostMapping("/sync-all")
    public Result<Void> syncAll() {
        documentService.syncKnowledgeBase();
        return Result.success();
    }
}
