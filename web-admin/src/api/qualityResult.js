import request from './request'

export function getQualityResultList(params) {
  return request({
    url: '/quality-result/list',
    method: 'get',
    params
  })
}

export function getQualityResultDetail(id) {
  return request({
    url: '/quality-result/' + id,
    method: 'get'
  })
}

export function createQualityResult(data) {
  return request({
    url: '/quality-result/save',
    method: 'post',
    data
  })
}

export function updateQualityResult(data) {
  return request({
    url: '/quality-result/update',
    method: 'put',
    data
  })
}
