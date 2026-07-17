-- ============================================================
-- 智能手环MES系统 - 设备/维保/维修/入库/出库/异常模块重建脚本
-- 删除旧表，创建新表，插入丰富数据
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `equipment_info`;
DROP TABLE IF EXISTS `equipment_maintain`;
DROP TABLE IF EXISTS `equipment_repair_task`;
DROP TABLE IF EXISTS `stock_in`;
DROP TABLE IF EXISTS `stock_out`;
DROP TABLE IF EXISTS `exception_record`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. 设备档案表 (equipment_info)
-- ============================================================
CREATE TABLE IF NOT EXISTS `equipment_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '设备编号',
    `equipment_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `equipment_type` VARCHAR(50) COMMENT '设备类型',
    `model` VARCHAR(100) COMMENT '规格型号',
    `manufacturer` VARCHAR(100) COMMENT '生产厂商',
    `workshop` VARCHAR(50) COMMENT '所在车间',
    `location` VARCHAR(100) COMMENT '安装位置',
    `manager` VARCHAR(50) COMMENT '负责人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `purchase_date` DATE COMMENT '采购日期',
    `commission_date` DATE COMMENT '启用日期',
    `running_hours` DECIMAL(10,2) DEFAULT 0 COMMENT '运行时长(小时)',
    `last_maintain_date` DATETIME COMMENT '上次维保时间',
    `next_maintain_date` DATETIME COMMENT '下次维保时间',
    `status` INT DEFAULT 1 COMMENT '状态(0停用,1运行,2维修中,3报废)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_equipment_no` (`equipment_no`),
    INDEX `idx_equipment_name` (`equipment_name`),
    INDEX `idx_workshop` (`workshop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备档案表';

-- ============================================================
-- 2. 设备维保记录表 (equipment_maintain)
-- ============================================================
CREATE TABLE IF NOT EXISTS `equipment_maintain` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维保ID',
    `maintain_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '维保单号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `maintain_type` VARCHAR(20) COMMENT '维保类型(日常保养/定期保养/故障维修/大修)',
    `maintain_level` VARCHAR(20) COMMENT '维保级别(一级/二级/三级)',
    `plan_date` DATE COMMENT '计划维保日期',
    `actual_date` DATETIME COMMENT '实际维保日期',
    `maintain_content` VARCHAR(1000) COMMENT '维保内容',
    `maintain_result` VARCHAR(20) COMMENT '维保结果(合格/不合格)',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维保费用',
    `maintainer` VARCHAR(50) COMMENT '维保人',
    `duration_hours` DECIMAL(5,2) DEFAULT 0 COMMENT '耗时(小时)',
    `status` INT DEFAULT 1 COMMENT '状态(0未完成,1已完成,2已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_maintain_no` (`maintain_no`),
    INDEX `idx_equipment_id` (`equipment_id`),
    INDEX `idx_actual_date` (`actual_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维保记录表';

-- ============================================================
-- 3. 维修任务表 (equipment_repair_task)
-- ============================================================
CREATE TABLE IF NOT EXISTS `equipment_repair_task` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    `task_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '任务编号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `fault_type` VARCHAR(50) COMMENT '故障类型',
    `fault_level` VARCHAR(20) COMMENT '故障等级(紧急/一般/轻微)',
    `fault_desc` VARCHAR(1000) COMMENT '故障描述',
    `reporter` VARCHAR(50) COMMENT '报修人',
    `report_time` DATETIME COMMENT '报修时间',
    `assignee` VARCHAR(50) COMMENT '维修负责人',
    `assign_time` DATETIME COMMENT '派工时间',
    `start_time` DATETIME COMMENT '开始维修时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `solution` VARCHAR(1000) COMMENT '维修方案',
    `result` VARCHAR(20) COMMENT '维修结果(已修复/待验证/无法修复)',
    `parts_used` VARCHAR(500) COMMENT '使用配件',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    `priority` INT DEFAULT 2 COMMENT '优先级(1高,2中,3低)',
    `status` INT DEFAULT 0 COMMENT '状态(0待派工,1已派工,2维修中,3已完成,4已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_task_no` (`task_no`),
    INDEX `idx_equipment_id` (`equipment_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修任务表';

-- ============================================================
-- 4. 入库记录表 (stock_in)
-- ============================================================
CREATE TABLE IF NOT EXISTS `stock_in` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库ID',
    `stock_in_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '入库单号',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `spec` VARCHAR(100) COMMENT '规格型号',
    `unit` VARCHAR(20) COMMENT '单位',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `warehouse` VARCHAR(50) COMMENT '仓库名称',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '入库数量',
    `price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    `source_type` VARCHAR(20) COMMENT '来源类型(采购入库/生产入库/退货入库/调拨入库/其他)',
    `source_no` VARCHAR(50) COMMENT '来源单号',
    `supplier_id` BIGINT COMMENT '供应商ID',
    `supplier_name` VARCHAR(100) COMMENT '供应商名称',
    `operator` VARCHAR(50) COMMENT '操作人',
    `in_date` DATETIME COMMENT '入库时间',
    `status` INT DEFAULT 1 COMMENT '状态(0待审核,1已入库,2已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_stock_in_no` (`stock_in_no`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_in_date` (`in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- ============================================================
-- 5. 出库记录表 (stock_out)
-- ============================================================
CREATE TABLE IF NOT EXISTS `stock_out` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库ID',
    `stock_out_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '出库单号',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `spec` VARCHAR(100) COMMENT '规格型号',
    `unit` VARCHAR(20) COMMENT '单位',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `warehouse` VARCHAR(50) COMMENT '仓库名称',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '出库数量',
    `price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    `target_type` VARCHAR(20) COMMENT '出库类型(生产领料/销售出库/调拨出库/其他)',
    `target_no` VARCHAR(50) COMMENT '目标单号',
    `work_order_no` VARCHAR(50) COMMENT '关联工单',
    `receiver` VARCHAR(50) COMMENT '领用人',
    `department` VARCHAR(50) COMMENT '领用部门',
    `operator` VARCHAR(50) COMMENT '操作人',
    `out_date` DATETIME COMMENT '出库时间',
    `status` INT DEFAULT 1 COMMENT '状态(0待审核,1已出库,2已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_stock_out_no` (`stock_out_no`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_out_date` (`out_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- ============================================================
-- 6. 异常处理表 (exception_record)
-- ============================================================
CREATE TABLE IF NOT EXISTS `exception_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '异常ID',
    `exception_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '异常单号',
    `exception_type` VARCHAR(50) COMMENT '异常类型(质量异常/设备异常/工艺异常/物料异常/安全异常)',
    `exception_level` VARCHAR(20) COMMENT '异常等级(紧急/重要/一般)',
    `source_type` VARCHAR(20) COMMENT '来源类型(工单/工序/质检/库存/其他)',
    `source_no` VARCHAR(50) COMMENT '来源单号',
    `work_order_no` VARCHAR(50) COMMENT '关联工单',
    `title` VARCHAR(200) COMMENT '异常标题',
    `description` VARCHAR(2000) COMMENT '异常描述',
    `reporter` VARCHAR(50) COMMENT '上报人',
    `reporter_id` BIGINT COMMENT '上报人ID',
    `report_time` DATETIME COMMENT '上报时间',
    `handler` VARCHAR(50) COMMENT '处理人',
    `handler_id` BIGINT COMMENT '处理人ID',
    `assign_time` DATETIME COMMENT '分派时间',
    `handle_content` VARCHAR(2000) COMMENT '处理内容',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_result` VARCHAR(20) COMMENT '处理结果(已解决/已缓解/未解决/挂起)',
    `cause_analysis` VARCHAR(1000) COMMENT '原因分析',
    `preventive_action` VARCHAR(1000) COMMENT '预防措施',
    `image_urls` TEXT COMMENT '图片附件',
    `priority` INT DEFAULT 2 COMMENT '优先级(1高,2中,3低)',
    `status` INT DEFAULT 0 COMMENT '状态(0待处理,1处理中,2已处理,3已关闭,4已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_exception_no` (`exception_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_report_time` (`report_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常处理表';

-- ============================================================
-- 数据初始化
-- ============================================================

-- 1. 设备档案数据 (12条)
INSERT INTO `equipment_info` (`id`, `equipment_no`, `equipment_name`, `equipment_type`, `model`, `manufacturer`, `workshop`, `location`, `manager`, `phone`, `purchase_date`, `commission_date`, `running_hours`, `last_maintain_date`, `next_maintain_date`, `status`, `remark`) VALUES
(1, 'EQ001', '贴片机', 'SMT设备', 'ASM-SIPLACE-X4', 'ASMPT', 'SMT车间', 'A区1号工位', '张工', '13800000001', '2022-03-15', '2022-04-01', 5680.50, '2026-06-15 09:00:00', '2026-09-15 09:00:00', 1, '高精度贴片机'),
(2, 'EQ002', '回流焊炉', 'SMT设备', 'ERSA PowerFlow', 'ERSA', 'SMT车间', 'A区2号工位', '张工', '13800000001', '2022-03-15', '2022-04-01', 4520.00, '2026-06-20 10:00:00', '2026-09-20 10:00:00', 1, '回流焊接设备'),
(3, 'EQ003', 'AOI光学检测仪', '检测设备', 'Koh Young Zenith', '高永', 'SMT车间', 'A区3号工位', '李工', '13800000002', '2022-05-20', '2022-06-01', 3210.00, '2026-06-25 14:00:00', '2026-09-25 14:00:00', 1, '锡膏检测AOI'),
(4, 'EQ004', '注塑机', '成型设备', '海天MA1600/540', '海天塑机', '注塑车间', 'B区1号工位', '王工', '13800000003', '2021-08-10', '2021-09-01', 8960.00, '2026-07-01 08:00:00', '2026-10-01 08:00:00', 1, '160吨注塑机'),
(5, 'EQ005', 'CNC加工中心', '加工设备', 'DMG MORI NLX2500', 'DMG', '机加车间', 'C区1号工位', '赵工', '13800000004', '2023-01-15', '2023-02-01', 2340.00, '2026-06-10 09:00:00', '2026-09-10 09:00:00', 1, '高精度CNC'),
(6, 'EQ006', '激光打标机', '标识设备', 'Han''s Laser EP-15', '大族激光', '组装车间', 'D区1号工位', '钱工', '13800000005', '2022-10-20', '2022-11-01', 1860.00, '2026-06-30 10:00:00', '2026-09-30 10:00:00', 1, 'CO2激光打标机'),
(7, 'EQ007', '自动组装线', '组装设备', 'ASM-AD830', 'ASM', '组装车间', 'D区2号工位', '孙工', '13800000006', '2023-03-10', '2023-04-01', 1450.00, '2026-07-05 11:00:00', '2026-10-05 11:00:00', 1, '全自动组装线'),
(8, 'EQ008', '气密性测试仪', '测试设备', 'ATEQ F620', 'ATEQ', '测试车间', 'E区1号工位', '周工', '13800000007', '2022-12-05', '2023-01-01', 980.00, '2026-07-08 09:00:00', '2026-10-08 09:00:00', 1, '气密性检测'),
(9, 'EQ009', '喷涂机器人', '表面处理', 'ABB IRB-1200', 'ABB', '喷涂车间', 'F区1号工位', '吴工', '13800000008', '2023-05-15', '2023-06-01', 760.00, '2026-07-10 14:00:00', '2026-10-10 14:00:00', 1, '六轴喷涂机器人'),
(10, 'EQ010', '包装机', '包装设备', 'ProMach SD-2000', 'ProMach', '包装车间', 'G区1号工位', '郑工', '13800000009', '2022-07-20', '2022-08-01', 3200.00, '2026-06-28 10:00:00', '2026-09-28 10:00:00', 1, '自动包装机'),
(11, 'EQ011', '老化测试架', '测试设备', '自制HS-300', '自制', '测试车间', 'E区2号工位', '周工', '13800000007', '2023-02-10', '2023-03-01', 2200.00, '2026-07-12 09:00:00', '2026-10-12 09:00:00', 2, '产品老化测试'),
(12, 'EQ012', '印刷机', 'SMT设备', 'DEK Horizon 03iX', 'DEK', 'SMT车间', 'A区4号工位', '张工', '13800000001', '2022-04-20', '2022-05-01', 4120.00, '2026-06-22 11:00:00', '2026-09-22 11:00:00', 1, '锡膏印刷机');

-- 2. 设备维保记录数据 (12条)
INSERT INTO `equipment_maintain` (`id`, `maintain_no`, `equipment_id`, `equipment_no`, `equipment_name`, `maintain_type`, `maintain_level`, `plan_date`, `actual_date`, `maintain_content`, `maintain_result`, `cost`, `maintainer`, `duration_hours`, `status`, `remark`) VALUES
(1, 'MT202606001', 1, 'EQ001', '贴片机', '定期保养', '二级', '2026-06-15', '2026-06-15 09:00:00', '清洁吸嘴，检查贴装精度，更换过滤器，校准视觉系统', '合格', 1200.00, '张工程师', 4.5, 1, '正常保养'),
(2, 'MT202606002', 2, 'EQ002', '回流焊炉', '定期保养', '二级', '2026-06-20', '2026-06-20 10:00:00', '清理炉膛，检查加热元件，校验温度曲线', '合格', 800.00, '张工程师', 3.0, 1, '正常保养'),
(3, 'MT202606003', 3, 'EQ003', 'AOI光学检测仪', '日常保养', '一级', '2026-06-25', '2026-06-25 14:00:00', '清洁镜头，校准光源，检查传送带', '合格', 300.00, '李工程师', 1.5, 1, '日常保养'),
(4, 'MT202606004', 4, 'EQ004', '注塑机', '定期保养', '三级', '2026-07-01', '2026-07-01 08:00:00', '更换液压油，清理模具，检查加热圈', '合格', 3500.00, '王工程师', 8.0, 1, '大保养'),
(5, 'MT202606005', 5, 'EQ005', 'CNC加工中心', '定期保养', '二级', '2026-06-10', '2026-06-10 09:00:00', '更换主轴润滑油，清理冷却液过滤器，校验精度', '合格', 1500.00, '赵工程师', 5.0, 1, '正常保养'),
(6, 'MT202606006', 6, 'EQ006', '激光打标机', '日常保养', '一级', '2026-06-30', '2026-06-30 10:00:00', '清洁镜片，检查激光管功率', '合格', 200.00, '钱工程师', 1.0, 1, '日常保养'),
(7, 'MT202606007', 7, 'EQ007', '自动组装线', '定期保养', '二级', '2026-07-05', '2026-07-05 11:00:00', '清洁导轨，检查气路，校准传感器', '合格', 1800.00, '孙工程师', 6.0, 1, '正常保养'),
(8, 'MT202606008', 8, 'EQ008', '气密性测试仪', '故障维修', '一级', '2026-07-08', '2026-07-08 09:00:00', '更换压力传感器，重新校准', '合格', 2500.00, '周工程师', 2.0, 1, '故障已修复'),
(9, 'MT202606009', 9, 'EQ009', '喷涂机器人', '定期保养', '二级', '2026-07-10', '2026-07-10 14:00:00', '清洁机械手，更换密封圈，校验重复定位精度', '合格', 2200.00, '吴工程师', 4.0, 1, '正常保养'),
(10, 'MT202606010', 10, 'EQ010', '包装机', '定期保养', '二级', '2026-06-28', '2026-06-28 10:00:00', '调整传送带张力，更换切刀，检查封口温度', '合格', 900.00, '郑工程师', 3.5, 1, '正常保养'),
(11, 'MT202606011', 11, 'EQ011', '老化测试架', '故障维修', '二级', '2026-07-12', '2026-07-12 09:00:00', '更换电源模块，修复控制板', '合格', 1500.00, '周工程师', 3.0, 1, '已修复'),
(12, 'MT202606012', 12, 'EQ012', '印刷机', '定期保养', '二级', '2026-06-22', '2026-06-22 11:00:00', '清洁钢网，检查刮刀，校准印刷参数', '合格', 1100.00, '张工程师', 4.0, 1, '正常保养');

-- 3. 维修任务数据 (10条)
INSERT INTO `equipment_repair_task` (`id`, `task_no`, `equipment_id`, `equipment_no`, `equipment_name`, `fault_type`, `fault_level`, `fault_desc`, `reporter`, `report_time`, `assignee`, `assign_time`, `start_time`, `complete_time`, `solution`, `result`, `parts_used`, `cost`, `priority`, `status`, `remark`) VALUES
(1, 'RT202606001', 8, 'EQ008', '气密性测试仪', '电气故障', '紧急', '压力传感器异常，测试数据偏差大', '操作工小李', '2026-07-08 08:30:00', '周工程师', '2026-07-08 08:45:00', '2026-07-08 09:00:00', '2026-07-08 11:00:00', '更换压力传感器，重新校准测试参数', '已修复', '压力传感器×1', 2500.00, 1, 3, '已修复正常'),
(2, 'RT202606002', 11, 'EQ011', '老化测试架', '电气故障', '一般', '第3层测试位无电源输出', '操作工小王', '2026-07-12 08:00:00', '周工程师', '2026-07-12 08:15:00', '2026-07-12 08:30:00', '2026-07-12 11:30:00', '更换电源模块，修复控制板线路', '已修复', '电源模块×1,继电器×2', 1500.00, 2, 3, '已修复'),
(3, 'RT202606003', 4, 'EQ004', '注塑机', '机械故障', '一般', '合模位置偏差0.2mm', '操作工小张', '2026-07-13 10:00:00', '王工程师', '2026-07-13 10:30:00', '2026-07-13 11:00:00', NULL, '调整模板平行度，更换磨损导套', '待验证', '导套×2', 1800.00, 2, 2, '维修中'),
(4, 'RT202606004', 1, 'EQ001', '贴片机', '机械故障', '紧急', '贴装头X轴运动异常报警', '操作工小赵', '2026-07-14 14:00:00', '张工程师', '2026-07-14 14:15:00', '2026-07-14 14:30:00', NULL, '检查X轴电机和同步带', NULL, NULL, 0.00, 1, 2, '维修中'),
(5, 'RT202606005', 2, 'EQ002', '回流焊炉', '温度异常', '一般', '炉温波动超出工艺要求', '操作工小钱', '2026-07-15 09:00:00', '张工程师', '2026-07-15 09:30:00', NULL, NULL, NULL, NULL, NULL, 0.00, 2, 1, '已派工'),
(6, 'RT202606006', 6, 'EQ006', '激光打标机', '光学故障', '轻微', '打标位置偏移', '操作工小孙', '2026-07-10 13:00:00', '钱工程师', '2026-07-10 13:15:00', '2026-07-10 13:30:00', '2026-07-10 14:00:00', '调整振镜，重新校准', '已修复', '无', 0.00, 3, 3, '已修复'),
(7, 'RT202606007', 10, 'EQ010', '包装机', '机械故障', '一般', '封口不牢', '操作工小周', '2026-07-11 11:00:00', '郑工程师', '2026-07-11 11:30:00', '2026-07-11 12:00:00', '2026-07-11 13:30:00', '更换加热块，调整温度', '已修复', '加热块×1', 600.00, 2, 3, '已修复'),
(8, 'RT202606008', 9, 'EQ009', '喷涂机器人', '程序故障', '一般', '喷涂路径异常', '操作工小吴', '2026-07-09 15:00:00', '吴工程师', '2026-07-09 15:30:00', '2026-07-09 16:00:00', '2026-07-09 17:00:00', '重新编程示教', '已修复', '无', 0.00, 2, 3, '已修复'),
(9, 'RT202606009', 5, 'EQ005', 'CNC加工中心', '主轴故障', '紧急', '主轴异响', '操作工小郑', '2026-07-15 16:00:00', '赵工程师', '2026-07-15 16:15:00', NULL, NULL, NULL, NULL, NULL, 0.00, 1, 1, '已派工'),
(10, 'RT202606010', 3, 'EQ003', 'AOI光学检测仪', '软件故障', '轻微', '检测软件卡顿', '操作工小冯', '2026-07-13 15:00:00', '李工程师', '2026-07-13 15:30:00', '2026-07-13 16:00:00', '2026-07-13 17:00:00', '重装检测软件，优化参数', '已修复', '无', 0.00, 3, 3, '已修复');

-- 4. 入库记录数据 (12条)
INSERT INTO `stock_in` (`id`, `stock_in_no`, `material_id`, `material_no`, `material_name`, `spec`, `unit`, `warehouse_id`, `warehouse`, `batch_no`, `quantity`, `price`, `total_amount`, `source_type`, `source_no`, `supplier_id`, `supplier_name`, `operator`, `in_date`, `status`, `remark`) VALUES
(1, 'IN202606001', 1, 'MAT001', '蓝牙芯片', 'BLE 5.0', 'PCS', 1, '原材料仓', 'BT202606001', 5000.00, 8.50, 42500.00, '采购入库', 'PO202606001', 3, '上海芯片技术有限公司', '张仓管', '2026-06-01 10:00:00', 1, '正常入库'),
(2, 'IN202606002', 2, 'MAT002', 'PCB主板', '四层板 50×40mm', 'PCS', 1, '原材料仓', 'PCB202606001', 10000.00, 12.00, 120000.00, '采购入库', 'PO202606001', 1, '深圳鑫达电子有限公司', '张仓管', '2026-06-01 10:30:00', 1, '正常入库'),
(3, 'IN202606003', 3, 'MAT003', 'OLED屏幕', '1.3寸 240×240', 'PCS', 1, '原材料仓', 'OLED202606001', 3000.00, 25.00, 75000.00, '采购入库', 'PO202606002', 5, '苏州精密显示科技', '张仓管', '2026-06-03 14:00:00', 1, '正常入库'),
(4, 'IN202606004', 4, 'MAT004', '锂电池', '3.7V 200mAh', 'PCS', 1, '原材料仓', 'BAT202606001', 5000.00, 6.50, 32500.00, '采购入库', 'PO202606003', 6, '深圳新能源电池', '张仓管', '2026-06-05 09:00:00', 1, '正常入库'),
(5, 'IN202606005', 5, 'MAT005', '塑胶外壳', 'TPU 黑色', 'PCS', 2, '成品仓', 'SH202606001', 8000.00, 3.50, 28000.00, '采购入库', 'PO202606004', 2, '东莞精密塑胶制品厂', '李仓管', '2026-06-08 11:00:00', 1, '正常入库'),
(6, 'IN202606006', 6, 'MAT006', '硅胶表带', '硅胶 22mm 黑色', 'PCS', 2, '成品仓', 'RG202606001', 8000.00, 2.80, 22400.00, '采购入库', 'PO202606004', 2, '东莞精密塑胶制品厂', '李仓管', '2026-06-08 11:30:00', 1, '正常入库'),
(7, 'IN202606007', 7, 'MAT007', '心率传感器', 'PPG光电式', 'PCS', 1, '原材料仓', 'HR202606001', 4000.00, 18.00, 72000.00, '采购入库', 'PO202606005', 7, '深圳医疗电子有限公司', '张仓管', '2026-06-10 14:00:00', 1, '正常入库'),
(8, 'IN202606008', 8, 'MAT008', '六轴传感器', 'MPU6050', 'PCS', 1, '原材料仓', 'MO202606001', 4000.00, 7.50, 30000.00, '采购入库', 'PO202606005', 1, '深圳鑫达电子有限公司', '张仓管', '2026-06-10 14:30:00', 1, '正常入库'),
(9, 'IN202606009', 9, 'MAT009', '锡膏', 'SAC305 无铅', 'KG', 1, '原材料仓', 'SL202606001', 50.00, 380.00, 19000.00, '采购入库', 'PO202606006', 8, '深圳锡焊材料厂', '张仓管', '2026-06-15 10:00:00', 1, '正常入库'),
(10, 'IN202606010', 10, 'MAT010', '包装盒', '智能手环专用', 'PCS', 2, '成品仓', 'PB202606001', 20000.00, 0.80, 16000.00, '采购入库', 'PO202606007', 4, '广东包装材料公司', '李仓管', '2026-06-20 09:00:00', 1, '正常入库'),
(11, 'IN202606011', 1, 'MAT001', '蓝牙芯片', 'BLE 5.0', 'PCS', 1, '原材料仓', 'BT202606002', 2000.00, 8.50, 17000.00, '退货入库', 'RT202606001', 3, '上海芯片技术有限公司', '张仓管', '2026-07-01 14:00:00', 1, '不良品退货'),
(12, 'IN202606012', 11, 'MAT011', 'USB充电线', '磁吸式', 'PCS', 2, '成品仓', 'US202606001', 5000.00, 1.50, 7500.00, '采购入库', 'PO202606008', 9, '深圳线缆电子公司', '李仓管', '2026-07-05 10:00:00', 1, '正常入库');

-- 5. 出库记录数据 (12条)
INSERT INTO `stock_out` (`id`, `stock_out_no`, `material_id`, `material_no`, `material_name`, `spec`, `unit`, `warehouse_id`, `warehouse`, `batch_no`, `quantity`, `price`, `total_amount`, `target_type`, `target_no`, `work_order_no`, `receiver`, `department`, `operator`, `out_date`, `status`, `remark`) VALUES
(1, 'OUT202606001', 1, 'MAT001', '蓝牙芯片', 'BLE 5.0', 'PCS', 1, '原材料仓', 'BT202606001', 500.00, 8.50, 4250.00, '生产领料', 'WO202606001', 'WO202606001', '张三', 'SMT车间', '张仓管', '2026-06-05 09:00:00', 1, '工单领料'),
(2, 'OUT202606002', 2, 'MAT002', 'PCB主板', '四层板 50×40mm', 'PCS', 1, '原材料仓', 'PCB202606001', 500.00, 12.00, 6000.00, '生产领料', 'WO202606001', 'WO202606001', '张三', 'SMT车间', '张仓管', '2026-06-05 09:30:00', 1, '工单领料'),
(3, 'OUT202606003', 3, 'MAT003', 'OLED屏幕', '1.3寸 240×240', 'PCS', 1, '原材料仓', 'OLED202606001', 500.00, 25.00, 12500.00, '生产领料', 'WO202606001', 'WO202606001', '李四', '组装车间', '张仓管', '2026-06-06 10:00:00', 1, '工单领料'),
(4, 'OUT202606004', 5, 'MAT005', '塑胶外壳', 'TPU 黑色', 'PCS', 2, '成品仓', 'SH202606001', 500.00, 3.50, 1750.00, '生产领料', 'WO202606001', 'WO202606001', '李四', '组装车间', '李仓管', '2026-06-06 10:30:00', 1, '工单领料'),
(5, 'OUT202606005', 9, 'MAT009', '锡膏', 'SAC305 无铅', 'KG', 1, '原材料仓', 'SL202606001', 5.00, 380.00, 1900.00, '生产领料', 'WO202606001', 'WO202606001', '王五', 'SMT车间', '张仓管', '2026-06-05 14:00:00', 1, '工单领料'),
(6, 'OUT202606006', 1, 'MAT001', '蓝牙芯片', 'BLE 5.0', 'PCS', 1, '原材料仓', 'BT202606001', 1000.00, 8.50, 8500.00, '生产领料', 'WO202606002', 'WO202606002', '赵六', 'SMT车间', '张仓管', '2026-06-10 09:00:00', 1, '工单领料'),
(7, 'OUT202606007', 2, 'MAT002', 'PCB主板', '四层板 50×40mm', 'PCS', 1, '原材料仓', 'PCB202606001', 1000.00, 12.00, 12000.00, '生产领料', 'WO202606002', 'WO202606002', '赵六', 'SMT车间', '张仓管', '2026-06-10 09:30:00', 1, '工单领料'),
(8, 'OUT202606008', 3, 'MAT003', 'OLED屏幕', '1.3寸 240×240', 'PCS', 1, '原材料仓', 'OLED202606001', 1000.00, 25.00, 25000.00, '生产领料', 'WO202606002', 'WO202606002', '钱七', '组装车间', '张仓管', '2026-06-11 10:00:00', 1, '工单领料'),
(9, 'OUT202606009', 4, 'MAT004', '锂电池', '3.7V 200mAh', 'PCS', 1, '原材料仓', 'BAT202606001', 1000.00, 6.50, 6500.00, '生产领料', 'WO202606002', 'WO202606002', '钱七', '组装车间', '张仓管', '2026-06-11 10:30:00', 1, '工单领料'),
(10, 'OUT202606010', 7, 'MAT007', '心率传感器', 'PPG光电式', 'PCS', 1, '原材料仓', 'HR202606001', 800.00, 18.00, 14400.00, '生产领料', 'WO202606003', 'WO202606003', '孙八', '组装车间', '张仓管', '2026-06-15 09:00:00', 1, '工单领料'),
(11, 'OUT202606011', 10, 'MAT010', '包装盒', '智能手环专用', 'PCS', 2, '成品仓', 'PB202606001', 2000.00, 0.80, 1600.00, '生产领料', 'WO202606003', 'WO202606003', '周九', '包装车间', '李仓管', '2026-06-20 14:00:00', 1, '工单领料'),
(12, 'OUT202606012', 11, 'MAT011', 'USB充电线', '磁吸式', 'PCS', 2, '成品仓', 'US202606001', 1000.00, 1.50, 1500.00, '销售出库', 'SO202606001', '', '销售部', '销售部', '李仓管', '2026-07-10 10:00:00', 1, '客户订单');

-- 6. 异常处理数据 (12条)
INSERT INTO `exception_record` (`id`, `exception_no`, `exception_type`, `exception_level`, `source_type`, `source_no`, `work_order_no`, `title`, `description`, `reporter`, `reporter_id`, `report_time`, `handler`, `handler_id`, `assign_time`, `handle_content`, `handle_time`, `handle_result`, `cause_analysis`, `preventive_action`, `priority`, `status`, `remark`) VALUES
(1, 'EX202606001', '质量异常', '紧急', '工单', 'WO202606001', 'WO202606001', '产品外观划伤', '第3批次组装成品发现外壳划伤，不良率5%', '张三', 7, '2026-06-08 10:00:00', '李四', 8, '2026-06-08 10:30:00', '隔离不良品，修复组装线传送带，更新作业指导', '2026-06-08 16:00:00', '已解决', '传送带表面有异物，导致外壳划伤', '每周清洁传送带', 1, 2, '已解决'),
(2, 'EX202606002', '设备异常', '重要', '工序', 'PROC005', 'WO202606002', '回流焊炉温度异常', '回流焊炉第2区温度超出工艺范围±10度', '王五', 9, '2026-06-10 11:00:00', '张工程师', 3, '2026-06-10 11:30:00', '重新校准炉温，更换温控模块', '2026-06-10 14:00:00', '已解决', '温控模块老化导致温度漂移', '每月校验温控模块', 2, 2, '已解决'),
(3, 'EX202606003', '物料异常', '重要', '工单', 'WO202606003', 'WO202606003', '物料短缺', 'OLED屏幕库存不足，影响生产', '钱七', 4, '2026-06-12 09:00:00', '张仓管', 4, '2026-06-12 09:30:00', '紧急采购补货，调整生产顺序', '2026-06-12 17:00:00', '已缓解', '未及时更新安全库存', '完善库存预警机制', 2, 2, '已缓解'),
(4, 'EX202606004', '质量异常', '一般', '质检', 'QC202606001', 'WO202606004', '心率传感器不良', '心率传感器焊接不良率3%', '赵六', 5, '2026-06-15 14:00:00', '李工程师', 6, '2026-06-15 14:30:00', '调整回流焊参数，重新培训操作员', '2026-06-16 11:00:00', '已解决', '回流焊温度偏高导致虚焊', '优化工艺参数', 3, 2, '已解决'),
(5, 'EX202606005', '工艺异常', '一般', '工序', 'PROC003', 'WO202606005', '注塑件尺寸偏差', '注塑件长度尺寸超出公差范围', '孙八', 5, '2026-06-18 10:00:00', '王工程师', 5, '2026-06-18 10:30:00', '调整模具温度和保压时间', '2026-06-18 15:00:00', '已解决', '模具温度不稳定', '增加模具温控设备', 3, 2, '已解决'),
(6, 'EX202606006', '设备异常', '一般', '工序', 'PROC007', 'WO202606006', '激光打标偏移', '激光打标位置偏移0.3mm', '周九', 6, '2026-06-20 13:00:00', '钱工程师', 7, '2026-06-20 13:30:00', '调整振镜校准', '2026-06-20 14:00:00', '已解决', '振镜松动', '每日检查振镜固定', 3, 2, '已解决'),
(7, 'EX202606007', '安全异常', '紧急', '其他', 'SAFE001', NULL, '车间漏电', 'SMT车间发现漏电情况', '张工', 3, '2026-06-25 16:00:00', '安全员', 10, '2026-06-25 16:15:00', '断电检修，更换破损线缆', '2026-06-25 18:00:00', '已解决', '线缆老化破损', '每周安全检查', 1, 3, '已关闭'),
(8, 'EX202606008', '物料异常', '重要', '库存', 'MAT005', NULL, '塑胶外壳色差', '新到塑胶外壳与样品存在色差', '李仓管', 4, '2026-07-01 09:00:00', '采购部', 11, '2026-07-01 09:30:00', '与供应商沟通补料', '2026-07-05 14:00:00', '已解决', '供应商原材料批次差异', '加强来料检验', 2, 2, '已解决'),
(9, 'EX202606009', '质量异常', '一般', '工单', 'WO202606007', 'WO202606007', '包装盒破损', '成品包装时发现包装盒破损率2%', '郑十', 7, '2026-07-05 11:00:00', '冯十一', 12, '2026-07-05 11:30:00', '更换包装盒供应商批次', '2026-07-08 16:00:00', '已解决', '包装盒材料强度不足', '更换供应商', 3, 2, '已解决'),
(10, 'EX202607010', '设备异常', '紧急', '工序', 'PROC008', 'WO202606008', '气密性测试仪故障', '气密性测试仪压力异常，多个产品测试结果异常', '操作工小李', 7, '2026-07-08 08:30:00', '周工程师', 6, '2026-07-08 08:45:00', '更换压力传感器', '2026-07-08 11:00:00', '已解决', '压力传感器损坏', '每月校验测试设备', 1, 3, '已关闭'),
(11, 'EX202607011', '工艺异常', '一般', '工序', 'PROC006', 'WO202606009', 'CNC加工精度偏差', 'CNC加工件尺寸超差', '赵六', 5, '2026-07-10 10:00:00', '赵工程师', 8, '2026-07-10 10:30:00', '重新对刀，补偿磨损', '2026-07-10 16:00:00', '已解决', '刀具磨损', '定期更换刀具', 2, 1, '处理中'),
(12, 'EX202607012', '质量异常', '一般', '工单', 'WO202606010', 'WO202606010', '充电不良', '新批次USB充电线充电不良率1%', '钱七', 4, '2026-07-12 14:00:00', '', NULL, NULL, NULL, NULL, NULL, '分析中', '加强来料检验', 3, 0, '待处理');
