package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商信息
 */
@Data
@TableName("supplier_info")
public class SupplierInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String supplierName;

    private String contactName;

    private String phone;

    private String address;

    private Integer status;

    private LocalDateTime createTime;
}
