package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("process_info")
public class ProcessInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("process_code")
    private String processCode;

    @TableField("process_name")
    private String processName;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(exist = false)
    private String processDesc;

    @TableField(exist = false)
    private Integer standardTime;

    @TableField(exist = false)
    private Integer sort;

    @TableField(exist = false)
    private Integer status;

    @TableField(exist = false)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Long productId;
}
