-- ============================================================
-- 智能手环MES系统 - 订单采购基础档案模块重建脚本
-- 删除旧表，创建新表，插入丰富数据
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `order_customer`;
DROP TABLE IF EXISTS `purchase_order`;
DROP TABLE IF EXISTS `customer_info`;
DROP TABLE IF EXISTS `supplier_info`;
DROP TABLE IF EXISTS `material_info`;
DROP TABLE IF EXISTS `product_info`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. 客户档案表 (customer_info)
-- ============================================================
CREATE TABLE IF NOT EXISTS `customer_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    `customer_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '客户编号',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `contact` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `address` VARCHAR(255) COMMENT '地址',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `industry` VARCHAR(50) COMMENT '所属行业',
    `level` VARCHAR(20) COMMENT '客户等级(A/B/C)',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_customer_no` (`customer_no`),
    INDEX `idx_customer_name` (`customer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户档案表';

-- ============================================================
-- 2. 产品档案表 (product_info)
-- ============================================================
CREATE TABLE IF NOT EXISTS `product_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    `product_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '产品编号',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `category` VARCHAR(50) COMMENT '产品类别',
    `model` VARCHAR(100) COMMENT '型号规格',
    `price` DECIMAL(10,2) COMMENT '销售单价',
    `cost` DECIMAL(10,2) COMMENT '成本价',
    `unit` VARCHAR(20) DEFAULT '个' COMMENT '计量单位',
    `stock_qty` INT DEFAULT 0 COMMENT '库存数量',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `description` VARCHAR(500) COMMENT '产品描述',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_product_no` (`product_no`),
    INDEX `idx_product_name` (`product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品档案表';

-- ============================================================
-- 3. 供应商档案表 (supplier_info)
-- ============================================================
CREATE TABLE IF NOT EXISTS `supplier_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商ID',
    `supplier_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '供应商编号',
    `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `address` VARCHAR(255) COMMENT '地址',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `level` VARCHAR(20) COMMENT '供应商等级(A/B/C)',
    `credit_rating` VARCHAR(20) COMMENT '信用评级',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_supplier_no` (`supplier_no`),
    INDEX `idx_supplier_name` (`supplier_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商档案表';

-- ============================================================
-- 4. 物料档案表 (material_info)
-- ============================================================
CREATE TABLE IF NOT EXISTS `material_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物料ID',
    `material_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '物料编号',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `type` VARCHAR(50) COMMENT '物料类型',
    `spec` VARCHAR(100) COMMENT '规格型号',
    `unit` VARCHAR(20) DEFAULT '个' COMMENT '计量单位',
    `price` DECIMAL(10,2) COMMENT '采购单价',
    `stock_qty` INT DEFAULT 0 COMMENT '库存数量',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `supplier_id` BIGINT COMMENT '默认供应商ID',
    `supplier_name` VARCHAR(100) COMMENT '默认供应商名称',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_material_no` (`material_no`),
    INDEX `idx_material_name` (`material_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料档案表';

-- ============================================================
-- 5. 订单管理表 (order_customer)
-- ============================================================
CREATE TABLE IF NOT EXISTS `order_customer` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '订单编号',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `customer_name` VARCHAR(100) COMMENT '客户名称',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `product_name` VARCHAR(100) COMMENT '产品名称',
    `quantity` INT NOT NULL COMMENT '订单数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批,2生产中,3已完成,4已取消)',
    `delivery_date` DATETIME COMMENT '交货日期',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_customer_id` (`customer_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单管理表';

-- ============================================================
-- 6. 采购管理表 (purchase_order)
-- ============================================================
CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '采购单ID',
    `purchase_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '采购单号',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(100) COMMENT '供应商名称',
    `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `material_name` VARCHAR(100) COMMENT '物料名称',
    `quantity` INT NOT NULL COMMENT '采购数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `status` INT DEFAULT 0 COMMENT '状态(0待审批,1已审批,2已到货,3已完成)',
    `expected_date` DATETIME COMMENT '预计到货日期',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_purchase_no` (`purchase_no`),
    INDEX `idx_supplier_id` (`supplier_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购管理表';

-- ============================================================
-- 插入数据
-- ============================================================

-- 1. 客户档案数据 (12条)
INSERT INTO `customer_info` (`id`, `customer_no`, `customer_name`, `contact`, `phone`, `email`, `address`, `province`, `city`, `industry`, `level`, `status`, `remark`) VALUES
(1, 'CUST001', '华为技术有限公司', '张经理', '13800000001', 'huawei@example.com', '深圳市龙岗区坂田华为基地', '广东', '深圳', '通信', 'A', 1, '战略合作伙伴'),
(2, 'CUST002', '小米通讯技术有限公司', '雷总', '13800000002', 'xiaomi@example.com', '北京市海淀区清河中街', '北京', '北京', '电子', 'A', 1, '重要客户'),
(3, 'CUST003', 'OPPO广东移动通信有限公司', '陈总', '13800000003', 'oppo@example.com', '东莞市长安镇乌沙社区', '广东', '东莞', '电子', 'A', 1, '长期合作'),
(4, 'CUST004', 'vivo移动通信有限公司', '沈总', '13800000004', 'vivo@example.com', '东莞市长安镇步步高大道', '广东', '东莞', '电子', 'A', 1, '核心客户'),
(5, 'CUST005', '北京京东世纪贸易有限公司', '刘经理', '13800000005', 'jd@example.com', '北京市大兴区亦庄经济开发区', '北京', '北京', '电商', 'B', 1, '线上渠道'),
(6, 'CUST006', '苏宁易购集团股份有限公司', '孙总', '13800000006', 'suning@example.com', '南京市玄武区苏宁大道', '江苏', '南京', '电商', 'B', 1, '线上渠道'),
(7, 'CUST007', '深圳大疆创新科技有限公司', '汪总', '13800000007', 'dji@example.com', '深圳市南山区科技园', '广东', '深圳', '智能硬件', 'A', 1, '优质客户'),
(8, 'CUST008', '杭州阿里巴巴集团', '马总', '13800000008', 'alibaba@example.com', '杭州市余杭区文一西路', '浙江', '杭州', '互联网', 'B', 1, '电商平台'),
(9, 'CUST009', '腾讯科技(深圳)有限公司', '马总', '13800000009', 'tencent@example.com', '深圳市南山区科技园', '广东', '深圳', '互联网', 'B', 1, '互联网巨头'),
(10, 'CUST010', '百度在线网络技术有限公司', '李总', '13800000010', 'baidu@example.com', '北京市海淀区中关村', '北京', '北京', '互联网', 'B', 1, '搜索引擎'),
(11, 'CUST011', '美团点评科技有限公司', '王总', '13800000011', 'meituan@example.com', '北京市海淀区中关村', '北京', '北京', '互联网', 'C', 1, '本地生活'),
(12, 'CUST012', '顺丰速运集团有限公司', '王总', '13800000012', 'sf@example.com', '深圳市南山区科技园', '广东', '深圳', '物流', 'C', 1, '物流合作');

-- 2. 产品档案数据 (12条)
INSERT INTO `product_info` (`id`, `product_no`, `product_name`, `category`, `model`, `price`, `cost`, `unit`, `stock_qty`, `safety_stock`, `description`, `status`, `remark`) VALUES
(1, 'PRD001', '智能手环X1 Pro', '手环', '黑色/尊享版/防水IP68', 499.00, 349.30, '个', 2500, 500, '旗舰级智能手环，支持心率、血氧、GPS', 1, '热销产品'),
(2, 'PRD002', '智能手环S2', '手环', '蓝色/运动版/心率监测', 399.00, 279.30, '个', 3200, 600, '运动版智能手环，防水设计', 1, '主力产品'),
(3, 'PRD003', '智能手环Kids', '手环', '粉色/儿童版/定位功能', 299.00, 209.30, '个', 1800, 400, '儿童定位手环，安全守护', 1, '儿童系列'),
(4, 'PRD004', '智能手环Sport', '手环', '绿色/专业运动版/GPS', 599.00, 419.30, '个', 1200, 300, '专业运动手环，内置GPS', 1, '高端系列'),
(5, 'PRD005', '智能手环Elder', '手环', '白色/老人健康版/血压监测', 399.00, 279.30, '个', 900, 300, '老人健康手环，血压监测', 1, '健康系列'),
(6, 'PRD006', '智能手环Lite', '手环', '灰色/基础版/计步睡眠', 199.00, 139.30, '个', 5500, 1000, '入门级手环，基础功能', 1, '入门系列'),
(7, 'PRD007', '智能手表W1', '手表', '银色/商务版/蓝牙通话', 899.00, 629.30, '个', 800, 200, '智能手表，支持蓝牙通话', 1, '新品上市'),
(8, 'PRD008', '智能手表W2', '手表', '黑色/运动版/50米防水', 1299.00, 909.30, '个', 500, 150, '高端智能手表，运动追踪', 1, '旗舰系列'),
(9, 'PRD009', '智能耳机E1', '耳机', '白色/无线降噪', 299.00, 209.30, '个', 2000, 400, '无线蓝牙耳机，主动降噪', 1, '音频系列'),
(10, 'PRD010', '智能耳机E2', '耳机', '黑色/真无线', 199.00, 139.30, '个', 3000, 600, '真无线耳机，小巧便携', 1, '入门音频'),
(11, 'PRD011', '智能体脂秤S1', '健康设备', '白色/蓝牙连接', 199.00, 139.30, '台', 1500, 300, '体脂秤，15项身体数据', 1, '健康系列'),
(12, 'PRD012', '智能门锁L1', '智能家居', '银色/指纹密码', 999.00, 699.30, '套', 600, 150, '智能门锁，多重开锁方式', 1, '智能家居');

-- 3. 供应商档案数据 (12条)
INSERT INTO `supplier_info` (`id`, `supplier_no`, `supplier_name`, `contact`, `phone`, `email`, `address`, `province`, `city`, `level`, `credit_rating`, `status`, `remark`) VALUES
(1, 'SUP001', '深圳鑫达电子有限公司', '王经理', '13900000001', 'xinda@example.com', '深圳市宝安区西乡街道', '广东', '深圳', 'A', 'AAA', 1, '电子元件供应商'),
(2, 'SUP002', '东莞精密塑胶制品厂', '李厂长', '13900000002', 'jingmi@example.com', '东莞市长安镇乌沙社区', '广东', '东莞', 'A', 'AA', 1, '塑胶外壳供应商'),
(3, 'SUP003', '上海芯片技术有限公司', '赵总', '13900000003', 'chip@example.com', '上海市浦东新区张江高科技园区', '上海', '上海', 'A', 'AAA', 1, '芯片供应商'),
(4, 'SUP004', '苏州显示屏科技有限公司', '钱经理', '13900000004', 'display@example.com', '苏州市工业园区金鸡湖大道', '江苏', '苏州', 'A', 'AA', 1, '显示屏供应商'),
(5, 'SUP005', '惠州电池能源有限公司', '孙总', '13900000005', 'battery@example.com', '惠州市惠城区仲恺高新区', '广东', '惠州', 'A', 'AA', 1, '锂电池供应商'),
(6, 'SUP006', '厦门传感器制造厂', '周工', '13900000006', 'sensor@example.com', '厦门市集美区杏林工业区', '福建', '厦门', 'B', 'A', 1, '传感器供应商'),
(7, 'SUP007', '广州连接器科技有限公司', '吴总', '13900000007', 'connector@example.com', '广州市天河区科技园', '广东', '广州', 'B', 'A', 1, '连接器供应商'),
(8, 'SUP008', '宁波模具制造有限公司', '郑总', '13900000008', 'mold@example.com', '宁波市北仑区模具城', '浙江', '宁波', 'B', 'A', 1, '模具供应商'),
(9, 'SUP009', '温州五金制品厂', '王经理', '13900000009', 'hardware@example.com', '温州市乐清市柳市镇', '浙江', '温州', 'C', 'B', 1, '五金配件'),
(10, 'SUP010', '常州包装材料有限公司', '冯总', '13900000010', 'package@example.com', '常州市新北区工业园区', '江苏', '常州', 'C', 'B', 1, '包装材料'),
(11, 'SUP011', '青岛塑胶原料有限公司', '陈经理', '13900000011', 'plastic@example.com', '青岛市城阳区工业园', '山东', '青岛', 'C', 'B', 1, '塑胶原料'),
(12, 'SUP012', '天津电子元件有限公司', '褚经理', '13900000012', 'elec@example.com', '天津市滨海新区工业园', '天津', '天津', 'C', 'B', 1, '电子元件');

-- 4. 物料档案数据 (15条)
INSERT INTO `material_info` (`id`, `material_no`, `material_name`, `type`, `spec`, `unit`, `price`, `stock_qty`, `safety_stock`, `supplier_id`, `supplier_name`, `status`, `remark`) VALUES
(1, 'MAT001', '主控芯片STM32F4', '电子件', 'STM32F407ZGT6', '个', 45.00, 8500, 2000, 3, '上海芯片技术有限公司', 1, '核心元件'),
(2, 'MAT002', 'OLED显示屏0.96寸', '电子件', 'SSD1306/128x64', '个', 28.00, 3200, 1500, 4, '苏州显示屏科技有限公司', 1, '显示屏'),
(3, 'MAT003', '硅胶表带', '配件', '黑色/白色/彩色', '条', 5.50, 15000, 5000, 2, '东莞精密塑胶制品厂', 1, '表带'),
(4, 'MAT004', '锂电池3.7V 200mAh', '电子件', '103040/聚合物', '个', 12.00, 6800, 3000, 5, '惠州电池能源有限公司', 1, '电池'),
(5, 'MAT005', '心率传感器MAX30102', '电子件', '血氧心率二合一', '个', 18.00, 4200, 2000, 6, '厦门传感器制造厂', 1, '传感器'),
(6, 'MAT006', '蓝牙模块BLE5.0', '电子件', 'nRF52832', '个', 15.00, 5500, 2500, 1, '深圳鑫达电子有限公司', 1, '蓝牙'),
(7, 'MAT007', '六轴加速度计', '电子件', 'MPU6050', '个', 8.00, 3800, 2000, 1, '深圳鑫达电子有限公司', 1, '传感器'),
(8, 'MAT008', '塑胶外壳ABS', '结构件', '黑色/白色', '个', 3.20, 12000, 4000, 2, '东莞精密塑胶制品厂', 1, '外壳'),
(9, 'MAT009', '充电器Type-C', '配件', '5V/1A', '个', 6.00, 7500, 3000, 7, '广州连接器科技有限公司', 1, '充电器'),
(10, 'MAT010', '彩盒包装', '包材', '白卡纸/彩印', '个', 2.50, 9500, 5000, 10, '常州包装材料有限公司', 1, '包装'),
(11, 'MAT011', '防水圈硅胶', '结构件', '直径42mm', '个', 0.80, 850, 10000, 2, '东莞精密塑胶制品厂', 1, '防水件'),
(12, 'MAT012', 'GPS定位模组', '电子件', 'NEO-M8N', '个', 35.00, 1800, 1000, 1, '深圳鑫达电子有限公司', 1, 'GPS'),
(13, 'MAT013', '按键开关', '配件', '轻触开关6x6', '个', 0.30, 25000, 10000, 9, '温州五金制品厂', 1, '按键'),
(14, 'MAT014', 'PCB电路板', '结构件', 'FR4/双面板', '块', 8.50, 4500, 2000, 1, '深圳鑫达电子有限公司', 1, '电路板'),
(15, 'MAT015', '天线', '电子件', '陶瓷天线', '个', 3.00, 6000, 3000, 7, '广州连接器科技有限公司', 1, '天线');

-- 5. 订单数据 (20条)
INSERT INTO `order_customer` (`id`, `order_no`, `customer_id`, `customer_name`, `product_id`, `product_name`, `quantity`, `unit_price`, `total_amount`, `status`, `delivery_date`, `remark`) VALUES
(1, 'ORD20260701001', 1, '华为技术有限公司', 1, '智能手环X1 Pro', 2000, 499.00, 998000.00, 3, '2026-07-10', '紧急订单'),
(2, 'ORD20260702001', 2, '小米通讯技术有限公司', 2, '智能手环S2', 1500, 399.00, 598500.00, 2, '2026-07-18', ''),
(3, 'ORD20260703001', 3, 'OPPO广东移动通信有限公司', 1, '智能手环X1 Pro', 3000, 499.00, 1497000.00, 2, '2026-07-20', '大批量'),
(4, 'ORD20260704001', 4, 'vivo移动通信有限公司', 3, '智能手环Kids', 1000, 299.00, 299000.00, 1, '2026-07-25', ''),
(5, 'ORD20260705001', 5, '北京京东世纪贸易有限公司', 4, '智能手环Sport', 800, 599.00, 479200.00, 0, '2026-07-30', ''),
(6, 'ORD20260706001', 6, '苏宁易购集团股份有限公司', 2, '智能手环S2', 2500, 399.00, 997500.00, 1, '2026-07-22', ''),
(7, 'ORD20260707001', 7, '深圳大疆创新科技有限公司', 5, '智能手环Elder', 1200, 399.00, 478800.00, 2, '2026-07-19', ''),
(8, 'ORD20260708001', 8, '杭州阿里巴巴集团', 6, '智能手环Lite', 5000, 199.00, 995000.00, 0, '2026-08-05', '批量采购'),
(9, 'ORD20260709001', 1, '华为技术有限公司', 4, '智能手环Sport', 600, 599.00, 359400.00, 1, '2026-07-28', ''),
(10, 'ORD20260710001', 2, '小米通讯技术有限公司', 3, '智能手环Kids', 1800, 299.00, 538200.00, 2, '2026-07-17', ''),
(11, 'ORD20260711001', 3, 'OPPO广东移动通信有限公司', 6, '智能手环Lite', 3500, 199.00, 696500.00, 0, '2026-08-01', ''),
(12, 'ORD20260712001', 4, 'vivo移动通信有限公司', 1, '智能手环X1 Pro', 2200, 499.00, 1097800.00, 2, '2026-07-18', ''),
(13, 'ORD20260713001', 5, '北京京东世纪贸易有限公司', 5, '智能手环Elder', 900, 399.00, 359100.00, 1, '2026-07-26', ''),
(14, 'ORD20260714001', 6, '苏宁易购集团股份有限公司', 2, '智能手环S2', 1600, 399.00, 638400.00, 4, '2026-07-20', '已取消'),
(15, 'ORD20260715001', 7, '深圳大疆创新科技有限公司', 4, '智能手环Sport', 700, 599.00, 419300.00, 0, '2026-07-29', ''),
(16, 'ORD20260716001', 8, '杭州阿里巴巴集团', 7, '智能手表W1', 400, 899.00, 359600.00, 0, '2026-07-31', ''),
(17, 'ORD20260717001', 9, '腾讯科技(深圳)有限公司', 1, '智能手环X1 Pro', 500, 499.00, 249500.00, 1, '2026-07-24', ''),
(18, 'ORD20260718001', 10, '百度在线网络技术有限公司', 6, '智能手环Lite', 1000, 199.00, 199000.00, 0, '2026-08-02', ''),
(19, 'ORD20260719001', 11, '美团点评科技有限公司', 9, '智能耳机E1', 800, 299.00, 239200.00, 0, '2026-08-03', ''),
(20, 'ORD20260720001', 12, '顺丰速运集团有限公司', 11, '智能体脂秤S1', 600, 199.00, 119400.00, 0, '2026-08-04', '');

-- 6. 采购数据 (20条)
INSERT INTO `purchase_order` (`id`, `purchase_no`, `supplier_id`, `supplier_name`, `material_id`, `material_name`, `quantity`, `unit_price`, `total_amount`, `status`, `expected_date`, `remark`) VALUES
(1, 'PO20260701001', 1, '深圳鑫达电子有限公司', 1, '主控芯片STM32F4', 5000, 45.00, 225000.00, 3, '2026-07-05', ''),
(2, 'PO20260702001', 4, '苏州显示屏科技有限公司', 2, 'OLED显示屏0.96寸', 3000, 28.00, 84000.00, 2, '2026-07-10', ''),
(3, 'PO20260703001', 2, '东莞精密塑胶制品厂', 3, '硅胶表带', 8000, 5.50, 44000.00, 1, '2026-07-15', ''),
(4, 'PO20260704001', 5, '惠州电池能源有限公司', 4, '锂电池3.7V 200mAh', 4000, 12.00, 48000.00, 0, '2026-07-20', ''),
(5, 'PO20260705001', 3, '上海芯片技术有限公司', 5, '心率传感器MAX30102', 3500, 18.00, 63000.00, 2, '2026-07-12', ''),
(6, 'PO20260706001', 6, '厦门传感器制造厂', 6, '蓝牙模块BLE5.0', 2500, 15.00, 37500.00, 1, '2026-07-18', ''),
(7, 'PO20260707001', 1, '深圳鑫达电子有限公司', 7, '六轴加速度计', 3000, 8.00, 24000.00, 3, '2026-07-08', ''),
(8, 'PO20260708001', 2, '东莞精密塑胶制品厂', 8, '塑胶外壳ABS', 10000, 3.20, 32000.00, 2, '2026-07-14', ''),
(9, 'PO20260709001', 4, '苏州显示屏科技有限公司', 12, 'GPS定位模组', 1500, 35.00, 52500.00, 0, '2026-07-25', ''),
(10, 'PO20260710001', 5, '惠州电池能源有限公司', 11, '防水圈硅胶', 20000, 0.80, 16000.00, 1, '2026-07-22', ''),
(11, 'PO20260711001', 1, '深圳鑫达电子有限公司', 1, '主控芯片STM32F4', 3000, 45.00, 135000.00, 3, '2026-07-16', ''),
(12, 'PO20260711002', 4, '苏州显示屏科技有限公司', 2, 'OLED显示屏0.96寸', 2000, 28.00, 56000.00, 2, '2026-07-18', ''),
(13, 'PO20260712002', 2, '东莞精密塑胶制品厂', 3, '硅胶表带', 15000, 5.50, 82500.00, 1, '2026-07-20', ''),
(14, 'PO20260713002', 5, '惠州电池能源有限公司', 4, '锂电池3.7V 200mAh', 5000, 12.00, 60000.00, 0, '2026-07-25', ''),
(15, 'PO20260714002', 3, '上海芯片技术有限公司', 5, '心率传感器MAX30102', 3000, 18.00, 54000.00, 1, '2026-07-22', ''),
(16, 'PO20260715002', 6, '厦门传感器制造厂', 6, '蓝牙模块BLE5.0', 8000, 15.00, 120000.00, 0, '2026-07-28', ''),
(17, 'PO20260716002', 1, '深圳鑫达电子有限公司', 7, '六轴加速度计', 4000, 8.00, 32000.00, 1, '2026-07-23', ''),
(18, 'PO20260717002', 2, '东莞精密塑胶制品厂', 8, '塑胶外壳ABS', 10000, 3.20, 32000.00, 0, '2026-07-30', ''),
(19, 'PO20260718001', 3, '上海芯片技术有限公司', 1, '主控芯片STM32F4', 2000, 45.00, 90000.00, 0, '2026-07-25', ''),
(20, 'PO20260719001', 4, '苏州显示屏科技有限公司', 2, 'OLED显示屏0.96寸', 1500, 28.00, 42000.00, 0, '2026-07-28', '');

-- ============================================================
-- 数据插入完成！
-- ============================================================