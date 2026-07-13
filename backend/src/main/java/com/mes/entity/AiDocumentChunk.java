package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_document_chunk")
public class AiDocumentChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private String content;

    private String keywords;

    private Integer chunkIndex;

    private Integer totalChunks;

    private String embedding;

    private LocalDateTime createTime;
}
