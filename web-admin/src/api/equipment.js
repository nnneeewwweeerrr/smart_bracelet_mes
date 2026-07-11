import request from './request'

export const getList = () => {
  return request.get('/equipment/list')
}

export const getById = (id) => {
  return request.get('/equipment/' + id)
}

export const save = (data) => {
  return request.post('/equipment/save', data)
}

export const update = (data) => {
  return request.put('/equipment/update', data)
}

export const remove = (id) => {
  return request.delete('/equipment/delete/' + id)
}

export const getStatistics = () => {
  return request.get('/equipment/statistics')
}

export const getStatusList = () => {
  return request.get('/equipment/status-list')
}
