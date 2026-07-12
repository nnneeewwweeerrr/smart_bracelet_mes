import request from './request'

export function getBigScreenData() {
  return request({
    url: '/big-screen/data',
    method: 'get'
  })
}
