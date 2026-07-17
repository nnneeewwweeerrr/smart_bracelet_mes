package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("process_sop")
public class ProcessSop {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long processId;

    private String sopName;

    private String sopVersion;

    private String operationSteps;

    private String precautions;

    private String equipmentRequirement;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
