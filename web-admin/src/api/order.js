import request from './request'

export const getOrderList = (params) => {
  return request.get('/order/list', { params })
}

export const getOrderDetail = (id) => {
  return request.get(`/order/${id}`)
}

export const saveOrder = (data) => {
  return request.post('/order/save', data)
}

export const updateOrder = (data) => {
  return request.put('/order/update', data)
}

export const deleteOrder = (id) => {
  return request.delete(`/order/delete/${id}`)
}

export const auditOrder = (id) => {
  return request.put(`/order/audit/${id}`)
}

export const dispatchOrder = (id) => {
  return request.put(`/order/dispatch/${id}`)
}

export const cancelOrder = (id) => {
  return request.put(`/order/cancel/${id}`)
}

export const changeOrderStatus = (id, status) => {
  return request.put(`/order/status/${id}/${status}`)
}
