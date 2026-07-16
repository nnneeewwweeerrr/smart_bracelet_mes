import request from './request'

export function getList(params) {
  return request({
    url: '/equipment-maintain/list',
    method: 'get',
    params
  })
}

export function getByEquipmentId(equipmentId) {
  return request({
    url: `/equipment-maintain/equipment/${equipmentId}`,
    method: 'get'
  })
}

export function getById(id) {
  return request({
    url: `/equipment-maintain/${id}`,
    method: 'get'
  })
}

export function save(data) {
  return request({
    url: '/equipment-maintain/save',
    method: 'post',
    data
  })
}

export function update(data) {
  return request({
    url: '/equipment-maintain/update',
    method: 'put',
    data
  })
}

export function remove(id) {
  return request({
    url: `/equipment-maintain/delete/${id}`,
    method: 'delete'
  })
}
