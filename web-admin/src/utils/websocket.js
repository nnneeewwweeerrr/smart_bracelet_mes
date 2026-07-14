import { ElNotification } from 'element-plus'

class WebSocketClient {
  constructor() {
    this.ws = null
    this.mockMode = false
    this.mockTimer = null
    this.listeners = {}
    this.connected = false
    this.exceptionCount = 0
    this.messages = []
    this.maxMessages = 50
  }

  connect() {
    if (this.mockMode) {
      this.connected = true
      return
    }
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${protocol}//${window.location.host}/ws/mes`
    this.ws = new WebSocket(wsUrl)
    this.ws.onopen = () => {
      this.connected = true
      console.log('WebSocket connected:', wsUrl)
    }
    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this.handleMessage(data)
      } catch (e) {
        console.error('WebSocket message parse error:', e)
      }
    }
    this.ws.onclose = () => {
      this.connected = false
      console.log('WebSocket disconnected')
      setTimeout(() => this.connect(), 3000)
    }
    this.ws.onerror = (error) => {
      this.connected = false
      console.error('WebSocket error:', error)
      setTimeout(() => this.connect(), 3000)
    }
  }

  handleMessage(data) {
    const { type, payload } = data
    if (this.listeners[type]) {
      this.listeners[type].forEach(cb => cb(payload))
    }
    this.addMessage(type, payload)
  }

  addMessage(type, payload) {
    const now = new Date()
    const message = {
      id: Date.now(),
      type,
      payload,
      time: now.toLocaleString('zh-CN', { hour12: false }),
      timestamp: now.getTime(),
      read: false
    }
    this.messages.unshift(message)
    if (this.messages.length > this.maxMessages) {
      this.messages = this.messages.slice(0, this.maxMessages)
    }
    this.notifyMessageUpdate()
  }

  getMessages() {
    return this.messages
  }

  getUnreadCount() {
    return this.messages.filter(m => !m.read).length
  }

  markAsRead(id) {
    const msg = this.messages.find(m => m.id === id)
    if (msg) {
      msg.read = true
    }
  }

  markAllAsRead() {
    this.messages.forEach(m => m.read = true)
  }

  clearMessages() {
    this.messages = []
    this.notifyMessageUpdate()
  }

  notifyMessageUpdate() {
    window.dispatchEvent(new CustomEvent('message-update', {
      detail: {
        count: this.getUnreadCount(),
        messages: this.messages.slice(0, 10)
      }
    }))
  }

  on(type, callback) {
    if (!this.listeners[type]) {
      this.listeners[type] = []
    }
    this.listeners[type].push(callback)
  }

  off(type, callback) {
    if (!this.listeners[type]) return
    const index = this.listeners[type].indexOf(callback)
    if (index > -1) {
      this.listeners[type].splice(index, 1)
    }
  }

  startMockPush() {
    if (this.mockTimer) clearInterval(this.mockTimer)
    this.mockTimer = setInterval(() => {
      const rand = Math.random()
      if (rand < 0.4) {
        this.mockExceptionPush()
      } else if (rand < 0.9) {
        this.mockProgressPush()
      }
    }, 8000)
  }

  mockExceptionPush() {
    if (this.exceptionCount >= 3) return
    this.exceptionCount++
    const types = [
      { type: 1, typeName: '设备故障' },
      { type: 2, typeName: '质量异常' },
      { type: 3, typeName: '物料短缺' }
    ]
    const t = types[Math.floor(Math.random() * types.length)]
    const workers = ['张操作员', '李质检', '王工人', '赵仓管']
    const workOrders = ['WO-2024001', 'WO-2024004', 'WO-2024006']
    const products = ['智能健康手环 Pro', '智能运动手环 Lite']
    const descs = [
      '设备出现异常告警，需要紧急处理',
      '发现不良品率上升，需排查原因',
      '物料库存不足，可能导致停产',
      '工艺参数波动，需确认调整'
    ]
    const now = new Date()
    const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
    const exceptionNo = 'EX-' + dateStr + String(Math.floor(Math.random() * 900) + 100)

    const data = {
      id: Date.now(),
      exceptionNo,
      workOrderNo: workOrders[Math.floor(Math.random() * workOrders.length)],
      productName: products[Math.floor(Math.random() * products.length)],
      exceptionType: t.type,
      exceptionTypeName: t.typeName,
      exceptionDesc: descs[Math.floor(Math.random() * descs.length)],
      status: 0,
      reporter: workers[Math.floor(Math.random() * workers.length)],
      handler: '',
      createTime: now.toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
      imageUrl: ''
    }

    this.handleMessage({ type: 'NEW_EXCEPTION', payload: data })

    ElNotification({
      title: '新异常告警',
      message: `【${t.typeName}】${data.workOrderNo} - ${data.exceptionDesc.slice(0, 20)}...`,
      type: 'warning',
      duration: 5000,
      onClick: () => {
        window.dispatchEvent(new CustomEvent('navigate-to-exception'))
      }
    })
  }

  mockProgressPush() {
    const workOrders = [
      { id: 1, workOrderNo: 'WO-2024001', addQty: Math.floor(Math.random() * 20) + 5 },
      { id: 4, workOrderNo: 'WO-2024004', addQty: Math.floor(Math.random() * 15) + 3 },
      { id: 6, workOrderNo: 'WO-2024006', addQty: Math.floor(Math.random() * 25) + 8 }
    ]
    const wo = workOrders[Math.floor(Math.random() * workOrders.length)]
    const processes = ['组装', '测试', '包装']
    const processName = processes[Math.floor(Math.random() * processes.length)]
    const worker = ['张工人', '李工人', '王工人'][Math.floor(Math.random() * 3)]

    const data = {
      workOrderId: wo.id,
      workOrderNo: wo.workOrderNo,
      processName,
      addQuantity: wo.addQty,
      worker,
      reportTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    }

    this.handleMessage({ type: 'PROGRESS_UPDATE', payload: data })
  }

  simulateException() {
    this.mockExceptionPush()
  }

  simulateProgress(workOrderId, addQty) {
    const woMap = {
      1: 'WO-2024001',
      4: 'WO-2024004',
      6: 'WO-2024006'
    }
    const data = {
      workOrderId,
      workOrderNo: woMap[workOrderId] || 'WO-2024001',
      processName: '组装',
      addQuantity: addQty || 10,
      worker: '模拟工人',
      reportTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    }
    this.handleMessage({ type: 'PROGRESS_UPDATE', payload: data })
  }

  disconnect() {
    if (this.mockTimer) {
      clearInterval(this.mockTimer)
      this.mockTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.connected = false
  }
}

const wsClient = new WebSocketClient()

export default wsClient
