package com.mes.vo;

import lombok.Data;

@Data
public class TodoItemVO {

    private Long id;

    private String title;

    private String type;

    private String status;

    private String createTime;
}
