package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.SysUser;
import com.mes.mapper.SysUserMapper;
import com.mes.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = getOne(wrapper);
        if (user != null) {
            Long roleId = jdbcTemplate.queryForObject(
                    "SELECT role_id FROM sys_user_role WHERE user_id = ?",
                    Long.class,
                    user.getId()
            );
            user.setRoleId(roleId != null ? roleId : 1L);
        }
        return user;
    }
}