-- ============================================================
-- 智能手环MES系统 - 演示数据库脚本
-- 数据库名：mes_demo
-- 说明：专门用于演示的数据库，包含设备管理和仓储管理核心流程
-- ============================================================

CREATE DATABASE IF NOT EXISTS mes_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mes_demo;

-- ========== 设备管理模块 ==========

-- 1. 设备档案表
CREATE TABLE IF NOT EXISTS `demo_equipment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `equipment_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    `equipment_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `equipment_type` VARCHAR(50) COMMENT '设备类型',
    `model` VARCHAR(100) COMMENT '型号',
    `manufacturer` VARCHAR(100) COMMENT '制造商',
    `workshop` VARCHAR(50) COMMENT '所属车间',
    `location` VARCHAR(100) COMMENT '位置',
    `manager` VARCHAR(50) COMMENT '负责人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `purchase_date` DATE COMMENT '采购日期',
    `commission_date` DATE COMMENT '投产日期',
    `running_hours` DECIMAL(10,2) DEFAULT 0 COMMENT '运行时长(小时)',
    `last_maintain_date` DATETIME COMMENT '上次保养日期',
    `next_maintain_date` DATETIME COMMENT '下次保养日期',
    `current_fault_id` BIGINT COMMENT '当前故障ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-运行 2-维修中 3-报废',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_equipment_no (`equipment_no`),
    INDEX idx_status (`status`),
    INDEX idx_workshop (`workshop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备档案';

-- 2. 设备故障表
CREATE TABLE IF NOT EXISTS `demo_equipment_fault` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `fault_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '故障编号',
    `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `fault_type` VARCHAR(50) COMMENT '故障类型',
    `fault_level` VARCHAR(20) COMMENT '故障等级：一般/严重/紧急',
    `fault_desc` VARCHAR(500) COMMENT '故障简述',
    `fault_description` TEXT COMMENT '故障详情',
    `reporter_id` BIGINT COMMENT '上报人ID',
    `reporter` VARCHAR(50) COMMENT '上报人',
    `report_time` DATETIME NOT NULL COMMENT '上报时间',
    `repair_task_id` BIGINT COMMENT '关联维修任务ID',
    `assignee` VARCHAR(50) COMMENT '派工人员',
    `assign_time` DATETIME COMMENT '派工时间',
    `solution` TEXT COMMENT '解决方案',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待派工 1-已派工 2-维修中 3-已修复 4-已关闭',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_fault_no (`fault_no`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_status (`status`),
    INDEX idx_report_time (`report_time`),
    CONSTRAINT fk_fault_equipment FOREIGN KEY (`equipment_id`) REFERENCES `demo_equipment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障记录';

-- 3. 维修任务表
CREATE TABLE IF NOT EXISTS `demo_repair_task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `task_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    `fault_id` BIGINT COMMENT '关联故障ID',
    `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `fault_desc` VARCHAR(500) COMMENT '故障简述',
    `fault_description` TEXT COMMENT '故障详情',
    `priority` TINYINT DEFAULT 1 COMMENT '优先级：0-低 1-中 2-高',
    `repairer_id` BIGINT COMMENT '维修人员ID',
    `assignee` VARCHAR(50) COMMENT '维修人员',
    `assign_time` DATETIME NOT NULL COMMENT '派工时间',
    `accept_time` DATETIME COMMENT '接单时间',
    `start_time` DATETIME COMMENT '开始维修时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `repair_content` TEXT COMMENT '维修内容',
    `solution` TEXT COMMENT '解决方案',
    `parts_used` VARCHAR(500) COMMENT '使用配件',
    `repair_cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '费用',
    `acceptance_result` VARCHAR(20) COMMENT '验收结果：pass-通过 reject-驳回',
    `acceptance_time` DATETIME COMMENT '验收时间',
    `acceptance_by` VARCHAR(50) COMMENT '验收人',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待接单 1-已派工 2-维修中 3-待验收 4-已验收',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_no (`task_no`),
    INDEX idx_fault_id (`fault_id`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_repairer_id (`repairer_id`),
    INDEX idx_status (`status`),
    CONSTRAINT fk_task_fault FOREIGN KEY (`fault_id`) REFERENCES `demo_equipment_fault`(`id`) ON DELETE SET NULL,
    CONSTRAINT fk_task_equipment FOREIGN KEY (`equipment_id`) REFERENCES `demo_equipment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修任务';

-- 4. 设备维保记录表
CREATE TABLE IF NOT EXISTS `demo_equipment_maintain` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `maintain_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '维保编号',
    `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `maintain_type` VARCHAR(20) COMMENT '维保类型：保养/维修/校准',
    `maintain_level` VARCHAR(20) COMMENT '维保级别：一级/二级/三级',
    `plan_date` DATE COMMENT '计划日期',
    `actual_date` DATETIME COMMENT '实际日期',
    `maintain_content` TEXT COMMENT '维保内容',
    `maintain_result` VARCHAR(20) COMMENT '维保结果：正常/异常/待跟进',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维保费用',
    `maintainer` VARCHAR(50) COMMENT '维保人员',
    `duration_hours` DECIMAL(5,2) COMMENT '维保时长(小时)',
    `related_task_id` BIGINT COMMENT '关联维修任务ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-待执行 1-执行中 2-已完成',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_maintain_no (`maintain_no`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_status (`status`),
    INDEX idx_actual_date (`actual_date`),
    CONSTRAINT fk_maintain_equipment FOREIGN KEY (`equipment_id`) REFERENCES `demo_equipment`(`id`) ON DELETE CASCADE,
    CONSTRAINT fk_maintain_task FOREIGN KEY (`related_task_id`) REFERENCES `demo_repair_task`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维保记录';

-- ========== 仓储管理模块 ==========

-- 5. 物料档案表
CREATE TABLE IF NOT EXISTS `demo_material` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `material_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '物料编号',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `type` VARCHAR(50) COMMENT '物料类型',
    `spec` VARCHAR(200) COMMENT '规格型号',
    `unit` VARCHAR(20) DEFAULT '个' COMMENT '单位',
    `price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `max_stock` INT DEFAULT 0 COMMENT '最大库存',
    `supplier_name` VARCHAR(100) COMMENT '供应商',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_material_no (`material_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料档案';

-- 6. 仓库信息表
CREATE TABLE IF NOT EXISTS `demo_warehouse` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `warehouse_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '仓库编码',
    `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
    `address` VARCHAR(200) COMMENT '地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_warehouse_code (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息';

-- 7. 库存表
CREATE TABLE IF NOT EXISTS `demo_stock` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `warehouse_code` VARCHAR(50) COMMENT '仓库编码',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `quantity` INT DEFAULT 0 COMMENT '库存数量',
    `location` VARCHAR(50) COMMENT '库位',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `expire_date` DATE COMMENT '过期日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_material_id (`material_id`),
    INDEX idx_warehouse_id (`warehouse_id`),
    CONSTRAINT fk_stock_material FOREIGN KEY (`material_id`) REFERENCES `demo_material`(`id`) ON DELETE CASCADE,
    CONSTRAINT fk_stock_warehouse FOREIGN KEY (`warehouse_id`) REFERENCES `demo_warehouse`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存';

-- 8. 入库记录表
CREATE TABLE IF NOT EXISTS `demo_stock_in` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `stock_in_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '入库单号',
    `type` VARCHAR(20) COMMENT '入库类型：采购入库/生产入库/退料入库/其他',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `warehouse_code` VARCHAR(50) COMMENT '仓库编码',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '入库数量',
    `unit_price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    `supplier_name` VARCHAR(100) COMMENT '供应商',
    `purchase_order_no` VARCHAR(50) COMMENT '采购订单号',
    `work_order_no` VARCHAR(50) COMMENT '工单编号',
    `operator` VARCHAR(50) COMMENT '操作人',
    `in_date` DATETIME NOT NULL COMMENT '入库时间',
    `scan_code` VARCHAR(100) COMMENT '扫码编号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核 1-已入库 2-已取消',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stock_in_no (`stock_in_no`),
    INDEX idx_material_id (`material_id`),
    INDEX idx_warehouse_id (`warehouse_id`),
    INDEX idx_in_date (`in_date`),
    CONSTRAINT fk_stockin_material FOREIGN KEY (`material_id`) REFERENCES `demo_material`(`id`) ON DELETE SET NULL,
    CONSTRAINT fk_stockin_warehouse FOREIGN KEY (`warehouse_id`) REFERENCES `demo_warehouse`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录';

-- 9. 出库记录表
CREATE TABLE IF NOT EXISTS `demo_stock_out` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `stock_out_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '出库单号',
    `type` VARCHAR(20) COMMENT '出库类型：生产领料/销售出库/退料出库/其他',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `warehouse_code` VARCHAR(50) COMMENT '仓库编码',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '出库数量',
    `unit_price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    `work_order_no` VARCHAR(50) COMMENT '工单编号',
    `order_no` VARCHAR(50) COMMENT '订单编号',
    `operator` VARCHAR(50) COMMENT '操作人',
    `out_date` DATETIME NOT NULL COMMENT '出库时间',
    `scan_code` VARCHAR(100) COMMENT '扫码编号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核 1-已出库 2-已取消',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stock_out_no (`stock_out_no`),
    INDEX idx_material_id (`material_id`),
    INDEX idx_warehouse_id (`warehouse_id`),
    INDEX idx_out_date (`out_date`),
    CONSTRAINT fk_stockout_material FOREIGN KEY (`material_id`) REFERENCES `demo_material`(`id`) ON DELETE SET NULL,
    CONSTRAINT fk_stockout_warehouse FOREIGN KEY (`warehouse_id`) REFERENCES `demo_warehouse`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录';

-- 10. 库存盘点表
CREATE TABLE IF NOT EXISTS `demo_stock_check` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `check_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '盘点单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `warehouse_code` VARCHAR(50) COMMENT '仓库编码',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `check_date` DATETIME NOT NULL COMMENT '盘点日期',
    `operator` VARCHAR(50) COMMENT '操作人',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-盘点中 1-已审核 2-已完成',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_check_no (`check_no`),
    INDEX idx_warehouse_id (`warehouse_id`),
    CONSTRAINT fk_check_warehouse FOREIGN KEY (`warehouse_id`) REFERENCES `demo_warehouse`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点';

-- 11. 盘点明细表
CREATE TABLE IF NOT EXISTS `demo_stock_check_detail` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `check_id` BIGINT NOT NULL COMMENT '盘点单ID',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `system_quantity` INT DEFAULT 0 COMMENT '系统数量',
    `actual_quantity` INT DEFAULT 0 COMMENT '实际数量',
    `diff_quantity` INT DEFAULT 0 COMMENT '差异数量',
    `diff_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '差异金额',
    `diff_reason` VARCHAR(500) COMMENT '差异原因',
    `handle_status` TINYINT DEFAULT 0 COMMENT '处理状态：0-待处理 1-已处理',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_check_id (`check_id`),
    INDEX idx_material_id (`material_id`),
    CONSTRAINT fk_checkdetail_check FOREIGN KEY (`check_id`) REFERENCES `demo_stock_check`(`id`) ON DELETE CASCADE,
    CONSTRAINT fk_checkdetail_material FOREIGN KEY (`material_id`) REFERENCES `demo_material`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细';

-- 12. 库存预警表
CREATE TABLE IF NOT EXISTS `demo_stock_alert` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `alert_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '预警编号',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编号',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `warehouse_code` VARCHAR(50) COMMENT '仓库编码',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `alert_type` VARCHAR(20) COMMENT '预警类型：低于安全库存/高于最大库存/即将过期',
    `current_quantity` INT DEFAULT 0 COMMENT '当前数量',
    `threshold` INT DEFAULT 0 COMMENT '阈值',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-未处理 1-已处理',
    `handle_remark` VARCHAR(500) COMMENT '处理备注',
    `handle_time` DATETIME COMMENT '处理时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_alert_no (`alert_no`),
    INDEX idx_material_id (`material_id`),
    INDEX idx_status (`status`),
    CONSTRAINT fk_alert_material FOREIGN KEY (`material_id`) REFERENCES `demo_material`(`id`) ON DELETE SET NULL,
    CONSTRAINT fk_alert_warehouse FOREIGN KEY (`warehouse_id`) REFERENCES `demo_warehouse`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警';
