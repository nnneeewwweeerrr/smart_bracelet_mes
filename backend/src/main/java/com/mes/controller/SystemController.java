package com.mes.controller;

import com.mes.common.Result;
import com.mes.entity.SysPermission;
import com.mes.entity.SysRole;
import com.mes.entity.SysUser;
import com.mes.service.SysPermissionService;
import com.mes.service.SysRoleService;
import com.mes.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @GetMapping("/user/list")
    public Result<List<SysUser>> listUsers() {
        return Result.success(sysUserService.list());
    }

    @GetMapping("/user/{id}")
    public Result<SysUser> getUser(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @PostMapping("/user/save")
    public Result<Void> saveUser(@RequestBody SysUser user) {
        sysUserService.save(user);
        return Result.success();
    }

    @PutMapping("/user/update")
    public Result<Void> updateUser(@RequestBody SysUser user) {
        sysUserService.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/user/delete/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }

    @GetMapping("/role/list")
    public Result<List<SysRole>> listRoles() {
        return Result.success(sysRoleService.list());
    }

    @PostMapping("/role/save")
    public Result<Void> saveRole(@RequestBody SysRole role) {
        sysRoleService.save(role);
        return Result.success();
    }

    @PutMapping("/role/update")
    public Result<Void> updateRole(@RequestBody SysRole role) {
        sysRoleService.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/role/delete/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return Result.success();
    }

    @GetMapping("/permission/list")
    public Result<List<SysPermission>> listPermissions() {
        return Result.success(sysPermissionService.list());
    }

    @PostMapping("/permission/save")
    public Result<Void> savePermission(@RequestBody SysPermission permission) {
        sysPermissionService.save(permission);
        return Result.success();
    }

    @PutMapping("/permission/update")
    public Result<Void> updatePermission(@RequestBody SysPermission permission) {
        sysPermissionService.updateById(permission);
        return Result.success();
    }

    @DeleteMapping("/permission/delete/{id}")
    public Result<Void> deletePermission(@PathVariable Long id) {
        sysPermissionService.removeById(id);
        return Result.success();
    }
}
