import { post, get } from '../utils/request'

export function getQualityTaskList(params) {
  return get('/quality-task/list', params)
}

export function getQualityTaskDetail(id) {
  return get('/quality-task/' + id)
}

export function createQualityTask(data) {
  return post('/quality-task/save', data)
}

export function submitQualityResult(data) {
  return post('/quality-result/save', data)
}

export function getQualityStatistics() {
  return get('/mini/quality/statistics')
}