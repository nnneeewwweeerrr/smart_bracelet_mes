import request from './request'

// SOP 基本信息
export function getSopByProcessId(processId) {
  return request({
    url: '/sop/process/' + processId,
    method: 'get'
  })
}

export function saveSop(data) {
  return request({
    url: '/sop/save',
    method: 'post',
    data
  })
}

export function updateSop(data) {
  return request({
    url: '/sop/update',
    method: 'put',
    data
  })
}

export function deleteSop(id) {
  return request({
    url: '/sop/delete/' + id,
    method: 'delete'
  })
}

// 工艺参数标准
export function getParamList(processId) {
  return request({
    url: '/sop/param/list/' + processId,
    method: 'get'
  })
}

export function saveParam(data) {
  return request({
    url: '/sop/param/save',
    method: 'post',
    data
  })
}

export function deleteParam(id) {
  return request({
    url: '/sop/param/delete/' + id,
    method: 'delete'
  })
}

// 物料 BOM
export function getBomList(processId) {
  return request({
    url: '/sop/bom/list/' + processId,
    method: 'get'
  })
}

export function saveBom(data) {
  return request({
    url: '/sop/bom/save',
    method: 'post',
    data
  })
}

export function deleteBom(id) {
  return request({
    url: '/sop/bom/delete/' + id,
    method: 'delete'
  })
}

// 防错校验
export function verifyMaterial(data) {
  return request({
    url: '/sop/verify/material',
    method: 'post',
    data
  })
}

export function verifyParams(data) {
  return request({
    url: '/sop/verify/params',
    method: 'post',
    data
  })
}
