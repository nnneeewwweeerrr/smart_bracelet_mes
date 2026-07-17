package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quality_result")
public class QualityResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String checkItem;

    private String standardValue;

    private String actualValue;

    private Integer result;

    private String remark;
}
