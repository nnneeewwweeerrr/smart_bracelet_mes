import request from './request'

export const getList = (params) => {
  return request.get('/material/list', { params })
}

export const getMaterialList = getList

export const getById = (id) => {
  return request.get('/material/' + id)
}

export const getByCode = (code) => {
  return request.get('/material/code/' + code)
}

export const save = (data) => {
  return request.post('/material/save', data)
}

export const update = (data) => {
  return request.put('/material/update', data)
}

export const remove = (id) => {
  return request.delete('/material/delete/' + id)
}
