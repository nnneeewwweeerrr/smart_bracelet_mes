package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.AiDocument;
import com.mes.entity.AiDocumentChunk;
import com.mes.mapper.AiDocumentMapper;
import com.mes.mapper.AiDocumentChunkMapper;
import com.mes.service.AiDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiDocumentServiceImpl extends ServiceImpl<AiDocumentMapper, AiDocument> implements AiDocumentService {

    @Autowired
    private AiDocumentChunkMapper chunkMapper;

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;

    @Override
    public IPage<AiDocument> pageList(Page<AiDocument> page, String fileName, Integer status) {
        LambdaQueryWrapper<AiDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(fileName), AiDocument::getFileName, fileName)
                .eq(status != null, AiDocument::getStatus, status)
                .orderByDesc(AiDocument::getUploadTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void uploadDocument(String fileName, String content, String uploadedBy) {
        AiDocument document = new AiDocument();
        document.setFileName(fileName);
        document.setContent(content);
        document.setSummary(generateSummary(content));
        document.setFileType(getFileType(fileName));
        document.setStatus(1);
        document.setUploadedBy(uploadedBy);
        document.setUploadTime(LocalDateTime.now());
        baseMapper.insert(document);

        List<AiDocumentChunk> chunks = splitContent(content, document.getId());
        for (AiDocumentChunk chunk : chunks) {
            chunkMapper.insert(chunk);
        }

        log.info("文档上传成功: {}, 切分数: {}", fileName, chunks.size());
    }

    @Override
    public void syncKnowledgeBase() {
        List<AiDocument> docs = list();
        for (AiDocument doc : docs) {
            if (doc.getStatus() == 1 && doc.getContent() != null) {
                chunkMapper.delete(new LambdaQueryWrapper<AiDocumentChunk>()
                        .eq(AiDocumentChunk::getDocumentId, doc.getId()));

                List<AiDocumentChunk> chunks = splitContent(doc.getContent(), doc.getId());
                for (AiDocumentChunk chunk : chunks) {
                    chunkMapper.insert(chunk);
                }
                doc.setSyncTime(LocalDateTime.now());
                updateById(doc);
            }
        }
        log.info("知识库同步完成, 文档数: {}", docs.size());
    }

    @Override
    public List<Map<String, Object>> searchKnowledge(String query, int topK) {
        if (StringUtils.isEmpty(query)) {
            return Collections.emptyList();
        }

        List<AiDocumentChunk> allChunks = chunkMapper.selectList(null);
        if (allChunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (AiDocumentChunk chunk : allChunks) {
            double score = calculateSimilarity(query, chunk.getContent());
            if (score > 0.2) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("chunkId", chunk.getId());
                result.put("documentId", chunk.getDocumentId());
                result.put("content", chunk.getContent());
                result.put("keywords", chunk.getKeywords());
                result.put("score", score);

                AiDocument doc = getById(chunk.getDocumentId());
                if (doc != null) {
                    result.put("fileName", doc.getFileName());
                }
                results.add(result);
            }
        }

        results.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        chunkMapper.delete(new LambdaQueryWrapper<AiDocumentChunk>()
                .eq(AiDocumentChunk::getDocumentId, id));
        removeById(id);
    }

    private List<AiDocumentChunk> splitContent(String content, Long documentId) {
        List<AiDocumentChunk> chunks = new ArrayList<>();
        if (StringUtils.isEmpty(content)) {
            return chunks;
        }

        String[] paragraphs = content.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            if (currentChunk.length() + paragraph.length() > CHUNK_SIZE && currentChunk.length() > 0) {
                chunks.add(createChunk(currentChunk.toString().trim(), documentId, chunkIndex++));
                String overlap = currentChunk.substring(Math.max(0, currentChunk.length() - CHUNK_OVERLAP));
                currentChunk = new StringBuilder(overlap).append("\n").append(paragraph);
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(createChunk(currentChunk.toString().trim(), documentId, chunkIndex));
        }

        for (AiDocumentChunk chunk : chunks) {
            chunk.setTotalChunks(chunks.size());
        }

        return chunks;
    }

    private AiDocumentChunk createChunk(String content, Long documentId, int index) {
        AiDocumentChunk chunk = new AiDocumentChunk();
        chunk.setDocumentId(documentId);
        chunk.setContent(content);
        chunk.setKeywords(extractKeywords(content));
        chunk.setChunkIndex(index);
        chunk.setCreateTime(LocalDateTime.now());
        return chunk;
    }

    private String extractKeywords(String content) {
        List<String> keywords = new ArrayList<>();
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]{2,4}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            keywords.add(matcher.group());
        }

        Map<String, Integer> freq = new HashMap<>();
        for (String kw : keywords) {
            freq.merge(kw, 1, Integer::sum);
        }

        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(","));
    }

    private String generateSummary(String content) {
        if (content.length() <= 200) {
            return content;
        }
        return content.substring(0, 200) + "...";
    }

    private String getFileType(String fileName) {
        if (fileName.endsWith(".md")) return "markdown";
        if (fileName.endsWith(".txt")) return "text";
        if (fileName.endsWith(".docx")) return "docx";
        return "other";
    }

    private double calculateSimilarity(String query, String content) {
        Set<String> queryWords = extractWords(query);
        Set<String> contentWords = extractWords(content);

        if (queryWords.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(queryWords);
        intersection.retainAll(contentWords);

        return (double) intersection.size() / queryWords.size();
    }

    private Set<String> extractWords(String text) {
        Set<String> words = new HashSet<>();
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]{2,}|[a-zA-Z]{3,}");
        Matcher matcher = pattern.matcher(text.toLowerCase());
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }
}
