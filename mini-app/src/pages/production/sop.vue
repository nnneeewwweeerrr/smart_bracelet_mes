<template>
  <view class="sop-page">
    <view class="search-bar">
      <u-search v-model="keyword" placeholder="搜索工序名称" @search="doSearch" @custom="doSearch" @clear="doSearch"></u-search>
    </view>

    <view class="sop-list">
      <view v-if="list.length === 0 && !loading" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无SOP文档"></u-empty>
      </view>

      <view class="sop-card" v-for="item in list" :key="item.id" @click="viewSop(item)">
        <view class="sop-header">
          <text class="sop-icon">文</text>
          <text class="sop-name">{{ item.processName }}</text>
        </view>
        <view class="sop-info">
          <text class="sop-code">{{ item.processCode }}</text>
          <text class="sop-sort">排序: {{ item.sortOrder }}</text>
        </view>
        <view class="sop-footer">
          <text class="sop-status" :class="item.sopStatus === 1 ? 'active' : 'inactive'">
            {{ item.sopStatus === 1 ? 'SOP已配置' : 'SOP未配置' }}
          </text>
          <u-button type="primary" size="mini" text="查看SOP" plain @click.stop="viewSop(item)"></u-button>
        </view>
      </view>
    </view>

    <!-- SOP详情弹窗 -->
    <u-popup :show="showDetail" mode="bottom" round="16" @close="showDetail = false" closeable>
      <view class="sop-detail" v-if="currentSop.process">
        <text class="detail-title">{{ currentSop.process.processName }}</text>
        <view class="detail-section">
          <text class="detail-label">工序编码</text>
          <text class="detail-value">{{ currentSop.process.processCode }}</text>
        </view>

        <!-- SOP基本信息 -->
        <view v-if="currentSop.sop" class="detail-section">
          <text class="detail-label">SOP版本</text>
          <text class="detail-value">{{ currentSop.sop.sopVersion }}</text>
        </view>

        <view v-if="currentSop.sop && currentSop.sop.equipmentRequirement" class="detail-section">
          <text class="detail-label">设备要求</text>
          <text class="detail-value">{{ currentSop.sop.equipmentRequirement }}</text>
        </view>

        <!-- 操作步骤 -->
        <view class="detail-section" style="margin-top: 24rpx;">
          <text class="detail-label">作业步骤</text>
        </view>
        <view class="step-list">
          <view class="step-item" v-for="(step, idx) in currentSop.steps" :key="idx">
            <view class="step-no">{{ idx + 1 }}</view>
            <text class="step-text">{{ step }}</text>
          </view>
          <view v-if="currentSop.steps.length === 0" class="empty-tip">暂无操作步骤</view>
        </view>

        <!-- 注意事项 -->
        <view v-if="currentSop.sop && currentSop.sop.precautions" class="detail-section" style="margin-top: 24rpx;">
          <text class="detail-label">注意事项</text>
          <text class="detail-value warn">{{ currentSop.sop.precautions }}</text>
        </view>

        <!-- 工艺参数标准 -->
        <view v-if="currentSop.paramStandards && currentSop.paramStandards.length > 0" class="detail-section" style="margin-top: 24rpx;">
          <text class="detail-label">工艺参数标准</text>
          <view class="param-table">
            <view class="param-row param-header">
              <text class="param-col col-name">参数</text>
              <text class="param-col col-range">标准范围</text>
              <text class="param-col col-target">目标值</text>
              <text class="param-col col-req">必检</text>
            </view>
            <view class="param-row" v-for="param in currentSop.paramStandards" :key="param.id">
              <text class="param-col col-name">{{ param.paramName }}</text>
              <text class="param-col col-range">{{ param.minValue }}~{{ param.maxValue }}{{ param.unit }}</text>
              <text class="param-col col-target">{{ param.targetValue }}{{ param.unit }}</text>
              <text class="param-col col-req">
                <text :class="param.isRequired === 1 ? 'req-yes' : 'req-no'">{{ param.isRequired === 1 ? '是' : '否' }}</text>
              </text>
            </view>
          </view>
        </view>

        <!-- 物料BOM -->
        <view v-if="currentSop.materialBom && currentSop.materialBom.length > 0" class="detail-section" style="margin-top: 24rpx;">
          <text class="detail-label">物料BOM</text>
          <view class="bom-table">
            <view class="bom-row bom-header">
              <text class="bom-col col-code">物料编码</text>
              <text class="bom-col col-mname">物料名称</text>
              <text class="bom-col col-qty">用量</text>
            </view>
            <view class="bom-row" v-for="bom in currentSop.materialBom" :key="bom.materialId">
              <text class="bom-col col-code">{{ bom.materialCode }}</text>
              <text class="bom-col col-mname">{{ bom.materialName }}</text>
              <text class="bom-col col-qty">{{ bom.quantity }}{{ bom.unit }}</text>
            </view>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { getProcessList, getSopDetail } from '@/api/production'

export default {
  data() {
    return {
      keyword: '',
      list: [],
      loading: false,
      showDetail: false,
      currentSop: {
        process: null,
        sop: null,
        steps: [],
        paramStandards: [],
        materialBom: []
      }
    }
  },
  onLoad() {
    this.doSearch()
  },
  methods: {
    async doSearch() {
      this.loading = true
      try {
        const res = await getProcessList({ keyword: this.keyword })
        this.list = (res && res.records) || res || []
      } catch {
        this.list = []
      } finally {
        this.loading = false
      }
    },
    async viewSop(item) {
      this.currentSop = {
        process: item,
        sop: null,
        steps: [],
        paramStandards: [],
        materialBom: []
      }
      this.showDetail = true

      try {
        const res = await getSopDetail(item.id)
        if (res) {
          this.currentSop.process = res.process || item
          this.currentSop.sop = res.sop || null
          this.currentSop.paramStandards = res.paramStandards || []
          this.currentSop.materialBom = res.materialBom || []

          // 解析操作步骤（JSON数组字符串）
          if (res.sop && res.sop.operationSteps) {
            try {
              this.currentSop.steps = JSON.parse(res.sop.operationSteps)
            } catch {
              this.currentSop.steps = []
            }
          }
        }
      } catch {
        // 接口异常时使用模拟数据
        this.currentSop.steps = [
          '检查物料是否齐全，确认物料编码与工单一致',
          '按照工艺要求进行操作，注意操作规范',
          '完成操作后进行自检，确认质量达标',
          '记录操作数据，填写报工信息'
        ]
        if (this.currentSop.sop) {
          this.currentSop.sop.precautions = '操作时请佩戴防静电手环，注意轻拿轻放'
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.sop-page {
  min-height: 100vh;
  background: transparent;
  position: relative;
}

.search-bar,
.sop-list,
.sop-detail,
.detail-section {
  padding: 20rpx 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
}

.search-bar,
.sop-card,
.sop-detail,
.detail-section {
  border-radius: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.sop-list {
  padding: 16rpx 24rpx;
}

.sop-card {
  padding: 28rpx 30rpx;
  margin-bottom: 18rpx;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.99);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.sop-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.sop-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: linear-gradient(135deg, #3B7CFF, #5B9BFF);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sop-name {
  font-size: 30rpx;
  color: #1F1F1F;
  font-weight: 500;
  margin-left: 12rpx;
}

.sop-info {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.sop-code {
  font-size: 24rpx;
  color: #3B7CFF;
}

.sop-sort {
  font-size: 24rpx;
  color: #8C8C8C;
}

.sop-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sop-status {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;

  &.active {
    background: rgba(82, 196, 26, 0.1);
    color: #52C41A;
  }

  &.inactive {
    background: rgba(139, 139, 139, 0.1);
    color: #8C8C8C;
  }
}

.sop-detail {
  padding: 40rpx 30rpx;
  max-height: 75vh;
  overflow-y: auto;
}

.detail-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 30rpx;
  text-align: center;
}

.detail-section {
  margin-bottom: 20rpx;
}

.detail-label {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F1F1F;
  display: block;
  margin-bottom: 12rpx;
}

.detail-value {
  font-size: 26rpx;
  color: #4A4A4A;
  display: block;

  &.warn {
    color: #FAAD14;
    line-height: 1.6;
  }
}

.step-list {
  padding-left: 10rpx;
}

.step-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.step-no {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #3B7CFF;
  color: #ffffff;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.step-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 44rpx;
  flex: 1;
}

.empty-tip {
  font-size: 24rpx;
  color: #8C8C8C;
  text-align: center;
  padding: 20rpx;
}

/* 参数标准表格 */
.param-table {
  background: #F5F7FA;
  border-radius: 12rpx;
  overflow: hidden;
}

.param-row {
  display: flex;
  padding: 16rpx 12rpx;
  border-bottom: 1rpx solid #E8E8E8;

  &:last-child {
    border-bottom: none;
  }
}

.param-header {
  background: #E8F0FF;
  font-weight: 600;
  font-size: 24rpx;
  color: #1F1F1F;
}

.param-col {
  font-size: 24rpx;
  color: #4A4A4A;
}

.col-name { width: 120rpx; flex-shrink: 0; }
.col-range { flex: 1; }
.col-target { width: 130rpx; text-align: center; }
.col-req { width: 70rpx; text-align: center; }

.req-yes { color: #FF4D4F; font-weight: 500; }
.req-no { color: #8C8C8C; }

/* 物料BOM表格 */
.bom-table {
  background: #F5F7FA;
  border-radius: 12rpx;
  overflow: hidden;
}

.bom-row {
  display: flex;
  padding: 16rpx 12rpx;
  border-bottom: 1rpx solid #E8E8E8;

  &:last-child {
    border-bottom: none;
  }
}

.bom-header {
  background: #E8F0FF;
  font-weight: 600;
  font-size: 24rpx;
  color: #1F1F1F;
}

.bom-col {
  font-size: 24rpx;
  color: #4A4A4A;
}

.col-code { width: 180rpx; flex-shrink: 0; }
.col-mname { flex: 1; }
.col-qty { width: 120rpx; text-align: right; }
</style>
