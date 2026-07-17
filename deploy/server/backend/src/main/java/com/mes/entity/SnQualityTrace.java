package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sn_quality_trace")
public class SnQualityTrace {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long snId;

    private String snCode;

    private Long taskId;

    private String taskNo;

    private String taskType;

    private Long processId;

    private String processName;

    private String checkItem;

    private String standardValue;

    private String actualValue;

    private Integer result;

    private Long inspectorId;

    private String inspectorName;

    private String defectType;

    private String defectDesc;

    private LocalDateTime checkTime;

    private LocalDateTime createTime;
}
