package com.mes.controller;

import com.mes.common.Result;
import com.mes.dto.LoginDTO;
import com.mes.entity.SysRole;
import com.mes.entity.SysUser;
import com.mes.service.SysRoleService;
import com.mes.service.SysUserService;
import com.mes.utils.JwtUtil;
import com.mes.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        String token = jwtUtil.generateToken(authentication.getName());

        SysUser user = sysUserService.getByUsername(authentication.getName());
        SysRole role = sysRoleService.getById(user.getRoleId());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(86400L);
        loginVO.setUsername(user.getUserName());
        loginVO.setRealName(user.getRealName());
        loginVO.setRoleName(role != null ? role.getRoleName() : null);

        return Result.success(loginVO);
    }

    @GetMapping("/info")
    public Result<SysUser> info() {
        return Result.success(new SysUser());
    }

    @PostMapping("/init-password")
    public Result<Void> initPassword() {
        SysUser admin = sysUserService.getByUsername("admin");
        if (admin != null) {
            admin.setPassword(passwordEncoder.encode("123456"));
            sysUserService.updateById(admin);
        }
        SysUser worker = sysUserService.getByUsername("worker001");
        if (worker != null) {
            worker.setPassword(passwordEncoder.encode("123456"));
            sysUserService.updateById(worker);
        }
        return Result.success();
    }
}
