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

-- ============================================
-- 丰富SOP数据：更新操作步骤格式、新增版本/参数/物料
-- ============================================

-- 1. 更新已有SOP的operation_steps为详细JSON格式（含stepName、content、tip）
UPDATE `process_sop` SET `operation_steps` = '[{"stepName":"PCB板来料检查","content":"检查PCB板外观，确认无损伤、无氧化、无变形","tip":"氧化严重的PCB板需退回仓库，禁止使用"},{"stepName":"物料核对","content":"核对元件清单，确认物料型号与BOM一致","tip":"必须双人核对，防止错料"},{"stepName":"PCB板上料","content":"将PCB板放入贴片机，设置贴片参数","tip":"注意PCB板方向，标记点朝左下角"},{"stepName":"启动贴片程序","content":"启动贴片程序，监控贴片过程","tip":"首件必须停机检查贴装精度"},{"stepName":"贴装精度检查","content":"贴片完成后检查元件贴装精度","tip":"偏移量超过0.1mm需调整贴片坐标"},{"stepName":"AOI光学检测","content":"进行AOI光学检测，确认焊点质量","tip":"AOI报警必须人工复检确认"}]' WHERE `id` = 1;

UPDATE `process_sop` SET `operation_steps` = '[{"stepName":"焊接工具准备","content":"准备焊接工具：烙铁、焊锡丝、助焊剂","tip":"烙铁头必须提前预热5分钟"},{"stepName":"温度校准","content":"检查焊接工位温度，烙铁温度设置为360±10°C","tip":"温度偏差超过10°C需更换烙铁芯"},{"stepName":"PCB板固定","content":"将贴片完成的PCB板固定在焊接夹具上","tip":"确认夹具定位销完全卡入PCB定位孔"},{"stepName":"手工焊接","content":"手工焊接连接器、电池座等元件","tip":"每个焊点焊接时间不超过3秒，防止过热"},{"stepName":"焊点质量检查","content":"焊接完成后检查焊点质量，确认无虚焊、连锡","tip":"虚焊是最常见缺陷，重点检查边角焊点"},{"stepName":"表面清洁","content":"清理PCB板表面残留助焊剂","tip":"使用专用清洗剂，禁止使用酒精替代"}]' WHERE `id` = 2;

UPDATE `process_sop` SET `operation_steps` = '[{"stepName":"烧录器连接","content":"连接烧录器与PC，打开烧录软件","tip":"确认驱动安装正确，设备管理器无黄色感叹号"},{"stepName":"固件版本选择","content":"选择对应产品型号的固件版本","tip":"必须核对产品型号与固件版本匹配表"},{"stepName":"PCB板连接","content":"将待烧录PCB板连接到烧录器","tip":"注意接口方向，禁止反插"},{"stepName":"启动烧录","content":"启动烧录程序，等待烧录完成","tip":"烧录过程中禁止触碰连接线，防止断连"},{"stepName":"固件校验","content":"烧录完成后进行校验，确认固件完整性","tip":"校验失败需更换芯片重新烧录"},{"stepName":"记录登记","content":"记录烧录批次和固件版本信息","tip":"每批次首件需签名确认"}]' WHERE `id` = 3;

UPDATE `process_sop` SET `operation_steps` = '[{"stepName":"测试治具连接","content":"连接测试治具，接通电源","tip":"确认治具供电电压为5V，禁止使用超过5.5V电源"},{"stepName":"屏幕显示测试","content":"测试屏幕显示：确认屏幕点亮、显示清晰","tip":"检查有无坏点、色斑、亮线"},{"stepName":"按键功能测试","content":"测试按键功能：触摸按键响应正常","tip":"每个按键需测试3次确认一致性"},{"stepName":"传感器测试","content":"测试传感器功能：心率、血氧、加速度计","tip":"心率测试需使用模拟器，禁止空测"},{"stepName":"蓝牙连接测试","content":"测试蓝牙连接：能正常连接手机APP","tip":"测试距离需超过3米"},{"stepName":"充电测试","content":"测试电池充电：充电电流正常，充电指示正常","tip":"充电电流应在200-500mA范围内"},{"stepName":"防水测试","content":"测试防水功能：进行IP67防水测试","tip":"测试后必须检查内部是否有水汽"}]' WHERE `id` = 4;

UPDATE `process_sop` SET `operation_steps` = '[{"stepName":"光源准备","content":"在明亮光线下检查产品外观","tip":"照度需达到800lux以上"},{"stepName":"外壳表面检查","content":"检查外壳表面：无划痕、无变形、无色差","tip":"划痕长度超过2mm判为不合格"},{"stepName":"屏幕检查","content":"检查屏幕：无气泡、无灰尘、无坏点","tip":"在黑底白底两种背景下分别检查"},{"stepName":"接口检查","content":"检查接口：USB接口无歪斜、无松动","tip":"插入充电线测试松紧度"},{"stepName":"装配检查","content":"检查装配：外壳缝隙均匀，无毛刺","tip":"缝隙不均匀度超过0.3mm需返工"},{"stepName":"印刷检查","content":"检查印刷：Logo、文字清晰、无错印","tip":"与标准样品对比确认"}]' WHERE `id` = 5;

UPDATE `process_sop` SET `operation_steps` = '[{"stepName":"包装材料准备","content":"准备包装盒、说明书、充电器等配件","tip":"核对配件清单，缺少任何配件不得开始包装"},{"stepName":"防静电包装","content":"将合格产品放入防静电袋","tip":"防静电袋必须完全密封"},{"stepName":"装箱操作","content":"放入包装盒，确认配件齐全","tip":"说明书放在最上层，充电线理顺放入"},{"stepName":"封箱贴标","content":"密封包装盒，粘贴合格证标签","tip":"标签必须粘贴在指定位置，不可歪斜"},{"stepName":"扫码入库","content":"扫描条码入库，记录入库数量","tip":"每箱扫码确认，禁止批量扫码"},{"stepName":"货架摆放","content":"将产品整齐摆放到货架指定位置","tip":"按照批次分区摆放，先进先出"}]' WHERE `id` = 6;

-- 2. 为SMT贴片工序增加V2.0版本SOP（工艺改进版本）
INSERT INTO `process_sop` (`process_id`, `sop_name`, `sop_version`, `operation_steps`, `precautions`, `equipment_requirement`, `status`) VALUES
(1, 'SMT贴片标准作业指导书（工艺改进版）', 'V2.0',
'[{"stepName":"PCB板自动检测","content":"使用AOI预检设备自动扫描PCB板，检测来料质量","tip":"V2.0新增自动预检环节，替代人工目检"},{"stepName":"智能物料核对","content":"通过扫码枪扫描物料条码，系统自动与BOM比对","tip":"V2.0使用扫码替代人工核对，降低错料率"},{"stepName":"PCB板自动上料","content":"通过自动上料机将PCB板送入贴片机，系统自动识别板型","tip":"V2.0新增自动上料，减少人工操作"},{"stepName":"贴片参数智能调优","content":"贴片机根据PCB板型号自动加载最优参数，启动贴片","tip":"V2.0参数自优化，首件精度提升30%"},{"stepName":"3D SPI检测","content":"使用3D SPI设备检测焊膏印刷质量，确认锡膏高度和面积","tip":"V2.0新增3D SPI替代2D AOI，检测更全面"},{"stepName":"贴装精度AI复检","content":"贴片完成后AI视觉系统自动复检贴装精度","tip":"V2.0使用AI复检，误判率降低50%"},{"stepName":"在线AOI全检","content":"进行在线AOI光学检测，系统自动分类缺陷类型","tip":"V2.0 AOI自动分类，减少人工判断"}]',
'V2.0版本采用智能检测设备，减少人工操作环节。操作时仍需佩戴防静电手环，保持工作台整洁。AI检测报警时必须人工复检确认。',
'SMT贴片机（支持参数自优化）、3D SPI检测仪、AI视觉复检系统、自动上料机、防静电工作台', 1);

-- 3. 增加更多工艺参数标准数据（每个工序至少5个参数）
INSERT INTO `process_param_standard` (`process_id`, `param_name`, `param_code`, `unit`, `min_value`, `max_value`, `target_value`, `is_required`, `sort_order`) VALUES
-- SMT贴片（已有3个，补2个）
(1, '锡膏厚度', 'paste_height', 'mm', 0.10, 0.15, 0.12, 1, 4),
(1, '贴装压力', 'mount_pressure', 'N', 1.00, 3.00, 2.00, 1, 5),
-- 焊接组装（已有3个，补2个）
(2, '焊锡丝直径', 'wire_diameter', 'mm', 0.50, 0.80, 0.60, 1, 4),
(2, '助焊剂比例', 'flux_ratio', '%', 1.50, 3.00, 2.00, 0, 5),
-- 固件烧录（已有2个，补3个）
(3, '烧录速率', 'baud_rate', 'kbps', 100.00, 500.00, 256.00, 1, 3),
(3, '校验方式', 'checksum', 'type', 0.00, 1.00, 1.00, 1, 4),
(3, '擦除时间', 'erase_time', 's', 5.00, 15.00, 10.00, 0, 5),
-- 功能测试（已有3个，补2个）
(4, '充电电流', 'charge_current', 'mA', 200.00, 500.00, 350.00, 1, 4),
(4, '屏幕亮度', 'brightness', 'cd/m2', 200.00, 400.00, 300.00, 0, 5),
-- 外观检查（已有1个，补4个）
(5, '划痕限度', 'scratch_limit', 'mm', 0.00, 2.00, 0.00, 1, 2),
(5, '缝隙均匀度', 'gap_uniform', 'mm', 0.00, 0.30, 0.10, 1, 3),
(5, '色差DeltaE', 'color_delta', 'ΔE', 0.00, 3.00, 1.00, 1, 4),
(5, '印刷清晰度', 'print_clarity', '%', 90.00, 100.00, 98.00, 0, 5),
-- 包装入库（已有2个，补3个）
(6, '包装重量', 'weight', 'g', 180.00, 220.00, 200.00, 1, 3),
(6, '防静电电阻', 'esd_resistance', 'Ω', 1e5, 1e9, 1e7, 1, 4),
(6, '条码识别率', 'barcode_rate', '%', 99.00, 100.00, 100.00, 1, 5);

-- 4. 增加更多物料BOM数据（每个工序至少3种物料）
INSERT INTO `process_material_bom` (`process_id`, `material_id`, `quantity`, `is_required`, `sort_order`) VALUES
-- SMT贴片（已有2条，补1条）
(1, 9, 0.05, 1, 3),
-- 焊接组装（已有2条，补1条）
(2, 10, 0.02, 1, 3),
-- 固件烧录（已有1条，补2条）
(3, 2, 1.00, 1, 2),
(3, 10, 0.01, 0, 3),
-- 功能测试（已有1条，补2条）
(4, 3, 0.50, 1, 2),
(4, 9, 0.01, 0, 3),
-- 外观检查（补3条，原来没有物料）
(5, 10, 0.01, 0, 1),
(5, 4, 1.00, 0, 2),
(5, 9, 0.01, 0, 3),
-- 包装入库（已有3条，无需补充）
