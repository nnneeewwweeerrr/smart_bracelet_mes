package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_document")
public class AiDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String filePath;

    private String fileType;

    private String content;

    private String summary;

    private Integer status;

    private String uploadedBy;

    private LocalDateTime uploadTime;

    private LocalDateTime syncTime;
}
