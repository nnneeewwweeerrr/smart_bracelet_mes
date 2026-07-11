import request from './request'

export function getWorkOrderList(params) {
  return request({
    url: '/work-order/list',
    method: 'get',
    params
  })
}

export function getWorkOrderDetail(id) {
  return request({
    url: '/work-order/' + id,
    method: 'get'
  })
}

export function createWorkOrder(data) {
  return request({
    url: '/work-order',
    method: 'post',
    data
  })
}

export function updateWorkOrder(data) {
  return request({
    url: '/work-order',
    method: 'put',
    data
  })
}

export function deleteWorkOrder(id) {
  return request({
    url: '/work-order/' + id,
    method: 'delete'
  })
}

export function startWorkOrder(id) {
  return request({
    url: '/work-order/' + id + '/start',
    method: 'post'
  })
}

export function finishWorkOrder(id) {
  return request({
    url: '/work-order/' + id + '/finish',
    method: 'post'
  })
}
