package com.mes.controller;

import com.mes.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据修复接口：修复表字符集和中文乱码问题
 * 修复完成后建议删除或注释掉此接口
 */
@RestController
@RequestMapping("/api/fix")
public class DataFixController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 修复表字符集为 utf8mb4 并修复中文数据
     */
    @PostMapping("/encoding")
    public Result<String> fixEncoding() {
        StringBuilder log = new StringBuilder();

        // 1. 修改 smart_bracelet_mes 数据库中问题表的字符集
        String[] tablesToFix = {"unqualified_product", "exception_record"};
        for (String table : tablesToFix) {
            try {
                jdbcTemplate.execute("ALTER TABLE " + table + " CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                log.append(table).append(" 字符集已修改为 utf8mb4\n");
            } catch (Exception e) {
                log.append(table).append(" 字符集修改失败: ").append(e.getMessage()).append("\n");
            }
        }

        // 2. 修复 mes_demo 数据库中所有表的字符集
        String[] demoTables = {"demo_material", "demo_warehouse", "demo_stock", "demo_stock_in",
                "demo_stock_out", "demo_stock_check", "demo_stock_alert",
                "demo_equipment", "demo_equipment_fault", "demo_repair_task", "demo_equipment_maintain"};
        for (String table : demoTables) {
            try {
                jdbcTemplate.execute("ALTER TABLE mes_demo." + table + " CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                log.append("mes_demo.").append(table).append(" 字符集已修改\n");
            } catch (Exception e) {
                log.append("mes_demo.").append(table).append(" 修改失败: ").append(e.getMessage()).append("\n");
            }
        }

        // 3. 修复 mes_demo 中的 Mojibake 数据（UTF-8 字节被 Latin-1 读取）
        String[][] fixColumns = {
                {"demo_material", "material_name"},
                {"demo_material", "spec"},
                {"demo_material", "unit"},
                {"demo_warehouse", "warehouse_name"},
                {"demo_warehouse", "location"},
                {"demo_stock", "material_name"},
                {"demo_stock", "warehouse_name"},
                {"demo_stock_in", "material_name"},
                {"demo_stock_in", "warehouse_name"},
                {"demo_stock_in", "supplier_name"},
                {"demo_stock_in", "type"},
                {"demo_stock_out", "material_name"},
                {"demo_stock_out", "warehouse_name"},
                {"demo_stock_out", "type"},
                {"demo_stock_check", "warehouse_name"},
                {"demo_stock_alert", "material_name"},
                {"demo_stock_alert", "warehouse_name"},
                {"demo_stock_alert", "alert_type"},
                {"demo_equipment", "equipment_name"},
                {"demo_equipment", "model"},
                {"demo_equipment", "manufacturer"},
                {"demo_equipment_fault", "equipment_name"},
                {"demo_equipment_fault", "fault_desc"},
                {"demo_equipment_fault", "handler"},
                {"demo_repair_task", "equipment_name"},
                {"demo_repair_task", "fault_desc"},
                {"demo_repair_task", "repairer"},
                {"demo_equipment_maintain", "equipment_name"},
                {"demo_equipment_maintain", "maintain_content"},
                {"demo_equipment_maintain", "maintainer"}
        };
        for (String[] col : fixColumns) {
            try {
                int rows = jdbcTemplate.update("UPDATE mes_demo." + col[0] + " SET " + col[1] +
                        " = CONVERT(CONVERT(" + col[1] + " USING binary) USING utf8mb4) WHERE " +
                        col[1] + " LIKE '%Ã%' OR " + col[1] + " LIKE '%Â%'");
                if (rows > 0) {
                    log.append("mes_demo.").append(col[0]).append(".").append(col[1])
                            .append(" 修复 ").append(rows).append(" 行 Mojibake\n");
                }
            } catch (Exception e) {
                log.append("mes_demo.").append(col[0]).append(".").append(col[1])
                        .append(" 修复失败: ").append(e.getMessage()).append("\n");
            }
        }

        return Result.success(log.toString());
    }

    /**
     * 修复 unqualified_product 表的中文数据（数据已损坏为问号，需重新写入）
     */
    @PostMapping("/unqualified-data")
    public Result<String> fixUnqualifiedData() {
        StringBuilder log = new StringBuilder();
        String[][] updates = {
                {"1", "来料检验", "OLED屏幕", "外观缺陷", "屏幕表面有划痕", "质检员张三", "退货", "已退回供应商", "供应商", "加强来料检验", "已处理"},
                {"2", "过程检验", "蓝牙芯片", "焊接不良", "芯片引脚虚焊", "质检员李四", "返修", "已返修合格", "SMT车间", "调整回流焊参数", "已处理"},
                {"3", "成品检验", "塑胶外壳", "外观缺陷", "外壳表面有划伤", "质检员王五", "返工", "已返工合格", "组装车间", "优化传送带清洁", "已处理"},
                {"4", "来料检验", "塑胶外壳", "尺寸偏差", "外壳尺寸超差", "质检员张三", "退货", "已退回供应商", "供应商", "加强尺寸检验", "已处理"},
                {"5", "过程检验", "心率传感器", "焊接不良", "传感器引脚虚焊", "质检员李四", "返修", "已返修合格", "组装车间", "培训操作员", "已处理"},
                {"6", "客户退货", "蓝牙芯片", "功能不良", "蓝牙连接不稳定", "质检员张三", "报废", "已报废处理", "SMT车间", "分析根本原因", "客户退货"},
                {"7", "来料检验", "六轴传感器", "包装破损", "物料包装破损", "质检员李四", "退货", "已退回供应商", "供应商", "加强包装检验", "已处理"},
                {"8", "过程检验", "包装盒", "印刷不良", "包装盒印刷模糊", "质检员王五", "返工", "已返工合格", "包装车间", "更换印刷模板", "已处理"},
                {"9", "成品检验", "USB充电线", "功能不良", "充电线接触不良", "质检员张三", "报废", "已报废处理", "组装车间", "更换供应商", "已处理"},
                {"10", "过程检验", "心率传感器", "焊接不良", "传感器焊接不良", "质检员李四", "", "", "", "", "待处置"}
        };
        for (String[] u : updates) {
            try {
                int rows = jdbcTemplate.update("UPDATE unqualified_product SET source_type=?, material_name=?, " +
                        "defect_type=?, defect_desc=?, discoverer=?, disposition=?, disposition_result=?, " +
                        "responsible=?, rectify_action=?, remark=? WHERE id=?",
                        u[1], u[2], u[3], u[4], u[5], u[6], u[7], u[8], u[9], u[10], u[0]);
                log.append("unqualified id=").append(u[0]).append(": ").append(rows).append(" 行更新\n");
            } catch (Exception e) {
                log.append("unqualified id=").append(u[0]).append(" 失败: ").append(e.getMessage()).append("\n");
            }
        }
        return Result.success(log.toString());
    }

    /**
     * 修复 exception_record 表的中文数据（数据已损坏为问号，需重新写入）
     */
    @PostMapping("/exception-data")
    public Result<String> fixExceptionData() {
        StringBuilder log = new StringBuilder();
        String[][] updates = {
                {"1", "质量异常", "紧急", "工单", "产品外观划伤", "第3批次组装成品发现外壳划伤，不良率5%", "张三", "李四", "隔离不良品，修复组装线传送带，更新作业指导", "已解决", "传送带表面有异物，导致外壳划伤", "每周清洁传送带", "已解决"},
                {"2", "设备异常", "重要", "工序", "回流焊炉温度异常", "回流焊炉第2区温度超出工艺范围正负10度", "王五", "张工程师", "重新校准炉温，更换温控模块", "已解决", "温控模块老化导致温度漂移", "每月校验温控模块", "已解决"},
                {"3", "物料异常", "重要", "工单", "物料短缺", "OLED屏幕库存不足，影响生产", "钱七", "张仓管", "紧急采购补货，调整生产顺序", "已缓解", "未及时更新安全库存", "完善库存预警机制", "已缓解"},
                {"4", "质量异常", "一般", "质检", "心率传感器不良", "心率传感器焊接不良率3%", "赵六", "李工程师", "调整回流焊参数，重新培训操作员", "已解决", "回流焊温度偏高导致虚焊", "优化工艺参数", "已解决"},
                {"5", "工艺异常", "一般", "工序", "注塑件尺寸偏差", "注塑件长度尺寸超出公差范围", "孙八", "王工程师", "调整模具温度和保压时间", "已解决", "模具温度不稳定", "增加模具温控设备", "已解决"},
                {"6", "设备异常", "一般", "工序", "激光打标偏移", "激光打标位置偏移0.3mm", "周九", "钱工程师", "调整振镜校准", "已解决", "振镜松动", "每日检查振镜固定", "已解决"},
                {"7", "安全异常", "紧急", "其他", "车间漏电", "SMT车间发现漏电情况", "张工", "安全员", "断电检修，更换破损线缆", "已解决", "线缆老化破损", "每周安全检查", "已关闭"},
                {"8", "物料异常", "重要", "库存", "塑胶外壳色差", "新到塑胶外壳与样品存在色差", "李仓管", "采购部", "与供应商沟通补料", "已解决", "供应商原材料批次差异", "加强来料检验", "已解决"},
                {"9", "质量异常", "一般", "工单", "包装盒破损", "成品包装时发现包装盒破损率2%", "郑十", "冯十一", "更换包装盒供应商批次", "已解决", "包装盒材料强度不足", "更换供应商", "已解决"},
                {"10", "设备异常", "紧急", "工序", "气密性测试仪故障", "气密性测试仪压力异常，多个产品测试结果异常", "操作工小李", "周工程师", "更换压力传感器", "已解决", "压力传感器损坏", "每月校验测试设备", "已关闭"},
                {"11", "工艺异常", "一般", "工序", "CNC加工精度偏差", "CNC加工件尺寸超差", "赵六", "赵工程师", "重新对刀，补偿磨损", "处理中", "刀具磨损", "定期更换刀具", "处理中"},
                {"12", "质量异常", "一般", "工单", "充电不良", "新批次USB充电线充电不良率1%", "钱七", "", "", "", "分析中", "加强来料检验", "待处理"}
        };
        for (String[] u : updates) {
            try {
                int rows = jdbcTemplate.update("UPDATE exception_record SET exception_type=?, exception_level=?, " +
                        "source_type=?, title=?, description=?, reporter=?, handler=?, handle_content=?, " +
                        "handle_result=?, cause_analysis=?, preventive_action=?, remark=? WHERE id=?",
                        u[1], u[2], u[3], u[4], u[5], u[6], u[7], u[8], u[9], u[10], u[11], u[12], u[0]);
                log.append("exception id=").append(u[0]).append(": ").append(rows).append(" 行更新\n");
            } catch (Exception e) {
                log.append("exception id=").append(u[0]).append(" 失败: ").append(e.getMessage()).append("\n");
            }
        }
        return Result.success(log.toString());
    }
}
