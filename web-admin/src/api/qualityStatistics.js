import request from './request'

export function getOverview() {
  return request({
    url: '/quality-statistics/overview',
    method: 'get'
  })
}

export function getAvatarReport() {
  return request({
    url: '/quality-statistics/avatar-report',
    method: 'get'
  })
}
