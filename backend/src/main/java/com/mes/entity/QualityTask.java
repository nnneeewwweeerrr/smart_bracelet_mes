package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quality_task")
public class QualityTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    private String taskType;

    private Long sourceId;

    private Long inspectorId;

    private Integer status;

    private LocalDateTime createTime;
}
