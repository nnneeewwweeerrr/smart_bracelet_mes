import request from './request'

export function getPurchaseList(params) {
  return request({
    url: '/purchase/list',
    method: 'get',
    params
  })
}

export function getPurchaseDetail(id) {
  return request({
    url: '/purchase/' + id,
    method: 'get'
  })
}

export function createPurchase(data) {
  return request({
    url: '/purchase',
    method: 'post',
    data
  })
}

export function updatePurchase(data) {
  return request({
    url: '/purchase',
    method: 'put',
    data
  })
}

export function deletePurchase(id) {
  return request({
    url: '/purchase/' + id,
    method: 'delete'
  })
}

export function approvePurchase(id) {
  return request({
    url: '/purchase/' + id + '/approve',
    method: 'post'
  })
}
