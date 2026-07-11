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

export function getReverseTrace(workNo) {
  return get('/exception/batch-trace/' + workNo)
}

export function getForwardTrace(batchNo) {
  return get('/exception/batch-trace/' + batchNo)
}

export function verifyMaterial(data) {
  return post('/mini/production/report/scan', data)
}

export function getDashboard() {
  return get('/mini/production/dashboard')
}