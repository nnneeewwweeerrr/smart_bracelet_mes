package com.mes.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String realName;
    private String roleName;
}
