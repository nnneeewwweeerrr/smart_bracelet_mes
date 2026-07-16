<template>
  <div class="page-container">
    <div class="avatar-header">
      <div class="avatar-title">
        <el-icon size="24" color="#409EFF"><Monitor /></el-icon>
        <span>质量管理数字人 — 小智</span>
      </div>
      <div class="avatar-subtitle">AI 质量助手 · 实时播报质检动态</div>
    </div>

    <div class="main-content">
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <div class="avatar-body">
            <div class="avatar-head">
              <div class="avatar-face">
                <div class="eye left" :class="{ blink: isBlinking }">
                  <div class="pupil"></div>
                </div>
                <div class="eye right" :class="{ blink: isBlinking }">
                  <div class="pupil"></div>
                </div>
                <div class="mouth" :class="mouthExpression"></div>
              </div>
              <div class="antenna">
                <div class="antenna-top" :class="{ active: isSpeaking }"></div>
              </div>
              <div class="ear left"></div>
              <div class="ear right"></div>
            </div>
            <div class="avatar-chest">
              <div class="screen">
                <div class="heart-rate">
                  <div class="line" v-for="n in 5" :key="n"></div>
                </div>
              </div>
            </div>
            <div class="avatar-shadow"></div>
          </div>
          <div class="avatar-name">小智</div>
          <div class="avatar-role">质量助手</div>
        </div>

        <div class="speech-section">
          <div class="speech-bubble">
            <div class="bubble-tail"></div>
            <div class="speech-content">
              <div v-if="currentReport" class="typing-text">{{ displayedText }}<span class="cursor">|</span></div>
              <div v-else class="loading-text">正在分析质检数据...</div>
            </div>
          </div>
          <div class="speech-controls">
            <el-button type="primary" size="small" :icon="isPlaying ? 'VideoPause' : 'VideoPlay'" @click="togglePlay">
              {{ isPlaying ? '暂停播报' : '继续播报' }}
            </el-button>
            <el-button size="small" icon="Refresh" @click="replay">重新播报</el-button>
          </div>
        </div>
      </div>

      <div class="stats-section">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon blue"><el-icon><DocumentChecked /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ reportData.totalTasks || 0 }}</div>
                <div class="stat-label">质检任务总数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon green"><el-icon><CircleCheck /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ reportData.passRate || '100.0' }}%</div>
                <div class="stat-label">质检合格率</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon red"><el-icon><CircleClose /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ reportData.failResults || 0 }}</div>
                <div class="stat-label">不合格项</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon orange"><el-icon><Warning /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ reportData.pendingExceptions || 0 }}</div>
                <div class="stat-label">待处理异常</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="detail-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="never" class="alert-card">
              <template #header>
                <div class="card-header">
                  <el-icon color="#E6A23C"><Bell /></el-icon>
                  <span>重点关注</span>
                </div>
              </template>
              <div v-if="alerts.length > 0" class="alert-list">
                <div v-for="(alert, index) in alerts" :key="index" class="alert-item">
                  <div class="alert-dot"></div>
                  <div class="alert-text">{{ alert }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无重点关注事项" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="exception-card">
              <template #header>
                <div class="card-header">
                  <el-icon color="#F56C6C"><WarningFilled /></el-icon>
                  <span>近期异常</span>
                </div>
              </template>
              <div v-if="recentExceptions.length > 0" class="exception-list">
                <div v-for="(item, index) in recentExceptions" :key="index" class="exception-item">
                  <div class="exception-tag">{{ item.type }}</div>
                  <div class="exception-title" :title="item.title">{{ item.title }}</div>
                  <el-tag size="small" :type="getExceptionStatusType(item.status)">{{ item.status }}</el-tag>
                </div>
              </div>
              <el-empty v-else description="暂无异常记录" />
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvatarReport } from '@/api/qualityStatistics'

const loading = ref(false)
const isPlaying = ref(true)
const isSpeaking = ref(false)
const isBlinking = ref(false)
const mouthExpression = ref('smile')

const reportData = reactive({
  totalTasks: 0,
  passRate: '100.0',
  failResults: 0,
  pendingExceptions: 0
})

const allTexts = ref([])
const alerts = ref([])
const recentExceptions = ref([])

const currentReport = ref('')
const displayedText = ref('')
let typeIndex = 0
let textIndex = 0
let typeTimer = null
let blinkTimer = null
let speakTimer = null

function fetchData() {
  loading.value = true
  getAvatarReport().then(res => {
    const data = res.data || {}
    Object.assign(reportData, data)
    allTexts.value = data.reports || []
    alerts.value = data.alerts || []
    recentExceptions.value = data.recentExceptions || []
    startReport()
    loading.value = false
  }).catch(err => {
    ElMessage.error('加载质检数据失败: ' + (err.message || ''))
    loading.value = false
  })
}

function startReport() {
  textIndex = 0
  typeIndex = 0
  displayedText.value = ''
  if (allTexts.value.length > 0) {
    currentReport.value = allTexts.value[0]
    isPlaying.value = true
    startTyping()
  }
}

function startTyping() {
  clearTimeout(typeTimer)
  if (!isPlaying.value) return
  if (textIndex >= allTexts.value.length) {
    textIndex = 0
  }
  currentReport.value = allTexts.value[textIndex]
  isSpeaking.value = true
  mouthExpression.value = 'talk'
  typeNextChar()
}

function typeNextChar() {
  if (!isPlaying.value) return
  const text = allTexts.value[textIndex]
  if (typeIndex < text.length) {
    displayedText.value = text.substring(0, typeIndex + 1)
    typeIndex++
    typeTimer = setTimeout(typeNextChar, 45 + Math.random() * 30)
  } else {
    isSpeaking.value = false
    mouthExpression.value = 'smile'
    typeTimer = setTimeout(() => {
      typeIndex = 0
      displayedText.value = ''
      textIndex++
      if (textIndex < allTexts.value.length) {
        startTyping()
      } else {
        textIndex = 0
        startTyping()
      }
    }, 3000)
  }
}

function togglePlay() {
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    if (typeIndex >= currentReport.value.length) {
      typeIndex = 0
      displayedText.value = ''
      textIndex = (textIndex + 1) % allTexts.value.length
    }
    startTyping()
  } else {
    clearTimeout(typeTimer)
    isSpeaking.value = false
    mouthExpression.value = 'smile'
  }
}

function replay() {
  clearTimeout(typeTimer)
  textIndex = 0
  typeIndex = 0
  displayedText.value = ''
  isPlaying.value = true
  startTyping()
}

function startBlink() {
  blinkTimer = setInterval(() => {
    isBlinking.value = true
    setTimeout(() => { isBlinking.value = false }, 200)
  }, 3000 + Math.random() * 2000)
}

function getExceptionStatusType(status) {
  const map = { '待处理': 'danger', '处理中': 'warning', '已处理': 'success', '已关闭': 'info', '已取消': 'info' }
  return map[status] || 'info'
}

onMounted(() => {
  fetchData()
  startBlink()
})

onUnmounted(() => {
  clearTimeout(typeTimer)
  clearInterval(blinkTimer)
  clearTimeout(speakTimer)
})
</script>

<style scoped>
.page-container {
  width: 100%;
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  padding: 20px;
}

.avatar-header {
  text-align: center;
  margin-bottom: 24px;
}
.avatar-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.avatar-subtitle {
  font-size: 14px;
  color: #909399;
  margin-top: 6px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

.avatar-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.avatar-body {
  position: relative;
  width: 160px;
  height: 200px;
}

.avatar-head {
  width: 120px;
  height: 110px;
  background: linear-gradient(145deg, #409EFF, #66b1ff);
  border-radius: 30px 30px 24px 24px;
  position: absolute;
  top: 0;
  left: 20px;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
}

.avatar-face {
  position: absolute;
  top: 32px;
  left: 20px;
  width: 80px;
  height: 50px;
}

.eye {
  position: absolute;
  width: 22px;
  height: 28px;
  background: #fff;
  border-radius: 50%;
  top: 0;
  overflow: hidden;
  transition: height 0.15s;
}
.eye.blink {
  height: 3px;
  margin-top: 12px;
}
.eye.left { left: 6px; }
.eye.right { right: 6px; }

.pupil {
  width: 12px;
  height: 14px;
  background: #303133;
  border-radius: 50%;
  position: absolute;
  top: 8px;
  left: 5px;
  animation: lookAround 4s ease-in-out infinite;
}

@keyframes lookAround {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(2px); }
  75% { transform: translateX(-2px); }
}

.mouth {
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 10px;
  background: #fff;
  border-radius: 0 0 14px 14px;
  transition: all 0.3s;
}
.mouth.smile {
  height: 12px;
  border-radius: 0 0 16px 16px;
}
.mouth.talk {
  height: 16px;
  width: 22px;
  border-radius: 50%;
  animation: talkAnim 0.4s ease-in-out infinite alternate;
}
@keyframes talkAnim {
  from { height: 10px; width: 24px; }
  to { height: 18px; width: 18px; }
}

.antenna {
  position: absolute;
  top: -18px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 18px;
  background: #409EFF;
}
.antenna-top {
  width: 14px;
  height: 14px;
  background: #c0c4cc;
  border-radius: 50%;
  position: absolute;
  top: -10px;
  left: -4px;
  transition: background 0.3s;
}
.antenna-top.active {
  background: #67C23A;
  box-shadow: 0 0 10px #67C23A;
  animation: antennaPulse 0.6s ease-in-out infinite alternate;
}
@keyframes antennaPulse {
  from { box-shadow: 0 0 5px #67C23A; }
  to { box-shadow: 0 0 15px #67C23A, 0 0 25px #67C23A; }
}

.ear {
  position: absolute;
  width: 14px;
  height: 30px;
  background: #409EFF;
  border-radius: 8px;
  top: 36px;
}
.ear.left { left: -8px; }
.ear.right { right: -8px; }

.avatar-chest {
  width: 90px;
  height: 70px;
  background: linear-gradient(145deg, #ecf5ff, #d9ecff);
  border-radius: 16px;
  position: absolute;
  bottom: 10px;
  left: 35px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.screen {
  width: 60px;
  height: 36px;
  background: #303133;
  border-radius: 6px;
  position: absolute;
  top: 12px;
  left: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.heart-rate {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 20px;
}
.heart-rate .line {
  width: 3px;
  background: #67C23A;
  border-radius: 2px;
  animation: heartBeat 0.8s ease-in-out infinite alternate;
}
.heart-rate .line:nth-child(1) { height: 6px; animation-delay: 0s; }
.heart-rate .line:nth-child(2) { height: 14px; animation-delay: 0.1s; }
.heart-rate .line:nth-child(3) { height: 20px; animation-delay: 0.2s; }
.heart-rate .line:nth-child(4) { height: 10px; animation-delay: 0.3s; }
.heart-rate .line:nth-child(5) { height: 6px; animation-delay: 0.4s; }

@keyframes heartBeat {
  from { opacity: 0.4; transform: scaleY(0.7); }
  to { opacity: 1; transform: scaleY(1); }
}

.avatar-shadow {
  width: 100px;
  height: 12px;
  background: rgba(0,0,0,0.08);
  border-radius: 50%;
  position: absolute;
  bottom: 0;
  left: 30px;
}

.avatar-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-top: 12px;
}
.avatar-role {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.speech-section {
  flex: 1;
  max-width: 600px;
}

.speech-bubble {
  position: relative;
  background: #fff;
  border: 2px solid #e4e7ed;
  border-radius: 16px;
  padding: 24px 28px;
  min-height: 140px;
  margin-bottom: 16px;
}
.bubble-tail {
  position: absolute;
  left: -12px;
  top: 40px;
  width: 0;
  height: 0;
  border-top: 10px solid transparent;
  border-bottom: 10px solid transparent;
  border-right: 12px solid #e4e7ed;
}
.bubble-tail::after {
  content: '';
  position: absolute;
  left: 2px;
  top: -9px;
  width: 0;
  height: 0;
  border-top: 9px solid transparent;
  border-bottom: 9px solid transparent;
  border-right: 11px solid #fff;
}

.speech-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  min-height: 80px;
}
.typing-text {
  white-space: pre-wrap;
}
.cursor {
  color: #409EFF;
  animation: cursorBlink 0.8s infinite;
}
@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.loading-text {
  color: #909399;
  font-style: italic;
}

.speech-controls {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.stat-icon.blue { background: #ecf5ff; color: #409EFF; }
.stat-icon.green { background: #f0f9eb; color: #67C23A; }
.stat-icon.red { background: #fef0f0; color: #F56C6C; }
.stat-icon.orange { background: #fdf6ec; color: #E6A23C; }
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.detail-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 15px;
}

.alert-card, .exception-card {
  border-radius: 12px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  background: #fdf6ec;
  border-radius: 8px;
  border-left: 3px solid #E6A23C;
}
.alert-dot {
  width: 8px;
  height: 8px;
  background: #E6A23C;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}
.alert-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.exception-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.exception-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 8px;
}
.exception-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: #fef0f0;
  color: #F56C6C;
  border-radius: 4px;
  flex-shrink: 0;
}
.exception-title {
  flex: 1;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
