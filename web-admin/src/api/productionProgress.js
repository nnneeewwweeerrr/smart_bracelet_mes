import request from './request'

export function getProductionProgressList() {
  return request({
    url: '/production-progress/list',
    method: 'get'
  })
}
