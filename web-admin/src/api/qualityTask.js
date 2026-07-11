import request from './request'

export const getList = (params) => {
  return request.get('/quality-task/list', { params })
}

export const getById = (id) => {
  return request.get('/quality-task/' + id)
}

export const save = (data) => {
  return request.post('/quality-task/save', data)
}

export const update = (data) => {
  return request.put('/quality-task/update', data)
}

export const remove = (id) => {
  return request.delete('/quality-task/delete/' + id)
}

export const start = (id) => {
  return request.put('/quality-task/start/' + id)
}

export const complete = (id) => {
  return request.put('/quality-task/complete/' + id)
}
