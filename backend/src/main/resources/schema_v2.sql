-- ============================================================
-- 智能手环MES系统 - 数据库建表脚本 v2.0
-- 版本：v2.0
-- 说明：基于实体类（Entity）构建，确保前后端字段一致
-- 使用方法：在Navicat中选中数据库，直接运行此文件
-- 注意：会先删除所有表，请确保已备份重要数据
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ========== 清理旧表（按依赖顺序反向删除） ==========
DROP TABLE IF EXISTS `sn_quality_trace`;
DROP TABLE IF EXISTS `sn_material_trace`;
DROP TABLE IF EXISTS `sn_process_trace`;
DROP TABLE IF EXISTS `product_sn`;
DROP TABLE IF EXISTS `ai_document_chunk`;
DROP TABLE IF EXISTS `ai_document`;
DROP TABLE IF EXISTS `ai_knowledge_document`;
DROP TABLE IF EXISTS `ai_chat_history`;
DROP TABLE IF EXISTS `process_material_bom`;
DROP TABLE IF EXISTS `process_param_standard`;
DROP TABLE IF EXISTS `process_sop`;
DROP TABLE IF EXISTS `equipment_repair_task`;
DROP TABLE IF EXISTS `equipment_fault`;
DROP TABLE IF EXISTS `equipment_maintain`;
DROP TABLE IF EXISTS `unqualified_product`;
DROP TABLE IF EXISTS `exception_record`;
DROP TABLE IF EXISTS `quality_result`;
DROP TABLE IF EXISTS `quality_task`;
DROP TABLE IF EXISTS `production_report`;
DROP TABLE IF EXISTS `work_order`;
DROP TABLE IF EXISTS `production_plan`;
DROP TABLE IF EXISTS `stock_alert`;
DROP TABLE IF EXISTS `stock_check_detail`;
DROP TABLE IF EXISTS `stock_check`;
DROP TABLE IF EXISTS `stock_out`;
DROP TABLE IF EXISTS `stock_in`;
DROP TABLE IF EXISTS `stock_info`;
DROP TABLE IF EXISTS `process_info`;
DROP TABLE IF EXISTS `equipment_info`;
DROP TABLE IF EXISTS `warehouse_info`;
DROP TABLE IF EXISTS `purchase_order`;
DROP TABLE IF EXISTS `material_info`;
DROP TABLE IF EXISTS `order_customer`;
DROP TABLE IF EXISTS `supplier_info`;
DROP TABLE IF EXISTS `product_info`;
DROP TABLE IF EXISTS `customer_info`;
DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `role_permission`;
DROP TABLE IF EXISTS `sys_dict`;
DROP TABLE IF EXISTS `sys_permission`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_role`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 模块一：系统管理 (6张表)
-- ============================================================

-- 1. 角色表
CREATE TABLE `sys_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) UNIQUE COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `user_name` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 权限表
CREATE TABLE `sys_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) UNIQUE COMMENT '权限编码',
    `resource_type` VARCHAR(20) COMMENT '资源类型',
    `resource_path` VARCHAR(200) COMMENT '资源路径',
    `parent_id` BIGINT COMMENT '父级ID',
    INDEX `idx_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 角色权限关联表
CREATE TABLE `role_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 数据字典
CREATE TABLE `sys_dict` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    INDEX `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 6. 操作日志
CREATE TABLE `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `module` VARCHAR(50) COMMENT '模块',
    `type` VARCHAR(20) COMMENT '操作类型',
    `description` VARCHAR(200) COMMENT '操作描述',
    `user_name` VARCHAR(50) COMMENT '操作人',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `method` VARCHAR(200) COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `result` TEXT COMMENT '返回结果',
    `status` INT DEFAULT 1 COMMENT '状态(0失败,1成功)',
    `error_msg` VARCHAR(500) COMMENT '错误信息',
    `cost_time` BIGINT COMMENT '耗时(ms)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_module` (`module`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 模块二：基础档案 (7张表)
-- ============================================================

-- 1. 客户档案表
CREATE TABLE `customer_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    `customer_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '客户编号',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `contact` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(200) COMMENT '地址',
    `email` VARCHAR(100) COMMENT '邮箱',
    `level` INT COMMENT '客户等级',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_customer_no` (`customer_no`),
    INDEX `idx_customer_name` (`customer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户档案表';

-- 2. 供应商表
CREATE TABLE `supplier_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商ID',
    `supplier_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '供应商编号',
    `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(200) COMMENT '地址',
    `email` VARCHAR(100) COMMENT '邮箱',
    `level` INT COMMENT '供应商等级',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_supplier_no` (`supplier_no`),
    INDEX `idx_supplier_name` (`supplier_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 3. 物料档案表
CREATE TABLE `material_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物料ID',
    `material_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '物料编码',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `type` VARCHAR(50) COMMENT '物料类型',
    `spec` VARCHAR(200) COMMENT '规格型号',
    `unit` VARCHAR(20) COMMENT '单位',
    `price` DECIMAL(10,2) COMMENT '单价',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `supplier_id` BIGINT COMMENT '默认供应商ID',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_material_no` (`material_no`),
    INDEX `idx_material_name` (`material_name`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料档案表';

-- 4. 产品档案表
CREATE TABLE `product_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    `product_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '产品编号',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `type` VARCHAR(50) COMMENT '产品类型',
    `spec` VARCHAR(200) COMMENT '规格型号',
    `unit` VARCHAR(20) COMMENT '单位',
    `price` DECIMAL(10,2) COMMENT '单价',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_product_no` (`product_no`),
    INDEX `idx_product_name` (`product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品档案表';

-- 5. 仓库表
CREATE TABLE `warehouse_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仓库ID',
    `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
    `warehouse_code` VARCHAR(50) UNIQUE COMMENT '仓库编码',
    `address` VARCHAR(200) COMMENT '地址',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    INDEX `idx_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 6. 设备档案表
CREATE TABLE `equipment_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    `equipment_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `equipment_type` VARCHAR(50) COMMENT '设备类型',
    `model` VARCHAR(100) COMMENT '设备型号',
    `manufacturer` VARCHAR(100) COMMENT '生产厂家',
    `workshop` VARCHAR(50) COMMENT '所在车间',
    `location` VARCHAR(100) COMMENT '放置位置',
    `manager` VARCHAR(50) COMMENT '设备负责人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `purchase_date` DATE COMMENT '采购日期',
    `commission_date` DATE COMMENT '投产日期',
    `running_hours` DECIMAL(10,2) DEFAULT 0 COMMENT '运行时长(小时)',
    `last_maintain_date` DATETIME COMMENT '上次维护日期',
    `next_maintain_date` DATETIME COMMENT '下次维护日期',
    `current_fault_id` BIGINT COMMENT '当前故障ID',
    `status` INT DEFAULT 1 COMMENT '状态(0停用,1运行,2维修中,3报废)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_equipment_no` (`equipment_no`),
    INDEX `idx_workshop` (`workshop`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备档案表';

-- 7. 工序表
CREATE TABLE `process_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工序ID',
    `process_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '工序编号',
    `process_name` VARCHAR(100) NOT NULL COMMENT '工序名称',
    `product_id` BIGINT COMMENT '产品ID',
    `workshop` VARCHAR(50) COMMENT '所属车间',
    `sort_order` INT DEFAULT 0 COMMENT '工序顺序',
    `standard_hours` DECIMAL(10,2) COMMENT '标准工时(小时)',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_process_no` (`process_no`),
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序表';

-- ============================================================
-- 模块三：订单采购 (2张表)
-- ============================================================

-- 1. 客户订单表
CREATE TABLE `order_customer` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    `customer_id` BIGINT COMMENT '客户ID',
    `customer_name` VARCHAR(100) COMMENT '客户名称',
    `product_id` BIGINT COMMENT '产品ID',
    `product_name` VARCHAR(100) COMMENT '产品名称',
    `quantity` INT NOT NULL COMMENT '订单数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `order_date` DATE COMMENT '订单日期',
    `delivery_date` DATE COMMENT '交付日期',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批,2生产中,3已完成,4已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户订单表';

-- 2. 采购订单表
CREATE TABLE `purchase_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '采购订单ID',
    `purchase_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '采购单号',
    `supplier_id` BIGINT COMMENT '供应商ID',
    `supplier_name` VARCHAR(100) COMMENT '供应商名称',
    `material_id` BIGINT COMMENT '物料ID',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `quantity` INT NOT NULL COMMENT '采购数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `purchase_date` DATE COMMENT '采购日期',
    `expected_date` DATE COMMENT '预计到货日期',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批,2已到货,3已完成,4已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_purchase_no` (`purchase_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- ============================================================
-- 模块四：仓储管理 (6张表)
-- ============================================================

-- 1. 库存表
CREATE TABLE `stock_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '库存数量',
    `location` VARCHAR(100) COMMENT '库位',
    UNIQUE KEY `uk_material_warehouse` (`material_id`, `warehouse_id`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 2. 入库记录表
CREATE TABLE `stock_in` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库ID',
    `stock_in_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '入库单号',
    `type` VARCHAR(20) COMMENT '入库类型',
    `material_id` BIGINT COMMENT '物料ID',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `quantity` INT NOT NULL COMMENT '入库数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `supplier_id` BIGINT COMMENT '供应商ID',
    `purchase_order_no` VARCHAR(50) COMMENT '采购单号',
    `work_order_no` VARCHAR(50) COMMENT '工单号',
    `operator` VARCHAR(50) COMMENT '操作人',
    `in_date` DATE COMMENT '入库日期',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批,2已入库)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_stock_in_no` (`stock_in_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- 3. 出库记录表
CREATE TABLE `stock_out` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库ID',
    `stock_out_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '出库单号',
    `type` VARCHAR(20) COMMENT '出库类型',
    `material_id` BIGINT COMMENT '物料ID',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `quantity` INT NOT NULL COMMENT '出库数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `work_order_no` VARCHAR(50) COMMENT '工单号',
    `operator` VARCHAR(50) COMMENT '操作人',
    `out_date` DATE COMMENT '出库日期',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批,2已出库)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_stock_out_no` (`stock_out_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- 4. 库存盘点表
CREATE TABLE `stock_check` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '盘点ID',
    `check_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '盘点单号',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `check_date` DATE COMMENT '盘点日期',
    `operator` VARCHAR(50) COMMENT '盘点人',
    `status` INT DEFAULT 0 COMMENT '状态(0盘点中,1已完成,2已审核)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_check_no` (`check_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点表';

-- 5. 盘点明细表
CREATE TABLE `stock_check_detail` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    `check_id` BIGINT NOT NULL COMMENT '盘点单ID',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编码',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `system_quantity` INT COMMENT '系统数量',
    `actual_quantity` INT COMMENT '实际数量',
    `diff_quantity` INT COMMENT '差异数量',
    `diff_reason` VARCHAR(500) COMMENT '差异原因',
    INDEX `idx_check_id` (`check_id`),
    FOREIGN KEY (`check_id`) REFERENCES `stock_check`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

-- 6. 库存预警表
CREATE TABLE `stock_alert` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预警ID',
    `alert_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '预警编号',
    `material_id` BIGINT COMMENT '物料ID',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `warehouse_name` VARCHAR(100) COMMENT '仓库名称',
    `alert_type` VARCHAR(20) COMMENT '预警类型(低于安全库存/高于最大库存)',
    `current_quantity` INT COMMENT '当前库存',
    `threshold` INT COMMENT '阈值',
    `status` INT DEFAULT 0 COMMENT '状态(0未处理,1已处理)',
    `handle_remark` VARCHAR(500) COMMENT '处理说明',
    `handle_time` DATETIME COMMENT '处理时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_alert_no` (`alert_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警表';

-- ============================================================
-- 模块五：生产管理 (3张表)
-- ============================================================

-- 1. 生产计划表
CREATE TABLE `production_plan` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    `plan_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '计划编号',
    `order_id` BIGINT COMMENT '订单ID',
    `order_no` VARCHAR(50) COMMENT '订单编号',
    `product_id` BIGINT COMMENT '产品ID',
    `product_name` VARCHAR(100) COMMENT '产品名称',
    `quantity` INT NOT NULL COMMENT '计划数量',
    `plan_start_date` DATE COMMENT '计划开始日期',
    `plan_end_date` DATE COMMENT '计划结束日期',
    `priority` INT DEFAULT 1 COMMENT '优先级',
    `status` INT DEFAULT 0 COMMENT '状态(0待执行,1执行中,2已完成,3已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_plan_no` (`plan_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划表';

-- 2. 生产工单表
CREATE TABLE `work_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    `work_order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '工单号',
    `plan_id` BIGINT COMMENT '计划ID',
    `product_id` BIGINT COMMENT '产品ID',
    `product_name` VARCHAR(100) COMMENT '产品名称',
    `process_id` BIGINT COMMENT '工序ID',
    `process_name` VARCHAR(100) COMMENT '工序名称',
    `quantity` INT NOT NULL COMMENT '工单数量',
    `planned_start_time` DATETIME COMMENT '计划开始时间',
    `planned_end_time` DATETIME COMMENT '计划结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `operator` VARCHAR(50) COMMENT '负责人',
    `status` INT DEFAULT 0 COMMENT '状态(0待派发,1执行中,2已完成,3已暂停,4已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_work_order_no` (`work_order_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产工单表';

-- 3. 生产报工表
CREATE TABLE `production_report` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报工ID',
    `report_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '报工编号',
    `work_order_id` BIGINT NOT NULL COMMENT '工单ID',
    `work_order_no` VARCHAR(50) COMMENT '工单号',
    `process_id` BIGINT COMMENT '工序ID',
    `process_name` VARCHAR(100) COMMENT '工序名称',
    `operator` VARCHAR(50) COMMENT '操作人员',
    `report_quantity` INT COMMENT '报工数量',
    `qualified_quantity` INT COMMENT '合格数量',
    `unqualified_quantity` INT COMMENT '不合格数量',
    `work_hours` DECIMAL(10,2) COMMENT '工时(小时)',
    `report_time` DATETIME COMMENT '报工时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_report_no` (`report_no`),
    INDEX `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产报工表';

-- ============================================================
-- 模块六：质量管理 (4张表)
-- ============================================================

-- 1. 质检任务表
CREATE TABLE `quality_task` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '质检任务ID',
    `task_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    `type` VARCHAR(20) COMMENT '质检类型',
    `source_no` VARCHAR(50) COMMENT '来源单号',
    `product_id` BIGINT COMMENT '产品ID',
    `product_name` VARCHAR(100) COMMENT '产品名称',
    `quantity` INT COMMENT '检验数量',
    `inspector` VARCHAR(50) COMMENT '检验人员',
    `status` INT DEFAULT 0 COMMENT '状态(0待检验,1检验中,2已完成,3已取消)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_task_no` (`task_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检任务表';

-- 2. 质检结果表
CREATE TABLE `quality_result` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    `task_id` BIGINT NOT NULL COMMENT '质检任务ID',
    `task_no` VARCHAR(50) COMMENT '任务编号',
    `item_name` VARCHAR(100) COMMENT '检验项目',
    `standard_value` VARCHAR(100) COMMENT '标准值',
    `actual_value` VARCHAR(100) COMMENT '实际值',
    `result` INT COMMENT '结果(0不合格,1合格)',
    `inspector` VARCHAR(50) COMMENT '检验人员',
    `inspect_time` DATETIME COMMENT '检验时间',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_task_id` (`task_id`),
    FOREIGN KEY (`task_id`) REFERENCES `quality_task`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检结果表';

-- 3. 不合格品表
CREATE TABLE `unqualified_product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '不合格品ID',
    `unqualified_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '不合格单号',
    `type` VARCHAR(20) COMMENT '不合格类型',
    `source_no` VARCHAR(50) COMMENT '来源单号',
    `product_id` BIGINT COMMENT '产品ID',
    `product_name` VARCHAR(100) COMMENT '产品名称',
    `quantity` INT COMMENT '不合格数量',
    `defect_type` VARCHAR(50) COMMENT '缺陷类型',
    `description` VARCHAR(500) COMMENT '不合格描述',
    `disposition` VARCHAR(20) COMMENT '处置方式',
    `handler` VARCHAR(50) COMMENT '处理人',
    `status` INT DEFAULT 0 COMMENT '状态(0待处理,1处理中,2已处理,3已关闭)',
    `handle_result` VARCHAR(500) COMMENT '处理结果',
    `handle_time` DATETIME COMMENT '处理时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_unqualified_no` (`unqualified_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品表';

-- 4. 异常记录表
CREATE TABLE `exception_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '异常ID',
    `exception_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '异常编号',
    `type` VARCHAR(20) COMMENT '异常类型',
    `title` VARCHAR(200) COMMENT '异常标题',
    `description` VARCHAR(500) COMMENT '异常描述',
    `workshop` VARCHAR(50) COMMENT '发生车间',
    `reporter` VARCHAR(50) COMMENT '上报人',
    `report_time` DATETIME COMMENT '上报时间',
    `handler` VARCHAR(50) COMMENT '处理人',
    `status` INT DEFAULT 0 COMMENT '状态(0待处理,1处理中,2已处理,3已关闭)',
    `handle_result` VARCHAR(500) COMMENT '处理结果',
    `handle_time` DATETIME COMMENT '处理时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_exception_no` (`exception_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常记录表';

-- ============================================================
-- 模块七：设备管理 (3张表)
-- ============================================================

-- 1. 设备维保记录表
CREATE TABLE `equipment_maintain` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维保ID',
    `maintain_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '维保编号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `type` VARCHAR(20) COMMENT '维保类型(保养/维修)',
    `content` VARCHAR(500) COMMENT '维保内容',
    `maintainer` VARCHAR(50) COMMENT '维保人员',
    `maintain_date` DATE COMMENT '维保日期',
    `cost` DECIMAL(10,2) COMMENT '维保费用',
    `status` INT DEFAULT 1 COMMENT '状态(0已取消,1已完成)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_maintain_no` (`maintain_no`),
    INDEX `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维保记录表';

-- 2. 设备故障表
CREATE TABLE `equipment_fault` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '故障ID',
    `fault_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '故障编号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `fault_type` VARCHAR(50) COMMENT '故障类型',
    `fault_level` VARCHAR(20) COMMENT '故障等级(一般/严重/紧急)',
    `fault_desc` VARCHAR(500) COMMENT '故障描述',
    `fault_description` VARCHAR(500) COMMENT '故障描述(冗余字段)',
    `reporter_id` BIGINT COMMENT '上报人ID',
    `reporter` VARCHAR(50) COMMENT '上报人',
    `report_time` DATETIME COMMENT '上报时间',
    `repair_task_id` BIGINT COMMENT '关联维修任务ID',
    `assignee` VARCHAR(50) COMMENT '指派人员',
    `assign_time` DATETIME COMMENT '指派时间',
    `solution` VARCHAR(500) COMMENT '解决方案',
    `status` INT DEFAULT 0 COMMENT '状态(0待派工,1已派工,2维修中,3已修复,4已关闭)',
    `cost` DECIMAL(10,2) COMMENT '维修费用',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_fault_no` (`fault_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障表';

-- 3. 维修任务表
CREATE TABLE `equipment_repair_task` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    `task_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    `fault_id` BIGINT COMMENT '关联故障ID',
    `equipment_id` BIGINT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `priority` INT COMMENT '优先级(0低,1中,2高)',
    `fault_desc` VARCHAR(500) COMMENT '故障描述',
    `fault_description` VARCHAR(500) COMMENT '故障描述',
    `repairer_id` BIGINT COMMENT '维修人员ID',
    `assignee` VARCHAR(50) COMMENT '维修人员',
    `assign_time` DATETIME COMMENT '派工时间',
    `accept_time` DATETIME COMMENT '接单时间',
    `start_time` DATETIME COMMENT '开始维修时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `repair_content` VARCHAR(1000) COMMENT '维修内容',
    `solution` VARCHAR(1000) COMMENT '解决方案',
    `parts_used` VARCHAR(500) COMMENT '使用配件',
    `repair_cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用(冗余)',
    `acceptance_result` VARCHAR(20) COMMENT '验收结果(pass/fail)',
    `acceptance_time` DATETIME COMMENT '验收时间',
    `acceptance_by` VARCHAR(50) COMMENT '验收人',
    `status` INT DEFAULT 0 COMMENT '状态(0待派工,1已派工,2维修中,3已完成,4已验收)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_task_no` (`task_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_fault_id` (`fault_id`),
    INDEX `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修任务表';

-- ============================================================
-- 模块八：工艺配置 (3张表)
-- ============================================================

-- 1. 作业指导书表
CREATE TABLE `process_sop` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'SOP ID',
    `sop_no` VARCHAR(50) NOT NULL UNIQUE COMMENT 'SOP编号',
    `sop_name` VARCHAR(100) NOT NULL COMMENT 'SOP名称',
    `process_id` BIGINT COMMENT '工序ID',
    `process_name` VARCHAR(100) COMMENT '工序名称',
    `version` VARCHAR(20) COMMENT '版本号',
    `content` TEXT COMMENT 'SOP内容',
    `attachment` VARCHAR(200) COMMENT '附件',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_sop_no` (`sop_no`),
    INDEX `idx_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业指导书表';

-- 2. 工艺参数标准表
CREATE TABLE `process_param_standard` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    `param_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '参数编号',
    `param_name` VARCHAR(100) NOT NULL COMMENT '参数名称',
    `process_id` BIGINT COMMENT '工序ID',
    `process_name` VARCHAR(100) COMMENT '工序名称',
    `standard_value` VARCHAR(100) COMMENT '标准值',
    `upper_limit` VARCHAR(100) COMMENT '上限',
    `lower_limit` VARCHAR(100) COMMENT '下限',
    `unit` VARCHAR(20) COMMENT '单位',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_param_no` (`param_no`),
    INDEX `idx_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺参数标准表';

-- 3. 工序物料BOM表
CREATE TABLE `process_material_bom` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'BOM ID',
    `bom_no` VARCHAR(50) NOT NULL UNIQUE COMMENT 'BOM编号',
    `process_id` BIGINT COMMENT '工序ID',
    `process_name` VARCHAR(100) COMMENT '工序名称',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编码',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `quantity` DECIMAL(10,2) COMMENT '用量',
    `unit` VARCHAR(20) COMMENT '单位',
    `loss_rate` DECIMAL(5,2) COMMENT '损耗率(%)',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_bom_no` (`bom_no`),
    INDEX `idx_process_id` (`process_id`),
    INDEX `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序物料BOM表';

-- ============================================================
-- 模块九：AI助手 (4张表)
-- ============================================================

-- 1. AI文档表
CREATE TABLE `ai_document` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    `doc_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '文档编号',
    `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
    `category` VARCHAR(50) COMMENT '文档分类',
    `content` TEXT COMMENT '文档内容',
    `file_path` VARCHAR(200) COMMENT '文件路径',
    `file_type` VARCHAR(20) COMMENT '文件类型',
    `file_size` BIGINT COMMENT '文件大小',
    `author` VARCHAR(50) COMMENT '作者',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_doc_no` (`doc_no`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI文档表';

-- 2. AI文档分块表
CREATE TABLE `ai_document_chunk` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分块ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `chunk_index` INT COMMENT '分块序号',
    `content` TEXT COMMENT '分块内容',
    `embedding` TEXT COMMENT '向量嵌入',
    `token_count` INT COMMENT 'token数量',
    INDEX `idx_document_id` (`document_id`),
    FOREIGN KEY (`document_id`) REFERENCES `ai_document`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI文档分块表';

-- 3. 知识库文档表
CREATE TABLE `ai_knowledge_document` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `doc_name` VARCHAR(200) COMMENT '文档名称',
    `doc_type` VARCHAR(50) COMMENT '文档类型',
    `category` VARCHAR(50) COMMENT '分类',
    `content` TEXT COMMENT '内容',
    `file_path` VARCHAR(200) COMMENT '文件路径',
    `file_name` VARCHAR(200) COMMENT '文件名',
    `uploader` VARCHAR(50) COMMENT '上传人',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 4. AI问答历史表
CREATE TABLE `ai_chat_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `user_id` BIGINT COMMENT '用户ID',
    `user_name` VARCHAR(50) COMMENT '用户名',
    `question` TEXT COMMENT '问题',
    `answer` TEXT COMMENT '回答',
    `model` VARCHAR(50) COMMENT '模型',
    `response_time` INT COMMENT '响应时间(ms)',
    `feedback` INT COMMENT '反馈(0差评,1好评)',
    `chat_status` INT COMMENT '对话状态(0正常,1异常)',
    `context` TEXT COMMENT '上下文(JSON)',
    `scene` VARCHAR(50) COMMENT '场景',
    `chat_time` DATETIME COMMENT '对话时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI问答历史表';

-- ============================================================
-- 模块十：产品追溯 (4张表)
-- ============================================================

-- 1. 产品SN表
CREATE TABLE `product_sn` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'SN ID',
    `sn_code` VARCHAR(100) NOT NULL UNIQUE COMMENT 'SN编码',
    `product_id` BIGINT COMMENT '产品ID',
    `product_name` VARCHAR(100) COMMENT '产品名称',
    `work_order_id` BIGINT COMMENT '工单ID',
    `work_order_no` VARCHAR(50) COMMENT '工单号',
    `production_date` DATE COMMENT '生产日期',
    `status` INT DEFAULT 1 COMMENT '状态(0报废,1在库,2已出库,3已售后)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_sn_code` (`sn_code`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品SN表';

-- 2. SN工序追溯表
CREATE TABLE `sn_process_trace` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `sn_id` BIGINT NOT NULL COMMENT 'SN ID',
    `sn_code` VARCHAR(100) COMMENT 'SN编码',
    `process_id` BIGINT COMMENT '工序ID',
    `process_name` VARCHAR(100) COMMENT '工序名称',
    `operator` VARCHAR(50) COMMENT '操作人员',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `result` INT COMMENT '结果(0不合格,1合格)',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_sn_id` (`sn_id`),
    FOREIGN KEY (`sn_id`) REFERENCES `product_sn`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN工序追溯表';

-- 3. SN物料追溯表
CREATE TABLE `sn_material_trace` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `sn_id` BIGINT NOT NULL COMMENT 'SN ID',
    `sn_code` VARCHAR(100) COMMENT 'SN编码',
    `material_id` BIGINT COMMENT '物料ID',
    `material_no` VARCHAR(50) COMMENT '物料编码',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `quantity` DECIMAL(10,2) COMMENT '用量',
    `supplier_name` VARCHAR(100) COMMENT '供应商',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_sn_id` (`sn_id`),
    FOREIGN KEY (`sn_id`) REFERENCES `product_sn`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN物料追溯表';

-- 4. SN质量追溯表
CREATE TABLE `sn_quality_trace` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `sn_id` BIGINT NOT NULL COMMENT 'SN ID',
    `sn_code` VARCHAR(100) COMMENT 'SN编码',
    `inspect_type` VARCHAR(50) COMMENT '检验类型',
    `inspect_item` VARCHAR(100) COMMENT '检验项目',
    `standard_value` VARCHAR(100) COMMENT '标准值',
    `actual_value` VARCHAR(100) COMMENT '实际值',
    `result` INT COMMENT '结果(0不合格,1合格)',
    `inspector` VARCHAR(50) COMMENT '检验人员',
    `inspect_time` DATETIME COMMENT '检验时间',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_sn_id` (`sn_id`),
    FOREIGN KEY (`sn_id`) REFERENCES `product_sn`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN质量追溯表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 数据库初始化完成
-- ============================================================
