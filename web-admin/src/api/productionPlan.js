import request from './request'

export function getProductionPlanList(params) {
  return request({
    url: '/production-plan/list',
    method: 'get',
    params
  })
}

export function getProductionPlanDetail(id) {
  return request({
    url: '/production-plan/' + id,
    method: 'get'
  })
}

export function createProductionPlan(data) {
  return request({
    url: '/production-plan',
    method: 'post',
    data
  })
}

export function updateProductionPlan(data) {
  return request({
    url: '/production-plan',
    method: 'put',
    data
  })
}

export function deleteProductionPlan(id) {
  return request({
    url: '/production-plan/' + id,
    method: 'delete'
  })
}

export function approveProductionPlan(id) {
  return request({
    url: '/production-plan/' + id + '/approve',
    method: 'post'
  })
}
