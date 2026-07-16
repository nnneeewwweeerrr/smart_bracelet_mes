package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exception_record")
public class ExceptionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String exceptionNo;

    private String exceptionType;

    private String exceptionLevel;

    private String sourceType;

    private String sourceNo;

    private String workOrderNo;

    private String title;

    private String description;

    private String reporter;

    private Long reporterId;

    private LocalDateTime reportTime;

    private String handler;

    private Long handlerId;

    private LocalDateTime assignTime;

    private String handleContent;

    private LocalDateTime handleTime;

    private String handleResult;

    private String causeAnalysis;

    private String preventiveAction;

    private String imageUrls;

    private Integer priority;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String handlerName;

    @TableField(exist = false)
    private String reporterName;
}
