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
    url: '/production-plan/save',
    method: 'post',
    data
  })
}

export function updateProductionPlan(data) {
  return request({
    url: '/production-plan/update',
    method: 'put',
    data
  })
}

export function deleteProductionPlan(id) {
  return request({
    url: '/production-plan/delete/' + id,
    method: 'delete'
  })
}

export function startProductionPlan(id) {
  return request({
    url: '/production-plan/start/' + id,
    method: 'put'
  })
}
