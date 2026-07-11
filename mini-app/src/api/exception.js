import { post, get, put } from '../utils/request'

export function getExceptionList(params) {
  return get('/exception/list', params)
}

export function getExceptionDetail(id) {
  return get('/exception/' + id)
}

export function createException(data) {
  return post('/exception', data)
}

export function handleException(id, data) {
  return put('/exception/' + id + '/handle', data)
}
