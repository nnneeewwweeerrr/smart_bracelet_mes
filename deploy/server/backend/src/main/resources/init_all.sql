-- ============================================================
-- 智能手环MES系统 - 完整初始化脚本（Navicat一键执行版）
-- 包含：建表 + 数据初始化
-- 使用方法：在Navicat中选中数据库，直接运行此文件即可
-- 默认密码：123456
-- ============================================================

-- ========== 第一部分：创建表结构 ==========

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `user_name` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role_id` BIGINT COMMENT '角色ID',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_name` (`user_name`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) UNIQUE COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) UNIQUE COMMENT '权限编码',
    `resource_type` VARCHAR(20) COMMENT '资源类型',
    `resource_path` VARCHAR(200) COMMENT '资源路径',
    `parent_id` BIGINT COMMENT '父级ID',
    INDEX `idx_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
    `dict_value` VARCHAR(200) NOT NULL COMMENT '字典值',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` INT DEFAULT 1 COMMENT '状态',
    INDEX `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

CREATE TABLE IF NOT EXISTS `customer_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    `customer_code` VARCHAR(50) UNIQUE COMMENT '客户编码',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `contact_name` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `address` VARCHAR(200) COMMENT '地址',
    `status` INT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

CREATE TABLE IF NOT EXISTS `product_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    `product_code` VARCHAR(50) UNIQUE COMMENT '产品编码',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `product_type` VARCHAR(50) COMMENT '产品类型',
    `specification` VARCHAR(200) COMMENT '规格型号',
    `unit_price` DECIMAL(10,2) COMMENT '参考单价',
    `status` INT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品信息表';

CREATE TABLE IF NOT EXISTS `order_customer` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) UNIQUE COMMENT '订单编号',
    `customer_id` BIGINT COMMENT '客户ID',
    `product_id` BIGINT COMMENT '产品ID',
    `quantity` INT NOT NULL COMMENT '订单数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `status` INT DEFAULT 0 COMMENT '订单状态(0待审批,1已审批,2生产中,3已完成,4已取消)',
    `delivery_date` DATETIME COMMENT '交货日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_customer_id` (`customer_id`),
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户订单表';

CREATE TABLE IF NOT EXISTS `supplier_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商ID',
    `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact_name` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(200) COMMENT '地址',
    `status` INT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_supplier_name` (`supplier_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

CREATE TABLE IF NOT EXISTS `material_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物料ID',
    `material_code` VARCHAR(50) UNIQUE COMMENT '物料编码',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `material_type` VARCHAR(50) COMMENT '物料类型',
    `unit` VARCHAR(20) COMMENT '计量单位',
    `unit_price` DECIMAL(10,2) COMMENT '参考单价',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `status` INT DEFAULT 1 COMMENT '状态',
    INDEX `idx_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料表';

CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '采购单ID',
    `purchase_no` VARCHAR(50) UNIQUE COMMENT '采购单号',
    `supplier_id` BIGINT COMMENT '供应商ID',
    `material_id` BIGINT COMMENT '物料ID',
    `quantity` INT NOT NULL COMMENT '采购数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `status` INT DEFAULT 0 COMMENT '采购状态(0待审批,1已审批,2已到货,3已完成)',
    `expected_date` DATETIME COMMENT '预计到货日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_purchase_no` (`purchase_no`),
    INDEX `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE IF NOT EXISTS `warehouse_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仓库ID',
    `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
    `warehouse_code` VARCHAR(50) UNIQUE COMMENT '仓库编码',
    `address` VARCHAR(200) COMMENT '仓库地址',
    `status` INT DEFAULT 1 COMMENT '状态',
    INDEX `idx_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

CREATE TABLE IF NOT EXISTS `stock_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    `material_id` BIGINT COMMENT '物料ID',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `quantity` INT DEFAULT 0 COMMENT '库存数量',
    `location` VARCHAR(50) COMMENT '库位',
    INDEX `idx_material_warehouse` (`material_id`, `warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE IF NOT EXISTS `stock_in` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库单ID',
    `in_no` VARCHAR(50) UNIQUE COMMENT '入库单号',
    `material_id` BIGINT COMMENT '物料ID',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `quantity` INT NOT NULL COMMENT '入库数量',
    `source_type` VARCHAR(20) COMMENT '来源类型',
    `source_no` VARCHAR(50) COMMENT '来源单号',
    `operator_id` BIGINT COMMENT '操作人ID',
    `status` INT DEFAULT 0 COMMENT '状态：0-待审批，1-已审批',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_in_no` (`in_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

CREATE TABLE IF NOT EXISTS `stock_out` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库单ID',
    `out_no` VARCHAR(50) UNIQUE COMMENT '出库单号',
    `material_id` BIGINT COMMENT '物料ID',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `quantity` INT NOT NULL COMMENT '出库数量',
    `target_type` VARCHAR(20) COMMENT '出库类型',
    `target_no` VARCHAR(50) COMMENT '目标单号',
    `operator_id` BIGINT COMMENT '操作人ID',
    `status` INT DEFAULT 0 COMMENT '状态：0-待审批，1-已审批',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_out_no` (`out_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

CREATE TABLE IF NOT EXISTS `process_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工序ID',
    `process_name` VARCHAR(100) NOT NULL COMMENT '工序名称',
    `process_code` VARCHAR(50) UNIQUE COMMENT '工序编码',
    `product_id` BIGINT COMMENT '产品ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    INDEX `idx_process_code` (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序表';

CREATE TABLE IF NOT EXISTS `production_plan` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    `plan_no` VARCHAR(50) UNIQUE COMMENT '计划编号',
    `plan_name` VARCHAR(100) COMMENT '计划名称',
    `order_id` BIGINT COMMENT '订单ID',
    `product_id` BIGINT COMMENT '产品ID',
    `planned_quantity` INT NOT NULL COMMENT '计划产量',
    `start_date` DATETIME COMMENT '计划开始日期',
    `end_date` DATETIME COMMENT '计划结束日期',
    `status` INT DEFAULT 0 COMMENT '计划状态(0待执行,1执行中,2已完成)',
    `manager` VARCHAR(50) COMMENT '负责人',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_plan_no` (`plan_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划表';

CREATE TABLE IF NOT EXISTS `work_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    `work_no` VARCHAR(50) UNIQUE COMMENT '工单号',
    `plan_id` BIGINT COMMENT '计划ID',
    `product_id` BIGINT COMMENT '产品ID',
    `process_id` BIGINT COMMENT '工序ID',
    `quantity` INT NOT NULL COMMENT '工单数量',
    `status` INT DEFAULT 0 COMMENT '工单状态(0待派发,1执行中,2已完成)',
    `manager` VARCHAR(50) COMMENT '负责人',
    `remark` VARCHAR(500) COMMENT '备注',
    `plan_start_date` DATE COMMENT '计划开始日期',
    `plan_end_date` DATE COMMENT '计划完成日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_work_no` (`work_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单号表';

CREATE TABLE IF NOT EXISTS `production_report` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报工ID',
    `work_order_id` BIGINT COMMENT '工单ID',
    `worker_id` BIGINT COMMENT '工人ID',
    `process_id` BIGINT COMMENT '工序ID',
    `report_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报工日期',
    `good_quantity` INT DEFAULT 0 COMMENT '合格数量',
    `bad_quantity` INT DEFAULT 0 COMMENT '不合格数量',
    INDEX `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报工记录表';

CREATE TABLE IF NOT EXISTS `quality_task` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '质检任务ID',
    `task_no` VARCHAR(50) UNIQUE COMMENT '任务编号',
    `task_type` VARCHAR(20) COMMENT '任务类型',
    `source_id` BIGINT COMMENT '来源ID',
    `inspector_id` BIGINT COMMENT '检验员ID',
    `status` INT DEFAULT 0 COMMENT '任务状态(0待处理,1处理中,2已完成)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_task_no` (`task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检任务表';

CREATE TABLE IF NOT EXISTS `quality_result` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '质检结果ID',
    `task_id` BIGINT COMMENT '质检任务ID',
    `check_item` VARCHAR(200) COMMENT '检验项目',
    `standard_value` VARCHAR(200) COMMENT '标准值',
    `actual_value` VARCHAR(200) COMMENT '实际值',
    `result` INT COMMENT '检验结果(0不合格,1合格)',
    `remark` TEXT COMMENT '备注',
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检结果表';

CREATE TABLE IF NOT EXISTS `equipment_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    `equipment_code` VARCHAR(50) UNIQUE COMMENT '设备编码',
    `equipment_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `equipment_type` VARCHAR(50) COMMENT '设备类型',
    `status` INT DEFAULT 1 COMMENT '设备状态(0停用,1运行,2维修)',
    `location` VARCHAR(100) COMMENT '设备位置',
    `purchase_date` DATETIME COMMENT '购入日期',
    `last_maintain_date` DATETIME COMMENT '上次保养日期',
    `next_maintain_date` DATETIME COMMENT '下次保养日期',
    `running_hours` DECIMAL(10,2) DEFAULT 0 COMMENT '运行时长(小时)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_equipment_code` (`equipment_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

CREATE TABLE IF NOT EXISTS `equipment_maintain` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '保养记录ID',
    `maintain_no` VARCHAR(50) UNIQUE COMMENT '保养单号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `maintain_type` VARCHAR(50) COMMENT '保养类型(日常保养/定期保养/专项保养)',
    `maintain_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '保养日期',
    `maintain_content` TEXT COMMENT '保养内容',
    `maintain_result` VARCHAR(20) COMMENT '保养结果(正常/异常/待处理)',
    `maintainer_id` BIGINT COMMENT '保养人ID',
    `maintainer_name` VARCHAR(50) COMMENT '保养人姓名',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_maintain_no` (`maintain_no`),
    INDEX `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备保养记录表';

CREATE TABLE IF NOT EXISTS `exception_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '异常ID',
    `exception_no` VARCHAR(50) UNIQUE COMMENT '异常编号',
    `exception_type` VARCHAR(50) COMMENT '异常类型',
    `work_order_id` BIGINT COMMENT '工单ID',
    `description` TEXT COMMENT '异常描述',
    `reporter_id` BIGINT COMMENT '上报人ID',
    `status` INT DEFAULT 0 COMMENT '处理状态(0待处理,1处理中,2已解决)',
    `report_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    `handle_time` DATETIME COMMENT '处理时间',
    INDEX `idx_exception_no` (`exception_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常记录表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT COMMENT '角色ID',
    `permission_id` BIGINT COMMENT '权限ID',
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '用户ID',
    `user_name` VARCHAR(50) COMMENT '用户名',
    `operation_type` VARCHAR(50) COMMENT '操作类型',
    `operation_module` VARCHAR(50) COMMENT '操作模块',
    `operation_desc` VARCHAR(200) COMMENT '操作描述',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

CREATE TABLE IF NOT EXISTS `equipment_fault` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '故障ID',
    `fault_no` VARCHAR(50) UNIQUE COMMENT '故障编号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `fault_level` INT COMMENT '故障等级(1一般/2严重/3紧急)',
    `fault_desc` TEXT COMMENT '故障描述',
    `reporter` VARCHAR(50) COMMENT '报修人',
    `report_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
    `status` INT DEFAULT 0 COMMENT '状态(0待处理/1维修中/2已修复/3无法修复)',
    `handler` VARCHAR(50) COMMENT '处理人',
    `solution` TEXT COMMENT '处理方案',
    `repair_hours` DECIMAL(5,1) COMMENT '维修时长(小时)',
    `handle_time` DATETIME COMMENT '处理时间',
    `process` VARCHAR(100) COMMENT '所在工序',
    `remark` TEXT COMMENT '备注',
    `assignee_id` BIGINT COMMENT '派工维修人员ID',
    `assign_time` DATETIME COMMENT '派工时间',
    `assign_type` VARCHAR(20) COMMENT '派工类型(manual手动/auto自动)',
    INDEX `idx_fault_no` (`fault_no`),
    INDEX `idx_equipment_id` (`equipment_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障记录表';

-- ========== 第二部分：初始化数据 ==========

-- 禁用外键检查，清空所有表数据
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `ai_chat_history`;
TRUNCATE TABLE `operation_log`;
TRUNCATE TABLE `role_permission`;
TRUNCATE TABLE `sys_permission`;
TRUNCATE TABLE `sys_dict`;
TRUNCATE TABLE `production_report`;
TRUNCATE TABLE `quality_result`;
TRUNCATE TABLE `quality_task`;
TRUNCATE TABLE `work_order`;
TRUNCATE TABLE `stock_out`;
TRUNCATE TABLE `stock_in`;
TRUNCATE TABLE `stock_info`;
TRUNCATE TABLE `purchase_order`;
TRUNCATE TABLE `order_customer`;
TRUNCATE TABLE `equipment_maintain`;
TRUNCATE TABLE `equipment_fault`;
TRUNCATE TABLE `equipment_info`;
TRUNCATE TABLE `exception_record`;
TRUNCATE TABLE `process_info`;
TRUNCATE TABLE `production_plan`;
TRUNCATE TABLE `material_info`;
TRUNCATE TABLE `warehouse_info`;
TRUNCATE TABLE `supplier_info`;
TRUNCATE TABLE `product_info`;
TRUNCATE TABLE `customer_info`;
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `sys_role`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 角色数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'admin', '系统超级管理员，拥有所有权限'),
(2, '订单专员', 'order_clerk', '负责订单管理相关工作'),
(3, '采购专员', 'purchase_clerk', '负责采购管理相关工作'),
(4, '仓库管理员', 'warehouse_keeper', '负责仓储管理相关工作'),
(5, '生产主管', 'production_manager', '负责生产管理相关工作'),
(6, '质检人员', 'quality_inspector', '负责质量管理相关工作'),
(7, '一线工人', 'worker', '负责生产报工、异常上报、设备维修');

-- 2. 用户数据（默认密码: 123456，BCrypt加密）
INSERT INTO `sys_user` (`id`, `user_name`, `password`, `real_name`, `phone`, `email`, `role_id`, `status`) VALUES
(1, 'admin', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '系统管理员', '13800138000', 'admin@mes.com', 1, 1),
(2, 'order_clerk', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '王订单', '13800138001', 'order@mes.com', 2, 1),
(3, 'purchase_clerk', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '李采购', '13800138002', 'purchase@mes.com', 3, 1),
(4, 'warehouse_keeper', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '张仓管', '13800138003', 'warehouse@mes.com', 4, 1),
(5, 'production_manager', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '赵生产', '13800138004', 'production@mes.com', 5, 1),
(6, 'quality_inspector', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '陈质检', '13800138005', 'quality@mes.com', 6, 1),
(7, 'worker001', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '张三', '13800138006', 'worker001@mes.com', 7, 1),
(8, 'worker002', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '李四', '13800138007', 'worker002@mes.com', 7, 1),
(9, 'worker003', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '王五', '13800138008', 'worker003@mes.com', 7, 1),
(10, 'repair_zhang', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '张维修', '13800138009', 'repair1@mes.com', 7, 1),
(11, 'repair_li', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '李维修', '13800138010', 'repair2@mes.com', 7, 1);

-- 3. 权限数据
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `resource_type`, `resource_path`, `parent_id`) VALUES
(1, '订单管理', 'order:manage', 'menu', '/order', NULL),
(2, '订单列表', 'order:list', 'button', '/order/list', 1),
(3, '订单创建', 'order:create', 'button', '/order/create', 1),
(4, '订单审批', 'order:approve', 'button', '/order/approve', 1),
(5, '采购管理', 'purchase:manage', 'menu', '/purchase', NULL),
(6, '采购订单', 'purchase:order', 'button', '/purchase/order', 5),
(7, '供应商管理', 'purchase:supplier', 'button', '/purchase/supplier', 5),
(8, '仓储管理', 'stock:manage', 'menu', '/stock', NULL),
(9, '入库管理', 'stock:in', 'button', '/stock/in', 8),
(10, '出库管理', 'stock:out', 'button', '/stock/out', 8),
(11, '库存查询', 'stock:query', 'button', '/stock/query', 8),
(12, '生产管理', 'production:manage', 'menu', '/production', NULL),
(13, '生产计划', 'production:plan', 'button', '/production/plan', 12),
(14, '工单管理', 'production:workorder', 'button', '/production/workorder', 12),
(15, '生产报工', 'production:report', 'button', '/production/report', 12),
(16, '质量管理', 'quality:manage', 'menu', '/quality', NULL),
(17, '质检任务', 'quality:task', 'button', '/quality/task', 16),
(18, '质检录入', 'quality:record', 'button', '/quality/record', 16),
(19, '设备管理', 'equipment:manage', 'menu', '/equipment', NULL),
(20, '设备台账', 'equipment:list', 'button', '/equipment/list', 19),
(21, '设备维护', 'equipment:maintain', 'button', '/equipment/maintain', 19),
(22, '系统管理', 'system:manage', 'menu', '/system', NULL),
(23, '用户管理', 'system:user', 'button', '/system/user', 22),
(24, '角色管理', 'system:role', 'button', '/system/role', 22),
(25, '数据字典', 'system:dict', 'button', '/system/dict', 22),
(26, '异常管理', 'exception:manage', 'menu', '/exception', NULL),
(27, '异常上报', 'exception:report', 'button', '/exception/report', 26),
(28, '异常处理', 'exception:handle', 'button', '/exception/handle', 26),
(29, '设备故障', 'equipment:fault', 'button', '/equipment/fault', 19),
(30, '维修任务', 'repair:task', 'button', '/repair/task', 19);

-- 4. 角色权限关联
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),(1, 7),(1, 8),(1, 9),(1, 10),
(1, 11),(1, 12),(1, 13),(1, 14),(1, 15),(1, 16),(1, 17),(1, 18),(1, 19),
(1, 20),(1, 21),(1, 22),(1, 23),(1, 24),(1, 25),(1, 26),(1, 27),(1, 28),(1,29),(1,30),
(2, 1),(2, 2),(2, 3),(2, 4),
(3, 5),(3, 6),(3, 7),
(4, 8),(4, 9),(4, 10),(4, 11),
(5, 12),(5, 13),(5, 14),(5, 15),(5, 19),(5, 20),(5, 21),(5,29),(5,30),
(6, 16),(6, 17),(6, 18),
(7, 12),(7, 14),(7, 15),(7, 26),(7, 27),(7,29),(7,30);

-- 5. 数据字典
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('order_status', '0', '待审批', 1, 1),
('order_status', '1', '已审批', 2, 1),
('order_status', '2', '生产中', 3, 1),
('order_status', '3', '已完成', 4, 1),
('order_status', '4', '已取消', 5, 1),
('purchase_status', '0', '待审批', 1, 1),
('purchase_status', '1', '已审批', 2, 1),
('purchase_status', '2', '已到货', 3, 1),
('purchase_status', '3', '已完成', 4, 1),
('plan_status', '0', '待执行', 1, 1),
('plan_status', '1', '执行中', 2, 1),
('plan_status', '2', '已完成', 3, 1),
('workorder_status', '0', '待派发', 1, 1),
('workorder_status', '1', '执行中', 2, 1),
('workorder_status', '2', '已完成', 3, 1),
('quality_status', '0', '待处理', 1, 1),
('quality_status', '1', '处理中', 2, 1),
('quality_status', '2', '已完成', 3, 1),
('quality_result', '0', '不合格', 1, 1),
('quality_result', '1', '合格', 2, 1),
('equipment_status', '0', '停用', 1, 1),
('equipment_status', '1', '运行', 2, 1),
('equipment_status', '2', '维修', 3, 1),
('exception_status', '0', '待处理', 1, 1),
('exception_status', '1', '处理中', 2, 1),
('exception_status', '2', '已解决', 3, 1),
('exception_type', 'quality', '质量问题', 1, 1),
('exception_type', 'equipment', '设备故障', 2, 1),
('exception_type', 'material', '物料不足', 3, 1),
('exception_type', 'safety', '安全问题', 4, 1),
('exception_type', 'other', '其他', 5, 1),
('unit', 'pcs', '个', 1, 1),
('unit', 'box', '箱', 2, 1),
('unit', 'kg', '千克', 3, 1),
('unit', 'm', '米', 4, 1),
('unit', 'set', '套', 5, 1),
('common_status', '0', '禁用', 1, 1),
('common_status', '1', '启用', 2, 1);

-- 6. 客户数据（8条）
INSERT INTO `customer_info` (`id`, `customer_code`, `customer_name`, `contact_name`, `phone`, `email`, `address`, `status`) VALUES
(1, 'C001', '华为技术有限公司', '张总', '13800000001', 'huawei@example.com', '深圳市龙岗区', 1),
(2, 'C002', '小米通讯技术有限公司', '雷总', '13800000002', 'xiaomi@example.com', '北京市海淀区', 1),
(3, 'C003', 'OPPO广东移动通信有限公司', '陈总', '13800000003', 'oppo@example.com', '东莞市长安镇', 1),
(4, 'C004', 'vivo移动通信有限公司', '沈总', '13800000004', 'vivo@example.com', '东莞市长安镇', 1),
(5, 'C005', '北京京东世纪贸易有限公司', '刘总', '13800000005', 'jd@example.com', '北京市大兴区', 1),
(6, 'C006', '苏宁易购集团股份有限公司', '孙总', '13800000006', 'suning@example.com', '南京市玄武区', 1),
(7, 'C007', '深圳大疆创新科技有限公司', '汪总', '13800000007', 'dji@example.com', '深圳市南山区', 1),
(8, 'C008', '杭州阿里巴巴集团', '马总', '13800000008', 'alibaba@example.com', '杭州市余杭区', 1);

-- 7. 产品数据（6种）
INSERT INTO `product_info` (`id`, `product_code`, `product_name`, `product_type`, `specification`, `unit_price`, `status`) VALUES
(1, 'P001', '智能手环X1 Pro', '手环', '黑色/尊享版/防水IP68', 499.00, 1),
(2, 'P002', '智能手环S2', '手环', '蓝色/运动版/心率监测', 399.00, 1),
(3, 'P003', '智能手环Kids', '手环', '粉色/儿童版/定位功能', 299.00, 1),
(4, 'P004', '智能手环Sport', '手环', '绿色/专业运动版/GPS', 599.00, 1),
(5, 'P005', '智能手环Elder', '手环', '白色/老人健康版/血压监测', 399.00, 1),
(6, 'P006', '智能手环Lite', '手环', '灰色/基础版/计步睡眠', 199.00, 1);

-- 8. 供应商数据（6条）
INSERT INTO `supplier_info` (`id`, `supplier_name`, `contact_name`, `phone`, `address`, `status`) VALUES
(1, '深圳鑫达电子有限公司', '王经理', '13900000001', '深圳市宝安区', 1),
(2, '东莞精密塑胶制品厂', '李厂长', '13900000002', '东莞市长安镇', 1),
(3, '上海芯片技术有限公司', '赵总', '13900000003', '上海市浦东新区', 1),
(4, '苏州显示屏科技有限公司', '钱经理', '13900000004', '苏州市工业园区', 1),
(5, '惠州电池能源有限公司', '孙总', '13900000005', '惠州市惠城区', 1),
(6, '厦门传感器制造厂', '周工', '13900000006', '厦门市集美区', 1);

-- 9. 物料数据（12条）
INSERT INTO `material_info` (`id`, `material_code`, `material_name`, `material_type`, `unit`, `unit_price`, `safety_stock`, `status`) VALUES
(1, 'M001', '主控芯片STM32', '电子件', 'pcs', 45.00, 2000, 1),
(2, 'M002', 'OLED显示屏0.96寸', '电子件', 'pcs', 28.00, 1500, 1),
(3, 'M003', '硅胶表带', '配件', 'pcs', 5.50, 5000, 1),
(4, 'M004', '锂电池3.7V 200mAh', '电子件', 'pcs', 12.00, 3000, 1),
(5, 'M005', '心率传感器MAX30102', '电子件', 'pcs', 18.00, 2000, 1),
(6, 'M006', '蓝牙模块BLE5.0', '电子件', 'pcs', 15.00, 2500, 1),
(7, 'M007', '六轴加速度计', '电子件', 'pcs', 8.00, 2000, 1),
(8, 'M008', '塑胶外壳ABS', '结构件', 'pcs', 3.20, 4000, 1),
(9, 'M009', '充电器Type-C', '配件', 'pcs', 6.00, 3000, 1),
(10, 'M010', '彩盒包装', '包材', 'pcs', 2.50, 5000, 1),
(11, 'M011', '防水圈硅胶', '结构件', 'pcs', 0.80, 10000, 1),
(12, 'M012', 'GPS定位模组', '电子件', 'pcs', 35.00, 1000, 1);

-- 10. 仓库数据（3个）
INSERT INTO `warehouse_info` (`id`, `warehouse_name`, `warehouse_code`, `address`, `status`) VALUES
(1, '主原料仓', 'WH001', '厂区A栋1楼', 1),
(2, '成品仓', 'WH002', '厂区B栋1楼', 1),
(3, '半成品仓', 'WH003', '厂区A栋2楼', 1);

-- 11. 工序数据（5个）
INSERT INTO `process_info` (`id`, `process_name`, `process_code`, `product_id`, `sort_order`) VALUES
(1, '主板贴片', 'PR001', 1, 1),
(2, '屏幕组装', 'PR002', 1, 2),
(3, '外壳组装', 'PR003', 1, 3),
(4, '功能测试', 'PR004', 1, 4),
(5, '包装入库', 'PR005', 1, 5);

-- 12. 设备数据（10条）
INSERT INTO `equipment_info` (`id`, `equipment_code`, `equipment_name`, `equipment_type`, `status`, `location`, `purchase_date`, `last_maintain_date`, `next_maintain_date`, `running_hours`) VALUES
(1, 'EQ001', 'SMT贴片机01', '贴片设备', 1, '车间A-01', '2024-01-15', '2026-06-15', '2026-08-15', 4200.50),
(2, 'EQ002', 'SMT贴片机02', '贴片设备', 1, '车间A-02', '2024-01-20', '2026-06-20', '2026-08-20', 3850.00),
(3, 'EQ003', '回流焊炉01', '焊接设备', 1, '车间A-03', '2024-02-10', '2026-06-10', '2026-08-10', 3600.00),
(4, 'EQ004', '自动组装线01', '组装设备', 1, '车间B-01', '2024-03-05', '2026-07-01', '2026-09-01', 2900.00),
(5, 'EQ005', '自动组装线02', '组装设备', 1, '车间B-02', '2024-03-10', '2026-07-05', '2026-09-05', 2750.00),
(6, 'EQ006', 'AOI自动光学检测仪', '检测设备', 1, '车间C-01', '2024-04-15', '2026-06-15', '2026-08-15', 2100.00),
(7, 'EQ007', '老化测试柜', '检测设备', 1, '车间C-02', '2024-05-20', '2026-06-20', '2026-08-20', 1800.00),
(8, 'EQ008', '自动包装机01', '包装设备', 1, '车间D-01', '2024-06-01', '2026-07-01', '2026-09-01', 1500.00),
(9, 'EQ009', '激光打标机', '辅助设备', 2, '车间D-02', '2024-06-15', '2026-06-15', '2026-07-20', 1200.00),
(10, 'EQ010', '波峰焊机', '焊接设备', 0, '车间A-04', '2023-08-10', '2025-12-10', '2026-02-10', 5600.00);

-- 13. 设备保养记录（8条）
INSERT INTO `equipment_maintain` (`id`, `maintain_no`, `equipment_id`, `maintain_type`, `maintain_date`, `maintain_content`, `maintain_result`, `maintainer_id`, `maintainer_name`, `remark`) VALUES
(1, 'MN20260701001', 1, '定期保养', '2026-07-01 09:00:00', '清洁吸嘴、校准贴片精度、更换过滤器', '正常', 7, '张三', '按计划执行'),
(2, 'MN20260702001', 2, '定期保养', '2026-07-02 14:00:00', '清洁吸嘴、校准贴片精度、润滑导轨', '正常', 8, '李四', '按计划执行'),
(3, 'MN20260703001', 3, '日常保养', '2026-07-03 08:30:00', '清洁炉膛、检查温控系统', '正常', 7, '张三', '温度偏差在范围内'),
(4, 'MN20260705001', 4, '定期保养', '2026-07-05 10:00:00', '检查传送带张力、润滑机械臂', '正常', 8, '李四', '运行平稳'),
(5, 'MN20260708001', 6, '日常保养', '2026-07-08 16:00:00', '清洁镜头、校准光源', '正常', 9, '王五', '检测精度正常'),
(6, 'MN20260710001', 7, '定期保养', '2026-07-10 09:00:00', '检查温控模块、清洁内部灰尘', '异常', 10, '张维修', '发现温控模块老化，已报修'),
(7, 'MN20260712001', 8, '日常保养', '2026-07-12 15:00:00', '清洁封切口、检查温控', '正常', 9, '王五', '包装质量稳定'),
(8, 'MN20260715001', 9, '专项保养', '2026-07-15 11:00:00', '更换激光管、校准焦距', '待处理', 11, '李维修', '激光管到货后更换');

-- 14. 订单数据（15条）
INSERT INTO `order_customer` (`id`, `order_no`, `customer_id`, `product_id`, `quantity`, `unit_price`, `total_amount`, `status`, `delivery_date`, `create_time`, `update_time`) VALUES
(1, 'ORD20260701001', 1, 1, 2000, 499.00, 998000.00, 3, '2026-07-10', '2026-07-01 09:00:00', '2026-07-10 18:00:00'),
(2, 'ORD20260702001', 2, 2, 1500, 399.00, 598500.00, 2, '2026-07-18', '2026-07-02 10:30:00', '2026-07-12 14:00:00'),
(3, 'ORD20260703001', 3, 1, 3000, 499.00, 1497000.00, 2, '2026-07-20', '2026-07-03 08:00:00', '2026-07-13 09:00:00'),
(4, 'ORD20260704001', 4, 3, 1000, 299.00, 299000.00, 1, '2026-07-25', '2026-07-04 14:00:00', '2026-07-05 10:00:00'),
(5, 'ORD20260705001', 5, 4, 800, 599.00, 479200.00, 0, '2026-07-30', '2026-07-05 11:00:00', '2026-07-05 11:00:00'),
(6, 'ORD20260706001', 6, 2, 2500, 399.00, 997500.00, 1, '2026-07-22', '2026-07-06 09:30:00', '2026-07-07 16:00:00'),
(7, 'ORD20260707001', 7, 5, 1200, 399.00, 478800.00, 2, '2026-07-19', '2026-07-07 10:00:00', '2026-07-14 11:00:00'),
(8, 'ORD20260708001', 8, 6, 5000, 199.00, 995000.00, 0, '2026-08-05', '2026-07-08 08:00:00', '2026-07-08 08:00:00'),
(9, 'ORD20260709001', 1, 4, 600, 599.00, 359400.00, 1, '2026-07-28', '2026-07-09 13:00:00', '2026-07-10 09:00:00'),
(10, 'ORD20260710001', 2, 3, 1800, 299.00, 538200.00, 2, '2026-07-17', '2026-07-10 09:00:00', '2026-07-12 10:00:00'),
(11, 'ORD20260711001', 3, 6, 3500, 199.00, 696500.00, 0, '2026-08-01', '2026-07-11 10:00:00', '2026-07-11 10:00:00'),
(12, 'ORD20260712001', 4, 1, 2200, 499.00, 1097800.00, 2, '2026-07-18', '2026-07-12 08:00:00', '2026-07-14 15:00:00'),
(13, 'ORD20260713001', 5, 5, 900, 399.00, 359100.00, 1, '2026-07-26', '2026-07-13 11:00:00', '2026-07-14 09:00:00'),
(14, 'ORD20260714001', 6, 2, 1600, 399.00, 638400.00, 4, '2026-07-20', '2026-07-14 09:00:00', '2026-07-15 10:00:00'),
(15, 'ORD20260715001', 7, 4, 700, 599.00, 419300.00, 0, '2026-07-29', '2026-07-15 08:30:00', '2026-07-15 08:30:00');

-- 15. 采购数据（10条）
INSERT INTO `purchase_order` (`id`, `purchase_no`, `supplier_id`, `material_id`, `quantity`, `unit_price`, `total_amount`, `status`, `expected_date`, `create_time`) VALUES
(1, 'PO20260701001', 1, 1, 5000, 45.00, 225000.00, 3, '2026-07-05', '2026-07-01 09:00:00'),
(2, 'PO20260702001', 4, 2, 3000, 28.00, 84000.00, 2, '2026-07-10', '2026-07-02 10:00:00'),
(3, 'PO20260703001', 2, 8, 8000, 3.20, 25600.00, 1, '2026-07-15', '2026-07-03 11:00:00'),
(4, 'PO20260704001', 5, 4, 4000, 12.00, 48000.00, 0, '2026-07-20', '2026-07-04 09:00:00'),
(5, 'PO20260705001', 3, 6, 3500, 15.00, 52500.00, 2, '2026-07-12', '2026-07-05 14:00:00'),
(6, 'PO20260706001', 6, 5, 2500, 18.00, 45000.00, 1, '2026-07-18', '2026-07-06 08:30:00'),
(7, 'PO20260707001', 1, 7, 3000, 8.00, 24000.00, 3, '2026-07-08', '2026-07-07 10:00:00'),
(8, 'PO20260708001', 2, 3, 10000, 5.50, 55000.00, 2, '2026-07-14', '2026-07-08 09:00:00'),
(9, 'PO20260709001', 4, 12, 1500, 35.00, 52500.00, 0, '2026-07-25', '2026-07-09 11:00:00'),
(10, 'PO20260710001', 5, 11, 20000, 0.80, 16000.00, 1, '2026-07-22', '2026-07-10 13:00:00');

-- 16. 库存数据（15条）
INSERT INTO `stock_info` (`id`, `material_id`, `warehouse_id`, `quantity`, `location`) VALUES
(1, 1, 1, 8500, 'A-01-01'),
(2, 2, 1, 3200, 'A-01-02'),
(3, 3, 1, 15000, 'A-01-03'),
(4, 4, 1, 6800, 'A-01-04'),
(5, 5, 1, 4200, 'A-01-05'),
(6, 6, 1, 5500, 'A-02-01'),
(7, 7, 1, 3800, 'A-02-02'),
(8, 8, 1, 12000, 'A-02-03'),
(9, 9, 1, 7500, 'A-02-04'),
(10, 10, 1, 9500, 'A-03-01'),
(11, 11, 1, 850, 'A-03-02'),
(12, 12, 1, 1800, 'A-03-03'),
(13, 1, 3, 4200, 'C-01-01'),
(14, 2, 3, 2100, 'C-01-02'),
(15, 3, 3, 6500, 'C-02-01');

-- 17. 入库数据（6条）
INSERT INTO `stock_in` (`id`, `in_no`, `material_id`, `warehouse_id`, `quantity`, `source_type`, `source_no`, `operator_id`, `status`, `create_time`) VALUES
(1, 'IN20260712001', 1, 1, 3000, 'purchase', 'PO20260701001', 4, 1, '2026-07-12 09:00:00'),
(2, 'IN20260713001', 2, 1, 2000, 'purchase', 'PO20260702001', 4, 1, '2026-07-13 10:30:00'),
(3, 'IN20260714001', 6, 1, 1500, 'purchase', 'PO20260705001', 4, 1, '2026-07-14 14:00:00'),
(4, 'IN20260715001', 7, 1, 2000, 'purchase', 'PO20260707001', 4, 1, '2026-07-15 09:00:00'),
(5, 'IN20260716001', 3, 1, 5000, 'purchase', 'PO20260708001', 4, 1, '2026-07-16 11:00:00'),
(6, 'IN20260717001', 4, 1, 2500, 'purchase', 'PO20260705001', 4, 1, '2026-07-17 08:30:00');

-- 18. 出库数据（6条）
INSERT INTO `stock_out` (`id`, `out_no`, `material_id`, `warehouse_id`, `quantity`, `target_type`, `target_no`, `operator_id`, `status`, `create_time`) VALUES
(1, 'OUT20260712001', 1, 1, 800, 'production', 'WO20260712001', 7, 1, '2026-07-12 08:00:00'),
(2, 'OUT20260713001', 2, 1, 600, 'production', 'WO20260712002', 7, 1, '2026-07-13 08:00:00'),
(3, 'OUT20260714001', 3, 1, 1200, 'production', 'WO20260713001', 8, 1, '2026-07-14 08:00:00'),
(4, 'OUT20260715001', 4, 1, 900, 'production', 'WO20260714001', 8, 1, '2026-07-15 08:00:00'),
(5, 'OUT20260716001', 5, 1, 500, 'production', 'WO20260715001', 9, 1, '2026-07-16 08:00:00'),
(6, 'OUT20260717001', 6, 1, 700, 'production', 'WO20260716001', 9, 1, '2026-07-17 08:00:00');

-- 19. 工单数据（16条，关联报工数据）
INSERT INTO `work_order` (`id`, `work_no`, `plan_id`, `product_id`, `process_id`, `quantity`, `status`, `manager`, `remark`, `plan_start_date`, `plan_end_date`, `create_time`) VALUES
(1, 'WO20260712001', NULL, 1, 1, 500, 2, '赵生产', '紧急订单，优先排产', '2026-07-12', '2026-07-15', '2026-07-12 08:00:00'),
(2, 'WO20260712002', NULL, 1, 2, 500, 2, '赵生产', '', '2026-07-12', '2026-07-15', '2026-07-12 09:00:00'),
(3, 'WO20260713001', NULL, 2, 1, 400, 2, '赵生产', '', '2026-07-13', '2026-07-16', '2026-07-13 08:00:00'),
(4, 'WO20260713002', NULL, 2, 2, 400, 2, '赵生产', '', '2026-07-13', '2026-07-16', '2026-07-13 09:00:00'),
(5, 'WO20260714001', NULL, 3, 1, 300, 2, '赵生产', '新订单排产', '2026-07-14', '2026-07-17', '2026-07-14 08:00:00'),
(6, 'WO20260714002', NULL, 3, 2, 300, 1, '赵生产', '', '2026-07-14', '2026-07-17', '2026-07-14 09:00:00'),
(7, 'WO20260715001', NULL, 4, 1, 200, 2, '赵生产', '', '2026-07-15', '2026-07-18', '2026-07-15 08:00:00'),
(8, 'WO20260715002', NULL, 4, 2, 200, 1, '赵生产', '', '2026-07-15', '2026-07-18', '2026-07-15 09:00:00'),
(9, 'WO20260716001', NULL, 5, 1, 250, 1, '赵生产', '', '2026-07-16', '2026-07-19', '2026-07-16 08:00:00'),
(10, 'WO20260716002', NULL, 5, 2, 250, 0, '赵生产', '', '2026-07-16', '2026-07-19', '2026-07-16 09:00:00'),
(11, 'WO20260717001', NULL, 6, 1, 600, 1, '赵生产', '', '2026-07-17', '2026-07-20', '2026-07-17 08:00:00'),
(12, 'WO20260717002', NULL, 6, 2, 600, 0, '赵生产', '', '2026-07-17', '2026-07-20', '2026-07-17 09:00:00'),
(13, 'WO20260718001', NULL, 1, 1, 350, 0, '赵生产', '追加订单', '2026-07-18', '2026-07-22', '2026-07-18 08:00:00'),
(14, 'WO20260718002', NULL, 2, 1, 280, 0, '赵生产', '', '2026-07-18', '2026-07-22', '2026-07-18 09:00:00'),
(15, 'WO20260719001', NULL, 3, 1, 220, 0, '赵生产', '答辩日排产', '2026-07-19', '2026-07-23', '2026-07-19 08:00:00'),
(16, 'WO20260719002', NULL, 4, 1, 180, 0, '赵生产', '', '2026-07-19', '2026-07-23', '2026-07-19 09:00:00');

-- 19.1 生产计划数据（8条）
INSERT INTO `production_plan` (`id`, `plan_no`, `plan_name`, `order_id`, `product_id`, `planned_quantity`, `start_date`, `end_date`, `status`, `manager`, `remark`, `create_time`) VALUES
(1, 'PP20260710001', '7月中旬Pro手环生产计划', 1, 1, 1000, '2026-07-12', '2026-07-18', 2, '赵生产', '华为订单，优先排产', '2026-07-10 10:00:00'),
(2, 'PP20260710002', '7月中旬S2手环生产计划', 2, 2, 800, '2026-07-13', '2026-07-19', 2, '赵生产', '小米订单', '2026-07-10 11:00:00'),
(3, 'PP20260711001', '7月中旬Kids手环计划', 4, 3, 600, '2026-07-14', '2026-07-20', 1, '赵生产', 'OPPO订单', '2026-07-11 09:00:00'),
(4, 'PP20260712001', 'Sport手环试产计划', 5, 4, 400, '2026-07-15', '2026-07-21', 1, '赵生产', '京东新品试产', '2026-07-12 08:30:00'),
(5, 'PP20260713001', 'Elder手环生产计划', 7, 5, 500, '2026-07-16', '2026-07-22', 1, '赵生产', '大疆健康版', '2026-07-13 14:00:00'),
(6, 'PP20260714001', 'Lite手环量产计划', 8, 6, 1200, '2026-07-17', '2026-07-23', 0, '赵生产', '阿里批量订单', '2026-07-14 10:00:00'),
(7, 'PP20260715001', 'Pro手环追加计划', 3, 1, 700, '2026-07-18', '2026-07-24', 0, '赵生产', 'OPPO追加订单', '2026-07-15 09:00:00'),
(8, 'PP20260716001', 'S2手环补货计划', 6, 2, 560, '2026-07-19', '2026-07-25', 0, '赵生产', '苏宁补货', '2026-07-16 11:00:00');

-- 20. 生产报工数据（8天×5工序=40条，日期范围2026-07-12到2026-07-19）
INSERT INTO `production_report` (`id`, `work_order_id`, `worker_id`, `process_id`, `report_date`, `good_quantity`, `bad_quantity`) VALUES
-- 2026-07-12
(1, 1, 7, 1, '2026-07-12 10:30:00', 498, 2),
(2, 1, 7, 1, '2026-07-12 16:00:00', 500, 0),
(3, 2, 8, 2, '2026-07-12 11:00:00', 495, 5),
(4, 2, 8, 2, '2026-07-12 17:00:00', 500, 0),
-- 2026-07-13
(5, 3, 7, 1, '2026-07-13 10:00:00', 398, 2),
(6, 3, 7, 1, '2026-07-13 16:00:00', 400, 0),
(7, 4, 8, 2, '2026-07-13 11:00:00', 395, 5),
(8, 4, 8, 2, '2026-07-13 17:00:00', 400, 0),
-- 2026-07-14
(9, 5, 7, 1, '2026-07-14 10:00:00', 298, 2),
(10, 5, 7, 1, '2026-07-14 16:00:00', 300, 0),
(11, 6, 8, 2, '2026-07-14 11:00:00', 295, 5),
(12, 6, 8, 2, '2026-07-14 17:00:00', 298, 2),
-- 2026-07-15
(13, 7, 7, 1, '2026-07-15 10:00:00', 198, 2),
(14, 7, 7, 1, '2026-07-15 16:00:00', 200, 0),
(15, 8, 8, 2, '2026-07-15 11:00:00', 198, 2),
(16, 8, 8, 2, '2026-07-15 17:00:00', 200, 0),
-- 2026-07-16
(17, 9, 7, 1, '2026-07-16 10:00:00', 248, 2),
(18, 9, 7, 1, '2026-07-16 16:00:00', 250, 0),
(19, 10, 8, 2, '2026-07-16 11:00:00', 0, 0),
(20, 10, 8, 2, '2026-07-16 17:00:00', 0, 0),
-- 2026-07-17
(21, 11, 7, 1, '2026-07-17 10:00:00', 598, 2),
(22, 11, 7, 1, '2026-07-17 16:00:00', 600, 0),
(23, 12, 8, 2, '2026-07-17 11:00:00', 0, 0),
(24, 12, 8, 2, '2026-07-17 17:00:00', 0, 0),
-- 2026-07-18
(25, 13, 7, 1, '2026-07-18 10:00:00', 175, 2),
(26, 13, 7, 1, '2026-07-18 16:00:00', 173, 0),
(27, 14, 8, 1, '2026-07-18 10:00:00', 140, 1),
(28, 14, 8, 1, '2026-07-18 16:00:00', 139, 0),
-- 2026-07-19（答辩当天）
(29, 15, 7, 1, '2026-07-19 10:00:00', 110, 1),
(30, 15, 7, 1, '2026-07-19 16:00:00', 109, 0),
(31, 16, 8, 1, '2026-07-19 10:00:00', 90, 1),
(32, 16, 8, 1, '2026-07-19 16:00:00', 89, 0);

-- 21. 质检任务数据（8条）
INSERT INTO `quality_task` (`id`, `task_no`, `task_type`, `source_id`, `inspector_id`, `status`, `create_time`) VALUES
(1, 'QT20260712001', 'IPQC', 1, 6, 2, '2026-07-12 14:00:00'),
(2, 'QT20260713001', 'IPQC', 3, 6, 2, '2026-07-13 14:00:00'),
(3, 'QT20260714001', 'IPQC', 5, 6, 1, '2026-07-14 14:00:00'),
(4, 'QT20260715001', 'IPQC', 7, 6, 1, '2026-07-15 14:00:00'),
(5, 'QT20260716001', 'IPQC', 9, 6, 0, '2026-07-16 14:00:00'),
(6, 'QT20260717001', 'IPQC', 11, 6, 0, '2026-07-17 14:00:00'),
(7, 'QT20260718001', 'OQC', NULL, 6, 0, '2026-07-18 10:00:00'),
(8, 'QT20260719001', 'IQC', NULL, 6, 0, '2026-07-19 09:00:00');

-- 22. 质检结果数据
INSERT INTO `quality_result` (`id`, `task_id`, `check_item`, `standard_value`, `actual_value`, `result`, `remark`) VALUES
(1, 1, '外观检查', '无划痕、无变形', '合格', 1, '抽检100件全部合格'),
(2, 1, '尺寸测量', '长度20±0.1mm', '20.05mm', 1, '在公差范围内'),
(3, 1, '功能初测', '全部功能正常', '正常', 1, '首件测试通过'),
(4, 2, '外观检查', '无划痕、无变形', '合格', 1, '抽检100件全部合格'),
(5, 2, '焊接质量', '无虚焊、假焊', '合格', 1, 'AOI检测通过'),
(6, 2, '贴片精度', '偏移量≤0.1mm', '0.08mm', 1, '符合要求'),
(7, 3, '外观检查', '无划痕、无变形', '待检', 0, ''),
(8, 4, '外观检查', '无划痕、无变形', '待检', 0, '');

-- 23. 异常记录数据（6条）
INSERT INTO `exception_record` (`id`, `exception_no`, `exception_type`, `work_order_id`, `description`, `reporter_id`, `status`, `report_time`, `handle_time`) VALUES
(1, 'EX20260712001', 'equipment', 1, 'SMT贴片机01号飞达进料不畅，导致贴片偏移2件', 7, 2, '2026-07-12 10:45:00', '2026-07-12 14:00:00'),
(2, 'EX20260713001', 'quality', 3, '外壳组装发现5件按键手感不良', 7, 2, '2026-07-13 15:30:00', '2026-07-13 17:00:00'),
(3, 'EX20260714001', 'material', 5, '物料批次B20260710存在色差问题', 7, 1, '2026-07-14 16:45:00', NULL),
(4, 'EX20260715001', 'equipment', 7, '自动组装线传送带轻微打滑', 8, 2, '2026-07-15 11:30:00', '2026-07-15 14:30:00'),
(5, 'EX20260716001', 'other', 9, '车间温度过高，影响焊接质量', 7, 0, '2026-07-16 14:00:00', NULL),
(6, 'EX20260717001', 'material', 11, '蓝牙模块到货延期，导致工单等待', 8, 1, '2026-07-17 09:30:00', NULL);

-- 24. 设备故障数据（6条）
INSERT INTO `equipment_fault` (`id`, `fault_no`, `equipment_id`, `fault_level`, `fault_desc`, `reporter`, `report_time`, `status`, `handler`, `solution`, `repair_hours`, `handle_time`, `process`, `remark`, `assignee_id`, `assign_time`, `assign_type`) VALUES
(1, 'EF20260712001', 1, 2, 'SMT贴片机01号飞达吸嘴磨损，贴片偏移率超过0.3%，严重影响良品率', '张三', '2026-07-12 10:00:00', 2, '张维修', '已更换3号和5号吸嘴，校准贴片头，测试偏移量恢复至0.05%以内', 2.5, '2026-07-12 14:00:00', 'SMT车间A区', '紧急影响了ORD20260712001订单', 10, '2026-07-12 10:15:00', 'auto'),
(2, 'EF20260713001', 4, 1, '自动组装线01传送带轻微打滑，每小时出现2-3次物料偏移', '李四', '2026-07-13 08:30:00', 2, '李维修', '调整传送带张力，清洁滚轮，测试运行2小时正常', 1.5, '2026-07-13 12:00:00', '组装车间B区', '影响较小但需及时处理', 11, '2026-07-13 08:45:00', 'manual'),
(3, 'EF20260714001', 6, 2, 'AOI检测仪镜头模糊，误判率上升至15%，严重影响质检效率', '陈质检', '2026-07-14 09:00:00', 1, '张维修', '正在清洁镜头并校准光源，预计2小时完成', NULL, NULL, '质检车间C区', '已安排张维修处理', 10, '2026-07-14 09:20:00', 'auto'),
(4, 'EF20260715001', 8, 1, '自动包装机封口温度偏低，包装袋封口有3%出现开裂', '王五', '2026-07-15 15:00:00', 0, NULL, NULL, NULL, NULL, '包装车间D区', '问题轻微但需处理', NULL, NULL, NULL),
(5, 'EF20260716001', 3, 3, '回流焊炉温控模块故障，无法升温至工艺要求温度250℃，严重影响生产', '张三', '2026-07-16 07:30:00', 0, NULL, NULL, NULL, NULL, 'SMT车间A区', '紧急！已暂停该产线生产，需立即处理', NULL, NULL, NULL),
(6, 'EF20260717001', 7, 1, '老化测试柜显示屏出现坏点，无法正常显示测试数据', '李四', '2026-07-17 10:00:00', 0, NULL, NULL, NULL, NULL, '质检车间C区', '需要更换显示屏模组', NULL, NULL, NULL);

-- ============================================================
-- 初始化完成！
-- 默认账号：admin / 123456
-- ============================================================
