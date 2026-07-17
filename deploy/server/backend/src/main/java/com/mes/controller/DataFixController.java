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

    /**
     * 修复 AI 知识库和问答历史表：字符集 + 补缺失列 + 重写中文数据
     */
    @PostMapping("/ai-tables")
    public Result<String> fixAiTables() {
        StringBuilder log = new StringBuilder();

        // 1. 修改表字符集为 utf8mb4
        String[] aiTables = {"ai_knowledge_document", "ai_chat_history"};
        for (String table : aiTables) {
            try {
                jdbcTemplate.execute("ALTER TABLE " + table + " CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                log.append(table).append(" 字符集已修改为 utf8mb4\n");
            } catch (Exception e) {
                log.append(table).append(" 字符集修改失败: ").append(e.getMessage()).append("\n");
            }
        }

        // 2. 给 ai_chat_history 补充缺失列（忽略已存在的列错误）
        String[] addColumns = {
                "ADD COLUMN session_id VARCHAR(50) COMMENT '会话ID'",
                "ADD COLUMN model VARCHAR(50) COMMENT 'AI模型'",
                "ADD COLUMN response_time INT DEFAULT 0 COMMENT '响应时间(毫秒)'",
                "ADD COLUMN feedback INT COMMENT '反馈(1好评/-1差评)'",
                "ADD COLUMN chat_time DATETIME COMMENT '对话时间'",
                "ADD COLUMN scene VARCHAR(50) COMMENT '场景'",
                "ADD COLUMN context TEXT COMMENT '上下文信息JSON'"
        };
        for (String col : addColumns) {
            try {
                jdbcTemplate.execute("ALTER TABLE ai_chat_history " + col);
                log.append("ai_chat_history ").append(col).append("\n");
            } catch (Exception e) {
                // 列已存在则忽略
                if (!e.getMessage().contains("Duplicate column")) {
                    log.append("ai_chat_history ").append(col).append(" 失败: ").append(e.getMessage()).append("\n");
                }
            }
        }

        // 3. 给 ai_chat_history 补充 chat_status 列（如果不存在）
        try {
            jdbcTemplate.execute("ALTER TABLE ai_chat_history ADD COLUMN chat_status INT DEFAULT 0 COMMENT '状态(0成功,1失败)'");
            log.append("ai_chat_history ADD COLUMN chat_status\n");
        } catch (Exception e) {
            if (!e.getMessage().contains("Duplicate column")) {
                log.append("ai_chat_history chat_status 失败: ").append(e.getMessage()).append("\n");
            }
        }

        // 4. 重写 ai_knowledge_document 中文数据
        String[][] docs = {
                {"1", "DOC001", "智能手环组装工艺规范", "PDF", "工艺规范", "/docs/assembly_process.pdf", "102400", "智能手环组装的标准工艺流程，包括PCB贴片、外壳组装、测试等环节", "组装,工艺,规范", "张工程师"},
                {"2", "DOC002", "SMT贴片操作手册", "PDF", "操作手册", "/docs/smt_manual.pdf", "204800", "SMT贴片机的操作步骤、参数设置和日常维护指南", "SMT,贴片,操作", "李工程师"},
                {"3", "DOC003", "质量检验标准文档", "WORD", "质量标准", "/docs/quality_standard.docx", "153600", "智能手环各工序的质量检验标准和判定准则", "质量,检验,标准", "王质检"},
                {"4", "DOC004", "设备维护保养手册", "PDF", "设备管理", "/docs/maintenance_manual.pdf", "256000", "生产设备的日常保养、定期维护和故障处理指南", "设备,维护,保养", "赵设备"},
                {"5", "DOC005", "物料识别与存储规范", "TEXT", "物料管理", "/docs/material_spec.txt", "51200", "原材料识别、存储条件和有效期管理规范", "物料,存储,规范", "钱仓管"},
                {"6", "DOC006", "安全生产操作规程", "PDF", "安全管理", "/docs/safety_rules.pdf", "786432", "车间安全生产操作规程和应急处理流程", "安全,生产,规程", "孙安全"},
                {"7", "DOC007", "包装作业指导书", "WORD", "作业指导", "/docs/packaging_guide.docx", "89600", "产品包装的标准作业流程和注意事项", "包装,作业,指导", "周包装"},
                {"8", "DOC008", "焊接工艺参数表", "EXCEL", "工艺参数", "/docs/welding_params.xlsx", "64000", "回流焊和波峰焊的工艺参数设置标准", "焊接,工艺,参数", "吴工艺"},
                {"9", "DOC009", "产品追溯管理规范", "PDF", "追溯管理", "/docs/traceability.pdf", "128000", "产品序列号管理和全流程追溯规范", "追溯,序列号,管理", "郑追溯"},
                {"10", "DOC010", "不良品处理流程", "WORD", "质量标准", "/docs/defect_process.docx", "76800", "不良品的识别、隔离、处置和分析流程", "不良品,处理,流程", "冯质量"}
        };
        for (String[] d : docs) {
            try {
                int rows = jdbcTemplate.update("UPDATE ai_knowledge_document SET doc_name=?, category=?, " +
                        "summary=?, keywords=?, uploader=? WHERE id=?",
                        d[2], d[4], d[7], d[8], d[9], d[0]);
                log.append("doc id=").append(d[0]).append(": ").append(rows).append(" 行更新\n");
            } catch (Exception e) {
                log.append("doc id=").append(d[0]).append(" 失败: ").append(e.getMessage()).append("\n");
            }
        }

        // 5. 插入 ai_chat_history 测试数据（如果表为空）
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_chat_history", Integer.class);
            if (count == null || count == 0) {
                String[][] chats = {
                        {"sess001", "1", "admin", "如何查看今日生产进度？", "请在Dashboard页面查看生产进度看板，包含工单完成率和实时产量。", "gpt-4o-mini", "1200", "1", "生产管理"},
                        {"sess001", "1", "admin", "设备故障如何上报？", "在设备故障管理页面点击新增按钮，填写故障信息后提交即可。", "gpt-4o-mini", "980", "1", "设备管理"},
                        {"sess002", "2", "order_clerk", "如何创建生产工单？", "在生产管理-工单管理页面点击新增，填写工单编号和产品信息。", "gpt-4o-mini", "1500", "1", "生产管理"},
                        {"sess002", "2", "order_clerk", "工单状态有哪些？", "工单状态包括：待开始、进行中、已暂停、已完成、已取消。", "gpt-4o-mini", "850", "1", "生产管理"},
                        {"sess003", "3", "quality_zhang", "质检标准在哪里查看？", "在知识库管理中搜索'质量检验标准文档'即可查看详细标准。", "gpt-4o-mini", "1100", "1", "质量管理"},
                        {"sess003", "3", "quality_zhang", "不合格品如何处置？", "在不合格品管理页面进行登记，选择处置方式（退货/返修/返工/报废）。", "gpt-4o-mini", "1300", "1", "质量管理"},
                        {"sess004", "4", "repair_zhang", "维修任务如何接单？", "在维修任务页面点击接单按钮，任务状态将变为维修中。", "gpt-4o-mini", "900", "1", "设备管理"},
                        {"sess004", "4", "repair_zhang", "设备保养周期是多久？", "请参考设备维护保养手册，一般设备每月保养一次。", "gpt-4o-mini", "1050", "1", "设备管理"},
                        {"sess005", "1", "admin", "库存预警如何设置？", "在库存预警页面可以设置安全库存，低于阈值时自动预警。", "gpt-4o-mini", "1150", "-1", "仓库管理"},
                        {"sess005", "1", "admin", "如何导出报表？", "在各统计页面点击导出按钮，支持Excel和PDF格式导出。", "gpt-4o-mini", "800", "1", "系统操作"}
                };
                for (String[] c : chats) {
                    try {
                        jdbcTemplate.update("INSERT INTO ai_chat_history (session_id, user_id, user_name, question, answer, " +
                                        "model, response_time, feedback, chat_status, scene, chat_time, create_time) " +
                                        "VALUES (?,?,?,?,?,?,?,?,?,?,NOW(),NOW())",
                                c[0], c[1], c[2], c[3], c[4], c[5], Integer.parseInt(c[6]), Integer.parseInt(c[7]), 0, c[8]);
                        log.append("chat 插入: ").append(c[0]).append("\n");
                    } catch (Exception e) {
                        log.append("chat 插入失败: ").append(e.getMessage()).append("\n");
                    }
                }
            } else {
                // 表已有数据但中文可能是问号，按 id 重写
                String[][] rewrite = {
                        {"1", "sess001", "admin", "如何查看今日生产进度？", "请在Dashboard页面查看生产进度看板，包含工单完成率和实时产量。", "gpt-4o-mini", "1200", "1", "生产管理"},
                        {"2", "sess001", "admin", "设备故障如何上报？", "在设备故障管理页面点击新增按钮，填写故障信息后提交即可。", "gpt-4o-mini", "980", "1", "设备管理"},
                        {"3", "sess002", "order_clerk", "如何创建生产工单？", "在生产管理-工单管理页面点击新增，填写工单编号和产品信息。", "gpt-4o-mini", "1500", "1", "生产管理"},
                        {"4", "sess002", "order_clerk", "工单状态有哪些？", "工单状态包括：待开始、进行中、已暂停、已完成、已取消。", "gpt-4o-mini", "850", "1", "生产管理"},
                        {"5", "sess003", "quality_zhang", "质检标准在哪里查看？", "在知识库管理中搜索质量检验标准文档即可查看详细标准。", "gpt-4o-mini", "1100", "1", "质量管理"},
                        {"6", "sess003", "quality_zhang", "不合格品如何处置？", "在不合格品管理页面进行登记，选择处置方式（退货/返修/返工/报废）。", "gpt-4o-mini", "1300", "1", "质量管理"},
                        {"7", "sess004", "repair_zhang", "维修任务如何接单？", "在维修任务页面点击接单按钮，任务状态将变为维修中。", "gpt-4o-mini", "900", "1", "设备管理"},
                        {"8", "sess004", "repair_zhang", "设备保养周期是多久？", "请参考设备维护保养手册，一般设备每月保养一次。", "gpt-4o-mini", "1050", "1", "设备管理"},
                        {"9", "sess005", "admin", "库存预警如何设置？", "在库存预警页面可以设置安全库存，低于阈值时自动预警。", "gpt-4o-mini", "1150", "-1", "仓库管理"},
                        {"10", "sess005", "admin", "如何导出报表？", "在各统计页面点击导出按钮，支持Excel和PDF格式导出。", "gpt-4o-mini", "800", "1", "系统操作"}
                };
                for (String[] c : rewrite) {
                    try {
                        jdbcTemplate.update("UPDATE ai_chat_history SET session_id=?, user_name=?, question=?, answer=?, " +
                                        "model=?, response_time=?, feedback=?, scene=? WHERE id=?",
                                c[1], c[2], c[3], c[4], c[5], Integer.parseInt(c[6]), Integer.parseInt(c[7]), c[8], c[0]);
                        log.append("chat id=").append(c[0]).append(" 重写完成\n");
                    } catch (Exception e) {
                        log.append("chat id=").append(c[0]).append(" 重写失败: ").append(e.getMessage()).append("\n");
                    }
                }
                log.append("ai_chat_history 已有 ").append(count).append(" 条数据，已重写中文\n");
            }
        } catch (Exception e) {
            log.append("ai_chat_history 数据检查失败: ").append(e.getMessage()).append("\n");
        }

        return Result.success(log.toString());
    }
}
