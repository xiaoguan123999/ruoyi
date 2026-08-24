<template>
  <div class="app-container ops-home">
    <!-- 顶部欢迎 -->
    <div class="welcome-bar">
      <div class="welcome-left">
        <h2>{{ greeting }}，{{ nickName }}</h2>
        <p>XFZL 运营工作台 · 以下为模拟数据，便于先确认页面布局</p>
      </div>
      <div class="welcome-right">
        <el-tag type="warning" effect="plain">模拟数据</el-tag>
        <span class="update-time">数据更新：{{ mockUpdatedAt }}</span>
      </div>
    </div>

    <!-- 核心指标 -->
    <el-row :gutter="16" class="stat-row">
      <el-col v-for="item in statCards" :key="item.key" :xs="12" :sm="12" :md="8" :lg="4">
        <div class="stat-card" :class="{ clickable: !!item.path }" :style="{ '--accent': item.color }" @click="go(item.path)">
          <div class="stat-top">
            <span class="stat-label">{{ item.label }}</span>
            <el-icon :size="18"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-foot">
            <span :class="item.trend >= 0 ? 'up' : 'down'">
              {{ item.trend >= 0 ? '+' : '' }}{{ item.trend }}%
            </span>
            <span class="muted">较昨日</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 待办审核 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>待办审核</span>
              <el-badge :value="todoTotal" :max="99" type="danger" />
            </div>
          </template>
          <div
            v-for="todo in todoList"
            :key="todo.key"
            class="todo-item"
            @click="go(todo.path)"
          >
            <div class="todo-icon" :style="{ background: todo.bg }">
              <el-icon :size="18" :color="todo.color"><component :is="todo.icon" /></el-icon>
            </div>
            <div class="todo-body">
              <div class="todo-title">{{ todo.title }}</div>
              <div class="todo-desc">{{ todo.desc }}</div>
            </div>
            <el-tag :type="todo.tagType" effect="light" round>{{ todo.count }} 待处理</el-tag>
          </div>
          <el-empty v-if="!todoList.length" description="暂无待办权限" :image-size="64" />
        </el-card>
      </el-col>

      <!-- 近7日趋势 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>近 7 日趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="renderTrendChart">
                <el-radio-button value="register">注册</el-radio-button>
                <el-radio-button value="order">认购</el-radio-button>
                <el-radio-button value="fund">资金</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="trend-chart" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="bottom-row">
      <!-- 运营快捷入口 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>运营快捷入口</span>
            </div>
          </template>
          <el-row v-if="shortcuts.length" :gutter="12">
            <el-col v-for="entry in shortcuts" :key="entry.path" :span="8">
              <div class="shortcut" @click="go(entry.path)">
                <div class="shortcut-icon" :style="{ background: entry.bg, color: entry.color }">
                  <svg-icon v-if="entry.menuIcon" :icon-class="entry.menuIcon" class="menu-svg" />
                  <el-icon v-else :size="20"><component :is="entry.icon" /></el-icon>
                </div>
                <div class="shortcut-name">{{ entry.name }}</div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else description="当前账号暂无运营菜单权限" :image-size="72" />
        </el-card>
      </el-col>

      <!-- 最近动态 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>最近业务动态</span>
              <el-button v-if="walletLogPath" link type="primary" @click="go(walletLogPath)">查看流水</el-button>
            </div>
          </template>
          <el-table :data="recentActivities" size="small" stripe style="width: 100%">
            <el-table-column prop="time" label="时间" width="160" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.tagType" size="small" effect="plain">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="user" label="会员" width="120" show-overflow-tooltip />
            <el-table-column prop="content" label="摘要" min-width="180" show-overflow-tooltip />
            <el-table-column prop="amount" label="金额" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.amountClass">{{ row.amount }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="Index">
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import {
  User,
  ShoppingCart,
  Wallet,
  CreditCard,
  Coin,
  Warning,
  Document,
  Picture,
  ChatDotRound,
  Headset,
  DataBoard,
  Stamp,
  Present
} from '@element-plus/icons-vue'
import { getNormalPath } from '@/utils/ruoyi'
import { isHttp } from '@/utils/validate'
import useUserStore from '@/store/modules/user'
import usePermissionStore from '@/store/modules/permission'

interface FlatMenu {
  title: string
  path: string
  icon: string
}

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const nickName = computed(() => userStore.nickName || '运营同学')
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const mockUpdatedAt = '2026-08-24 18:30'

/** 从动态菜单树扁平化出可跳转页面（路径随上级目录变化自动适配） */
function flattenMenus(routes: any[], basePath = ''): FlatMenu[] {
  const list: FlatMenu[] = []
  for (const route of routes || []) {
    if (route.hidden) continue
    const raw = route.path || ''
    const segment = raw.startsWith('/') || isHttp(raw) ? raw : `/${raw}`
    const fullPath = isHttp(raw) ? raw : getNormalPath(basePath + segment)
    const title = route.meta?.title as string | undefined
    const hasChildren = Array.isArray(route.children) && route.children.length > 0

    if (title && !hasChildren && !isHttp(fullPath) && route.redirect !== 'noRedirect') {
      list.push({
        title,
        path: fullPath,
        icon: (route.meta?.icon as string) || ''
      })
    }
    if (hasChildren) {
      list.push(...flattenMenus(route.children, fullPath))
    }
  }
  return list
}

const menuMap = computed(() => {
  const map = new Map<string, FlatMenu>()
  flattenMenus(permissionStore.defaultRoutes).forEach((item) => {
    if (!map.has(item.title)) map.set(item.title, item)
  })
  return map
})

function resolveMenuPath(...titles: string[]): string {
  for (const title of titles) {
    const hit = menuMap.value.get(title)
    if (hit?.path) return hit.path
  }
  return ''
}

function resolveMenuIcon(...titles: string[]): string {
  for (const title of titles) {
    const hit = menuMap.value.get(title)
    if (hit?.icon && hit.icon !== '#') return hit.icon
  }
  return ''
}

/** 核心指标（模拟；跳转路径取自动态菜单） */
const statCards = computed(() => [
  { key: 'member', label: '会员总数', value: '12,846', trend: 2.4, color: '#409EFF', icon: User, path: resolveMenuPath('会员管理') },
  { key: 'todayMember', label: '今日新增', value: '86', trend: 12.1, color: '#67C23A', icon: User, path: resolveMenuPath('会员管理') },
  { key: 'order', label: '今日认购单', value: '142', trend: -3.2, color: '#E6A23C', icon: ShoppingCart, path: resolveMenuPath('认购订单') },
  { key: 'recharge', label: '今日充值(元)', value: '328,600', trend: 8.6, color: '#F56C6C', icon: Wallet, path: resolveMenuPath('充值审核') },
  { key: 'withdraw', label: '今日提现(元)', value: '96,400', trend: 1.5, color: '#909399', icon: CreditCard, path: resolveMenuPath('提现审核') },
  { key: 'commission', label: '今日分佣(元)', value: '18,230', trend: 5.0, color: '#9B59B6', icon: Coin, path: resolveMenuPath('分佣记录') }
])

/** 待办审核（模拟；仅展示当前账号有权限的菜单） */
const todoList = computed(() => {
  const defs = [
    {
      key: 'recharge',
      title: '充值审核',
      desc: '含 CNY / USDT 待审申请',
      count: 12,
      menuTitles: ['充值审核'],
      icon: Wallet,
      color: '#F56C6C',
      bg: 'rgba(245, 108, 108, 0.12)',
      tagType: 'danger' as const
    },
    {
      key: 'withdraw',
      title: '提现审核',
      desc: '请先线下打款再确认',
      count: 7,
      menuTitles: ['提现审核'],
      icon: CreditCard,
      color: '#E6A23C',
      bg: 'rgba(230, 162, 60, 0.12)',
      tagType: 'warning' as const
    },
    {
      key: 'reward',
      title: '等级奖励发放',
      desc: '领航 / 星域待发放',
      count: 4,
      menuTitles: ['等级奖励发放'],
      icon: Present,
      color: '#409EFF',
      bg: 'rgba(64, 158, 255, 0.12)',
      tagType: 'primary' as const
    },
    {
      key: 'risk',
      title: '异常提醒',
      desc: '大额提现 / 重复充值',
      count: 2,
      menuTitles: ['资金流水'],
      icon: Warning,
      color: '#909399',
      bg: 'rgba(144, 147, 153, 0.12)',
      tagType: 'info' as const
    }
  ]
  return defs
    .map((item) => ({ ...item, path: resolveMenuPath(...item.menuTitles) }))
    .filter((item) => !!item.path)
})

const todoTotal = computed(() => todoList.value.reduce((s, i) => s + i.count, 0))

/** 运营快捷入口：按菜单标题匹配动态路由，无权限则不显示 */
const shortcutDefs = [
  { name: '新闻资讯', menuTitles: ['新闻资讯'], icon: Document, color: '#409EFF', bg: 'rgba(64,158,255,.1)' },
  { name: '视频轮播', menuTitles: ['视频轮播'], icon: Picture, color: '#67C23A', bg: 'rgba(103,194,58,.1)' },
  { name: '官方群聊', menuTitles: ['官方群聊'], icon: ChatDotRound, color: '#E6A23C', bg: 'rgba(230,162,60,.1)' },
  { name: '客服中心', menuTitles: ['客服中心'], icon: Headset, color: '#F56C6C', bg: 'rgba(245,108,108,.1)' },
  { name: '运行概览', menuTitles: ['运行概览'], icon: DataBoard, color: '#9B59B6', bg: 'rgba(155,89,182,.1)' },
  { name: '关于我们', menuTitles: ['关于我们'], icon: Stamp, color: '#607D8B', bg: 'rgba(96,125,139,.1)' }
]

const shortcuts = computed(() =>
  shortcutDefs
    .map((item) => ({
      ...item,
      path: resolveMenuPath(...item.menuTitles),
      menuIcon: resolveMenuIcon(...item.menuTitles)
    }))
    .filter((item) => !!item.path)
)

const walletLogPath = computed(() => resolveMenuPath('资金流水'))

/** 最近动态（模拟） */
const recentActivities = [
  { time: '2026-08-24 18:12', type: '充值', tagType: 'success', user: '会员 U8821', content: '提交 USDT 充值申请', amount: '+5,000 USDT', amountClass: 'amt-plus' },
  { time: '2026-08-24 17:58', type: '认购', tagType: 'warning', user: '会员 U1034', content: '认购「星耀计划 A」', amount: '¥12,000', amountClass: '' },
  { time: '2026-08-24 17:40', type: '提现', tagType: 'danger', user: '会员 U5510', content: '申请银行卡提现', amount: '-¥8,600', amountClass: 'amt-minus' },
  { time: '2026-08-24 16:22', type: '注册', tagType: 'info', user: '会员 U9902', content: '邀请码注册成功', amount: '—', amountClass: 'muted' },
  { time: '2026-08-24 15:05', type: '分佣', tagType: 'primary', user: '会员 U2201', content: '直推认购分佣入账', amount: '+¥360', amountClass: 'amt-plus' },
  { time: '2026-08-24 14:18', type: '签到', tagType: '', user: '会员 U7740', content: '连续签到第 7 天抽奖', amount: '+¥20', amountClass: 'amt-plus' }
]

/** 趋势图模拟数据 */
const trendDates = ['08-18', '08-19', '08-20', '08-21', '08-22', '08-23', '08-24']
const trendSeries: Record<string, { name: string; data: number[]; color: string }[]> = {
  register: [
    { name: '新增注册', data: [62, 71, 58, 80, 76, 90, 86], color: '#409EFF' }
  ],
  order: [
    { name: '认购笔数', data: [98, 112, 105, 130, 121, 148, 142], color: '#E6A23C' },
    { name: '认购人数', data: [70, 82, 75, 95, 88, 102, 96], color: '#67C23A' }
  ],
  fund: [
    { name: '充值金额(千元)', data: [210, 245, 198, 280, 260, 310, 328], color: '#F56C6C' },
    { name: '提现金额(千元)', data: [80, 95, 72, 110, 88, 102, 96], color: '#909399' }
  ]
}

const trendType = ref<'register' | 'order' | 'fund'>('fund')
const trendChartRef = ref<HTMLElement | null>(null)
let chart: ECharts | null = null

function renderTrendChart(): void {
  if (!trendChartRef.value) return
  if (!chart) {
    chart = echarts.init(trendChartRef.value)
  }
  const series = trendSeries[trendType.value]
  chart.setOption({
    color: series.map((s) => s.color),
    tooltip: { trigger: 'axis' },
    legend: { data: series.map((s) => s.name), bottom: 0 },
    grid: { left: 40, right: 20, top: 24, bottom: 36 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendDates,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#EBEEF5' } },
      axisLabel: { color: '#909399' }
    },
    series: series.map((s) => ({
      name: s.name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { opacity: 0.08 },
      data: s.data
    }))
  }, true)
}

function go(path?: string): void {
  if (!path) {
    ElMessage.warning('当前账号无对应菜单权限，或菜单尚未配置')
    return
  }
  router.push(path).catch(() => {
    ElMessage.warning('菜单路径跳转失败，请从侧边栏进入')
  })
}

function onResize(): void {
  chart?.resize()
}

onMounted(() => {
  nextTick(() => {
    renderTrendChart()
    window.addEventListener('resize', onResize)
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped lang="scss">
.ops-home {
  padding-bottom: 12px;
}

.welcome-bar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  h2 {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  p {
    margin: 0;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .welcome-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .update-time {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}

.stat-row {
  margin-bottom: 8px;
}

.stat-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 16px;
  transition: box-shadow 0.2s, transform 0.2s;
  border-top: 3px solid var(--accent);

  &.clickable {
    cursor: pointer;

    &:hover {
      box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
      transform: translateY(-1px);
    }
  }

  .stat-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .stat-value {
    margin-top: 10px;
    font-size: 24px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: 0.5px;
  }

  .stat-foot {
    margin-top: 8px;
    font-size: 12px;
    display: flex;
    gap: 6px;
    align-items: center;

    .up { color: #67c23a; }
    .down { color: #f56c6c; }
    .muted { color: var(--el-text-color-placeholder); }
  }
}

.panel-card {
  margin-bottom: 16px;
  border-radius: 10px;

  :deep(.el-card__header) {
    padding: 14px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-card__body) {
    padding: 12px 16px 16px;
  }
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  & + .todo-item {
    border-top: 1px dashed var(--el-border-color-lighter);
  }

  .todo-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .todo-body {
    flex: 1;
    min-width: 0;
  }

  .todo-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .todo-desc {
    margin-top: 2px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.trend-chart {
  height: 280px;
  width: 100%;
}

.shortcut {
  text-align: center;
  padding: 14px 8px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background 0.15s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  .shortcut-icon {
    width: 44px;
    height: 44px;
    margin: 0 auto 8px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .shortcut-name {
    font-size: 13px;
    color: var(--el-text-color-regular);
  }

  .menu-svg {
    font-size: 20px;
  }
}

.amt-plus { color: #67c23a; font-weight: 600; }
.amt-minus { color: #f56c6c; font-weight: 600; }
.muted { color: var(--el-text-color-placeholder); }

.bottom-row {
  margin-top: 0;
}
</style>
