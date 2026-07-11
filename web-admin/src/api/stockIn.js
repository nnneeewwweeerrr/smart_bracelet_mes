import request from './request'

export const getList = (params) => {
  return request.get('/stock-in/list', { params })
}

export const getById = (id) => {
  return request.get('/stock-in/' + id)
}

export const save = (data) => {
  return request.post('/stock-in/save', data)
}

export const remove = (id) => {
  return request.delete('/stock-in/delete/' + id)
}
