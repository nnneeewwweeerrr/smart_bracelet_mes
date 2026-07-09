import request from './request'

export const getList = (params) => {
  return request.get('/production-report/list', { params })
}

export const save = (data) => {
  return request.post('/production-report/save', data)
}

export const remove = (id) => {
  return request.delete('/production-report/delete/' + id)
}
