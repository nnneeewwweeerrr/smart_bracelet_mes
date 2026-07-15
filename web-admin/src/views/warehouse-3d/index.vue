<template>
  <div class="warehouse-3d-screen">
    <!-- 3D场景容器 -->
    <div ref="sceneRef" class="scene-container"></div>

    <!-- 顶部控制栏 -->
    <div class="top-bar">
      <div class="top-left">
        <h1 class="main-title">3D 智能仓储</h1>
        <span class="sub-title">VR 仓库可视化管理系统</span>
      </div>
      <div class="top-center">
        <div class="ctrl-group">
          <span class="ctrl-label">视角</span>
          <button
            v-for="v in viewModes" :key="v.key"
            class="ctrl-btn" :class="{ active: currentView === v.key }"
            @click="switchView(v.key)"
          >{{ v.label }}</button>
        </div>
        <div class="ctrl-divider"></div>
        <div class="ctrl-group">
          <span class="ctrl-label">墙壁</span>
          <button class="ctrl-btn" :class="{ active: wallsVisible }" @click="toggleWalls">
            {{ wallsVisible ? '显示' : '隐藏' }}
          </button>
        </div>
        <div class="ctrl-divider"></div>
        <div class="ctrl-group">
          <span class="ctrl-label">区域</span>
          <button
            v-for="z in zoneFilters" :key="z.key"
            class="ctrl-btn zone-btn" :class="{ active: activeZones.includes(z.key), ['zone-' + z.key]: true }"
            @click="toggleZone(z.key)"
          >{{ z.label }}</button>
        </div>
        <div class="ctrl-divider"></div>
        <button class="ctrl-btn fp-btn" :class="{ active: firstPerson }" @click="toggleFirstPerson">
          {{ firstPerson ? '退出第一人称' : '第一人称' }}
        </button>
      </div>
      <div class="top-right">
        <button class="ctrl-btn" @click="resetCamera">复位视角</button>
        <button class="ctrl-btn exit-btn" @click="handleExit">退出</button>
      </div>
    </div>

    <!-- 右侧信息面板 -->
    <div class="right-panel">
      <div class="panel-section">
        <div class="section-title">仓库概览</div>
        <div class="overview-grid">
          <div class="ov-item">
            <div class="ov-value">{{ overview.totalArea }}</div>
            <div class="ov-label">总面积 (m²)</div>
          </div>
          <div class="ov-item">
            <div class="ov-value" :class="overview.usageRate > 85 ? 'text-warn' : ''">{{ overview.usageRate }}%</div>
            <div class="ov-label">使用率</div>
          </div>
          <div class="ov-item">
            <div class="ov-value">{{ overview.totalGoods }}</div>
            <div class="ov-label">货物总数</div>
          </div>
          <div class="ov-item">
            <div class="ov-value">{{ overview.shelfCount }}</div>
            <div class="ov-label">货架总数</div>
          </div>
        </div>
      </div>

      <div class="panel-section">
        <div class="section-title">区域占用</div>
        <div class="zone-list">
          <div
            v-for="z in zoneStats" :key="z.key"
            class="zone-item"
            :class="'zone-' + z.key"
            @mouseenter="highlightZone(z.key)"
            @mouseleave="highlightZone(null)"
          >
            <div class="zone-header">
              <div class="zone-dot"></div>
              <span class="zone-name">{{ z.name }}</span>
              <span class="zone-pct">{{ z.usage }}%</span>
            </div>
            <div class="zone-bar-bg">
              <div class="zone-bar" :style="{ width: z.usage + '%' }"></div>
            </div>
            <div class="zone-meta">{{ z.used }}/{{ z.total }} 货位</div>
          </div>
        </div>
      </div>

      <div class="panel-section">
        <div class="section-title">
          库存预警
          <span class="warn-count">{{ warnings.length }}</span>
        </div>
        <div class="warning-list">
          <div v-for="w in warnings" :key="w.id" class="warn-item" :class="'warn-' + w.level">
            <div class="warn-icon">{{ w.level === 'high' ? '!!' : '!' }}</div>
            <div class="warn-info">
              <div class="warn-name">{{ w.name }}</div>
              <div class="warn-desc">{{ w.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 货物详情弹窗 -->
    <transition name="fade">
      <div v-if="selectedBox" class="detail-popup" :style="popupStyle">
        <div class="detail-header">
          <span class="detail-title">{{ selectedBox.name }}</span>
          <button class="close-btn" @click="selectedBox = null">×</button>
        </div>
        <div class="detail-body">
          <div class="detail-row" v-for="r in boxDetailRows" :key="r.label">
            <span class="detail-label">{{ r.label }}</span>
            <span class="detail-value" :class="r.cls || ''">{{ r.value }}</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 底部图例 -->
    <div class="bottom-legend">
      <div class="legend-item"><div class="legend-line legend-raw"></div><span>原料区</span></div>
      <div class="legend-item"><div class="legend-line legend-semi"></div><span>半成品区</span></div>
      <div class="legend-item"><div class="legend-line legend-finish"></div><span>成品区</span></div>
      <div class="legend-item"><div class="legend-line legend-aux"></div><span>辅料区</span></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

// ============================================================
// 响应式数据
// ============================================================
const sceneRef = ref(null)
const currentView = ref('free')
const wallsVisible = ref(true)
const firstPerson = ref(false)
const selectedBox = ref(null)
const popupStyle = ref({})
const activeZones = ref(['raw', 'semi', 'finish', 'aux'])

const viewModes = [
  { key: 'top', label: '俯视图' },
  { key: 'side', label: '侧视图' },
  { key: 'free', label: '自由视角' }
]

const zoneFilters = [
  { key: 'raw', label: '原料' },
  { key: 'semi', label: '半成品' },
  { key: 'finish', label: '成品' },
  { key: 'aux', label: '辅料' }
]

// 区域配置
const zoneConfig = {
  raw:    { name: '原料仓',   color: 0x3b82f6, lineColor: 0x2563eb, posX: -18 },
  semi:   { name: '半成品仓', color: 0xf59e0b, lineColor: 0xd97706, posX: -6  },
  finish: { name: '成品仓',   color: 0x10b981, lineColor: 0x059669, posX: 6   },
  aux:    { name: '辅料仓',   color: 0xa855f7, lineColor: 0x9333ea, posX: 18  }
}

// Mock数据
const overview = ref({
  totalArea: 2400,
  usageRate: 78,
  totalGoods: 1247,
  shelfCount: 60
})

const zoneStats = ref([
  { key: 'raw',    name: '原料仓',   usage: 85, used: 51, total: 60 },
  { key: 'semi',   name: '半成品仓', usage: 62, used: 37, total: 60 },
  { key: 'finish', name: '成品仓',   usage: 74, used: 44, total: 60 },
  { key: 'aux',    name: '辅料仓',   usage: 45, used: 27, total: 60 }
])

const warnings = ref([
  { id: 1, level: 'high', name: 'PCB主板', desc: '库存不足，仅剩 23 件，低于安全库存 50 件' },
  { id: 2, level: 'high', name: '锂电池', desc: '库存不足，仅剩 15 件，低于安全库存 40 件' },
  { id: 3, level: 'low', name: 'TPU表带', desc: '库存偏高，当前 890 件，超过上限 600 件' },
  { id: 4, level: 'low', name: '充电线', desc: '库存偏高，当前 560 件，超过上限 400 件' },
  { id: 5, level: 'high', name: 'OLED屏幕', desc: '库存不足，仅剩 8 件，低于安全库存 30 件' }
])

const boxDetailRows = computed(() => {
  const b = selectedBox.value
  if (!b) return []
  return [
    { label: '物料名称', value: b.name },
    { label: '物料编码', value: b.code },
    { label: '所属区域', value: b.zoneName },
    { label: '存放位置', value: `第${b.row + 1}排-第${b.col + 1}列-第${b.layer + 1}层` },
    { label: '库存数量', value: b.qty },
    { label: '安全库存', value: b.safeQty },
    { label: '状态', value: b.status, cls: b.status === '不足' ? 'text-warn' : '' }
  ]
})

// ============================================================
// Three.js变量
// ============================================================
let scene, camera, renderer, controls
let animationId = null
let walls = []
let floorZones = {}
let boxMeshes = []
let highlightedZone = null
let raycaster = null
let mouse = null
let clock = null
let firstPersonControls = null

// 货物mock数据生成
const materialNames = {
  raw: ['PCB主板', '锂电池', '芯片IC', '电阻电容', '连接器', '传感器', '天线模块', '蓝牙模块'],
  semi: ['贴片板', '组装主板', '焊接半成品', '测试板', '固件烧录板'],
  finish: ['智能手环V3', '智能手环Pro', '运动版手环', '健康版手环', '儿童版手环'],
  aux: ['TPU表带', '充电线', '包装盒', '说明书', '保修卡', '螺丝配件']
}

function generateBoxData() {
  const boxes = []
  const zones = ['raw', 'semi', 'finish', 'aux']
  zones.forEach((zone, zi) => {
    for (let row = 0; row < 4; row++) {
      for (let col = 0; col < 5; col++) {
        for (let layer = 0; layer < 3; layer++) {
          if (Math.random() > 0.35) {
            const names = materialNames[zone]
            const name = names[Math.floor(Math.random() * names.length)]
            const qty = Math.floor(Math.random() * 200) + 10
            const safeQty = Math.floor(Math.random() * 50) + 20
            boxes.push({
              zone, row, col, layer,
              name,
              code: `${zone.toUpperCase()}-${String(row + 1).padStart(2, '0')}${String(col + 1).padStart(2, '0')}${String(layer + 1).padStart(2, '0')}`,
              zoneName: zoneConfig[zone].name,
              qty,
              safeQty,
              status: qty < safeQty ? '不足' : '正常'
            })
          }
        }
      }
    }
  })
  return boxes
}

const allBoxData = generateBoxData()

// ============================================================
// 视角切换
// ============================================================
function switchView(key) {
  currentView.value = key
  if (firstPerson.value) toggleFirstPerson()
  if (!camera || !controls) return
  let pos, target
  switch (key) {
    case 'top':
      pos = new THREE.Vector3(0, 50, 0.1)
      target = new THREE.Vector3(0, 0, 0)
      break
    case 'side':
      pos = new THREE.Vector3(50, 10, 0)
      target = new THREE.Vector3(0, 5, 0)
      break
    default:
      pos = new THREE.Vector3(30, 25, 30)
      target = new THREE.Vector3(0, 3, 0)
  }
  smoothCameraTo(pos, target)
}

function smoothCameraTo(camPos, lookAt) {
  const startCam = camera.position.clone()
  const startLook = controls.target.clone()
  let t = 0
  function step() {
    t += 0.035
    if (t >= 1) {
      camera.position.copy(camPos)
      controls.target.copy(lookAt)
      controls.update()
      return
    }
    const e = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
    camera.position.lerpVectors(startCam, camPos, e)
    controls.target.lerpVectors(startLook, lookAt, e)
    controls.update()
    requestAnimationFrame(step)
  }
  step()
}

function resetCamera() {
  currentView.value = 'free'
  if (firstPerson.value) toggleFirstPerson()
  smoothCameraTo(new THREE.Vector3(30, 25, 30), new THREE.Vector3(0, 3, 0))
}

function handleExit() {
  window.history.back()
}

function toggleWalls() {
  wallsVisible.value = !wallsVisible.value
  walls.forEach(w => { w.visible = wallsVisible.value })
}

function toggleZone(key) {
  const idx = activeZones.value.indexOf(key)
  if (idx > -1) {
    if (activeZones.value.length <= 1) return
    activeZones.value.splice(idx, 1)
  } else {
    activeZones.value.push(key)
  }
  updateZoneVisibility()
}

function updateZoneVisibility() {
  boxMeshes.forEach(bm => {
    const visible = activeZones.value.includes(bm.userData.zone)
    bm.visible = visible
  })
  Object.keys(floorZones).forEach(zk => {
    if (floorZones[zk]) {
      floorZones[zk].visible = activeZones.value.includes(zk)
    }
  })
}

function highlightZone(key) {
  highlightedZone = key
}

function toggleFirstPerson() {
  firstPerson.value = !firstPerson.value
  if (firstPerson.value) {
    camera.position.set(-18, 2, 20)
    controls.target.set(-18, 2, 19)
    controls.enableRotate = false
    controls.enableZoom = false
    controls.enablePan = true
    controls.update()
  } else {
    controls.enableRotate = true
    controls.enableZoom = true
    controls.enablePan = true
    resetCamera()
  }
}

// ============================================================
// Three.js 场景初始化
// ============================================================
function initThree() {
  const container = sceneRef.value
  if (!container) { console.error('sceneRef is null'); return }

  const w = container.clientWidth || window.innerWidth
  const h = container.clientHeight || window.innerHeight
  if (w === 0 || h === 0) { console.error('container has zero size'); return }

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x0a0f1e)
  scene.fog = new THREE.FogExp2(0x0a0f1e, 0.008)

  camera = new THREE.PerspectiveCamera(55, w / h, 0.1, 500)
  camera.position.set(30, 25, 30)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.0
  container.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.06
  controls.minDistance = 5
  controls.maxDistance = 80
  controls.maxPolarAngle = Math.PI / 2 - 0.02
  controls.target.set(0, 3, 0)
  controls.update()

  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()
  clock = new THREE.Clock()

  buildLights()
  buildFloor()
  buildZoneLines()
  buildShelves()
  buildWalls()
  buildCeilingLights()

  renderer.domElement.addEventListener('click', onCanvasClick)
  renderer.domElement.addEventListener('mousemove', onCanvasHover)
  window.addEventListener('resize', onResize)

  animate()
}

function buildLights() {
  scene.add(new THREE.AmbientLight(0x334466, 0.8))

  const dirLight = new THREE.DirectionalLight(0xccddff, 0.8)
  dirLight.position.set(20, 30, 20)
  dirLight.castShadow = true
  dirLight.shadow.mapSize.set(2048, 2048)
  dirLight.shadow.camera.near = 0.5
  dirLight.shadow.camera.far = 100
  dirLight.shadow.camera.left = -40
  dirLight.shadow.camera.right = 40
  dirLight.shadow.camera.top = 40
  dirLight.shadow.camera.bottom = -40
  scene.add(dirLight)

  const fill = new THREE.DirectionalLight(0x4488cc, 0.3)
  fill.position.set(-20, 15, -10)
  scene.add(fill)
}

function buildFloor() {
  // 主地面
  const floorGeo = new THREE.PlaneGeometry(60, 40)
  const floorMat = new THREE.MeshStandardMaterial({
    color: 0x1a1f2e,
    roughness: 0.85,
    metalness: 0.15
  })
  const floor = new THREE.Mesh(floorGeo, floorMat)
  floor.rotation.x = -Math.PI / 2
  floor.receiveShadow = true
  scene.add(floor)

  // 细网格
  const grid = new THREE.GridHelper(60, 60, 0x1e293b, 0x151b2b)
  grid.position.y = 0.01
  scene.add(grid)
}

function buildZoneLines() {
  const zones = [
    { key: 'raw',    x: -22.5, color: 0x3b82f6 },
    { key: 'semi',   x: -7.5,  color: 0xf59e0b },
    { key: 'finish', x: 7.5,   color: 0x10b981 },
    { key: 'aux',    x: 22.5,  color: 0xa855f7 }
  ]

  zones.forEach(z => {
    const group = new THREE.Group()

    // 区域地面着色
    const zoneFloorGeo = new THREE.PlaneGeometry(14, 30)
    const zoneFloorMat = new THREE.MeshStandardMaterial({
      color: z.color,
      transparent: true,
      opacity: 0.06,
      roughness: 0.9
    })
    const zoneFloor = new THREE.Mesh(zoneFloorGeo, zoneFloorMat)
    zoneFloor.rotation.x = -Math.PI / 2
    zoneFloor.position.set(z.x, 0.02, 0)
    zoneFloor.receiveShadow = true
    group.add(zoneFloor)

    // 区域边框线（四边）
    const lineMat = new THREE.MeshBasicMaterial({ color: z.color, transparent: true, opacity: 0.5 })
    const halfW = 7
    const halfH = 15

    // 四条边
    const edges = [
      { sx: -halfW, sz: -halfH, ex: halfW, ez: -halfH },
      { sx: -halfW, sz: halfH, ex: halfW, ez: halfH },
      { sx: -halfW, sz: -halfH, ex: -halfW, ez: halfH },
      { sx: halfW, sz: -halfH, ex: halfW, ez: halfH }
    ]
    edges.forEach(e => {
      const dx = e.ex - e.sx
      const dz = e.ez - e.sz
      const len = Math.max(0.01, Math.sqrt(dx * dx + dz * dz))
      const lineGeo = new THREE.BoxGeometry(len, 0.04, 0.12)
      const line = new THREE.Mesh(lineGeo, lineMat)
      line.position.set(z.x + (e.sx + e.ex) / 2, 0.03, (e.sz + e.ez) / 2)
      line.rotation.y = Math.atan2(dz, dx) - Math.PI / 2
      group.add(line)
    })

    // 区域标签底座
    const labelGeo = new THREE.BoxGeometry(3, 0.08, 1.2)
    const labelMat = new THREE.MeshBasicMaterial({ color: z.color, transparent: true, opacity: 0.3 })
    const label = new THREE.Mesh(labelGeo, labelMat)
    label.position.set(z.x, 0.04, 14)
    group.add(label)

    scene.add(group)
    floorZones[z.key] = group
  })
}

function buildShelves() {
  const zones = ['raw', 'semi', 'finish', 'aux']
  const zoneXPositions = [-22.5, -7.5, 7.5, 22.5]

  zones.forEach((zone, zi) => {
    const baseX = zoneXPositions[zi]

    for (let row = 0; row < 4; row++) {
      const zPos = -10 + row * 7

      // 货架框架
      buildShelfFrame(baseX, zPos, zone)

      // 货物箱
      for (let col = 0; col < 5; col++) {
        for (let layer = 0; layer < 3; layer++) {
          const boxData = allBoxData.find(
            b => b.zone === zone && b.row === row && b.col === col && b.layer === layer
          )
          if (boxData) {
            buildCargoBox(baseX, zPos, col, layer, zone, boxData)
          }
        }
      }
    }
  })
}

function buildShelfFrame(x, z, zone) {
  const group = new THREE.Group()
  const frameMat = new THREE.MeshStandardMaterial({
    color: 0x374151,
    roughness: 0.5,
    metalness: 0.6
  })
  const accentMat = new THREE.MeshBasicMaterial({
    color: zoneConfig[zone].color,
    transparent: true,
    opacity: 0.4
  })

  // 竖直支柱（4根）
  const pillarGeo = new THREE.BoxGeometry(0.12, 6, 0.12)
  const positions = [
    [-2.3, 3, -0.8],
    [2.3, 3, -0.8],
    [-2.3, 3, 0.8],
    [2.3, 3, 0.8]
  ]
  positions.forEach(p => {
    const pillar = new THREE.Mesh(pillarGeo, frameMat)
    pillar.position.set(p[0], p[1], p[2])
    pillar.castShadow = true
    group.add(pillar)
  })

  // 层板（3层）
  for (let i = 0; i < 3; i++) {
    const shelfGeo = new THREE.BoxGeometry(5, 0.08, 2)
    const shelf = new THREE.Mesh(shelfGeo, frameMat)
    shelf.position.set(0, i * 2 + 0.5, 0)
    shelf.receiveShadow = true
    group.add(shelf)

    // 层板前沿高亮
    const edgeGeo = new THREE.BoxGeometry(5, 0.03, 0.04)
    const edge = new THREE.Mesh(edgeGeo, accentMat)
    edge.position.set(0, i * 2 + 0.54, 1.02)
    group.add(edge)
  }

  // 顶部横梁
  const beamGeo = new THREE.BoxGeometry(5, 0.08, 2)
  const beam = new THREE.Mesh(beamGeo, frameMat)
  beam.position.set(0, 6.04, 0)
  group.add(beam)

  group.position.set(x, 0, z)
  scene.add(group)
}

function buildCargoBox(x, z, col, layer, zone, boxData) {
  const zoneColor = zoneConfig[zone].color
  // 根据库存状态调整颜色
  let boxColor = zoneColor
  if (boxData.status === '不足') {
    boxColor = 0xef4444
  }

  const boxW = Math.max(0.01, 0.75 + Math.random() * 0.15)
  const boxH = Math.max(0.01, 0.5 + Math.random() * 0.3)
  const boxD = Math.max(0.01, 1.2 + Math.random() * 0.2)

  const geo = new THREE.BoxGeometry(boxW, boxH, boxD)
  const mat = new THREE.MeshStandardMaterial({
    color: boxColor,
    emissive: boxColor,
    emissiveIntensity: 0.12,
    roughness: 0.5,
    metalness: 0.3,
    transparent: true,
    opacity: 0.9
  })
  const mesh = new THREE.Mesh(geo, mat)
  mesh.castShadow = true
  mesh.receiveShadow = true

  const xPos = (col - 2) * 0.95
  const yPos = layer * 2 + 0.5 + boxH / 2 + 0.06
  mesh.position.set(x + xPos, yPos, z)
  mesh.userData = { ...boxData, isBox: true, zone }

  // 描边
  const edgesGeo = new THREE.EdgesGeometry(geo)
  const edgesMat = new THREE.LineBasicMaterial({
    color: zoneColor,
    transparent: true,
    opacity: 0.5
  })
  const edges = new THREE.LineSegments(edgesGeo, edgesMat)
  mesh.add(edges)

  scene.add(mesh)
  boxMeshes.push(mesh)
}

function buildWalls() {
  const wallMat = new THREE.MeshStandardMaterial({
    color: 0x1e293b,
    transparent: true,
    opacity: 0.25,
    side: THREE.DoubleSide,
    roughness: 0.8,
    metalness: 0.2
  })

  // 后墙
  const backGeo = new THREE.PlaneGeometry(60, 10)
  const backWall = new THREE.Mesh(backGeo, wallMat)
  backWall.position.set(0, 5, -20)
  scene.add(backWall)
  walls.push(backWall)

  // 前墙
  const frontWall = new THREE.Mesh(backGeo, wallMat.clone())
  frontWall.position.set(0, 5, 20)
  scene.add(frontWall)
  walls.push(frontWall)

  // 左墙
  const sideGeo = new THREE.PlaneGeometry(40, 10)
  const leftWall = new THREE.Mesh(sideGeo, wallMat.clone())
  leftWall.rotation.y = Math.PI / 2
  leftWall.position.set(-30, 5, 0)
  scene.add(leftWall)
  walls.push(leftWall)

  // 右墙
  const rightWall = new THREE.Mesh(sideGeo, wallMat.clone())
  rightWall.rotation.y = Math.PI / 2
  rightWall.position.set(30, 5, 0)
  scene.add(rightWall)
  walls.push(rightWall)

  // 墙壁发光线条
  const stripMat = new THREE.MeshBasicMaterial({ color: 0x3b82f6, transparent: true, opacity: 0.2 })
  for (let i = 0; i < 4; i++) {
    // 后墙横线
    const stripGeo = new THREE.BoxGeometry(60, 0.04, 0.04)
    const strip = new THREE.Mesh(stripGeo, stripMat)
    strip.position.set(0, 1.5 + i * 2.5, -19.95)
    scene.add(strip)
    walls.push(strip)

    // 侧墙横线
    const sideStripGeo = new THREE.BoxGeometry(40, 0.04, 0.04)
    const sideStripL = new THREE.Mesh(sideStripGeo, stripMat.clone())
    sideStripL.rotation.y = Math.PI / 2
    sideStripL.position.set(-29.95, 1.5 + i * 2.5, 0)
    scene.add(sideStripL)
    walls.push(sideStripL)

    const sideStripR = new THREE.Mesh(sideStripGeo, stripMat.clone())
    sideStripR.rotation.y = Math.PI / 2
    sideStripR.position.set(29.95, 1.5 + i * 2.5, 0)
    scene.add(sideStripR)
    walls.push(sideStripR)
  }
}

function buildCeilingLights() {
  // 顶板
  const ceilGeo = new THREE.PlaneGeometry(60, 40)
  const ceilMat = new THREE.MeshStandardMaterial({
    color: 0x0f172a,
    transparent: true,
    opacity: 0.3,
    side: THREE.DoubleSide
  })
  const ceil = new THREE.Mesh(ceilGeo, ceilMat)
  ceil.rotation.x = Math.PI / 2
  ceil.position.y = 10
  scene.add(ceil)

  // 顶灯
  const lightPositions = [
    [-20, 9.8, -10], [-20, 9.8, 0], [-20, 9.8, 10],
    [0, 9.8, -10], [0, 9.8, 0], [0, 9.8, 10],
    [20, 9.8, -10], [20, 9.8, 0], [20, 9.8, 10]
  ]
  lightPositions.forEach(pos => {
    // 灯具外壳
    const housingGeo = new THREE.BoxGeometry(2.5, 0.15, 0.6)
    const housingMat = new THREE.MeshStandardMaterial({ color: 0x334155, metalness: 0.7, roughness: 0.3 })
    const housing = new THREE.Mesh(housingGeo, housingMat)
    housing.position.set(pos[0], pos[1], pos[2])
    scene.add(housing)

    // 灯管发光
    const tubeGeo = new THREE.BoxGeometry(2.2, 0.05, 0.2)
    const tubeMat = new THREE.MeshBasicMaterial({ color: 0xdbeafe, transparent: true, opacity: 0.8 })
    const tube = new THREE.Mesh(tubeGeo, tubeMat)
    tube.position.set(pos[0], pos[1] - 0.1, pos[2])
    scene.add(tube)

    // 点光源
    const pLight = new THREE.PointLight(0xc8d8f0, 0.6, 18)
    pLight.position.set(pos[0], pos[1] - 0.3, pos[2])
    scene.add(pLight)
  })
}

// ============================================================
// 动画循环
// ============================================================
function animate() {
  animationId = requestAnimationFrame(animate)
  if (!renderer || !scene || !camera) return

  const elapsed = clock ? clock.getElapsedTime() : 0

  // 区域高亮动画
  Object.keys(floorZones).forEach(zk => {
    const group = floorZones[zk]
    if (!group) return
    group.traverse(child => {
      if (child.isMesh && child.material && child.material.opacity !== undefined) {
        const isHovered = highlightedZone === zk
        const baseOpacity = child.material.userData?.baseOpacity ?? child.material.opacity
        if (!child.material.userData) child.material.userData = {}
        if (child.material.userData.baseOpacity === undefined) {
          child.material.userData.baseOpacity = child.material.opacity
        }
        if (isHovered) {
          child.material.opacity = Math.min(1, baseOpacity * 2.5)
          child.material.emissiveIntensity = 0.3
        } else {
          child.material.opacity = baseOpacity
          child.material.emissiveIntensity = 0
        }
      }
    })
  })

  // 箱子微动效果
  boxMeshes.forEach((bm, idx) => {
    if (bm.material && bm.material.emissiveIntensity !== undefined) {
      bm.material.emissiveIntensity = 0.1 + Math.sin(elapsed * 1.5 + idx * 0.3) * 0.04
    }
  })

  // 第一人称视角WASD控制
  if (firstPerson.value) {
    // 通过OrbitControls的pan实现移动
  }

  controls.update()
  renderer.render(scene, camera)
}

// ============================================================
// 交互事件
// ============================================================
function onCanvasClick(event) {
  if (!renderer || !camera) return
  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(mouse, camera)

  const intersects = raycaster.intersectObjects(boxMeshes, false)
  if (intersects.length > 0) {
    const hit = intersects[0].object
    if (hit.userData && hit.userData.isBox) {
      selectedBox.value = hit.userData
      popupStyle.value = {
        left: event.clientX + 'px',
        top: event.clientY + 'px'
      }
      return
    }
  }
  selectedBox.value = null
}

function onCanvasHover(event) {
  if (!renderer || !camera) return
  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(mouse, camera)

  const intersects = raycaster.intersectObjects(boxMeshes, false)
  // 重置所有箱子
  boxMeshes.forEach(bm => {
    if (bm.material) {
      bm.material.emissiveIntensity = 0.1
    }
    bm.scale.set(1, 1, 1)
  })

  if (intersects.length > 0) {
    const hit = intersects[0].object
    if (hit.userData && hit.userData.isBox) {
      hit.material.emissiveIntensity = 0.4
      hit.scale.set(1.05, 1.05, 1.05)
      renderer.domElement.style.cursor = 'pointer'
      return
    }
  }
  renderer.domElement.style.cursor = 'default'
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
  nextTick(() => {
    initThree()
  })
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) {
    renderer.domElement.removeEventListener('click', onCanvasClick)
    renderer.domElement.removeEventListener('mousemove', onCanvasHover)
    renderer.dispose()
  }
  // 清理几何体和材质
  boxMeshes.forEach(bm => {
    bm.geometry?.dispose()
    bm.material?.dispose()
  })
  boxMeshes = []
  walls = []
  Object.values(floorZones).forEach(g => {
    g?.traverse(child => {
      if (child.isMesh) {
        child.geometry?.dispose()
        child.material?.dispose()
      }
    })
  })
  floorZones = {}
  window.removeEventListener('resize', onResize)
  scene = null
  camera = null
  renderer = null
  controls = null
})
</script>

<style scoped>
.warehouse-3d-screen {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background: #0a0f1e;
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

/* ============ 顶部控制栏 ============ */
.top-bar {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 56px;
  background: linear-gradient(180deg, rgba(10,15,30,0.94) 0%, rgba(10,15,30,0.7) 70%, transparent 100%);
  display: flex;
  align-items: center;
  padding: 0 20px;
  z-index: 10;
  gap: 16px;
}

.top-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-shrink: 0;
}

.main-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sub-title {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
}

.top-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ctrl-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ctrl-label {
  font-size: 11px;
  color: #64748b;
  margin-right: 4px;
  white-space: nowrap;
}

.ctrl-btn {
  padding: 4px 10px;
  font-size: 11px;
  cursor: pointer;
  background: rgba(59,130,246,0.08);
  border: 1px solid rgba(59,130,246,0.25);
  color: #93c5fd;
  border-radius: 4px;
  transition: all 0.25s;
  white-space: nowrap;
}

.ctrl-btn:hover {
  background: rgba(59,130,246,0.2);
  color: #fff;
}

.ctrl-btn.active {
  background: rgba(59,130,246,0.3);
  border-color: #60a5fa;
  color: #fff;
}

.ctrl-btn.zone-btn.zone-raw.active { background: rgba(59,130,246,0.3); border-color: #3b82f6; }
.ctrl-btn.zone-btn.zone-semi.active { background: rgba(245,158,11,0.3); border-color: #f59e0b; color: #fbbf24; }
.ctrl-btn.zone-btn.zone-finish.active { background: rgba(16,185,129,0.3); border-color: #10b981; color: #34d399; }
.ctrl-btn.zone-btn.zone-aux.active { background: rgba(168,85,247,0.3); border-color: #a855f7; color: #c084fc; }

.fp-btn.active {
  background: rgba(16,185,129,0.3) !important;
  border-color: #10b981 !important;
  color: #34d399 !important;
}

.ctrl-divider {
  width: 1px;
  height: 20px;
  background: rgba(59,130,246,0.2);
  margin: 0 4px;
}

.top-right {
  flex-shrink: 0;
}

/* ============ 右侧面板 ============ */
.right-panel {
  position: absolute;
  right: 16px;
  top: 68px;
  width: 240px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.right-panel::-webkit-scrollbar {
  width: 3px;
}
.right-panel::-webkit-scrollbar-thumb {
  background: rgba(59,130,246,0.3);
  border-radius: 2px;
}

.panel-section {
  background: rgba(10,15,30,0.92);
  border: 1px solid rgba(59,130,246,0.2);
  border-radius: 10px;
  padding: 14px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #e2e8f0;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.warn-count {
  background: rgba(239,68,68,0.2);
  color: #f87171;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 10px;
  font-weight: 700;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.ov-item {
  text-align: center;
  padding: 8px 4px;
  background: rgba(30,41,59,0.4);
  border-radius: 6px;
}

.ov-value {
  font-size: 18px;
  font-weight: 700;
  color: #f1f5f9;
  font-family: 'Courier New', monospace;
}

.ov-label {
  font-size: 10px;
  color: #64748b;
  margin-top: 2px;
}

.text-warn { color: #f87171 !important; }

/* 区域占用 */
.zone-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.zone-item {
  cursor: pointer;
  padding: 8px 10px;
  background: rgba(30,41,59,0.3);
  border-radius: 6px;
  border-left: 3px solid transparent;
  transition: all 0.3s;
}

.zone-item:hover {
  background: rgba(30,41,59,0.6);
}

.zone-item.zone-raw { border-left-color: #3b82f6; }
.zone-item.zone-semi { border-left-color: #f59e0b; }
.zone-item.zone-finish { border-left-color: #10b981; }
.zone-item.zone-aux { border-left-color: #a855f7; }

.zone-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.zone-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.zone-raw .zone-dot { background: #3b82f6; box-shadow: 0 0 6px rgba(59,130,246,0.5); }
.zone-semi .zone-dot { background: #f59e0b; box-shadow: 0 0 6px rgba(245,158,11,0.5); }
.zone-finish .zone-dot { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.5); }
.zone-aux .zone-dot { background: #a855f7; box-shadow: 0 0 6px rgba(168,85,247,0.5); }

.zone-name {
  font-size: 12px;
  color: #e2e8f0;
  flex: 1;
}

.zone-pct {
  font-size: 13px;
  font-weight: 700;
  color: #f1f5f9;
  font-family: 'Courier New', monospace;
}

.zone-bar-bg {
  height: 4px;
  background: rgba(30,41,59,0.6);
  border-radius: 2px;
  overflow: hidden;
}

.zone-bar {
  height: 100%;
  border-radius: 2px;
  transition: width 0.5s ease;
}

.zone-raw .zone-bar { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
.zone-semi .zone-bar { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.zone-finish .zone-bar { background: linear-gradient(90deg, #10b981, #34d399); }
.zone-aux .zone-bar { background: linear-gradient(90deg, #a855f7, #c084fc); }

.zone-meta {
  font-size: 10px;
  color: #64748b;
  margin-top: 4px;
}

/* 预警 */
.warning-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 220px;
  overflow-y: auto;
}

.warning-list::-webkit-scrollbar {
  width: 3px;
}
.warning-list::-webkit-scrollbar-thumb {
  background: rgba(239,68,68,0.3);
  border-radius: 2px;
}

.warn-item {
  display: flex;
  gap: 8px;
  padding: 8px 10px;
  background: rgba(30,41,59,0.3);
  border-radius: 6px;
  border-left: 3px solid transparent;
}

.warn-item.warn-high { border-left-color: #ef4444; }
.warn-item.warn-low { border-left-color: #f59e0b; }

.warn-icon {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.warn-high .warn-icon {
  background: rgba(239,68,68,0.2);
  color: #f87171;
}

.warn-low .warn-icon {
  background: rgba(245,158,11,0.2);
  color: #fbbf24;
}

.warn-info {
  flex: 1;
  min-width: 0;
}

.warn-name {
  font-size: 12px;
  font-weight: 600;
  color: #e2e8f0;
}

.warn-desc {
  font-size: 10px;
  color: #64748b;
  margin-top: 2px;
  line-height: 1.4;
}

/* ============ 详情弹窗 ============ */
.detail-popup {
  position: fixed;
  width: 280px;
  background: rgba(10,15,30,0.96);
  border: 1px solid rgba(59,130,246,0.4);
  border-radius: 10px;
  z-index: 20;
  box-shadow: 0 16px 48px rgba(0,0,0,0.6);
  transform: translate(10px, -50%);
  pointer-events: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: linear-gradient(135deg, rgba(59,130,246,0.15), rgba(99,102,241,0.06));
  border-bottom: 1px solid rgba(59,130,246,0.2);
}

.detail-title {
  font-size: 14px;
  font-weight: 600;
  color: #f1f5f9;
}

.close-btn {
  width: 22px;
  height: 22px;
  border: none;
  background: rgba(255,255,255,0.08);
  color: #94a3b8;
  border-radius: 50%;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(255,255,255,0.15);
  color: #fff;
}

.detail-body {
  padding: 10px 14px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid rgba(148,163,184,0.06);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 11px;
  color: #94a3b8;
}

.detail-value {
  font-size: 12px;
  font-weight: 600;
  color: #f1f5f9;
}

/* ============ 底部图例 ============ */
.bottom-legend {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 18px;
  padding: 8px 20px;
  background: rgba(10,15,30,0.9);
  border: 1px solid rgba(59,130,246,0.2);
  border-radius: 8px;
  z-index: 10;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #94a3b8;
}

.legend-line {
  width: 20px;
  height: 3px;
  border-radius: 2px;
}

.legend-raw { background: linear-gradient(90deg, #2563eb, #3b82f6); }
.legend-semi { background: linear-gradient(90deg, #d97706, #f59e0b); }
.legend-finish { background: linear-gradient(90deg, #059669, #10b981); }
.legend-aux { background: linear-gradient(90deg, #9333ea, #a855f7); }

/* ============ 过渡动画 ============ */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.25s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
