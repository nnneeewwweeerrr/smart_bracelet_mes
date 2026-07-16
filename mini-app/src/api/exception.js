import { post, get, put } from '../utils/request'

export function getExceptionList(params) {
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
  return get('/exception/list', newParams)
}

export function getExceptionDetail(id) {
  return get('/exception/' + id)
}

export function createException(data) {
  return post('/exception/save', data)
}

export function handleException(id, data) {
  return put('/exception/handle/' + id + '/detail', data)
}