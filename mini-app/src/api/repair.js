import { get, post, put } from '../utils/request'

export function getRepairList(params) {
  return get('/demo/repair-task/list', params)
}

export function getRepairDetail(id) {
  return get('/demo/repair-task/' + id)
}

export function createRepair(data) {
  return post('/demo/repair-task/save', data)
}

export function updateRepair(data) {
  return put('/demo/repair-task/update', data)
}

export function acceptRepair(id) {
  return post('/demo/repair-task/accept/' + id)
}

export function startRepair(id) {
  return post('/demo/repair-task/start/' + id)
}

export function finishRepair(id, data) {
  return post('/demo/repair-task/complete/' + id, data)
}

export function recheckRepair(id, data) {
  return post('/demo/repair-task/acceptance/' + id, data)
}

export function getRepairStatistics() {
  return get('/demo/repair-task/statistics')
}