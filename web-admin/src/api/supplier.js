import request from './request'

export const getSupplierList = (params) => {
  return request.get('/supplier/list', { params })
}

export const getSupplierDetail = (id) => {
  return request.get(`/supplier/${id}`)
}

export const saveSupplier = (data) => {
  return request.post('/supplier/save', data)
}

export const updateSupplier = (data) => {
  return request.put('/supplier/update', data)
}

export const deleteSupplier = (id) => {
  return request.delete(`/supplier/delete/${id}`)
}
