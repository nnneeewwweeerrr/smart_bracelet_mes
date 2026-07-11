import request from './request'

export const getList = () => {
  return request.get('/warehouse/list')
}

export const save = (data) => {
  return request.post('/warehouse/save', data)
}

export const update = (data) => {
  return request.put('/warehouse/update', data)
}

export const remove = (id) => {
  return request.delete('/warehouse/delete/' + id)
}
