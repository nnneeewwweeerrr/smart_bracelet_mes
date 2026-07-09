package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exception_record")
public class ExceptionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String exceptionNo;

    private String exceptionType;

    private Long workOrderId;

    private String description;

    private Long reporterId;

    private Integer status;

    private LocalDateTime reportTime;

    private LocalDateTime handleTime;
}
