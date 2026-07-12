import { getToken, getUser, logout } from './auth'

const BASE_URL = '/api'
const MOCK_MODE = true

const mockHandlers = {
  'POST /mini/login': (data) => ({
    token: 'mock_token_' + Date.now(),
    user: {
      id: 2,
      userName: data.userName || 'worker001',
      realName: data.userName === 'admin' ? '管理员' : '张三',
      roleId: data.userName === 'admin' ? 1 : 7
    }
  }),
  'GET /mini/worker/tasks': () => [
    { id: 1, workNo: 'WO202401001', description: '智能手环V2组装工序', status: 1, statusText: '进行中', statusType: 'primary' },
    { id: 2, workNo: 'WO202401002', description: '智能手环V2测试工序', status: 0, statusText: '待处理', statusType: 'info' },
    { id: 3, workNo: 'WO202401003', description: '智能手环V2包装工序', status: 2, statusText: '已完成', statusType: 'success' }
  ],
  'POST /mini/stock/in/scan': (data) => ({
    materialId: 1,
    materialCode: data.code || 'MAT001',
    materialName: 'OLED显示屏'
  }),
  'POST /mini/stock/out/scan': (data) => ({
    materialId: 1,
    materialCode: data.code || 'MAT001',
    materialName: 'OLED显示屏',
    currentStock: 200
  }),
  'GET /stock/warehouse/list': () => [
    { id: 1, warehouseName: '原材料仓', warehouseCode: 'WH001', address: '厂区A栋1楼' },
    { id: 2, warehouseName: '车间仓', warehouseCode: 'WH002', address: '厂区B栋2楼' },
    { id: 3, warehouseName: '成品仓', warehouseCode: 'WH003', address: '厂区C栋1楼' }
  ],
  'GET /stock/list': () => [
    { id: 1, materialId: 1, materialName: 'OLED显示屏', materialCode: 'MAT001', warehouseId: 1, warehouseName: '原材料仓', quantity: 200, location: 'A-01-01' },
    { id: 2, materialId: 2, materialName: '锂电池', materialCode: 'MAT002', warehouseId: 1, warehouseName: '原材料仓', quantity: 500, location: 'A-01-02' },
    { id: 3, materialId: 3, materialName: '塑胶外壳', materialCode: 'MAT003', warehouseId: 1, warehouseName: '原材料仓', quantity: 80, location: 'A-02-01' },
    { id: 4, materialId: 4, materialName: 'PCB主板', materialCode: 'MAT004', warehouseId: 2, warehouseName: '车间仓', quantity: 150, location: 'B-01-01' },
    { id: 5, materialId: 5, materialName: '蓝牙模块', materialCode: 'MAT005', warehouseId: 2, warehouseName: '车间仓', quantity: 300, location: 'B-01-02' }
  ],
  'GET /stock/warning': () => [
    { id: 1, materialId: 3, materialName: '塑胶外壳', materialCode: 'MAT003', quantity: 80, safetyStock: 500 },
    { id: 2, materialId: 4, materialName: 'PCB主板', materialCode: 'MAT004', quantity: 150, safetyStock: 100 }
  ],
  'POST /stock/in': () => ({ id: Date.now(), inNo: 'SI' + Date.now() }),
  'POST /stock/out': () => ({ id: Date.now(), outNo: 'SO' + Date.now() }),
  'POST /stock/check': () => ({ id: Date.now() }),
  'GET /purchase/material/list': (data) => {
    const all = [
      { id: 1, materialCode: 'MAT001', materialName: 'OLED显示屏', materialType: 'component', unit: '个', unitPrice: 35.00, safetyStock: 100 },
      { id: 2, materialCode: 'MAT002', materialName: '锂电池', materialType: 'component', unit: '个', unitPrice: 25.00, safetyStock: 200 },
      { id: 3, materialCode: 'MAT003', materialName: '塑胶外壳', materialType: 'component', unit: '个', unitPrice: 15.00, safetyStock: 500 },
      { id: 4, materialCode: 'MAT004', materialName: 'PCB主板', materialType: 'component', unit: '个', unitPrice: 45.00, safetyStock: 100 },
      { id: 5, materialCode: 'MAT005', materialName: '蓝牙模块', materialType: 'component', unit: '个', unitPrice: 18.00, safetyStock: 150 }
    ]
    let filtered = all
    if (data && data.materialType) filtered = filtered.filter(m => m.materialType === data.materialType)
    if (data && data.keyword) {
      const kw = data.keyword.toLowerCase()
      filtered = filtered.filter(m => m.materialCode.toLowerCase().includes(kw) || m.materialName.toLowerCase().includes(kw))
    }
    return { records: filtered, total: filtered.length }
  },
  'GET /stock/in/list': () => [
    { id: 1, inNo: 'SI202401001', materialId: 1, materialName: 'OLED显示屏', warehouseId: 1, warehouseName: '原材料仓', quantity: 100, sourceType: 'purchase', sourceNo: 'PO202401001', createTime: '2024-01-15 10:30:00' },
    { id: 2, inNo: 'SI202401002', materialId: 2, materialName: '锂电池', warehouseId: 1, warehouseName: '原材料仓', quantity: 200, sourceType: 'purchase', sourceNo: 'PO202401002', createTime: '2024-01-16 14:20:00' },
    { id: 3, inNo: 'SI202401003', materialId: 4, materialName: 'PCB主板', warehouseId: 2, warehouseName: '车间仓', quantity: 50, sourceType: 'production', sourceNo: 'WO202401001', createTime: '2024-01-17 09:15:00' }
  ],
  'GET /stock/out/list': () => [
    { id: 1, outNo: 'SO202401001', materialId: 1, materialName: 'OLED显示屏', warehouseId: 2, warehouseName: '车间仓', quantity: 30, targetType: 'production', targetNo: 'WO202401001', createTime: '2024-01-18 08:00:00' },
    { id: 2, outNo: 'SO202401002', materialId: 2, materialName: '锂电池', warehouseId: 2, warehouseName: '车间仓', quantity: 50, targetType: 'production', targetNo: 'WO202401002', createTime: '2024-01-18 10:30:00' }
  ],
  'GET /production/workorder/list': (data) => {
    const all = [
      { id: 1, workNo: 'WO202401001', planId: 1, planNo: 'PL202401001', productId: 1, productName: '智能运动手环A1', processId: 1, processName: 'SMT贴片', quantity: 500, status: 1, createTime: '2024-01-10 08:00:00' },
      { id: 2, workNo: 'WO202401002', planId: 1, planNo: 'PL202401001', productId: 1, productName: '智能运动手环A1', processId: 2, processName: '焊接组装', quantity: 500, status: 1, createTime: '2024-01-10 08:30:00' },
      { id: 3, workNo: 'WO202401003', planId: 1, planNo: 'PL202401001', productId: 1, productName: '智能运动手环A1', processId: 3, processName: '固件烧录', quantity: 500, status: 0, createTime: '2024-01-10 09:00:00' },
      { id: 4, workNo: 'WO202401004', planId: 2, planNo: 'PL202401002', productId: 2, productName: '健康监测手环H1', processId: 4, processName: '功能测试', quantity: 300, status: 2, createTime: '2024-01-11 10:00:00' },
      { id: 5, workNo: 'WO202401005', planId: 2, planNo: 'PL202401002', productId: 2, productName: '健康监测手环H1', processId: 5, processName: '外观检查', quantity: 300, status: 0, createTime: '2024-01-11 10:30:00' }
    ]
    let filtered = all
    if (data && data.status !== undefined && data.status !== '') filtered = filtered.filter(w => String(w.status) === String(data.status))
    return { records: filtered, total: filtered.length }
  },
  'GET /production/process/list': () => [
    { id: 1, processName: 'SMT贴片', processCode: 'PROC001', productId: 1, productName: '智能运动手环A1', sortOrder: 1 },
    { id: 2, processName: '焊接组装', processCode: 'PROC002', productId: 1, productName: '智能运动手环A1', sortOrder: 2 },
    { id: 3, processName: '固件烧录', processCode: 'PROC003', productId: 1, productName: '智能运动手环A1', sortOrder: 3 },
    { id: 4, processName: '功能测试', processCode: 'PROC004', productId: 1, productName: '智能运动手环A1', sortOrder: 4 },
    { id: 5, processName: '外观检查', processCode: 'PROC005', productId: 1, productName: '智能运动手环A1', sortOrder: 5 },
    { id: 6, processName: '包装入库', processCode: 'PROC006', productId: 1, productName: '智能运动手环A1', sortOrder: 6 }
  ],
  'POST /mini/production/report/scan': (data) => ({
    workOrderId: 1,
    workNo: data.code || 'WO202401001',
    processId: 1,
    processName: 'SMT贴片'
  }),
  'POST /production/report': () => ({ id: Date.now() }),
  'POST /production-report/save': () => ({ id: Date.now() }),
  'POST /production/report/verify-material': (data) => {
    const failCodes = ['MAT999', 'FAIL001']
    if (failCodes.includes(data.materialCode)) {
      return { pass: false, materialCode: data.materialCode, materialName: '未知物料', reason: '物料编码 ' + data.materialCode + ' 与当前工单BOM不匹配' }
    }
    const materialMap = {
      'MAT001': 'OLED显示屏',
      'MAT002': '锂电池',
      'MAT003': '塑胶外壳',
      'MAT004': 'PCB主板',
      'MAT005': '蓝牙模块'
    }
    const name = materialMap[data.materialCode]
    if (name) {
      return { pass: true, materialCode: data.materialCode, materialName: name }
    }
    return { pass: true, materialCode: data.materialCode, materialName: '物料' + data.materialCode }
  },
  'GET /production/workorder/:id': () => ({
    id: 1,
    workNo: 'WO202401001',
    planId: 1,
    planNo: 'PL202401001',
    productId: 1,
    productName: '智能运动手环A1',
    processId: 1,
    processName: 'SMT贴片',
    quantity: 500,
    completedQuantity: 320,
    status: 1,
    createTime: '2024-01-10 08:00:00',
    reports: [
      { id: 1, processName: 'SMT贴片', workerName: '张三', goodQuantity: 150, badQuantity: 2, reportDate: '2024-01-12 14:30:00' },
      { id: 2, processName: 'SMT贴片', workerName: '张三', goodQuantity: 168, badQuantity: 0, reportDate: '2024-01-13 10:20:00' }
    ]
  }),
  'GET /production/trace/reverse': (data) => {
    const snCode = data.snCode || data.workNo || ''
    if (!snCode) return []
    return {
      snCode: snCode.startsWith('SN') ? snCode : `SN${snCode}20240101001`,
      productModel: '智能运动手环A1 Pro',
      workOrderNo: 'WO20240101001',
      batchNo: 'BATCH20240101',
      productionDate: '2024-01-15 09:30:00',
      status: 'delivered',
      statusText: '已交付',
      materialList: [
        { id: 1, name: '主控芯片', code: 'IC-MCU-001', batch: 'IC20240105', supplier: '联发科科技', color: 'rgba(59, 124, 255, 0.15)' },
        { id: 2, name: '心率传感器', code: 'SENSOR-HR-001', batch: 'HR20240108', supplier: '汇顶科技', color: 'rgba(245, 108, 108, 0.15)' },
        { id: 3, name: '屏幕模组', code: 'LCD-001', batch: 'LCD20240110', supplier: '京东方', color: 'rgba(103, 194, 58, 0.15)' },
        { id: 4, name: '锂电池', code: 'BAT-380', batch: 'BAT20240112', supplier: '宁德时代', color: 'rgba(250, 173, 20, 0.15)' }
      ],
      productionHistory: [
        { processName: 'SMT贴片', lineName: 'A线-贴片机01', workerName: '张工', executeTime: '2024-01-15 09:30-10:15', status: 'pass', statusText: '已完成', statusTag: 'success' },
        { processName: '焊接组装', lineName: 'A线-焊接工位03', workerName: '李工', executeTime: '2024-01-15 10:30-11:20', status: 'pass', statusText: '已完成', statusTag: 'success' },
        { processName: '固件烧录', lineName: 'A线-烧录工位01', workerName: '王工', executeTime: '2024-01-15 13:00-13:30', status: 'pass', statusText: '已完成', statusTag: 'success' },
        { processName: '功能测试', lineName: 'A线-测试工位05', workerName: '赵工', executeTime: '2024-01-15 14:00-14:45', status: 'pass', statusText: '已完成', statusTag: 'success' },
        { processName: '外观检查', lineName: 'A线-质检工位02', workerName: '孙工', executeTime: '2024-01-15 15:00-15:15', status: 'pass', statusText: '已完成', statusTag: 'success' },
        { processName: '包装入库', lineName: '仓库-包装区', workerName: '周工', executeTime: '2024-01-15 15:30-15:45', status: 'pass', statusText: '已完成', statusTag: 'success' }
      ],
      qualityRecords: [
        { id: 1, inspectionType: 'IQC来料检验', detail: '芯片、传感器、屏幕、电池来料检验合格', inspector: '质检员-刘', inspectionTime: '2024-01-14 16:00', result: 'pass', resultText: '合格', resultTag: 'success' },
        { id: 2, inspectionType: '过程巡检', detail: '焊接工序焊点质量抽检合格', inspector: '巡检员-陈', inspectionTime: '2024-01-15 11:00', result: 'pass', resultText: '合格', resultTag: 'success' },
        { id: 3, inspectionType: '成品终检', detail: '功能测试全部通过，外观检查合格', inspector: '终检员-杨', inspectionTime: '2024-01-15 16:00', result: 'pass', resultText: '合格', resultTag: 'success' }
      ],
      testData: {
        heartRate: { value: '60-180bpm', pass: true },
        bluetooth: { value: '连接成功', pass: true },
        battery: { value: '380mAh', pass: true },
        waterproof: { value: 'IP68', pass: true }
      },
      deliveryInfo: {
        customerName: '京东商城',
        orderNo: 'JD20240120001',
        deliveryDate: '2024-01-20 10:00',
        trackingNo: 'SF1234567890'
      }
    }
  },
  'GET /production/trace/forward': (data) => {
    const batchNo = data.batchNo || ''
    if (!batchNo) return []
    return [
      { processName: '来料检验(IQC)', executeTime: '2024-01-08 10:00:00', result: 'pass', detail: '批次 ' + batchNo + ' IQC检验合格', relatedWorkNo: '' },
      { processName: '领料出库', executeTime: '2024-01-10 08:30:00', result: 'pass', detail: '批次 ' + batchNo + ' 出库至车间仓', relatedWorkNo: '' },
      { processName: 'SMT贴片', executeTime: '2024-01-12 14:00:00', result: 'pass', detail: '使用批次 ' + batchNo + ' 上料贴片', relatedWorkNo: 'WO202401001' },
      { processName: '焊接组装', executeTime: '2024-01-14 09:00:00', result: 'pass', detail: '使用批次 ' + batchNo + ' 焊接', relatedWorkNo: 'WO202401001' },
      { processName: '功能测试', executeTime: '2024-01-16 14:00:00', result: 'fail', detail: '批次 ' + batchNo + ' 中3件蓝牙信号弱', relatedWorkNo: 'WO202401001' }
    ]
  },
  'GET /production/sop/list': () => [
    { id: 1, processName: 'SMT贴片', processCode: 'PROC001', productName: '智能运动手环A1', sortOrder: 1 },
    { id: 2, processName: '焊接组装', processCode: 'PROC002', productName: '智能运动手环A1', sortOrder: 2 },
    { id: 3, processName: '固件烧录', processCode: 'PROC003', productName: '智能运动手环A1', sortOrder: 3 },
    { id: 4, processName: '功能测试', processCode: 'PROC004', productName: '智能运动手环A1', sortOrder: 4 },
    { id: 5, processName: '外观检查', processCode: 'PROC005', productName: '智能运动手环A1', sortOrder: 5 },
    { id: 6, processName: '包装入库', processCode: 'PROC006', productName: '智能运动手环A1', sortOrder: 6 }
  ],
  'GET /production/sop/:id': () => ({
    id: 1,
    processName: 'SMT贴片',
    processCode: 'PROC001',
    productName: '智能运动手环A1',
    steps: [
      { stepNo: 1, stepName: '来料确认', description: '核对物料编码与BOM一致性，确认物料数量', mediaType: 'image', mediaUrl: 'https://picsum.photos/seed/sop1/750/420' },
      { stepNo: 2, stepName: '锡膏印刷', description: '使用锡膏印刷机在PCB上均匀印刷锡膏', mediaType: 'image', mediaUrl: 'https://picsum.photos/seed/sop2/750/420' },
      { stepNo: 3, stepName: '贴片操作', description: '将元器件精准贴装到PCB焊盘上', mediaType: 'video', mediaUrl: 'https://www.w3schools.com/html/mov_bbb.mp4' },
      { stepNo: 4, stepName: '回流焊接', description: '将贴装好的PCB送入回流焊炉进行焊接', mediaType: 'image', mediaUrl: 'https://picsum.photos/seed/sop4/750/420' },
      { stepNo: 5, stepName: 'AOI检测', description: '使用AOI自动光学检测设备检查焊接质量', mediaType: 'image', mediaUrl: 'https://picsum.photos/seed/sop5/750/420' }
    ]
  }),
  'GET /quality/task/list': (data) => {
    const all = [
      { id: 1, taskNo: 'QT202401001', taskType: 'incoming', sourceId: 1, inspectorId: 2, inspectorName: '张三', status: 0, createTime: '2024-01-15 09:00:00' },
      { id: 2, taskNo: 'QT202401002', taskType: 'process', sourceId: 2, inspectorId: 2, inspectorName: '张三', status: 1, createTime: '2024-01-15 14:00:00' },
      { id: 3, taskNo: 'QT202401003', taskType: 'finished', sourceId: 3, inspectorId: 2, inspectorName: '张三', status: 2, createTime: '2024-01-16 10:00:00' },
      { id: 4, taskNo: 'QT202401004', taskType: 'incoming', sourceId: 4, inspectorId: 2, inspectorName: '张三', status: 0, createTime: '2024-01-17 08:30:00' }
    ]
    let filtered = all
    if (data && data.status !== undefined && data.status !== '') filtered = filtered.filter(t => String(t.status) === String(data.status))
    if (data && data.taskType) filtered = filtered.filter(t => t.taskType === data.taskType)
    return { records: filtered, total: filtered.length }
  },
  'POST /quality/result': () => ({ id: Date.now() }),
  'GET /quality/statistics': () => ({ total: 4, passed: 1, failed: 0, pending: 3 }),
  'GET /exception/list': (data) => {
    const all = [
      { id: 1, exceptionNo: 'EX202401001', exceptionType: 'equipment', workOrderId: 1, description: 'SMT贴片机运行异常，频繁报警停机', reporterId: 2, reporterName: '张三', status: 0, reportTime: '2024-01-18 08:30:00' },
      { id: 2, exceptionNo: 'EX202401002', exceptionType: 'material', workOrderId: 2, description: '锂电池库存不足，无法继续生产', reporterId: 2, reporterName: '张三', status: 1, reportTime: '2024-01-18 10:00:00' },
      { id: 3, exceptionNo: 'EX202401003', exceptionType: 'quality', workOrderId: 1, description: 'OLED显示屏来料不良率偏高，需复检', reporterId: 2, reporterName: '张三', status: 2, reportTime: '2024-01-17 15:00:00' },
      { id: 4, exceptionNo: 'EX202401004', exceptionType: 'other', workOrderId: 3, description: '车间温度过高，影响焊接质量', reporterId: 2, reporterName: '张三', status: 0, reportTime: '2024-01-19 09:00:00' }
    ]
    let filtered = all
    if (data && data.status !== undefined && data.status !== '') filtered = filtered.filter(e => String(e.status) === String(data.status))
    return { records: filtered, total: filtered.length }
  },
  'POST /exception': () => ({ id: Date.now(), exceptionNo: 'EX' + Date.now() }),
  'POST /exception/save': () => ({ id: Date.now(), exceptionNo: 'EX' + Date.now() }),
  'PUT /exception/:id/handle': () => ({ id: Date.now() }),
  'PUT /exception/handle/:id': () => ({ id: Date.now() }),
  'GET /mini/production/sop/scan': (data) => {
    const stationMap = {
      'STATION0001': { code: 'STATION0001', name: 'SMT贴片工位', process: 'SMT贴片', operator: '张三' },
      'STATION0002': { code: 'STATION0002', name: '焊接组装工位', process: '焊接组装', operator: '李四' },
      'STATION0003': { code: 'STATION0003', name: '固件烧录工位', process: '固件烧录', operator: '王五' },
      'STATION0004': { code: 'STATION0004', name: '功能测试工位', process: '功能测试', operator: '赵六' },
      'STATION0005': { code: 'STATION0005', name: '外观检查工位', process: '外观检查', operator: '孙七' },
      'STATION0006': { code: 'STATION0006', name: '包装入库工位', process: '包装入库', operator: '周八' }
    }
    return stationMap[data.code] || null
  },
  'GET /system/user/info': () => getUser(),
  'PUT /system/user/:id': () => ({ id: Date.now() }),
  'PUT /system/user/password': () => ({}),
  'GET /system/log/list': (data) => {
    const all = [
      { id: 1, operationModule: 'stock', operationType: '入库', operationContent: '扫码入库 OLED显示屏 x100', operationNo: 'SI202401001', success: true, operationTime: '2024-01-15 10:30:00' },
      { id: 2, operationModule: 'stock', operationType: '出库', operationContent: '扫码出库 OLED显示屏 x30', operationNo: 'SO202401001', success: true, operationTime: '2024-01-18 08:00:00' },
      { id: 3, operationModule: 'production', operationType: '报工', operationContent: '工单WO202401001 SMT贴片报工 合格80/不合格2', operationNo: 'WO202401001', success: true, operationTime: '2024-01-18 14:20:00' },
      { id: 4, operationModule: 'quality', operationType: '检验', operationContent: 'IQC来料检验 QT202401001 外观检查合格', operationNo: 'QT202401001', success: true, operationTime: '2024-01-17 09:00:00' },
      { id: 5, operationModule: 'exception', operationType: '上报', operationContent: '上报异常: SMT贴片机运行异常', operationNo: 'EX202401001', success: true, operationTime: '2024-01-18 08:30:00' },
      { id: 6, operationModule: 'stock', operationType: '盘点', operationContent: '库存盘点 塑胶外壳 实盘80 差异-420', operationNo: 'MAT003', success: true, operationTime: '2024-01-19 10:00:00' },
      { id: 7, operationModule: 'production', operationType: '报工', operationContent: '工单WO202401002 焊接组装报工 合格50/不合格1', operationNo: 'WO202401002', success: true, operationTime: '2024-01-19 11:30:00' },
      { id: 8, operationModule: 'system', operationType: '登录', operationContent: '用户worker001登录系统', operationNo: '-', success: true, operationTime: '2024-01-20 08:00:00' }
    ]
    let filtered = all
    if (data && data.module) filtered = filtered.filter(l => l.operationModule === data.module)
    return { records: filtered, total: filtered.length }
  },
  'GET /system/log/count': () => ({ total: 8, today: 2 }),
  'GET /system/user/performance': () => ({
    todayOutput: 150,
    departmentRank: 3,
    departmentTotal: 12,
    rankTrend: 1
  }),
  'GET /production/dashboard': () => ({
    todayOutput: 320,
    todayPassRate: 98.5,
    outputTrend: 12.5,
    rateTrend: 1.2,
    weekOutput: [280, 310, 295, 340, 320, 0, 0],
    weekPassRate: [97.2, 98.1, 97.8, 98.5, 98.5, 0, 0],
    weekLabels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    processOutput: [
      { processName: 'SMT贴片', output: 150, passRate: 98.7 },
      { processName: '焊接组装', output: 148, passRate: 100 },
      { processName: '固件烧录', output: 148, passRate: 100 },
      { processName: '功能测试', output: 145, passRate: 97.3 },
      { processName: '外观检查', output: 145, passRate: 100 },
      { processName: '包装入库', output: 0, passRate: 0 }
    ]
  }),
  'POST /ai/chat': (data) => {
    const q = (data && data.question) || ''
    const ql = q.toLowerCase()
    if (ql.indexOf('焊接') > -1 && ql.indexOf('温度') > -1) {
      return {
        answer: '智能手环焊接工序推荐温度参数如下：\n1. 回流焊峰值温度：240~250℃\n2. 预热区温度：150~180℃\n3. 焊接时间：30~60秒\n4. 冷却速率：≤4℃/秒\n\n注意事项：温度过高会导致元器件损坏，过低会出现虚焊。建议每班次首件检测焊接质量，如发现批量不良请立即进行异常上报。',
        references: ['SOP-焊接作业指导书', '回流焊工艺参数表', 'IPC-A-610标准']
      }
    }
    if (ql.indexOf('气泡') > -1) {
      return {
        answer: '屏幕贴合出现气泡的常见原因及处理方法：\n1. 清洁不彻底：使用无尘布+酒精重新清洁屏幕和贴合面\n2. 胶水涂抹不均：建议使用点胶机均匀涂布OCA光学胶\n3. 环境灰尘：在无尘车间（洁净度≥1000级）操作\n4. 贴合压力不足：调整贴合机压力至0.3~0.5MPa\n\n如气泡面积超过5%或无法消除，需按异常上报流程处理，并联系质检复检。',
        references: ['屏幕贴合SOP', 'OCA光学胶使用说明', '无尘车间管理规范']
      }
    }
    if (ql.indexOf('报工') > -1) {
      return {
        answer: '生产报工操作流程：\n1. 扫描工单条码进入报工页面\n2. 核对工单号、工序名称、产品信息\n3. 录入合格数量、不合格数量及不良原因\n4. 扫描所用物料批次码（系统自动校验BOM）\n5. 提交报工，系统自动更新工单进度\n\n报工需在工序完成后24小时内完成，逾期将影响产能统计。可通过「报工」快捷入口直接操作。',
        references: ['生产报工操作手册', '工单管理规范']
      }
    }
    if (ql.indexOf('防水') > -1) {
      return {
        answer: '防水测试标准与操作步骤：\n【测试标准】IP67级防水\n1. 将手环置于水深1米，静置30分钟\n2. 测试后功能正常，无进水现象\n\n【操作步骤】\n1. 检查外壳密封圈是否完好\n2. 放入防水测试仪，加压0.1MPa保压5分钟\n3. 观察压力表有无压降（压降≤0.005MPa为合格）\n4. 测试不合格需进行异常上报并隔离批次',
        references: ['防水测试SOP', 'IP防护等级标准', '气密性检测规范']
      }
    }
    if (ql.indexOf('工单') > -1 || /wo\d/i.test(ql)) {
      const match = q.match(/WO\d+/i)
      const wo = match ? match[0] : 'WO202401001'
      return {
        answer: '工单 ' + wo + ' 生产进度如下：\n• 产品：智能运动手环A1\n• 工序：SMT贴片\n• 计划数量：500件\n• 已完成：320件（64%）\n• 合格率：98.7%\n• 状态：进行中\n\n当前工序进度正常，预计今日可完成。如需报工请点击下方「去报工」按钮，发现异常请进行异常上报。',
        references: ['工单' + wo, '生产计划PL202401001', 'SMT贴片工序记录']
      }
    }
    if (ql.indexOf('sn') > -1 || ql.indexOf('追溯') > -1) {
      const match = q.match(/SN\d+/i)
      const sn = match ? match[0] : 'SN202607080001'
      return {
        answer: 'SN码 ' + sn + ' 生产追溯信息：\n1. 原材料入库：2024-01-08 OLED显示屏批次上料\n2. SMT贴片：2024-01-12 合格150/不合格2\n3. 焊接组装：2024-01-14 合格148/不合格0\n4. 功能测试：2024-01-16 合格145/不合格3（蓝牙信号弱）\n5. 外观检查：2024-01-17 合格145/不合格0\n\n该SN在功能测试环节存在蓝牙信号弱记录，建议复查射频性能。如确认异常请异常上报。',
        references: ['SN追溯记录', '功能测试报告', '蓝牙性能检测记录']
      }
    }
    if (ql.indexOf('物料') > -1 || ql.indexOf('库存') > -1 || /batch/i.test(ql)) {
      return {
        answer: '物料库存信息如下：\n• OLED显示屏（MAT001）：库存200件，安全库存100件，状态正常\n• 锂电池（MAT002）：库存500件，安全库存200件，状态正常\n• 塑胶外壳（MAT003）：库存80件，安全库存500件，⚠️库存不足\n• PCB主板（MAT004）：库存150件，安全库存100件，状态正常\n\n塑胶外壳库存低于安全线，建议尽快采购补货。可通过「库存查询」查看详细出入库记录。',
        references: ['库存台账', '物料安全库存表', '仓储管理系统']
      }
    }
    if (ql.indexOf('异常') > -1) {
      return {
        answer: '异常上报流程：\n1. 点击下方「去异常上报」按钮进入上报页面\n2. 选择异常类型（设备/物料/质量/其他）\n3. 关联工单号（可选）\n4. 详细描述异常现象\n5. 拍照上传现场图片\n6. 提交后系统自动通知相关责任人\n\n异常需在发现后30分钟内上报，紧急异常请同时电话通知车间主管。',
        references: ['异常管理规范', '应急响应流程']
      }
    }
    return {
      answer: '已收到您的问题："' + q + '"。\n\n我是智能手环MES生产助手，可为您提供以下帮助：\n• 生产工艺参数查询（焊接温度、防水测试等）\n• 工单进度与SN追溯查询\n• 报工操作指引\n• 库存信息查询\n• 异常上报流程指引\n\n请尝试提问更具体的问题，或点击上方快捷标签。',
      references: ['生产知识库', 'MES操作手册']
    }
  },
  'GET /ai/history/recent': () => [
    {
      role: 'user',
      content: '焊接工序的推荐温度是多少？'
    },
    {
      role: 'ai',
      content: '智能手环焊接工序推荐温度：回流焊峰值温度240~250℃，预热区150~180℃，焊接时间30~60秒。温度过高会导致元器件损坏，过低会出现虚焊。',
      references: ['SOP-焊接作业指导书', '回流焊工艺参数表']
    }
  ],
  'POST /ai/voice/recognize': (data) => ({
    text: '焊接温度的标准是多少',
    filePath: data && data.filePath
  })
}

function request(options) {
  if (MOCK_MODE) {
    const key = (options.method || 'GET') + ' ' + options.url
    let handler = mockHandlers[key]
    if (!handler) {
      for (const pattern of Object.keys(mockHandlers)) {
        const regex = new RegExp('^' + pattern.replace(/:\w+/g, '\\d+') + '$')
        if (regex.test(key)) {
          handler = mockHandlers[pattern]
          break
        }
      }
    }
    if (handler) {
      return new Promise(resolve => {
        setTimeout(() => resolve(handler(options.data)), 300)
      })
    }
  }

  return new Promise((resolve, reject) => {
    const token = getToken()
    const header = {
      'Content-Type': 'application/json',
      ...(options.header || {})
    }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success(res) {
        const { statusCode, data } = res
        if (statusCode === 200) {
          if (data.code === 200) {
            resolve(data.data)
          } else if (data.code === 401) {
            uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
            logout()
            reject(data)
          } else {
            uni.showToast({ title: data.message || '请求失败', icon: 'none' })
            reject(data)
          }
        } else {
          uni.showToast({ title: '网络请求失败', icon: 'none' })
          reject(res)
        }
      },
      fail(err) {
        uni.showToast({ title: '网络连接异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export function get(url, data) {
  return request({ url, method: 'GET', data })
}

export function post(url, data) {
  return request({ url, method: 'POST', data })
}

export function put(url, data) {
  return request({ url, method: 'PUT', data })
}

export function del(url, data) {
  return request({ url, method: 'DELETE', data })
}

export default request

