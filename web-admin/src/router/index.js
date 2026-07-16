import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getMenusByRole } from '../config/roleMenu'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/big-screen',
    name: 'BigScreen',
    component: () => import('../views/big-screen/index.vue'),
    meta: { title: '数字化车间大屏', requiresAuth: false }
  },
  {
    path: '/digital-twin',
    name: 'DigitalTwin',
    component: () => import('../views/digital-twin/index.vue'),
    meta: { title: '3D数字孪生产线', requiresAuth: false }
  },
  {
    path: '/trace-screen',
    name: 'TraceScreen',
    component: () => import('../views/trace-screen/index.vue'),
    meta: { title: '订单全链路追溯', requiresAuth: false }
  },

  {
    path: '/sn-trace',
    name: 'SnTrace',
    component: () => import('../views/sn-trace/index.vue'),
    meta: { title: 'SN全生命周期追溯', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页看板', icon: 'Odometer' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/order/index.vue'),
        meta: { title: '订单管理', icon: 'Document' }
      },
      {
        path: 'purchase',
        name: 'Purchase',
        component: () => import('../views/purchase/index.vue'),
        meta: { title: '采购管理', icon: 'ShoppingCart' }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('../views/supplier/index.vue'),
        meta: { title: '供应商管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'archive-overview',
        name: 'ArchiveOverview',
        component: () => import('../views/archive-overview/index.vue'),
        meta: { title: '客户供应商图谱', icon: 'Share' }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('../views/customer/index.vue'),
        meta: { title: '客户档案', icon: 'User' }
      },
      {
        path: 'material',
        name: 'Material',
        component: () => import('../views/material/index.vue'),
        meta: { title: '物料档案', icon: 'Box' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('../views/product/index.vue'),
        meta: { title: '产品档案', icon: 'Present' }
      },
      {
        path: 'quality-repair-flow',
        name: 'QualityRepairFlow',
        component: () => import('../views/quality-repair-flow/index.vue'),
        meta: { title: '质检维修流程', icon: 'CircleCheck' }
      },
      {
        path: 'order-flow',
        name: 'OrderFlow',
        component: () => import('../views/order-flow/index.vue'),
        meta: { title: '订单采购流程', icon: 'Connection' }
      },
      {
        path: 'equipment',
        name: 'Equipment',
        component: () => import('../views/equipment/index.vue'),
        meta: { title: '设备档案', icon: 'Cpu' }
      },
      {
        path: 'equipment-monitor',
        name: 'EquipmentMonitor',
        component: () => import('../views/equipment-monitor/index.vue'),
        meta: { title: '设备监控', icon: 'DataLine' }
      },
      {
        path: 'equipment-fault',
        name: 'EquipmentFault',
        component: () => import('../views/equipment-fault/index.vue'),
        meta: { title: '设备故障管理', icon: 'Warning' }
      },
      {
        path: 'equipment-maintain',
        name: 'EquipmentMaintain',
        component: () => import('../views/equipment-maintain/index.vue'),
        meta: { title: '设备维保记录', icon: 'Tools' }
      },
      {
        path: 'repair-task',
        name: 'RepairTask',
        component: () => import('../views/repair-task/index.vue'),
        meta: { title: '维修任务', icon: 'FirstAidKit' }
      },
      {
        path: 'warehouse-3d',
        name: 'Warehouse3D',
        component: () => import('../views/warehouse-3d/index.vue'),
        meta: { title: 'VR仓储3D', icon: 'View' }
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('../views/stock/index.vue'),
        meta: { title: '库存查询', icon: 'DataAnalysis' }
      },
      {
        path: 'stock-in',
        name: 'StockIn',
        component: () => import('../views/stock-in/index.vue'),
        meta: { title: '入库记录', icon: 'Bottom' }
      },
      {
        path: 'stock-out',
        name: 'StockOut',
        component: () => import('../views/stock-out/index.vue'),
        meta: { title: '出库记录', icon: 'Top' }
      },
      {
        path: 'stock-take',
        name: 'StockTake',
        component: () => import('../views/stock-take/index.vue'),
        meta: { title: '库存盘点', icon: 'Document' }
      },
      {
        path: 'stock-warning',
        name: 'StockWarning',
        component: () => import('../views/stock-warning/index.vue'),
        meta: { title: '库存预警', icon: 'Bell' }
      },
      {
        path: 'bracelet-3d',
        name: 'Bracelet3D',
        component: () => import('../views/bracelet-3d/index.vue'),
        meta: { title: '3D零件展示', icon: 'Box' }
      },
      {
        path: 'work-order',
        name: 'WorkOrder',
        component: () => import('../views/work-order/index.vue'),
        meta: { title: '生产工单', icon: 'Tickets' }
      },
      {
        path: 'production-plan',
        name: 'ProductionPlan',
        component: () => import('../views/production-plan/index.vue'),
        meta: { title: '生产计划', icon: 'Calendar' }
      },
      {
        path: 'production-progress',
        name: 'ProductionProgress',
        component: () => import('../views/production-progress/index.vue'),
        meta: { title: '生产进度', icon: 'TrendCharts' }
      },
      {
        path: 'process-flow',
        name: 'ProcessFlow',
        component: () => import('../views/process-flow/index.vue'),
        meta: { title: '生产全流程联动', icon: 'Connection' }
      },
      {
        path: 'process-config',
        name: 'ProcessConfig',
        component: () => import('../views/process-config/index.vue'),
        meta: { title: '工艺配置', icon: 'SetUp' }
      },
      {
        path: 'quality-record',
        name: 'QualityRecord',
        component: () => import('../views/quality-record/index.vue'),
        meta: { title: '质检记录', icon: 'CircleCheck' }
      },
      {
        path: 'unqualified',
        name: 'Unqualified',
        component: () => import('../views/unqualified/index.vue'),
        meta: { title: '不合格品管理', icon: 'Warning' }
      },
      {
        path: 'exception-workbench',
        name: 'ExceptionWorkbench',
        component: () => import('../views/exception-workbench/index.vue'),
        meta: { title: '异常处理工作台', icon: 'Warning' }
      },
      {
        path: 'exception',
        name: 'Exception',
        component: () => import('../views/exception/index.vue'),
        meta: { title: '异常处理记录', icon: 'WarningFilled' }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('../views/system/index.vue'),
        meta: { title: '系统管理', icon: 'Tools' }
      },
      {
        path: 'operation-log',
        name: 'OperationLog',
        component: () => import('../views/operation-log/index.vue'),
        meta: { title: '操作日志', icon: 'Notebook' }
      },
      {
        path: 'knowledge-base',
        name: 'KnowledgeBase',
        component: () => import('../views/knowledge-base/index.vue'),
        meta: { title: '知识库管理', icon: 'DataBoard' }
      },
      {
        path: 'knowledge-graph',
        name: 'KnowledgeGraph',
        component: () => import('../views/knowledge-graph/index.vue'),
        meta: { title: '知识图谱', icon: 'Share' }
      },
      {
        path: 'ai-stats',
        name: 'AiStats',
        component: () => import('../views/ai-stats/index.vue'),
        meta: { title: '问答统计', icon: 'DataLine' }
      },
      {
        path: 'order-statistics',
        name: 'OrderStatistics',
        component: () => import('../views/order-statistics/index.vue'),
        meta: { title: '订单统计', icon: 'DataLine' }
      },
      {
        path: 'production-statistics',
        name: 'ProductionStatistics',
        component: () => import('../views/production-statistics/index.vue'),
        meta: { title: '生产统计', icon: 'DataLine' }
      },
      {
        path: 'purchase-statistics',
        name: 'PurchaseStatistics',
        component: () => import('../views/purchase-statistics/index.vue'),
        meta: { title: '采购统计', icon: 'DataLine' }
      },
      {
        path: 'quality-statistics',
        name: 'QualityStatistics',
        component: () => import('../views/quality-statistics/index.vue'),
        meta: { title: '质量统计', icon: 'DataLine' }
      },
      {
        path: 'quality-avatar',
        name: 'QualityAvatar',
        component: () => import('../views/quality-avatar/index.vue'),
        meta: { title: '质检数字人小智', icon: 'Monitor' }
      },
      {
        path: 'stock-statistics',
        name: 'StockStatistics',
        component: () => import('../views/stock-statistics/index.vue'),
        meta: { title: '库存统计', icon: 'DataLine' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else if (to.meta.requiresAuth && token) {
    const role = userStore.userInfo?.role || 'admin'
    const menus = getMenusByRole(role)
    const flatPaths = []
    menus.forEach(menu => {
      if (menu.children && menu.children.length > 0) {
        menu.children.forEach(child => {
          flatPaths.push(child.path)
        })
      } else {
        flatPaths.push(menu.path)
      }
    })
    const path = to.path.substring(1)
    const hasPermission = flatPaths.includes(path)
    
    if (hasPermission || to.path === '/dashboard' || to.path === '/') {
      next()
    } else {
      next('/dashboard')
    }
  } else {
    next()
  }
})

export default router
