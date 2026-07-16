<template>
  <view class="login-page">
    <view class="login-bg">
      <image 
        class="bg-image" 
        src="/static/login-bg.png" 
        mode="widthFix" 
      />
      <view class="bg-gradient"></view>
    </view>
    
    <view class="login-content" :style="{ paddingTop: statusBarPadding }">
      <view class="login-card">
        <view class="login-header">
          <view class="logo-wrap">
            <image class="login-logo" src="/static/logo.png" mode="aspectFit" />
          </view>
          <text class="login-title">智能手环MES系统</text>
          <text class="login-subtitle">Manufacturing Execution System</text>
        </view>

        <view class="login-form">
          <view class="form-header">
            <text class="form-title">账号登录</text>
            <view class="form-decoration"></view>
          </view>
          
          <view class="form-group">
            <view class="input-wrapper">
              <view class="icon-wrap">
                <text class="input-icon">👤</text>
              </view>
              <input 
                v-model="form.userName" 
                type="text" 
                placeholder="请输入用户名"
                class="login-input"
              />
              <view class="input-line"></view>
            </view>
          </view>
          
          <view class="form-group">
            <view class="input-wrapper">
              <view class="icon-wrap">
                <text class="input-icon">🔒</text>
              </view>
              <input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码"
                class="login-input"
              />
              <view class="input-line"></view>
            </view>
          </view>

          <button 
            class="login-btn"
            :loading="loading"
            :disabled="loading"
            @click="handleLogin"
          >
            <text class="btn-text">登 录</text>
            <view class="btn-shine"></view>
          </button>
        </view>

        <view class="login-footer">
          <text class="footer-text">智能手环MES v1.0.0</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { miniLogin } from '@/api/user'
import { setToken, setUser } from '@/utils/auth'

export default {
  data() {
    return {
      form: {
        userName: '',
        password: ''
      },
      loading: false,
      statusBarPadding: '88rpx'
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync()
    const statusBarHeight = sysInfo.statusBarHeight || 44
    this.statusBarPadding = `calc(${statusBarHeight}px + 40rpx)`
  },
  methods: {
    async handleLogin() {
      if (!this.form.userName) {
        uni.showToast({ title: '请输入用户名', icon: 'none' })
        return
      }
      if (!this.form.password) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      this.loading = true
      try {
        const res = await miniLogin({
          userName: this.form.userName,
          password: this.form.password
        })
        setToken(res.token)
        setUser(res.user)
        uni.reLaunch({
          url: '/pages/workbench/index'
        })
      } catch {
        uni.showToast({ title: '登录失败，请检查账号密码', icon: 'none' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.bg-image {
  width: 100%;
  display: block;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.02) 0%, rgba(0, 0, 0, 0.02) 50%, rgba(255, 255, 255, 0.3) 80%, rgba(255, 255, 255, 0.6) 100%);
}

.login-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding: 40rpx;
  padding-bottom: 80rpx;
  z-index: 1;
  position: relative;
}

.login-card {
  width: 100%;
  max-width: 680rpx;
  background: transparent;
  border-radius: 32rpx;
  padding: 50rpx 44rpx;
  box-shadow: none;
  border: none;
  position: relative;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
}

.logo-wrap {
  width: 130rpx;
  height: 130rpx;
  border-radius: 34rpx;
  background: rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  animation: logoFloat 4s ease-in-out infinite;
  position: relative;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  }
  50% {
    transform: translateY(-10rpx);
    box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.1);
  }
}

.login-logo {
  width: 75rpx;
  height: 75rpx;
  position: relative;
  z-index: 1;
}

.login-title {
  font-size: 46rpx;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 12rpx;
  letter-spacing: 6rpx;
}

.login-subtitle {
  font-size: 24rpx;
  color: #666666;
  letter-spacing: 3rpx;
  font-weight: 300;
}

.login-form {
  width: 100%;
}

.form-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
}

.form-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 16rpx;
}

.form-decoration {
  width: 80rpx;
  height: 6rpx;
  background: linear-gradient(90deg, transparent, #3B7CFF, transparent);
  border-radius: 3rpx;
}

.form-group {
  margin-bottom: 36rpx;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 20rpx;
  padding: 0 28rpx;
  height: 100rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
  position: relative;

  &:active {
    border-color: rgba(59, 124, 255, 0.3);
    background: rgba(255, 255, 255, 0.4);
  }
}

.icon-wrap {
  position: absolute;
  left: 28rpx;
  top: 50%;
  transform: translateY(-50%);
}

.input-icon {
  font-size: 32rpx;
  opacity: 0.4;
}

.login-input {
  flex: 1;
  height: 100%;
  font-size: 30rpx;
  color: #333;
  background: transparent;
  padding-left: 60rpx;
}

.login-input::placeholder {
  color: #B0B0B0;
}

.input-line {
  position: absolute;
  bottom: 0;
  left: 28rpx;
  right: 28rpx;
  height: 2rpx;
  background: #E8E8E8;
  transition: all 0.3s ease;

  .input-wrapper:active & {
    background: linear-gradient(90deg, transparent, #3B7CFF, transparent);
  }
}

.login-btn {
  width: 100%;
  height: 100rpx;
  margin-top: 40rpx;
  background: linear-gradient(135deg, #3B7CFF 0%, #6BA1FF 40%, #4A8DFF 100%);
  border-radius: 50rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 48rpx rgba(59, 124, 255, 0.45);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 12rpx 36rpx rgba(59, 124, 255, 0.38);
  }
}

.btn-text {
  font-size: 34rpx;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: 8rpx;
  position: relative;
  z-index: 1;
}

.btn-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.25), transparent);
  transition: left 0.6s ease;

  .login-btn:active & {
    left: 100%;
  }
}

.login-footer {
  margin-top: 48rpx;
  text-align: center;
}

.footer-text {
  font-size: 22rpx;
  color: #999999;
  opacity: 0.5;
}
</style>
