<template>
  <div class="situation">
    <!-- 顶部标题栏 -->
    <header class="sit-header">
      <div class="h-left">
        <span class="dot" :class="wsConnected ? 'on' : 'off'" />
        <h1 class="h-title">网络安全态势感知大屏</h1>
      </div>
      <div class="h-right">
        <span class="clock">{{ clock }}</span>
        <el-select v-model="contestId" size="small" style="width: 180px" placeholder="选择赛事" @change="reload">
          <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
    </header>

    <!-- 指标卡 -->
    <section class="kpi-row">
      <div class="kpi" v-for="k in kpis" :key="k.key">
        <div class="kpi-value">{{ k.value }}</div>
        <div class="kpi-label">{{ k.label }}</div>
      </div>
    </section>

    <div class="sit-body">
      <!-- 左：排行榜 + 靶机监控 -->
      <aside class="col left">
        <div class="card">
          <div class="card-title">实时排行榜 TOP10</div>
          <div class="rank-list">
            <div v-for="(t, i) in ranking" :key="t.teamId || t.id" class="rank-row" :class="`r${i + 1}`" @click="goTeam(t)">
              <span class="rk">{{ i + 1 }}</span>
              <span class="tn">{{ t.teamName || t.name }}</span>
              <span class="sc">{{ t.score }}</span>
            </div>
            <el-empty v-if="!ranking.length" description="暂无数据" :image-size="50" />
          </div>
        </div>
        <div class="card">
          <div class="card-title">靶机监控</div>
          <div class="monitor-list">
            <div v-for="m in monitors" :key="m.id" class="monitor-row" :class="m.status.toLowerCase()">
              <span class="m-name">{{ m.name }}</span>
              <span class="m-team">{{ m.teamName }}</span>
              <span class="m-state">{{ m.status }}</span>
            </div>
            <el-empty v-if="!monitors.length" description="暂无监控数据" :image-size="50" />
          </div>
        </div>
      </aside>

      <!-- 中：3D 态势 -->
      <main class="col center">
        <div ref="threeHost" class="three-host" />
        <div class="three-hint">拖拽旋转 · 滚轮缩放 · 点击节点查看详情</div>
      </main>

      <!-- 右：攻击趋势 + 事件流 -->
      <aside class="col right">
        <div class="card">
          <div class="card-title">攻击趋势（近 12 轮）</div>
          <div ref="trendRef" class="chart" />
        </div>
        <div class="card">
          <div class="card-title">实时事件</div>
          <div class="event-list">
            <div v-for="e in events" :key="e.id" class="event" :class="e.type.toLowerCase()">
              <span class="e-time">{{ e.time }}</span>
              <span class="e-text">{{ e.text }}</span>
            </div>
            <el-empty v-if="!events.length" description="暂无事件" :image-size="50" />
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import * as THREE from 'three'
import { getSituationOverview, getSituationRanking, get3DData, getTargetMonitor, listContests } from '@/api'
import { connectWs } from '@/utils/ws'

const props = defineProps({ contestId: { type: [String, Number], default: null } })
const router = useRouter()

const contestId = ref(props.contestId ? Number(props.contestId) : null)
const contests = ref([])
const overview = ref({})
const ranking = ref([])
const monitors = ref([])
const events = ref([])
const threeNodes = ref([])
const trendData = ref({ rounds: [], series: [] })
const clock = ref('')
const wsConnected = ref(false)

const threeHost = ref(null)
const trendRef = ref(null)
let three = null
let trendInst = null
let ws = null
let clockTimer = null
let evSeq = 0
let rafId = null

const kpis = computed(() => [
  { key: 'teams', label: '参赛战队', value: overview.value.teamCount ?? ranking.value.length ?? 0 },
  { key: 'targets', label: '在线靶机', value: overview.value.targetCount ?? monitors.value.filter((m) => m.status !== 'DOWN').length },
  { key: 'attacks', label: '本轮攻击', value: overview.value.attackCount ?? 0 },
  { key: 'solves', label: '解题总数', value: overview.value.solveCount ?? 0 },
  { key: 'first', label: '一血数', value: overview.value.firstBloodCount ?? 0 }
])

function tick() {
  clock.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

function pushEvent(type, text) {
  events.value.unshift({ id: ++evSeq, type, text, time: new Date().toLocaleTimeString('zh-CN', { hour12: false }) })
  if (events.value.length > 60) events.value.pop()
}

function goTeam(t) {
  const id = t.teamId || t.id
  if (id) router.push(`/team/${id}`)
}

/* ---------- Three.js 初始化 ---------- */
function initThree() {
  const host = threeHost.value
  if (!host) return
  const w = host.clientWidth
  const h = host.clientHeight
  const scene = new THREE.Scene()
  scene.fog = new THREE.FogExp2(0x050a18, 0.02)

  const camera = new THREE.PerspectiveCamera(50, w / h, 0.1, 1000)
  camera.position.set(0, 24, 46)
  camera.lookAt(0, 0, 0)

  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(w, h)
  host.appendChild(renderer.domElement)

  scene.add(new THREE.AmbientLight(0x8899bb, 0.7))
  const dir = new THREE.DirectionalLight(0xffffff, 1.1)
  dir.position.set(20, 40, 20)
  scene.add(dir)
  const p1 = new THREE.PointLight(0x4dabf7, 1.4, 120)
  p1.position.set(-20, 10, -20)
  scene.add(p1)

  // 网格地面
  const grid = new THREE.GridHelper(120, 40, 0x1e3a5f, 0x122238)
  scene.add(grid)

  // 中心核心球
  const core = new THREE.Mesh(
    new THREE.IcosahedronGeometry(3, 1),
    new THREE.MeshStandardMaterial({ color: 0x4dabf7, emissive: 0x1a4a7a, roughness: 0.3, metalness: 0.6, wireframe: false })
  )
  core.position.y = 6
  scene.add(core)
  const halo = new THREE.Mesh(
    new THREE.IcosahedronGeometry(4.2, 1),
    new THREE.MeshBasicMaterial({ color: 0x4dabf7, wireframe: true, transparent: true, opacity: 0.22 })
  )
  halo.position.y = 6
  scene.add(halo)

  // 节点组
  const nodeGroup = new THREE.Group()
  scene.add(nodeGroup)

  // 轨道环
  const ringGeo = new THREE.TorusGeometry(20, 0.06, 8, 120)
  const ringMat = new THREE.MeshBasicMaterial({ color: 0x2a6fa8, transparent: true, opacity: 0.35 })
  const ring1 = new THREE.Mesh(ringGeo, ringMat)
  ring1.rotation.x = Math.PI / 2
  ring1.position.y = 1
  scene.add(ring1)
  const ring2 = ring1.clone()
  ring2.rotation.z = Math.PI / 4
  ring2.scale.setScalar(0.7)
  ring2.position.y = 8
  scene.add(ring2)

  three = {
    scene, camera, renderer, nodeGroup, core, halo,
    nodes: [],
    // 简易轨道控制
    spherical: { radius: 52, theta: 0, phi: Math.PI / 3 },
    drag: false, lastX: 0, lastY: 0
  }

  // 事件绑定
  host.addEventListener('mousedown', onDown)
  host.addEventListener('mousemove', onMove)
  host.addEventListener('mouseup', onUp)
  host.addEventListener('mouseleave', onUp)
  host.addEventListener('wheel', onWheel, { passive: false })

  animate()
}

function onDown(e) { three.drag = true; three.lastX = e.clientX; three.lastY = e.clientY }
function onUp() { three.drag = false }
function onMove(e) {
  if (!three?.drag) return
  const dx = e.clientX - three.lastX
  const dy = e.clientY - three.lastY
  three.lastX = e.clientX; three.lastY = e.clientY
  three.spherical.theta -= dx * 0.006
  three.spherical.phi = Math.max(0.25, Math.min(Math.PI / 2.2, three.spherical.phi - dy * 0.004))
}
function onWheel(e) {
  e.preventDefault()
  if (!three) return
  three.spherical.radius = Math.max(26, Math.min(110, three.spherical.radius + e.deltaY * 0.05))
}

let t0 = 0
function animate() {
  rafId = requestAnimationFrame(animate)
  if (!three) return
  const time = performance.now() / 1000
  const s = three.spherical
  const x = s.radius * Math.sin(s.phi) * Math.cos(s.theta)
  const y = s.radius * Math.cos(s.phi) + 6
  const z = s.radius * Math.sin(s.phi) * Math.sin(s.theta)
  three.camera.position.set(x, y, z)
  three.camera.lookAt(0, 6, 0)

  three.core.rotation.y = time * 0.25
  three.core.rotation.x = time * 0.12
  three.halo.rotation.y = -time * 0.18
  three.halo.rotation.z = time * 0.1

  // 节点缓慢自转 + 脉冲
  three.nodeGroup.rotation.y = time * 0.05
  for (const n of three.nodes) {
    if (!n.userData || !n.userData.pulse) continue
    const p = 1 + Math.sin(time * 2.2 + n.userData.phase) * 0.06
    n.scale.setScalar(p)
  }
  three.renderer.render(three.scene, three.camera)
}

/* ---------- 3D 数据映射 ---------- */
function buildNodes(list) {
  if (!three) return
  // 清理旧节点
  for (const n of three.nodes) {
    three.nodeGroup.remove(n)
    n.geometry?.dispose?.()
    n.material?.dispose?.()
  }
  three.nodes = []

  const nodes = Array.isArray(list) ? list : (list?.nodes || [])
  if (!nodes.length) {
    // 占位：按排名生成虚拟节点
    nodes.push(...ranking.value.slice(0, 10).map((t, i) => ({
      id: t.teamId || t.id || i,
      name: t.teamName || t.name || `战队${i + 1}`,
      score: t.score || 0,
      type: 'TEAM'
    })))
  }

  const count = nodes.length || 1
  const radius = 20
  nodes.forEach((nd, i) => {
    const score = Number(nd.score ?? nd.value ?? 0)
    const size = Math.max(0.7, Math.min(2.6, 0.7 + score / 600))
    const color = nd.status === 'DOWN' ? 0xf53f3f : nd.status === 'ATTACKED' ? 0xff7d00 : (nd.type === 'TARGET' ? 0xffd43b : 0x4dabf7)
    const geo = new THREE.SphereGeometry(size, 16, 16)
    const mat = new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.45, roughness: 0.35, metalness: 0.5 })
    const mesh = new THREE.Mesh(geo, mat)
    const angle = (i / count) * Math.PI * 2
    const yOffset = nd.type === 'TARGET' ? -2 : 2
    mesh.position.set(Math.cos(angle) * radius, yOffset + Math.sin(i * 1.7) * 2, Math.sin(angle) * radius)
    mesh.userData = { id: nd.id, name: nd.name, score, type: nd.type, pulse: true, phase: i * 0.8 }
    three.nodeGroup.add(mesh)
    three.nodes.push(mesh)

    // 节点到核心的连接线
    const points = [mesh.position.clone(), new THREE.Vector3(0, 6, 0)]
    const lineGeo = new THREE.BufferGeometry().setFromPoints(points)
    const lineMat = new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.18 })
    const line = new THREE.Line(lineGeo, lineMat)
    three.nodeGroup.add(line)
    three.nodes.push(line)
  })
}

/* ---------- 趋势图 ---------- */
function renderTrend(data) {
  if (!trendInst) return
  const rounds = data?.rounds || data?.labels || []
  const series = (data?.series || data?.datasets || []).map((s) => ({
    name: s.name || s.teamName || '攻击',
    type: 'line',
    smooth: true,
    showSymbol: false,
    areaStyle: { opacity: 0.12 },
    data: s.data || s.values || []
  }))
  trendInst.setOption({
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#a8b7cc', fontSize: 10 }, top: 0, type: 'scroll' },
    grid: { left: 36, right: 12, top: 30, bottom: 22 },
    xAxis: {
      type: 'category',
      data: rounds.length ? rounds : Array.from({ length: 12 }, (_, i) => `R${i + 1}`),
      axisLabel: { color: '#86909c', fontSize: 10 },
      axisLine: { lineStyle: { color: '#e5e6eb' } }
    },
    yAxis: { type: 'value', axisLabel: { color: '#86909c', fontSize: 10 }, splitLine: { lineStyle: { color: '#ffffff' } } },
    series: series.length ? series : [{
      name: '攻击次数', type: 'line', smooth: true, showSymbol: false,
      areaStyle: { opacity: 0.15 }, data: Array.from({ length: 12 }, () => 0)
    }]
  }, true)
}

/* ---------- 数据加载 ---------- */
async function reload() {
  await Promise.all([loadOverview(), loadRanking(), load3D(), loadMonitor()])
}

async function loadOverview() {
  try {
    const o = await getSituationOverview(contestId.value ? { contestId: contestId.value } : undefined)
    overview.value = o || {}
    if (o?.trend) renderTrend(o.trend)
  } catch (e) { /* 静默 */ }
}

async function loadRanking() {
  try {
    const r = await getSituationRanking(contestId.value ? { contestId: contestId.value, limit: 10 } : { limit: 10 })
    ranking.value = Array.isArray(r) ? r : (r?.records || r?.list || [])
  } catch (e) { ranking.value = [] }
}

async function load3D() {
  try {
    const d = await get3DData(contestId.value ? { contestId: contestId.value } : undefined)
    threeNodes.value = Array.isArray(d) ? d : (d?.nodes || [])
    buildNodes(threeNodes.value)
  } catch (e) { buildNodes([]) }
}

async function loadMonitor() {
  try {
    const m = await getTargetMonitor(contestId.value ? { contestId: contestId.value } : undefined)
    monitors.value = (Array.isArray(m) ? m : (m?.records || [])).map((x) => ({
      id: x.id, name: x.name || x.hostname || `靶机-${x.id}`,
      teamName: x.teamName || x.ownerTeam || '—',
      status: (x.status || 'UNKNOWN').toUpperCase()
    }))
  } catch (e) { monitors.value = [] }
}

async function loadContests() {
  try {
    const c = await listContests({ size: 50 })
    contests.value = Array.isArray(c) ? c : (c?.records || [])
    if (!contestId.value && contests.value.length) contestId.value = contests.value[0].id
  } catch (e) { /* 静默 */ }
}

function subscribe() {
  try {
    const path = contestId.value ? `/ws/contest/${contestId.value}` : '/ws/contest/0'
    ws = connectWs(path, (msg) => {
      const type = (msg.type || '').toUpperCase()
      const d = msg.data || {}
      wsConnected.value = true
      if (type === 'SCORE_UPDATE') {
        pushEvent('SCORE', `${d.teamName || '战队'} 得分更新 ${d.score ?? ''}`)
        loadRanking()
        load3D()
      } else if (type === 'TARGET_DOWN') {
        pushEvent('DOWN', `靶机 ${d.targetName || d.name || ''} 宕机`)
        loadMonitor()
        load3D()
      } else if (type === 'TARGET_UP') {
        pushEvent('INFO', `靶机 ${d.targetName || d.name || ''} 恢复`)
        loadMonitor()
      } else if (type === 'FIRST_BLOOD') {
        pushEvent('FIRST', `一血：${d.teamName || ''} ${d.targetName || ''}`)
        loadOverview()
      } else if (type === 'ANNOUNCE') {
        pushEvent('INFO', d.content || String(d))
      }
    })
    setTimeout(() => { wsConnected.value = true }, 800)
  } catch (e) { wsConnected.value = false }
}

function resize() {
  if (three && threeHost.value) {
    const w = threeHost.value.clientWidth
    const h = threeHost.value.clientHeight
    three.camera.aspect = w / h
    three.camera.updateProjectionMatrix()
    three.renderer.setSize(w, h)
  }
  trendInst?.resize()
}

onMounted(async () => {
  tick()
  clockTimer = setInterval(tick, 1000)
  await loadContests()
  await nextTick()
  initThree()
  trendInst = echarts.init(trendRef.value)
  await reload()
  subscribe()
  window.addEventListener('resize', resize)
  pushEvent('INFO', '态势大屏已接入实时数据流')
})

onBeforeUnmount(() => {
  clearInterval(clockTimer)
  ws?.close()
  cancelAnimationFrame(rafId)
  window.removeEventListener('resize', resize)
  if (three) {
    const host = threeHost.value
    if (host) {
      host.removeEventListener('mousedown', onDown)
      host.removeEventListener('mousemove', onMove)
      host.removeEventListener('mouseup', onUp)
      host.removeEventListener('mouseleave', onUp)
      host.removeEventListener('wheel', onWheel)
      if (three.renderer.domElement.parentNode === host) host.removeChild(three.renderer.domElement)
    }
    three.renderer.dispose()
  }
  trendInst?.dispose()
})
</script>

<style scoped>
.situation {
  height: 100vh; display: flex; flex-direction: column;
  background: radial-gradient(ellipse at 50% 0%, #ffffff 0%, #f7f8fa 75%);
  color: #1d2129; font-family: var(--csp-font-family); overflow: hidden;
}

.sit-header {
  height: 60px; flex: 0 0 60px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; border-bottom: 1px solid rgba(77, 171, 247, 0.18);
  background: linear-gradient(90deg, rgba(17, 26, 53, 0.9), rgba(22, 32, 61, 0.5));
}
.h-left { display: flex; align-items: center; gap: 12px; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot.on { background: #00b42a; box-shadow: 0 0 8px #00b42a; }
.dot.off { background: #f53f3f; }
.h-title { margin: 0; font-size: 20px; font-weight: 600; letter-spacing: 2px; }
.h-right { display: flex; align-items: center; gap: 16px; }
.clock { font-family: var(--csp-font-mono); font-size: 14px; color: #86909c; }

.kpi-row { flex: 0 0 auto; display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; padding: 14px 24px 0; }
.kpi {
  padding: 10px 14px; border-radius: 8px;
  background: linear-gradient(135deg, rgba(77, 171, 247, 0.14), rgba(77, 171, 247, 0.03));
  border: 1px solid rgba(77, 171, 247, 0.2);
}
.kpi-value { font-family: var(--csp-font-mono); font-size: 24px; font-weight: 700; color: #295FFF; }
.kpi-label { font-size: 12px; color: #86909c; margin-top: 2px; }

.sit-body { flex: 1; min-height: 0; display: grid; grid-template-columns: 320px 1fr 340px; gap: 14px; padding: 14px 24px 20px; }
.col { display: flex; flex-direction: column; gap: 14px; min-height: 0; }
.card {
  display: flex; flex-direction: column; min-height: 0;
  background: rgba(15, 23, 48, 0.78); border: 1px solid rgba(77, 171, 247, 0.14);
  border-radius: 8px; padding: 12px 14px;
}
.left .card:first-child { flex: 0 0 auto; }
.left .card:last-child { flex: 1; }
.right .card:first-child { flex: 0 0 240px; }
.right .card:last-child { flex: 1; }
.card-title { font-size: 13px; font-weight: 600; color: #4e5969; letter-spacing: 1px; margin-bottom: 8px; flex: 0 0 auto; }

.rank-list { display: flex; flex-direction: column; gap: 4px; overflow-y: auto; }
.rank-row {
  display: grid; grid-template-columns: 26px 1fr auto; align-items: center; gap: 8px;
  padding: 6px 8px; border-radius: 6px; font-size: 13px; cursor: pointer;
  background: rgba(255, 255, 255, 0.02); transition: background 0.15s;
}
.rank-row:hover { background: rgba(77, 171, 247, 0.14); }
.rank-row .rk { font-family: var(--csp-font-mono); color: #86909c; text-align: center; }
.rank-row .tn { color: #4e5969; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-row .sc { font-family: var(--csp-font-mono); color: #ff7d00; font-weight: 700; }
.rank-row.r1 { background: rgba(255, 212, 59, 0.12); }
.rank-row.r1 .rk { color: #ff7d00; }
.rank-row.r2 { background: rgba(192, 204, 218, 0.08); }
.rank-row.r3 { background: rgba(205, 127, 50, 0.1); }

.monitor-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 5px; }
.monitor-row {
  display: grid; grid-template-columns: 1fr 1fr 62px; gap: 8px; align-items: center;
  font-size: 12px; padding: 5px 8px; border-radius: 5px;
  background: rgba(255, 255, 255, 0.03); border-left: 3px solid #00b42a;
}
.monitor-row.down { border-left-color: #f53f3f; }
.monitor-row.attacked { border-left-color: #ff7d00; }
.monitor-row.unknown { border-left-color: #86909c; }
.m-name { color: #1d2129; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.m-team { color: #86909c; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.m-state { font-family: var(--csp-font-mono); font-size: 11px; color: #a8b7cc; text-align: right; }

.center { position: relative; }
.three-host { flex: 1; min-height: 0; border-radius: 8px; overflow: hidden; background: radial-gradient(ellipse at center, #ffffff 0%, #f7f8fa 80%); border: 1px solid rgba(77, 171, 247, 0.12); }
.three-host canvas { display: block; }
.three-hint { position: absolute; bottom: 12px; left: 50%; transform: translateX(-50%); font-size: 11px; color: #4b5b74; pointer-events: none; }

.chart { flex: 1; min-height: 0; }
.event-list { flex: 1; min-height: 0; overflow-y: auto; display: flex; flex-direction: column; gap: 4px; }
.event { display: flex; gap: 8px; font-size: 12px; line-height: 1.5; padding: 3px 0; border-bottom: 1px dashed rgba(255, 255, 255, 0.04); }
.e-time { flex: 0 0 auto; color: #86909c; font-family: var(--csp-font-mono); }
.event.down .e-text { color: #f53f3f; }
.event.first .e-text { color: #ff7d00; }
.event.score .e-text { color: #295FFF; }
.event.info .e-text { color: #86909c; }

:deep(.el-input__wrapper) { background: #f7f8fa; box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset; }
:deep(.el-input__inner) { color: #1d2129; }
</style>