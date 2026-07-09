import request from './request'

export const getList = (params) => {
  return request.get('/customer/list', { params })
}

export const getCustomerList = getList

export const getById = (id) => {
  return request.get('/customer/' + id)
}

export const save = (data) => {
  return request.post('/customer/save', data)
}

export const update = (data) => {
  return request.put('/customer/update', data)
}

export const remove = (id) => {
  return request.delete('/customer/delete/' + id)
}
