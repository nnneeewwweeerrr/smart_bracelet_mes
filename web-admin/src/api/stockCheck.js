import request from './request'

// 获取库存盘点列表
export function getStockCheckList(params) {
  return request({
    url: '/stock-check/list',
    method: 'get',
    params
  })
}

// 获取盘点详情
export function getStockCheckDetail(id) {
  return request({
    url: `/stock-check/${id}`,
    method: 'get'
  })
}

// 获取盘点统计
export function getStockCheckStats() {
  return request({
    url: '/stock-check/statistics',
    method: 'get'
  })
}

// 新增盘点
export function addStockCheck(data) {
  return request({
    url: '/stock-check/save',
    method: 'post',
    data
  })
}

// 更新盘点
export function updateStockCheck(data) {
  return request({
    url: '/stock-check/update',
    method: 'put',
    data
  })
}

// 完成盘点
export function completeStockCheck(id) {
  return request({
    url: `/stock-check/complete/${id}`,
    method: 'put'
  })
}

// 删除盘点
export function deleteStockCheck(id) {
  return request({
    url: `/stock-check/delete/${id}`,
    method: 'delete'
  })
}