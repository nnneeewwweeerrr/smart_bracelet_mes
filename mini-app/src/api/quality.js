import { post, get } from '../utils/request'

export function getQualityTaskList(params) {
  const newParams = { ...params }
  if (newParams.page !== undefined) {
    newParams.pageNum = newParams.page
    delete newParams.page
  }
  if (newParams.size !== undefined) {
    newParams.pageSize = newParams.size
    delete newParams.size
  }
  if (newParams.status !== undefined && newParams.status !== null && newParams.status !== '') {
    newParams.status = Number(newParams.status)
  }
  return get('/quality-task/list', newParams)
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