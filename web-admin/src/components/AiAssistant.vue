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
  '防水测试怎么做？'
]

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
    messages.value.push({
      role: 'assistant',
      content: answer,
      time: formatTime(new Date()),
      sources: sources
    })
  }).catch(() => {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用，请稍后再试。',
      time: formatTime(new Date()),
      sources: []
    })
  }).finally(() => {
    loading.value = false
    scrollToBottom()
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
