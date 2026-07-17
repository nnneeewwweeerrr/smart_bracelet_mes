-- ============================================================
-- 数据修复脚本：修复 unqualified_product 和 exception_record 表中文问号问题
-- 数据库：smart_bracelet_mes
-- 原因：数据写入时编码不正确导致中文变成 ?
-- 使用方法：在 Navicat 或 MySQL 客户端中执行此脚本
-- ============================================================

-- 先设置连接编码为 utf8mb4
SET NAMES utf8mb4;

-- ============================================================
-- 1. 修复 unqualified_product 表（10条记录）
-- ============================================================

UPDATE `unqualified_product` SET
  `source_type` = '来料检验', `material_name` = 'OLED屏幕', `defect_type` = '外观缺陷',
  `defect_desc` = '屏幕表面有划痕', `discoverer` = '质检员张三',
  `disposition` = '退货', `disposition_result` = '已退回供应商',
  `responsible` = '供应商', `rectify_action` = '加强来料检验', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202606001';

UPDATE `unqualified_product` SET
  `source_type` = '过程检验', `material_name` = '蓝牙芯片', `defect_type` = '焊接不良',
  `defect_desc` = '芯片引脚虚焊', `discoverer` = '质检员李四',
  `disposition` = '返修', `disposition_result` = '已返修合格',
  `responsible` = 'SMT车间', `rectify_action` = '调整回流焊参数', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202606002';

UPDATE `unqualified_product` SET
  `source_type` = '成品检验', `material_name` = '塑胶外壳', `defect_type` = '外观缺陷',
  `defect_desc` = '外壳表面有划伤', `discoverer` = '质检员王五',
  `disposition` = '返工', `disposition_result` = '已返工合格',
  `responsible` = '组装车间', `rectify_action` = '优化传送带清洁', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202606003';

UPDATE `unqualified_product` SET
  `source_type` = '来料检验', `material_name` = '塑胶外壳', `defect_type` = '尺寸偏差',
  `defect_desc` = '外壳尺寸超差', `discoverer` = '质检员张三',
  `disposition` = '退货', `disposition_result` = '已退回供应商',
  `responsible` = '供应商', `rectify_action` = '加强尺寸检验', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202606004';

UPDATE `unqualified_product` SET
  `source_type` = '过程检验', `material_name` = '心率传感器', `defect_type` = '焊接不良',
  `defect_desc` = '传感器引脚虚焊', `discoverer` = '质检员李四',
  `disposition` = '返修', `disposition_result` = '已返修合格',
  `responsible` = '组装车间', `rectify_action` = '培训操作员', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202606005';

UPDATE `unqualified_product` SET
  `source_type` = '客户退货', `material_name` = '蓝牙芯片', `defect_type` = '功能不良',
  `defect_desc` = '蓝牙连接不稳定', `discoverer` = '质检员张三',
  `disposition` = '报废', `disposition_result` = '已报废处理',
  `responsible` = 'SMT车间', `rectify_action` = '分析根本原因', `remark` = '客户退货'
WHERE `unqualified_no` = 'UQ202607001';

UPDATE `unqualified_product` SET
  `source_type` = '来料检验', `material_name` = '六轴传感器', `defect_type` = '包装破损',
  `defect_desc` = '物料包装破损', `discoverer` = '质检员李四',
  `disposition` = '退货', `disposition_result` = '已退回供应商',
  `responsible` = '供应商', `rectify_action` = '加强包装检验', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202607002';

UPDATE `unqualified_product` SET
  `source_type` = '过程检验', `material_name` = '包装盒', `defect_type` = '印刷不良',
  `defect_desc` = '包装盒印刷模糊', `discoverer` = '质检员王五',
  `disposition` = '返工', `disposition_result` = '已返工合格',
  `responsible` = '包装车间', `rectify_action` = '更换印刷模板', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202607003';

UPDATE `unqualified_product` SET
  `source_type` = '成品检验', `material_name` = 'USB充电线', `defect_type` = '功能不良',
  `defect_desc` = '充电线接触不良', `discoverer` = '质检员张三',
  `disposition` = '报废', `disposition_result` = '已报废处理',
  `responsible` = '组装车间', `rectify_action` = '更换供应商', `remark` = '已处理'
WHERE `unqualified_no` = 'UQ202607004';

UPDATE `unqualified_product` SET
  `source_type` = '过程检验', `material_name` = '心率传感器', `defect_type` = '焊接不良',
  `defect_desc` = '传感器焊接不良', `discoverer` = '质检员李四', `remark` = '待处置'
WHERE `unqualified_no` = 'UQ202607005';

-- ============================================================
-- 2. 修复 exception_record 表（12条记录）
-- ============================================================

UPDATE `exception_record` SET
  `exception_type` = '质量异常', `exception_level` = '紧急', `source_type` = '工单',
  `title` = '产品外观划伤', `description` = '第3批次组装成品发现外壳划伤，不良率5%',
  `reporter` = '张三', `handler` = '李四', `handle_content` = '隔离不良品，修复组装线传送带，更新作业指导',
  `handle_result` = '已解决', `cause_analysis` = '传送带表面有异物，导致外壳划伤',
  `preventive_action` = '每周清洁传送带', `remark` = '已解决'
WHERE `exception_no` = 'EX202606001';

UPDATE `exception_record` SET
  `exception_type` = '设备异常', `exception_level` = '重要', `source_type` = '工序',
  `title` = '回流焊炉温度异常', `description` = '回流焊炉第2区温度超出工艺范围±10度',
  `reporter` = '王五', `handler` = '张工程师', `handle_content` = '重新校准炉温，更换温控模块',
  `handle_result` = '已解决', `cause_analysis` = '温控模块老化导致温度漂移',
  `preventive_action` = '每月校验温控模块', `remark` = '已解决'
WHERE `exception_no` = 'EX202606002';

UPDATE `exception_record` SET
  `exception_type` = '物料异常', `exception_level` = '重要', `source_type` = '工单',
  `title` = '物料短缺', `description` = 'OLED屏幕库存不足，影响生产',
  `reporter` = '钱七', `handler` = '张仓管', `handle_content` = '紧急采购补货，调整生产顺序',
  `handle_result` = '已缓解', `cause_analysis` = '未及时更新安全库存',
  `preventive_action` = '完善库存预警机制', `remark` = '已缓解'
WHERE `exception_no` = 'EX202606003';

UPDATE `exception_record` SET
  `exception_type` = '质量异常', `exception_level` = '一般', `source_type` = '质检',
  `title` = '心率传感器不良', `description` = '心率传感器焊接不良率3%',
  `reporter` = '赵六', `handler` = '李工程师', `handle_content` = '调整回流焊参数，重新培训操作员',
  `handle_result` = '已解决', `cause_analysis` = '回流焊温度偏高导致虚焊',
  `preventive_action` = '优化工艺参数', `remark` = '已解决'
WHERE `exception_no` = 'EX202606004';

UPDATE `exception_record` SET
  `exception_type` = '工艺异常', `exception_level` = '一般', `source_type` = '工序',
  `title` = '注塑件尺寸偏差', `description` = '注塑件长度尺寸超出公差范围',
  `reporter` = '孙八', `handler` = '王工程师', `handle_content` = '调整模具温度和保压时间',
  `handle_result` = '已解决', `cause_analysis` = '模具温度不稳定',
  `preventive_action` = '增加模具温控设备', `remark` = '已解决'
WHERE `exception_no` = 'EX202606005';

UPDATE `exception_record` SET
  `exception_type` = '设备异常', `exception_level` = '一般', `source_type` = '工序',
  `title` = '激光打标偏移', `description` = '激光打标位置偏移0.3mm',
  `reporter` = '周九', `handler` = '钱工程师', `handle_content` = '调整振镜校准',
  `handle_result` = '已解决', `cause_analysis` = '振镜松动',
  `preventive_action` = '每日检查振镜固定', `remark` = '已解决'
WHERE `exception_no` = 'EX202606006';

UPDATE `exception_record` SET
  `exception_type` = '安全异常', `exception_level` = '紧急', `source_type` = '其他',
  `title` = '车间漏电', `description` = 'SMT车间发现漏电情况',
  `reporter` = '张工', `handler` = '安全员', `handle_content` = '断电检修，更换破损线缆',
  `handle_result` = '已解决', `cause_analysis` = '线缆老化破损',
  `preventive_action` = '每周安全检查', `remark` = '已关闭'
WHERE `exception_no` = 'EX202606007';

UPDATE `exception_record` SET
  `exception_type` = '物料异常', `exception_level` = '重要', `source_type` = '库存',
  `title` = '塑胶外壳色差', `description` = '新到塑胶外壳与样品存在色差',
  `reporter` = '李仓管', `handler` = '采购部', `handle_content` = '与供应商沟通补料',
  `handle_result` = '已解决', `cause_analysis` = '供应商原材料批次差异',
  `preventive_action` = '加强来料检验', `remark` = '已解决'
WHERE `exception_no` = 'EX202606008';

UPDATE `exception_record` SET
  `exception_type` = '质量异常', `exception_level` = '一般', `source_type` = '工单',
  `title` = '包装盒破损', `description` = '成品包装时发现包装盒破损率2%',
  `reporter` = '郑十', `handler` = '冯十一', `handle_content` = '更换包装盒供应商批次',
  `handle_result` = '已解决', `cause_analysis` = '包装盒材料强度不足',
  `preventive_action` = '更换供应商', `remark` = '已解决'
WHERE `exception_no` = 'EX202606009';

UPDATE `exception_record` SET
  `exception_type` = '设备异常', `exception_level` = '紧急', `source_type` = '工序',
  `title` = '气密性测试仪故障', `description` = '气密性测试仪压力异常，多个产品测试结果异常',
  `reporter` = '操作工小李', `handler` = '周工程师', `handle_content` = '更换压力传感器',
  `handle_result` = '已解决', `cause_analysis` = '压力传感器损坏',
  `preventive_action` = '每月校验测试设备', `remark` = '已关闭'
WHERE `exception_no` = 'EX202607010';

UPDATE `exception_record` SET
  `exception_type` = '工艺异常', `exception_level` = '一般', `source_type` = '工序',
  `title` = 'CNC加工精度偏差', `description` = 'CNC加工件尺寸超差',
  `reporter` = '赵六', `handler` = '赵工程师', `handle_content` = '重新对刀，补偿磨损',
  `handle_result` = '处理中', `cause_analysis` = '刀具磨损',
  `preventive_action` = '定期更换刀具', `remark` = '处理中'
WHERE `exception_no` = 'EX202607011';

UPDATE `exception_record` SET
  `exception_type` = '质量异常', `exception_level` = '一般', `source_type` = '工单',
  `title` = '充电不良', `description` = '新批次USB充电线充电不良率1%',
  `reporter` = '钱七', `cause_analysis` = '分析中',
  `preventive_action` = '加强来料检验', `remark` = '待处理'
WHERE `exception_no` = 'EX202607012';

-- ============================================================
-- 3. 修复 mes_demo 数据库的 Mojibake 乱码（UTF-8 被当 Latin-1 读）
-- 通过 CONVERT 函数修复
-- ============================================================
USE mes_demo;

-- 修复 demo_material 表
UPDATE `demo_material` SET
  `material_name` = CONVERT(CONVERT(`material_name` USING binary) USING utf8mb4),
  `spec` = CONVERT(CONVERT(`spec` USING binary) USING utf8mb4),
  `unit` = CONVERT(CONVERT(`unit` USING binary) USING utf8mb4)
WHERE `material_name` LIKE '%Ã%' OR `material_name` LIKE '%Â%';

-- 修复 demo_warehouse 表
UPDATE `demo_warehouse` SET
  `warehouse_name` = CONVERT(CONVERT(`warehouse_name` USING binary) USING utf8mb4),
  `location` = CONVERT(CONVERT(`location` USING binary) USING utf8mb4)
WHERE `warehouse_name` LIKE '%Ã%' OR `warehouse_name` LIKE '%Â%';

-- 修复 demo_stock 表
UPDATE `demo_stock` SET
  `material_name` = CONVERT(CONVERT(`material_name` USING binary) USING utf8mb4),
  `warehouse_name` = CONVERT(CONVERT(`warehouse_name` USING binary) USING utf8mb4)
WHERE `material_name` LIKE '%Ã%' OR `material_name` LIKE '%Â%';

-- 修复 demo_stock_in 表
UPDATE `demo_stock_in` SET
  `material_name` = CONVERT(CONVERT(`material_name` USING binary) USING utf8mb4),
  `warehouse_name` = CONVERT(CONVERT(`warehouse_name` USING binary) USING utf8mb4),
  `supplier_name` = CONVERT(CONVERT(`supplier_name` USING binary) USING utf8mb4),
  `type` = CONVERT(CONVERT(`type` USING binary) USING utf8mb4)
WHERE `material_name` LIKE '%Ã%' OR `material_name` LIKE '%Â%';

-- 修复 demo_stock_out 表
UPDATE `demo_stock_out` SET
  `material_name` = CONVERT(CONVERT(`material_name` USING binary) USING utf8mb4),
  `warehouse_name` = CONVERT(CONVERT(`warehouse_name` USING binary) USING utf8mb4),
  `type` = CONVERT(CONVERT(`type` USING binary) USING utf8mb4)
WHERE `material_name` LIKE '%Ã%' OR `material_name` LIKE '%Â%';

-- 修复 demo_stock_check 表
UPDATE `demo_stock_check` SET
  `warehouse_name` = CONVERT(CONVERT(`warehouse_name` USING binary) USING utf8mb4)
WHERE `warehouse_name` LIKE '%Ã%' OR `warehouse_name` LIKE '%Â%';

-- 修复 demo_stock_alert 表
UPDATE `demo_stock_alert` SET
  `material_name` = CONVERT(CONVERT(`material_name` USING binary) USING utf8mb4),
  `warehouse_name` = CONVERT(CONVERT(`warehouse_name` USING binary) USING utf8mb4),
  `alert_type` = CONVERT(CONVERT(`alert_type` USING binary) USING utf8mb4)
WHERE `material_name` LIKE '%Ã%' OR `material_name` LIKE '%Â%';

-- 修复 demo_equipment 表
UPDATE `demo_equipment` SET
  `equipment_name` = CONVERT(CONVERT(`equipment_name` USING binary) USING utf8mb4),
  `model` = CONVERT(CONVERT(`model` USING binary) USING utf8mb4),
  `manufacturer` = CONVERT(CONVERT(`manufacturer` USING binary) USING utf8mb4)
WHERE `equipment_name` LIKE '%Ã%' OR `equipment_name` LIKE '%Â%';

-- 修复 demo_equipment_fault 表
UPDATE `demo_equipment_fault` SET
  `equipment_name` = CONVERT(CONVERT(`equipment_name` USING binary) USING utf8mb4),
  `fault_desc` = CONVERT(CONVERT(`fault_desc` USING binary) USING utf8mb4),
  `handler` = CONVERT(CONVERT(`handler` USING binary) USING utf8mb4)
WHERE `equipment_name` LIKE '%Ã%' OR `equipment_name` LIKE '%Â%';

-- 修复 demo_repair_task 表
UPDATE `demo_repair_task` SET
  `equipment_name` = CONVERT(CONVERT(`equipment_name` USING binary) USING utf8mb4),
  `fault_desc` = CONVERT(CONVERT(`fault_desc` USING binary) USING utf8mb4),
  `repairer` = CONVERT(CONVERT(`repairer` USING binary) USING utf8mb4)
WHERE `equipment_name` LIKE '%Ã%' OR `equipment_name` LIKE '%Â%';

-- 修复 demo_equipment_maintain 表
UPDATE `demo_equipment_maintain` SET
  `equipment_name` = CONVERT(CONVERT(`equipment_name` USING binary) USING utf8mb4),
  `maintain_content` = CONVERT(CONVERT(`maintain_content` USING binary) USING utf8mb4),
  `maintainer` = CONVERT(CONVERT(`maintainer` USING binary) USING utf8mb4)
WHERE `equipment_name` LIKE '%Ã%' OR `equipment_name` LIKE '%Â%';

-- 完成
SELECT '数据修复完成！' AS message;
