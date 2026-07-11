import request from './request'

export function getQualityTaskList(params) {
  return request({
    url: '/quality-task/list',
    method: 'get',
    params
  })
}

export function getQualityTaskDetail(id) {
  return request({
    url: '/quality-task/' + id,
    method: 'get'
  })
}

export function createQualityTask(data) {
  return request({
    url: '/quality-task/save',
    method: 'post',
    data
  })
}

export function updateQualityTask(data) {
  return request({
    url: '/quality-task/update',
    method: 'put',
    data
  })
}

export function deleteQualityTask(id) {
  return request({
    url: '/quality-task/delete/' + id,
    method: 'delete'
  })
}
