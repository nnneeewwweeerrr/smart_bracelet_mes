import { post, get } from '../utils/request'

export function scanProductionReport(data) {
  return post('/mini/production/report/scan', data)
}

export function getWorkOrderList(params) {
  return get('/production/workorder/list', params)
}

export function getWorkOrderDetail(id) {
  return get('/production/workorder/' + id)
}

export function productionReport(data) {
  return post('/production/report', data)
}

export function getProcessList(params) {
  return get('/production/process/list', params)
}

export function getProductionPlanProgress(id) {
  return get('/production/plan/' + id + '/progress')
}

export function getSopList(params) {
  return get('/production/sop/list', params)
}

export function getSopDetail(id) {
  return get('/production/sop/' + id)
}

export function getReverseTrace(workNo) {
  return get('/production/trace/reverse', { workNo })
}

export function getForwardTrace(batchNo) {
  return get('/production/trace/forward', { batchNo })
}

export function verifyMaterial(data) {
  return post('/production/report/verify-material', data)
}

export function getDashboard() {
  return get('/production/dashboard')
}
