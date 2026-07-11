import request from './request'

export const getList = (params) => {
  return request.get('/product/list', { params })
}

export const getProductList = getList

export const getById = (id) => {
  return request.get('/product/' + id)
}

export const save = (data) => {
  return request.post('/product/save', data)
}

export const update = (data) => {
  return request.put('/product/update', data)
}

export const remove = (id) => {
  return request.delete('/product/delete/' + id)
}
