import request from './request'

export const getList = (params) => {
  return request.get('/production-plan/list', { params })
}

export const getById = (id) => {
  return request.get('/production-plan/' + id)
}

export const save = (data) => {
  return request.post('/production-plan/save', data)
}

export const update = (data) => {
  return request.put('/production-plan/update', data)
}

export const remove = (id) => {
  return request.delete('/production-plan/delete/' + id)
}

export const start = (id) => {
  return request.put('/production-plan/start/' + id)
}

export const finish = (id) => {
  return request.put('/production-plan/finish/' + id)
}
