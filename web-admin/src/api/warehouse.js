import request from './request'

export function getWarehouseList(params) {
  return request({
    url: '/warehouse/list',
    method: 'get',
    params
  })
}

export function getWarehouseDetail(id) {
  return request({
    url: '/warehouse/' + id,
    method: 'get'
  })
}

export function createWarehouse(data) {
  return request({
    url: '/warehouse',
    method: 'post',
    data
  })
}

export function updateWarehouse(data) {
  return request({
    url: '/warehouse',
    method: 'put',
    data
  })
}

export function deleteWarehouse(id) {
  return request({
    url: '/warehouse/' + id,
    method: 'delete'
  })
}
