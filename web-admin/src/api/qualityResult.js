import request from './request'

export const getList = (params) => {
  return request.get('/quality-result/list', { params })
}

export const save = (data) => {
  return request.post('/quality-result/save', data)
}

export const batchSave = (data) => {
  return request.post('/quality-result/batch-save', data)
}

export const update = (data) => {
  return request.put('/quality-result/update', data)
}

export const remove = (id) => {
  return request.delete('/quality-result/delete/' + id)
}

export const getReport = (taskId) => {
  return request.get('/quality-result/report/' + taskId)
}
