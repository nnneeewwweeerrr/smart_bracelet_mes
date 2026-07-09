-- ============================================================
-- 智能手环MES系统 - 初始化数据
-- 默认密码（BCrypt加密后）: 123456
-- ============================================================

-- 1. 角色数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'admin', '系统超级管理员，拥有所有权限'),
(2, '订单专员', 'order_clerk', '负责订单管理相关工作'),
(3, '采购专员', 'purchase_clerk', '负责采购管理相关工作'),
(4, '仓库管理员', 'warehouse_keeper', '负责仓储管理相关工作'),
(5, '生产主管', 'production_manager', '负责生产管理相关工作'),
(6, '质检人员', 'quality_inspector', '负责质量管理相关工作'),
(7, '一线工人', 'worker', '负责生产报工、异常上报');

-- 2. 用户数据（默认密码: 123456，BCrypt加密）
-- 启动后 DataInitializer 会自动检测并将密码更新为正确的BCrypt hash
-- 也可调用 POST /api/auth/init-data 一键初始化
INSERT INTO `sys_user` (`id`, `user_name`, `password`, `real_name`, `phone`, `email`, `role_id`, `status`) VALUES
(1, 'admin', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '系统管理员', '13800138000', 'admin@mes.com', 1, 1),
(2, 'order_clerk', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '王订单', '13800138001', 'order@mes.com', 2, 1),
(3, 'purchase_clerk', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '李采购', '13800138002', 'purchase@mes.com', 3, 1),
(4, 'warehouse_keeper', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '张仓管', '13800138003', 'warehouse@mes.com', 4, 1),
(5, 'production_manager', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '赵生产', '13800138004', 'production@mes.com', 5, 1),
(6, 'quality_inspector', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '陈质检', '13800138005', 'quality@mes.com', 6, 1),
(7, 'worker001', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '张三', '13800138006', 'worker001@mes.com', 7, 1),
(8, 'worker002', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '李四', '13800138007', 'worker002@mes.com', 7, 1),
(9, 'worker003', '$2a$10$jQSvjCO.lRYeNPKg9wmYJ.vxn12P2A5PKbQ3PIw2jFvz7o8kXX5Tq', '王五', '13800138008', 'worker003@mes.com', 7, 1);

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
(28, '异常处理', 'exception:handle', 'button', '/exception/handle', 26);

-- 4. 角色权限关联
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),(1, 7),(1, 8),(1, 9),(1, 10),
(1, 11),(1, 12),(1, 13),(1, 14),(1, 15),(1, 16),(1, 17),(1, 18),(1, 19),
(1, 20),(1, 21),(1, 22),(1, 23),(1, 24),(1, 25),(1, 26),(1, 27),(1, 28),
(2, 1),(2, 2),(2, 3),(2, 4),
(3, 5),(3, 6),(3, 7),
(4, 8),(4, 9),(4, 10),(4, 11),
(5, 12),(5, 13),(5, 14),(5, 15),(5, 19),(5, 20),(5, 21),
(6, 16),(6, 17),(6, 18),
(7, 12),(7, 14),(7, 15),(7, 26),(7, 27);

-- 5. 数据字典 - 订单状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('order_status', '0', '待审批', 1, 1),
('order_status', '1', '已审批', 2, 1),
('order_status', '2', '生产中', 3, 1),
('order_status', '3', '已完成', 4, 1),
('order_status', '4', '已取消', 5, 1);

-- 数据字典 - 采购状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('purchase_status', '0', '待审批', 1, 1),
('purchase_status', '1', '已审批', 2, 1),
('purchase_status', '2', '已到货', 3, 1),
('purchase_status', '3', '已完成', 4, 1);

-- 数据字典 - 生产计划状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('plan_status', '0', '待执行', 1, 1),
('plan_status', '1', '执行中', 2, 1),
('plan_status', '2', '已完成', 3, 1);

-- 数据字典 - 工单状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('workorder_status', '0', '待派发', 1, 1),
('workorder_status', '1', '执行中', 2, 1),
('workorder_status', '2', '已完成', 3, 1);

-- 数据字典 - 质检状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('quality_status', '0', '待处理', 1, 1),
('quality_status', '1', '处理中', 2, 1),
('quality_status', '2', '已完成', 3, 1);

-- 数据字典 - 质检结果
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('quality_result', '0', '不合格', 1, 1),
('quality_result', '1', '合格', 2, 1);

-- 数据字典 - 设备状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('equipment_status', '0', '停用', 1, 1),
('equipment_status', '1', '运行', 2, 1),
('equipment_status', '2', '维修', 3, 1);

-- 数据字典 - 异常状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('exception_status', '0', '待处理', 1, 1),
('exception_status', '1', '处理中', 2, 1),
('exception_status', '2', '已解决', 3, 1);

-- 数据字典 - 异常类型
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('exception_type', 'quality', '质量问题', 1, 1),
('exception_type', 'equipment', '设备故障', 2, 1),
('exception_type', 'material', '物料不足', 3, 1),
('exception_type', 'safety', '安全问题', 4, 1),
('exception_type', 'other', '其他', 5, 1);

-- 数据字典 - 单位
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('unit', 'pcs', '个', 1, 1),
('unit', 'box', '箱', 2, 1),
('unit', 'kg', '千克', 3, 1),
('unit', 'm', '米', 4, 1),
('unit', 'set', '套', 5, 1);

-- 数据字典 - 通用状态
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_value`, `sort_order`, `status`) VALUES
('common_status', '0', '禁用', 1, 1),
('common_status', '1', '启用', 2, 1);

-- 6. 测试客户数据
INSERT INTO `customer_info` (`id`, `customer_code`, `customer_name`, `contact_name`, `phone`, `email`, `address`, `status`) VALUES
(1, 'C001', '华为技术有限公司', '张总', '13800000001', 'huawei@example.com', '深圳市龙岗区', 1),
(2, 'C002', '小米通讯技术有限公司', '雷总', '13800000002', 'xiaomi@example.com', '北京市海淀区', 1),
(3, 'C003', 'OPPO广东移动通信有限公司', '陈总', '13800000003', 'oppo@example.com', '东莞市长安镇', 1);

-- 7. 测试产品数据
INSERT INTO `product_info` (`id`, `product_code`, `product_name`, `product_type`, `specification`, `unit_price`, `status`) VALUES
(1, 'P001', '智能手环X1', '手环', '黑色/标准版', 299.00, 1),
(2, 'P002', '智能手环X1 Pro', '手环', '黑色/尊享版', 499.00, 1),
(3, 'P003', '智能手环S2', '手环', '蓝色/运动版', 399.00, 1);

-- 8. 测试供应商数据
INSERT INTO `supplier_info` (`id`, `supplier_name`, `contact_name`, `phone`, `address`, `status`) VALUES
(1, '深圳鑫达电子有限公司', '王经理', '13900000001', '深圳市宝安区', 1),
(2, '东莞精密塑胶制品厂', '李厂长', '13900000002', '东莞市长安镇', 1),
(3, '上海芯片技术有限公司', '赵总', '13900000003', '上海市浦东新区', 1);

-- 9. 测试物料数据
INSERT INTO `material_info` (`id`, `material_code`, `material_name`, `material_type`, `unit`, `unit_price`, `safety_stock`, `status`) VALUES
(1, 'M001', '手环主板', '电子件', 'pcs', 50.00, 1000, 1),
(2, 'M002', 'OLED屏幕', '电子件', 'pcs', 30.00, 1000, 1),
(3, 'M003', '硅胶表带', '配件', 'pcs', 5.00, 5000, 1),
(4, 'M004', '锂电池', '电子件', 'pcs', 15.00, 2000, 1),
(5, 'M005', '传感器模组', '电子件', 'pcs', 20.00, 1500, 1);

-- 10. 测试仓库数据
INSERT INTO `warehouse_info` (`id`, `warehouse_name`, `warehouse_code`, `address`, `status`) VALUES
(1, '主原料仓', 'WH001', '厂区A栋1楼', 1),
(2, '成品仓', 'WH002', '厂区B栋1楼', 1),
(3, '半成品仓', 'WH003', '厂区A栋2楼', 1);

-- 11. 测试工序数据
INSERT INTO `process_info` (`id`, `process_name`, `process_code`, `product_id`, `sort_order`) VALUES
(1, '主板贴片', 'PR001', 1, 1),
(2, '屏幕组装', 'PR002', 1, 2),
(3, '外壳组装', 'PR003', 1, 3),
(4, '功能测试', 'PR004', 1, 4),
(5, '包装入库', 'PR005', 1, 5);

-- 12. 测试设备数据
INSERT INTO `equipment_info` (`id`, `equipment_code`, `equipment_name`, `equipment_type`, `status`, `location`, `purchase_date`) VALUES
(1, 'EQ001', 'SMT贴片机', '贴片设备', 1, '车间A-01', '2024-01-15'),
(2, 'EQ002', '回流焊炉', '焊接设备', 1, '车间A-02', '2024-01-20'),
(3, 'EQ003', '自动测试仪', '测试设备', 1, '车间B-01', '2024-02-10'),
(4, 'EQ004', '包装机', '包装设备', 1, '车间B-02', '2024-02-15');

-- 13. 生产计划示例数据
INSERT INTO `production_plan` (`id`, `plan_no`, `order_id`, `product_id`, `planned_quantity`, `start_date`, `end_date`, `status`) VALUES
(1, 'PL202501010001', NULL, 1, 500, '2025-01-10 08:00:00', '2025-01-15 18:00:00', 1),
(2, 'PL202501020001', NULL, 2, 300, '2025-01-12 08:00:00', '2025-01-18 18:00:00', 0),
(3, 'PL202501050001', NULL, 3, 1000, '2025-01-05 08:00:00', '2025-01-10 18:00:00', 2);

-- 14. 生产工单示例数据
INSERT INTO `work_order` (`id`, `work_no`, `plan_id`, `product_id`, `process_id`, `quantity`, `status`) VALUES
(1, 'WO202501100001', 1, 1, 1, 500, 2),
(2, 'WO202501100002', 1, 1, 2, 500, 2),
(3, 'WO202501100003', 1, 1, 3, 500, 1),
(4, 'WO202501100004', 1, 1, 4, 500, 0),
(5, 'WO202501100005', 1, 1, 5, 500, 0),
(6, 'WO202501050001', 3, 3, 1, 1000, 2),
(7, 'WO202501050002', 3, 3, 2, 1000, 2);

-- 15. 报工记录示例数据
INSERT INTO `production_report` (`id`, `work_order_id`, `worker_id`, `process_id`, `report_date`, `good_quantity`, `bad_quantity`) VALUES
(1, 1, 7, 1, '2025-01-10 10:30:00', 200, 2),
(2, 1, 7, 1, '2025-01-10 16:00:00', 298, 0),
(3, 2, 8, 2, '2025-01-11 09:00:00', 250, 1),
(4, 2, 8, 2, '2025-01-11 15:30:00', 248, 1),
(5, 3, 9, 3, '2025-01-12 10:00:00', 180, 3),
(6, 3, 9, 3, '2025-01-12 14:00:00', 200, 2),
(7, 6, 7, 1, '2025-01-06 10:00:00', 500, 5),
(8, 6, 7, 1, '2025-01-06 16:00:00', 495, 0);

-- 16. 质检任务示例数据
INSERT INTO `quality_task` (`id`, `task_no`, `task_type`, `source_id`, `inspector_id`, `status`, `create_time`) VALUES
(1, 'QT20250108001', 'IQC', 1, 6, 2, '2025-01-08 09:00:00'),
(2, 'QT20250110001', 'IPQC', 1, 6, 1, '2025-01-10 14:00:00'),
(3, 'QT20250112001', 'IPQC', 3, 6, 0, '2025-01-12 08:30:00'),
(4, 'QT20250115001', 'OQC', NULL, 6, 0, '2025-01-15 10:00:00');

-- 17. 质检结果示例数据
INSERT INTO `quality_result` (`id`, `task_id`, `check_item`, `standard_value`, `actual_value`, `result`, `remark`) VALUES
(1, 1, '外观检查', '无划痕、无变形', '合格', 1, '抽检100pcs全部合格'),
(2, 1, '尺寸测量', '长度20±0.1mm', '20.05mm', 1, '在公差范围内'),
(3, 1, '材质确认', 'ABS+PC', 'ABS+PC', 1, '材质报告齐全'),
(4, 2, '焊接质量', '无虚焊、假焊', '合格', 1, 'AOI检测通过'),
(5, 2, '贴片精度', '偏移量≤0.1mm', '0.08mm', 1, '符合要求'),
(6, 2, '功能初测', '全部功能正常', '正常', 1, '首件测试通过'),
(7, 3, '外观检查', '无划痕、无变形', '待检', 0, '');

-- 18. 异常记录示例数据
INSERT INTO `exception_record` (`id`, `exception_no`, `exception_type`, `work_order_id`, `description`, `reporter_id`, `status`, `report_time`, `handle_time`) VALUES
(1, 'EX20250110001', '设备异常', 1, 'SMT贴片机2号飞达进料不畅，导致贴片偏移', 7, 2, '2025-01-10 11:20:00', '2025-01-10 14:00:00'),
(2, 'EX20250112001', '品质异常', 3, '外壳组装发现5pcs按键手感不良', 9, 1, '2025-01-12 15:30:00', NULL),
(3, 'EX20250112002', '物料异常', 3, '物料批次B2025010存在色差问题', 9, 0, '2025-01-12 16:45:00', NULL);
