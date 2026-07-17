import request from './request'

export const systemUser = {
  getList: () => request.get('/system/user/list'),
  getById: (id) => request.get('/system/user/' + id),
  save: (data) => request.post('/system/user/save', data),
  update: (data) => request.put('/system/user/update', data),
  updatePassword: (data) => request.put('/system/user/password', data),
  remove: (id) => request.delete('/system/user/delete/' + id)
}

export const systemRole = {
  getList: () => request.get('/system/role/list'),
  save: (data) => request.post('/system/role/save', data),
  update: (data) => request.put('/system/role/update', data),
  remove: (id) => request.delete('/system/role/delete/' + id)
}

export const systemPermission = {
  getList: () => request.get('/system/permission/list'),
  save: (data) => request.post('/system/permission/save', data),
  update: (data) => request.put('/system/permission/update', data),
  remove: (id) => request.delete('/system/permission/delete/' + id)
}

export const operationLog = {
  getList: (params) => request.get('/operation-log/list', { params }),
  getModules: () => request.get('/operation-log/modules'),
  getById: (id) => request.get('/operation-log/' + id),
  getCount: () => request.get('/operation-log/count')
}
