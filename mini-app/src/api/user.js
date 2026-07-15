import { post, get, put } from '../utils/request'

export function miniLogin(data) {
  return post('/mini/login', data)
}

export function getWorkerTasks() {
  return get('/mini/worker/tasks')
}

export function getUserInfo() {
  return get('/auth/info')
}

export function updateUserInfo(data) {
  return put('/system/user/update', data)
}

export function changePassword(data) {
  return put('/system/user/password', data)
}

export function getOperationLogList(params) {
  return get('/operation-log/list', params)
}

export function getOperationLogCount() {
  return get('/operation-log/count')
}

export function getPersonalPerformance() {
  return get('/mini/production/dashboard')
}