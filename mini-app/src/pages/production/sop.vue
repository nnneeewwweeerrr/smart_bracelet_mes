<template>
  <view class="sop-page">
    
    <view class="header-section">
      <view class="search-bar">
        <u-search v-model="keyword" placeholder="搜索工序名称" @search="doSearch" @custom="doSearch" @clear="doSearch"></u-search>
      </view>
      
      <view class="scan-btn" @click="handleScan">
        <view class="scan-icon-wrap">
          <text class="scan-icon">扫</text>
        </view>
        <text class="scan-text">扫工位码</text>
      </view>
      <view class="input-btn" @click="showInputModal = true">
        <view class="input-icon-wrap">
          <text class="input-icon">输</text>
        </view>
        <text class="input-text">输工位码</text>
      </view>
    </view>

    <view class="station-info" v-if="currentStation">
      <view class="station-card">
        <view class="station-header">
          <view class="station-dot"></view>
          <text class="station-title">{{ currentStation.name }}</text>
        </view>
        <view class="station-detail">
          <view class="station-item">
            <text class="station-label">工位编码</text>
            <text class="station-value">{{ currentStation.code }}</text>
          </view>
          <view class="station-item">
            <text class="station-label">当前工序</text>
            <text class="station-value">{{ currentStation.process }}</text>
          </view>
          <view class="station-item">
            <text class="station-label">作业人员</text>
            <text class="station-value">{{ currentStation.operator }}</text>
          </view>
        </view>
        <view class="station-actions">
          <u-button type="primary" size="small" text="查看SOP" @click="viewCurrentStationSop"></u-button>
          <u-button type="info" size="small" text="去报工" @click="goReport"></u-button>
        </view>
      </view>
    </view>

    <view class="sop-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无SOP文档"></u-empty>
      </view>

      <view class="sop-card" v-for="item in list" :key="item.id" @click="viewSop(item)">
        <view class="sop-header">
          <view class="sop-icon-wrap">
            <text class="sop-icon">{{ item.icon || '工' }}</text>
          </view>
          <view class="sop-title-area">
            <text class="sop-name">{{ item.processName }}</text>
            <text class="sop-code">{{ item.processCode }}</text>
          </view>
          <view class="sop-arrow">
            <text class="arrow-icon">></text>
          </view>
        </view>
        <view class="sop-info">
          <text class="sop-product">{{ item.productName || '-' }}</text>
          <text class="sop-sort">工序顺序: {{ item.sortOrder }}</text>
        </view>
      </view>
    </view>

    <u-popup v-if="showDetail" :show="showDetail" mode="bottom" round="24" @close="showDetail = false" closeable>
      <view class="sop-detail">
        <view class="detail-header">
          <text class="detail-title">{{ currentSop.processName }}</text>
          <text class="detail-code">{{ currentSop.processCode }}</text>
        </view>

        <view class="detail-nav">
          <view 
            v-for="tab in detailTabs" 
            :key="tab.key"
            class="nav-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <text class="nav-text">{{ tab.label }}</text>
          </view>
        </view>

        <scroll-view scroll-y class="detail-content">
          <view v-if="activeTab === 'steps'" class="steps-section">
            <view class="steps-header">
              <text class="steps-title">作业步骤</text>
              <text class="steps-count">共 {{ currentSop.steps.length }} 步</text>
            </view>
            <view class="steps-list">
              <view class="step-card" v-for="(step, idx) in currentSop.steps" :key="idx">
                <view class="step-header">
                  <view class="step-no-wrap" :class="{ completed: idx < completedSteps }">
                    <text class="step-no">{{ idx + 1 }}</text>
                  </view>
                  <text class="step-title">{{ step.title }}</text>
                  <view class="step-status" v-if="idx < completedSteps">
                    <text class="status-icon">✓</text>
                  </view>
                </view>
                <text class="step-desc">{{ step.description }}</text>
                <view class="step-image" v-if="step.image">
                  <image :src="step.image" mode="widthFix" @click="previewImage(step.image)"></image>
                </view>
                <view class="step-video" v-if="step.video">
                  <video 
                    :src="step.video" 
                    :poster="step.poster || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Video%20thumbnail%20for%20smart%20watch%20production%20tutorial&image_size=landscape_16_9'"
                    controls 
                    class="video-player"
                    @click="playVideo(step.video)"
                  ></video>
                </view>
              </view>
            </view>
          </view>

          <view v-if="activeTab === 'params'" class="params-section">
            <view class="params-header">
              <text class="params-title">工艺参数标准</text>
            </view>
            <view class="params-list">
              <view class="param-card" v-for="(param, idx) in currentSop.params" :key="idx">
                <view class="param-header">
                  <text class="param-name">{{ param.name }}</text>
                  <text class="param-value" :class="param.status">{{ param.value }}</text>
                </view>
                <view class="param-range">
                  <text class="range-text">标准范围: {{ param.min }} - {{ param.max }} {{ param.unit }}</text>
                  <view class="range-bar">
                    <view class="range-fill" :style="{ width: param.percent + '%' }" :class="param.status"></view>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view v-if="activeTab === 'notes'" class="notes-section">
            <view class="notes-header">
              <text class="notes-title">注意事项</text>
            </view>
            <view class="notes-list">
              <view class="note-card" v-for="(note, idx) in currentSop.notes" :key="idx">
                <view class="note-icon-wrap" :class="note.type">
                  <text class="note-icon">{{ note.icon }}</text>
                </view>
                <text class="note-text">{{ note.text }}</text>
              </view>
            </view>
          </view>

          <view v-if="activeTab === 'tools'" class="tools-section">
            <view class="tools-header">
              <text class="tools-title">工具清单</text>
            </view>
            <view class="tools-list">
              <view class="tool-item" v-for="(tool, idx) in currentSop.tools" :key="idx">
                <view class="tool-check">
                  <view class="check-box" :class="{ checked: tool.checked }">
                    <text class="check-icon" v-if="tool.checked">✓</text>
                  </view>
                </view>
                <text class="tool-name">{{ tool.name }}</text>
                <text class="tool-count">x{{ tool.count }}</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="detail-footer">
          <u-button type="primary" text="开始作业" @click="goReport"></u-button>
        </view>
      </view>
    </u-popup>

    <u-popup v-if="showImagePreview" :show="showImagePreview" @close="showImagePreview = false">
      <image :src="previewUrl" mode="aspectFit" class="preview-image"></image>
    </u-popup>

    <u-popup v-if="showInputModal" :show="showInputModal" mode="center" round="24" @close="showInputModal = false">
      <view class="input-modal">
        <view class="modal-header">
          <text class="modal-title">输入工位码</text>
        </view>
        <view class="modal-body">
          <text class="modal-hint">请输入工位编码，如 STATION0001</text>
          <u-input v-model="inputStationCode" placeholder="例如: STATION0001" @confirm="confirmInput"></u-input>
          <view class="quick-tags">
            <view 
              class="tag-item" 
              v-for="tag in quickTags" 
              :key="tag" 
              @click="selectTag(tag)"
            >
              <text class="tag-text">{{ tag }}</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <u-button type="default" text="取消" @click="showInputModal = false"></u-button>
          <u-button type="primary" text="确定" @click="confirmInput"></u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { getProcessList, scanSop, getSopDetail } from '@/api/production'
import { scanCode } from '@/utils/scan'

export default {
  data() {
    return {
      keyword: '',
      list: [
        { id: 1, processName: 'SMT贴片', processCode: 'PROC001', sortOrder: 1, productName: '智能运动手环A1', icon: '贴' },
        { id: 2, processName: '焊接组装', processCode: 'PROC002', sortOrder: 2, productName: '智能运动手环A1', icon: '焊' },
        { id: 3, processName: '固件烧录', processCode: 'PROC003', sortOrder: 3, productName: '智能运动手环A1', icon: '烧' },
        { id: 4, processName: '功能测试', processCode: 'PROC004', sortOrder: 4, productName: '智能运动手环A1', icon: '测' },
        { id: 5, processName: '外观检查', processCode: 'PROC005', sortOrder: 5, productName: '智能运动手环A1', icon: '检' },
        { id: 6, processName: '包装入库', processCode: 'PROC006', sortOrder: 6, productName: '智能运动手环A1', icon: '包' }
      ],
      showDetail: false,
      showImagePreview: false,
      showInputModal: false,
      previewUrl: '',
      inputStationCode: '',
      currentSop: {},
      currentStation: null,
      completedSteps: 0,
      activeTab: 'steps',
      detailTabs: [
        { key: 'steps', label: '作业步骤' },
        { key: 'params', label: '工艺参数' },
        { key: 'notes', label: '注意事项' },
        { key: 'tools', label: '工具清单' }
      ],
      quickTags: ['STATION0001', 'STATION0002', 'STATION0003', 'STATION0004', 'STATION0005', 'STATION0006']
    }
  },
  onLoad(options) {
    console.log('SOP页面已加载')
    console.log('列表数据:', this.list)
    if (options && options.stationCode) {
      this.loadStationInfo(options.stationCode)
    }
  },
  methods: {
    async doSearch() {
      try {
        const res = await getProcessList({ keyword: this.keyword })
        const records = (res && res.records) || res || []
        if (records.length > 0) {
          this.list = records
        } else {
          this.list = this.getDefaultList()
        }
      } catch {
        this.list = this.getDefaultList()
      }
    },
    getDefaultList() {
      return [
        { id: 1, processName: 'SMT贴片', processCode: 'PROC001', sortOrder: 1, productName: '智能运动手环A1', icon: '贴' },
        { id: 2, processName: '焊接组装', processCode: 'PROC002', sortOrder: 2, productName: '智能运动手环A1', icon: '焊' },
        { id: 3, processName: '固件烧录', processCode: 'PROC003', sortOrder: 3, productName: '智能运动手环A1', icon: '烧' },
        { id: 4, processName: '功能测试', processCode: 'PROC004', sortOrder: 4, productName: '智能运动手环A1', icon: '测' },
        { id: 5, processName: '外观检查', processCode: 'PROC005', sortOrder: 5, productName: '智能运动手环A1', icon: '检' },
        { id: 6, processName: '包装入库', processCode: 'PROC006', sortOrder: 6, productName: '智能运动手环A1', icon: '包' }
      ]
    },
    async handleScan() {
      try {
        const code = await scanCode()
        await this.loadStationInfo(code)
      } catch {
      }
    },
    async loadStationInfo(stationCode) {
      uni.showLoading({ title: '加载工位信息...' })
      try {
        const res = await scanSop(stationCode)
        if (res) {
          this.currentStation = res
          uni.showToast({ title: '已匹配工位: ' + this.currentStation.name, icon: 'success' })
        } else {
          uni.showToast({ title: '未找到对应工位', icon: 'none' })
          this.currentStation = null
        }
      } catch {
        uni.showToast({ title: '加载工位信息失败', icon: 'none' })
        this.currentStation = null
      } finally {
        uni.hideLoading()
      }
    },
    viewCurrentStationSop() {
      if (!this.currentStation) return
      
      const processName = this.currentStation.process
      const item = this.list.find(i => i.processName === processName)
      if (item) {
        this.viewSop(item)
      }
    },
    async viewSop(item) {
      uni.showLoading({ title: '加载SOP详情...' })
      try {
        const res = await getSopDetail(item.id)
        if (res) {
          this.currentSop = {
            ...item,
            steps: res.steps || [],
            params: res.params || [],
            notes: res.notes || [],
            tools: res.tools || []
          }
          this.completedSteps = 0
          this.activeTab = 'steps'
          this.showDetail = true
        } else {
          uni.showToast({ title: '未找到SOP详情', icon: 'none' })
        }
      } catch {
        uni.showToast({ title: '加载SOP详情失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    previewImage(url) {
      this.previewUrl = url
      this.showImagePreview = true
    },
    playVideo(url) {
      uni.createVideoContext('video-' + Date.now()).play()
    },
    goReport() {
      uni.navigateTo({ url: '/pages/production/report' })
    },
    selectTag(tag) {
      this.inputStationCode = tag
    },
    async confirmInput() {
      if (!this.inputStationCode.trim()) {
        uni.showToast({ title: '请输入工位码', icon: 'none' })
        return
      }
      this.showInputModal = false
      await this.loadStationInfo(this.inputStationCode.trim())
    }
  }
}
</script>

<style lang="scss" scoped>
.sop-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F6FF 0%, #F0F7FF 50%, #F5F8FF 100%);
}

.header-section {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  position: relative;
  z-index: 10;
}

.search-bar {
  flex: 1;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.scan-btn {
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9BFF 100%);
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(59, 124, 255, 0.4);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 12rpx rgba(59, 124, 255, 0.3);
  }
}

.input-btn {
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(82, 196, 26, 0.4);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
  }
}

.scan-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-icon {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
}

.scan-text {
  font-size: 20rpx;
  color: #ffffff;
  margin-top: 6rpx;
}

.input-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-icon {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
}

.input-text {
  font-size: 20rpx;
  color: #ffffff;
  margin-top: 6rpx;
}

.station-info {
  padding: 0 24rpx 20rpx;
  position: relative;
  z-index: 10;
}

.station-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  padding: 28rpx;
  border-left: 6rpx solid #3B7CFF;
}

.station-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.station-dot {
  width: 16rpx;
  height: 16rpx;
  background: #52C41A;
  border-radius: 50%;
  margin-right: 12rpx;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.station-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.station-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.station-item {
  flex: 1;
  min-width: 30%;
}

.station-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 4rpx;
}

.station-value {
  font-size: 26rpx;
  color: #1F1F1F;
  font-weight: 500;
}

.station-actions {
  display: flex;
  gap: 16rpx;
}

.sop-list {
  padding: 16rpx 24rpx;
  position: relative;
  z-index: 10;
}

.sop-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  padding: 24rpx;
  margin-bottom: 16rpx;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.sop-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.sop-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9BFF 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.sop-icon {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 600;
}

.sop-title-area {
  flex: 1;
}

.sop-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.sop-code {
  font-size: 22rpx;
  color: #3B7CFF;
  margin-top: 4rpx;
}

.sop-arrow {
  width: 48rpx;
  height: 48rpx;
  background: rgba(59, 124, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  font-size: 28rpx;
  color: #3B7CFF;
}

.sop-info {
  display: flex;
  gap: 24rpx;
}

.sop-product {
  font-size: 24rpx;
  color: #4A4A4A;
}

.sop-sort {
  font-size: 24rpx;
  color: #8C8C8C;
}

.sop-detail {
  padding: 32rpx;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.detail-header {
  text-align: center;
  margin-bottom: 28rpx;
}

.detail-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
}

.detail-code {
  font-size: 24rpx;
  color: #3B7CFF;
  margin-top: 8rpx;
}

.detail-nav {
  display: flex;
  background: #F5F7FA;
  border-radius: 16rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
}

.nav-item {
  flex: 1;
  padding: 16rpx 0;
  text-align: center;
  border-radius: 12rpx;
  transition: all 0.3s ease;

  &.active {
    background: #FFFFFF;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }
}

.nav-text {
  font-size: 26rpx;
  color: #8C8C8C;

  .active & {
    color: #3B7CFF;
    font-weight: 500;
  }
}

.detail-content {
  flex: 1;
  overflow-y: auto;
}

.steps-section,
.params-section,
.notes-section,
.tools-section {
  padding-bottom: 20rpx;
}

.steps-header,
.params-header,
.notes-header,
.tools-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.steps-title,
.params-title,
.notes-title,
.tools-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.steps-count {
  font-size: 24rpx;
  color: #8C8C8C;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.step-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  padding: 24rpx;
}

.step-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.step-no-wrap {
  width: 48rpx;
  height: 48rpx;
  background: #E8EEF7;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
  transition: all 0.3s ease;

  &.completed {
    background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  }
}

.step-no {
  font-size: 24rpx;
  font-weight: 600;
  color: #3B7CFF;

  .completed & {
    color: #ffffff;
  }
}

.step-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: #1F1F1F;
}

.step-status {
  width: 36rpx;
  height: 36rpx;
  background: #52C41A;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-icon {
  font-size: 20rpx;
  color: #ffffff;
}

.step-desc {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.step-image image {
  width: 100%;
  border-radius: 12rpx;
}

.step-video {
  margin-top: 16rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.video-player {
  width: 100%;
  height: 360rpx;
}

.video-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.video-icon {
  font-size: 36rpx;
  color: #3B7CFF;
}

.video-text {
  font-size: 26rpx;
  color: #4A4A4A;
}

.params-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.param-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  padding: 20rpx 24rpx;
}

.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.param-name {
  font-size: 26rpx;
  color: #4A4A4A;
}

.param-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #52C41A;

  &.warning { color: #FAAD14; }
  &.error { color: #FF4D4F; }
}

.param-range {
  margin-top: 8rpx;
}

.range-text {
  font-size: 22rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 8rpx;
}

.range-bar {
  height: 8rpx;
  background: #E8EEF7;
  border-radius: 4rpx;
  overflow: hidden;
}

.range-fill {
  height: 100%;
  background: linear-gradient(90deg, #52C41A 0%, #73D13D 100%);
  border-radius: 4rpx;
  transition: width 0.5s ease;

  &.warning { background: linear-gradient(90deg, #FAAD14 0%, #FFC53D 100%); }
  &.error { background: linear-gradient(90deg, #FF4D4F 0%, #FF7875 100%); }
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.note-card {
  display: flex;
  align-items: flex-start;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  padding: 20rpx 24rpx;
}

.note-icon-wrap {
  width: 44rpx;
  height: 44rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;

  &.danger { background: rgba(255, 77, 79, 0.1); }
  &.warning { background: rgba(250, 173, 20, 0.1); }
  &.info { background: rgba(59, 124, 255, 0.1); }
  &.success { background: rgba(82, 196, 26, 0.1); }
}

.note-icon {
  font-size: 22rpx;

  .danger & { color: #FF4D4F; }
  .warning & { color: #FAAD14; }
  .info & { color: #3B7CFF; }
  .success & { color: #52C41A; }
}

.note-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
  flex: 1;
}

.tools-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.tool-item {
  display: flex;
  align-items: center;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
}

.tool-check {
  margin-right: 16rpx;
}

.check-box {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #D9D9D9;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;

  &.checked {
    background: #3B7CFF;
    border-color: #3B7CFF;
  }
}

.check-icon {
  font-size: 24rpx;
  color: #ffffff;
}

.tool-name {
  flex: 1;
  font-size: 26rpx;
  color: #1F1F1F;
}

.tool-count {
  font-size: 24rpx;
  color: #8C8C8C;
}

.detail-footer {
  padding-top: 20rpx;
  border-top: 1rpx solid #F0F0F0;
  margin-top: 20rpx;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.input-modal {
  width: 600rpx;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  padding: 32rpx;
  text-align: center;
  border-bottom: 1rpx solid #F0F0F0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.modal-body {
  padding: 32rpx;
}

.modal-hint {
  font-size: 26rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 20rpx;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 20rpx;
}

.tag-item {
  padding: 12rpx 24rpx;
  background: #F5F7FA;
  border-radius: 20rpx;
  border: 1rpx solid #E8EEF7;
  transition: all 0.3s ease;

  &:active {
    background: rgba(59, 124, 255, 0.1);
    border-color: #3B7CFF;
  }
}

.tag-text {
  font-size: 24rpx;
  color: #4A4A4A;
}

.modal-footer {
  display: flex;
  border-top: 1rpx solid #F0F0F0;

  .u-button {
    flex: 1;
    margin: 0;
    border-radius: 0;
  }
}
</style>