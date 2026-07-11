<template>
  <view class="login-page">
    <view class="login-bg">
      <view class="bg-circle c1"></view>
      <view class="bg-circle c2"></view>
      <view class="bg-circle c3"></view>
    </view>

    <view class="login-header">
      <view class="logo-wrap">
        <image class="login-logo" src="/static/logo.png" mode="aspectFit" />
      </view>
      <text class="login-title">智能手环MES系统</text>
      <text class="login-subtitle">Manufacturing Execution System</text>
    </view>

    <view class="login-form">
      <view class="form-title">账号登录</view>
      <u-form :model="form" ref="uFormRef">
        <u-form-item prop="userName" :borderBottom="true">
          <u-input
            v-model="form.userName"
            placeholder="请输入用户名"
            prefixIcon="account"
            prefixIconStyle="font-size: 22px;color: #8C8C8C"
            clearable
            :customStyle="{ fontSize: '30rpx' }"
          />
        </u-form-item>
        <u-form-item prop="password" :borderBottom="true">
          <u-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefixIcon="lock"
            prefixIconStyle="font-size: 22px;color: #8C8C8C"
            clearable
            :customStyle="{ fontSize: '30rpx' }"
          />
        </u-form-item>
      </u-form>

      <u-button
        type="primary"
        :loading="loading"
        :disabled="loading"
        @click="handleLogin"
        customStyle="margin-top: 60rpx; height: 96rpx; border-radius: 48rpx; font-size: 32rpx; font-weight: 600; background: linear-gradient(135deg, #3B7CFF, #6BA1FF); box-shadow: 0 8rpx 24rpx rgba(59, 124, 255, 0.35);"
      >
        登 录
      </u-button>
    </view>

    <view class="login-footer">
      <text class="footer-text">智能手环MES v1.0.0</text>
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
      loading: false
    }
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
        // error handled in request
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
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-xxl;
  background: linear-gradient(180deg, #EEF4FF 0%, #F5F8FF 50%, #F8FAFF 100%);
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

.bg-circle {
  position: absolute;
  border-radius: 50%;

  &.c1 {
    width: 640rpx;
    height: 640rpx;
    background: radial-gradient(circle, rgba(59, 124, 255, 0.15) 0%, rgba(107, 161, 255, 0.05) 70%);
    top: -240rpx;
    right: -180rpx;
    animation: float 6s ease-in-out infinite;
  }

  &.c2 {
    width: 440rpx;
    height: 440rpx;
    background: radial-gradient(circle, rgba(82, 196, 26, 0.08) 0%, rgba(82, 196, 26, 0.02) 70%);
    bottom: 180rpx;
    left: -120rpx;
    animation: float 8s ease-in-out infinite reverse;
  }

  &.c3 {
    width: 360rpx;
    height: 360rpx;
    background: radial-gradient(circle, rgba(250, 173, 20, 0.06) 0%, rgba(250, 173, 20, 0.01) 70%);
    bottom: -100rpx;
    right: -80rpx;
    animation: float 7s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-30rpx) scale(1.05);
  }
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 90rpx;
  z-index: 1;
}

.logo-wrap {
  width: 150rpx;
  height: 150rpx;
  border-radius: $radius-xl;
  background: $brand-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
  box-shadow: 0 16rpx 40rpx rgba(59, 124, 255, 0.35);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: -8rpx;
    left: -8rpx;
    right: -8rpx;
    bottom: -8rpx;
    border-radius: $radius-xl;
    background: linear-gradient(45deg, rgba(255, 255, 255, 0.4), transparent);
  }
}

.login-logo {
  width: 90rpx;
  height: 90rpx;
  position: relative;
  z-index: 1;
}

.login-title {
  font-size: $font-size-display;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-sm;
  letter-spacing: 4rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.login-subtitle {
  font-size: $font-size-sm;
  color: $text-tertiary;
  letter-spacing: 2rpx;
  font-weight: 400;
}

.login-form {
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: $radius-card;
  padding: $spacing-xxl $spacing-xl;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.08);
  z-index: 1;
  border: 1rpx solid rgba(255, 255, 255, 0.5);
}

.form-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xxl;
  text-align: center;
}

.login-footer {
  position: fixed;
  bottom: $spacing-xxl;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 1;
}

.footer-text {
  font-size: $font-size-xs;
  color: $text-tertiary;
  opacity: 0.7;
}
</style>
