package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("customer_info")
public class CustomerInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String customerNo;

    private String customerName;

    private String contact;

    private String phone;

    private String email;

    private String address;

    private String province;

    private String city;

    private String industry;

    private String level;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}