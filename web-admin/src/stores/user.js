import { defineStore } from 'pinia'
import { login, getUserInfo } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        if (res.code === 200) {
          this.token = res.data.token
          localStorage.setItem('token', res.data.token)
          return res
        }
        return res
      } catch (error) {
        throw error
      }
    },
    async getUserInfo() {
      try {
        const res = await getUserInfo()
        if (res.code === 200) {
          this.userInfo = res.data
          localStorage.setItem('userInfo', JSON.stringify(res.data))
        }
        return res
      } catch (error) {
        throw error
      }
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
