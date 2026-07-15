import { getToken, getUser, logout } from './auth'

const BASE_URL = 'http://192.168.110.90:8080/api'

function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const header = {
      'Content-Type': 'application/json',
      ...(options.header || {})
    }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    let requestData = options.data || {}
    if (requestData.page !== undefined && requestData.size !== undefined) {
      requestData = {
        ...requestData,
        pageNum: requestData.page,
        pageSize: requestData.size,
        page: undefined,
        size: undefined
      }
    }

    const fullUrl = BASE_URL + options.url
    console.log('请求地址:', fullUrl)
    console.log('请求方法:', options.method || 'GET')
    console.log('请求数据:', requestData)
    console.log('请求头:', header)

    uni.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: requestData,
      header,
      timeout: 30000,
      success(res) {
        console.log('请求成功:', res)
        const { statusCode, data } = res
        if (statusCode === 200) {
          if (data.code === 200) {
            resolve(data.data)
          } else if (data.code === 401) {
            uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
            logout()
            reject(data)
          } else {
            uni.showToast({ title: data.message || '请求失败', icon: 'none' })
            reject(data)
          }
        } else {
          uni.showToast({ title: '网络请求失败', icon: 'none' })
          reject(res)
        }
      },
      fail(err) {
        console.error('请求失败:', err)
        uni.showToast({ title: '网络连接异常，请检查网络', icon: 'none' })
        reject(err)
      },
      complete(res) {
        console.log('请求完成:', res)
      }
    })
  })
}

export function get(url, data) {
  return request({ url, method: 'GET', data })
}

export function post(url, data) {
  return request({ url, method: 'POST', data })
}

export function put(url, data) {
  return request({ url, method: 'PUT', data })
}

export function del(url, data) {
  return request({ url, method: 'DELETE', data })
}

export default request