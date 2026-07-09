INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'admin', '系统超级管理员，拥有所有权限'),
(2, '订单专员', 'order_clerk', '负责订单管理相关工作'),
(3, '采购专员', 'purchase_clerk', '负责采购管理相关工作'),
(4, '仓库管理员', 'warehouse_keeper', '负责仓储管理相关工作'),
(5, '生产主管', 'production_manager', '负责生产管理相关工作'),
(6, '质检人员', 'quality_inspector', '负责质量管理相关工作'),
(7, '一线工人', 'worker', '负责生产报工、异常上报');

INSERT INTO `sys_user` (`id`, `user_name`, `password`, `real_name`, `phone`, `email`, `role_id`, `status`) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '管理员', '13800138000', 'admin@example.com', 1, 1),
(2, 'worker001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '张三', '13800138001', 'worker001@example.com', 7, 1);

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

INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),(1, 7),(1, 8),(1, 9),(1, 10),
(1, 11),(1, 12),(1, 13),(1, 14),(1, 15),(1, 16),(1, 17),(1, 18),(1, 19),
(1, 20),(1, 21),(1, 22),(1, 23),(1, 24),(1, 25),(1, 26),(1, 27),(1, 28),
(2, 1),(2, 2),(2, 3),(2, 4),
(3, 5),(3, 6),(3, 7),
(4, 8),(4, 9),(4, 10),(4, 11),
(5, 12),(5, 13),(5, 14),
(6, 16),(6, 17),(6, 18),
(7, 15),(7, 27);

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
('work_order_status', '0', '待派发', 1, 1),
('work_order_status', '1', '执行中', 2, 1),
('work_order_status', '2', '已完成', 3, 1),
('quality_task_status', '0', '待处理', 1, 1),
('quality_task_status', '1', '处理中', 2, 1),
('quality_task_status', '2', '已完成', 3, 1),
('equipment_status', '0', '停用', 1, 1),
('equipment_status', '1', '运行', 2, 1),
('equipment_status', '2', '维修', 3, 1),
('exception_status', '0', '待处理', 1, 1),
('exception_status', '1', '处理中', 2, 1),
('exception_status', '2', '已解决', 3, 1),
('exception_type', 'equipment', '设备故障', 1, 1),
('exception_type', 'material', '物料短缺', 2, 1),
('exception_type', 'quality', '质量问题', 3, 1),
('exception_type', 'other', '其他异常', 4, 1),
('task_type', 'incoming', '来料检验', 1, 1),
('task_type', 'process', '过程检验', 2, 1),
('task_type', 'finished', '成品检验', 3, 1),
('material_type', 'raw', '原材料', 1, 1),
('material_type', 'component', '零部件', 2, 1),
('material_type', 'finished', '成品', 3, 1),
('product_type', 'sport', '运动手环', 1, 1),
('product_type', 'health', '健康手环', 2, 1),
('product_type', 'fashion', '时尚手环', 3, 1),
('warehouse_type', 'raw_material', '原材料仓', 1, 1),
('warehouse_type', 'workshop', '车间仓', 2, 1),
('warehouse_type', 'finished', '成品仓', 3, 1);

INSERT INTO `customer_info` (`customer_code`, `customer_name`, `contact_name`, `phone`, `status`) VALUES
('CUS001', '北京科技有限公司', '李四', '13900139001', 1),
('CUS002', '上海智能科技有限公司', '王五', '13900139002', 1);

INSERT INTO `supplier_info` (`supplier_name`, `contact_name`, `phone`, `address`, `status`) VALUES
('深圳电子元件有限公司', '赵六', '13900139003', '深圳市南山区科技园', 1),
('东莞塑胶制品有限公司', '孙七', '13900139004', '东莞市长安镇', 1);

INSERT INTO `product_info` (`product_code`, `product_name`, `product_type`, `specification`, `unit_price`, `status`) VALUES
('PRD001', '智能运动手环A1', 'sport', '黑色/白色/蓝色', 199.00, 1),
('PRD002', '健康监测手环H1', 'health', '心率监测/睡眠监测', 299.00, 1);

INSERT INTO `material_info` (`material_code`, `material_name`, `material_type`, `unit`, `unit_price`, `safety_stock`, `status`) VALUES
('MAT001', 'OLED显示屏', 'component', '个', 35.00, 100, 1),
('MAT002', '锂电池', 'component', '个', 25.00, 200, 1),
('MAT003', '塑胶外壳', 'component', '个', 15.00, 500, 1),
('MAT004', 'PCB主板', 'component', '个', 45.00, 100, 1),
('MAT005', '蓝牙模块', 'component', '个', 18.00, 150, 1);

INSERT INTO `warehouse_info` (`warehouse_name`, `warehouse_code`, `address`, `status`) VALUES
('原材料仓', 'WH001', '厂区A栋1楼', 1),
('车间仓', 'WH002', '厂区B栋2楼', 1),
('成品仓', 'WH003', '厂区C栋1楼', 1);

INSERT INTO `process_info` (`process_name`, `process_code`, `product_id`, `sort_order`) VALUES
('SMT贴片', 'PROC001', 1, 1),
('焊接组装', 'PROC002', 1, 2),
('固件烧录', 'PROC003', 1, 3),
('功能测试', 'PROC004', 1, 4),
('外观检查', 'PROC005', 1, 5),
('包装入库', 'PROC006', 1, 6);

INSERT INTO `equipment_info` (`equipment_code`, `equipment_name`, `equipment_type`, `status`, `location`, `purchase_date`) VALUES
('EQ001', 'SMT贴片机', '生产设备', 1, 'B栋1线', '2024-01-15'),
('EQ002', '回流焊', '生产设备', 1, 'B栋1线', '2024-01-15'),
('EQ003', '焊接机器人', '生产设备', 1, 'B栋2线', '2024-02-20'),
('EQ004', '测试治具', '检测设备', 1, 'B栋测试区', '2024-03-10'),
('EQ005', '包装机', '包装设备', 1, 'C栋包装区', '2024-04-05');