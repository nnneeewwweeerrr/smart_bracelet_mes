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
    url: '/work-order/save',
    method: 'post',
    data
  })
}

export function updateWorkOrder(data) {
  return request({
    url: '/work-order/update',
    method: 'put',
    data
  })
}

export function deleteWorkOrder(id) {
  return request({
    url: '/work-order/delete/' + id,
    method: 'delete'
  })
}

export function startWorkOrder(id) {
  return request({
    url: '/work-order/dispatch/' + id,
    method: 'put'
  })
}

export function finishWorkOrder(id) {
  return request({
    url: '/work-order/complete/' + id,
    method: 'put'
  })
}
