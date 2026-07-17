package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_history")
public class AiChatHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private Long userId;

    private String userName;

    private String question;

    private String answer;

    private String model;

    private Integer responseTime;

    private Integer feedback;

    private Integer chatStatus;

    private String context;

    private String scene;

    private LocalDateTime chatTime;

    private LocalDateTime createTime;
}