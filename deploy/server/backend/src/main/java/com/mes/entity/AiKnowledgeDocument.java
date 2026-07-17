package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_knowledge_document")
public class AiKnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docNo;

    private String docName;

    private String docType;

    private String category;

    private String filePath;

    private Long fileSize;

    private String content;

    private String summary;

    private String keywords;

    private String uploader;

    private LocalDateTime uploadTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}