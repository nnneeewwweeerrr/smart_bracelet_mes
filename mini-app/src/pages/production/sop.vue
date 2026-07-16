﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <view class="sop-page">
    <view class="page-content">
    <view class="search-bar">
      <view class="search-wrapper">
        <u-search v-model="keyword" placeholder="搜索工序名称" @search="doSearch" @custom="doSearch" @clear="doSearch"></u-search>
      </view>
      <view class="scan-btn" @click="handleScan">
        <text class="scan-icon">📷</text>
      </view>
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

    <u-popup :show="showDetail" mode="bottom" :height="850" round="16" @close="showDetail = false" closeable>
      <scroll-view scroll-y class="sop-detail-scroll" v-if="currentSop.process" :scroll-top="scrollTop">
      <view class="sop-detail">
        <text class="detail-title">{{ currentSop.process.processName }}</text>
        <view class="detail-section">
          <text class="detail-label">工序编码</text>
          <text class="detail-value">{{ currentSop.process.processCode }}</text>
        </view>

        <view v-if="currentSop.sop" class="detail-section">
          <text class="detail-label">SOP版本</text>
          <text class="detail-value">{{ currentSop.sop.sopVersion }}</text>
        </view>

        <view v-if="currentSop.sop && currentSop.sop.equipmentRequirement" class="detail-section">
          <text class="detail-label">设备要求</text>
          <text class="detail-value">{{ currentSop.sop.equipmentRequirement }}</text>
        </view>

        <view class="detail-section" style="margin-top: 24rpx;">
          <text class="detail-label">作业步骤</text>
        </view>
        <view class="step-list">
          <view class="step-item" v-for="(step, idx) in currentSop.steps" :key="idx">
            <view class="step-left">
              <view class="step-no">{{ idx + 1 }}</view>
            </view>
            <view class="step-content">
              <text class="step-text">{{ step.step || step }}</text>
              <view v-if="step.image" class="step-media">
                <image 
                  :src="step.image" 
                  mode="widthFix" 
                  class="step-image" 
                  @click="previewImage(step.image)"
                  @error="handleImageError($event, idx)"
                  :class="{ 'image-error': !step.imageLoaded }"
                ></image>
                <view v-if="!step.imageLoaded" class="image-placeholder">
                  <text class="placeholder-icon">📷</text>
                  <text class="placeholder-text">图片加载中...</text>
                </view>
              </view>
              <view v-if="step.video" class="step-media">
                <view class="video-container">
                  <video :src="step.video" class="step-video" controls></video>
                </view>
              </view>
            </view>
          </view>
          <view v-if="currentSop.steps.length === 0" class="empty-tip">暂无操作步骤</view>
        </view>

        <view v-if="currentSop.sop && currentSop.sop.precautions" class="detail-section" style="margin-top: 24rpx;">
          <text class="detail-label">注意事项</text>
          <text class="detail-value warn">{{ currentSop.sop.precautions }}</text>
        </view>

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
      </scroll-view>
    </u-popup>
    </view>
  </view>
</template>

<script>
import { getSopList, getSopDetail, scanSop } from '@/api/production'

export default {
  components: {},
  data() {
    return {
      keyword: '',
      loading: false,
      showDetail: false,
      scrollTop: 0,
      currentSop: {
        process: null,
        sop: null,
        steps: [],
        paramStandards: [],
        materialBom: []
      },
      sopList: [
        {
          id: 1,
          processName: 'SMT贴片标准作业指导书',
          processCode: 'PR001',
          sortOrder: 1,
          sopVersion: 'V1.0',
          precautions: '操作时必须佩戴防静电手环，保持工作台整洁。贴片过程中禁止触碰元件引脚，避免静电损坏元件。',
          equipmentRequirement: 'SMT贴片机、AOI检测仪、防静电工作台',
          steps: [
            { step: '检查PCB板外观，确认无损伤、无氧化', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=PCB%20board%20quality%20inspection%20clean%20surface%20no%20damage%20electronic%20manufacturing&image_size=landscape_16_9', video: '' },
            { step: '核对元件清单，确认物料型号与BOM一致', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=electronic%20components%20list%20BOM%20checking%20parts%20inventory%20SMD%20parts&image_size=landscape_16_9', video: '' },
            { step: '将PCB板放入贴片机，设置贴片参数', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=SMT%20machine%20PCB%20loading%20automated%20assembly%20line%20electronics%20manufacturing&image_size=landscape_16_9', video: '' },
            { step: '启动贴片程序，监控贴片过程', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=SMT%20machine%20operation%20monitoring%20chip%20placement%20process%20factory&image_size=landscape_16_9', video: '' },
            { step: '贴片完成后检查元件贴装精度', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=PCB%20after%20SMT%20inspection%20component%20placement%20accuracy%20visual%20check&image_size=landscape_16_9', video: '' },
            { step: '进行AOI光学检测，确认焊点质量', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=AOI%20machine%20optical%20inspection%20PCB%20quality%20control%20automated%20testing&image_size=landscape_16_9', video: '' }
          ],
          paramStandards: [
            { paramName: '贴片温度', paramCode: 'temperature', unit: '℃', minValue: 230.00, maxValue: 250.00, targetValue: 240.00, isRequired: 1 },
            { paramName: '贴片速度', paramCode: 'speed', unit: 'mm/s', minValue: 80.00, maxValue: 120.00, targetValue: 100.00, isRequired: 1 },
            { paramName: '刮刀压力', paramCode: 'pressure', unit: 'MPa', minValue: 0.3, maxValue: 0.6, targetValue: 0.45, isRequired: 1 }
          ],
          materialBom: []
        },
        {
          id: 2,
          processName: '焊接组装标准作业指导书',
          processCode: 'PR002',
          sortOrder: 2,
          sopVersion: 'V1.0',
          precautions: '焊接时注意安全，防止烫伤。焊锡量要适中，避免过多或过少导致焊接不良。',
          equipmentRequirement: '恒温烙铁、焊接夹具、放大镜、防静电手环',
          steps: [
            { step: '准备焊接工具：烙铁、焊锡丝、助焊剂', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=soldering%20tools%20iron%20solder%20flux%20electronics%20workbench%20preparation&image_size=landscape_16_9', video: '' },
            { step: '检查焊接工位温度，烙铁温度设置为360±10°C', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=soldering%20station%20temperature%20setting%20thermal%20control%20digital%20display&image_size=landscape_16_9', video: '' },
            { step: '将贴片完成的PCB板固定在焊接夹具上', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=PCB%20board%20fixture%20soldering%20jig%20holding%20clamp%20workstation&image_size=landscape_16_9', video: '' },
            { step: '手工焊接连接器、电池座等元件', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hand%20soldering%20connector%20battery%20socket%20precision%20work%20electronics&image_size=landscape_16_9', video: '' },
            { step: '焊接完成后检查焊点质量，确认无虚焊、连锡', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=solder%20joint%20inspection%20quality%20check%20magnifying%20glass%20no%20cold%20solder&image_size=landscape_16_9', video: '' },
            { step: '清理PCB板表面残留助焊剂', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=PCB%20cleaning%20flux%20residue%20removal%20clean%20surface%20electronics&image_size=landscape_16_9', video: '' }
          ],
          paramStandards: [
            { paramName: '焊接温度', paramCode: 'temperature', unit: '℃', minValue: 350.00, maxValue: 380.00, targetValue: 365.00, isRequired: 1 },
            { paramName: '焊接时间', paramCode: 'time', unit: 's', minValue: 2.00, maxValue: 5.00, targetValue: 3.50, isRequired: 1 },
            { paramName: '环境湿度', paramCode: 'humidity', unit: '%RH', minValue: 30.00, maxValue: 60.00, targetValue: 45.00, isRequired: 0 }
          ],
          materialBom: []
        },
        {
          id: 3,
          processName: '固件烧录标准作业指导书',
          processCode: 'PR003',
          sortOrder: 3,
          sopVersion: 'V1.0',
          precautions: '烧录过程中禁止断电，确保连接稳定。确认固件版本与产品型号匹配。',
          equipmentRequirement: '烧录器、PC烧录工作站、防静电手套',
          steps: [
            { step: '连接烧录器与PC，打开烧录软件', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=programmer%20device%20connected%20PC%20software%20firmware%20burning%20setup&image_size=landscape_16_9', video: '' },
            { step: '选择对应产品型号的固件版本', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=firmware%20selection%20software%20interface%20product%20model%20version%20choice&image_size=landscape_16_9', video: '' },
            { step: '将待烧录PCB板连接到烧录器', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=PCB%20connected%20programmer%20firmware%20flashing%20connection%20setup&image_size=landscape_16_9', video: '' },
            { step: '启动烧录程序，等待烧录完成', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=firmware%20burning%20progress%20bar%20software%20screen%20programming%20process&image_size=landscape_16_9', video: '' },
            { step: '烧录完成后进行校验，确认固件完整性', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=firmware%20verification%20success%20check%20software%20validation%20complete&image_size=landscape_16_9', video: '' },
            { step: '记录烧录批次和固件版本信息', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=production%20log%20batch%20record%20firmware%20version%20documentation%20tracking&image_size=landscape_16_9', video: '' }
          ],
          paramStandards: [
            { paramName: '烧录电压', paramCode: 'voltage', unit: 'V', minValue: 3.2, maxValue: 3.4, targetValue: 3.30, isRequired: 1 },
            { paramName: '烧录时间', paramCode: 'time', unit: 's', minValue: 15.00, maxValue: 30.00, targetValue: 20.00, isRequired: 1 }
          ],
          materialBom: []
        },
        {
          id: 4,
          processName: '功能测试标准作业指导书',
          processCode: 'PR004',
          sortOrder: 4,
          sopVersion: 'V1.0',
          precautions: '测试前确认测试设备已校准。测试过程中记录每项测试结果，不合格品及时标记。',
          equipmentRequirement: '功能测试治具、心率模拟器、蓝牙测试仪、万用表',
          steps: [
            { step: '连接测试治具，接通电源', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=test%20fixture%20connection%20power%20supply%20electronic%20testing%20setup&image_size=landscape_16_9', video: '' },
            { step: '测试屏幕显示：确认屏幕点亮、显示清晰', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20watch%20screen%20test%20display%20quality%20brightness%20check&image_size=landscape_16_9', video: '' },
            { step: '测试按键功能：触摸按键响应正常', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20watch%20button%20testing%20touch%20screen%20responsive%20interaction&image_size=landscape_16_9', video: '' },
            { step: '测试传感器功能：心率、血氧、加速度计', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=sensor%20testing%20heart%20rate%20blood%20oxygen%20accelerometer%20medical%20device&image_size=landscape_16_9', video: '' },
            { step: '测试蓝牙连接：能正常连接手机APP', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bluetooth%20connection%20test%20smart%20watch%20phone%20app%20pairing&image_size=landscape_16_9', video: '' },
            { step: '测试电池充电：充电电流正常，充电指示正常', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=battery%20charging%20test%20smart%20watch%20power%20indicator%20current%20monitor&image_size=landscape_16_9', video: '' },
            { step: '测试防水功能：进行IP67防水测试', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=waterproof%20testing%20IP67%20smart%20watch%20water%20immersion%20quality%20control&image_size=landscape_16_9', video: '' }
          ],
          paramStandards: [
            { paramName: '测试电压', paramCode: 'voltage', unit: 'V', minValue: 3.0, maxValue: 4.2, targetValue: 3.70, isRequired: 1 },
            { paramName: '心率误差', paramCode: 'deviation', unit: 'bpm', minValue: 0.00, maxValue: 5.00, targetValue: 2.00, isRequired: 1 },
            { paramName: '蓝牙信号', paramCode: 'signal', unit: 'dBm', minValue: -70.00, maxValue: -30.00, targetValue: -50.00, isRequired: 0 }
          ],
          materialBom: []
        },
        {
          id: 5,
          processName: '外观检查标准作业指导书',
          processCode: 'PR005',
          sortOrder: 5,
          sopVersion: 'V1.0',
          precautions: '外观检查采用标准对比样品进行比对。对可疑缺陷使用放大镜仔细确认。',
          equipmentRequirement: '标准光源箱、放大镜、对比样品架',
          steps: [
            { step: '在明亮光线下检查产品外观', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=visual%20inspection%20lighting%20booth%20product%20quality%20check%20bright%20light&image_size=landscape_16_9', video: '' },
            { step: '检查外壳表面：无划痕、无变形、无色差', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20watch%20case%20inspection%20scratch%20free%20surface%20quality%20control&image_size=landscape_16_9', video: '' },
            { step: '检查屏幕：无气泡、无灰尘、无坏点', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20watch%20screen%20inspection%20no%20bubbles%20dust%20dead%20pixels&image_size=landscape_16_9', video: '' },
            { step: '检查接口：USB接口无歪斜、无松动', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=USB%20port%20inspection%20connector%20alignment%20secure%20connection%20check&image_size=landscape_16_9', video: '' },
            { step: '检查装配：外壳缝隙均匀，无毛刺', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=product%20assembly%20inspection%20even%20gap%20smooth%20edges%20no%20burrs&image_size=landscape_16_9', video: '' },
            { step: '检查印刷：Logo、文字清晰、无错印', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=logo%20print%20quality%20inspection%20text%20clarity%20correct%20branding&image_size=landscape_16_9', video: '' }
          ],
          paramStandards: [
            { paramName: '检查照度', paramCode: 'lux', unit: 'lux', minValue: 800.00, maxValue: 1200.00, targetValue: 1000.00, isRequired: 1 }
          ],
          materialBom: []
        },
        {
          id: 6,
          processName: '包装入库标准作业指导书',
          processCode: 'PR006',
          sortOrder: 6,
          sopVersion: 'V1.0',
          precautions: '包装前确认产品已通过所有测试。入库时确认库存位置与系统记录一致。',
          equipmentRequirement: '包装工作台、条码扫描枪、标签打印机',
          steps: [
            { step: '准备包装盒、说明书、充电器等配件', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=product%20packaging%20materials%20box%20manual%20charger%20accessories%20preparation&image_size=landscape_16_9', video: '' },
            { step: '将合格产品放入防静电袋', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ESD%20bag%20packing%20electronic%20product%20anti%20static%20protection&image_size=landscape_16_9', video: '' },
            { step: '放入包装盒，确认配件齐全', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=product%20boxing%20accessories%20check%20complete%20package%20verification&image_size=landscape_16_9', video: '' },
            { step: '密封包装盒，粘贴合格证标签', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=box%20sealing%20certification%20label%20quality%20sticker%20packaging%20final&image_size=landscape_16_9', video: '' },
            { step: '扫描条码入库，记录入库数量', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=barcode%20scanning%20warehouse%20inventory%20tracking%20scan%20gun%20operation&image_size=landscape_16_9', video: '' },
            { step: '将产品整齐摆放到货架指定位置', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=warehouse%20shelving%20product%20storage%20organized%20inventory%20management&image_size=landscape_16_9', video: '' }
          ],
          paramStandards: [
            { paramName: '封箱压力', paramCode: 'pressure', unit: 'MPa', minValue: 0.2, maxValue: 0.5, targetValue: 0.35, isRequired: 0 },
            { paramName: '标签清晰度', paramCode: 'clarity', unit: '%', minValue: 95.00, maxValue: 100.00, targetValue: 100.00, isRequired: 0 }
          ],
          materialBom: []
        }
      ]
    }
  },
  computed: {
    list() {
      if (!this.keyword) return this.sopList
      return this.sopList.filter(item => 
        item.processName && item.processName.includes(this.keyword)
      )
    }
  },
  onLoad() {
  },
  methods: {
    doSearch() {
    },
    async handleScan() {
      try {
        const { result } = await uni.scanCode({
          scanType: ['qrCode']
        })
        let res = null
        try {
          res = await scanSop(result)
        } catch {
          res = this.getMockSop(result)
        }
        if (res && res.process) {
          this.viewSop(res.process)
        } else if (res && res.error) {
          uni.showToast({ title: res.error, icon: 'none' })
        } else {
          uni.showToast({ title: '扫码失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '取消扫码', icon: 'none' })
      }
    },
    getMockSop(code) {
      const mockItems = {
        'STATION0001': { process: { id: 1, processName: 'SMT贴片标准作业指导书', processCode: 'PR001' } },
        'STATION0002': { process: { id: 2, processName: 'AOI检测标准作业指导书', processCode: 'PR002' } },
        'STATION0003': { process: { id: 3, processName: '焊接组装标准作业指导书', processCode: 'PR003' } },
        'STATION0004': { process: { id: 4, processName: '功能测试标准作业指导书', processCode: 'PR004' } },
        'STATION0005': { process: { id: 5, processName: '外观检查标准作业指导书', processCode: 'PR005' } },
        'STATION0006': { process: { id: 6, processName: '包装入库标准作业指导书', processCode: 'PR006' } },
        'PR001': { process: { id: 1, processName: 'SMT贴片标准作业指导书', processCode: 'PR001' } },
        'PR002': { process: { id: 2, processName: 'AOI检测标准作业指导书', processCode: 'PR002' } },
        'PR003': { process: { id: 3, processName: '焊接组装标准作业指导书', processCode: 'PR003' } },
        'PR004': { process: { id: 4, processName: '功能测试标准作业指导书', processCode: 'PR004' } },
        'PR005': { process: { id: 5, processName: '外观检查标准作业指导书', processCode: 'PR005' } },
        'PR006': { process: { id: 6, processName: '包装入库标准作业指导书', processCode: 'PR006' } }
      }
      return mockItems[code] || { process: null, error: '未找到对应SOP' }
    },
    viewSop(item) {
      this.scrollTop = 0
      const localData = this.sopList.find(s => s.id === item.id) || item
      
      this.currentSop = {
        process: {
          id: localData.id,
          processName: localData.processName,
          processCode: localData.processCode,
          sortOrder: localData.sortOrder
        },
        sop: {
          sopVersion: localData.sopVersion,
          equipmentRequirement: localData.equipmentRequirement,
          precautions: localData.precautions
        },
        steps: (localData.steps || []).map(s => ({
          step: s.step,
          image: s.image,
          video: s.video,
          imageLoaded: !!s.image
        })),
        paramStandards: localData.paramStandards || [],
        materialBom: localData.materialBom || []
      }
      this.showDetail = true
    },
    previewImage(url) {
      uni.previewImage({
        urls: [url]
      })
    },
    handleImageError(e, index) {
      if (this.currentSop.steps[index]) {
        this.currentSop.steps[index].imageLoaded = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8rpx); }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.sop-page {
  min-height: 100vh;
  background: #F0F4F8;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  gap: 16rpx;
  position: relative;
  z-index: 10;
  background: linear-gradient(180deg, #3B7CFF 0%, #5B9AFF 100%);
  padding-top: calc(env(safe-area-inset-top) + 30rpx);
  padding-bottom: 40rpx;
}

.search-wrapper {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  border: 2rpx solid rgba(255, 255, 255, 0.6);
}

.scan-btn {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.92);
    background: rgba(255, 255, 255, 0.3);
  }
}

.scan-icon {
  font-size: 40rpx;
  color: #fff;
}

.sop-list {
  padding: 0 24rpx;
  margin-top: -30rpx;
}

.sop-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.06), 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &:active {
    transform: scale(0.98) translateY(-4rpx);
    box-shadow: 0 12rpx 36rpx rgba(0, 0, 0, 0.1), 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    background: linear-gradient(90deg, #3B7CFF, #5B9AFF, #7CB7FF, #5B9AFF, #3B7CFF);
    background-size: 200% 100%;
    animation: shimmer 3s linear infinite;
  }
}

.sop-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.sop-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #3B7CFF, #5B9AFF);
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(59, 124, 255, 0.35);
  animation: float 3s ease-in-out infinite;
}

.sop-name {
  font-size: 32rpx;
  color: #1F1F1F;
  font-weight: 600;
  margin-left: 20rpx;
  line-height: 1.4;
}

.sop-info {
  display: flex;
  gap: 24rpx;
  margin-bottom: 20rpx;
  padding-left: 92rpx;
}

.sop-code {
  font-size: 24rpx;
  color: #3B7CFF;
  font-weight: 500;
  background: rgba(59, 124, 255, 0.08);
  padding: 6rpx 16rpx;
  border-radius: 10rpx;
}

.sop-sort {
  font-size: 24rpx;
  color: #999;
}

.sop-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 92rpx;
}

.sop-status {
  font-size: 22rpx;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  font-weight: 500;

  &.active {
    background: linear-gradient(135deg, rgba(82, 196, 26, 0.15), rgba(82, 196, 26, 0.05));
    color: #52C41A;
  }

  &.inactive {
    background: rgba(139, 139, 139, 0.1);
    color: #8C8C8C;
  }
}

.sop-detail-scroll {
  height: 850rpx;
  width: 100%;
}

.sop-detail {
  padding: 50rpx 32rpx;
  min-height: 100%;
  background: #F8FAFC;
}

.detail-title {
  font-size: 38rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 36rpx;
  text-align: center;
  letter-spacing: 2rpx;
}

.detail-section {
  margin-bottom: 28rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.detail-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 14rpx;
  position: relative;
  padding-left: 20rpx;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 24rpx;
    background: linear-gradient(180deg, #3B7CFF, #5B9AFF);
    border-radius: 3rpx;
  }
}

.detail-value {
  font-size: 28rpx;
  color: #4A4A4A;
  display: block;
  line-height: 1.6;

  &.warn {
    color: #FAAD14;
    line-height: 1.8;
    background: rgba(250, 173, 20, 0.08);
    padding: 16rpx;
    border-radius: 12rpx;
    border-left: 4rpx solid #FAAD14;
  }
}

.step-list {
  padding-left: 16rpx;
}

.step-item {
  display: flex;
  margin-bottom: 28rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  border: 2rpx solid rgba(59, 124, 255, 0.12);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  }
}

.step-left {
  flex-shrink: 0;
  margin-right: 20rpx;
}

.step-no {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #3B7CFF, #5B9AFF);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 8rpx 20rpx rgba(59, 124, 255, 0.35);
}

.step-content {
  flex: 1;
}

.step-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
  display: block;
  margin-bottom: 12rpx;
}

.step-media {
  margin-top: 12rpx;
  border-radius: 12rpx;
  overflow: hidden;
  position: relative;
}

.step-image {
  width: 100%;
  max-height: 400rpx;
  display: block;
}

.step-image.image-error {
  display: none;
}

.image-placeholder {
  width: 100%;
  height: 300rpx;
  background: #F5F7FA;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
}

.placeholder-icon {
  font-size: 60rpx;
  margin-bottom: 12rpx;
}

.placeholder-text {
  font-size: 24rpx;
  color: #999;
}

.video-container {
  width: 100%;
  background: #000;
  border-radius: 12rpx;
  overflow: hidden;
}

.step-video {
  width: 100%;
  height: 360rpx;
}

.empty-tip {
  font-size: 24rpx;
  color: #8C8C8C;
  text-align: center;
  padding: 20rpx;
}

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