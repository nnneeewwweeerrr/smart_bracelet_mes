<template>
  <div class="digital-twin-screen">
    <!-- 3D场景容器 - 必须在最底层 -->
    <div ref="sceneRef" class="scene-container"></div>

    <!-- 顶部数据面板 -->
    <div class="top-panel">
      <div class="top-left">
        <div class="title-group">
          <h1 class="main-title">智能手环MES</h1>
          <h2 class="sub-title">3D数字孪生产线</h2>
        </div>
        <div class="time-info">
          <span class="date">{{ currentDate }}</span>
        </div>
      </div>
      <div class="top-center">
        <div class="data-card" v-for="card in topCards" :key="card.label">
          <div class="data-icon" :class="card.cls"></div>
          <div class="data-content">
            <div class="data-label">{{ card.label }}</div>
            <div class="data-value" :class="card.warn ? 'warning' : ''">{{ card.value }}</div>
          </div>
        </div>
      </div>
      <div class="top-right">
        <div class="control-group">
          <button class="ctrl-btn" :class="{ active: autoDemo }" @click="toggleAutoDemo">{{ autoDemo ? '⏸ 暂停演示' : '▶ 自动演示' }}</button>
          <button class="ctrl-btn" :class="{ active: autoCamera }" @click="toggleAutoCamera">{{ autoCamera ? '⏹ 停止巡航' : '🎥 自动巡航' }}</button>
          <button class="ctrl-btn" @click="resetCamera">🔄 复位视角</button>
          <button class="back-btn" @click="goBack">← 返回</button>
        </div>
        <div class="time-display">{{ currentTime }}</div>
      </div>
    </div>

    <!-- 右侧工位面板 -->
    <div class="side-panel">
      <div class="panel-header">
        <span class="panel-title">工位状态</span>
        <span class="panel-count">{{ workstations.length }}个工位</span>
      </div>
      <div class="workstation-list">
        <div v-for="ws in workstations" :key="ws.id" class="ws-item" :class="ws.statusClass" @click="focusWorkstation(ws.id)">
          <div class="ws-indicator"></div>
          <div class="ws-info">
            <div class="ws-name">{{ ws.name }}</div>
            <div class="ws-meta">{{ ws.code }} · 在制: {{ ws.wip }}</div>
          </div>
          <div class="ws-status">{{ ws.statusText }}</div>
        </div>
      </div>
    </div>

    <!-- 悬浮详情面板 -->
    <transition name="fade">
      <div v-if="selectedWorkstation" class="detail-panel">
        <div class="detail-header">
          <span class="detail-title">{{ selectedWorkstation.name }}</span>
          <button class="close-btn" @click="selectedWorkstation = null">×</button>
        </div>
        <div class="detail-body">
          <div class="detail-row" v-for="row in detailRows" :key="row.label">
            <span class="detail-label">{{ row.label }}</span>
            <span class="detail-value" :class="row.cls || ''">{{ row.value }}</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 底部图例 -->
    <div class="bottom-legend">
      <div class="legend-item"><div class="legend-dot running"></div><span>运行中</span></div>
      <div class="legend-item"><div class="legend-dot standby"></div><span>待机</span></div>
      <div class="legend-item"><div class="legend-dot fault"></div><span>故障</span></div>
      <div class="legend-item"><div class="legend-line"></div><span>传送带</span></div>
      <div class="legend-item"><div class="legend-box"></div><span>产品物料</span></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

const router = useRouter()
const sceneRef = ref(null)
const selectedWorkstation = ref(null)
const autoDemo = ref(false)
const autoCamera = ref(false)
const currentDate = ref('')
const currentTime = ref('')

let scene, camera, renderer, controls
let workstationGroups = {}
let productMeshes = []
let animationId = null
let demoTimer = null
let cameraTimer = null
let clock = null

const workstations = ref([
  { id: 'w1', name: 'SMT贴片', code: 'SMT-001', status: 1, wip: 120, todayOutput: 850, yield: 98.2, runTime: 6.5, statusClass: 'running', statusText: '运行中' },
  { id: 'w2', name: '回流焊接', code: 'REF-001', status: 1, wip: 85, todayOutput: 790, yield: 97.5, runTime: 6.2, statusClass: 'running', statusText: '运行中' },
  { id: 'w3', name: '点胶固化', code: 'GLU-001', status: 0, wip: 0, todayOutput: 720, yield: 99.0, runTime: 5.8, statusClass: 'standby', statusText: '待机' },
  { id: 'w4', name: '屏幕贴合', code: 'BOND-001', status: 1, wip: 200, todayOutput: 680, yield: 96.8, runTime: 5.5, statusClass: 'running', statusText: '运行中' },
  { id: 'w5', name: '功能测试', code: 'TST-001', status: 2, wip: 65, todayOutput: 620, yield: 95.3, runTime: 5.0, statusClass: 'fault', statusText: '故障' },
  { id: 'w6', name: '包装入库', code: 'PKG-001', status: 1, wip: 30, todayOutput: 580, yield: 99.5, runTime: 4.8, statusClass: 'running', statusText: '运行中' }
])

const topCards = computed(() => [
  { label: '今日产量', value: '3,850', cls: 'prod' },
  { label: '当前良率', value: '96.5%', cls: 'yield' },
  { label: '设备运行', value: '9/12', cls: 'running' },
  { label: '在制总量', value: totalWip.value, cls: 'wip' }
])

const totalWip = computed(() => workstations.value.reduce((s, w) => s + w.wip, 0))

const detailRows = computed(() => {
  const ws = selectedWorkstation.value
  if (!ws) return []
  return [
    { label: '工位编码', value: ws.code },
    { label: '设备状态', value: ws.statusText, cls: ws.status === 2 ? 'text-red' : '' },
    { label: '在制数量', value: ws.wip },
    { label: '今日产量', value: ws.todayOutput },
    { label: '良品率', value: ws.yield + '%', cls: ws.yield < 95 ? 'text-red' : '' },
    { label: '运行时长', value: ws.runTime + 'h' }
  ]
})

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const week = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()]
  currentDate.value = `${y}年${m}月${d}日 星期${week}`
  currentTime.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
}

function goBack() { router.push('/dashboard') }

function toggleAutoDemo() {
  autoDemo.value = !autoDemo.value
  if (autoDemo.value) startDemo()
  else stopDemo()
}

function toggleAutoCamera() {
  autoCamera.value = !autoCamera.value
  if (autoCamera.value) startCameraCruise()
  else stopCameraCruise()
}

function resetCamera() {
  if (!camera || !controls) return
  camera.position.set(20, 15, 20)
  controls.target.set(0, 3, 0)
  controls.update()
}

function focusWorkstation(id) {
  const ws = workstations.value.find(w => w.id === id)
  if (!ws) return
  selectedWorkstation.value = ws
  const group = workstationGroups[id]
  if (!group || !camera || !controls) return
  const pos = group.position.clone()
  const camTarget = pos.clone().add(new THREE.Vector3(8, 6, 8))
  smoothCameraTo(camTarget, pos)
}

function smoothCameraTo(camPos, lookAt) {
  const startCam = camera.position.clone()
  const startLook = controls.target.clone()
  let t = 0
  function step() {
    t += 0.03
    if (t >= 1) { camera.position.copy(camPos); controls.target.copy(lookAt); controls.update(); return }
    const e = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
    camera.position.lerpVectors(startCam, camPos, e)
    controls.target.lerpVectors(startLook, lookAt, e)
    controls.update()
    requestAnimationFrame(step)
  }
  step()
}

function startDemo() {
  stopDemo()
  demoTimer = setInterval(() => {
    workstations.value.forEach(ws => {
      if (ws.status === 1) {
        ws.wip = Math.max(0, ws.wip + Math.floor(Math.random() * 7) - 3)
        ws.todayOutput += Math.floor(Math.random() * 12) + 1
        ws.yield = Math.max(90, Math.min(99.9, ws.yield + (Math.random() - 0.5) * 0.4))
      }
    })
    spawnProduct()
  }, 1500)
}

function stopDemo() {
  if (demoTimer) { clearInterval(demoTimer); demoTimer = null }
}

function startCameraCruise() {
  stopCameraCruise()
  let angle = Math.atan2(camera.position.x, camera.position.z)
  cameraTimer = setInterval(() => {
    angle += 0.003
    const r = 28
    camera.position.x = Math.sin(angle) * r
    camera.position.z = Math.cos(angle) * r
    camera.position.y = 14 + Math.sin(angle * 1.5) * 4
    controls.target.set(0, 3, 0)
    controls.update()
  }, 16)
}

function stopCameraCruise() {
  if (cameraTimer) { clearInterval(cameraTimer); cameraTimer = null }
}

function spawnProduct() {
  if (!scene) return
  const geo = new THREE.BoxGeometry(0.35, 0.18, 0.5)
  const mat = new THREE.MeshPhongMaterial({ color: 0x3b82f6, emissive: 0x1a365d, emissiveIntensity: 0.4, shininess: 80 })
  const mesh = new THREE.Mesh(geo, mat)
  mesh.position.set(-14, 0.55, -4.5 + Math.random() * 9)
  scene.add(mesh)
  productMeshes.push(mesh)
}

// ============================================================
// THREE.JS 场景初始化
// ============================================================
function initThree() {
  const container = sceneRef.value
  if (!container) { console.error('sceneRef is null'); return }

  const w = container.clientWidth || window.innerWidth
  const h = container.clientHeight || window.innerHeight
  if (w === 0 || h === 0) { console.error('container has zero size'); return }

  // 场景
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x070e1a)
  scene.fog = new THREE.FogExp2(0x070e1a, 0.012)

  // 相机
  camera = new THREE.PerspectiveCamera(55, w / h, 0.1, 500)
  camera.position.set(20, 15, 20)

  // 渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2
  container.appendChild(renderer.domElement)

  // 控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.06
  controls.minDistance = 8
  controls.maxDistance = 55
  controls.maxPolarAngle = Math.PI / 2 - 0.05
  controls.target.set(0, 3, 0)
  controls.update()

  // 时钟
  clock = new THREE.Clock()

  buildLights()
  buildFloor()
  buildConveyor()
  buildWorkstations()
  buildDecorations()

  // 事件
  renderer.domElement.addEventListener('click', onCanvasClick)
  window.addEventListener('resize', onResize)

  // 启动渲染循环
  animate()
}

function buildLights() {
  scene.add(new THREE.AmbientLight(0x334466, 0.6))

  const sun = new THREE.DirectionalLight(0xccddff, 1.2)
  sun.position.set(15, 25, 15)
  sun.castShadow = true
  sun.shadow.mapSize.set(2048, 2048)
  sun.shadow.camera.near = 0.5
  sun.shadow.camera.far = 80
  sun.shadow.camera.left = -30
  sun.shadow.camera.right = 30
  sun.shadow.camera.top = 30
  sun.shadow.camera.bottom = -30
  scene.add(sun)

  const fill = new THREE.DirectionalLight(0x4488cc, 0.4)
  fill.position.set(-15, 10, -10)
  scene.add(fill)

  // 科技蓝点光源
  const point1 = new THREE.PointLight(0x3b82f6, 2, 40)
  point1.position.set(0, 8, 0)
  scene.add(point1)
}

function buildFloor() {
  // 地面
  const floorGeo = new THREE.PlaneGeometry(60, 30)
  const floorMat = new THREE.MeshStandardMaterial({ color: 0x0b1422, roughness: 0.9, metalness: 0.1 })
  const floor = new THREE.Mesh(floorGeo, floorMat)
  floor.rotation.x = -Math.PI / 2
  floor.receiveShadow = true
  scene.add(floor)

  // 网格
  const grid = new THREE.GridHelper(60, 30, 0x162a4a, 0x0d1b30)
  grid.position.y = 0.01
  scene.add(grid)

  // 地面发光边框
  const edgeGeo = new THREE.BoxGeometry(60.4, 0.15, 30.4)
  const edgeMat = new THREE.MeshBasicMaterial({ color: 0x1e40af, transparent: true, opacity: 0.5 })
  const edge = new THREE.Mesh(edgeGeo, edgeMat)
  edge.position.y = -0.05
  scene.add(edge)
}

function buildConveyor() {
  // 主传送带
  const beltGeo = new THREE.BoxGeometry(26, 0.25, 2.4)
  const beltMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, roughness: 0.6, metalness: 0.3 })
  const belt = new THREE.Mesh(beltGeo, beltMat)
  belt.position.set(0, 0.25, 0)
  belt.receiveShadow = true
  scene.add(belt)

  // 传送带高亮线条
  const lineGeo = new THREE.BoxGeometry(26, 0.02, 0.08)
  const lineMat = new THREE.MeshBasicMaterial({ color: 0x3b82f6, transparent: true, opacity: 0.6 })
  const line1 = new THREE.Mesh(lineGeo, lineMat)
  line1.position.set(0, 0.39, 1.15)
  scene.add(line1)
  const line2 = line1.clone()
  line2.position.z = -1.15
  scene.add(line2)

  // 滚轮
  for (let i = -12; i <= 12; i += 2) {
    const rollerGeo = new THREE.CylinderGeometry(0.15, 0.15, 2.5, 12)
    const rollerMat = new THREE.MeshStandardMaterial({ color: 0x475569, metalness: 0.6, roughness: 0.3 })
    const roller = new THREE.Mesh(rollerGeo, rollerMat)
    roller.rotation.x = Math.PI / 2
    roller.position.set(i, 0.15, 0)
    scene.add(roller)
  }
}

function buildWorkstations() {
  const stationDefs = [
    { id: 'w1', name: 'SMT贴片', x: -10, z: -5, h: 2.2, color: 0x00ff88 },
    { id: 'w2', name: '回流焊接', x: -6, z: -5, h: 2.8, color: 0x00ff88 },
    { id: 'w3', name: '点胶固化', x: -2, z: -5, h: 2.0, color: 0xaaaaaa },
    { id: 'w4', name: '屏幕贴合', x: 2, z: -5, h: 2.4, color: 0x00ff88 },
    { id: 'w5', name: '功能测试', x: 6, z: -5, h: 3.0, color: 0xff4444 },
    { id: 'w6', name: '包装入库', x: 10, z: -5, h: 2.0, color: 0x00ff88 }
  ]

  stationDefs.forEach(def => {
    const group = new THREE.Group()
    group.position.set(def.x, 0, def.z)

    // 底座平台
    const baseGeo = new THREE.BoxGeometry(2.8, 0.4, 2.8)
    const baseMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, roughness: 0.5, metalness: 0.4 })
    const base = new THREE.Mesh(baseGeo, baseMat)
    base.position.y = 0.4
    base.castShadow = true
    base.receiveShadow = true
    group.add(base)

    // 设备主体
    const bodyGeo = new THREE.BoxGeometry(2.2, def.h, 2.2)
    const bodyMat = new THREE.MeshStandardMaterial({
      color: 0x1a2332,
      emissive: def.color,
      emissiveIntensity: 0.15,
      roughness: 0.4,
      metalness: 0.5
    })
    const body = new THREE.Mesh(bodyGeo, bodyMat)
    body.position.y = def.h / 2 + 0.6
    body.castShadow = true
    group.add(body)

    // 科技描边
    const edgesGeo = new THREE.EdgesGeometry(bodyGeo)
    const edgesMat = new THREE.LineBasicMaterial({ color: 0x3b82f6, transparent: true, opacity: 0.7 })
    const edges = new THREE.LineSegments(edgesGeo, edgesMat)
    edges.position.y = def.h / 2 + 0.6
    group.add(edges)

    // 状态指示灯
    const lightGeo = new THREE.SphereGeometry(0.12, 16, 16)
    const lightMat = new THREE.MeshBasicMaterial({ color: def.color })
    const indicator = new THREE.Mesh(lightGeo, lightMat)
    indicator.position.set(0, def.h + 0.85, 0)
    indicator.name = 'indicator'
    group.add(indicator)

    // 小屏幕
    const scrGeo = new THREE.PlaneGeometry(0.9, 0.5)
    const scrMat = new THREE.MeshBasicMaterial({ color: 0x00ff88, transparent: true, opacity: 0.8 })
    const scr = new THREE.Mesh(scrGeo, scrMat)
    scr.position.set(0, def.h / 2 + 0.6 + 0.35, 1.12)
    group.add(scr)

    // 连接传送带的通道
    const chGeo = new THREE.BoxGeometry(0.6, 0.1, 2.4)
    const chMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.5 })
    const channel = new THREE.Mesh(chGeo, chMat)
    channel.position.set(0, 0.38, 3.5)
    group.add(channel)

    group.userData = { id: def.id, name: def.name }
    scene.add(group)
    workstationGroups[def.id] = group
  })
}

function buildDecorations() {
  // 悬浮粒子
  const particleCount = 60
  const pGeo = new THREE.BufferGeometry()
  const positions = new Float32Array(particleCount * 3)
  for (let i = 0; i < particleCount; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 50
    positions[i * 3 + 1] = Math.random() * 15 + 2
    positions[i * 3 + 2] = (Math.random() - 0.5) * 25
  }
  pGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const pMat = new THREE.PointsMaterial({ color: 0x3b82f6, size: 0.15, transparent: true, opacity: 0.4 })
  const particles = new THREE.Points(pGeo, pMat)
  particles.name = 'particles'
  scene.add(particles)

  // 背景墙
  const wallMat = new THREE.MeshStandardMaterial({ color: 0x0a1220, transparent: true, opacity: 0.4, side: THREE.DoubleSide })
  const wallGeo = new THREE.PlaneGeometry(60, 12)
  const backWall = new THREE.Mesh(wallGeo, wallMat)
  backWall.position.set(0, 6, -15)
  scene.add(backWall)

  // 墙上发光线条
  for (let i = 0; i < 5; i++) {
    const stripGeo = new THREE.BoxGeometry(60, 0.05, 0.05)
    const stripMat = new THREE.MeshBasicMaterial({ color: 0x1e40af, transparent: true, opacity: 0.3 })
    const strip = new THREE.Mesh(stripGeo, stripMat)
    strip.position.set(0, 2 + i * 2.5, -14.9)
    scene.add(strip)
  }
}

// ============================================================
// 动画循环
// ============================================================
function animate() {
  animationId = requestAnimationFrame(animate)
  if (!renderer || !scene || !camera) return

  const elapsed = clock ? clock.getElapsedTime() : 0

  // 更新工位颜色和动画
  workstations.value.forEach(ws => {
    const group = workstationGroups[ws.id]
    if (!group) return
    let emColor, emIntensity
    if (ws.status === 1) { emColor = 0x00ff88; emIntensity = 0.12 + Math.sin(elapsed * 2) * 0.05 }
    else if (ws.status === 2) { emColor = 0xff4444; emIntensity = 0.25 + Math.sin(elapsed * 4) * 0.2 }
    else { emColor = 0x666666; emIntensity = 0.05 }

    group.traverse(child => {
      if (child.isMesh && child.material && child.material.emissive) {
        if (child.name === 'indicator') {
          child.material.color.setHex(ws.status === 1 ? 0x00ff88 : ws.status === 2 ? 0xff4444 : 0x666666)
        } else if (child.material.type === 'MeshStandardMaterial') {
          child.material.emissive.setHex(emColor)
          child.material.emissiveIntensity = emIntensity
        }
      }
    })
  })

  // 移动产品
  for (let i = productMeshes.length - 1; i >= 0; i--) {
    const p = productMeshes[i]
    p.position.x += 0.06
    p.rotation.y += 0.03
    if (p.position.x > 14) {
      scene.remove(p)
      p.geometry.dispose()
      p.material.dispose()
      productMeshes.splice(i, 1)
    }
  }

  // 粒子微动
  const particles = scene.getObjectByName('particles')
  if (particles) {
    particles.rotation.y = elapsed * 0.02
  }

  controls.update()
  renderer.render(scene, camera)
}

function onCanvasClick(event) {
  if (!renderer || !camera) return
  const rect = renderer.domElement.getBoundingClientRect()
  const mouse = new THREE.Vector2(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  )
  const raycaster = new THREE.Raycaster()
  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects(scene.children, true)
  for (const hit of intersects) {
    let parent = hit.object.parent
    while (parent && !parent.userData.id) parent = parent.parent
    if (parent && parent.userData.id) {
      const ws = workstations.value.find(w => w.id === parent.userData.id)
      if (ws) { selectedWorkstation.value = ws; return }
    }
  }
  selectedWorkstation.value = null
}

function onResize() {
  if (!renderer || !camera || !sceneRef.value) return
  const w = sceneRef.value.clientWidth || window.innerWidth
  const h = sceneRef.value.clientHeight || window.innerHeight
  camera.aspect = w / h
  camera.updateProjectionMatrix()
  renderer.setSize(w, h)
}

// ============================================================
// 生命周期
// ============================================================
onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
  nextTick(() => {
    initThree()
    // 初始生成一些产品
    for (let i = 0; i < 5; i++) {
      setTimeout(() => spawnProduct(), i * 300)
    }
  })
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  stopDemo()
  stopCameraCruise()
  if (renderer) {
    renderer.domElement.removeEventListener('click', onCanvasClick)
    renderer.dispose()
  }
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.digital-twin-screen {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background: #070e1a;
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
  z-index: 9999;
}

.scene-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* ============ 顶部面板 ============ */
.top-panel {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 76px;
  background: linear-gradient(180deg, rgba(7,14,26,0.92) 0%, rgba(7,14,26,0.6) 70%, transparent 100%);
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 10;
  pointer-events: auto;
}

.top-left { flex: 1; }
.title-group { display: flex; align-items: baseline; gap: 12px; }
.main-title {
  font-size: 20px; font-weight: 700; margin: 0; letter-spacing: 3px;
  background: linear-gradient(90deg, #60a5fa, #818cf8);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.sub-title { font-size: 13px; color: #818cf8; margin: 0; font-weight: 500; }
.date { font-size: 11px; color: #475569; }

.top-center { flex: 2; display: flex; justify-content: center; gap: 14px; }
.data-card {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 16px;
  background: rgba(15,23,42,0.85);
  border: 1px solid rgba(59,130,246,0.25);
  border-radius: 8px;
}
.data-icon { width: 30px; height: 30px; border-radius: 6px; }
.data-icon.prod { background: linear-gradient(135deg, #06b6d4, #3b82f6); }
.data-icon.yield { background: linear-gradient(135deg, #10b981, #06b6d4); }
.data-icon.running { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.data-icon.wip { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
.data-content { display: flex; flex-direction: column; gap: 1px; }
.data-label { font-size: 10px; color: #64748b; }
.data-value { font-size: 17px; font-weight: 700; color: #f1f5f9; font-family: 'Courier New', monospace; }
.data-value.warning { color: #f87171; }

.top-right { flex: 1; display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.control-group { display: flex; gap: 6px; flex-wrap: wrap; justify-content: flex-end; }
.ctrl-btn {
  padding: 5px 10px; font-size: 11px; cursor: pointer;
  background: rgba(59,130,246,0.12); border: 1px solid rgba(59,130,246,0.35); color: #93c5fd;
  border-radius: 5px; transition: all 0.3s;
}
.ctrl-btn:hover { background: rgba(59,130,246,0.25); color: #fff; }
.ctrl-btn.active { background: rgba(59,130,246,0.35); border-color: #60a5fa; color: #fff; }
.back-btn {
  padding: 5px 10px; font-size: 11px; cursor: pointer;
  background: rgba(239,68,68,0.12); border: 1px solid rgba(239,68,68,0.35); color: #fca5a5;
  border-radius: 5px; transition: all 0.3s;
}
.back-btn:hover { background: rgba(239,68,68,0.25); color: #fff; }
.time-display { font-size: 18px; font-weight: 600; color: #60a5fa; font-family: 'Courier New', monospace; letter-spacing: 2px; }

/* ============ 右侧面板 ============ */
.side-panel {
  position: absolute; right: 16px; top: 88px; width: 230px;
  background: rgba(10,18,32,0.92); border: 1px solid rgba(59,130,246,0.25);
  border-radius: 10px; overflow: hidden; z-index: 10;
}
.panel-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px; border-bottom: 1px solid rgba(59,130,246,0.15);
}
.panel-title { font-size: 13px; font-weight: 600; color: #e2e8f0; }
.panel-count { font-size: 11px; color: #64748b; }
.workstation-list { padding: 6px; max-height: 380px; overflow-y: auto; }
.ws-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 10px; margin-bottom: 4px;
  background: rgba(30,41,59,0.4); border-radius: 6px;
  cursor: pointer; transition: all 0.3s; border-left: 3px solid transparent;
}
.ws-item:hover { background: rgba(59,130,246,0.12); }
.ws-item.running { border-left-color: #10b981; }
.ws-item.standby { border-left-color: #94a3b8; }
.ws-item.fault { border-left-color: #ef4444; animation: fpulse 1s infinite; }
@keyframes fpulse { 0%,100% { background: rgba(30,41,59,0.4); } 50% { background: rgba(239,68,68,0.08); } }
.ws-indicator { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.ws-item.running .ws-indicator { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.5); }
.ws-item.standby .ws-indicator { background: #94a3b8; }
.ws-item.fault .ws-indicator { background: #ef4444; box-shadow: 0 0 6px rgba(239,68,68,0.5); animation: blink .5s infinite; }
@keyframes blink { 0%,100% { opacity:1; } 50% { opacity:.3; } }
.ws-info { flex: 1; min-width: 0; }
.ws-name { font-size: 12px; font-weight: 500; color: #e2e8f0; }
.ws-meta { font-size: 10px; color: #64748b; }
.ws-status { font-size: 11px; font-weight: 600; white-space: nowrap; }
.ws-item.running .ws-status { color: #10b981; }
.ws-item.standby .ws-status { color: #94a3b8; }
.ws-item.fault .ws-status { color: #ef4444; }

/* ============ 详情面板 ============ */
.detail-panel {
  position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%);
  width: 300px; background: rgba(10,18,32,0.96);
  border: 1px solid rgba(59,130,246,0.4); border-radius: 12px;
  z-index: 20; box-shadow: 0 20px 60px rgba(0,0,0,0.6);
}
.detail-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(59,130,246,0.18), rgba(99,102,241,0.08));
  border-bottom: 1px solid rgba(59,130,246,0.25);
}
.detail-title { font-size: 15px; font-weight: 600; color: #f1f5f9; }
.close-btn {
  width: 22px; height: 22px; border: none;
  background: rgba(255,255,255,0.08); color: #94a3b8;
  border-radius: 50%; font-size: 14px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.close-btn:hover { background: rgba(255,255,255,0.15); color: #fff; }
.detail-body { padding: 12px 16px; }
.detail-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 7px 0; border-bottom: 1px solid rgba(148,163,184,0.08);
}
.detail-row:last-child { border-bottom: none; }
.detail-label { font-size: 12px; color: #94a3b8; }
.detail-value { font-size: 13px; font-weight: 600; color: #f1f5f9; }
.text-red { color: #f87171 !important; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ============ 底部图例 ============ */
.bottom-legend {
  position: absolute; bottom: 16px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 18px; padding: 8px 18px;
  background: rgba(10,18,32,0.9); border: 1px solid rgba(59,130,246,0.25);
  border-radius: 8px; z-index: 10;
}
.legend-item { display: flex; align-items: center; gap: 5px; font-size: 11px; color: #94a3b8; }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; }
.legend-dot.running { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.5); }
.legend-dot.standby { background: #94a3b8; }
.legend-dot.fault { background: #ef4444; box-shadow: 0 0 6px rgba(239,68,68,0.5); }
.legend-line { width: 20px; height: 3px; background: linear-gradient(90deg, #1e3a5f, #3b82f6); border-radius: 2px; }
.legend-box { width: 10px; height: 10px; background: #3b82f6; border: 1px solid #60a5fa; border-radius: 2px; }
</style>
