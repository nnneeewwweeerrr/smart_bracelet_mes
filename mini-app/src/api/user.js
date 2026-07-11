import { post, get, put } from '../utils/request'

export function miniLogin(data) {
  return post('/mini/login', data)
}

export function systemLogin(data) {
  return post('/system/login', data)
}

export function getWorkerTasks() {
  return get('/mini/worker/tasks')
}

export function getUserInfo() {
  return get('/system/user/info')
}

export function updateUserInfo(data) {
  return put('/system/user/' + data.id, data)
}

export function changePassword(data) {
  return put('/system/user/password', data)
}

export function getOperationLogList(params) {
  return get('/system/log/list', params)
}

export function getOperationLogCount() {
  return get('/system/log/count')
}

export function getPersonalPerformance() {
  return get('/system/user/performance')
}
