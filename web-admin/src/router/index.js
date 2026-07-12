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
    path: '/trace-screen',
    name: 'TraceScreen',
    component: () => import('../views/trace-screen/index.vue'),
    meta: { title: '订单全链路追溯大屏', requiresAuth: false }
  },
  {
    path: '/sop-board',
    name: 'SopBoard',
    component: () => import('../views/sop-board/index.vue'),
    meta: { title: 'SOP电子看板', requiresAuth: false }
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
        path: 'equipment',
        name: 'Equipment',
        component: () => import('../views/equipment/index.vue'),
        meta: { title: '设备档案', icon: 'Cpu' }
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('../views/stock/index.vue'),
        meta: { title: '库存查询', icon: 'Warehouse' }
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
        path: 'work-order',
        name: 'WorkOrder',
        component: () => import('../views/work-order/index.vue'),
        meta: { title: '生产工单', icon: 'Tickets' }
      },
      {
        path: 'production-progress',
        name: 'ProductionProgress',
        component: () => import('../views/production-progress/index.vue'),
        meta: { title: '生产进度', icon: 'TrendCharts' }
      },
      {
        path: 'process-config',
        name: 'ProcessConfig',
        component: () => import('../views/process-config/index.vue'),
        meta: { title: '工艺配置', icon: 'SetUp' }
      },
      {
        path: 'sn-trace',
        name: 'SnTrace',
        component: () => import('../views/sn-trace/index.vue'),
        meta: { title: 'SN全生命周期追溯', icon: 'Key' }
      },
      {
        path: 'sop-board',
        name: 'SopBoard',
        component: () => import('../views/sop-board/index.vue'),
        meta: { title: 'SOP电子看板', icon: 'Monitor' }
      },
      {
        path: 'quality-record',
        name: 'QualityRecord',
        component: () => import('../views/quality-record/index.vue'),
        meta: { title: '质检记录', icon: 'CircleCheck' }
      },
      {
        path: 'exception-workbench',
        name: 'ExceptionWorkbench',
        component: () => import('../views/exception-workbench/index.vue'),
        meta: { title: '异常处理', icon: 'Warning' }
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
