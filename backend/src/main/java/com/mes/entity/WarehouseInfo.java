package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("warehouse_info")
public class WarehouseInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String warehouseName;

    private String warehouseCode;

    private String address;

    private Integer status;
}
