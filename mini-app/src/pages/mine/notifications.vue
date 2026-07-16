<template>
  <view class="notifications-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">消息通知</text>
          <text class="page-desc">产线预警、任务提醒、质量通知</text>
        </view>
        <view class="header-actions">
          <view class="mark-read-btn" @click="markAllRead">
            <text class="mark-text">全部已读</text>
          </view>
        </view>
      </view>

      <view class="filter-tabs">
        <u-tabs :list="tabList" :current="currentTab" @click="onTabChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="40"></u-tabs>
      </view>

      <view class="notifications-list">
        <view 
          class="notification-card" 
          v-for="(item, idx) in filteredNotifications" 
          :key="idx"
          :class="{ unread: !item.read }"
          @click="handleNotificationClick(item)"
        >
          <view class="notification-icon" :class="item.type">
            <text class="icon-text">{{ getItemIcon(item.type) }}</text>
          </view>
          <view class="notification-content">
            <view class="content-header">
              <text class="notification-title">{{ item.title }}</text>
              <view class="badge" v-if="item.badge">{{ item.badge }}</view>
            </view>
            <text class="notification-desc">{{ item.description }}</text>
            <view class="content-footer">
              <text class="notification-time">{{ item.time }}</text>
              <text class="notification-source">{{ item.source }}</text>
            </view>
          </view>
          <view class="notification-arrow">
            <text class="arrow-icon">›</text>
          </view>
        </view>
      </view>

      <view v-if="filteredNotifications.length === 0" style="padding-top: 120rpx;">
        <view class="empty-state">
          <view class="empty-icon">🔔</view>
          <text class="empty-title">暂无通知</text>
          <text class="empty-desc">{{ currentTab === 0 ? '您还没有收到任何通知' : '该分类下暂无通知' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { getNotificationList, markNotificationAsRead, markAllNotificationsAsRead } from '@/api/user'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      currentTab: 0,
      tabList: [
        { name: '全部', value: 'all' },
        { name: '工单', value: 'workorder' },
        { name: '异常', value: 'exception' },
        { name: '质量', value: 'quality' },
        { name: '仓储', value: 'stock' }
      ],
      notifications: []
    }
  },
  computed: {
    filteredNotifications() {
      if (this.currentTab === 0) return this.notifications
      const type = this.tabList[this.currentTab].value
      return this.notifications.filter(n => n.type === type)
    },
    unreadCount() {
      return this.notifications.filter(n => !n.read).length
    }
  },
  onShow() {
    this.loadNotifications()
  },
  methods: {
    async loadNotifications() {
      try {
        this.notifications = await getNotificationList() || []
      } catch {
        this.notifications = []
      }
    },
    onTabChange({ index }) {
      this.currentTab = index
    },
    getItemIcon(type) {
      const icons = {
        exception: '⚠',
        workorder: '📋',
        quality: '✅',
        stock: '📦'
      }
      return icons[type] || '📌'
    },
    async handleNotificationClick(item) {
      item.read = true
      item.badge = null
      try {
        await markNotificationAsRead(item.id)
      } catch { /* ignore */ }
      this.$emit('update-count', this.unreadCount)
      uni.$emit('notificationUpdate')
    },
    async markAllRead() {
      this.notifications.forEach(n => {
        n.read = true
        n.badge = null
      })
      try {
        await markAllNotificationsAsRead()
      } catch { /* ignore */ }
      this.$emit('update-count', 0)
      uni.$emit('notificationUpdate')
      uni.showToast({ title: '已全部标记为已读', icon: 'success' })
    }
  }
}
</script>

<style lang="scss" scoped>
.notifications-page {
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.header-actions {
  position: relative;
  z-index: 1;
}

.mark-read-btn {
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 24rpx;
}

.mark-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}

.filter-tabs {
  margin: 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.notifications-list {
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.notification-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  border-left: 6rpx solid transparent;

  &.unread {
    border-left-color: #3B7CFF;
    background: linear-gradient(145deg, #FFFFFF 0%, #F0F7FF 100%);

    .notification-title {
      font-weight: 600;
    }
  }

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }
}

.notification-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;

  &.exception { background: rgba(255, 77, 79, 0.1); }
  &.workorder { background: rgba(59, 124, 255, 0.1); }
  &.quality { background: rgba(82, 196, 26, 0.1); }
  &.stock { background: rgba(250, 173, 20, 0.1); }
}

.icon-text {
  font-size: 36rpx;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.notification-title {
  font-size: 28rpx;
  color: #1F1F1F;
  font-weight: 500;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge {
  background: #FF4D4F;
  color: #ffffff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.notification-desc {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  line-height: 1.5;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.content-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-time {
  font-size: 22rpx;
  color: #BFBFBF;
}

.notification-source {
  font-size: 22rpx;
  color: #3B7CFF;
}

.notification-arrow {
  margin-left: 16rpx;
}

.arrow-icon {
  font-size: 32rpx;
  color: #CCCCCC;
}

.empty-state {
  text-align: center;
  padding: 80rpx 40rpx;
}

.empty-icon {
  font-size: 96rpx;
  display: block;
  margin-bottom: 32rpx;
}

.empty-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #8C8C8C;
  display: block;
  line-height: 1.6;
}
</style>