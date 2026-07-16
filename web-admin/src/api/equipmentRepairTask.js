import request from './request'

export function getRepairTaskList(params) {
  return request({
    url: '/equipment-repair-task/list',
    method: 'get',
    params
  })
}

export function getRepairTaskDetail(id) {
  return request({
    url: `/equipment-repair-task/${id}`,
    method: 'get'
  })
}

export function getRepairTaskStats() {
  return request({
    url: '/equipment-repair-task/statistics',
    method: 'get'
  })
}

export function addRepairTask(data) {
  return request({
    url: '/equipment-repair-task/save',
    method: 'post',
    data
  })
}

export function updateRepairTask(data) {
  return request({
    url: '/equipment-repair-task/update',
    method: 'put',
    data
  })
}

export function assignRepairTask(data) {
  return request({
    url: '/equipment-repair-task/assign',
    method: 'post',
    data
  })
}

export function acceptRepairTask(id, data) {
  return request({
    url: `/equipment-repair-task/accept/${id}`,
    method: 'put',
    data
  })
}

export function completeRepairTask(id, data) {
  return request({
    url: `/equipment-repair-task/complete/${id}`,
    method: 'put',
    data
  })
}

export function acceptanceRepairTask(id, data) {
  return request({
    url: `/equipment-repair-task/acceptance/${id}`,
    method: 'put',
    data
  })
}

export function deleteRepairTask(id) {
  return request({
    url: `/equipment-repair-task/delete/${id}`,
    method: 'delete'
  })
}
