<template>
  <div class="bracelet-3d-page">
    <!-- 左侧3D画布区域 -->
    <div class="canvas-area">
      <div ref="canvasContainer" class="canvas-container"></div>
    </div>

    <!-- 右侧零件面板 -->
    <div class="side-panel">
      <div class="panel-header">
        <span class="panel-title">零件列表</span>
        <span class="panel-count">{{ parts.length }}个组件</span>
      </div>
      <div class="part-list">
        <div
          v-for="part in parts"
          :key="part.id"
          class="part-item"
          :class="{ active: highlightedPart === part.id }"
          @click="highlightPart(part.id)"
          @mouseenter="hoverPart(part.id)"
          @mouseleave="unhoverPart()"
        >
          <div class="part-color" :style="{ backgroundColor: part.color }"></div>
          <div class="part-info">
            <div class="part-name">{{ part.name }}</div>
            <div class="part-desc">{{ part.desc }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 顶部控制栏 -->
    <div class="top-bar">
      <div class="top-left">
        <h1 class="page-title">智能手环3D拆解</h1>
        <span class="page-subtitle">Smart Bracelet Exploded View</span>
      </div>
      <div class="top-controls">
        <button class="ctrl-btn explode" :class="{ disabled: isExploded }" @click="explodeView">
          <span class="btn-icon">⬆</span> 拆分
        </button>
        <button class="ctrl-btn assemble" :class="{ disabled: !isExploded }" @click="assembleView">
          <span class="btn-icon">⬇</span> 合并
        </button>
        <div class="divider"></div>
        <button class="ctrl-btn" :class="{ active: autoRotate }" @click="toggleAutoRotate">
          <span class="btn-icon">🔄</span> 自动旋转
        </button>
        <button class="ctrl-btn" @click="zoomIn">
          <span class="btn-icon">🔍+</span>
        </button>
        <button class="ctrl-btn" @click="zoomOut">
          <span class="btn-icon">🔍-</span>
        </button>
        <button class="ctrl-btn" @click="resetCamera">
          <span class="btn-icon">↩</span> 复位
        </button>
        <button class="ctrl-btn" @click="toggleFullscreen">
          <span class="btn-icon">{{ isFullscreen ? '⛶' : '⛶' }}</span> {{ isFullscreen ? '退出全屏' : '全屏' }}
        </button>
      </div>
    </div>

    <!-- 底部信息栏 -->
    <div class="bottom-bar">
      <div class="product-info">
        <span class="info-tag">型号: SB-Pro X1</span>
        <span class="info-tag">屏幕: 1.4" AMOLED</span>
        <span class="info-tag">电池: 240mAh</span>
        <span class="info-tag">防水: IP68</span>
      </div>
      <div class="operation-hint">
        鼠标左键旋转 · 滚轮缩放 · 右键平移 · 点击零件查看详情
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

// ========== 响应式数据 ==========
const canvasContainer = ref(null)
const isExploded = ref(false)
const autoRotate = ref(true)
const highlightedPart = ref(null)
const isFullscreen = ref(false)

const parts = reactive([
  { id: 'screen', name: '屏幕组件', desc: '1.4英寸AMOLED', color: '#1e3a5f' },
  { id: 'case', name: '表壳', desc: '铝合金边框', color: '#6b7280' },
  { id: 'band', name: '表带', desc: '硅胶材质', color: '#4b5563' },
  { id: 'battery', name: '电池', desc: '240mAh锂电池', color: '#ef4444' },
  { id: 'motherboard', name: '主板', desc: '主控芯片+传感器', color: '#10b981' },
  { id: 'sensor', name: '传感器', desc: '心率/血氧', color: '#3b82f6' },
  { id: 'motor', name: '震动马达', desc: '线性震动器', color: '#8b5cf6' },
  { id: 'coil', name: '充电线圈', desc: '无线充电', color: '#f59e0b' },
])

// ========== Three.js 变量 ==========
let scene, camera, renderer, controls
let partMeshes = {} // { partId: mesh }
let animationId = null
let animationProgress = 0
let isAnimating = false
let animationDirection = 1 // 1 = exploding, -1 = assembling
const animationDuration = 1000 // ms
let animationStartTime = 0

// 每个零件的原始位置和爆炸位置
const partPositions = {
  screen:      { origin: [0, 0.22, 0],      exploded: [0, 2.8, 0] },
  case:        { origin: [0, 0, 0],           exploded: [0, 0, 0] },
  band:        { origin: [0, -0.15, 0],       exploded: [0, -2.5, 0] },
  battery:     { origin: [0, -0.08, 0.08],    exploded: [2.2, -0.5, 1.5] },
  motherboard: { origin: [0, 0.0, 0.05],      exploded: [-2.2, 0, 1.5] },
  sensor:      { origin: [0, -0.12, -0.12],   exploded: [0, -1.2, -2.5] },
  motor:       { origin: [0.12, 0.0, 0.0],    exploded: [2.5, 0.8, -0.5] },
  coil:        { origin: [0, -0.16, 0.0],     exploded: [-2.0, -1.5, -1.0] },
}

// ========== 缓动函数 ==========
function easeOut(t) {
  return 1 - Math.pow(1 - t, 3)
}

// ========== 创建场景 ==========
function initScene() {
  const container = canvasContainer.value
  const width = container.clientWidth
  const height = container.clientHeight

  // 场景
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x0f172a)

  // 相机
  camera = new THREE.PerspectiveCamera(50, width / height, 0.1, 100)
  camera.position.set(4, 3, 5)
  camera.lookAt(0, 0, 0)

  // 渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2
  container.appendChild(renderer.domElement)

  // 控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.autoRotate = autoRotate.value
  controls.autoRotateSpeed = 1.5
  controls.minDistance = 2
  controls.maxDistance = 15

  // 灯光
  setupLights()

  // 创建零件
  createParts()

  // 创建环境
  createEnvironment()

  // 监听窗口大小变化
  window.addEventListener('resize', onResize)
}

function setupLights() {
  // 环境光
  const ambientLight = new THREE.AmbientLight(0x404060, 0.6)
  scene.add(ambientLight)

  // 主方向光
  const mainLight = new THREE.DirectionalLight(0xffffff, 1.2)
  mainLight.position.set(5, 8, 5)
  mainLight.castShadow = true
  mainLight.shadow.mapSize.width = 2048
  mainLight.shadow.mapSize.height = 2048
  mainLight.shadow.camera.near = 0.5
  mainLight.shadow.camera.far = 30
  mainLight.shadow.camera.left = -5
  mainLight.shadow.camera.right = 5
  mainLight.shadow.camera.top = 5
  mainLight.shadow.camera.bottom = -5
  scene.add(mainLight)

  // 补光
  const fillLight = new THREE.DirectionalLight(0x6090ff, 0.4)
  fillLight.position.set(-3, 4, -3)
  scene.add(fillLight)

  // 底部补光
  const bottomLight = new THREE.DirectionalLight(0xff9060, 0.2)
  bottomLight.position.set(0, -3, 2)
  scene.add(bottomLight)

  // 点光源装饰
  const pointLight1 = new THREE.PointLight(0x3b82f6, 0.5, 10)
  pointLight1.position.set(-3, 2, 3)
  scene.add(pointLight1)

  const pointLight2 = new THREE.PointLight(0x8b5cf6, 0.3, 10)
  pointLight2.position.set(3, -1, -2)
  scene.add(pointLight2)
}

function createMaterial(color, emissiveIntensity = 0.05) {
  return new THREE.MeshStandardMaterial({
    color: new THREE.Color(color),
    metalness: 0.3,
    roughness: 0.4,
    emissive: new THREE.Color(color),
    emissiveIntensity,
  })
}

function createParts() {
  // 屏幕组件 - 扁平长方体
  const screenGeo = new THREE.BoxGeometry(
    Math.max(0.01, 1.0),
    Math.max(0.01, 0.04),
    Math.max(0.01, 0.85)
  )
  const screenMat = createMaterial('#1e3a5f', 0.1)
  const screenMesh = new THREE.Mesh(screenGeo, screenMat)
  screenMesh.castShadow = true
  screenMesh.receiveShadow = true
  addPartMesh('screen', screenMesh)

  // 表壳 - 圆柱体
  const caseGeo = new THREE.CylinderGeometry(
    Math.max(0.01, 0.6),
    Math.max(0.01, 0.6),
    Math.max(0.01, 0.35),
    32
  )
  const caseMat = createMaterial('#6b7280', 0.08)
  caseMat.metalness = 0.7
  caseMat.roughness = 0.2
  const caseMesh = new THREE.Mesh(caseGeo, caseMat)
  caseMesh.castShadow = true
  caseMesh.receiveShadow = true
  addPartMesh('case', caseMesh)

  // 表带 - 使用两个长方体和弧形连接
  const bandGroup = new THREE.Group()

  const bandGeo = new THREE.BoxGeometry(
    Math.max(0.01, 0.5),
    Math.max(0.01, 0.06),
    Math.max(0.01, 1.8)
  )
  const bandMat = createMaterial('#4b5563', 0.02)
  bandMat.roughness = 0.8
  const bandTop = new THREE.Mesh(bandGeo, bandMat)
  bandTop.position.set(0, 0, 0.85)
  bandTop.castShadow = true
  bandGroup.add(bandTop)

  const bandBottom = new THREE.Mesh(bandGeo, bandMat)
  bandBottom.position.set(0, 0, -0.85)
  bandBottom.castShadow = true
  bandGroup.add(bandBottom)

  // 弧形连接段
  const torusGeo = new THREE.TorusGeometry(
    Math.max(0.01, 0.25),
    Math.max(0.01, 0.03),
    8,
    16,
    Math.PI
  )
  const torusTop = new THREE.Mesh(torusGeo, bandMat)
  torusTop.position.set(0, 0, 1.75)
  torusTop.rotation.x = Math.PI / 2
  bandGroup.add(torusTop)

  const torusBottom = new THREE.Mesh(torusGeo, bandMat)
  torusBottom.position.set(0, 0, -1.75)
  torusBottom.rotation.x = -Math.PI / 2
  bandGroup.add(torusBottom)

  addPartMesh('band', bandGroup)

  // 电池 - 小长方体
  const batteryGeo = new THREE.BoxGeometry(
    Math.max(0.01, 0.55),
    Math.max(0.01, 0.06),
    Math.max(0.01, 0.35)
  )
  const batteryMat = createMaterial('#ef4444', 0.08)
  const batteryMesh = new THREE.Mesh(batteryGeo, batteryMat)
  batteryMesh.castShadow = true
  addPartMesh('battery', batteryMesh)

  // 主板 - 绿色PCB板
  const mbGeo = new THREE.BoxGeometry(
    Math.max(0.01, 0.7),
    Math.max(0.01, 0.03),
    Math.max(0.01, 0.55)
  )
  const mbMat = createMaterial('#10b981', 0.1)
  const mbMesh = new THREE.Mesh(mbGeo, mbMat)
  mbMesh.castShadow = true
  addPartMesh('motherboard', mbMesh)

  // 主板上芯片细节
  const chipGeo = new THREE.BoxGeometry(
    Math.max(0.01, 0.12),
    Math.max(0.01, 0.015),
    Math.max(0.01, 0.12)
  )
  const chipMat = new THREE.MeshStandardMaterial({ color: 0x1a1a2e, metalness: 0.5, roughness: 0.3 })
  const chip1 = new THREE.Mesh(chipGeo, chipMat)
  chip1.position.set(-0.1, 0.02, 0.05)
  mbMesh.add(chip1)
  const chip2 = new THREE.Mesh(chipGeo, chipMat)
  chip2.position.set(0.15, 0.02, -0.08)
  mbMesh.add(chip2)

  // 传感器 - 小圆柱
  const sensorGeo = new THREE.CylinderGeometry(
    Math.max(0.01, 0.1),
    Math.max(0.01, 0.1),
    Math.max(0.01, 0.06),
    16
  )
  const sensorMat = createMaterial('#3b82f6', 0.15)
  const sensorMesh = new THREE.Mesh(sensorGeo, sensorMat)
  sensorMesh.castShadow = true
  addPartMesh('sensor', sensorMesh)

  // 传感器LED细节
  const ledGeo = new THREE.CylinderGeometry(
    Math.max(0.01, 0.03),
    Math.max(0.01, 0.03),
    Math.max(0.01, 0.01),
    8
  )
  const ledMat = new THREE.MeshStandardMaterial({
    color: 0x3b82f6,
    emissive: 0x3b82f6,
    emissiveIntensity: 0.8,
  })
  for (let i = 0; i < 4; i++) {
    const led = new THREE.Mesh(ledGeo, ledMat)
    const angle = (i / 4) * Math.PI * 2
    led.position.set(Math.cos(angle) * 0.06, 0.035, Math.sin(angle) * 0.06)
    sensorMesh.add(led)
  }

  // 震动马达 - 小圆柱
  const motorGeo = new THREE.CylinderGeometry(
    Math.max(0.01, 0.06),
    Math.max(0.01, 0.06),
    Math.max(0.01, 0.18),
    12
  )
  const motorMat = createMaterial('#8b5cf6', 0.08)
  const motorMesh = new THREE.Mesh(motorGeo, motorMat)
  motorMesh.castShadow = true
  addPartMesh('motor', motorMesh)

  // 充电线圈 - 环形
  const coilGeo = new THREE.RingGeometry(
    Math.max(0.01, 0.2),
    Math.max(0.01, 0.35),
    32
  )
  const coilMat = createMaterial('#f59e0b', 0.12)
  const coilMesh = new THREE.Mesh(coilGeo, coilMat)
  coilMesh.rotation.x = -Math.PI / 2
  coilMesh.castShadow = true
  addPartMesh('coil', coilMesh)

  // 线圈中心细节
  const coilCenterGeo = new THREE.RingGeometry(
    Math.max(0.01, 0.05),
    Math.max(0.01, 0.15),
    16
  )
  const coilCenterMesh = new THREE.Mesh(coilCenterGeo, coilMat)
  coilCenterMesh.position.z = 0.002
  coilMesh.add(coilCenterMesh)

  // 设置初始位置
  setAllPositions('origin')
}

function addPartMesh(partId, mesh) {
  partMeshes[partId] = mesh
  scene.add(mesh)
}

function setAllPositions(positionType) {
  for (const [id, pos] of Object.entries(partPositions)) {
    const mesh = partMeshes[id]
    if (mesh) {
      const p = pos[positionType]
      mesh.position.set(p[0], p[1], p[2])
    }
  }
}

function createEnvironment() {
  // 地面网格
  const gridHelper = new THREE.GridHelper(20, 40, 0x1e293b, 0x1e293b)
  gridHelper.position.y = -1.5
  gridHelper.material.opacity = 0.3
  gridHelper.material.transparent = true
  scene.add(gridHelper)

  // 圆形地面
  const groundGeo = new THREE.CircleGeometry(Math.max(0.01, 6), 64)
  const groundMat = new THREE.MeshStandardMaterial({
    color: 0x1e293b,
    metalness: 0.1,
    roughness: 0.9,
    transparent: true,
    opacity: 0.5,
  })
  const ground = new THREE.Mesh(groundGeo, groundMat)
  ground.rotation.x = -Math.PI / 2
  ground.position.y = -1.49
  ground.receiveShadow = true
  scene.add(ground)

  // 粒子效果
  createParticles()
}

function createParticles() {
  const count = 200
  const positions = new Float32Array(count * 3)
  for (let i = 0; i < count; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 15
    positions[i * 3 + 1] = (Math.random() - 0.5) * 10
    positions[i * 3 + 2] = (Math.random() - 0.5) * 15
  }
  const particleGeo = new THREE.BufferGeometry()
  particleGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const particleMat = new THREE.PointsMaterial({
    color: 0x60a5fa,
    size: 0.02,
    transparent: true,
    opacity: 0.6,
    sizeAttenuation: true,
  })
  const particles = new THREE.Points(particleGeo, particleMat)
  particles.name = 'particles'
  scene.add(particles)
}

// ========== 动画循环 ==========
function animate() {
  animationId = requestAnimationFrame(animate)

  // 更新控制器
  controls.update()

  // 粒子动画
  const particles = scene.getObjectByName('particles')
  if (particles) {
    particles.rotation.y += 0.0003
  }

  // 高亮呼吸效果
  if (highlightedPart.value && partMeshes[highlightedPart.value]) {
    const mesh = partMeshes[highlightedPart.value]
    const pulse = 0.5 + Math.sin(Date.now() * 0.005) * 0.15
    if (mesh.material) {
      mesh.material.emissiveIntensity = pulse
    }
    // Group情况（表带）
    if (mesh.children) {
      mesh.traverse((child) => {
        if (child.isMesh && child.material) {
          child.material.emissiveIntensity = pulse
        }
      })
    }
  }

  renderer.render(scene, camera)
}

// ========== 拆分/合并动画 ==========
function explodeView() {
  if (isExploded.value || isAnimating) return
  isAnimating = true
  animationDirection = 1
  animationStartTime = Date.now()
  animateExplosion()
}

function assembleView() {
  if (!isExploded.value || isAnimating) return
  isAnimating = true
  animationDirection = -1
  animationStartTime = Date.now()
  animateExplosion()
}

function animateExplosion() {
  const elapsed = Date.now() - animationStartTime
  let t = Math.min(elapsed / animationDuration, 1)
  t = easeOut(t)

  for (const [id, pos] of Object.entries(partPositions)) {
    const mesh = partMeshes[id]
    if (!mesh) continue

    const from = animationDirection === 1 ? pos.origin : pos.exploded
    const to = animationDirection === 1 ? pos.exploded : pos.origin

    mesh.position.set(
      from[0] + (to[0] - from[0]) * t,
      from[1] + (to[1] - from[1]) * t,
      from[2] + (to[2] - from[2]) * t
    )
  }

  if (t < 1) {
    requestAnimationFrame(animateExplosion)
  } else {
    isAnimating = false
    isExploded.value = animationDirection === 1
  }
}

// ========== 交互方法 ==========
function highlightPart(partId) {
  // 还原之前高亮的零件
  resetHighlight()

  if (highlightedPart.value === partId) {
    highlightedPart.value = null
    return
  }

  highlightedPart.value = partId

  // 降低其他零件透明度
  for (const [id, mesh] of Object.entries(partMeshes)) {
    if (id === partId) continue
    setMeshOpacity(mesh, 0.25)
  }
}

function resetHighlight() {
  for (const [id, mesh] of Object.entries(partMeshes)) {
    setMeshOpacity(mesh, 1.0)
    // 重置emissiveIntensity
    if (mesh.material) {
      mesh.material.emissiveIntensity = 0.05
    }
    mesh.traverse((child) => {
      if (child.isMesh && child.material && child.material.emissiveIntensity > 0.1) {
        // 保留LED等高发光材质
        if (child.material.color && child.material.color.getHexString() !== '3b82f6') {
          child.material.emissiveIntensity = 0.05
        }
      }
    })
  }
}

function setMeshOpacity(mesh, opacity) {
  const applyOpacity = (m) => {
    if (m.material) {
      m.material.transparent = opacity < 1.0
      m.material.opacity = opacity
      m.material.needsUpdate = true
    }
  }

  if (mesh.isMesh) {
    applyOpacity(mesh)
  }
  if (mesh.children) {
    mesh.traverse((child) => {
      if (child.isMesh) {
        applyOpacity(child)
      }
    })
  }
}

function hoverPart(partId) {
  if (highlightedPart.value) return
  // 悬停时轻微降低其他零件透明度
  for (const [id, mesh] of Object.entries(partMeshes)) {
    if (id === partId) continue
    setMeshOpacity(mesh, 0.6)
  }
}

function unhoverPart() {
  if (highlightedPart.value) return
  for (const [, mesh] of Object.entries(partMeshes)) {
    setMeshOpacity(mesh, 1.0)
  }
}

function toggleAutoRotate() {
  autoRotate.value = !autoRotate.value
  controls.autoRotate = autoRotate.value
}

function zoomIn() {
  const direction = new THREE.Vector3()
  camera.getWorldDirection(direction)
  camera.position.addScaledVector(direction, 0.5)
}

function zoomOut() {
  const direction = new THREE.Vector3()
  camera.getWorldDirection(direction)
  camera.position.addScaledVector(direction, -0.5)
}

function resetCamera() {
  camera.position.set(4, 3, 5)
  camera.lookAt(0, 0, 0)
  controls.target.set(0, 0, 0)
  controls.update()
}

function toggleFullscreen() {
  const container = document.querySelector('.bracelet-3d-page')
  if (!document.fullscreenElement) {
    container?.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

function onResize() {
  const container = canvasContainer.value
  if (!container) return
  const width = container.clientWidth
  const height = container.clientHeight
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

// ========== 生命周期 ==========
onMounted(() => {
  initScene()
  animate()
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  // 清理Three.js资源
  if (controls) controls.dispose()
  if (renderer) {
    renderer.dispose()
    renderer.forceContextLoss()
    renderer.domElement = null
  }
  if (scene) {
    scene.traverse((child) => {
      if (child.isMesh) {
        child.geometry.dispose()
        if (child.material.isMaterial) {
          child.material.dispose()
        }
      }
      if (child.isPoints) {
        child.geometry.dispose()
        child.material.dispose()
      }
    })
  }
  scene = null
  camera = null
  renderer = null
  controls = null
  partMeshes = {}
})
</script>

<style scoped>
.bracelet-3d-page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
  overflow: hidden;
  display: flex;
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
  color: #e2e8f0;
}

/* ========== 3D画布区域 ========== */
.canvas-area {
  flex: 1;
  position: relative;
}

.canvas-container {
  width: 100%;
  height: 100%;
}

/* ========== 右侧零件面板 ========== */
.side-panel {
  width: 280px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 27, 75, 0.9) 100%);
  border-left: 1px solid rgba(99, 102, 241, 0.2);
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(20px);
  z-index: 10;
}

.panel-header {
  padding: 20px 20px 16px;
  border-bottom: 1px solid rgba(99, 102, 241, 0.15);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
}

.panel-count {
  font-size: 12px;
  color: #94a3b8;
  background: rgba(99, 102, 241, 0.15);
  padding: 2px 8px;
  border-radius: 10px;
}

.part-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
}

.part-list::-webkit-scrollbar {
  width: 4px;
}

.part-list::-webkit-scrollbar-track {
  background: transparent;
}

.part-list::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.3);
  border-radius: 2px;
}

.part-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  margin: 4px 0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.part-item:hover {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
  transform: translateX(-2px);
}

.part-item.active {
  background: rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.1);
}

.part-color {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  flex-shrink: 0;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.3);
}

.part-info {
  flex: 1;
  min-width: 0;
}

.part-name {
  font-size: 13px;
  font-weight: 600;
  color: #f1f5f9;
  line-height: 1.4;
}

.part-desc {
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.4;
}

/* ========== 顶部控制栏 ========== */
.top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 280px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.9) 0%, transparent 100%);
  z-index: 10;
}

.top-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #818cf8, #6366f1, #4f46e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.page-subtitle {
  font-size: 12px;
  color: #64748b;
  letter-spacing: 0.5px;
}

.top-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.divider {
  width: 1px;
  height: 24px;
  background: rgba(99, 102, 241, 0.2);
  margin: 0 4px;
}

.ctrl-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 500;
  color: #cbd5e1;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  user-select: none;
}

.ctrl-btn:hover {
  background: rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.4);
  color: #f1f5f9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.ctrl-btn:active {
  transform: translateY(0);
}

.ctrl-btn.active {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.5);
  color: #a5b4fc;
}

.ctrl-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

.ctrl-btn.explode {
  border-color: rgba(59, 130, 246, 0.3);
}

.ctrl-btn.explode:hover {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.5);
}

.ctrl-btn.assemble {
  border-color: rgba(16, 185, 129, 0.3);
}

.ctrl-btn.assemble:hover {
  background: rgba(16, 185, 129, 0.15);
  border-color: rgba(16, 185, 129, 0.5);
}

.btn-icon {
  font-size: 13px;
}

/* ========== 底部信息栏 ========== */
.bottom-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 280px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: linear-gradient(0deg, rgba(15, 23, 42, 0.9) 0%, transparent 100%);
  z-index: 10;
}

.product-info {
  display: flex;
  gap: 16px;
}

.info-tag {
  font-size: 11px;
  color: #94a3b8;
  background: rgba(30, 41, 59, 0.8);
  padding: 3px 10px;
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.operation-hint {
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.3px;
}
</style>
