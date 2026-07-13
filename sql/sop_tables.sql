-- ============================================
-- 关键工序防错 + SOP电子看板 扩展表
-- ============================================

-- 1. 工序SOP表（电子作业指导书）
DROP TABLE IF EXISTS `process_sop`;
CREATE TABLE `process_sop` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `process_id` BIGINT NOT NULL COMMENT '工序ID（关联process_info.id）',
  `sop_name` VARCHAR(200) NOT NULL COMMENT 'SOP名称',
  `sop_version` VARCHAR(20) DEFAULT 'V1.0' COMMENT 'SOP版本号',
  `operation_steps` TEXT COMMENT '操作步骤（JSON数组，每步含步骤描述+图文说明）',
  `precautions` TEXT COMMENT '注意事项',
  `equipment_requirement` VARCHAR(500) COMMENT '设备要求说明',
  `status` INT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序SOP电子作业指导书';

-- 2. 工序工艺参数标准表（阈值配置）
DROP TABLE IF EXISTS `process_param_standard`;
CREATE TABLE `process_param_standard` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `process_id` BIGINT NOT NULL COMMENT '工序ID',
  `param_name` VARCHAR(100) NOT NULL COMMENT '参数名称（如焊接温度）',
  `param_code` VARCHAR(50) NOT NULL COMMENT '参数编码（如temperature）',
  `unit` VARCHAR(20) COMMENT '单位（如℃、MPa、mm/s）',
  `min_value` DECIMAL(10,2) COMMENT '最小值（标准下限）',
  `max_value` DECIMAL(10,2) COMMENT '最大值（标准上限）',
  `target_value` DECIMAL(10,2) COMMENT '目标值',
  `is_required` INT DEFAULT 1 COMMENT '是否必检：1是 0否',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序工艺参数标准';

-- 3. 工序物料BOM表（防错校验用）
DROP TABLE IF EXISTS `process_material_bom`;
CREATE TABLE `process_material_bom` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `process_id` BIGINT NOT NULL COMMENT '工序ID',
  `material_id` BIGINT NOT NULL COMMENT '物料ID（关联material_info.id）',
  `quantity` DECIMAL(10,2) DEFAULT 1 COMMENT '单件用量',
  `is_required` INT DEFAULT 1 COMMENT '是否必须校验：1是 0否',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序物料BOM（防错校验）';

-- ============================================
-- 初始化SOP数据（基于现有6道工序）
-- ============================================

-- 假设process_info已有6条记录（id 1-6），对应SMT贴片/焊接组装/固件烧录/功能测试/外观检查/包装入库

-- SOP数据
INSERT INTO `process_sop` (`process_id`, `sop_name`, `sop_version`, `operation_steps`, `precautions`, `equipment_requirement`, `status`) VALUES
(1, 'SMT贴片标准作业指导书', 'V1.0', '["检查PCB板外观，确认无损伤、无氧化","核对元件清单，确认物料型号与BOM一致","将PCB板放入贴片机，设置贴片参数","启动贴片程序，监控贴片过程","贴片完成后检查元件贴装精度","进行AOI光学检测，确认焊点质量"]', '操作时必须佩戴防静电手环，保持工作台整洁。贴片过程中禁止触碰元件引脚，避免静电损坏元件。', 'SMT贴片机、AOI检测仪、防静电工作台', 1),
(2, '焊接组装标准作业指导书', 'V1.0', '["准备焊接工具：烙铁、焊锡丝、助焊剂","检查焊接工位温度，烙铁温度设置为360±10°C","将贴片完成的PCB板固定在焊接夹具上","手工焊接连接器、电池座等元件","焊接完成后检查焊点质量，确认无虚焊、连锡","清理PCB板表面残留助焊剂"]', '焊接时注意安全，防止烫伤。焊锡量要适中，避免过多或过少导致焊接不良。', '恒温烙铁、焊接夹具、放大镜、防静电手环', 1),
(3, '固件烧录标准作业指导书', 'V1.0', '["连接烧录器与PC，打开烧录软件","选择对应产品型号的固件版本","将待烧录PCB板连接到烧录器","启动烧录程序，等待烧录完成","烧录完成后进行校验，确认固件完整性","记录烧录批次和固件版本信息"]', '烧录过程中禁止断电，确保连接稳定。确认固件版本与产品型号匹配。', '烧录器、PC烧录工作站、防静电手套', 1),
(4, '功能测试标准作业指导书', 'V1.0', '["连接测试治具，接通电源","测试屏幕显示：确认屏幕点亮、显示清晰","测试按键功能：触摸按键响应正常","测试传感器功能：心率、血氧、加速度计","测试蓝牙连接：能正常连接手机APP","测试电池充电：充电电流正常，充电指示正常","测试防水功能：进行IP67防水测试"]', '测试前确认测试设备已校准。测试过程中记录每项测试结果，不合格品及时标记。', '功能测试治具、心率模拟器、蓝牙测试仪、万用表', 1),
(5, '外观检查标准作业指导书', 'V1.0', '["在明亮光线下检查产品外观","检查外壳表面：无划痕、无变形、无色差","检查屏幕：无气泡、无灰尘、无坏点","检查接口：USB接口无歪斜、无松动","检查装配：外壳缝隙均匀，无毛刺","检查印刷：Logo、文字清晰、无错印"]', '外观检查采用标准对比样品进行比对。对可疑缺陷使用放大镜仔细确认。', '标准光源箱、放大镜、对比样品架', 1),
(6, '包装入库标准作业指导书', 'V1.0', '["准备包装盒、说明书、充电器等配件","将合格产品放入防静电袋","放入包装盒，确认配件齐全","密封包装盒，粘贴合格证标签","扫描条码入库，记录入库数量","将产品整齐摆放到货架指定位置"]', '包装前确认产品已通过所有测试。入库时确认库存位置与系统记录一致。', '包装工作台、条码扫描枪、标签打印机', 1);

-- 工艺参数标准数据
INSERT INTO `process_param_standard` (`process_id`, `param_name`, `param_code`, `unit`, `min_value`, `max_value`, `target_value`, `is_required`, `sort_order`) VALUES
-- SMT贴片
(1, '贴片温度', 'temperature', '℃', 230.00, 250.00, 240.00, 1, 1),
(1, '贴片速度', 'speed', 'mm/s', 80.00, 120.00, 100.00, 1, 2),
(1, '刮刀压力', 'pressure', 'MPa', 0.3, 0.6, 0.45, 1, 3),
-- 焊接组装
(2, '焊接温度', 'temperature', '℃', 350.00, 380.00, 365.00, 1, 1),
(2, '焊接时间', 'time', 's', 2.00, 5.00, 3.50, 1, 2),
(2, '环境湿度', 'humidity', '%RH', 30.00, 60.00, 45.00, 0, 3),
-- 固件烧录
(3, '烧录电压', 'voltage', 'V', 3.2, 3.4, 3.30, 1, 1),
(3, '烧录时间', 'time', 's', 15.00, 30.00, 20.00, 1, 2),
-- 功能测试
(4, '测试电压', 'voltage', 'V', 3.0, 4.2, 3.70, 1, 1),
(4, '心率误差', 'deviation', 'bpm', 0.00, 5.00, 2.00, 1, 2),
(4, '蓝牙信号', 'signal', 'dBm', -70.00, -30.00, -50.00, 0, 3),
-- 外观检查
(5, '检查照度', 'lux', 'lux', 800.00, 1200.00, 1000.00, 1, 1),
-- 包装入库
(6, '封箱压力', 'pressure', 'MPa', 0.2, 0.5, 0.35, 0, 1),
(6, '标签清晰度', 'clarity', '%', 95.00, 100.00, 100.00, 0, 2);

-- 工序物料BOM数据（假设material_info已有数据，id 1-10）
INSERT INTO `process_material_bom` (`process_id`, `material_id`, `quantity`, `is_required`, `sort_order`) VALUES
-- SMT贴片需要的物料
(1, 1, 1.00, 1, 1),
(1, 2, 1.00, 1, 2),
-- 焊接组装需要的物料
(2, 3, 1.00, 1, 1),
(2, 4, 2.00, 1, 2),
-- 固件烧录
(3, 1, 1.00, 1, 1),
-- 功能测试
(4, 5, 1.00, 0, 1),
-- 外观检查（无需物料）
-- 包装入库
(6, 6, 1.00, 1, 1),
(6, 7, 1.00, 1, 2),
(6, 8, 1.00, 1, 3);
