<template>
  <div class="knowledge-graph">
    <div class="page-header">
      <h2 class="page-title">知识图谱</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索节点..."
          class="search-input"
          @keyup.enter="searchNodes"
        >
          <template #append>
            <el-button @click="searchNodes">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="loadGraphData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon process-icon">
          <el-icon><SetUp /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.processCount }}</div>
          <div class="stat-label">工序节点</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon defect-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.defectCount }}</div>
          <div class="stat-label">不良现象</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cause-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.causeCount }}</div>
          <div class="stat-label">缺陷原因</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon solution-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.solutionCount }}</div>
          <div class="stat-label">解决方案</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon material-icon">
          <el-icon><Box /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.materialCount }}</div>
          <div class="stat-label">物料节点</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon equipment-icon">
          <el-icon><Cpu /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.equipmentCount }}</div>
          <div class="stat-label">设备节点</div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="graph-container">
        <div ref="graphRef" class="graph-canvas"></div>
      </div>
      <div class="detail-panel">
        <div class="panel-header">
          <h3>节点详情</h3>
          <el-button v-if="selectedNode" size="small" @click="clearSelection">清空</el-button>
        </div>
        <div v-if="selectedNode" class="panel-body">
          <div class="node-type-tag">
            <el-tag :type="getNodeTypeTag(selectedNode.type)">
              {{ getNodeTypeLabel(selectedNode.type) }}
            </el-tag>
          </div>
          <div class="node-name">{{ selectedNode.name }}</div>
          <div class="node-code">{{ selectedNode.code }}</div>
          <div class="node-description">{{ selectedNode.description }}</div>
          <div v-if="selectedNode.params" class="node-params">
            <div class="params-title">参数配置</div>
            <pre>{{ selectedNode.params }}</pre>
          </div>
          <div v-if="selectedNode.severity" class="node-detail-row">
            <span class="detail-label">严重程度：</span>
            <el-tag :type="getSeverityTag(selectedNode.severity)">{{ selectedNode.severity }}</el-tag>
          </div>
          <div v-if="selectedNode.probability" class="node-detail-row">
            <span class="detail-label">概率：</span>
            <span>{{ (selectedNode.probability * 100).toFixed(0) }}%</span>
          </div>
          <div v-if="selectedNode.effectiveness" class="node-detail-row">
            <span class="detail-label">有效性：</span>
            <span>{{ (selectedNode.effectiveness * 100).toFixed(0) }}%</span>
          </div>
          <div v-if="selectedNode.specification" class="node-detail-row">
            <span class="detail-label">规格：</span>
            <span>{{ selectedNode.specification }}</span>
          </div>
          <div v-if="selectedNode.supplier" class="node-detail-row">
            <span class="detail-label">供应商：</span>
            <span>{{ selectedNode.supplier }}</span>
          </div>
          <div v-if="selectedNode.model" class="node-detail-row">
            <span class="detail-label">型号：</span>
            <span>{{ selectedNode.model }}</span>
          </div>
          <div v-if="selectedNode.location" class="node-detail-row">
            <span class="detail-label">位置：</span>
            <span>{{ selectedNode.location }}</span>
          </div>
        </div>
        <div v-else class="panel-empty">
          <el-icon :size="48" class="empty-icon"><Share /></el-icon>
          <p>点击图谱中的节点查看详情</p>
        </div>
      </div>
    </div>

    <div class="legend-bar">
      <div class="legend-title">图例说明</div>
      <div class="legend-items">
        <div class="legend-item">
          <div class="legend-color process"></div>
          <span>工序(Process)</span>
        </div>
        <div class="legend-item">
          <div class="legend-color defect"></div>
          <span>不良(Defect)</span>
        </div>
        <div class="legend-item">
          <div class="legend-color cause"></div>
          <span>原因(Cause)</span>
        </div>
        <div class="legend-item">
          <div class="legend-color solution"></div>
          <span>方案(Solution)</span>
        </div>
        <div class="legend-item">
          <div class="legend-color material"></div>
          <span>物料(Material)</span>
        </div>
        <div class="legend-item">
          <div class="legend-color equipment"></div>
          <span>设备(Equipment)</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>import { ref, onMounted, nextTick } from 'vue';
import { Search, Refresh, SetUp, Warning, TrendCharts, CircleCheck, Box, Cpu, Share } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getGraphData, searchGraphNodes, getGraphStats } from '../../api/knowledge';
const searchKeyword = ref('');
const selectedNode = ref(null);
const stats = ref({
 processCount: 0,
 defectCount: 0,
 causeCount: 0,
 solutionCount: 0,
 materialCount: 0,
 equipmentCount: 0
});
const graphRef = ref(null);
let chartInstance = null;
const nodeTypeColors = {
 process: '#3b82f6',
 defect: '#ef4444',
 cause: '#f59e0b',
 solution: '#10b981',
 material: '#8b5cf6',
 equipment: '#06b6d4'
};
function lightenColor(color, percent) {
 const num = parseInt(color.replace('#', ''), 16);
 const amt = Math.round(2.55 * percent);
 const R = Math.min(255, (num >> 16) + amt);
 const G = Math.min(255, ((num >> 8) & 0x00FF) + amt);
 const B = Math.min(255, (num & 0x0000FF) + amt);
 return '#' + (0x1000000 + R * 0x10000 + G * 0x100 + B).toString(16).slice(1);
}
function getNodeTypeLabel(type) {
 const map = {
 process: '工序',
 defect: '不良现象',
 cause: '缺陷原因',
 solution: '解决方案',
 material: '物料',
 equipment: '设备'
 };
 return map[type] || type;
}
function getNodeTypeTag(type) {
 const map = {
 process: 'primary',
 defect: 'danger',
 cause: 'warning',
 solution: 'success',
 material: 'purple',
 equipment: 'info'
 };
 return map[type] || 'info';
}
function getSeverityTag(severity) {
 const map = {
 '高': 'danger',
 '中': 'warning',
 '低': 'success'
 };
 return map[severity] || 'info';
}
async function loadGraphData() {
 try {
 const res = await getGraphData();
 const data = res?.data || {};
 renderGraph(data.nodes || [], data.edges || []);
 } catch (e) {
 console.error('加载图谱数据失败', e);
 }
}
async function loadStats() {
 try {
 const res = await getGraphStats();
 stats.value = res?.data || {};
 } catch (e) {
 console.error('加载统计数据失败', e);
 }
}
async function searchNodes() {
 if (!searchKeyword.value.trim()) {
 loadGraphData();
 return;
 }
 try {
 const res = await searchGraphNodes(searchKeyword.value);
 const nodes = res?.data || [];
 renderSearchResult(nodes);
 } catch (e) {
 console.error('搜索失败', e);
 }
}
function renderGraph(nodes, edges) {
 if (!chartInstance) {
 chartInstance = echarts.init(graphRef.value);
 }
 const graphNodes = nodes.map(node => ({
 id: node.id,
 name: node.name,
 symbolSize: getSymbolSize(node.type),
 itemStyle: {
 color: {
 type: 'radial',
 x: 0.3,
 y: 0.3,
 r: 0.7,
 colorStops: [
 { offset: 0, color: lightenColor(nodeTypeColors[node.type] || '#64748b', 30) },
 { offset: 1, color: nodeTypeColors[node.type] || '#64748b' }
 ]
 },
 borderColor: '#fff',
 borderWidth: 2,
 shadowBlur: 10,
 shadowColor: nodeTypeColors[node.type] || '#64748b'
 },
 label: {
 show: true,
 position: 'bottom',
 fontSize: 11,
 color: '#1e293b'
 },
 data: node
 }));
 const graphEdges = edges.map(edge => ({
 source: edge.source,
 target: edge.target,
 lineStyle: {
 color: getEdgeColor(edge.type),
 width: edge.weight ? edge.weight * 3 : 2,
 curveness: 0.2
 },
 label: {
 show: true,
 fontSize: 10,
 color: getEdgeColor(edge.type),
 formatter: edge.type
 }
 }));
 const option = {
 backgroundColor: '#f8fafc',
 tooltip: {
 trigger: 'item',
 formatter: function (params) {
 if (params.dataType === 'node') {
 return `<div style="padding: 8px;">
 <div style="font-weight: bold; margin-bottom: 4px;">${params.data.name}</div>
 <div style="font-size: 12px; color: #64748b;">类型: ${getNodeTypeLabel(params.data.data.type)}</div>
 <div style="font-size: 12px; color: #64748b;">编码: ${params.data.data.code}</div>
 </div>`;
 }
 else if (params.dataType === 'edge') {
 return `<div style="padding: 8px;">
 <div style="font-size: 12px;">关系: ${params.data.label.formatter}</div>
 </div>`;
 }
 return '';
 }
 },
 series: [{
 type: 'graph',
 layout: 'force',
 data: graphNodes,
 links: graphEdges,
 roam: true,
 draggable: true,
 force: {
 repulsion: 300,
 gravity: 0.1,
 edgeLength: [80, 150]
 },
 emphasis: {
 focus: 'adjacency',
 lineStyle: {
 width: 4
 }
 }
 }]
 };
 chartInstance.setOption(option);
 chartInstance.off('click');
 chartInstance.on('click', function (params) {
 if (params.dataType === 'node' && params.data) {
 selectedNode.value = params.data.data;
 }
 });
}
function renderSearchResult(nodes) {
 if (!chartInstance) {
 chartInstance = echarts.init(graphRef.value);
 }
 const graphNodes = nodes.map(node => ({
 id: node.type + ':' + node.code,
 name: node.name,
 symbolSize: getSymbolSize(node.type),
 itemStyle: {
 color: {
 type: 'radial',
 x: 0.3,
 y: 0.3,
 r: 0.7,
 colorStops: [
 { offset: 0, color: lightenColor(nodeTypeColors[node.type] || '#64748b', 30) },
 { offset: 1, color: nodeTypeColors[node.type] || '#64748b' }
 ]
 },
 borderColor: '#fff',
 borderWidth: 2,
 shadowBlur: 10,
 shadowColor: nodeTypeColors[node.type] || '#64748b'
 },
 label: {
 show: true,
 position: 'bottom',
 fontSize: 11,
 color: '#1e293b',
 fontWeight: 'bold'
 },
 data: node
 }));
 const option = {
 backgroundColor: '#f8fafc',
 tooltip: {
 trigger: 'item',
 formatter: function (params) {
 return `<div style="padding: 8px;">
 <div style="font-weight: bold; margin-bottom: 4px;">${params.data.name}</div>
 <div style="font-size: 12px; color: #64748b;">类型: ${getNodeTypeLabel(params.data.data.type)}</div>
 </div>`;
 }
 },
 series: [{
 type: 'graph',
 layout: 'force',
 data: graphNodes,
 links: [],
 roam: true,
 draggable: true,
 force: {
 repulsion: 200,
 gravity: 0.2
 },
 emphasis: {
 focus: 'self'
 }
 }]
 };
 chartInstance.setOption(option);
 chartInstance.off('click');
 chartInstance.on('click', function (params) {
 if (params.dataType === 'node' && params.data) {
 selectedNode.value = params.data.data;
 }
 });
}
function getSymbolSize(type) {
 const map = {
 process: 45,
 defect: 35,
 cause: 30,
 solution: 30,
 material: 35,
 equipment: 40
 };
 return map[type] || 35;
}
function getEdgeColor(relationType) {
 const map = {
 USES: '#8b5cf6',
 REQUIRES: '#06b6d4',
 LEADS_TO: '#ef4444',
 CAUSES: '#f59e0b',
 TREATS: '#10b981',
 BELONGS_TO: '#3b82f6'
 };
 return map[relationType] || '#94a3b8';
}
function clearSelection() {
 selectedNode.value = null;
}
onMounted(() => {
 nextTick(() => {
 loadGraphData();
 loadStats();
 window.addEventListener('resize', () => {
 if (chartInstance) {
 chartInstance.resize();
 }
 });
 });
});
</script>

<style scoped>
.knowledge-graph {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.search-input {
  width: 250px;
}

.stats-row {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 140px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.process-icon { background: #3b82f6; }
.defect-icon { background: #ef4444; }
.cause-icon { background: #f59e0b; }
.solution-icon { background: #10b981; }
.material-icon { background: #8b5cf6; }
.equipment-icon { background: #06b6d4; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

.main-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 320px);
}

.graph-container {
  flex: 1;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.graph-canvas {
  width: 100%;
  height: 100%;
}

.detail-panel {
  width: 320px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.panel-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.panel-body {
  padding: 20px;
  overflow-y: auto;
}

.node-type-tag {
  margin-bottom: 10px;
}

.node-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.node-code {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 15px;
}

.node-description {
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
  margin-bottom: 15px;
}

.node-params {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 15px;
}

.params-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}

.node-params pre {
  font-size: 12px;
  color: #334155;
  margin: 0;
  white-space: pre-wrap;
}

.node-detail-row {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #475569;
  margin-bottom: 8px;
}

.detail-label {
  color: #64748b;
  min-width: 70px;
}

.panel-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.empty-icon {
  margin-bottom: 10px;
  opacity: 0.5;
}

.legend-bar {
  margin-top: 20px;
  padding: 15px 20px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.legend-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 10px;
}

.legend-items {
  display: flex;
  gap: 25px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #475569;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.process { background: #3b82f6; }
.legend-color.defect { background: #ef4444; }
.legend-color.cause { background: #f59e0b; }
.legend-color.solution { background: #10b981; }
.legend-color.material { background: #8b5cf6; }
.legend-color.equipment { background: #06b6d4; }
</style>