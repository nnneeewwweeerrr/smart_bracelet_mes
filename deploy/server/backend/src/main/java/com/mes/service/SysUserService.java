package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser getByUsername(String username);
}
