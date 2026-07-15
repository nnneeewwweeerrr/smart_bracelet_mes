<template>
  <view class="ai-page">
    <!-- 深色科技背景 -->
    <view class="ai-bg">
      <view class="bg-glow glow-1"></view>
      <view class="bg-glow glow-2"></view>
      <view class="bg-grid"></view>
    </view>

    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <view class="top-info">
        <view class="bot-avatar">
          <text class="bot-avatar-text">AI</text>
          <view class="bot-status"></view>
        </view>
        <view class="top-text">
          <text class="top-title">AI生产助手</text>
          <text class="top-status">在线 · 基于生产知识库</text>
        </view>
      </view>
      <view class="top-actions">
        <view class="top-btn" @click="handleScan">
          <text class="top-btn-icon">扫</text>
        </view>
        <view class="top-btn" @click="clearChat">
          <text class="top-btn-icon">清</text>
        </view>
      </view>
    </view>

    <!-- 快捷问题标签 -->
    <scroll-view class="quick-questions" scroll-x :show-scrollbar="false">
      <view class="quick-list">
        <view
          v-for="(q, i) in quickQuestions"
          :key="i"
          class="quick-tag"
          @click="sendQuickQuestion(q)"
        >
          <text class="quick-tag-text">{{ q.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 对话区域 -->
    <scroll-view
      class="chat-area"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <view class="chat-list">
        <view
          v-for="(msg, index) in messages"
          :key="msg.id"
          class="msg-item"
          :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'"
        >
          <!-- AI 头像 -->
          <view v-if="msg.role === 'ai'" class="msg-avatar">
            <text class="msg-avatar-text">AI</text>
          </view>

          <view class="msg-bubble-wrap">
            <view class="msg-bubble" :class="msg.role === 'user' ? 'bubble-user' : 'bubble-ai'">
              <text class="msg-content">{{ msg.content }}</text>
            </view>

            <!-- AI 引用来源 -->
            <view v-if="msg.role === 'ai' && msg.references && msg.references.length" class="msg-refs">
              <text class="refs-title">引用来源：</text>
              <view class="refs-tags">
                <view v-for="(ref, ri) in msg.references" :key="ri" class="ref-tag">
                  <text class="ref-tag-text">{{ ref }}</text>
                </view>
              </view>
            </view>

            <!-- AI 操作行：语音播报 + 快捷操作 -->
            <view v-if="msg.role === 'ai'" class="msg-actions">
              <view class="action-mini play-btn" @click="playVoice(msg)">
                <text class="action-mini-icon">{{ msg.playing ? '⏸' : '▶' }}</text>
                <text class="action-mini-text">{{ msg.playing ? '停止' : '播报' }}</text>
              </view>
              <view
                v-for="(act, ai) in getQuickActions(msg.content)"
                :key="ai"
                class="action-mini jump-btn"
                @click="jumpTo(act)"
              >
                <text class="action-mini-text">{{ act.label }} ›</text>
              </view>
            </view>
          </view>

          <!-- 用户头像 -->
          <view v-if="msg.role === 'user'" class="msg-avatar msg-avatar-user">
            <text class="msg-avatar-text">我</text>
          </view>
        </view>

        <!-- AI 思考中 -->
        <view v-if="loading" class="msg-item msg-ai">
          <view class="msg-avatar">
            <text class="msg-avatar-text">AI</text>
          </view>
          <view class="msg-bubble-wrap">
            <view class="msg-bubble bubble-ai">
              <view class="typing">
                <view class="typing-dot"></view>
                <view class="typing-dot"></view>
                <view class="typing-dot"></view>
              </view>
            </view>
          </view>
        </view>

        <view v-if="messages.length === 0 && !loading" class="empty-state">
          <view class="empty-icon">
            <text class="empty-icon-text">AI</text>
          </view>
          <text class="empty-title">你好，我是AI生产助手</text>
          <view class="empty-desc">
            <text class="empty-desc-line">可询问生产工艺、报工操作、异常处理等问题</text>
            <text class="empty-desc-line">也可点击上方扫码按钮，扫描工单/SN/物料码自动提问</text>
          </view>
        </view>

        <view class="chat-bottom"></view>
      </view>
    </scroll-view>

    <!-- 底部输入区 -->
    <view class="input-area">
      <view class="input-wrap">
        <view
          class="voice-btn"
          :class="{ 'voice-active': recording }"
          @touchstart.prevent="startVoice"
          @touchend.prevent="stopVoice"
          @touchcancel.prevent="stopVoice"
        >
          <text class="voice-icon">{{ recording ? '■' : '🎤' }}</text>
        </view>
        <input
          v-model="inputText"
          class="text-input"
          type="text"
          placeholder="请输入问题..."
          placeholder-class="input-placeholder"
          confirm-type="send"
          @confirm="sendText"
        />
        <view
          class="send-btn"
          :class="{ 'send-active': inputText.trim() }"
          @click="sendText"
        >
          <text class="send-text">发送</text>
        </view>
      </view>
    </view>

    <!-- 录音遮罩 -->
    <view v-if="recording" class="voice-mask">
      <view class="voice-ripple">
        <view class="ripple-ring r1"></view>
        <view class="ripple-ring r2"></view>
        <view class="ripple-ring r3"></view>
        <view class="voice-mic">
          <text class="voice-mic-icon">🎤</text>
        </view>
      </view>
      <text class="voice-tip">松开结束录音，识别后自动填入</text>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { aiChat, getRecentHistory, recognizeVoice } from '@/api/ai'
import { isLoggedIn } from '@/utils/auth'

const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const recording = ref(false)
const scrollTop = ref(0)

let recorderManager = null
let msgIdSeed = 0
let pageOptions = {}

const quickQuestions = [
  { label: '焊接温度', text: '智能手环焊接工序的推荐温度是多少？' },
  { label: '屏幕气泡', text: '屏幕贴合出现气泡应如何处理？' },
  { label: '报工操作', text: '如何进行生产报工操作？' },
  { label: '防水测试', text: '防水测试的标准和操作步骤是什么？' }
]

const quickActionRules = [
  { keyword: '异常上报', label: '去异常上报', url: '/pages/exception/create' },
  { keyword: '报工', label: '去报工', url: '/pages/production/report' },
  { keyword: '库存查询', label: '库存查询', url: '/pages/stock/index' }
]

function getQuickActions(content) {
  if (!content) return []
  const result = []
  const seen = new Set()
  quickActionRules.forEach(rule => {
    if (content.indexOf(rule.keyword) > -1 && !seen.has(rule.url)) {
      seen.add(rule.url)
      result.push(rule)
    }
  })
  return result
}

function jumpTo(act) {
  uni.navigateTo({ url: act.url })
}

function genId() {
  msgIdSeed += 1
  return 'msg_' + Date.now() + '_' + msgIdSeed
}

function scrollToBottom() {
  nextTick(() => {
    scrollTop.value = scrollTop.value === 99998 ? 99999 : 99998
  })
}

function pushMessage(msg) {
  messages.value.push(msg)
  scrollToBottom()
}

async function loadHistory() {
  try {
    const data = await getRecentHistory({ limit: 20 })
    const list = Array.isArray(data) ? data : (data && data.records) || []
    if (list.length) {
      messages.value = list.map(item => ({
        id: genId(),
        role: item.role || 'ai',
        content: item.content || '',
        references: item.references || [],
        playing: false
      }))
      scrollToBottom()
    }
  } catch {
    // keep empty
  }
}

async function sendText() {
  const text = inputText.value.trim()
  if (!text || loading.value) return
  inputText.value = ''
  await chat(text)
}

async function sendQuickQuestion(q) {
  if (loading.value) return
  await chat(q.text)
}

async function chat(question) {
  pushMessage({ id: genId(), role: 'user', content: question, playing: false })
  loading.value = true
  try {
    const data = await aiChat({ question })
    loading.value = false
    pushMessage({
      id: genId(),
      role: 'ai',
      content: data.answer || data.content || '抱歉，我暂时无法回答这个问题。',
      references: data.references || data.sources || [],
      playing: false
    })
  } catch {
    loading.value = false
    pushMessage({
      id: genId(),
      role: 'ai',
      content: '服务繁忙，请稍后再试。',
      references: [],
      playing: false
    })
  }
}

function clearChat() {
  if (messages.value.length === 0) return
  uni.showModal({
    title: '清空对话',
    content: '确定要清空当前对话记录吗？',
    success: (res) => {
      if (res.confirm) {
        messages.value = []
      }
    }
  })
}

// 扫码联动
function handleScan() {
  uni.scanCode({
    onlyFromCamera: false,
    scanType: ['qrCode', 'barCode'],
    success: (res) => {
      const code = res.result || ''
      const question = buildQuestionFromCode(code)
      if (question) {
        chat(question)
      } else {
        uni.showToast({ title: '未识别的码内容', icon: 'none' })
      }
    },
    fail: (err) => {
      if (err && err.errMsg && err.errMsg.indexOf('cancel') === -1) {
        uni.showToast({ title: '扫码失败', icon: 'none' })
      }
    }
  })
}

function buildQuestionFromCode(code) {
  if (!code) return ''
  const upper = code.toUpperCase()
  if (upper.indexOf('WO') === 0) {
    return '工单' + code + '的生产进度如何？'
  }
  if (upper.indexOf('SN') === 0) {
    return 'SN码' + code + '的生产追溯信息？'
  }
  if (upper.indexOf('M/BATCH') === 0 || upper.indexOf('BATCH') === 0) {
    const batchNo = code.replace(/^M\//i, '').replace(/^BATCH/i, '')
    return '物料' + (batchNo || code) + '的库存信息？'
  }
  return '请查询"' + code + '"的相关信息'
}

function initRecorder() {
  if (recorderManager) return recorderManager
  try {
    recorderManager = uni.getRecorderManager()
    recorderManager.onStart(() => {
      console.log('录音开始')
    })
    recorderManager.onStop((res) => {
      console.log('录音停止, tempFilePath:', res.tempFilePath)
      recording.value = false
      handleRecognize(res.tempFilePath)
    })
    recorderManager.onError((err) => {
      console.error('录音错误:', err)
      recording.value = false
      uni.showToast({ title: '录音失败', icon: 'none' })
    })
  } catch (e) {
    console.error('初始化录音管理器失败:', e)
    recorderManager = null
  }
  return recorderManager
}

function startVoice() {
  if (loading.value) return
  
  uni.getSetting({
    success: (res) => {
      const authSetting = res.authSetting || {}
      const recordAuth = authSetting['scope.record']
      
      if (recordAuth === true) {
        doStartRecording()
      } else if (recordAuth === undefined) {
        uni.authorize({
          scope: 'scope.record',
          success: () => {
            doStartRecording()
          },
          fail: () => {
            uni.showModal({
              title: '录音权限',
              content: '需要录音权限才能使用语音功能，请点击确定前往设置开启',
              showCancel: false,
              success: () => {
                uni.openSetting({
                  success: (settingRes) => {
                    if (settingRes.authSetting && settingRes.authSetting['scope.record'] === true) {
                      doStartRecording()
                    }
                  }
                })
              }
            })
          }
        })
      } else {
        uni.showModal({
          title: '录音权限',
          content: '需要录音权限才能使用语音功能，请点击确定前往设置开启',
          showCancel: false,
          success: () => {
            uni.openSetting({
              success: (settingRes) => {
                if (settingRes.authSetting && settingRes.authSetting['scope.record'] === true) {
                  doStartRecording()
                }
              }
            })
          }
        })
      }
    },
    fail: () => {
      uni.showToast({ title: '获取权限设置失败', icon: 'none' })
    }
  })
}

function doStartRecording() {
  const rm = initRecorder()
  if (!rm) {
    uni.showToast({ title: '当前环境不支持录音', icon: 'none' })
    return
  }
  recording.value = true
  try {
    rm.start({ duration: 60000, format: 'mp3', sampleRate: 16000, numberOfChannels: 1 })
  } catch {
    recording.value = false
    uni.showToast({ title: '录音启动失败', icon: 'none' })
  }
}

function stopVoice() {
  if (!recording.value) return
  const rm = initRecorder()
  if (rm) {
    try {
      rm.stop()
    } catch (e) {
      console.error('停止录音失败:', e)
      recording.value = false
    }
  } else {
    recording.value = false
  }
}

async function handleRecognize(filePath) {
  if (!filePath) {
    uni.showToast({ title: '录音文件无效', icon: 'none' })
    return
  }
  uni.showLoading({ title: '识别中...' })
  try {
    console.log('开始语音识别, filePath:', filePath)
    const data = await recognizeVoice(filePath)
    console.log('语音识别结果:', data)
    uni.hideLoading()
    const text = data.text || data.content || ''
    console.log('识别到的文本:', text)
    if (text) {
      inputText.value = text
      uni.showToast({ title: '已识别', icon: 'success' })
    } else {
      uni.showToast({ title: '未识别到内容', icon: 'none' })
    }
  } catch (e) {
    console.error('语音识别失败:', e)
    uni.hideLoading()
    uni.showToast({ title: '语音识别失败', icon: 'none' })
  }
}

let audioContext = null

function initAudioContext() {
  if (audioContext) return audioContext
  audioContext = uni.createInnerAudioContext()
  audioContext.loop = false
  return audioContext
}

async function playVoice(msg) {
  const isApp = typeof plus !== 'undefined' && plus.speech
  
  if (msg.playing) {
    if (isApp) {
      try { plus.speech.stopSpeak() } catch { /* ignore */ }
    }
    if (audioContext) {
      try { audioContext.stop() } catch { /* ignore */ }
    }
    msg.playing = false
    return
  }
  
  messages.value.forEach(m => { m.playing = false })
  msg.playing = true
  
  if (isApp) {
    try {
      plus.speech.startSpeak({ text: msg.content }, () => {
        msg.playing = false
      }, () => {
        msg.playing = false
      })
    } catch {
      msg.playing = false
      uni.showToast({ title: '语音合成不可用', icon: 'none' })
    }
  } else {
    await playMiniProgramVoice(msg)
  }
}

async function playMiniProgramVoice(msg) {
  try {
    console.log('开始语音合成, 文本:', msg.content)
    
    const { post } = await import('@/utils/request')
    const data = await post('/ai/voice/synthesize', { text: msg.content })
    
    console.log('语音合成结果:', data)
    
    if (data && data.audioUrl) {
      const baseUrl = 'http://192.168.110.90:8080'
      const fullUrl = data.audioUrl.startsWith('http') ? data.audioUrl : baseUrl + data.audioUrl
      
      console.log('音频URL:', fullUrl)
      
      const ac = uni.createInnerAudioContext()
      
      ac.onError = (err) => {
        console.error('音频播放错误:', err)
        msg.playing = false
        uni.showToast({ title: '音频播放失败', icon: 'none' })
      }
      
      ac.onEnded = () => {
        console.log('音频播放结束')
        msg.playing = false
        ac.destroy()
      }
      
      ac.onStop = () => {
        console.log('音频播放停止')
        msg.playing = false
        ac.destroy()
      }
      
      ac.onLoadedMetadata = () => {
        console.log('音频元数据加载完成')
      }
      
      ac.onCanplay = () => {
        console.log('音频可以播放')
        setTimeout(() => {
          ac.play()
        }, 100)
      }
      
      ac.src = fullUrl
      
      setTimeout(() => {
        ac.play()
      }, 500)
    } else {
      msg.playing = false
      uni.showToast({ title: '语音合成失败', icon: 'none' })
    }
  } catch (e) {
    console.error('语音合成错误:', e)
    msg.playing = false
    uni.showToast({ title: '语音合成不可用', icon: 'none' })
  }
}

onLoad((options) => {
  pageOptions = options || {}
})

onShow(() => {
  if (!isLoggedIn()) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  if (messages.value.length === 0) {
    loadHistory()
  }
  // 带参扫码进入时自动提问
  if (pageOptions.code && messages.value.length === 0) {
    const q = buildQuestionFromCode(decodeURIComponent(pageOptions.code))
    if (q) {
      nextTick(() => chat(q))
      pageOptions = {}
    }
  }
})
</script>

<style lang="scss" scoped>
.ai-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
  background: #0f1929;
}

/* 深色科技背景 */
.ai-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  background: linear-gradient(180deg, #0f1929 0%, #16243f 50%, #1e3a5f 100%);
  overflow: hidden;
}

.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60rpx);
}

.glow-1 {
  width: 500rpx;
  height: 500rpx;
  top: -150rpx;
  right: -120rpx;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.25) 0%, transparent 70%);
  animation: glowFloat 10s ease-in-out infinite;
}

.glow-2 {
  width: 400rpx;
  height: 400rpx;
  bottom: 100rpx;
  left: -100rpx;
  background: radial-gradient(circle, rgba(59, 124, 255, 0.2) 0%, transparent 70%);
  animation: glowFloat 12s ease-in-out infinite reverse;
}

.bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.04) 1rpx, transparent 1rpx),
    linear-gradient(90deg, rgba(0, 212, 255, 0.04) 1rpx, transparent 1rpx);
  background-size: 60rpx 60rpx;
}

@keyframes glowFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(40rpx, -30rpx); }
}

/* 顶部操作栏 */
.top-bar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg;
  background: rgba(15, 25, 41, 0.6);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(0, 212, 255, 0.15);
}

.top-info {
  display: flex;
  align-items: center;
}

.bot-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, #00d4ff 0%, #3B7CFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;
  position: relative;
  box-shadow: 0 0 24rpx rgba(0, 212, 255, 0.4);
}

.bot-avatar-text {
  font-size: $font-size-md;
  font-weight: 700;
  color: #ffffff;
}

.bot-status {
  position: absolute;
  bottom: -2rpx;
  right: -2rpx;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #52C41A;
  border: 3rpx solid #0f1929;
  box-shadow: 0 0 8rpx rgba(82, 196, 26, 0.6);
}

.top-text {
  display: flex;
  flex-direction: column;
}

.top-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: #ffffff;
}

.top-status {
  font-size: $font-size-xs;
  color: rgba(0, 212, 255, 0.8);
  margin-top: 4rpx;
}

.top-actions {
  display: flex;
  gap: $spacing-sm;
}

.top-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: $radius-md;
  background: rgba(0, 212, 255, 0.12);
  border: 1rpx solid rgba(0, 212, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    background: rgba(0, 212, 255, 0.25);
    transform: scale(0.95);
  }
}

.top-btn-icon {
  font-size: $font-size-base;
  color: #00d4ff;
  font-weight: 600;
}

/* 快捷问题 */
.quick-questions {
  position: relative;
  z-index: 2;
  white-space: nowrap;
  padding: $spacing-md $spacing-lg;
  background: rgba(15, 25, 41, 0.3);
}

.quick-list {
  display: inline-flex;
  gap: $spacing-md;
}

.quick-tag {
  display: inline-flex;
  align-items: center;
  padding: $spacing-xs $spacing-lg;
  border-radius: $radius-full;
  background: rgba(0, 212, 255, 0.1);
  border: 1rpx solid rgba(0, 212, 255, 0.3);
  flex-shrink: 0;

  &:active {
    background: rgba(0, 212, 255, 0.25);
    transform: scale(0.96);
  }
}

.quick-tag-text {
  font-size: $font-size-sm;
  color: #00d4ff;
  white-space: nowrap;
}

/* 对话区域 */
.chat-area {
  position: relative;
  z-index: 1;
  flex: 1;
  padding: $spacing-md $spacing-lg;
  box-sizing: border-box;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.chat-area::-webkit-scrollbar {
  display: none;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  padding-bottom: 40rpx;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  animation: msgIn 0.3s ease-out;
}

.msg-user {
  flex-direction: row-reverse;
}

@keyframes msgIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: $radius-md;
  background: linear-gradient(135deg, #00d4ff 0%, #3B7CFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 0 16rpx rgba(0, 212, 255, 0.35);
}

.msg-avatar-user {
  background: linear-gradient(135deg, #3B7CFF 0%, #6BA1FF 100%);
}

.msg-avatar-text {
  font-size: $font-size-sm;
  font-weight: 700;
  color: #ffffff;
}

.msg-bubble-wrap {
  max-width: 76%;
  display: flex;
  flex-direction: column;
}

.msg-user .msg-bubble-wrap {
  align-items: flex-end;
}

.msg-bubble {
  padding: $spacing-md $spacing-lg;
  border-radius: $radius-lg;
  position: relative;
  word-break: break-all;
}

.bubble-ai {
  background: rgba(255, 255, 255, 0.06);
  border: 1rpx solid rgba(0, 212, 255, 0.2);
  border-top-left-radius: $radius-xs;
  backdrop-filter: blur(10px);
}

.bubble-user {
  background: linear-gradient(135deg, #3B7CFF 0%, #6BA1FF 100%);
  border-top-right-radius: $radius-xs;
  box-shadow: 0 6rpx 20rpx rgba(59, 124, 255, 0.3);
}

.msg-content {
  font-size: $font-size-base;
  line-height: $line-height-relaxed;
  color: rgba(255, 255, 255, 0.92);
  white-space: pre-wrap;
}

.bubble-user .msg-content {
  color: #ffffff;
}

/* 引用来源 */
.msg-refs {
  margin-top: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: rgba(0, 212, 255, 0.06);
  border-radius: $radius-sm;
  border: 1rpx solid rgba(0, 212, 255, 0.12);
}

.refs-title {
  font-size: $font-size-xs;
  color: rgba(0, 212, 255, 0.8);
  display: block;
  margin-bottom: 6rpx;
}

.refs-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.ref-tag {
  padding: 2rpx $spacing-sm;
  border-radius: $radius-xs;
  background: rgba(0, 212, 255, 0.15);
}

.ref-tag-text {
  font-size: $font-size-xs;
  color: #00d4ff;
}

/* 操作行 */
.msg-actions {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin-top: $spacing-sm;
}

.action-mini {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx $spacing-md;
  border-radius: $radius-full;
  background: rgba(255, 255, 255, 0.08);
  border: 1rpx solid rgba(255, 255, 255, 0.15);

  &:active {
    transform: scale(0.95);
  }
}

.play-btn {
  background: rgba(0, 212, 255, 0.12);
  border-color: rgba(0, 212, 255, 0.3);
}

.action-mini-icon {
  font-size: $font-size-xs;
  color: #00d4ff;
}

.play-btn .action-mini-text {
  color: #00d4ff;
}

.action-mini-text {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.85);
}

.jump-btn {
  background: rgba(59, 124, 255, 0.18);
  border-color: rgba(59, 124, 255, 0.4);

  .action-mini-text {
    color: #6BA1FF;
  }
}

/* 思考动画 */
.typing {
  display: flex;
  gap: 10rpx;
  padding: 8rpx 0;
}

.typing-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #00d4ff;
  animation: typingBounce 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-12rpx); opacity: 1; }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx $spacing-xl;
  text-align: center;
}

.empty-icon {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2) 0%, rgba(59, 124, 255, 0.1) 100%);
  border: 2rpx solid rgba(0, 212, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-xl;
  box-shadow: 0 0 40rpx rgba(0, 212, 255, 0.2);
}

.empty-icon-text {
  font-size: $font-size-display;
  font-weight: 700;
  color: #00d4ff;
}

.empty-title {
  font-size: $font-size-lg;
  color: #ffffff;
  font-weight: 600;
  margin-bottom: $spacing-md;
}

.empty-desc {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.empty-desc-line {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.55);
  line-height: $line-height-relaxed;
  text-align: center;
}

.chat-bottom {
  height: 20rpx;
}

/* 底部输入区 */
.input-area {
  position: relative;
  z-index: 2;
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  background: rgba(15, 25, 41, 0.85);
  backdrop-filter: blur(20px);
  border-top: 1rpx solid rgba(0, 212, 255, 0.15);
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.voice-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(0, 212, 255, 0.12);
  border: 1rpx solid rgba(0, 212, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all $transition-fast;

  &:active {
    transform: scale(0.92);
  }
}

.voice-active {
  background: rgba(255, 77, 79, 0.3);
  border-color: #FF4D4F;
  box-shadow: 0 0 24rpx rgba(255, 77, 79, 0.5);
}

.voice-icon {
  font-size: $font-size-lg;
}

.text-input {
  flex: 1;
  height: 72rpx;
  padding: 0 $spacing-lg;
  background: rgba(255, 255, 255, 0.08);
  border: 1rpx solid rgba(255, 255, 255, 0.12);
  border-radius: $radius-full;
  font-size: $font-size-base;
  color: #ffffff;
  box-sizing: border-box;
}

.input-placeholder {
  color: rgba(255, 255, 255, 0.35);
}

.send-btn {
  height: 72rpx;
  padding: 0 $spacing-xl;
  border-radius: $radius-full;
  background: rgba(59, 124, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all $transition-fast;

  &:active {
    transform: scale(0.95);
  }
}

.send-active {
  background: linear-gradient(135deg, #3B7CFF 0%, #6BA1FF 100%);
  box-shadow: 0 4rpx 16rpx rgba(59, 124, 255, 0.4);
}

.send-text {
  font-size: $font-size-base;
  color: #ffffff;
  font-weight: 500;
}

/* 录音遮罩 */
.voice-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  background: rgba(15, 25, 41, 0.85);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.voice-ripple {
  position: relative;
  width: 240rpx;
  height: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-xl;
}

.ripple-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(0, 212, 255, 0.6);
  transform: translate(-50%, -50%);
  animation: rippleOut 1.8s ease-out infinite;
}

.r2 { animation-delay: 0.6s; }
.r3 { animation-delay: 1.2s; }

@keyframes rippleOut {
  0% {
    width: 120rpx;
    height: 120rpx;
    opacity: 0.8;
  }
  100% {
    width: 320rpx;
    height: 320rpx;
    opacity: 0;
  }
}

.voice-mic {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d4ff 0%, #3B7CFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 40rpx rgba(0, 212, 255, 0.6);
  z-index: 1;
}

.voice-mic-icon {
  font-size: 56rpx;
}

.voice-tip {
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.8);
}
</style>
