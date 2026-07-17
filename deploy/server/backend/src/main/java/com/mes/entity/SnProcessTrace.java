package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sn_process_trace")
public class SnProcessTrace {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long snId;

    private String snCode;

    private Long processId;

    private String processName;

    private String processCode;

    private Integer sortOrder;

    private Long workerId;

    private String workerName;

    private Long equipmentId;

    private String equipmentName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String processParams;

    private String remark;

    private LocalDateTime createTime;
}
