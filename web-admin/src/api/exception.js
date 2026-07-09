import request from './request'

export const getList = (params) => {
  return request.get('/exception/list', { params })
}

export const getById = (id) => {
  return request.get('/exception/' + id)
}

export const save = (data) => {
  return request.post('/exception/save', data)
}

export const update = (data) => {
  return request.put('/exception/update', data)
}

export const remove = (id) => {
  return request.delete('/exception/delete/' + id)
}

export const handle = (id) => {
  return request.put('/exception/handle/' + id)
}

export const resolve = (id) => {
  return request.put('/exception/resolve/' + id)
}

export const batchTrace = (workOrderId) => {
  return request.get('/exception/batch-trace/' + workOrderId)
}
