import request from './request'

export const getList = (params) => {
  return request.get('/work-order/list', { params })
}

export const getById = (id) => {
  return request.get('/work-order/' + id)
}

export const save = (data) => {
  return request.post('/work-order/save', data)
}

export const update = (data) => {
  return request.put('/work-order/update', data)
}

export const remove = (id) => {
  return request.delete('/work-order/delete/' + id)
}

export const dispatch = (id) => {
  return request.put('/work-order/dispatch/' + id)
}

export const complete = (id) => {
  return request.put('/work-order/complete/' + id)
}

export const generateFromPlan = (planId) => {
  return request.post('/work-order/generate-from-plan/' + planId)
}
