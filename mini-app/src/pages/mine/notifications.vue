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
      notifications: [
        {
          id: 1,
          type: 'exception',
          title: '异常预警：屏幕划痕不良',
          description: '发现1件屏幕划痕不良品，SN码：SN20240115001，请及时处理',
          time: '2分钟前',
          source: '质检系统',
          read: false,
          badge: 1
        },
        {
          id: 2,
          type: 'workorder',
          title: '工单下发：WO20240115002',
          description: '新工单已下发至A线，产品：智能运动手环A1，数量：500件，请及时安排生产',
          time: '15分钟前',
          source: '生产计划',
          read: false,
          badge: null
        },
        {
          id: 3,
          type: 'quality',
          title: '质量预警：心率传感器合格率下降',
          description: '心率传感器测试合格率下降至85%，低于目标值95%，请排查原因',
          time: '30分钟前',
          source: '质检看板',
          read: false,
          badge: 3
        },
        {
          id: 4,
          type: 'stock',
          title: '库存预警：电池物料不足',
          description: '锂电池BAT-380库存不足500件，建议及时补货',
          time: '1小时前',
          source: '仓储系统',
          read: true,
          badge: null
        },
        {
          id: 5,
          type: 'workorder',
          title: '工单进度提醒：WO20240115001',
          description: '工单WO20240115001进度已达80%，预计今日完成，请做好收尾工作',
          time: '2小时前',
          source: '生产监控',
          read: true,
          badge: null
        },
        {
          id: 6,
          type: 'exception',
          title: '异常处理通知：已闭环',
          description: '您上报的蓝牙连接异常（SN20240115003）已维修完成并复测合格',
          time: '3小时前',
          source: '维修系统',
          read: true,
          badge: null
        },
        {
          id: 7,
          type: 'quality',
          title: '抽检任务：请执行IQC检验',
          description: '新到物料IC-MCU-001批次IC20240115需进行IQC来料检验',
          time: '4小时前',
          source: '质检任务',
          read: false,
          badge: 1
        },
        {
          id: 8,
          type: 'workorder',
          title: '报工提醒：今日报工未完成',
          description: '您今日还有3笔报工记录未提交，请及时完成',
          time: '5小时前',
          source: '报工系统',
          read: true,
          badge: null
        },
        {
          id: 9,
          type: 'quality',
          title: '功能测试任务：待测试50件',
          description: '待功能测试产品50件，请尽快完成测试录入',
          time: '6小时前',
          source: '测试系统',
          read: false,
          badge: 50
        },
        {
          id: 10,
          type: 'stock',
          title: '物料入库通知',
          description: '屏幕模组LCD-001批次LCD20240115已入库，数量：1000件',
          time: '昨天',
          source: '仓储系统',
          read: true,
          badge: null
        }
      ]
    }
  },
  computed: {
    filteredNotifications() {
      if (this.currentTab === 0) return this.notifications
      const type = this.tabList[this.currentTab].value
      const typeMap = {
        workorder: ['workorder'],
        exception: ['exception'],
        quality: ['quality']
      }
      return this.notifications.filter(n => typeMap[type]?.includes(n.type))
    },
    unreadCount() {
      return this.notifications.filter(n => !n.read).length
    }
  },
  methods: {
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
    handleNotificationClick(item) {
      item.read = true
      item.badge = null
      uni.showToast({
        title: `已查看：${item.title}`,
        icon: 'none',
        duration: 1500
      })
    },
    markAllRead() {
      this.notifications.forEach(n => {
        n.read = true
        n.badge = null
      })
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