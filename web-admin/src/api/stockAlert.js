import request from './request'

// 获取库存预警列表
export function getStockAlertList(params) {
  return request({
    url: '/stock-alert/list',
    method: 'get',
    params
  })
}

// 获取预警详情
export function getStockAlertDetail(id) {
  return request({
    url: `/stock-alert/${id}`,
    method: 'get'
  })
}

// 获取预警统计
export function getStockAlertStats() {
  return request({
    url: '/stock-alert/statistics',
    method: 'get'
  })
}

// 处理预警
export function handleStockAlert(id, data) {
  return request({
    url: `/stock-alert/handle/${id}`,
    method: 'put',
    data
  })
}

// 删除预警
export function deleteStockAlert(id) {
  return request({
    url: `/stock-alert/delete/${id}`,
    method: 'delete'
  })
}