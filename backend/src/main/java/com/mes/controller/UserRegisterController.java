package com.mes.controller;

import com.mes.common.Result;
import com.mes.dto.RegisterDTO;
import com.mes.entity.SysUser;
import com.mes.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
public class UserRegisterController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Validated RegisterDTO registerDTO) {
        SysUser existUser = sysUserService.getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return Result.error(400, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUserName(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setRoleId(2L);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());

        sysUserService.save(user);
        return Result.success();
    }
}
