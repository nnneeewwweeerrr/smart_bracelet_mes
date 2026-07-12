import request from './request'

export function getReverseTrace(snCode) {
  return request({
    url: '/sn-trace/reverse/' + snCode,
    method: 'get'
  })
}

export function getForwardTrace(batchNo) {
  return request({
    url: '/sn-trace/forward/' + batchNo,
    method: 'get'
  })
}

export function getSnList(params) {
  return request({
    url: '/sn-trace/list',
    method: 'get',
    params
  })
}

export function getSnDetail(id) {
  return request({
    url: '/sn-trace/' + id,
    method: 'get'
  })
}
