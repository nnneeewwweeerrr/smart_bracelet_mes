-- ============================================================
-- 智能手环MES系统 - 剩余模块数据库重建脚本
-- 包含：设备故障、库存盘点、库存预警、质检记录、不合格品、知识库等
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 设备故障管理（已存在，重建）
DROP TABLE IF EXISTS `equipment_fault`;
-- 库存盘点表（新增）
DROP TABLE IF EXISTS `stock_check`;
-- 库存预警表（新增）
DROP TABLE IF EXISTS `stock_alert`;
-- 不合格品管理表（新增）
DROP TABLE IF EXISTS `unqualified_product`;
-- 系统设置表（已存在，保留）
-- 操作日志表（已存在，保留）
-- AI知识库文档表（已存在，重建）
DROP TABLE IF EXISTS `ai_knowledge_document`;
-- AI问答记录表（已存在，重建）
DROP TABLE IF EXISTS `ai_chat_history`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. 设备故障管理表 (equipment_fault)
-- ============================================================
CREATE TABLE IF NOT EXISTS `equipment_fault` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '故障ID',
    `fault_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '故障单号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `fault_type` VARCHAR(50) COMMENT '故障类型(电气故障/机械故障/软件故障/其他)',
    `fault_level` VARCHAR(20) COMMENT '故障等级(紧急/一般/轻微)',
    `fault_desc` VARCHAR(1000) COMMENT '故障描述',
    `reporter` VARCHAR(50) COMMENT '报修人',
    `report_time` DATETIME COMMENT '报修时间',
    `assignee` VARCHAR(50) COMMENT '派工人员',
    `assign_time` DATETIME COMMENT '派工时间',
    `solution` VARCHAR(1000) COMMENT '处理方案',
    `status` INT DEFAULT 0 COMMENT '状态(0待处理/1已派工/2已修复/3已关闭)',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_fault_no` (`fault_no`),
    INDEX `idx_equipment_id` (`equipment_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障管理表';

-- ============================================================
-- 2. 库存盘点表 (stock_check)
-- ============================================================
CREATE TABLE IF NOT EXISTS `stock_check` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '盘点ID',
    `check_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '盘点单号',
    `check_type` VARCHAR(20) COMMENT '盘点类型(全盘/抽盘/循环盘)',
    `warehouse` VARCHAR(50) COMMENT '盘点仓库',
    `check_date` DATE COMMENT '盘点日期',
    `total_items` INT DEFAULT 0 COMMENT '盘点物料总数',
    `match_items` INT DEFAULT 0 COMMENT '账实相符数',
    `diff_items` INT DEFAULT 0 COMMENT '盘盈盘亏数',
    `checker` VARCHAR(50) COMMENT '盘点人',
    `supervisor` VARCHAR(50) COMMENT '监盘人',
    `status` INT DEFAULT 0 COMMENT '状态(0待盘点/1盘点中/2已完成/3已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_check_no` (`check_no`),
    INDEX `idx_check_date` (`check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点表';

-- ============================================================
-- 3. 库存盘点明细表 (stock_check_detail)
-- ============================================================
CREATE TABLE IF NOT EXISTS `stock_check_detail` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    `check_id` BIGINT COMMENT '盘点单ID',
    `check_no` VARCHAR(50) COMMENT '盘点单号',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `spec` VARCHAR(100) COMMENT '规格型号',
    `unit` VARCHAR(20) COMMENT '单位',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `book_quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '账面数量',
    `actual_quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '实际数量',
    `diff_quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '差异数量',
    `diff_type` VARCHAR(20) COMMENT '差异类型(盘盈/盘亏/相符)',
    `diff_reason` VARCHAR(200) COMMENT '差异原因',
    `status` INT DEFAULT 0 COMMENT '状态',
    `remark` VARCHAR(200) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_check_id` (`check_id`),
    INDEX `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点明细表';

-- ============================================================
-- 4. 库存预警表 (stock_alert)
-- ============================================================
CREATE TABLE IF NOT EXISTS `stock_alert` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预警ID',
    `alert_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '预警单号',
    `alert_type` VARCHAR(20) COMMENT '预警类型(库存不足/库存超储/库存积压/效期预警)',
    `alert_level` VARCHAR(20) COMMENT '预警等级(高/中/低)',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse` VARCHAR(50) COMMENT '仓库',
    `current_quantity` DECIMAL(10,2) COMMENT '当前库存',
    `safe_quantity` DECIMAL(10,2) COMMENT '安全库存',
    `alert_quantity` DECIMAL(10,2) COMMENT '预警阈值',
    `alert_time` DATETIME COMMENT '预警时间',
    `handler` VARCHAR(50) COMMENT '处理人',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_result` VARCHAR(200) COMMENT '处理结果',
    `status` INT DEFAULT 0 COMMENT '状态(0待处理/1处理中/2已处理/3已关闭)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_alert_no` (`alert_no`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警表';

-- ============================================================
-- 5. 不合格品管理表 (unqualified_product)
-- ============================================================
CREATE TABLE IF NOT EXISTS `unqualified_product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '不合格品ID',
    `unqualified_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '不合格单号',
    `source_type` VARCHAR(20) COMMENT '来源类型(来料检验/过程检验/成品检验/客户退货)',
    `source_no` VARCHAR(50) COMMENT '来源单号',
    `work_order_no` VARCHAR(50) COMMENT '关联工单',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '不合格数量',
    `defect_type` VARCHAR(50) COMMENT '缺陷类型',
    `defect_desc` VARCHAR(500) COMMENT '缺陷描述',
    `discovery_time` DATETIME COMMENT '发现时间',
    `discoverer` VARCHAR(50) COMMENT '发现人',
    `disposition` VARCHAR(20) COMMENT '处置方式(返工/返修/报废/退货/让步接收)',
    `disposition_result` VARCHAR(200) COMMENT '处置结果',
    `responsible` VARCHAR(50) COMMENT '责任人',
    `rectify_action` VARCHAR(500) COMMENT '纠正措施',
    `status` INT DEFAULT 0 COMMENT '状态(0待处置/1处置中/2已完成/3已关闭)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_unqualified_no` (`unqualified_no`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品管理表';

-- ============================================================
-- 6. AI知识库文档表 (ai_knowledge_document)
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_knowledge_document` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    `doc_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '文档编号',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '文档名称',
    `doc_type` VARCHAR(20) COMMENT '文档类型(PDF/WORD/EXCEL/TEXT/OTHER)',
    `category` VARCHAR(50) COMMENT '文档分类',
    `file_path` VARCHAR(500) COMMENT '文件路径',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `content` TEXT COMMENT '文档内容',
    `summary` VARCHAR(500) COMMENT '文档摘要',
    `keywords` VARCHAR(200) COMMENT '关键词',
    `uploader` VARCHAR(50) COMMENT '上传人',
    `upload_time` DATETIME COMMENT '上传时间',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用/1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_doc_no` (`doc_no`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库文档表';

-- ============================================================
-- 7. AI问答历史表 (ai_chat_history)
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_chat_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `session_id` VARCHAR(50) COMMENT '会话ID',
    `user_id` BIGINT COMMENT '用户ID',
    `user_name` VARCHAR(50) COMMENT '用户名',
    `question` VARCHAR(2000) COMMENT '问题',
    `answer` TEXT COMMENT '回答',
    `model` VARCHAR(50) COMMENT 'AI模型',
    `response_time` INT DEFAULT 0 COMMENT '响应时间(毫秒)',
    `feedback` INT COMMENT '反馈(1好评/-1差评)',
    `chat_time` DATETIME COMMENT '对话时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_chat_time` (`chat_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI问答历史表';

-- ============================================================
-- 数据初始化
-- ============================================================

-- 1. 设备故障数据 (10条)
INSERT INTO `equipment_fault` (`id`, `fault_no`, `equipment_id`, `equipment_no`, `equipment_name`, `fault_type`, `fault_level`, `fault_desc`, `reporter`, `report_time`, `assignee`, `assign_time`, `solution`, `status`, `cost`, `remark`) VALUES
(1, 'EF202606001', 1, 'EQ001', '贴片机', '机械故障', '紧急', '贴装头X轴运动异常报警', '操作工小李', '2026-06-15 08:30:00', '张工程师', '2026-06-15 08:45:00', '更换X轴电机驱动板', 2, 1500.00, '已修复'),
(2, 'EF202606002', 2, 'EQ002', '回流焊炉', '电气故障', '一般', '炉温波动超出工艺要求', '操作工小王', '2026-06-18 10:00:00', '张工程师', '2026-06-18 10:30:00', '校准温控模块', 2, 800.00, '已修复'),
(3, 'EF202606003', 4, 'EQ004', '注塑机', '机械故障', '紧急', '合模位置偏差0.2mm', '操作工小张', '2026-06-20 14:00:00', '王工程师', '2026-06-20 14:30:00', '调整模板平行度', 2, 1200.00, '已修复'),
(4, 'EF202606004', 8, 'EQ008', '气密性测试仪', '电气故障', '一般', '压力传感器异常', '操作工小李', '2026-06-25 09:00:00', '周工程师', '2026-06-25 09:30:00', '更换压力传感器', 2, 2500.00, '已修复'),
(5, 'EF202607001', 11, 'EQ011', '老化测试架', '电气故障', '一般', '第3层测试位无电源输出', '操作工小王', '2026-07-02 11:00:00', '周工程师', '2026-07-02 11:30:00', '更换电源模块', 2, 1500.00, '已修复'),
(6, 'EF202607002', 6, 'EQ006', '激光打标机', '光学故障', '轻微', '打标位置偏移', '操作工小赵', '2026-07-05 15:00:00', '钱工程师', '2026-07-05 15:30:00', '调整振镜校准', 2, 0.00, '已修复'),
(7, 'EF202607003', 10, 'EQ010', '包装机', '机械故障', '一般', '封口不牢', '操作工小周', '2026-07-08 10:00:00', '郑工程师', '2026-07-08 10:30:00', '更换加热块', 2, 600.00, '已修复'),
(8, 'EF202607004', 9, 'EQ009', '喷涂机器人', '程序故障', '一般', '喷涂路径异常', '操作工小吴', '2026-07-10 14:00:00', '吴工程师', '2026-07-10 14:30:00', '重新编程示教', 2, 0.00, '已修复'),
(9, 'EF202607005', 1, 'EQ001', '贴片机', '软件故障', '轻微', '视觉系统识别错误', '操作工小李', '2026-07-12 16:00:00', NULL, NULL, NULL, 0, 0.00, '待派工'),
(10, 'EF202607006', 3, 'EQ003', 'AOI光学检测仪', '软件故障', '轻微', '检测软件卡顿', '操作工小冯', '2026-07-13 15:00:00', '李工程师', '2026-07-13 15:30:00', '重装检测软件', 2, 0.00, '已修复');

-- 2. 库存盘点数据 (8条)
INSERT INTO `stock_check` (`id`, `check_no`, `check_type`, `warehouse`, `check_date`, `total_items`, `match_items`, `diff_items`, `checker`, `supervisor`, `status`, `remark`) VALUES
(1, 'CK202606001', '全盘', '原材料仓', '2026-06-15', 50, 48, 2, '张仓管', '李主管', 2, '正常盘点'),
(2, 'CK202606002', '抽盘', '成品仓', '2026-06-20', 30, 30, 0, '李仓管', '张主管', 2, '抽盘无差异'),
(3, 'CK202606003', '循环盘', '原材料仓', '2026-06-25', 20, 19, 1, '张仓管', '王主管', 2, '循环盘点'),
(4, 'CK202607001', '全盘', '原材料仓', '2026-07-01', 55, 53, 2, '张仓管', '李主管', 2, '月度盘点'),
(5, 'CK202607002', '抽盘', '半成品仓', '2026-07-05', 25, 25, 0, '王仓管', '张主管', 2, '抽盘无差异'),
(6, 'CK202607003', '循环盘', '成品仓', '2026-07-10', 35, 34, 1, '李仓管', '王主管', 2, '循环盘点'),
(7, 'CK202607004', '全盘', '辅料仓', '2026-07-12', 40, 39, 1, '王仓管', '李主管', 2, '辅料盘点'),
(8, 'CK202607005', '抽盘', '原材料仓', '2026-07-15', 20, 20, 0, '张仓管', '王主管', 1, '盘点中');

-- 3. 库存预警数据 (10条)
INSERT INTO `stock_alert` (`id`, `alert_no`, `alert_type`, `alert_level`, `material_id`, `material_no`, `material_name`, `warehouse`, `current_quantity`, `safe_quantity`, `alert_quantity`, `alert_time`, `handler`, `handle_time`, `handle_result`, `status`, `remark`) VALUES
(1, 'SA202606001', '库存不足', '高', 3, 'MAT003', 'OLED屏幕', '原材料仓', 500.00, 1000.00, 800.00, '2026-06-18 09:00:00', '张仓管', '2026-06-18 14:00:00', '已下单补货', 2, '紧急补货'),
(2, 'SA202606002', '库存超储', '中', 10, 'MAT010', '包装盒', '成品仓', 25000.00, 15000.00, 20000.00, '2026-06-20 10:00:00', '李仓管', '2026-06-21 15:00:00', '调整采购计划', 2, '超储处理'),
(3, 'SA202606003', '库存不足', '高', 1, 'MAT001', '蓝牙芯片', '原材料仓', 800.00, 2000.00, 1000.00, '2026-06-25 11:00:00', '张仓管', '2026-06-26 09:00:00', '已补货', 2, '紧急补货'),
(4, 'SA202607001', '库存不足', '高', 7, 'MAT007', '心率传感器', '原材料仓', 600.00, 1500.00, 800.00, '2026-07-02 08:00:00', '张仓管', NULL, NULL, 0, '待处理'),
(5, 'SA202607002', '库存超储', '低', 5, 'MAT005', '塑胶外壳', '成品仓', 12000.00, 8000.00, 10000.00, '2026-07-05 09:00:00', '李仓管', NULL, NULL, 0, '待处理'),
(6, 'SA202607003', '效期预警', '高', 9, 'MAT009', '锡膏', '原材料仓', 30.00, 20.00, 25.00, '2026-07-08 10:00:00', '张仓管', '2026-07-09 14:00:00', '已安排使用', 2, '效期提醒'),
(7, 'SA202607004', '库存不足', '中', 4, 'MAT004', '锂电池', '原材料仓', 1500.00, 3000.00, 2000.00, '2026-07-10 11:00:00', '张仓管', NULL, NULL, 0, '待补货'),
(8, 'SA202607005', '库存积压', '中', 6, 'MAT006', '硅胶表带', '成品仓', 15000.00, 8000.00, 12000.00, '2026-07-12 09:00:00', '李仓管', NULL, NULL, 0, '积压提醒'),
(9, 'SA202607006', '库存不足', '高', 8, 'MAT008', '六轴传感器', '原材料仓', 800.00, 2000.00, 1000.00, '2026-07-14 08:00:00', '张仓管', NULL, NULL, 0, '待处理'),
(10, 'SA202607007', '效期预警', '中', 11, 'MAT011', 'USB充电线', '成品仓', 4500.00, 3000.00, 4000.00, '2026-07-15 10:00:00', '李仓管', NULL, NULL, 0, '效期预警');

-- 4. 不合格品数据 (10条)
INSERT INTO `unqualified_product` (`id`, `unqualified_no`, `source_type`, `source_no`, `work_order_no`, `material_id`, `material_no`, `material_name`, `batch_no`, `quantity`, `defect_type`, `defect_desc`, `discovery_time`, `discoverer`, `disposition`, `disposition_result`, `responsible`, `rectify_action`, `status`, `remark`) VALUES
(1, 'UQ202606001', '来料检验', 'IN202606003', NULL, 3, 'MAT003', 'OLED屏幕', 'OLED202606001', 15.00, '外观缺陷', '屏幕表面有划痕', '2026-06-03 14:30:00', '质检员张三', '退货', '已退回供应商', '供应商', '加强来料检验', 2, '已处理'),
(2, 'UQ202606002', '过程检验', 'QC202606001', 'WO202606001', 1, 'MAT001', '蓝牙芯片', 'BT202606001', 5.00, '焊接不良', '芯片引脚虚焊', '2026-06-08 10:00:00', '质检员李四', '返修', '已返修合格', 'SMT车间', '调整回流焊参数', 2, '已处理'),
(3, 'UQ202606003', '成品检验', 'QC202606002', 'WO202606002', 5, 'MAT005', '塑胶外壳', 'SH202606001', 8.00, '外观缺陷', '外壳表面有划伤', '2026-06-15 16:00:00', '质检员王五', '返工', '已返工合格', '组装车间', '优化传送带清洁', 2, '已处理'),
(4, 'UQ202606004', '来料检验', 'IN202606005', NULL, 5, 'MAT005', '塑胶外壳', 'SH202606001', 20.00, '尺寸偏差', '外壳尺寸超差', '2026-06-08 11:30:00', '质检员张三', '退货', '已退回供应商', '供应商', '加强尺寸检验', 2, '已处理'),
(5, 'UQ202606005', '过程检验', 'QC202606003', 'WO202606003', 7, 'MAT007', '心率传感器', 'HR202606001', 3.00, '焊接不良', '传感器引脚虚焊', '2026-06-18 14:00:00', '质检员李四', '返修', '已返修合格', '组装车间', '培训操作员', 2, '已处理'),
(6, 'UQ202607001', '客户退货', 'RT202607001', NULL, 1, 'MAT001', '蓝牙芯片', 'BT202606001', 2.00, '功能不良', '蓝牙连接不稳定', '2026-07-01 09:00:00', '质检员张三', '报废', '已报废处理', 'SMT车间', '分析根本原因', 2, '客户退货'),
(7, 'UQ202607002', '来料检验', 'IN202606008', NULL, 8, 'MAT008', '六轴传感器', 'MO202606001', 10.00, '包装破损', '物料包装破损', '2026-07-05 10:00:00', '质检员李四', '退货', '已退回供应商', '供应商', '加强包装检验', 2, '已处理'),
(8, 'UQ202607003', '过程检验', 'QC202607001', 'WO202606007', 10, 'MAT010', '包装盒', 'PB202606001', 50.00, '印刷不良', '包装盒印刷模糊', '2026-07-08 11:00:00', '质检员王五', '返工', '已返工合格', '包装车间', '更换印刷模板', 2, '已处理'),
(9, 'UQ202607004', '成品检验', 'QC202607002', 'WO202606008', 11, 'MAT011', 'USB充电线', 'US202606001', 10.00, '功能不良', '充电线接触不良', '2026-07-12 15:00:00', '质检员张三', '报废', '已报废处理', '组装车间', '更换供应商', 2, '已处理'),
(10, 'UQ202607005', '过程检验', 'QC202607003', 'WO202606010', 7, 'MAT007', '心率传感器', 'HR202606001', 2.00, '焊接不良', '传感器焊接不良', '2026-07-15 10:00:00', '质检员李四', NULL, NULL, NULL, NULL, 0, '待处置');

-- 5. AI知识库文档数据 (10条)
INSERT INTO `ai_knowledge_document` (`id`, `doc_no`, `doc_name`, `doc_type`, `category`, `file_path`, `file_size`, `summary`, `keywords`, `uploader`, `upload_time`, `status`, `remark`) VALUES
(1, 'DOC001', '智能手环生产工艺手册', 'PDF', '生产工艺', '/docs/production_manual.pdf', 2048576, '详细描述智能手环的完整生产工艺流程', '手环,生产工艺,流程,装配', 'admin', '2026-06-01 09:00:00', 1, '核心工艺文档'),
(2, 'DOC002', 'SMT贴片作业指导书', 'WORD', '作业指导', '/docs/smt_guide.docx', 1048576, 'SMT贴片标准作业流程和注意事项', 'SMT,贴片,作业,指导书', 'admin', '2026-06-02 10:00:00', 1, '作业指导'),
(3, 'DOC003', '蓝牙模块技术规格书', 'PDF', '技术规格', '/docs/bluetooth_spec.pdf', 524288, '蓝牙模块的详细技术参数和规格', '蓝牙,模块,规格,参数', 'admin', '2026-06-03 11:00:00', 1, '技术规格'),
(4, 'DOC004', '产品检验标准', 'PDF', '质量标准', '/docs/quality_standard.pdf', 1572864, '智能手环产品质量检验标准和判定准则', '检验,标准,质量,判定', 'admin', '2026-06-05 09:00:00', 1, '质检标准'),
(5, 'DOC005', '设备操作手册', 'WORD', '设备管理', '/docs/equipment_manual.docx', 3145728, '各生产设备的标准操作手册', '设备,操作,手册,维护', 'admin', '2026-06-08 10:00:00', 1, '设备操作'),
(6, 'DOC006', '物料编码规则', 'EXCEL', '基础档案', '/docs/material_code.xlsx', 262144, '物料编码的分类规则和编码体系', '物料,编码,规则,分类', 'admin', '2026-06-10 09:00:00', 1, '编码规则'),
(7, 'DOC007', '工艺流程图', 'PDF', '生产工艺', '/docs/process_flow.pdf', 1048576, '智能手环完整生产流程图', '工艺,流程,生产,装配', 'admin', '2026-06-12 10:00:00', 1, '工艺流程'),
(8, 'DOC008', '质量管理体系文件', 'PDF', '质量管理', '/docs/quality_system.pdf', 2097152, 'ISO9001质量管理体系相关文件', '质量管理,体系,ISO9001', 'admin', '2026-06-15 11:00:00', 1, '体系文件'),
(9, 'DOC009', '常见问题解答', 'TEXT', 'FAQ', '/docs/faq.txt', 131072, '生产过程中常见问题和解决方案', '问题,解答,FAQ,解决方案', 'admin', '2026-06-20 09:00:00', 1, '常见问题'),
(10, 'DOC010', '安全生产规范', 'PDF', '安全管理', '/docs/safety_rules.pdf', 786432, '车间安全生产规范和注意事项', '安全,生产,规范,注意事项', 'admin', '2026-06-25 10:00:00', 1, '安全规范');

-- 6. AI问答历史数据 (10条)
INSERT INTO `ai_chat_history` (`id`, `session_id`, `user_id`, `user_name`, `question`, `answer`, `model`, `response_time`, `feedback`, `chat_time`) VALUES
(1, 'S20260601001', 1, 'admin', 'SMT贴片机的标准温度是多少？', '根据SMT贴片作业指导书，回流焊炉的标准温度曲线为：预热区150-180℃，恒温区180-200℃，回流区230-250℃，冷却区小于200℃。具体参数需根据锡膏规格进行调整。', 'GPT-3.5', 1500, 1, '2026-06-01 10:00:00'),
(2, 'S20260601001', 1, 'admin', '如何判断回流焊温度是否正常？', '可以通过以下方式判断：1. 观察温度曲线是否符合规格；2. 检查焊点光泽度和形状；3. 进行X-Ray检测焊点质量；4. 测试样品的电气性能。', 'GPT-3.5', 1200, 1, '2026-06-01 10:05:00'),
(3, 'S20260602001', 4, 'warehouse_keeper', '蓝牙芯片的安全库存是多少？', '根据物料档案，蓝牙芯片的安全库存为2000PCS。当库存低于安全库存时，系统会自动预警提醒补货。', 'GPT-3.5', 800, 1, '2026-06-02 14:00:00'),
(4, 'S20260603001', 5, 'production_manager', '当前有哪些待处理的工单？', '根据系统数据，当前有3个待处理工单：WO202606001（组装手环500台）、WO202606002（组装手环1000台）、WO202606003（包装500套）。', 'GPT-3.5', 1000, 1, '2026-06-03 09:00:00'),
(5, 'S20260605001', 6, 'quality_inspector', 'OLED屏幕的检验标准是什么？', '根据产品检验标准，OLED屏幕检验要点包括：1. 外观无划痕、气泡；2. 显示无坏点、亮点；3. 色彩还原度≥95%；4. 对比度≥1000:1。', 'GPT-3.5', 1500, 1, '2026-06-05 11:00:00'),
(6, 'S20260608001', 1, 'admin', '设备故障处理流程是什么？', '设备故障处理流程：1. 发现故障及时上报；2. 设备管理部门派工；3. 维修人员到场维修；4. 故障排除后填写维修记录；5. 验证设备运行正常。', 'GPT-3.5', 1200, 0, '2026-06-08 15:00:00'),
(7, 'S20260610001', 7, 'worker001', '注塑机的操作注意事项有哪些？', '注塑机操作注意事项：1. 开机前检查设备状态；2. 按规定穿戴防护用品；3. 严禁在合模时伸手入模；4. 定期检查温度和压力参数；5. 发现异常立即停机。', 'GPT-3.5', 900, 1, '2026-06-10 08:00:00'),
(8, 'S20260615001', 3, 'admin', '什么是物料追溯？', '物料追溯是指通过条码或二维码等方式，记录物料从入库到成品出库的全过程信息，实现正向追踪和反向溯源，便于质量管控和问题定位。', 'GPT-3.5', 1100, 1, '2026-06-15 10:00:00'),
(9, 'S20260620001', 4, 'warehouse_keeper', '如何进行库存盘点？', '库存盘点流程：1. 系统导出库存清单；2. 实物盘点并记录；3. 录入盘点数据；4. 系统比对生成差异；5. 分析差异原因；6. 调整库存账目。', 'GPT-3.5', 1300, 1, '2026-06-20 14:00:00'),
(10, 'S20260701001', 1, 'admin', '生产计划如何制定？', '生产计划制定考虑因素：1. 销售订单需求；2. 物料库存情况；3. 设备产能；4. 人员配置；5. 交货周期。建议使用MRP运算辅助排产。', 'GPT-3.5', 1400, 1, '2026-07-01 09:00:00');