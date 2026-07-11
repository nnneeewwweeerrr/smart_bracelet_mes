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
    url: '/material/' + id,
    method: 'get'
  })
}

export function getMaterialByCode(code) {
  return request({
    url: '/material/code/' + code,
    method: 'get'
  })
}

export function createMaterial(data) {
  return request({
    url: '/material',
    method: 'post',
    data
  })
}

export function updateMaterial(data) {
  return request({
    url: '/material',
    method: 'put',
    data
  })
}

export function deleteMaterial(id) {
  return request({
    url: '/material/' + id,
    method: 'delete'
  })
}
