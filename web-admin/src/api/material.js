import request from './request'

export function getMaterialList(params) {
  return request({
    url: '/material/list',
    method: 'get',
    params
  })
}

export function getMaterialDetail(id) {
  return request({
    url: `/material/${id}`,
    method: 'get'
  })
}

export function getMaterialAll() {
  return request({
    url: '/material/all',
    method: 'get'
  })
}

export function getMaterialByCode(code) {
  return request({
    url: `/material/code/${code}`,
    method: 'get'
  })
}

export function createMaterial(data) {
  return request({
    url: '/material/save',
    method: 'post',
    data
  })
}

export function updateMaterial(data) {
  return request({
    url: '/material/update',
    method: 'put',
    data
  })
}

export function deleteMaterial(id) {
  return request({
    url: `/material/delete/${id}`,
    method: 'delete'
  })
}