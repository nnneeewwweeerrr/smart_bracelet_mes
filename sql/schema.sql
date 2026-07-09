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
    `order_id` BIGINT COMMENT '订单ID',
    `product_id` BIGINT COMMENT '产品ID',
    `planned_quantity` INT NOT NULL COMMENT '计划产量',
    `start_date` DATETIME COMMENT '计划开始日期',
    `end_date` DATETIME COMMENT '计划结束日期',
    `status` INT DEFAULT 0 COMMENT '计划状态(0待执行,1执行中,2已完成)',
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
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_equipment_code` (`equipment_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

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