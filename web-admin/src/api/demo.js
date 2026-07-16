import request from './request'

export const demoEquipment = {
  getList: (params) => request.get('/demo/equipment/list', { params }),
  getAll: () => request.get('/demo/equipment/all'),
  getById: (id) => request.get('/demo/equipment/' + id),
  save: (data) => request.post('/demo/equipment/save', data),
  update: (data) => request.put('/demo/equipment/update', data),
  remove: (id) => request.delete('/demo/equipment/delete/' + id),
  getStatistics: () => request.get('/demo/equipment/statistics')
}

export const demoEquipmentFault = {
  getList: (params) => request.get('/demo/equipment-fault/list', { params }),
  getAll: () => request.get('/demo/equipment-fault/all'),
  getById: (id) => request.get('/demo/equipment-fault/' + id),
  save: (data) => request.post('/demo/equipment-fault/save', data),
  update: (data) => request.put('/demo/equipment-fault/update', data),
  remove: (id) => request.delete('/demo/equipment-fault/delete/' + id),
  getStatistics: () => request.get('/demo/equipment-fault/statistics'),
  dispatch: (id, data) => request.post('/demo/equipment-fault/dispatch/' + id, data),
  close: (id) => request.post('/demo/equipment-fault/close/' + id)
}

export const demoRepairTask = {
  getList: (params) => request.get('/demo/repair-task/list', { params }),
  getAll: () => request.get('/demo/repair-task/all'),
  getById: (id) => request.get('/demo/repair-task/' + id),
  save: (data) => request.post('/demo/repair-task/save', data),
  update: (data) => request.put('/demo/repair-task/update', data),
  remove: (id) => request.delete('/demo/repair-task/delete/' + id),
  getStatistics: () => request.get('/demo/repair-task/statistics'),
  accept: (id) => request.post('/demo/repair-task/accept/' + id),
  start: (id) => request.post('/demo/repair-task/start/' + id),
  complete: (id, data) => request.post('/demo/repair-task/complete/' + id, data),
  acceptance: (id, data) => request.post('/demo/repair-task/acceptance/' + id, data)
}

export const demoEquipmentMaintain = {
  getList: (params) => request.get('/demo/equipment-maintain/list', { params }),
  getAll: () => request.get('/demo/equipment-maintain/all'),
  getById: (id) => request.get('/demo/equipment-maintain/' + id),
  save: (data) => request.post('/demo/equipment-maintain/save', data),
  update: (data) => request.put('/demo/equipment-maintain/update', data),
  remove: (id) => request.delete('/demo/equipment-maintain/delete/' + id),
  getStatistics: () => request.get('/demo/equipment-maintain/statistics'),
  start: (id) => request.post('/demo/equipment-maintain/start/' + id),
  complete: (id, data) => request.post('/demo/equipment-maintain/complete/' + id, data)
}

export const demoStockIn = {
  getList: (params) => request.get('/demo/stock-in/list', { params }),
  getAll: () => request.get('/demo/stock-in/all'),
  getById: (id) => request.get('/demo/stock-in/' + id),
  save: (data) => request.post('/demo/stock-in/save', data),
  update: (data) => request.put('/demo/stock-in/update', data),
  remove: (id) => request.delete('/demo/stock-in/delete/' + id),
  getStatistics: () => request.get('/demo/stock-in/statistics'),
  approve: (id) => request.post('/demo/stock-in/approve/' + id),
  cancel: (id) => request.post('/demo/stock-in/cancel/' + id),
  scan: (data) => request.post('/demo/stock-in/scan', data)
}

export const demoStockOut = {
  getList: (params) => request.get('/demo/stock-out/list', { params }),
  getAll: () => request.get('/demo/stock-out/all'),
  getById: (id) => request.get('/demo/stock-out/' + id),
  save: (data) => request.post('/demo/stock-out/save', data),
  update: (data) => request.put('/demo/stock-out/update', data),
  remove: (id) => request.delete('/demo/stock-out/delete/' + id),
  getStatistics: () => request.get('/demo/stock-out/statistics'),
  approve: (id) => request.post('/demo/stock-out/approve/' + id),
  cancel: (id) => request.post('/demo/stock-out/cancel/' + id)
}

export const demoStock = {
  getList: (params) => request.get('/demo/stock/list', { params }),
  getAll: () => request.get('/demo/stock/all'),
  getById: (id) => request.get('/demo/stock/' + id),
  save: (data) => request.post('/demo/stock/save', data),
  update: (data) => request.put('/demo/stock/update', data),
  remove: (id) => request.delete('/demo/stock/delete/' + id),
  getStatistics: () => request.get('/demo/stock/statistics'),
  checkAlert: () => request.post('/demo/stock/check-alert')
}

export const demoStockCheck = {
  getList: (params) => request.get('/demo/stock-check/list', { params }),
  getAll: () => request.get('/demo/stock-check/all'),
  getById: (id) => request.get('/demo/stock-check/' + id),
  save: (data) => request.post('/demo/stock-check/save', data),
  update: (data) => request.put('/demo/stock-check/update', data),
  remove: (id) => request.delete('/demo/stock-check/delete/' + id),
  getStatistics: () => request.get('/demo/stock-check/statistics'),
  getDetails: (checkId) => request.get('/demo/stock-check/details/' + checkId),
  createDetail: (checkId, data) => request.post('/demo/stock-check/create-detail/' + checkId, data),
  audit: (id) => request.post('/demo/stock-check/audit/' + id),
  complete: (id) => request.post('/demo/stock-check/complete/' + id)
}

export const demoStockAlert = {
  getList: (params) => request.get('/demo/stock-alert/list', { params }),
  getAll: () => request.get('/demo/stock-alert/all'),
  getById: (id) => request.get('/demo/stock-alert/' + id),
  save: (data) => request.post('/demo/stock-alert/save', data),
  update: (data) => request.put('/demo/stock-alert/update', data),
  remove: (id) => request.delete('/demo/stock-alert/delete/' + id),
  getStatistics: () => request.get('/demo/stock-alert/statistics'),
  process: (id, data) => request.post('/demo/stock-alert/process/' + id, data)
}