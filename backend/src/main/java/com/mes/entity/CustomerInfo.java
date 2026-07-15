package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_info")
public class CustomerInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String customerCode;

    private String customerName;

    private String contactName;

    private String phone;

    private String email;

    private String address;

    private Integer status;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String customerNo;

    @TableField(exist = false)
    private String contact;
}
