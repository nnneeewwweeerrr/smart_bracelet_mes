import { post, get } from '../utils/request'

export function scanStockIn(data) {
  return post('/mini/stock/in/scan', data)
}

export function scanStockOut(data) {
  return post('/mini/stock/out/scan', data)
}

export function createStockIn(data) {
  return post('/stock/in', data)
}

export function createStockOut(data) {
  return post('/stock/out', data)
}

export function getStockList(params) {
  return get('/stock/list', params)
}

export function stockCheck(data) {
  return post('/stock/check', data)
}

export function getStockWarning() {
  return get('/stock/warning')
}

export function getWarehouseList() {
  return get('/stock/warehouse/list')
}

export function getMaterialList(params) {
  return get('/purchase/material/list', params)
}

export function getStockInList(params) {
  return get('/stock/in/list', params)
}

export function getStockOutList(params) {
  return get('/stock/out/list', params)
}
