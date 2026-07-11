import request from './request'

export function getProductionReportList(params) {
  return request({
    url: '/production-report/list',
    method: 'get',
    params
  })
}

export function getProductionReportDetail(id) {
  return request({
    url: '/production-report/' + id,
    method: 'get'
  })
}

export function createProductionReport(data) {
  return request({
    url: '/production-report',
    method: 'post',
    data
  })
}
