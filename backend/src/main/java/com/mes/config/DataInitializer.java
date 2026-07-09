package com.mes.config;

import com.mes.entity.SysUser;
import com.mes.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 启动时数据初始化器
 * 检测默认密码是否已设置，如未设置则初始化
 */
@Slf4j
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            String[] defaultUsernames = {"admin", "order_clerk", "purchase_clerk", "warehouse_keeper",
                    "production_manager", "quality_inspector", "worker001", "worker002", "worker003"};
            int updated = 0;
            for (String username : defaultUsernames) {
                SysUser user = sysUserService.getByUsername(username);
                if (user != null && (user.getPassword() == null || user.getPassword().length() < 50)) {
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setUpdateTime(LocalDateTime.now());
                    sysUserService.updateById(user);
                    updated++;
                }
            }
            if (updated > 0) {
                log.info("默认账号密码已初始化，共更新 {} 个账号，默认密码: 123456", updated);
            }
        } catch (Exception e) {
            log.warn("数据初始化失败: {}", e.getMessage());
        }
    }
}
