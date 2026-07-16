export function scanCode() {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode', 'barCode'],
      success(res) {
        console.log('扫码结果:', res)
        resolve(res.result)
      },
      fail(err) {
        console.error('扫码失败:', err)
        if (err.errMsg && err.errMsg.indexOf('cancel') === -1) {
          uni.showToast({ title: '扫码失败，请重试', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

export function parseScanResult(result) {
  if (!result) return null

  const trimmed = result.trim()

  try {
    const url = new URL(trimmed)
    const type = url.pathname.replace(/^\//, '')
    const params = Object.fromEntries(url.searchParams)
    return { type, params, raw: trimmed }
  } catch {
  }

  let code = trimmed

  if (code.startsWith('SN_SN')) {
    code = code.substring(3)
    return { type: 'sn_code', params: { snCode: code }, raw: trimmed }
  }

  if (code.startsWith('WO_WO')) {
    code = code.substring(3)
    return { type: 'work_order', params: { workOrderNo: code }, raw: trimmed }
  }

  if (code.startsWith('MTRL_')) {
    code = code.substring(5)
    return { type: 'material', params: { materialCode: code }, raw: trimmed }
  }

  if (code.startsWith('BATCH_BATCH')) {
    code = code.substring(6)
    return { type: 'batch', params: { batchNo: code }, raw: trimmed }
  }

  if (code.startsWith('BATCH_B')) {
    code = code.substring(6)
    return { type: 'batch', params: { batchNo: 'B' + code }, raw: trimmed }
  }

  if (code.startsWith('RP_RP')) {
    code = code.substring(3)
    return { type: 'repair', params: { repairNo: code }, raw: trimmed }
  }

  if (code.startsWith('STATION_')) {
    code = code.substring(8)
    return { type: 'station', params: { stationCode: 'STATION' + code }, raw: trimmed }
  }

  if (code.startsWith('QCTASK_')) {
    code = code.substring(7)
    return { type: 'qc_task', params: { taskNo: code }, raw: trimmed }
  }

  if (/^SN\d{8,15}$/.test(trimmed)) {
    return { type: 'sn_code', params: { snCode: trimmed }, raw: trimmed }
  }

  if (/^WO\d{8,15}$/.test(trimmed)) {
    return { type: 'work_order', params: { workOrderNo: trimmed }, raw: trimmed }
  }

  if (/^(IC|SENSOR|LCD|BAT|MTRL)-\w+-\d+$/.test(trimmed)) {
    return { type: 'material', params: { materialCode: trimmed }, raw: trimmed }
  }

  if (/^BATCH\d{6,10}$/.test(trimmed)) {
    return { type: 'batch', params: { batchNo: trimmed }, raw: trimmed }
  }

  if (/^RP\d{8,15}$/.test(trimmed)) {
    return { type: 'repair', params: { repairNo: trimmed }, raw: trimmed }
  }

  if (/^STATION\d{4,8}$/.test(trimmed)) {
    return { type: 'station', params: { stationCode: trimmed }, raw: trimmed }
  }

  if (/^STATION\w{4,8}$/.test(trimmed)) {
    return { type: 'station', params: { stationCode: trimmed }, raw: trimmed }
  }

  if (/^QCTASK\d{6,12}$/.test(trimmed)) {
    return { type: 'qc_task', params: { taskNo: trimmed }, raw: trimmed }
  }

  if (/^IC-\d{4,6}$/.test(trimmed)) {
    return { type: 'material', params: { materialCode: trimmed }, raw: trimmed }
  }

  if (/^BAT-\d{4,6}$/.test(trimmed)) {
    return { type: 'material', params: { materialCode: trimmed }, raw: trimmed }
  }

  return { type: 'text', params: { content: trimmed }, raw: trimmed }
}

export function getCodeTypeName(type) {
  const map = {
    'sn_code': 'SN码',
    'work_order': '工单',
    'material': '物料',
    'batch': '批次',
    'repair': '维修',
    'station': '工位',
    'stock/in': '入库',
    'stock/out': '出库',
    'production/report': '报工',
    'quality/check': '质检',
    'exception': '异常',
    'text': '未知'
  }
  return map[type] || '未知'
}

export function navigateByScanResult(scanResult) {
  const parsed = parseScanResult(scanResult)
  if (!parsed) return

  const routeMap = {
    'sn_code': '/pages/production/trace?snCode=' + encodeURIComponent(parsed.params.snCode),
    'work_order': '/pages/production/detail?workOrderId=' + encodeURIComponent(parsed.params.workOrderNo),
    'material': '/pages/stock/material?code=' + encodeURIComponent(parsed.params.materialCode),
    'batch': '/pages/production/trace?batchNo=' + encodeURIComponent(parsed.params.batchNo),
    'repair': '/pages/production/repair?repairNo=' + encodeURIComponent(parsed.params.repairNo),
    'station': '/pages/production/sop?stationCode=' + encodeURIComponent(parsed.params.stationCode),
    'stock/in': '/pages/stock/scan-in?code=' + encodeURIComponent(parsed.raw),
    'stock/out': '/pages/stock/scan-out?code=' + encodeURIComponent(parsed.raw),
    'production/report': '/pages/production/report?code=' + encodeURIComponent(parsed.raw),
    'quality/check': '/pages/quality/index?code=' + encodeURIComponent(parsed.raw),
    'quality/test': '/pages/quality/test-input?snCode=' + encodeURIComponent(parsed.params.snCode),
    'exception': '/pages/exception/create?code=' + encodeURIComponent(parsed.raw)
  }

  const targetUrl = routeMap[parsed.type]
  if (targetUrl) {
    uni.navigateTo({ url: targetUrl })
  } else {
    uni.showModal({
      title: '扫码结果',
      content: parsed.raw,
      showCancel: false
    })
  }
}