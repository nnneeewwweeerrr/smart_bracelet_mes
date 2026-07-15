<template>
  <view class="settings-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">系统设置</text>
          <text class="page-desc">个性化配置与系统管理</text>
        </view>
      </view>

      <view class="settings-card">
        <view class="card-header">
          <text class="card-title">通知设置</text>
        </view>
        <view class="setting-item">
          <view class="item-info">
            <text class="item-title">消息通知</text>
            <text class="item-desc">接收工单、异常、质量等消息推送</text>
          </view>
          <u-switch v-model="notificationEnabled" activeColor="#3B7CFF" inactiveColor="#E0E0E0"></u-switch>
        </view>
        <view class="setting-item">
          <view class="item-info">
            <text class="item-title">声音提醒</text>
            <text class="item-desc">收到新消息时播放提示音</text>
          </view>
          <u-switch v-model="soundEnabled" activeColor="#3B7CFF" inactiveColor="#E0E0E0"></u-switch>
        </view>
        <view class="setting-item">
          <view class="item-info">
            <text class="item-title">震动提醒</text>
            <text class="item-desc">收到重要消息时震动提醒</text>
          </view>
          <u-switch v-model="vibrationEnabled" activeColor="#3B7CFF" inactiveColor="#E0E0E0"></u-switch>
        </view>
      </view>

      <view class="settings-card">
        <view class="card-header">
          <text class="card-title">数据设置</text>
        </view>
        <view class="setting-item" @click="clearCache">
          <view class="item-info">
            <text class="item-title">清除缓存</text>
            <text class="item-desc">清理本地缓存数据</text>
          </view>
          <text class="item-value">{{ cacheSize }}</text>
          <text class="item-arrow">›</text>
        </view>
        <view class="setting-item" @click="syncData">
          <view class="item-info">
            <text class="item-title">手动同步</text>
            <text class="item-desc">立即同步离线数据到服务器</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
      </view>

      <view class="settings-card">
        <view class="card-header">
          <text class="card-title">关于系统</text>
        </view>
        <view class="setting-item">
          <view class="item-info">
            <text class="item-title">版本号</text>
            <text class="item-desc">智能手环MES系统小程序端</text>
          </view>
          <text class="item-value">v1.0.0</text>
        </view>
        <view class="setting-item" @click="checkUpdate">
          <view class="item-info">
            <text class="item-title">检查更新</text>
            <text class="item-desc">检查是否有新版本可用</text>
          </view>
          <text class="item-arrow">›</text>
        </view>
      </view>

      <view class="footer-info">
        <text class="footer-text">© 2024 智能手环MES系统</text>
        <text class="footer-version">Version 1.0.0</text>
      </view>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      notificationEnabled: true,
      soundEnabled: true,
      vibrationEnabled: true,
      cacheSize: '2.4MB'
    }
  },
  methods: {
    clearCache() {
      uni.showModal({
        title: '确认清除',
        content: '确定要清除本地缓存数据吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '清除中...' })
            setTimeout(() => {
              uni.hideLoading()
              this.cacheSize = '0MB'
              uni.showToast({ title: '清除成功', icon: 'success' })
            }, 1000)
          }
        }
      })
    },
    syncData() {
      uni.showLoading({ title: '同步中...' })
      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({ title: '同步完成', icon: 'success' })
      }, 1500)
    },
    checkUpdate() {
      uni.showLoading({ title: '检查中...' })
      setTimeout(() => {
        uni.hideLoading()
        uni.showModal({
          title: '检查结果',
          content: '当前已是最新版本',
          showCancel: false
        })
      }, 1000)
    }
  }
}
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background: #F5F7FA;
  padding-bottom: 120rpx;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.page-header {
  padding: 40rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 40%, #7CB7FF 80%, #9DC8FF 100%);
  border-radius: 0 0 48rpx 48rpx;
  position: relative;
  overflow: hidden;
}

.page-title-wrap {
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
  margin-bottom: 8rpx;
}

.page-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.settings-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  margin: 24rpx;
  border-radius: 28rpx;
  padding: 4rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
}

.card-header {
  padding: 28rpx 32rpx 16rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #F2F2F2;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #F8FAFF;
  }
}

.item-info {
  flex: 1;
}

.item-title {
  font-size: 28rpx;
  color: #1F1F1F;
  font-weight: 500;
  display: block;
  margin-bottom: 6rpx;
}

.item-desc {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
}

.item-value {
  font-size: 26rpx;
  color: #3B7CFF;
  font-weight: 500;
  margin-right: 12rpx;
}

.item-arrow {
  font-size: 32rpx;
  color: #BFBFBF;
}

.footer-info {
  text-align: center;
  padding: 48rpx 32rpx;
}

.footer-text {
  font-size: 24rpx;
  color: #BFBFBF;
  display: block;
  margin-bottom: 8rpx;
}

.footer-version {
  font-size: 22rpx;
  color: #CCCCCC;
}
</style>
