package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supplier_info")
public class SupplierInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String supplierNo;

    private String supplierName;

    private String contact;

    private String phone;

    private String email;

    private String address;

    private String province;

    private String city;

    private String level;

    private String creditRating;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}