<template>
  <div class="ai-assistant">
    <transition name="slide-up">
      <div v-if="isExpanded" class="ai-panel">
        <div class="ai-header">
          <div class="ai-logo">
            <el-icon :size="20"><Document /></el-icon>
          </div>
          <div class="ai-title">
            <span class="title-text">AI生产助手</span>
            <span class="title-desc">智能手环MES系统</span>
          </div>
          <button class="close-btn" @click="toggleExpand">
            <el-icon><Warning /></el-icon>
          </button>
        </div>

        <div class="ai-body">
          <div class="chat-container">
            <transition-group name="msg-fade" tag="div">
            <div
              v-for="(item, index) in messages"
              :key="index"
              class="chat-item"
              :class="{ user: item.role === 'user', ai: item.role === 'assistant' }"
            >
              <div class="chat-avatar">
                <el-icon v-if="item.role === 'user'" :size="18"><UserFilled /></el-icon>
                <el-icon v-else :size="18"><Document /></el-icon>
              </div>
              <div class="chat-bubble">
                <div class="chat-content">{{ item.content }}</div>
                <div v-if="item.sources && item.sources.length > 0" class="chat-sources">
                  <div class="sources-title">
                    <el-icon :size="12"><Link /></el-icon>
                    <span>引用来源</span>
                  </div>
                  <div class="sources-list">
                    <div
                      v-for="(source, idx) in item.sources"
                      :key="idx"
                      class="source-item"
                      @click="toggleSourceDetail(idx)"
                    >
                      <el-tag :type="getSourceTagType(source.type)" size="small">
                        {{ source.type === 'knowledge_graph' ? '知识图谱' : '文档' }}
                      </el-tag>
                      <span class="source-title">{{ source.title }}</span>
                      <el-icon :size="10" class="expand-icon" :class="{ expanded: expandedSources[index]?.includes(idx) }">
                        <ArrowDown />
                      </el-icon>
                    </div>
                    <div
                      v-for="(source, idx) in item.sources"
                      :key="'detail-' + idx"
                      class="source-detail"
                      v-show="expandedSources[index]?.includes(idx)"
                    >
                      <pre>{{ source.content }}</pre>
                    </div>
                  </div>
                </div>
                <div class="chat-time">{{ item.time }}</div>
              </div>
            </div>
            </transition-group>

            <div v-if="loading" class="loading-indicator">
              <div class="loading-pulse"></div>
              <span class="loading-text">AI正在思考...</span>
            </div>
          </div>
        </div>

        <div class="ai-footer">
          <div class="input-wrapper">
            <input
              v-model="inputMessage"
              type="text"
              placeholder="请输入您的问题..."
              class="chat-input"
              @keydown.enter="sendMessage"
            />
            <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || loading">
              <el-icon><CircleCheck /></el-icon>
            </button>
          </div>
          <div class="quick-questions">
            <span class="quick-label">快速提问：</span>
            <button
              v-for="q in quickQuestions"
              :key="q"
              class="quick-btn"
              @click="sendQuickQuestion(q)"
            >
              {{ q }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <button class="ai-toggle-btn" @click="toggleExpand">
      <el-icon :size="22"><Document /></el-icon>
      <span v-if="!isExpanded" class="badge">{{ unreadCount }}</span>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import { Document, Warning, CircleCheck, UserFilled, Link, ArrowDown } from '@element-plus/icons-vue'
import { aiChat, getRecentHistory } from '../api/ai'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const isExpanded = ref(false)
const inputMessage = ref('')
const messages = ref([])
const loading = ref(false)
const unreadCount = ref(0)
const expandedSources = reactive({})

const quickQuestions = [
  '焊接温度标准是多少？',
  '屏幕贴合有气泡怎么处理？',
  '如何进行报工操作？',
  '防水测试怎么做？',
  '工单状态有哪些？',
  '生产计划如何制定？',
  'SMT贴片工艺流程',
  '产品质量检验标准'
]

const presetQA = {
  '焊接温度标准是多少？': {
    answer: '智能手环生产中，各工序焊接温度标准如下：\n\n1. **SMT回流焊**：\n   - 预热区：150-180℃，时间60-90秒\n   - 恒温区：180-200℃，时间60-120秒\n   - 回流区：最高温度235-245℃，峰值时间20-40秒\n   - 冷却区：降温速率≤4℃/秒\n\n2. **烙铁手焊**：\n   - 普通焊点：320-360℃，时间2-3秒\n   - 热敏元件：280-320℃，时间1-2秒\n\n3. **注意事项**：\n   - 每日首件必须测温板验证温度曲线\n   - 每4小时抽查一次焊接温度\n   - 焊接后需冷却30秒以上才可进行下一工序',
    sources: [
      { type: 'knowledge_graph', title: 'SMT工艺规范', content: '回流焊温度曲线：预热150-180℃/60-90s，恒温180-200℃/60-120s，回流235-245℃/20-40s' },
      { type: 'document', title: '焊接作业指导书', content: '烙铁温度标准：普通焊点320-360℃，热敏元件280-320℃' }
    ]
  },
  '屏幕贴合有气泡怎么处理？': {
    answer: '屏幕贴合出现气泡的处理方案：\n\n**一、气泡产生原因**\n1. 贴合环境洁净度不足（灰尘颗粒）\n2. 贴合压力不均匀或压力不足\n3. OCA胶厚度不匹配\n4. 贴合速度过快，空气未完全排出\n\n**二、解决方法**\n1. **轻微气泡（直径<0.1mm）**：\n   - 放入脱泡机，0.6MPa压力，60℃，30分钟\n   - 95%以上轻微气泡可消除\n\n2. **中度气泡（直径0.1-0.3mm）**：\n   - 先脱泡处理，如仍存在则返工\n   - 用专用解胶液拆除屏幕，清洁后重新贴合\n\n3. **严重气泡（直径>0.3mm）**：\n   - 直接报废处理，不得流入下道工序\n\n**三、预防措施**\n- 贴合车间洁净度保持万级以上\n- 每日校准贴合机压力和速度\n- OCA胶存储温度控制在25±5℃',
    sources: [
      { type: 'document', title: '屏幕贴合作业指导书', content: '脱泡参数：压力0.6MPa，温度60℃，时间30分钟' },
      { type: 'knowledge_graph', title: '贴合不良处理', content: '气泡不良率应控制在0.5%以下' }
    ]
  },
  '如何进行报工操作？': {
    answer: '生产工人报工操作流程：\n\n**操作步骤**\n1. 登录智能手环或MES系统\n2. 进入"生产报工"模块\n3. 扫描工单号二维码或手动输入工单号\n4. 选择当前工序（SMT/焊接/组装/测试/包装）\n5. 输入本次报工数量（良品数+不良品数）\n6. 如产生不良品，选择不良原因\n7. 确认提交，系统自动更新工单进度\n\n**注意事项**\n- 报工前请确认产品质量检验合格\n- 每2小时报工一次，或每完成100PCS报工一次\n- 不良品需单独放置并标识\n- 如发现异常请立即停线并上报班组长\n\n**系统路径**：生产管理 → 生产报工 → 新增报工',
    sources: [
      { type: 'document', title: '报工操作手册', content: '报工频率：每2小时或每100PCS报工一次' },
      { type: 'knowledge_graph', title: '报工流程', content: '扫码→选工序→填数量→选不良原因→提交' }
    ]
  },
  '防水测试怎么做？': {
    answer: '智能手环防水测试操作规范：\n\n**测试标准**\n- IP68级防水：5米水深，持续30分钟\n- 测试水温：25±5℃\n\n**测试步骤**\n1. **外观检查**：确认密封圈完好，按键、充电口无异物\n2. **气压初检**：\n   - 充气压力：100kPa\n   - 保压时间：30秒\n   - 压降≤50Pa为合格\n3. **浸水测试**（抽样）：\n   - 将样品放入5米水深（或加压等效）\n   - 保持30分钟\n   - 取出后擦干，检查内部有无进水\n4. **功能验证**：\n   - 触摸屏功能正常\n   - 按键响应正常\n   - 心率、血氧传感器正常\n   - 充电功能正常\n\n**测试频率**\n- 每批次首件必测\n- 量产中每2小时抽测5PCS\n- 不良率超过1%时全检',
    sources: [
      { type: 'document', title: '防水测试作业指导书', content: 'IP68测试标准：5米水深，30分钟' },
      { type: 'knowledge_graph', title: '质量检验标准', content: '气压测试压降≤50Pa为合格' }
    ]
  },
  '工单状态有哪些？': {
    answer: '生产工单共有4种状态：\n\n**1. 待生产（状态码：0）**\n- 工单已创建但尚未开始\n- 可进行编辑、修改、删除操作\n- 可查看BOM、工艺路线等生产准备信息\n\n**2. 生产中（状态码：1）**\n- 工单已启动，正在生产\n- 不允许修改工单基本信息\n- 实时统计生产进度和良品率\n- 可查看各工序报工明细\n\n**3. 已完成（状态码：2）**\n- 工单全部生产完成并质检合格\n- 自动计算工单成本和效率\n- 生成生产报表和质量分析\n\n**4. 已取消（状态码：3）**\n- 订单取消或其他原因终止生产\n- 仅待生产状态的工单可取消\n- 取消后不可恢复\n\n**状态流转**：待生产 → 生产中 → 已完成\n（待生产也可直接取消）',
    sources: [
      { type: 'knowledge_graph', title: '工单状态机', content: '待生产→生产中→已完成，待生产也可→已取消' }
    ]
  },
  '生产计划如何制定？': {
    answer: '生产计划制定流程：\n\n**一、计划制定依据**\n1. 销售订单需求\n2. 产品BOM和工艺路线\n3. 设备产能和人员配置\n4. 物料库存情况\n5. 标准工时数据\n\n**二、制定步骤**\n1. **需求汇总**：汇总本周/本月销售订单\n2. **产能核算**：\n   - 设备产能：SMT线 8000点/天\n   - 组装线：2000台/天/线\n   - 测试线：3000台/天/线\n3. **物料检查**：确认关键物料库存充足\n4. **计划排产**：\n   - 优先安排交期紧急订单\n   - 同产品合并生产减少换线\n   - 均衡分配各产线负荷\n5. **计划发布**：系统发布后通知各车间\n\n**三、调整规则**\n- 计划变更需提前24小时通知\n- 紧急插单需生产经理审批\n- 每周一上午召开生产计划例会',
    sources: [
      { type: 'document', title: '生产计划管理规范', content: '计划变更需提前24小时通知，紧急插单需生产经理审批' },
      { type: 'knowledge_graph', title: '产能数据', content: 'SMT线 8000点/天，组装线 2000台/天/线' }
    ]
  },
  'SMT贴片工艺流程': {
    answer: 'SMT贴片标准工艺流程：\n\n**1. 上料**\n- 核对物料编码、规格、数量\n- 按站位表上料，首件确认\n- 换料需双人复核签字\n\n**2. 锡膏印刷**\n- 锡膏型号：SAC305\n- 印刷厚度：0.12-0.15mm\n- 钢网清洁：每10块板自动清洗一次\n- 印刷后目视检查，无连锡、少锡\n\n**3. 贴片**\n- 高速机贴装CHIP元件（0201/0402等）\n- 泛用机贴装IC和异型元件\n- 贴装精度：±0.05mm\n- 每小时抽检贴装偏位\n\n**4. 回流焊**\n- 温度曲线：预热→恒温→回流→冷却\n- 峰值温度：235-245℃\n- 每日首件测温板验证\n\n**5. AOI检测**\n- 全自动光学检测\n- 检测项目：缺件、偏位、立碑、连锡、少锡\n- 不良品标记后人工复核\n\n**6. 返修（如有）**\n- 不良品标识、隔离\n- 专人返修，记录返修原因\n\n**关键控制点**：锡膏印刷质量、贴装精度、回流温度曲线',
    sources: [
      { type: 'document', title: 'SMT作业指导书', content: '贴装精度±0.05mm，每日首件测温板' },
      { type: 'knowledge_graph', title: 'SMT流程', content: '上料→锡膏印刷→贴片→回流焊→AOI检测→返修' }
    ]
  },
  '产品质量检验标准': {
    answer: '智能手环成品质量检验标准：\n\n**一、外观检验**\n- 外壳无划伤、变形、色差\n- 屏幕无划痕、亮点、暗斑（亮点≤1个且直径<0.1mm）\n- 按键手感一致，无卡滞\n- 表带扣合顺畅，无松动\n\n**二、功能检验**\n1. 触摸屏：全区域触摸灵敏，无死角\n2. 按键：按压力度150-250g，寿命10万次\n3. 心率监测：误差±2bpm\n4. 血氧监测：误差±2%\n5. 震动马达：震动强度3级可调\n6. 蓝牙：连接距离≥10米\n7. 充电：充电电流≥100mA，充满时间≤2小时\n8. 防水：IP68级，5米水深30分钟\n\n**三、可靠性测试（抽样）**\n- 高低温循环：-20℃~60℃，10个循环\n- 跌落测试：1.2米高度，6面各1次\n- 按键寿命：10万次\n- 表带拉力：50N持续10秒\n\n**四、包装检验**\n- 配件齐全（主机、充电线、说明书、保修卡）\n- 包装完好，标识清晰\n- 序列号与包装箱一致\n\n**质量目标**：成品合格率≥99%，客户投诉率<0.1%',
    sources: [
      { type: 'document', title: '成品检验规范', content: '成品合格率≥99%，客户投诉率<0.1%' },
      { type: 'knowledge_graph', title: '质量标准', content: '心率误差±2bpm，血氧误差±2%，防水IP68' }
    ]
  }
}

function getSourceTagType(type) {
  return type === 'knowledge_graph' ? 'primary' : 'success'
}

function toggleSourceDetail(sourceIndex) {
  const lastMessageIndex = messages.value.length - 1
  if (!expandedSources[lastMessageIndex]) {
    expandedSources[lastMessageIndex] = []
  }
  const idx = expandedSources[lastMessageIndex].indexOf(sourceIndex)
  if (idx > -1) {
    expandedSources[lastMessageIndex].splice(idx, 1)
  } else {
    expandedSources[lastMessageIndex].push(sourceIndex)
  }
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    unreadCount.value = 0
    nextTick(() => {
      scrollToBottom()
    })
  }
}

function scrollToBottom() {
  const container = document.querySelector('.chat-container')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

function formatTime(date) {
  const now = new Date(date)
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

function getRandomDelay() {
  return 3000 + Math.random() * 1000
}

function findPresetAnswer(question) {
  const keys = Object.keys(presetQA)
  for (const key of keys) {
    if (question === key || question.includes(key) || key.includes(question)) {
      return presetQA[key]
    }
  }
  return null
}

function sendMessage() {
  if (!inputMessage.value.trim() || loading.value) return

  const question = inputMessage.value.trim()
  inputMessage.value = ''

  messages.value.push({
    role: 'user',
    content: question,
    time: formatTime(new Date())
  })

  loading.value = true
  scrollToBottom()

  const preset = findPresetAnswer(question)
  const delay = getRandomDelay()

  if (preset) {
    setTimeout(() => {
      messages.value.push({
        role: 'assistant',
        content: preset.answer,
        time: formatTime(new Date()),
        sources: preset.sources
      })
      loading.value = false
      scrollToBottom()
    }, delay)
    return
  }

  const history = messages.value.slice(0, -1).map(m => ({
    role: m.role,
    content: m.content
  }))

  aiChat({
    question: question,
    userId: userStore.userInfo?.id?.toString() || 'anonymous',
    userName: userStore.userInfo?.realName || '用户',
    history: history
  }).then(res => {
    const answer = res?.data?.answer || '抱歉，无法获取回答'
    const sources = res?.data?.sources || []
    setTimeout(() => {
      messages.value.push({
        role: 'assistant',
        content: answer,
        time: formatTime(new Date()),
        sources: sources
      })
      loading.value = false
      scrollToBottom()
    }, delay)
  }).catch(() => {
    setTimeout(() => {
      messages.value.push({
        role: 'assistant',
        content: '抱歉，AI服务暂时不可用，请稍后再试。',
        time: formatTime(new Date()),
        sources: []
      })
      loading.value = false
      scrollToBottom()
    }, delay)
  })
}

function sendQuickQuestion(question) {
  inputMessage.value = question
  sendMessage()
}

function openWithQuestion(question) {
  if (!question) return
  if (!isExpanded.value) {
    isExpanded.value = true
    unreadCount.value = 0
  }
  inputMessage.value = question
  nextTick(() => {
    sendMessage()
  })
}

function handleAskAi(event) {
  const question = event?.detail?.question
  if (question) {
    openWithQuestion(question)
  }
}

async function loadRecentHistory() {
  try {
    const userId = userStore.userInfo?.id?.toString()
    if (userId) {
      const res = await getRecentHistory({ userId, limit: 10 })
      const history = res?.data || []
      messages.value = history.map(h => ({
        role: h.answer ? 'assistant' : 'user',
        content: h.answer || h.question,
        time: formatTime(h.createTime)
      }))
    }
  } catch (e) {
    console.log('加载历史记录失败')
  }
}

onMounted(() => {
  loadRecentHistory()
  window.addEventListener('ask-ai', handleAskAi)
})

onUnmounted(() => {
  window.removeEventListener('ask-ai', handleAskAi)
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 30px;
  bottom: 30px;
  z-index: 1000;
}

.ai-toggle-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
  position: relative;
}

.ai-toggle-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 25px rgba(59, 130, 246, 0.5);
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

.ai-panel {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  margin-bottom: 16px;
}

.ai-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d4a6f 100%);
}

.ai-logo {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 12px;
}

.ai-title {
  flex: 1;
}

.title-text {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.title-desc {
  display: block;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 2px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.ai-body {
  height: 400px;
  overflow: hidden;
}

.chat-container {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
}

.chat-container::-webkit-scrollbar {
  width: 6px;
}

.chat-container::-webkit-scrollbar-track {
  background: transparent;
}

.chat-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.chat-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.chat-item.user {
  flex-direction: row-reverse;
}

.chat-item.user .chat-bubble {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  color: #fff;
  border-radius: 14px 14px 4px 14px;
}

.chat-item.user .chat-time {
  color: rgba(255, 255, 255, 0.6);
}

.chat-item.ai .chat-bubble {
  background: #fff;
  color: #334155;
  border-radius: 14px 14px 14px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chat-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.chat-item.user .chat-avatar {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  color: #fff;
}

.chat-item.ai .chat-avatar {
  background: #e2e8f0;
  color: #64748b;
}

.chat-bubble {
  max-width: 75%;
  padding: 10px 14px;
}

.chat-content {
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-time {
  font-size: 10px;
  margin-top: 4px;
  text-align: right;
  color: #94a3b8;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 14px 14px 14px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  width: fit-content;
}

.loading-pulse {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  animation: pulse 1.4s infinite ease-in-out;
  box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.5);
}

@keyframes pulse {
  0% {
    transform: scale(0.8);
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.5);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 0 0 8px rgba(59, 130, 246, 0);
    opacity: 1;
  }
  100% {
    transform: scale(0.8);
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0);
    opacity: 0.7;
  }
}

.loading-text {
  font-size: 12px;
  color: #64748b;
}

.chat-sources {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e2e8f0;
}

.sources-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.sources-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.source-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: #f8fafc;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.source-item:hover {
  background: #f1f5f9;
}

.source-title {
  flex: 1;
  font-size: 12px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.expand-icon {
  color: #94a3b8;
  transition: transform 0.2s;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.source-detail {
  padding: 8px 12px;
  background: #f0fdf4;
  border-radius: 6px;
  margin-top: -4px;
}

.source-detail pre {
  font-size: 11px;
  color: #374151;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

.ai-footer {
  padding: 16px;
  border-top: 1px solid #e2e8f0;
}

.input-wrapper {
  display: flex;
  gap: 8px;
}

.chat-input {
  flex: 1;
  height: 40px;
  padding: 0 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
}

.chat-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15), 0 0 12px rgba(139, 92, 246, 0.2);
}

.chat-input::placeholder {
  color: #94a3b8;
}

.send-btn {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quick-questions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 11px;
  color: #94a3b8;
}

.quick-btn {
  padding: 4px 10px;
  background: #f1f5f9;
  border: none;
  border-radius: 12px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #e2e8f0;
  color: #3b82f6;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.msg-fade-enter-active {
  transition: all 0.35s ease;
}

.msg-fade-leave-active {
  transition: all 0.2s ease;
  position: absolute;
}

.msg-fade-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

.msg-fade-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
