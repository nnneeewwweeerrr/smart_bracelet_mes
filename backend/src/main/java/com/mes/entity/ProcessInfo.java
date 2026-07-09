package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("process_info")
public class ProcessInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String processName;

    private String processCode;

    private Long productId;

    private Integer sortOrder;
}
