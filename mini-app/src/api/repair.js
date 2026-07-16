import { get, post, put } from '../utils/request'

export function getRepairList(params) {
  return get('/repair/list', params)
}

export function getRepairDetail(id) {
  return get('/repair/' + id)
}

export function createRepair(data) {
  return post('/repair/save', data)
}

export function updateRepair(data) {
  return put('/repair/update', data)
}

export function startRepair(id) {
  return post('/repair/' + id + '/start')
}

export function finishRepair(id, data) {
  return post('/repair/' + id + '/finish', data)
}

export function recheckRepair(id, data) {
  return post('/repair/' + id + '/recheck', data)
}