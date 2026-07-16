import { post, get } from '../utils/request'

// 扫码查物料信息（入库）
export function scanStockIn(data) {
  return post('/mini/stock/in/scan', data)
}

// 扫码查物料信息（出库）
export function scanStockOut(data) {
  return post('/mini/stock/out/scan', data)
}

// 确认入库（对接 mes_demo 数据库，一步完成入库+库存更新）
export function submitStockIn(data) {
  return post('/demo/stock-in/scan', data)
}

// 确认出库（对接 mes_demo 数据库）
export function submitStockOut(data) {
  return post('/demo/stock-out/save', data)
}

// 仓库列表（demo 库）
export function getWarehouseList() {
  return get('/mini/warehouse/list')
}

// 库存列表（demo 库）
export function getStockList(params) {
  return get('/demo/stock/list', params)
}

// 入库记录列表（demo 库）
export function getStockInList(params) {
  return get('/demo/stock-in/list', params)
}

// 出库记录列表（demo 库）
export function getStockOutList(params) {
  return get('/demo/stock-out/list', params)
}

// 库存预警（demo 库）
export function getStockWarning() {
  return get('/demo/stock-alert/list')
}

// 库存盘点（demo 库）
export function stockCheck(data) {
  return post('/demo/stock-check/save', data)
}

// 物料列表（主库）
export function getMaterialList(params) {
  return get('/material/list', params)
}
