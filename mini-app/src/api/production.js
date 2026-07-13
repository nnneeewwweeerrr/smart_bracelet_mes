import { post, get } from '../utils/request'

export function scanProductionReport(data) {
  return post('/mini/production/report/scan', data)
}

export function getWorkOrderList(params) {
  return get('/work-order/list', params)
}

export function getWorkOrderDetail(id) {
  return get('/work-order/' + id)
}

export function productionReport(data) {
  return post('/production-report/save', data)
}

export function getProcessList(params) {
  return get('/process/list', params)
}

export function getProductionPlanProgress(id) {
  return get('/production-plan/' + id)
}

export function getSopList(params) {
  return get('/mini/production/sop/list', params)
}

export function getSopDetail(id) {
  return get('/mini/production/sop/' + id)
}

export function getSopByWorkOrder(workOrderId) {
  return get('/mini/production/sop/work-order/' + workOrderId)
}

export function getReverseTrace(workNo) {
  return get('/exception/batch-trace/' + workNo)
}

export function getForwardTrace(batchNo) {
  return get('/exception/batch-trace/' + batchNo)
}

export function verifyMaterial(data) {
  return post('/mini/production/verify/material', data)
}

export function verifyParams(data) {
  return post('/mini/production/verify/params', data)
}

export function getDashboard() {
  return get('/mini/production/dashboard')
}

// SN全生命周期追溯
export function getSnReverseTrace(snCode) {
  return get('/sn-trace/reverse/' + snCode)
}

export function getSnForwardTrace(batchNo) {
  return get('/sn-trace/forward/' + batchNo)
}
