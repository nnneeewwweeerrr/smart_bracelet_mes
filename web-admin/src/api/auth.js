import request from './request'

export const login = (data) => {
  return request.post('/auth/login', data)
}

export const getUserInfo = () => {
  return request.get('/auth/info')
}