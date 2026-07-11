<template>
  <view class="sop-page">
    <view class="search-bar">
      <u-search v-model="keyword" placeholder="搜索工序名称" @search="doSearch" @custom="doSearch" @clear="doSearch"></u-search>
    </view>

    <view class="sop-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
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
          <text class="sop-product">产品: {{ item.productName || '-' }}</text>
          <u-button type="primary" size="mini" text="查看SOP" plain @click.stop="viewSop(item)"></u-button>
        </view>
      </view>
    </view>

    <u-popup :show="showDetail" mode="bottom" round="16" @close="showDetail = false" closeable>
      <view class="sop-detail">
        <text class="detail-title">{{ currentSop.processName }}</text>
        <view class="detail-section">
          <text class="detail-label">工序编码</text>
          <text class="detail-value">{{ currentSop.processCode }}</text>
        </view>
        <view class="detail-section">
          <text class="detail-label">作业步骤</text>
        </view>
        <view class="step-list">
          <view class="step-item" v-for="(step, idx) in currentSop.steps" :key="idx">
            <view class="step-no">{{ idx + 1 }}</view>
            <text class="step-text">{{ step }}</text>
          </view>
        </view>
        <view class="detail-section" style="margin-top: 24rpx;">
          <text class="detail-label">注意事项</text>
          <text class="detail-value warn">{{ currentSop.precautions || '无' }}</text>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { getProcessList } from '@/api/production'

export default {
  data() {
    return {
      keyword: '',
      list: [],
      showDetail: false,
      currentSop: {}
    }
  },
  onLoad() {
    this.doSearch()
  },
  methods: {
    async doSearch() {
      try {
        const res = await getProcessList({ keyword: this.keyword })
        this.list = (res && res.records) || res || []
      } catch {
        this.list = [
          { id: 1, processName: 'SMT贴片', processCode: 'PROC001', sortOrder: 1, productName: '智能运动手环A1' },
          { id: 2, processName: '焊接组装', processCode: 'PROC002', sortOrder: 2, productName: '智能运动手环A1' },
          { id: 3, processName: '固件烧录', processCode: 'PROC003', sortOrder: 3, productName: '智能运动手环A1' },
          { id: 4, processName: '功能测试', processCode: 'PROC004', sortOrder: 4, productName: '智能运动手环A1' },
          { id: 5, processName: '外观检查', processCode: 'PROC005', sortOrder: 5, productName: '智能运动手环A1' },
          { id: 6, processName: '包装入库', processCode: 'PROC006', sortOrder: 6, productName: '智能运动手环A1' }
        ]
      }
    },
    viewSop(item) {
      const sopData = {
        'SMT贴片': {
          steps: [
            '检查PCB板外观，确认无损伤、无氧化',
            '核对元件清单，确认物料型号与BOM一致',
            '将PCB板放入贴片机，设置贴片参数',
            '启动贴片程序，监控贴片过程',
            '贴片完成后检查元件贴装精度',
            '进行AOI光学检测，确认焊点质量'
          ],
          precautions: '操作时必须佩戴防静电手环，保持工作台整洁。贴片过程中禁止触碰元件引脚，避免静电损坏元件。'
        },
        '焊接组装': {
          steps: [
            '准备焊接工具：烙铁、焊锡丝、助焊剂',
            '检查焊接工位温度，烙铁温度设置为360±10°C',
            '将贴片完成的PCB板固定在焊接夹具上',
            '手工焊接连接器、电池座等元件',
            '焊接完成后检查焊点质量，确认无虚焊、连锡',
            '清理PCB板表面残留助焊剂'
          ],
          precautions: '焊接时注意安全，防止烫伤。焊锡量要适中，避免过多或过少导致焊接不良。'
        },
        '固件烧录': {
          steps: [
            '连接烧录器与PC，打开烧录软件',
            '选择对应产品型号的固件版本',
            '将待烧录PCB板连接到烧录器',
            '启动烧录程序，等待烧录完成',
            '烧录完成后进行校验，确认固件完整性',
            '记录烧录批次和固件版本信息'
          ],
          precautions: '烧录过程中禁止断电，确保连接稳定。确认固件版本与产品型号匹配。'
        },
        '功能测试': {
          steps: [
            '连接测试治具，接通电源',
            '测试屏幕显示：确认屏幕点亮、显示清晰',
            '测试按键功能：触摸按键响应正常',
            '测试传感器功能：心率、血氧、加速度计',
            '测试蓝牙连接：能正常连接手机APP',
            '测试电池充电：充电电流正常，充电指示正常',
            '测试防水功能：进行IP67防水测试'
          ],
          precautions: '测试前确认测试设备已校准。测试过程中记录每项测试结果，不合格品及时标记。'
        },
        '外观检查': {
          steps: [
            '在明亮光线下检查产品外观',
            '检查外壳表面：无划痕、无变形、无色差',
            '检查屏幕：无气泡、无灰尘、无坏点',
            '检查接口：USB接口无歪斜、无松动',
            '检查装配：外壳缝隙均匀，无毛刺',
            '检查印刷：Logo、文字清晰、无错印'
          ],
          precautions: '外观检查采用标准对比样品进行比对。对可疑缺陷使用放大镜仔细确认。'
        },
        '包装入库': {
          steps: [
            '准备包装盒、说明书、充电器等配件',
            '将合格产品放入防静电袋',
            '放入包装盒，确认配件齐全',
            '密封包装盒，粘贴合格证标签',
            '扫描条码入库，记录入库数量',
            '将产品整齐摆放到货架指定位置'
          ],
          precautions: '包装前确认产品已通过所有测试。入库时确认库存位置与系统记录一致。'
        }
      }
      
      const data = sopData[item.processName] || {
        steps: [
          '检查物料是否齐全，确认物料编码与工单一致',
          '按照工艺要求进行操作，注意操作规范',
          '完成操作后进行自检，确认质量达标',
          '记录操作数据，填写报工信息'
        ],
        precautions: '操作时请佩戴防静电手环，注意轻拿轻放'
      }

      this.currentSop = {
        ...item,
        steps: data.steps,
        precautions: data.precautions
      }
      this.showDetail = true
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
.mode-switch,
.sop-list,
.sop-detail,
.detail-section {
  padding: 20rpx 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
}

.search-bar,
.mode-switch,
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

.sop-product {
  font-size: 24rpx;
  color: #4A4A4A;
}

.sop-detail {
  padding: 40rpx 30rpx;
  max-height: 70vh;
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
</style>