import request from './request'

export function getFlowOverview() {
  return request({ url: '/process-flow/overview', method: 'get' })
}

export function getFlowDetail(workOrderId) {
  return request({ url: '/process-flow/detail/' + workOrderId, method: 'get' })
}

export function getFlowTimeline(workOrderId) {
  return request({ url: '/process-flow/timeline/' + workOrderId, method: 'get' })
}
