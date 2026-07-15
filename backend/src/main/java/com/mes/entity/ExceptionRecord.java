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

    @TableField("exception_no")
    private String exceptionNo;

    @TableField("task_id")
    private Long taskId;

    @TableField("exception_type")
    private String exceptionType;

    @TableField("exception_level")
    private Integer exceptionLevel;

    @TableField("exception_desc")
    private String exceptionDesc;

    private String reporter;

    @TableField("report_time")
    private LocalDateTime reportTime;

    private String handler;

    @TableField("handle_content")
    private String handleContent;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("exception_status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    private Long workOrderId;

    private String description;

    private Long handlerId;

    private String handleReport;

    private String handleResult;

    private String imageUrls;

    private String findProcess;

    private String findOperator;

    private String handlerName;

    private String reporterName;
}
