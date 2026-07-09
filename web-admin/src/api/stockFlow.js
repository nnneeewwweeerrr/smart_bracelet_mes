import request from './request'

export const getList = (params) => {
  return request.get('/stock-flow/list', { params })
}
