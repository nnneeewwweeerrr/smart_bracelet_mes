import request from './request'

export function getProductList(params) {
  return request({
    url: '/product/list',
    method: 'get',
    params
  })
}

export function getProductDetail(id) {
  return request({
    url: '/product/' + id,
    method: 'get'
  })
}

export function createProduct(data) {
  return request({
    url: '/product/save',
    method: 'post',
    data
  })
}

export function updateProduct(data) {
  return request({
    url: '/product/update',
    method: 'put',
    data
  })
}

export function deleteProduct(id) {
  return request({
    url: '/product/delete/' + id,
    method: 'delete'
  })
}
