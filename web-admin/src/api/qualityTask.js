import request from './request'

// 获取质检任务列表
export function getList(params) {
  return request({ url: '/quality-task/list', method: 'get', params })
}

// 获取详情
export function getById(id) {
  return request({ url: '/quality-task/' + id, method: 'get' })
}

// 新增
export function save(data) {
  return request({ url: '/quality-task/save', method: 'post', data })
}

// 更新
export function update(data) {
  return request({ url: '/quality-task/update', method: 'put', data })
}

// 删除
export function remove(id) {
  return request({ url: '/quality-task/delete/' + id, method: 'delete' })
}

// 开始质检
export function start(id) {
  return request({ url: '/quality-task/start/' + id, method: 'put' })
}

// 完成质检
export function complete(id) {
  return request({ url: '/quality-task/complete/' + id, method: 'put' })
}
