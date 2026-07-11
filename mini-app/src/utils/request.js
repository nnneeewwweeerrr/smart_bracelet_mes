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
    const workNo = data.workNo || ''
    if (!workNo) return []
    return [
      { processName: '原材料入库', executeTime: '2024-01-08 09:00:00', result: 'pass', detail: 'OLED显示屏 x500 入库至原材料仓', relatedWorkNo: '' },
      { processName: 'SMT贴片', executeTime: '2024-01-12 14:30:00', result: 'pass', detail: '合格150 / 不合格2', relatedWorkNo: 'WO202401001' },
      { processName: '焊接组装', executeTime: '2024-01-14 09:00:00', result: 'pass', detail: '合格148 / 不合格0', relatedWorkNo: 'WO202401001' },
      { processName: '固件烧录', executeTime: '2024-01-15 11:00:00', result: 'pass', detail: '合格148 / 不合格0', relatedWorkNo: 'WO202401001' },
      { processName: '功能测试', executeTime: '2024-01-16 14:00:00', result: 'fail', detail: '合格145 / 不合格3 (蓝牙信号弱)', relatedWorkNo: 'WO202401001' },
      { processName: '外观检查', executeTime: '2024-01-17 10:00:00', result: 'pass', detail: '合格145 / 不合格0', relatedWorkNo: 'WO202401001' },
      { processName: '包装入库', executeTime: '2024-01-18 16:00:00', result: 'running', detail: '进行中', relatedWorkNo: 'WO202401001' }
    ]
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
  'PUT /exception/:id/handle': () => ({ id: Date.now() }),
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
