import request from './request'

export const getList = (params) => {
  return request.get('/stock/list', { params })
}
