export const roleMenus = {
  admin: [
    { path: 'dashboard', title: '首页看板', icon: 'Odometer' },
    {
      title: '订单采购',
      icon: 'ShoppingCart',
      children: [
        { path: 'order', title: '订单管理', icon: 'Document' },
        { path: 'purchase', title: '采购管理', icon: 'ShoppingBag' },
        { path: 'order-statistics', title: '订单统计', icon: 'DataLine' },
        { path: 'purchase-statistics', title: '采购统计', icon: 'DataLine' }
      ]
    },
    {
      title: '基础档案',
      icon: 'Files',
      children: [
        { path: 'customer', title: '客户档案', icon: 'User' },
        { path: 'supplier', title: '供应商管理', icon: 'OfficeBuilding' },
        { path: 'material', title: '物料档案', icon: 'Box' },
        { path: 'product', title: '产品档案', icon: 'Present' }
      ]
    },
    {
      title: '设备管理',
      icon: 'Cpu',
      children: [
        { path: 'equipment', title: '设备档案', icon: 'Cpu' },
        { path: 'equipment-fault', title: '设备故障管理', icon: 'Warning' },
        { path: 'equipment-maintain', title: '设备维保记录', icon: 'Tools' }
      ]
    },
    {
      title: '仓储管理',
      icon: 'Goods',
      children: [
        { path: 'stock', title: '库存查询', icon: 'DataAnalysis' },
        { path: 'stock-in', title: '入库记录', icon: 'Bottom' },
        { path: 'stock-out', title: '出库记录', icon: 'Top' },
        { path: 'stock-take', title: '库存盘点', icon: 'Document' },
        { path: 'stock-warning', title: '库存预警', icon: 'Bell' },
        { path: 'stock-statistics', title: '库存统计', icon: 'DataLine' }
      ]
    },
    {
      title: '生产管理',
      icon: 'SetUp',
      children: [
        { path: 'work-order', title: '生产工单', icon: 'Tickets' },
        { path: 'production-plan', title: '生产计划', icon: 'Calendar' },
        { path: 'production-progress', title: '生产进度', icon: 'TrendCharts' },
        { path: 'process-config', title: '工艺配置', icon: 'Setting' },
        { path: 'sn-trace', title: 'SN全生命周期追溯', icon: 'Key' },
        { path: 'sop-board', title: 'SOP电子看板', icon: 'Monitor' },
        { path: 'production-statistics', title: '生产统计', icon: 'DataLine' }
      ]
    },
    {
      title: '质量管理',
      icon: 'CircleCheck',
      children: [
        { path: 'quality-record', title: '质检记录', icon: 'CircleCheck' },
        { path: 'unqualified', title: '不合格品管理', icon: 'Warning' },
        { path: 'exception-workbench', title: '异常处理', icon: 'Warning' },
        { path: 'quality-statistics', title: '质量统计', icon: 'DataLine' }
      ]
    },
    {
        title: '系统管理',
        icon: 'Tools',
        children: [
          { path: 'system', title: '系统设置', icon: 'Tools' },
          { path: 'operation-log', title: '操作日志', icon: 'Notebook' }
        ]
      },
      {
        title: 'AI助手',
        icon: 'MagicStick',
        children: [
          { path: 'knowledge-base', title: '知识库管理', icon: 'DataBoard' },
          { path: 'knowledge-graph', title: '知识图谱', icon: 'Share' },
          { path: 'ai-stats', title: '问答统计', icon: 'DataLine' }
        ]
      }
    ],
  order_clerk: [
    { path: 'dashboard', title: '首页看板', icon: 'Odometer' },
    {
      title: '订单管理',
      icon: 'Document',
      children: [
        { path: 'order', title: '订单管理', icon: 'Document' },
        { path: 'customer', title: '客户档案', icon: 'User' },
        { path: 'product', title: '产品档案', icon: 'Present' }
      ]
    },
    {
      title: '生产质量',
      icon: 'TrendCharts',
      children: [
        { path: 'production-progress', title: '生产进度', icon: 'TrendCharts' },
        { path: 'quality-record', title: '质检记录', icon: 'CircleCheck' }
      ]
    }
  ],
  purchase_clerk: [
    { path: 'dashboard', title: '首页看板', icon: 'Odometer' },
    {
      title: '采购管理',
      icon: 'ShoppingCart',
      children: [
        { path: 'purchase', title: '采购管理', icon: 'ShoppingBag' },
        { path: 'supplier', title: '供应商管理', icon: 'OfficeBuilding' },
        { path: 'material', title: '物料档案', icon: 'Box' }
      ]
    },
    {
      title: '仓储管理',
      icon: 'Goods',
      children: [
        { path: 'stock', title: '库存查询', icon: 'DataAnalysis' },
        { path: 'stock-in', title: '入库记录', icon: 'Bottom' }
      ]
    }
  ],
  warehouse_keeper: [
    { path: 'dashboard', title: '首页看板', icon: 'Odometer' },
    {
      title: '仓储管理',
      icon: 'Goods',
      children: [
        { path: 'stock', title: '库存查询', icon: 'DataAnalysis' },
        { path: 'stock-in', title: '入库记录', icon: 'Bottom' },
        { path: 'stock-out', title: '出库记录', icon: 'Top' }
      ]
    },
    {
      title: '基础档案',
      icon: 'Files',
      children: [
        { path: 'material', title: '物料档案', icon: 'Box' },
        { path: 'product', title: '产品档案', icon: 'Present' }
      ]
    },
    { path: 'purchase', title: '采购管理', icon: 'ShoppingCart' }
  ],
  production_manager: [
    { path: 'dashboard', title: '首页看板', icon: 'Odometer' },
    {
      title: '生产管理',
      icon: 'SetUp',
      children: [
        { path: 'work-order', title: '生产工单', icon: 'Tickets' },
        { path: 'production-progress', title: '生产进度', icon: 'TrendCharts' },
        { path: 'process-config', title: '工艺配置', icon: 'Setting' },
        { path: 'sn-trace', title: 'SN全生命周期追溯', icon: 'Key' },
        { path: 'sop-board', title: 'SOP电子看板', icon: 'Monitor' },
        { path: 'equipment', title: '设备档案', icon: 'Cpu' }
      ]
    },
    {
      title: '质量管理',
      icon: 'CircleCheck',
      children: [
        { path: 'quality-record', title: '质检记录', icon: 'CircleCheck' },
        { path: 'exception-workbench', title: '异常处理', icon: 'Warning' }
      ]
    },
    {
      title: '基础档案',
      icon: 'Files',
      children: [
        { path: 'order', title: '订单管理', icon: 'Document' },
        { path: 'product', title: '产品档案', icon: 'Present' }
      ]
    }
  ],
  quality_inspector: [
    { path: 'dashboard', title: '首页看板', icon: 'Odometer' },
    {
      title: '质量管理',
      icon: 'CircleCheck',
      children: [
        { path: 'quality-record', title: '质检记录', icon: 'CircleCheck' },
        { path: 'exception-workbench', title: '异常处理', icon: 'Warning' }
      ]
    },
    {
      title: '生产相关',
      icon: 'TrendCharts',
      children: [
        { path: 'production-progress', title: '生产进度', icon: 'TrendCharts' },
        { path: 'work-order', title: '生产工单', icon: 'Tickets' },
        { path: 'product', title: '产品档案', icon: 'Present' }
      ]
    },
    {
      title: '仓储记录',
      icon: 'Warehouse',
      children: [
        { path: 'stock-in', title: '入库记录', icon: 'Bottom' },
        { path: 'stock-out', title: '出库记录', icon: 'Top' }
      ]
    }
  ]
}

export const roleNames = {
  admin: '超级管理员',
  order_clerk: '订单员',
  purchase_clerk: '采购员',
  warehouse_keeper: '仓库管理员',
  production_manager: '生产主管',
  quality_inspector: '质检员'
}

export const mockUsers = [
  { username: 'admin', password: '123456', role: 'admin', realName: '超级管理员' },
  { username: 'order_clerk', password: '123456', role: 'order_clerk', realName: '张订单' },
  { username: 'purchase_clerk', password: '123456', role: 'purchase_clerk', realName: '李采购' },
  { username: 'warehouse_keeper', password: '123456', role: 'warehouse_keeper', realName: '王仓库' },
  { username: 'production_manager', password: '123456', role: 'production_manager', realName: '赵生产' },
  { username: 'quality_inspector', password: '123456', role: 'quality_inspector', realName: '孙质检' }
]

export function getMenusByRole(role) {
  return roleMenus[role] || roleMenus.admin
}
