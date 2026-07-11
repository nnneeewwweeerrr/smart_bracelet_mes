import request from './request'

export const getList = (params) => {
  return request.get('/stock-out/list', { params })
}

export const getById = (id) => {
  return request.get('/stock-out/' + id)
}

export const save = (data) => {
  return request.post('/stock-out/save', data)
}

export const remove = (id) => {
  return request.delete('/stock-out/delete/' + id)
}
