import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/order/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'purchase',
        name: 'Purchase',
        component: () => import('../views/purchase/index.vue'),
        meta: { title: '采购管理' }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('../views/supplier/index.vue'),
        meta: { title: '供应商管理' }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('../views/customer/index.vue'),
        meta: { title: '客户档案' }
      },
      {
        path: 'material',
        name: 'Material',
        component: () => import('../views/material/index.vue'),
        meta: { title: '物料档案' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('../views/product/index.vue'),
        meta: { title: '产品档案' }
      },
      {
        path: 'equipment',
        name: 'Equipment',
        component: () => import('../views/equipment/index.vue'),
        meta: { title: '设备档案' }
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('../views/stock/index.vue'),
        meta: { title: '库存查询' }
      },
      {
        path: 'stock-in',
        name: 'StockIn',
        component: () => import('../views/stock-in/index.vue'),
        meta: { title: '入库记录' }
      },
      {
        path: 'stock-out',
        name: 'StockOut',
        component: () => import('../views/stock-out/index.vue'),
        meta: { title: '出库记录' }
      },
      {
        path: 'stock-flow',
        name: 'StockFlow',
        component: () => import('../views/stock-flow/index.vue'),
        meta: { title: '库存流水' }
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
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
