import request from './request'

// 获取设备故障列表
export function getEquipmentFaultList(params) {
  return request({
    url: '/equipment-fault/list',
    method: 'get',
    params
  })
}

// 获取故障详情
export function getEquipmentFaultDetail(id) {
  return request({
    url: `/equipment-fault/${id}`,
    method: 'get'
  })
}

// 获取故障统计
export function getEquipmentFaultStats() {
  return request({
    url: '/equipment-fault/statistics',
    method: 'get'
  })
}

// 新增故障
export function addEquipmentFault(data) {
  return request({
    url: '/equipment-fault/save',
    method: 'post',
    data
  })
}

// 更新故障
export function updateEquipmentFault(data) {
  return request({
    url: '/equipment-fault/update',
    method: 'put',
    data
  })
}

// 派工
export function assignEquipmentFault(id, data) {
  return request({
    url: `/equipment-fault/assign/${id}`,
    method: 'put',
    data
  })
}

// 修复故障
export function fixEquipmentFault(id, data) {
  return request({
    url: `/equipment-fault/fix/${id}`,
    method: 'put',
    data
  })
}

// 删除故障
export function deleteEquipmentFault(id) {
  return request({
    url: `/equipment-fault/delete/${id}`,
    method: 'delete'
  })
}