package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("work_order")
public class WorkOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("work_no")
    private String workNo;

    @TableField("plan_id")
    private Long planId;

    @TableField("product_id")
    private Long productId;

    @TableField("process_id")
    private Long processId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("status")
    private Integer status;

    @TableField("manager")
    private String manager;

    @TableField("remark")
    private String remark;

    @TableField("plan_start_date")
    private LocalDate planStartDate;

    @TableField("plan_end_date")
    private LocalDate planEndDate;

    @TableField("create_time")
    private LocalDateTime createTime;

    // ===== 虚拟字段（前端展示用）=====
    @TableField(exist = false)
    private String workOrderNo;

    @TableField(exist = false)
    private String orderNo;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String spec;

    @TableField(exist = false)
    private Integer finishedQuantity;

    @TableField(exist = false)
    private List<?> processList;

    // ===== 以下为原有虚拟字段 =====
    @TableField(exist = false)
    private String taskNo;

    @TableField(exist = false)
    private Long dispatchId;

    @TableField(exist = false)
    private Long deviceId;

    @TableField(exist = false)
    private Long empId;

    @TableField(exist = false)
    private Integer planNum;

    @TableField(exist = false)
    private Integer finishedNum;

    @TableField(exist = false)
    private Integer defectNum;

    @TableField(exist = false)
    private Integer taskStatus;

    @TableField(exist = false)
    private LocalDateTime startTime;

    @TableField(exist = false)
    private LocalDateTime endTime;

    @TableField(exist = false)
    private String taskRemark;

    @TableField(exist = false)
    private LocalDateTime updateTime;
}
