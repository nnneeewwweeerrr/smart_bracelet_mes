package com.mes.kg.service.impl;

import com.mes.kg.entity.*;
import com.mes.kg.service.KnowledgeGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 知识图谱服务实现类
 *
 * 演示数据来自公开的智能手环/SMT制造行业资料，覆盖工序、不良、原因、方案、物料、设备六大节点类型。
 */
@Slf4j
@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    /** 节点存储 (id -> GraphNode) */
    private final Map<String, GraphNode> nodes = new ConcurrentHashMap<>();

    /** 关系存储 (id -> GraphRelationship) */
    private final Map<String, GraphRelationship> relationships = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("初始化智能手环生产知识图谱演示数据...");
        initGraphData();
        log.info("知识图谱初始化完成，节点数: {}, 关系数: {}", nodes.size(), relationships.size());
    }

    @Override
    public void initDemoData() {
        if (nodes.isEmpty()) {
            initGraphData();
        }
    }

    /**
     * 初始化智能手环生产知识图谱
     */
    private void initGraphData() {
        // ============================================================
        // 一、工序节点 (Process) - 智能手环典型生产工序
        // ============================================================
        addProcess("P001", "IQC来料检验", "IQC", 1,
                "依据GB/T 2828.1抽样标准，对PCB/IC/电池/表带等来料进行外观、尺寸、性能抽检");
        addProcess("P002", "SMT贴片", "SMT", 2,
                "采用SMT贴片机将电阻/电容/IC等贴装到PCB焊盘上，锡膏印刷→SPI检测→贴装→回流焊");
        addProcess("P003", "回流焊接", "REFLOW", 3,
                "无铅回流焊工艺，预热区150-180℃、浸润区240-250℃（峰值≤245℃），共10个温区");
        addProcess("P004", "AOI光学检测", "AOI", 4,
                "自动光学检测设备识别立碑、偏移、锡珠、虚焊等焊接缺陷，误判率<0.3%");
        addProcess("P005", "PCBA测试", "PCBA-TEST", 5,
                "ICT在线测试 + FCT功能测试，验证电路连通性、功耗、信号完整性");
        addProcess("P006", "固件烧录", "FW-BURN", 6,
                "通过SWD/JTAG接口烧录BootLoader+App+FS，使用产线批量烧录治具（每烧录10片校验一次）");
        addProcess("P007", "屏幕OCA真空贴合", "OCA-BOND", 7,
                "OCA光学胶真空贴合Display+TP，真空度≤-95kPa，贴合压力0.3-0.5MPa，温度23±2℃");
        addProcess("P008", "外壳超声波焊接", "ULTRASONIC", 8,
                "20kHz超声波焊接密封外壳，焊接时间0.5-1.5s，保压0.3s");
        addProcess("P009", "电池组装", "BATTERY", 9,
                "锂聚合物电池组装，含电池点焊、镍片激光焊接、BMS保护板连接");
        addProcess("P010", "防水测试", "IP68-TEST", 10,
                "气密性检测+浸水测试，IP68等级要求1.5米水深30分钟无进水");
        addProcess("P011", "PPG心率血氧校准", "PPG-CAL", 11,
                "光电容积脉搏波传感器校准，双波长LED（660nm红光+940nm红外），建立R值到SpO2映射");
        addProcess("P012", "振动跌落测试", "DROP-TEST", 12,
                "1.2米六面跌落+振动测试，模拟用户日常使用场景");
        addProcess("P013", "高低温老化", "AGING", 13,
                "-20℃~60℃循环老化测试48小时，ESD静电放电±8kV接触±15kV空气");
        addProcess("P014", "OQC出厂检验", "OQC", 14,
                "依据AQL 1.0抽样检验外观/功能/包装，合格品贴防伪码入库");
        addProcess("P015", "包装入库", "PACKING", 15,
                "彩盒+说明书+数据线+表带配件装配，扫码入库");

        // ============================================================
        // 二、不良现象节点 (Defect) - 智能手环典型不良
        // ============================================================
        addDefect("D001", "立碑", "Tombstone", "P002", "P003", 0.05f,
                "片式元件一端翘起立起，多见于0402以下小封装，IPC-A-610D 3级判定");
        addDefect("D002", "锡珠", "Solder Ball", "P003", null, 0.08f,
                "焊盘周围出现直径<0.1mm的锡球，易引发短路，按IPC-7095判定");
        addDefect("D003", "焊锡桥连", "Solder Bridge", "P003", null, 0.03f,
                "相邻焊点被焊锡桥接，造成短路");
        addDefect("D004", "BGA空洞", "BGA Void", "P003", null, 0.04f,
                "BGA焊球内部存在气泡，空洞率>25%判定为不良");
        addDefect("D005", "元件偏移", "Component Shift", "P002", null, 0.06f,
                "元件贴装位置超出焊盘±0.05mm公差范围");
        addDefect("D006", "虚焊", "Cold Solder", "P003", null, 0.10f,
                "焊点未完全熔合，呈现灰色粗糙表面，接触不良");
        addDefect("D007", "少锡", "Insufficient Solder", "P002", "P003", 0.04f,
                "焊点锡量不足，强度不达标");
        addDefect("D008", "屏幕气泡", "OCA Bubble", "P007", null, 0.07f,
                "OCA贴合后Display与TP之间存在气泡，φ>0.2mm判定不良");
        addDefect("D009", "屏幕白点", "Display White Spot", "P007", null, 0.03f,
                "OLED/LED屏出现亮度异常亮点，3颗及以上不良");
        addDefect("D010", "屏幕漏光", "Light Leakage", "P008", null, 0.02f,
                "密封不良导致屏幕边缘漏光");
        addDefect("D011", "防水失效", "IP68 Failure", "P010", null, 0.05f,
                "浸水测试出现水汽/水珠，IP68等级判定为不合格");
        addDefect("D012", "心率测量偏差", "PPG Inaccuracy", "P011", null, 0.08f,
                "与医疗级对比静息心率偏差>±5bpm，运动状态>±10bpm");
        addDefect("D013", "血氧测量偏差", "SpO2 Inaccuracy", "P011", null, 0.06f,
                "与医疗级对比血氧偏差>±3%，参考ISO 80601-2-61标准");
        addDefect("D014", "电池续航不足", "Battery Life", "P009", null, 0.04f,
                "标准使用场景续航<5天，低于标称值80%");
        addDefect("D015", "充电异常", "Charging Failure", "P009", null, 0.03f,
                "磁吸/触点充电失败、充电速度<标称值50%");
        addDefect("D016", "按键失灵", "Button Failure", "P008", null, 0.02f,
                "物理按键无响应或响应迟钝，按压寿命<10万次");
        addDefect("D017", "ESD静电击穿", "ESD Damage", "P013", null, 0.01f,
                "±8kV接触放电导致IC击穿失效，按IEC 61000-4-2");
        addDefect("D018", "表带断裂", "Strap Break", "P015", null, 0.02f,
                "硅胶/氟橡胶表带拉伸测试断裂，寿命<6个月");
        addDefect("D019", "死机重启", "System Crash", "P006", null, 0.05f,
                "固件Bug导致看门狗复位或HardFault");
        addDefect("D020", "外壳划伤", "Shell Scratch", "P008", null, 0.03f,
                "铝合金/PC外壳表面划伤，0.5mm以上判定不良");

        // ============================================================
        // 三、原因节点 (Cause) - 工艺、设备、材料、环境四大类
        // ============================================================
        addCause("C001", "回流焊温度偏低", "TEMP_LOW", "回流焊峰值温度<240℃，浸润时间不足");
        addCause("C002", "回流焊温度偏高", "TEMP_HIGH", "回流焊峰值温度>250℃，导致PCB/元件热损伤");
        addCause("C003", "锡膏印刷厚度不足", "PASTE_THIN", "锡膏厚度<0.10mm，导致焊锡量不足");
        addCause("C004", "锡膏印刷偏位", "PASTE_OFFSET", "印刷模板对位偏差>±0.05mm");
        addCause("C005", "贴片机吸嘴堵塞", "NOZZLE_CLOG", "吸嘴气路堵塞导致元件吸取失败或偏移");
        addCause("C006", "贴装压力过大", "PLACEMENT_FORCE", "贴装压力>3N，挤压锡膏导致桥连");
        addCause("C007", "预热升温速率过快", "RAMP_FAST", "预热斜率>3℃/s，溶剂气化不充分");
        addCause("C008", "氮气氧含量过高", "O2_HIGH", "回流焊氮气保护O2>1000ppm，氧化加剧");
        addCause("C009", "固化压力不足", "BOND_PRESSURE", "OCA贴合压力<0.3MPa，气泡未排净");
        addCause("C010", "固化温度偏差", "BOND_TEMP", "OCA贴合温度偏离23±2℃");
        addCause("C011", "真空度不足", "VACUUM_LOW", "OCA真空贴合真空度>-95kPa");
        addCause("C012", "超声波功率偏低", "US_POWER_LOW", "焊接功率低于额定值20%");
        addCause("C013", "超声波时间过长", "US_TIME_LONG", "焊接时间>1.5s导致外壳烧焦变形");
        addCause("C014", "激光焊接偏移", "LASER_OFFSET", "电池镍片激光焊接光斑位置偏移>0.1mm");
        addCause("C015", "回流焊炉温异常", "OVEN_FAULT", "热电偶故障导致炉温失控");
        addCause("C016", "SPI检测漏检", "SPI_MISS", "锡膏检测设备分辨率不足或校准漂移");
        addCause("C017", "AOI误判", "AOI_FALSE", "AOI光源衰减或算法阈值不当");
        addCause("C018", "贴片机精度漂移", "PICK_DRIFT", "X/Y轴伺服漂移导致贴装精度<±0.03mm");
        addCause("C019", "PPG校准模型偏差", "PPG_MODEL", "校准数据集不足或个体差异大");
        addCause("C020", "电池BMS参数错误", "BMS_PARAM", "保护板过充/过放阈值设置异常");
        addCause("C021", "锡膏活性不足", "PASTE_INACTIVE", "锡膏开封后暴露>4小时或冷藏失效");
        addCause("C022", "PCB焊盘氧化", "PAD_OXIDE", "PCB受潮或库存>6个月，焊盘氧化");
        addCause("C023", "元件引脚氧化", "LEAD_OXIDE", "元件受潮，引脚共面性<0.1mm");
        addCause("C024", "OCA胶污染", "OCA_DIRTY", "OCA胶面粘附灰尘/油污");
        addCause("C025", "电池电芯容量虚标", "CELL_FAKE", "电池电芯实际容量<标称值80%");
        addCause("C026", "硅胶表带硫化不充分", "RUBBER_CURE", "表带硫化时间/温度不足导致强度不达标");
        addCause("C027", "车间温湿度超标", "ENV_TEMP", "温度偏离23±5℃或湿度>60%RH");
        addCause("C028", "ESD防护缺失", "ESD_PROTECT", "操作员未佩戴静电环或接地不良");
        addCause("C029", "洁净度不足", "CLEAN_FAIL", "车间洁净度<10万级，灰尘影响贴合");

        // ============================================================
        // 四、解决方案节点 (Solution) - 工艺优化、设备校准、材料替代
        // ============================================================
        addSolution("S001", "调整回流焊温度曲线", "调高峰值温度至243±2℃，延长浸润时间30-60s", 0.9f);
        addSolution("S002", "更换锡膏并冷藏", "更换新批次锡膏，使用前回温4小时", 0.85f);
        addSolution("S003", "重新对位印刷模板", "使用SPI检测结果调整模板对位", 0.88f);
        addSolution("S004", "清洁/更换贴片吸嘴", "使用异丙醇清洁吸嘴或更换新吸嘴", 0.92f);
        addSolution("S005", "调整贴装压力", "贴装压力调至1.5-2.5N", 0.87f);
        addSolution("S006", "降低预热升温速率", "预热斜率调至1.5-2.0℃/s", 0.80f);
        addSolution("S007", "校准炉温", "使用炉温测试仪校准各温区温度，±3℃内", 0.93f);
        addSolution("S008", "增加氮气保护", "确保N2保护O2<500ppm", 0.78f);
        addSolution("S009", "返工OCA贴合", "揭除旧OCA、清洁、重新真空贴合", 0.85f);
        addSolution("S010", "调整贴合压力", "贴合压力调至0.4MPa", 0.88f);
        addSolution("S011", "校准真空度", "检查真空泵及管路密封性", 0.90f);
        addSolution("S012", "调整超声波参数", "功率+10%、时间缩短至1.0s", 0.86f);
        addSolution("S013", "重新激光焊接", "调整光斑位置，使用X-Ray检测焊点", 0.89f);
        addSolution("S014", "AOI阈值优化", "根据DPMO数据调整AOI算法阈值", 0.75f);
        addSolution("S015", "更换电池", "更换为合格电芯，重新配对BMS", 0.95f);
        addSolution("S016", "重写固件", "修复看门狗配置和中断优先级", 0.92f);
        addSolution("S017", "增加ESD防护", "操作员佩戴静电环、地面铺导电胶", 0.95f);
        addSolution("S018", "PPG算法优化", "增加个体差异补偿，扩充校准数据集", 0.83f);
        addSolution("S019", "返工超声波焊接", "使用专用治具返工，重新焊接", 0.80f);
        addSolution("S020", "更换表带", "更换为合格批次表带，追溯供应商", 0.90f);
        addSolution("S021", "外观返工/报废", "轻微划伤抛光处理，深度划伤报废", 0.65f);
        addSolution("S022", "返工/补焊", "烙铁/选择性波峰焊补焊虚焊点", 0.85f);
        addSolution("S023", "除湿/烘烤PCB", "125℃烘烤4小时除湿", 0.82f);
        addSolution("S024", "更换密封圈", "更换防水硅胶圈，重新压合", 0.91f);
        addSolution("S025", "QC专项抽检", "加严抽检比例至AQL 0.65", 0.70f);

        // ============================================================
        // 五、物料节点 (Material) - BOM关键物料
        // ============================================================
        addMaterial("M001", "PCB主板", "PCB-MAIN-V3.2", "FR-4 4层板，1oz铜厚，ENIG表面处理", 0.5f);
        addMaterial("M002", "主控芯片", "IC-BLE-SoC", "Nordic nRF52840 BLE SoC，QFN48封装", 0.3f);
        addMaterial("M003", "PPG传感器", "IC-PPG-SENSOR", "支持心率/血氧/HRV，I2C接口", 0.4f);
        addMaterial("M004", "OLED屏幕", "DISP-OLED-1.4", "1.4英寸AMOLED，454×454分辨率", 0.2f);
        addMaterial("M005", "锂聚合物电池", "BAT-LIPO-200", "200mAh锂聚合物，3.8V标称", 0.6f);
        addMaterial("M006", "触点充电模组", "PWR-CHARGE", "磁吸式Pogo Pin充电，5V/1A", 0.3f);
        addMaterial("M007", "硅胶表带", "STRAP-SILICONE", "食品级硅胶，22mm宽度，抗菌涂层", 0.5f);
        addMaterial("M008", "铝合金外壳", "SHELL-ALU", "6063铝合金阳极氧化处理，IP68级", 0.4f);
        addMaterial("M009", "无铅锡膏", "SOLDER-PASTE", "SAC305无铅锡膏，Type 4粉径", 0.3f);
        addMaterial("M010", "OCA光学胶", "OCA-FILM-50", "50μm厚OCA光学胶，透光率>92%", 0.4f);
        addMaterial("M011", "马达", "MOTOR-LRA", "线性马达LRA，φ10mm，3V驱动", 0.3f);
        addMaterial("M012", "六轴加速度计", "IC-IMU-6AXIS", "LSM6DSO六轴IMU，I2C/SPI接口", 0.3f);

        // ============================================================
        // 六、设备节点 (Equipment) - 关键生产设备
        // ============================================================
        addEquipment("E001", "SMT贴片机", "SMT-PICK-YSM20", "YAMAHA YSM20，贴装速度80000cph，精度±0.025mm");
        addEquipment("E002", "回流焊炉", "REFLOW-ERSA", "10温区无铅回流焊，氮气保护可选");
        addEquipment("E003", "AOI光学检测仪", "AOI-OMRON-VT", "OMRON VT系列，3D光学检测");
        addEquipment("E004", "X-Ray检测仪", "XRAY-NORDSON", "YXLON/Y.Cheetah 2D/3D X-Ray");
        addEquipment("E005", "真空贴合机", "OCA-BOND-LAM", "OCA真空热贴合机，温控±1℃");
        addEquipment("E006", "超声波焊接机", "US-WELD-DUKANE", "20kHz超声波焊机，3000W功率");
        addEquipment("E007", "激光焊机", "LASER-WELD", "光纤激光焊机，波长1070nm");
        addEquipment("E008", "IP防水测试仪", "IP-TEST-HY", "气密性+浸水测试，IPX7/IPX8");
        addEquipment("E009", "PPG测试治具", "PPG-FIXTURE", "心率血氧校准测试治具");
        addEquipment("E010", "振动测试台", "VIB-TABLE", "电磁振动台，5-2000Hz，30kN推力");
        addEquipment("E011", "高低温试验箱", "TEMP-CHAMBER", "-70℃~150℃温区，±0.5℃精度");
        addEquipment("E012", "ESD静电枪", "ESD-GUN", "±30kV可调静电放电枪");

        // ============================================================
        // 建立关系
        // ============================================================
        // 工序使用物料 (USES)
        addRel("R001", "P002", "M001", "USES", 0.95f, "SMT贴装到PCB主板");
        addRel("R002", "P002", "M002", "USES", 0.95f, "贴装主控芯片");
        addRel("R003", "P002", "M003", "USES", 0.90f, "贴装PPG传感器");
        addRel("R004", "P002", "M012", "USES", 0.90f, "贴装六轴加速度计");
        addRel("R005", "P003", "M009", "USES", 0.95f, "回流焊消耗无铅锡膏");
        addRel("R006", "P007", "M004", "USES", 0.95f, "OCA贴合OLED屏幕");
        addRel("R007", "P007", "M010", "USES", 0.95f, "消耗OCA光学胶");
        addRel("R008", "P008", "M008", "USES", 0.85f, "超声波焊接铝合金外壳");
        addRel("R009", "P009", "M005", "USES", 0.95f, "组装锂聚合物电池");
        addRel("R010", "P009", "M006", "USES", 0.90f, "连接触点充电模组");
        addRel("R011", "P009", "M011", "USES", 0.80f, "装配线性马达");
        addRel("R012", "P015", "M007", "USES", 0.90f, "包装硅胶表带");

        // 工序需要设备 (REQUIRES)
        addRel("R013", "P002", "E001", "REQUIRES", 0.95f, "SMT贴片机");
        addRel("R014", "P003", "E002", "REQUIRES", 0.95f, "回流焊炉");
        addRel("R015", "P004", "E003", "REQUIRES", 0.95f, "AOI检测仪");
        addRel("R016", "P004", "E004", "REQUIRES", 0.70f, "X-Ray辅助验证BGA");
        addRel("R017", "P007", "E005", "REQUIRES", 0.95f, "OCA真空贴合机");
        addRel("R018", "P008", "E006", "REQUIRES", 0.95f, "超声波焊接机");
        addRel("R019", "P009", "E007", "REQUIRES", 0.90f, "激光焊机焊接镍片");
        addRel("R020", "P010", "E008", "REQUIRES", 0.95f, "IP68防水测试仪");
        addRel("R021", "P011", "E009", "REQUIRES", 0.95f, "PPG测试治具");
        addRel("R022", "P012", "E010", "REQUIRES", 0.95f, "振动测试台");
        addRel("R023", "P013", "E011", "REQUIRES", 0.95f, "高低温试验箱");
        addRel("R024", "P013", "E012", "REQUIRES", 0.90f, "ESD静电测试枪");

        // 工序导致不良 (LEADS_TO)
        addRel("R030", "P002", "D001", "LEADS_TO", 0.40f, "贴装偏移导致立碑");
        addRel("R031", "P002", "D005", "LEADS_TO", 0.70f, "贴片机精度问题直接导致元件偏移");
        addRel("R032", "P002", "D007", "LEADS_TO", 0.50f, "锡膏印刷量不足导致少锡");
        addRel("R040", "P003", "D001", "LEADS_TO", 0.60f, "回流焊温度曲线不当引发立碑");
        addRel("R041", "P003", "D002", "LEADS_TO", 0.80f, "回流焊是锡珠的主要产生工序");
        addRel("R042", "P003", "D003", "LEADS_TO", 0.70f, "锡膏过多或回流不当导致桥连");
        addRel("R043", "P003", "D004", "LEADS_TO", 0.65f, "回流焊温度曲线影响BGA空洞率");
        addRel("R044", "P003", "D006", "LEADS_TO", 0.75f, "温度不足或时间不够导致虚焊");
        addRel("R045", "P003", "D007", "LEADS_TO", 0.55f, "回流焊温度曲线异常导致少锡");
        addRel("R060", "P006", "D019", "LEADS_TO", 0.50f, "固件Bug导致系统异常");
        addRel("R070", "P007", "D008", "LEADS_TO", 0.85f, "OCA贴合是屏幕气泡的直接工序");
        addRel("R071", "P007", "D009", "LEADS_TO", 0.40f, "贴合压力过大损伤OLED");
        addRel("R080", "P008", "D010", "LEADS_TO", 0.70f, "超声波焊接不严导致漏光");
        addRel("R081", "P008", "D016", "LEADS_TO", 0.40f, "焊接过程损伤按键");
        addRel("R082", "P008", "D020", "LEADS_TO", 0.60f, "焊接治具导致外壳划伤");
        addRel("R090", "P009", "D014", "LEADS_TO", 0.70f, "电池组装质量影响续航");
        addRel("R091", "P009", "D015", "LEADS_TO", 0.80f, "触点/镍片焊接不良导致充电异常");
        addRel("R100", "P010", "D011", "LEADS_TO", 0.30f, "防水测试发现不良");
        addRel("R110", "P011", "D012", "LEADS_TO", 0.50f, "PPG校准精度影响心率测量");
        addRel("R111", "P011", "D013", "LEADS_TO", 0.50f, "PPG校准精度影响血氧测量");
        addRel("R120", "P012", "D018", "LEADS_TO", 0.60f, "振动测试发现表带断裂");
        addRel("R130", "P013", "D017", "LEADS_TO", 0.40f, "ESD测试发现IC击穿");

        // 原因引起不良 (CAUSES)
        addRel("R200", "C001", "D001", "CAUSES", 0.50f, "温度偏低导致焊点熔合不充分引起立碑");
        addRel("R201", "C001", "D006", "CAUSES", 0.65f, "温度偏低直接导致虚焊");
        addRel("R202", "C002", "D002", "CAUSES", 0.40f, "温度偏高导致锡膏飞溅形成锡珠");
        addRel("R203", "C002", "D004", "CAUSES", 0.45f, "温度偏高加剧BGA空洞");
        addRel("R204", "C003", "D007", "CAUSES", 0.80f, "锡膏厚度不足直接导致少锡");
        addRel("R205", "C004", "D005", "CAUSES", 0.60f, "印刷偏位导致元件偏移");
        addRel("R206", "C005", "D001", "CAUSES", 0.45f, "吸嘴堵塞导致元件吸取失败引起立碑");
        addRel("R207", "C005", "D005", "CAUSES", 0.70f, "吸嘴问题直接导致元件偏移");
        addRel("R208", "C006", "D003", "CAUSES", 0.60f, "贴装压力过大导致锡膏挤压引起桥连");
        addRel("R209", "C007", "D002", "CAUSES", 0.50f, "预热过快导致溶剂爆沸形成锡珠");
        addRel("R210", "C008", "D006", "CAUSES", 0.30f, "氧化加剧导致虚焊");
        addRel("R211", "C009", "D008", "CAUSES", 0.80f, "贴合压力不足导致气泡未排净");
        addRel("R212", "C010", "D008", "CAUSES", 0.40f, "贴合温度偏差影响OCA流平");
        addRel("R213", "C011", "D008", "CAUSES", 0.75f, "真空度不足导致气泡");
        addRel("R214", "C012", "D010", "CAUSES", 0.60f, "超声波功率不足导致焊接不严引起漏光");
        addRel("R215", "C013", "D020", "CAUSES", 0.50f, "焊接时间过长导致外壳烧焦");
        addRel("R216", "C014", "D015", "CAUSES", 0.70f, "激光偏移导致镍片虚焊引起充电异常");
        addRel("R217", "C014", "D014", "CAUSES", 0.30f, "焊接不良导致内阻高影响续航");
        addRel("R220", "C015", "D001", "CAUSES", 0.40f, "炉温异常导致立碑");
        addRel("R221", "C015", "D006", "CAUSES", 0.50f, "炉温异常导致虚焊");
        addRel("R222", "C016", "D007", "CAUSES", 0.50f, "SPI漏检导致少锡流入下工序");
        addRel("R223", "C017", "D001", "CAUSES", 0.20f, "AOI误判导致不良漏出");
        addRel("R224", "C018", "D005", "CAUSES", 0.75f, "贴片机精度漂移直接导致元件偏移");
        addRel("R225", "C019", "D012", "CAUSES", 0.65f, "PPG校准模型偏差导致心率测量偏差");
        addRel("R226", "C019", "D013", "CAUSES", 0.65f, "PPG校准模型偏差导致血氧测量偏差");
        addRel("R227", "C020", "D014", "CAUSES", 0.50f, "BMS参数错误影响电池续航");
        addRel("R228", "C020", "D015", "CAUSES", 0.40f, "BMS参数错误导致充电异常");
        addRel("R230", "C021", "D002", "CAUSES", 0.55f, "锡膏活性不足导致锡珠");
        addRel("R231", "C021", "D006", "CAUSES", 0.60f, "锡膏活性不足导致虚焊");
        addRel("R232", "C022", "D006", "CAUSES", 0.60f, "焊盘氧化导致虚焊");
        addRel("R233", "C023", "D001", "CAUSES", 0.40f, "引脚氧化导致立碑");
        addRel("R234", "C024", "D008", "CAUSES", 0.70f, "OCA胶污染导致气泡");
        addRel("R235", "C024", "D009", "CAUSES", 0.30f, "OCA胶污染影响屏幕显示");
        addRel("R236", "C025", "D014", "CAUSES", 0.80f, "电芯容量虚标直接导致续航不足");
        addRel("R237", "C026", "D018", "CAUSES", 0.75f, "硫化不充分导致表带强度不足");
        addRel("R240", "C027", "D002", "CAUSES", 0.30f, "湿度过高导致锡珠");
        addRel("R241", "C027", "D008", "CAUSES", 0.30f, "温湿度影响OCA贴合质量");
        addRel("R242", "C028", "D017", "CAUSES", 0.80f, "ESD防护缺失直接导致IC击穿");
        addRel("R243", "C029", "D008", "CAUSES", 0.60f, "洁净度不足导致OCA气泡");
        addRel("R244", "C029", "D009", "CAUSES", 0.40f, "灰尘影响屏幕显示");

        // 方案解决问题 (TREATS)
        addRel("R300", "S001", "D001", "TREATS", 0.85f, "调高回流焊温度消除立碑");
        addRel("R301", "S001", "D006", "TREATS", 0.85f, "调高温度消除虚焊");
        addRel("R302", "S002", "D002", "TREATS", 0.80f, "更换新锡膏减少锡珠");
        addRel("R303", "S002", "D006", "TREATS", 0.80f, "新锡膏活性好消除虚焊");
        addRel("R304", "S003", "D005", "TREATS", 0.85f, "重新对位解决元件偏移");
        addRel("R305", "S004", "D001", "TREATS", 0.90f, "清洁吸嘴解决立碑");
        addRel("R306", "S004", "D005", "TREATS", 0.85f, "清洁吸嘴解决元件偏移");
        addRel("R307", "S005", "D003", "TREATS", 0.85f, "调整贴装压力消除桥连");
        addRel("R308", "S006", "D002", "TREATS", 0.75f, "降低升温速率减少锡珠");
        addRel("R309", "S007", "D001", "TREATS", 0.80f, "校准炉温解决立碑");
        addRel("R310", "S007", "D006", "TREATS", 0.80f, "校准炉温解决虚焊");
        addRel("R311", "S008", "D006", "TREATS", 0.70f, "氮气保护减少氧化引起的虚焊");
        addRel("R312", "S009", "D008", "TREATS", 0.90f, "重新OCA贴合消除气泡");
        addRel("R313", "S010", "D008", "TREATS", 0.80f, "调整贴合压力消除气泡");
        addRel("R314", "S011", "D008", "TREATS", 0.85f, "校准真空度消除气泡");
        addRel("R315", "S012", "D010", "TREATS", 0.85f, "调整超声波参数解决漏光");
        addRel("R316", "S013", "D015", "TREATS", 0.85f, "重新激光焊接解决充电异常");
        addRel("R317", "S014", "D001", "TREATS", 0.50f, "AOI阈值优化减少误判");
        addRel("R318", "S015", "D014", "TREATS", 0.95f, "更换电池直接解决续航不足");
        addRel("R319", "S015", "D015", "TREATS", 0.85f, "更换电池组解决充电异常");
        addRel("R320", "S016", "D019", "TREATS", 0.95f, "重写固件解决死机重启");
        addRel("R321", "S017", "D017", "TREATS", 0.95f, "ESD防护减少IC击穿");
        addRel("R322", "S018", "D012", "TREATS", 0.80f, "PPG算法优化提升心率精度");
        addRel("R323", "S018", "D013", "TREATS", 0.80f, "PPG算法优化提升血氧精度");
        addRel("R324", "S019", "D010", "TREATS", 0.75f, "超声波焊接返工解决漏光");
        addRel("R325", "S020", "D018", "TREATS", 0.90f, "更换合格表带");
        addRel("R326", "S021", "D020", "TREATS", 0.60f, "外观返工/报废处理划伤");
        addRel("R327", "S022", "D006", "TREATS", 0.85f, "烙铁补焊解决虚焊");
        addRel("R328", "S023", "D006", "TREATS", 0.75f, "PCB烘烤除湿减少虚焊");
        addRel("R329", "S024", "D011", "TREATS", 0.90f, "更换密封圈解决防水失效");
        addRel("R330", "S025", "D002", "TREATS", 0.50f, "加严抽检拦截锡珠");
        addRel("R331", "S025", "D003", "TREATS", 0.50f, "加严抽检拦截桥连");

        // 工序归属 (BELONGS_TO)
        for (int i = 1; i <= 15; i++) {
            String processId = String.format("P%03d", i);
            addRel("R400-" + i, processId, "P000-PRODUCT", "BELONGS_TO", 1.0f, "智能手环生产工艺流程");
        }
    }

    // ============================================================
    // 节点添加辅助方法
    // ============================================================
    private void addProcess(String id, String name, String code, int sortOrder, String description) {
        ProcessNode node = new ProcessNode();
        node.setId(id);
        node.setName(name);
        node.setCode(code);
        node.setSortOrder(sortOrder);
        node.setDescription(description);
        nodes.put(id, node);
    }

    private void addDefect(String id, String name, String code, String processId, String nextProcessId, float defectRate, String description) {
        DefectNode node = new DefectNode();
        node.setId(id);
        node.setName(name);
        node.setCode(code);
        node.setProcessId(processId);
        node.setNextProcessId(nextProcessId);
        node.setDefectRate(defectRate);
        node.setDescription(description);
        nodes.put(id, node);
    }

    private void addCause(String id, String name, String code, String description) {
        CauseNode node = new CauseNode();
        node.setId(id);
        node.setName(name);
        node.setCode(code);
        node.setDescription(description);
        nodes.put(id, node);
    }

    private void addSolution(String id, String name, String description, float effectiveness) {
        SolutionNode node = new SolutionNode();
        node.setId(id);
        node.setName(name);
        node.setDescription(description);
        node.setEffectiveness(effectiveness);
        nodes.put(id, node);
    }

    private void addMaterial(String id, String name, String code, String specification, float criticality) {
        MaterialNode node = new MaterialNode();
        node.setId(id);
        node.setName(name);
        node.setCode(code);
        node.setSpecification(specification);
        node.setCriticality(criticality);
        nodes.put(id, node);
    }

    private void addEquipment(String id, String name, String code, String specification) {
        EquipmentNode node = new EquipmentNode();
        node.setId(id);
        node.setName(name);
        node.setCode(code);
        node.setSpecification(specification);
        nodes.put(id, node);
    }

    private void addRel(String id, String sourceId, String targetId, String type, float weight, String description) {
        GraphRelationship rel = new GraphRelationship();
        rel.setId(id);
        rel.setSourceId(sourceId);
        rel.setTargetId(targetId);
        rel.setType(type);
        rel.setWeight(weight);
        rel.setDescription(description);
        relationships.put(id, rel);
    }

    // ============================================================
    // 业务方法实现 - 适配现有接口（使用 Map 返回）
    // ============================================================

    @Override
    public List<Map<String, Object>> findAllNodes() {
        return nodes.values().stream().map(this::nodeToMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> findAllRelationships() {
        return relationships.values().stream().map(this::relToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> queryDefectAnalysis(String defectName) {
        Map<String, Object> result = new HashMap<>();
        GraphNode defectNode = nodes.values().stream()
                .filter(n -> "defect".equals(n.getType()) && containsIgnoreCase(n.getName(), defectName))
                .findFirst().orElse(null);
        if (defectNode == null) {
            result.put("found", false);
            return result;
        }
        result.put("found", true);
        result.put("defect", nodeToMap(defectNode));

        // 原因
        List<Map<String, Object>> causes = relationships.values().stream()
                .filter(r -> r.getTargetId().equals(defectNode.getId()) && "CAUSES".equals(r.getType()))
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    GraphNode cause = nodes.get(r.getSourceId());
                    m.put("name", cause != null ? cause.getName() : "");
                    m.put("description", cause != null ? cause.getDescription() : "");
                    m.put("weight", r.getWeight());
                    m.put("relationDesc", r.getDescription());
                    return m;
                }).collect(Collectors.toList());
        result.put("causes", causes);

        // 方案
        List<Map<String, Object>> solutions = relationships.values().stream()
                .filter(r -> r.getTargetId().equals(defectNode.getId()) && "TREATS".equals(r.getType()))
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    GraphNode sol = nodes.get(r.getSourceId());
                    m.put("name", sol != null ? sol.getName() : "");
                    m.put("description", sol != null ? sol.getDescription() : "");
                    m.put("weight", r.getWeight());
                    m.put("relationDesc", r.getDescription());
                    return m;
                }).collect(Collectors.toList());
        result.put("solutions", solutions);

        // 工序
        if (defectNode instanceof DefectNode) {
            String processId = ((DefectNode) defectNode).getProcessId();
            if (processId != null) {
                GraphNode process = nodes.get(processId);
                result.put("process", process != null ? nodeToMap(process) : null);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> queryProcessDefects(String processCode) {
        GraphNode processNode = nodes.values().stream()
                .filter(n -> "process".equals(n.getType()) && containsIgnoreCase(n.getCode(), processCode))
                .findFirst().orElse(null);
        if (processNode == null) return new ArrayList<>();
        return relationships.values().stream()
                .filter(r -> r.getSourceId().equals(processNode.getId()) && "LEADS_TO".equals(r.getType()))
                .map(r -> nodes.get(r.getTargetId()))
                .filter(Objects::nonNull)
                .map(this::nodeToMap)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> queryNodeDetails(String nodeType, String nodeCode) {
        Map<String, Object> result = new HashMap<>();
        GraphNode node = nodes.values().stream()
                .filter(n -> n.getType().equalsIgnoreCase(nodeType) && containsIgnoreCase(n.getName(), nodeCode))
                .findFirst().orElse(null);
        if (node == null) return result;
        result.put("node", nodeToMap(node));
        List<Map<String, Object>> outgoing = relationships.values().stream()
                .filter(r -> r.getSourceId().equals(node.getId()))
                .map(this::relToMap).collect(Collectors.toList());
        List<Map<String, Object>> incoming = relationships.values().stream()
                .filter(r -> r.getTargetId().equals(node.getId()))
                .map(this::relToMap).collect(Collectors.toList());
        result.put("outgoing", outgoing);
        result.put("incoming", incoming);
        return result;
    }

    @Override
    public void createNode(Map<String, Object> nodeData) {
        // 演示用：动态创建节点，重启失效
        String type = (String) nodeData.getOrDefault("type", "custom");
        String id = (String) nodeData.getOrDefault("id", "N-" + System.currentTimeMillis());
        String name = (String) nodeData.getOrDefault("name", "未命名");
        GraphNode node = new GraphNode();
        node.setId(id);
        node.setType(type);
        node.setName(name);
        node.setDescription((String) nodeData.getOrDefault("description", ""));
        nodes.put(id, node);
    }

    @Override
    public void createRelationship(String sourceType, String sourceCode, String targetType, String targetCode, String relationType) {
        // 演示用：根据type/code查找节点id并建立关系
        GraphNode source = findByCode(sourceType, sourceCode);
        GraphNode target = findByCode(targetType, targetCode);
        if (source != null && target != null) {
            String id = "R-USER-" + System.currentTimeMillis();
            addRel(id, source.getId(), target.getId(), relationType, 0.7f, "用户动态创建");
        }
    }

    @Override
    public void deleteNode(String nodeType, String nodeCode) {
        GraphNode node = findByCode(nodeType, nodeCode);
        if (node != null) {
            nodes.remove(node.getId());
            // 同时移除关系
            relationships.entrySet().removeIf(e ->
                    e.getValue().getSourceId().equals(node.getId()) || e.getValue().getTargetId().equals(node.getId()));
        }
    }

    @Override
    public long countNodesByType(String nodeType) {
        return nodes.values().stream().filter(n -> n.getType().equalsIgnoreCase(nodeType)).count();
    }

    @Override
    public Map<String, Object> queryMultiPathAnalysis(String defectName) {
        // 实现多路径分析：返回所有可能的(原因, 方案)组合
        Map<String, Object> result = new HashMap<>();
        GraphNode defectNode = nodes.values().stream()
                .filter(n -> "defect".equals(n.getType()) && containsIgnoreCase(n.getName(), defectName))
                .findFirst().orElse(null);
        if (defectNode == null) {
            result.put("found", false);
            return result;
        }
        result.put("found", true);
        result.put("defect", nodeToMap(defectNode));

        List<GraphRelationship> causeRels = relationships.values().stream()
                .filter(r -> r.getTargetId().equals(defectNode.getId()) && "CAUSES".equals(r.getType()))
                .collect(Collectors.toList());
        List<GraphRelationship> solutionRels = relationships.values().stream()
                .filter(r -> r.getTargetId().equals(defectNode.getId()) && "TREATS".equals(r.getType()))
                .collect(Collectors.toList());

        List<Map<String, Object>> paths = new ArrayList<>();
        for (GraphRelationship cr : causeRels) {
            GraphNode cause = nodes.get(cr.getSourceId());
            if (cause == null) continue;
            for (GraphRelationship sr : solutionRels) {
                GraphNode sol = nodes.get(sr.getSourceId());
                if (sol == null) continue;
                Map<String, Object> path = new HashMap<>();
                path.put("causeName", cause.getName());
                path.put("causeDesc", cause.getDescription());
                path.put("solutionName", sol.getName());
                path.put("solutionDesc", sol.getDescription());
                double conf = cr.getWeight() * sr.getWeight() * 100;
                path.put("confidence", Math.round(conf * 10) / 10.0);
                paths.add(path);
            }
        }
        paths.sort((a, b) -> Double.compare((double) b.get("confidence"), (double) a.get("confidence")));
        result.put("paths", paths);
        return result;
    }

    @Override
    public List<Map<String, Object>> searchNodes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return new ArrayList<>();
        return nodes.values().stream()
                .filter(n -> containsIgnoreCase(n.getName(), keyword) || containsIgnoreCase(n.getDescription(), keyword))
                .limit(50)
                .map(this::nodeToMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> queryProcessMaterials(String processCode) {
        GraphNode processNode = nodes.values().stream()
                .filter(n -> "process".equals(n.getType()) && containsIgnoreCase(n.getCode(), processCode))
                .findFirst().orElse(null);
        if (processNode == null) return new ArrayList<>();
        return relationships.values().stream()
                .filter(r -> r.getSourceId().equals(processNode.getId()) && "USES".equals(r.getType()))
                .map(r -> nodes.get(r.getTargetId()))
                .filter(Objects::nonNull)
                .map(this::nodeToMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> queryProcessEquipments(String processCode) {
        GraphNode processNode = nodes.values().stream()
                .filter(n -> "process".equals(n.getType()) && containsIgnoreCase(n.getCode(), processCode))
                .findFirst().orElse(null);
        if (processNode == null) return new ArrayList<>();
        return relationships.values().stream()
                .filter(r -> r.getSourceId().equals(processNode.getId()) && "REQUIRES".equals(r.getType()))
                .map(r -> nodes.get(r.getTargetId()))
                .filter(Objects::nonNull)
                .map(this::nodeToMap)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getGraphData() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> nodeList = nodes.values().stream().map(this::nodeToMap).collect(Collectors.toList());
        List<Map<String, Object>> edgeList = relationships.values().stream().map(this::relToMap).collect(Collectors.toList());
        result.put("nodes", nodeList);
        result.put("edges", edgeList);
        result.put("stats", getGraphStats());
        return result;
    }

    private Map<String, Object> getGraphStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", nodes.size());
        stats.put("totalRelationships", relationships.size());
        Map<String, Long> typeCount = nodes.values().stream()
                .collect(Collectors.groupingBy(GraphNode::getType, Collectors.counting()));
        stats.put("nodesByType", typeCount);
        Map<String, Long> relTypeCount = relationships.values().stream()
                .collect(Collectors.groupingBy(GraphRelationship::getType, Collectors.counting()));
        stats.put("relationshipsByType", relTypeCount);
        return stats;
    }

    private Map<String, Object> nodeToMap(GraphNode node) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", node.getId());
        m.put("name", node.getName());
        m.put("type", node.getType());
        m.put("code", node.getCode());
        m.put("description", node.getDescription());
        // 扩展字段
        if (node instanceof DefectNode) {
            DefectNode d = (DefectNode) node;
            m.put("processId", d.getProcessId());
            m.put("defectRate", d.getDefectRate());
        } else if (node instanceof SolutionNode) {
            m.put("effectiveness", ((SolutionNode) node).getEffectiveness());
        } else if (node instanceof MaterialNode) {
            m.put("specification", ((MaterialNode) node).getSpecification());
            m.put("criticality", ((MaterialNode) node).getCriticality());
        } else if (node instanceof EquipmentNode) {
            m.put("specification", ((EquipmentNode) node).getSpecification());
        } else if (node instanceof ProcessNode) {
            m.put("sortOrder", ((ProcessNode) node).getSortOrder());
        }
        return m;
    }

    private Map<String, Object> relToMap(GraphRelationship rel) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", rel.getId());
        m.put("source", rel.getSourceId());
        m.put("target", rel.getTargetId());
        m.put("type", rel.getType());
        m.put("weight", rel.getWeight());
        m.put("description", rel.getDescription());
        return m;
    }

    private GraphNode findByCode(String type, String code) {
        return nodes.values().stream()
                .filter(n -> n.getType().equalsIgnoreCase(type) && containsIgnoreCase(n.getName(), code))
                .findFirst().orElse(null);
    }

    private boolean containsIgnoreCase(String source, String target) {
        if (source == null || target == null) return false;
        return source.toLowerCase().contains(target.toLowerCase());
    }
}
