package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_warehouse")
public class DemoWarehouse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String warehouseCode;

    private String warehouseName;

    private String address;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}