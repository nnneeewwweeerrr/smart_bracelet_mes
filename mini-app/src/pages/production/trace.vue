<template>
  <view class="trace-page">
    <view class="search-bar">
      <u-search v-model="searchKey" :placeholder="mode === 'reverse' ? '输入工单号' : '输入批次号'" @search="doSearch" @custom="doSearch" @clear="doSearch" shape="round"></u-search>
    </view>

    <view class="mode-switch">
      <u-tabs :list="modeList" :current="modeIndex" @click="onModeChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="trace-result" v-if="timeline.length > 0">
      <view class="result-header">
        <text class="result-title">{{ mode === 'reverse' ? '工单履历' : '正向追溯结果' }}</text>
        <text class="result-key">{{ searchKey }}</text>
      </view>

      <view class="timeline">
        <view class="timeline-item" v-for="(item, idx) in timeline" :key="idx">
          <view class="timeline-left">
            <view class="timeline-dot" :class="getDotClass(item.result)"></view>
            <view class="timeline-line" v-if="idx < timeline.length - 1"></view>
          </view>
          <view class="timeline-content">
            <view class="content-header">
              <text class="content-process">{{ item.processName }}</text>
              <u-tag :text="item.resultText" :type="item.resultTag" size="mini"></u-tag>
            </view>
            <text class="content-time">{{ item.executeTime }}</text>
            <view class="content-detail" v-if="item.detail">
              <text class="detail-text">{{ item.detail }}</text>
            </view>
            <view class="content-related" v-if="item.relatedWorkNo">
              <text class="related-label">关联工单:</text>
              <text class="related-value" @click="goDetail(item.relatedWorkNo)">{{ item.relatedWorkNo }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="searched && timeline.length === 0" style="padding-top: 80rpx;">
      <u-empty mode="search" text="未找到追溯数据"></u-empty>
    </view>

    <view v-if="!searched" style="padding-top: 120rpx;">
      <u-empty mode="search" text="输入工单号或批次号开始追溯"></u-empty>
    </view>
  </view>
</template>

<script>
import { getReverseTrace, getForwardTrace } from '@/api/production'

export default {
  data() {
    return {
      mode: 'reverse',
      modeIndex: 0,
      modeList: [
        { name: '反向追溯(工单)', value: 'reverse' },
        { name: '正向追溯(批次)', value: 'forward' }
      ],
      searchKey: '',
      searched: false,
      timeline: []
    }
  },
  onLoad(options) {
    if (options.workNo) {
      this.searchKey = options.workNo
      this.doSearch()
    }
  },
  methods: {
    onModeChange({ index }) {
      this.modeIndex = index
      this.mode = this.modeList[index].value
      this.searchKey = ''
      this.timeline = []
      this.searched = false
    },
    async doSearch() {
      if (!this.searchKey.trim()) {
        this.timeline = []
        this.searched = false
        return
      }

      try {
        let res
        if (this.mode === 'reverse') {
          res = await getReverseTrace(this.searchKey.trim())
        } else {
          res = await getForwardTrace(this.searchKey.trim())
        }
        this.timeline = (res || []).map(item => ({
          ...item,
          resultText: this.getResultText(item.result),
          resultTag: this.getResultTag(item.result)
        }))
        this.searched = true
      } catch {
        this.timeline = []
        this.searched = true
      }
    },
    getResultText(r) {
      const m = { pass: '合格', fail: '不合格', pending: '待检', running: '进行中' }
      return m[r] || r || '未知'
    },
    getResultTag(r) {
      const m = { pass: 'success', fail: 'error', pending: 'warning', running: 'primary' }
      return m[r] || 'info'
    },
    getDotClass(r) {
      const m = { pass: 'dot-success', fail: 'dot-error', pending: 'dot-warn', running: 'dot-primary' }
      return m[r] || 'dot-default'
    },
    goDetail(workNo) {
      uni.navigateTo({
        url: '/pages/production/trace?workNo=' + workNo + '&mode=reverse'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.trace-page {
  min-height: 100vh;
  background: transparent;
}

.search-bar,
.mode-switch {
  padding: 20rpx 24rpx;
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.mode-switch {
  margin-bottom: 18rpx;
}

.trace-result {
  padding: 0 24rpx;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.result-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.result-key {
  font-size: 26rpx;
  color: #3B7CFF;
}

.timeline {
  padding-left: 10rpx;
}

.timeline-item {
  display: flex;
  min-height: 160rpx;
}

.timeline-left {
  width: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.timeline-dot {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  margin-top: 8rpx;
  flex-shrink: 0;

  &.dot-success { background: #52C41A; }
  &.dot-error { background: #FF4D4F; }
  &.dot-warn { background: #FAAD14; }
  &.dot-primary { background: #3B7CFF; }
  &.dot-default { background: #BFBFBF; }
}

.timeline-line {
  width: 4rpx;
  flex: 1;
  background: #E0E0E0;
  margin: 4rpx 0;
}

.timeline-content {
  flex: 1;
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx 30rpx;
  margin-left: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.content-process {
  font-size: 30rpx;
  color: #1F1F1F;
  font-weight: 500;
}

.content-time {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 10rpx;
}

.content-detail {
  margin-bottom: 8rpx;
}

.detail-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.5;
}

.content-related {
  display: flex;
  align-items: center;
}

.related-label {
  font-size: 24rpx;
  color: #8C8C8C;
  margin-right: 8rpx;
}

.related-value {
  font-size: 24rpx;
  color: #3B7CFF;
  text-decoration: underline;
}
</style>