-- ============================================================
-- 智能手环MES系统 - 最终版数据库建表脚本
-- 版本：v2.0
-- 说明：统一整理，补充外键索引和流程字段
-- 使用方法：在Navicat中选中数据库，直接运行此文件
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ========== 清理旧表 ==========
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
    `email` VARCHAR(100) COMMENT '邮箱',
    `role_id` BIGINT COMMENT '角色ID',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_name` (`user_name`),
    INDEX `idx_role_id` (`role_id`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
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
    `role_id` BIGINT COMMENT '角色ID',
    `permission_id` BIGINT COMMENT '权限ID',
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 数据字典表
CREATE TABLE `sys_dict` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
    `dict_value` VARCHAR(200) NOT NULL COMMENT '字典值',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` INT DEFAULT 1 COMMENT '状态',
    INDEX `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 6. 操作日志表
CREATE TABLE `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '用户ID',
    `user_name` VARCHAR(50) COMMENT '用户名',
    `operation_type` VARCHAR(50) COMMENT '操作类型',
    `operation_module` VARCHAR(50) COMMENT '操作模块',
    `operation_desc` VARCHAR(200) COMMENT '操作描述',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 模块二：基础档案 (7张表)
-- ============================================================

-- 7. 客户档案表
CREATE TABLE `customer_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    `customer_code` VARCHAR(50) UNIQUE COMMENT '客户编码',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `contact_name` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `address` VARCHAR(200) COMMENT '地址',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_customer_code` (`customer_code`),
    INDEX `idx_customer_name` (`customer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 8. 供应商表
CREATE TABLE `supplier_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商ID',
    `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact_name` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(200) COMMENT '地址',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_supplier_name` (`supplier_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 9. 物料档案表
CREATE TABLE `material_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物料ID',
    `material_code` VARCHAR(50) UNIQUE COMMENT '物料编码',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `material_type` VARCHAR(50) COMMENT '物料类型',
    `unit` VARCHAR(20) COMMENT '计量单位',
    `unit_price` DECIMAL(10,2) COMMENT '参考单价',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    INDEX `idx_material_code` (`material_code`),
    INDEX `idx_material_name` (`material_name`),
    INDEX `idx_material_type` (`material_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料表';

-- 10. 产品档案表
CREATE TABLE `product_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    `product_code` VARCHAR(50) UNIQUE COMMENT '产品编码',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `product_type` VARCHAR(50) COMMENT '产品类型',
    `specification` VARCHAR(200) COMMENT '规格型号',
    `unit_price` DECIMAL(10,2) COMMENT '参考单价',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_product_code` (`product_code`),
    INDEX `idx_product_name` (`product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品信息表';

-- 11. 仓库表
CREATE TABLE `warehouse_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仓库ID',
    `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
    `warehouse_code` VARCHAR(50) UNIQUE COMMENT '仓库编码',
    `address` VARCHAR(200) COMMENT '仓库地址',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    INDEX `idx_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 12. 设备档案表 (新增 current_fault_id 字段)
CREATE TABLE `equipment_info` (
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
    `current_fault_id` BIGINT COMMENT '当前故障ID',
    `status` INT DEFAULT 1 COMMENT '状态(0停用,1运行,2维修中,3报废)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_equipment_no` (`equipment_no`),
    INDEX `idx_equipment_name` (`equipment_name`),
    INDEX `idx_workshop` (`workshop`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备档案表';

-- 13. 工序表
CREATE TABLE `process_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工序ID',
    `process_name` VARCHAR(100) NOT NULL COMMENT '工序名称',
    `process_code` VARCHAR(50) UNIQUE COMMENT '工序编码',
    `product_id` BIGINT COMMENT '产品ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    INDEX `idx_process_code` (`process_code`),
    FOREIGN KEY (`product_id`) REFERENCES `product_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序表';

-- ============================================================
-- 模块三：订单采购 (2张表)
-- ============================================================

-- 14. 客户订单表
CREATE TABLE `order_customer` (
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
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`customer_id`) REFERENCES `customer_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户订单表';

-- 15. 采购订单表
CREATE TABLE `purchase_order` (
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
    INDEX `idx_supplier_id` (`supplier_id`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`supplier_id`) REFERENCES `supplier_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`material_id`) REFERENCES `material_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- ============================================================
-- 模块四：仓储管理 (6张表)
-- ============================================================

-- 16. 库存表
CREATE TABLE `stock_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    `material_id` BIGINT COMMENT '物料ID',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `quantity` INT DEFAULT 0 COMMENT '库存数量',
    `location` VARCHAR(50) COMMENT '库位',
    INDEX `idx_material_warehouse` (`material_id`, `warehouse_id`),
    FOREIGN KEY (`material_id`) REFERENCES `material_info`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse_info`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 17. 入库记录表
CREATE TABLE `stock_in` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库单ID',
    `in_no` VARCHAR(50) UNIQUE COMMENT '入库单号',
    `material_id` BIGINT COMMENT '物料ID',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `quantity` INT NOT NULL COMMENT '入库数量',
    `source_type` VARCHAR(20) COMMENT '来源类型(purchase采购/return退货/other其他)',
    `source_no` VARCHAR(50) COMMENT '来源单号',
    `operator_id` BIGINT COMMENT '操作人ID',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_in_no` (`in_no`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_warehouse_id` (`warehouse_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`material_id`) REFERENCES `material_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- 18. 出库记录表 (新增 work_order_no 字段)
CREATE TABLE `stock_out` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库单ID',
    `out_no` VARCHAR(50) UNIQUE COMMENT '出库单号',
    `material_id` BIGINT COMMENT '物料ID',
    `warehouse_id` BIGINT COMMENT '仓库ID',
    `quantity` INT NOT NULL COMMENT '出库数量',
    `target_type` VARCHAR(20) COMMENT '出库类型(production生产/sale销售/other其他)',
    `target_no` VARCHAR(50) COMMENT '目标单号',
    `work_order_no` VARCHAR(50) COMMENT '关联工单号',
    `operator_id` BIGINT COMMENT '操作人ID',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_out_no` (`out_no`),
    INDEX `idx_material_id` (`material_id`),
    INDEX `idx_warehouse_id` (`warehouse_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`material_id`) REFERENCES `material_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- 19. 库存盘点表
CREATE TABLE `stock_check` (
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
    INDEX `idx_check_date` (`check_date`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点表';

-- 20. 盘点明细表
CREATE TABLE `stock_check_detail` (
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
    INDEX `idx_material_id` (`material_id`),
    FOREIGN KEY (`check_id`) REFERENCES `stock_check`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点明细表';

-- 21. 库存预警表
CREATE TABLE `stock_alert` (
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
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`material_id`) REFERENCES `material_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警表';

-- ============================================================
-- 模块五：生产管理 (3张表)
-- ============================================================

-- 22. 生产计划表
CREATE TABLE `production_plan` (
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
    INDEX `idx_plan_no` (`plan_no`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`order_id`) REFERENCES `order_customer`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划表';

-- 23. 生产工单表
CREATE TABLE `work_order` (
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
    INDEX `idx_work_no` (`work_no`),
    INDEX `idx_plan_id` (`plan_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_process_id` (`process_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`plan_id`) REFERENCES `production_plan`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`process_id`) REFERENCES `process_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单号表';

-- 24. 生产报工表
CREATE TABLE `production_report` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报工ID',
    `work_order_id` BIGINT COMMENT '工单ID',
    `worker_id` BIGINT COMMENT '工人ID',
    `process_id` BIGINT COMMENT '工序ID',
    `report_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报工日期',
    `good_quantity` INT DEFAULT 0 COMMENT '合格数量',
    `bad_quantity` INT DEFAULT 0 COMMENT '不合格数量',
    INDEX `idx_work_order_id` (`work_order_id`),
    INDEX `idx_report_date` (`report_date`),
    FOREIGN KEY (`work_order_id`) REFERENCES `work_order`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`process_id`) REFERENCES `process_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报工记录表';

-- ============================================================
-- 模块六：质量管理 (4张表)
-- ============================================================

-- 25. 质检任务表
CREATE TABLE `quality_task` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '质检任务ID',
    `task_no` VARCHAR(50) UNIQUE COMMENT '任务编号',
    `task_type` VARCHAR(20) COMMENT '任务类型(IQC来料/IPQC制程/OQC出货)',
    `source_id` BIGINT COMMENT '来源ID',
    `inspector_id` BIGINT COMMENT '检验员ID',
    `status` INT DEFAULT 0 COMMENT '任务状态(0待处理,1处理中,2已完成)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_task_no` (`task_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检任务表';

-- 26. 质检结果表
CREATE TABLE `quality_result` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '质检结果ID',
    `task_id` BIGINT COMMENT '质检任务ID',
    `check_item` VARCHAR(200) COMMENT '检验项目',
    `standard_value` VARCHAR(200) COMMENT '标准值',
    `actual_value` VARCHAR(200) COMMENT '实际值',
    `result` INT COMMENT '检验结果(0不合格,1合格)',
    `remark` TEXT COMMENT '备注',
    INDEX `idx_task_id` (`task_id`),
    FOREIGN KEY (`task_id`) REFERENCES `quality_task`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检结果表';

-- 27. 不合格品表
CREATE TABLE `unqualified_product` (
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

-- 28. 异常记录表
CREATE TABLE `exception_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '异常ID',
    `exception_no` VARCHAR(50) UNIQUE COMMENT '异常编号',
    `exception_type` VARCHAR(50) COMMENT '异常类型(quality质量/equipment设备/material物料/safety安全/other其他)',
    `work_order_id` BIGINT COMMENT '工单ID',
    `description` TEXT COMMENT '异常描述',
    `reporter_id` BIGINT COMMENT '上报人ID',
    `handler` VARCHAR(50) COMMENT '处理人',
    `handle_result` VARCHAR(500) COMMENT '处理结果',
    `status` INT DEFAULT 0 COMMENT '处理状态(0待处理,1处理中,2已解决)',
    `report_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    `handle_time` DATETIME COMMENT '处理时间',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_exception_no` (`exception_no`),
    INDEX `idx_work_order_id` (`work_order_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常记录表';

-- ============================================================
-- 模块七：设备管理 (3张表)
-- ============================================================

-- 29. 设备维保记录表
CREATE TABLE `equipment_maintain` (
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
    INDEX `idx_actual_date` (`actual_date`),
    FOREIGN KEY (`equipment_id`) REFERENCES `equipment_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维保记录表';

-- 30. 设备故障表 (新增 repair_task_id 字段)
CREATE TABLE `equipment_fault` (
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
    `assign_type` VARCHAR(20) COMMENT '派工类型(manual手动/auto自动)',
    `repair_task_id` BIGINT COMMENT '关联维修任务ID',
    `solution` VARCHAR(1000) COMMENT '处理方案',
    `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    `status` INT DEFAULT 0 COMMENT '状态(0待派工/1已派工/2维修中/3已修复/4已关闭)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_fault_no` (`fault_no`),
    INDEX `idx_equipment_id` (`equipment_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`equipment_id`) REFERENCES `equipment_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障管理表';

-- 31. 维修任务表 (新增 fault_id 字段)
CREATE TABLE `equipment_repair_task` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    `task_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '任务编号',
    `equipment_id` BIGINT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) COMMENT '设备编号',
    `equipment_name` VARCHAR(100) COMMENT '设备名称',
    `fault_id` BIGINT COMMENT '关联故障ID',
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
    `status` INT DEFAULT 0 COMMENT '状态(0待派工/1已派工/2维修中/3已完成/4已验收)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_task_no` (`task_no`),
    INDEX `idx_equipment_id` (`equipment_id`),
    INDEX `idx_fault_id` (`fault_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`equipment_id`) REFERENCES `equipment_info`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修任务表';

-- ============================================================
-- 模块八：工艺配置 (3张表)
-- ============================================================

-- 32. 作业指导书表
CREATE TABLE `process_sop` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'SOP ID',
    `process_id` BIGINT COMMENT '工序ID',
    `sop_name` VARCHAR(100) COMMENT 'SOP名称',
    `sop_content` TEXT COMMENT 'SOP内容',
    `version` VARCHAR(20) COMMENT '版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_process_id` (`process_id`),
    FOREIGN KEY (`process_id`) REFERENCES `process_info`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业指导书表';

-- 33. 工艺参数标准表
CREATE TABLE `process_param_standard` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    `process_id` BIGINT COMMENT '工序ID',
    `param_name` VARCHAR(50) COMMENT '参数名称',
    `standard_value` VARCHAR(100) COMMENT '标准值',
    `upper_limit` VARCHAR(50) COMMENT '上限',
    `lower_limit` VARCHAR(50) COMMENT '下限',
    `unit` VARCHAR(20) COMMENT '单位',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_process_id` (`process_id`),
    FOREIGN KEY (`process_id`) REFERENCES `process_info`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺参数标准表';

-- 34. 工序物料BOM表
CREATE TABLE `process_material_bom` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'BOM ID',
    `process_id` BIGINT COMMENT '工序ID',
    `material_id` BIGINT COMMENT '物料ID',
    `quantity` DECIMAL(10,2) COMMENT '用量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_process_id` (`process_id`),
    INDEX `idx_material_id` (`material_id`),
    FOREIGN KEY (`process_id`) REFERENCES `process_info`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`material_id`) REFERENCES `material_info`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序物料BOM表';

-- ============================================================
-- 模块九：AI助手 (2张表)
-- ============================================================

-- 35. 知识库文档表
CREATE TABLE `ai_knowledge_document` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '文档名称',
    `doc_type` VARCHAR(20) COMMENT '文档类型(PDF/WORD/TEXT/URL)',
    `content` TEXT COMMENT '文档内容',
    `summary` VARCHAR(500) COMMENT '内容摘要',
    `uploader` VARCHAR(50) COMMENT '上传人',
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `sync_status` INT DEFAULT 0 COMMENT '同步状态(0未同步,1同步中,2已同步)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_doc_name` (`doc_name`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 36. AI问答历史表
CREATE TABLE `ai_chat_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` VARCHAR(100) COMMENT '用户ID',
    `user_name` VARCHAR(50) COMMENT '用户名',
    `question` TEXT NOT NULL COMMENT '用户问题',
    `answer` TEXT COMMENT 'AI回答',
    `context` TEXT COMMENT '上下文信息JSON',
    `chat_status` INT DEFAULT 0 COMMENT '状态(0成功,1失败)',
    `feedback` INT COMMENT '反馈(0差评,1好评)',
    `response_time` BIGINT COMMENT '响应时间(毫秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI问答历史表';

-- ============================================================
-- 模块十：产品追溯 (4张表)
-- ============================================================

-- 37. 产品SN表
CREATE TABLE `product_sn` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `sn_code` VARCHAR(50) UNIQUE NOT NULL COMMENT 'SN编码',
    `product_id` BIGINT COMMENT '产品ID',
    `work_order_id` BIGINT COMMENT '工单ID',
    `production_date` DATE COMMENT '生产日期',
    `status` INT DEFAULT 1 COMMENT '状态(0不合格,1合格)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_sn_code` (`sn_code`),
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品SN表';

-- 38. SN工序追溯表
CREATE TABLE `sn_process_trace` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `sn_id` BIGINT COMMENT 'SN ID',
    `sn_code` VARCHAR(50) COMMENT 'SN编码',
    `process_id` BIGINT COMMENT '工序ID',
    `process_name` VARCHAR(100) COMMENT '工序名称',
    `worker_id` BIGINT COMMENT '工人ID',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `result` INT COMMENT '结果(0不合格,1合格)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_sn_id` (`sn_id`),
    INDEX `idx_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN工序追溯表';

-- 39. SN物料追溯表
CREATE TABLE `sn_material_trace` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `sn_id` BIGINT COMMENT 'SN ID',
    `sn_code` VARCHAR(50) COMMENT 'SN编码',
    `material_id` BIGINT COMMENT '物料ID',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `quantity` DECIMAL(10,2) COMMENT '用量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_sn_id` (`sn_id`),
    INDEX `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN物料追溯表';

-- 40. SN质量追溯表
CREATE TABLE `sn_quality_trace` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `sn_id` BIGINT COMMENT 'SN ID',
    `sn_code` VARCHAR(50) COMMENT 'SN编码',
    `check_item` VARCHAR(200) COMMENT '检验项目',
    `standard_value` VARCHAR(200) COMMENT '标准值',
    `actual_value` VARCHAR(200) COMMENT '实际值',
    `result` INT COMMENT '结果(0不合格,1合格)',
    `inspector` VARCHAR(50) COMMENT '检验员',
    `check_time` DATETIME COMMENT '检验时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_sn_id` (`sn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN质量追溯表';

-- ============================================================
-- 建表完成！
-- ============================================================
