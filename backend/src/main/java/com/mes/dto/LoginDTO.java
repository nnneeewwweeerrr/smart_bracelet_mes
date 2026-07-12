package com.mes.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

// utils/request.js
const request = (options) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token');
        uni.request({
            url: baseURL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: {
                'Content-Type': 'application/json',
                ...(token ? {'Authorization': 'Bearer ' + token} : {})
            },
            success: (res) => {
                if (res.data.code === 200) resolve(res.data);
                else reject(res.data);
            },
            fail: reject
        });
    });
};
