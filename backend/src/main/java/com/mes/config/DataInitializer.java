package com.mes.config;

import com.mes.entity.SysUser;
import com.mes.service.AiDocumentService;
import com.mes.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AiDocumentService aiDocumentService;

    @Override
    public void run(String... args) {
        try {
            initAiChatHistoryTable();
        } catch (Exception e) {
            log.warn("AI对话表初始化失败: {}", e.getMessage());
        }

        try {
            initAiDocumentTables();
            initSopDocuments();
        } catch (Exception e) {
            log.warn("AI文档表初始化失败: {}", e.getMessage());
        }

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

    private void initAiChatHistoryTable() throws SQLException {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS `ai_chat_history` (
                    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                    `user_id` VARCHAR(100) COMMENT '用户ID',
                    `user_name` VARCHAR(50) COMMENT '用户名',
                    `question` TEXT NOT NULL COMMENT '用户问题',
                    `answer` TEXT COMMENT 'AI回答',
                    `context` TEXT COMMENT '上下文信息JSON',
                    `chat_status` INT DEFAULT 0 COMMENT '状态(0成功,1失败)',
                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    INDEX `idx_user_id` (`user_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表'
                """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            log.info("AI对话表 ai_chat_history 初始化成功");
        }
    }

    /**
     * 创建AI文档表和文档切片表
     */
    private void initAiDocumentTables() throws SQLException {
        String createDocumentSql = """
                CREATE TABLE IF NOT EXISTS `ai_document` (
                    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                    `file_name` VARCHAR(255) COMMENT '文件名',
                    `file_path` VARCHAR(500) COMMENT '文件路径',
                    `file_type` VARCHAR(50) COMMENT '文件类型',
                    `content` TEXT COMMENT '文件内容',
                    `summary` TEXT COMMENT '摘要',
                    `status` INT DEFAULT 1 COMMENT '状态(1已同步,0待同步)',
                    `uploaded_by` VARCHAR(50) COMMENT '上传人',
                    `upload_time` DATETIME COMMENT '上传时间',
                    `sync_time` DATETIME COMMENT '同步时间'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识文档表'
                """;

        String createChunkSql = """
                CREATE TABLE IF NOT EXISTS `ai_document_chunk` (
                    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                    `document_id` BIGINT COMMENT '文档ID',
                    `content` TEXT COMMENT '切片内容',
                    `keywords` VARCHAR(500) COMMENT '关键词',
                    `chunk_index` INT COMMENT '切片序号',
                    `total_chunks` INT COMMENT '总切片数',
                    `embedding` TEXT COMMENT '向量 embedding',
                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    INDEX `idx_document_id` (`document_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI文档切片表'
                """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createDocumentSql);
            stmt.execute(createChunkSql);
            log.info("AI文档表 ai_document / ai_document_chunk 初始化成功");
        }
    }

    /**
     * 预置SOP文档数据（仅在表为空时插入）
     */
    private void initSopDocuments() throws SQLException {
        // 检查是否已有文档数据
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ai_document");
            if (rs.next() && rs.getInt(1) > 0) {
                log.info("AI文档表已有数据，跳过SOP预置");
                return;
            }
        }

        String[][] sops = {
                {"SMT贴片工艺SOP.md",
                        """
                        SMT贴片工艺标准操作规程(SOP)

                        1. 适用范围
                        适用于智能手环主板SMT贴片工序，包括0402/0603电阻电容、QFP/BGA集成电路的贴装。

                        2. 设备与物料
                        - 设备：SMT全自动贴片机、锡膏印刷机、SPI检测仪、回流焊炉、AOI检测仪
                        - 物料：PCB板、焊膏(锡铅合金)、元器件、钢网

                        3. 操作步骤
                        3.1 锡膏印刷
                        - 检查锡膏回温状态，回温时间≥4小时
                        - 钢网对位，印刷压力0.2-0.3MPa
                        - 印刷后检查锡膏厚度，标准0.12-0.15mm

                        3.2 贴片
                        - 装载贴片程序，校准吸嘴和飞达
                        - 贴片精度要求：±0.05mm
                        - 贴片速度根据元件类型调整
                        - 首件必须100%全检确认

                        3.3 回流焊接
                        - 预热区温度：150-180°C
                        - 回流区温度：240-260°C
                        - 焊接时间：60-90秒
                        - 冷却方式：风冷降温

                        4. 质量检验
                        - SPI检测锡膏印刷质量
                        - AOI检测贴片偏移、缺件、错件
                        - 首件确认合格后方可批量生产

                        5. 常见不良及处理
                        - 偏移：校准贴片坐标，检查PCB定位
                        - 缺件：检查飞达供料状态，补充物料
                        - 锡膏不良：调整印刷参数，更换锡膏
                        """
                },
                {"回流焊接工艺SOP.md",
                        """
                        回流焊接工艺标准操作规程(SOP)

                        1. 适用范围
                        适用于智能手环PCB板回流焊接工序。

                        2. 设备
                        - 八温区回流焊炉
                        - 温度曲线测试仪
                        - KIC温度记录仪

                        3. 温度曲线参数
                        3.1 预热区
                        - 温度范围：150-180°C
                        - 升温速率：1-3°C/s
                        - 作用：挥发溶剂，激活助焊剂

                        3.2 均热区
                        - 温度范围：170-190°C
                        - 保温时间：60-90秒
                        - 作用：使PCB和元件温度均匀

                        3.3 回流区
                        - 峰值温度：240-260°C
                        - 液相线以上时间：30-60秒
                        - 作用：形成良好焊点

                        3.4 冷却区
                        - 冷却方式：风冷
                        - 降温速率：2-4°C/s

                        4. 操作步骤
                        4.1 开机前检查设备状态和氮气供应
                        4.2 设置温度曲线参数
                        4.3 用温度测试仪验证曲线
                        4.4 首件过炉并检测焊接质量
                        4.5 批量生产，每2小时抽检一次

                        5. 常见不良及处理
                        - 虚焊：检查温度曲线，提高峰值温度
                        - 连锡：调整锡膏量，优化钢网开孔
                        - 立碑：检查两端受热是否均匀
                        - 锡珠：优化预热区升温速率
                        """
                },
                {"屏幕贴合工艺SOP.md",
                        """
                        屏幕贴合工艺标准操作规程(SOP)

                        1. 适用范围
                        适用于智能手环OLED/LCD屏幕与TP触摸屏的贴合工序。

                        2. 设备与辅料
                        - 设备：真空贴合机、除泡机、UV固化机
                        - 辅料：OCA光学胶、无尘布、无尘手套

                        3. 工艺参数
                        - 贴合方式：真空贴合
                        - 贴合压力：0.4-0.6MPa
                        - 贴合温度：40-60°C
                        - 真空度：≤-95kPa
                        - 保压时间：10-15秒
                        - UV固化时间：30-60秒

                        4. 操作步骤
                        4.1 在无尘室(洁净度≥1000级)中操作
                        4.2 清洁屏幕和TP触摸屏，用无尘布擦拭
                        4.3 贴附OCA光学胶，排除气泡
                        4.4 放入真空贴合机，设定参数
                        4.5 启动贴合程序，监控真空度和压力
                        4.6 取出贴合件，检查贴合质量
                        4.7 UV固化处理
                        4.8 放入除泡机，60°C除泡30分钟

                        5. 质量检验
                        - 目视检查：无气泡、无偏移、无划伤
                        - 显示检查：点亮屏幕检查显示效果
                        - 触摸检查：测试触摸灵敏度

                        6. 常见不良及处理
                        - 气泡：检查清洁度，延长保压时间，重新除泡
                        - 偏移：校准贴合定位治具
                        - 气泡残留：检查OCA胶质量，调整贴合参数
                        - 异物：加强无尘室管理，规范操作流程
                        """
                },
                {"防水测试SOP.md",
                        """
                        防水测试标准操作规程(SOP)

                        1. 适用范围
                        适用于智能手环成品的防水性能测试，确保产品达到IP68防水等级。

                        2. 设备
                        - 防水测试罐（气密性检测仪）
                        - 水压测试设备
                        - 烘干设备

                        3. 测试标准
                        - 防水等级：IP68
                        - 测试水压：1.5米水深
                        - 测试时间：30分钟
                        - 判定标准：无进水、功能正常

                        4. 气密性测试参数
                        - 充气压力：50kPa
                        - 稳压时间：5秒
                        - 测试时间：10秒
                        - 合格判定：泄漏值≤50Pa

                        5. 操作步骤
                        5.1 气密性测试
                        - 将产品放入测试夹具，密封接口
                        - 启动气密性测试仪，充气加压
                        - 稳压后检测泄漏值
                        - 泄漏值≤50Pa为合格，进入水压测试
                        - 不合格直接判定为防水不良

                        5.2 水压测试
                        - 将合格品放入防水测试罐
                        - 加压至1.5米水深压力
                        - 保持30分钟
                        - 取出后烘干

                        5.3 功能复测
                        - 烘干后检查屏幕显示
                        - 测试蓝牙连接、心率监测、触屏功能
                        - 全部功能正常为合格

                        6. 常见不良及处理
                        - 气密性不合格：检查密封圈、壳体结合处
                        - 进水：检查防水胶、按键密封
                        - 功能异常：烘干后复测，仍异常需维修
                        """
                },
                {"功能测试SOP.md",
                        """
                        功能测试标准操作规程(SOP)

                        1. 适用范围
                        适用于智能手环成品的功能测试，确保所有功能模块正常工作。

                        2. 测试设备
                        - 功能测试治具
                        - 蓝牙测试仪
                        - 心率模拟仪
                        - 充电测试仪

                        3. 测试项目与标准
                        3.1 蓝牙连接测试
                        - 蓝牙版本：5.0
                        - 连接距离：≥10米
                        - 连接稳定性：30秒内不断连

                        3.2 心率监测测试
                        - 测试范围：30-200bpm
                        - 误差范围：±5bpm
                        - 响应时间：≤10秒

                        3.3 运动计步测试
                        - 计步精度：±5%
                        - 最小步数检测：1步
                        - 测试方式：模拟步行100步

                        3.4 触屏响应测试
                        - 触摸灵敏度：≤50ms响应
                        - 测试点：屏幕9点触控
                        - 滑动测试：上下左右滑动

                        3.5 电池续航测试
                        - 待机时间：≥7天
                        - 工作时间：≥3天
                        - 充电时间：≤2小时充满

                        4. 操作步骤
                        4.1 将产品放入功能测试治具
                        4.2 启动测试程序，自动执行各项测试
                        4.3 记录测试数据，系统自动判定
                        4.4 全部项目合格为良品
                        4.5 不合格品贴标签并隔离，记录不良项

                        5. 常见不良及处理
                        - 蓝牙无法连接：检查天线焊接，重新烧录固件
                        - 心率不准：清洁传感器，校准参数
                        - 触屏失灵：检查屏幕排线，重新贴合
                        - 续航不足：更换电池，检查功耗
                        """
                }
        };

        try {
            for (String[] sop : sops) {
                aiDocumentService.uploadDocument(sop[0], sop[1], "system");
            }
            log.info("SOP文档预置完成，共 {} 份", sops.length);
        } catch (Exception e) {
            log.warn("SOP文档预置失败: {}", e.getMessage());
        }
    }
}
