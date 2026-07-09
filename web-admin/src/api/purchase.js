import request from './request'

export const getPurchaseList = (params) => {
  return request.get('/purchase/list', { params })
}

export const getPurchaseDetail = (id) => {
  return request.get(`/purchase/${id}`)
}

export const savePurchase = (data) => {
  return request.post('/purchase/save', data)
}

export const updatePurchase = (data) => {
  return request.put('/purchase/update', data)
}

export const deletePurchase = (id) => {
  return request.delete(`/purchase/delete/${id}`)
}

export const auditPurchase = (id) => {
  return request.put(`/purchase/audit/${id}`)
}

export const arrivePurchase = (id) => {
  return request.put(`/purchase/arrive/${id}`)
}

export const completePurchase = (id) => {
  return request.put(`/purchase/complete/${id}`)
}

export const cancelPurchase = (id) => {
  return request.put(`/purchase/cancel/${id}`)
}
