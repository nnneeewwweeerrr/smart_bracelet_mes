import request from './request'

export const getList = (params) => {
  return request.get('/process/list', { params })
}

export const getById = (id) => {
  return request.get('/process/' + id)
}

export const getByProductId = (productId) => {
  return request.get('/process/product/' + productId)
}

export const save = (data) => {
  return request.post('/process/save', data)
}

export const update = (data) => {
  return request.put('/process/update', data)
}

export const remove = (id) => {
  return request.delete('/process/delete/' + id)
}
