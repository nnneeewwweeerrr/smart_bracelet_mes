import { post, get } from '../utils/request'

export function getQualityTaskList(params) {
  return get('/quality/task/list', params)
}

export function getQualityTaskDetail(id) {
  return get('/quality/task/' + id)
}

export function createQualityTask(data) {
  return post('/quality/task', data)
}

export function submitQualityResult(data) {
  return post('/quality/result', data)
}

export function getQualityStatistics() {
  return get('/quality/statistics')
}
