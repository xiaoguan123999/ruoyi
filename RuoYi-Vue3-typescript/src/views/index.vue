<template>
  <div class="app-container ops-home" v-loading="loading">
    <div class="welcome-bar">
      <div class="welcome-left">
        <h2>{{ greeting }}，{{ nickName }}</h2>
        <p>仪表盘看待办与趋势；经营日报按所选自然日统计「当日」与「总计」。金额按币种分开，不把 CNY 与 USDT 相加。</p>
      </div>
      <div class="welcome-right">
        <el-date-picker
          v-model="queryDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="统计日期"
          :clearable="false"
          style="width: 160px"
          @change="reload"
        />
        <el-button type="primary" icon="Search" @click="reload">查询</el-button>
        <span class="update-time">数据日期：{{ stats.date || queryDate }}</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="home-tabs" @tab-change="onTabChange">
      <el-tab-pane label="仪表盘" name="dashboard">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="10">
            <el-card shadow="never" class="panel-card">
              <template #header>
                <div class="panel-header">
                  <span>待办事项</span>
                  <el-badge :value="todoTotal" :max="99" type="danger" />
                </div>
              </template>
              <div
                v-for="todo in todoList"
                :key="todo.key"
                class="todo-item"
                @click="go(todo.path, todo.query)"
              >
                <div class="todo-icon" :style="{ background: todo.bg }">
                  <el-icon :size="18" :color="todo.color"><component :is="todo.icon" /></el-icon>
                </div>
                <div class="todo-body">
                  <div class="todo-title">{{ todo.title }}</div>
                  <div class="todo-desc">{{ todo.desc }}</div>
                </div>
                <div class="todo-right">
                  <template v-if="todo.amountText">
                    <div class="todo-amount">{{ todo.amountText }}</div>
                    <div v-if="todo.amountSub" class="todo-amount-sub">{{ todo.amountSub }}</div>
                  </template>
                  <el-tag v-else :type="todo.tagType" effect="light" round>{{ fmtInt(todo.count) }} {{ todo.unit }}</el-tag>
                </div>
              </div>
              <el-empty v-if="!todoList.length" description="暂无待办权限" :image-size="64" />
            </el-card>
          </el-col>

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
                <el-table-column prop="amount" label="金额" width="130" align="right">
                  <template #default="{ row }">
                    <span :class="row.amountClass">{{ row.amount }}</span>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!recentActivities.length" description="暂无流水" :image-size="64" />
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="经营日报" name="daily" lazy>
        <el-alert type="info" :closable="false" show-icon class="hint-alert">
          充值/提现成功按审核通过时间计入当日。拉单即认购订单。提现拆分按申请备注页签：产品收益、助力值/推广收益；未带页签的成功提现只计入总额。今日实名按会员资料最后更新日估算。
        </el-alert>

        <div class="section-title">用户与资金</div>
        <el-row :gutter="14" class="metric-row">
          <el-col v-for="card in userFundCards" :key="card.key" :xs="12" :sm="8" :md="6" :lg="6">
            <div class="metric-card" :class="{ clickable: !!card.path }" @click="card.path && go(card.path)">
              <div class="metric-head">
                <span class="metric-title">{{ card.title }}</span>
                <span class="metric-tag">今日</span>
              </div>
              <div v-if="card.dualMoney" class="metric-value dual">
                <div>{{ card.today }}</div>
                <div>{{ card.todayUsdt }}</div>
              </div>
              <div v-else class="metric-value">
                {{ card.today }}
                <span v-if="card.unit" class="unit">{{ card.unit }}</span>
              </div>
              <div v-if="card.sub" class="metric-sub">{{ card.sub }}</div>
              <div class="metric-foot">
                <span>总计</span>
                <span v-if="card.dualMoney" class="foot-dual">
                  <span>{{ card.total }}</span>
                  <span>{{ card.totalUsdt }}</span>
                </span>
                <span v-else>{{ card.total }}</span>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="section-title">发放与持仓</div>
        <el-row :gutter="14" class="metric-row">
          <el-col v-for="card in extraCards" :key="card.key" :xs="12" :sm="8" :md="6" :lg="6">
            <div class="metric-card tint" :class="{ clickable: !!card.path }" @click="card.path && go(card.path)">
              <div class="metric-head">
                <span class="metric-title">{{ card.title }}</span>
                <span class="metric-tag" :class="{ stock: card.stock }">{{ card.tag || '今日' }}</span>
              </div>
              <div v-if="card.dualMoney" class="metric-value dual">
                <div>{{ card.today }}</div>
                <div>{{ card.todayUsdt }}</div>
              </div>
              <div v-else class="metric-value">
                {{ card.today }}
                <span v-if="card.unit" class="unit">{{ card.unit }}</span>
              </div>
              <div v-if="card.sub" class="metric-sub">{{ card.sub }}</div>
              <div class="metric-foot">
                <span>{{ card.footLabel || '总计' }}</span>
                <span v-if="card.dualMoney" class="foot-dual">
                  <span>{{ card.total }}</span>
                  <span>{{ card.totalUsdt }}</span>
                </span>
                <span v-else>{{ card.total }}</span>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="section-title">钱包余额</div>
        <el-row :gutter="14" class="metric-row">
          <el-col v-for="card in walletCards" :key="card.key" :xs="12" :sm="8" :md="6" :lg="6">
            <div class="metric-card">
              <div class="metric-head">
                <span class="metric-title">{{ card.title }}</span>
                <span class="metric-tag stock">累计</span>
              </div>
              <div class="metric-value">{{ card.today }}<span class="unit">{{ card.unit }}</span></div>
              <div class="metric-sub">{{ card.sub }}</div>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts" name="Index">
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import {
  Wallet,
  CreditCard,
  Document,
  Picture,
  ChatDotRound,
  Headset,
  DataBoard,
  Stamp,
  User
} from '@element-plus/icons-vue'
import { getNormalPath, parseTime } from '@/utils/ruoyi'
import { isHttp } from '@/utils/validate'
import auth from '@/plugins/auth'
import useUserStore from '@/store/modules/user'
import usePermissionStore from '@/store/modules/permission'
import { getDashboardStats, getDashboardTrend } from '@/api/biz'

interface FlatMenu {
  title: string
  path: string
  icon: string
}

interface MetricCard {
  key: string
  title: string
  today: string
  total: string
  unit?: string
  sub?: string
  path?: string
  tag?: string
  stock?: boolean
  footLabel?: string
  /** CNY / USDT 同级双行展示 */
  dualMoney?: boolean
  todayUsdt?: string
  totalUsdt?: string
}

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const activeTab = ref('dashboard')

const nickName = computed(() => userStore.nickName || '运营同学')
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

function todayText() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const queryDate = ref(todayText())
const loading = ref(false)
const stats = ref<any>({})
const trend = ref<any>({})

const emptyCount = { today: 0, total: 0 }
const emptyMoney = { todayCny: 0, todayUsdt: 0, totalCny: 0, totalUsdt: 0, todayCount: 0, totalCount: 0 }

function n(v: any) {
  const num = Number(v)
  return Number.isFinite(num) ? num : 0
}

function fmtInt(v: any) {
  return n(v).toLocaleString('zh-CN')
}

function fmtMoney(v: any) {
  return n(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function countOf(key: string) {
  return stats.value?.[key] || emptyCount
}

function moneyOf(key: string) {
  return stats.value?.[key] || emptyMoney
}

function moneyToday(m: any) {
  return `${fmtMoney(m.todayCny)} CNY`
}

function moneyTodayUsdt(m: any) {
  return `${fmtMoney(m.todayUsdt)} USDT`
}

function moneySub(m: any, countLabel?: string) {
  const usdt = `${fmtMoney(m.todayUsdt)} USDT`
  if (!countLabel) return usdt
  return `${countLabel} ${fmtInt(m.todayCount)} · ${usdt}`
}

function moneyCountHint(m: any, countLabel: string) {
  const totalHint = m.totalCount ? ` · 累计 ${fmtInt(m.totalCount)} 笔` : ''
  return `${countLabel} ${fmtInt(m.todayCount)}${totalHint}`
}

function moneyTotal(m: any) {
  const extra = m.totalCount ? ` / ${fmtInt(m.totalCount)} 笔` : ''
  return `${fmtMoney(m.totalCny)} CNY · ${fmtMoney(m.totalUsdt)} USDT${extra}`
}

function moneyTotalCny(m: any) {
  return `${fmtMoney(m.totalCny)} CNY`
}

function moneyTotalUsdt(m: any) {
  return `${fmtMoney(m.totalUsdt)} USDT`
}

function dualMoneyCard(
  key: string,
  title: string,
  m: any,
  countLabel: string,
  path?: string
): MetricCard {
  return {
    key,
    title,
    dualMoney: true,
    today: moneyToday(m),
    todayUsdt: moneyTodayUsdt(m),
    total: moneyTotalCny(m),
    totalUsdt: moneyTotalUsdt(m),
    sub: moneyCountHint(m, countLabel),
    path
  }
}

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
      list.push({ title, path: fullPath, icon: (route.meta?.icon as string) || '' })
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

function canShow(path: string, perms: string[]): boolean {
  void userStore.permissions
  return !!path && auth.hasPermiOr(perms)
}

function menuPathIfAllowed(titles: string[], perms: string[]): string {
  const path = resolveMenuPath(...titles)
  return canShow(path, perms) ? path : ''
}

const memberPath = computed(() => menuPathIfAllowed(['会员管理'], ['biz:member:list']))
const checkinPath = computed(() => menuPathIfAllowed(['签到记录'], ['biz:checkin:list']))
const rechargePath = computed(() => menuPathIfAllowed(['充值审核'], ['biz:recharge:list', 'biz:recharge:query']))
const orderPath = computed(() => menuPathIfAllowed(['认购订单'], ['biz:order:list']))
const withdrawPath = computed(() => menuPathIfAllowed(['提现审核'], ['biz:withdraw:list', 'biz:withdraw:query']))
const commissionPath = computed(() => menuPathIfAllowed(['分佣记录'], ['biz:commission:list', 'biz:commission:query']))
const walletLogPath = computed(() => {
  const path = resolveMenuPath('资金流水')
  return canShow(path, ['biz:walletLog:list', 'biz:walletLog:query']) ? path : ''
})

const userFundCards = computed<MetricCard[]>(() => {
  const register = countOf('register')
  const kyc = countOf('kyc')
  const checkin = countOf('checkin')
  const recharge = moneyOf('recharge')
  const rechargeUsers = countOf('rechargeUsers')
  const rechargeOrders = countOf('rechargeOrders')
  const subscribeUsers = countOf('subscribeUsers')
  const subscribeNew = countOf('subscribeNewUsers')
  const pullCount = countOf('pullCount')
  const pullAmount = moneyOf('pullAmount')
  const wdProduct = moneyOf('withdrawProduct')
  const wdPromo = moneyOf('withdrawPromo')
  const wdAssist = moneyOf('withdrawAssist')
  const wdTotal = moneyOf('withdrawTotal')
  const wdCount = countOf('withdrawCount')
  return [
    { key: 'register', title: '注册人数', today: fmtInt(register.today), total: fmtInt(register.total), unit: '人', path: memberPath.value },
    { key: 'kyc', title: '实名人数', today: fmtInt(kyc.today), total: fmtInt(kyc.total), unit: '人', path: memberPath.value },
    { key: 'checkin', title: 'App 签到', today: fmtInt(checkin.today), total: fmtInt(checkin.total), unit: '次', path: checkinPath.value },
    dualMoneyCard('recharge', '充值', recharge, '成功单数', rechargePath.value),
    { key: 'rechargeUsers', title: '充值用户数', today: fmtInt(rechargeUsers.today), total: fmtInt(rechargeUsers.total), unit: '人', path: rechargePath.value },
    { key: 'rechargeOrders', title: '充值成功单数', today: fmtInt(rechargeOrders.today), total: fmtInt(rechargeOrders.total), unit: '单', path: rechargePath.value },
    { key: 'pullCount', title: '拉单数量', today: fmtInt(pullCount.today), total: fmtInt(pullCount.total), unit: '笔', path: orderPath.value },
    dualMoneyCard('pullAmount', '拉单金额', pullAmount, '认购笔数', orderPath.value),
    { key: 'subscribeUsers', title: '认购用户数', today: fmtInt(subscribeUsers.today), total: fmtInt(subscribeUsers.total), unit: '人', path: orderPath.value },
    { key: 'subscribeNew', title: '当日新增认购用户', today: fmtInt(subscribeNew.today), total: fmtInt(subscribeNew.total), unit: '人', path: orderPath.value },
    dualMoneyCard('wdProduct', '提现成功-产品收益', wdProduct, '成功笔数', withdrawPath.value),
    dualMoneyCard('wdPromo', '提现成功-推广收益', wdPromo, '成功笔数', withdrawPath.value),
    dualMoneyCard('wdAssist', '提现成功-助力值', wdAssist, '成功笔数', withdrawPath.value),
    dualMoneyCard('wdTotal', '提现成功-总额', wdTotal, '成功笔数', withdrawPath.value),
    { key: 'wdCount', title: '提现成功-总数量', today: fmtInt(wdCount.today), total: fmtInt(wdCount.total), unit: '笔', path: withdrawPath.value }
  ]
})

const extraCards = computed<MetricCard[]>(() => {
  const reward = moneyOf('checkinReward')
  const rebate = moneyOf('rebate')
  const commission = moneyOf('commission')
  const invite = moneyOf('invite')
  const apply = moneyOf('withdrawApply')
  const holdingOrders = countOf('holdingOrders')
  const holdingUsers = countOf('holdingUsers')
  return [
    { key: 'checkinReward', title: '签到奖励发放', today: moneyToday(reward), total: moneyTotal(reward), sub: moneySub(reward, '发放人次'), path: checkinPath.value },
    { key: 'rebate', title: '产品日返发放', today: moneyToday(rebate), total: moneyTotal(rebate), sub: moneySub(rebate, '发放笔数'), path: orderPath.value },
    { key: 'commission', title: '团队分佣发放', today: moneyToday(commission), total: moneyTotal(commission), sub: moneySub(commission, '发放笔数'), path: commissionPath.value },
    { key: 'invite', title: '邀请奖励发放', today: moneyToday(invite), total: moneyTotal(invite), sub: `${fmtMoney(invite.todayUsdt)} USDT`, path: commissionPath.value },
    { key: 'wdApply', title: '提现申请', today: moneyToday(apply), total: `${fmtInt(apply.todayCount)} 笔`, sub: moneySub(apply), path: withdrawPath.value, footLabel: '申请笔数' },
    { key: 'holdingOrders', title: '持仓订单', today: fmtInt(holdingOrders.today), total: fmtInt(holdingUsers.today) + ' 人', unit: '笔', tag: '当前', stock: true, footLabel: '持仓用户', path: orderPath.value },
    { key: 'holdingUsers', title: '持仓用户', today: fmtInt(holdingUsers.today), total: fmtInt(holdingOrders.today) + ' 笔', unit: '人', tag: '当前', stock: true, footLabel: '持仓订单', path: orderPath.value }
  ]
})

const walletCards = computed<MetricCard[]>(() => {
  const available = moneyOf('walletAvailable')
  const frozen = moneyOf('walletFrozen')
  return [
    { key: 'cnyAvail', title: '可用余额 · CNY', today: fmtMoney(available.totalCny), total: '', unit: '元', sub: '会员钱包合计' },
    { key: 'usdtAvail', title: '可用余额 · USDT', today: fmtMoney(available.totalUsdt), total: '', unit: 'USDT', sub: '会员钱包合计' },
    { key: 'cnyFrozen', title: '冻结余额 · CNY', today: fmtMoney(frozen.totalCny), total: '', unit: '元', sub: '含待审提现冻结' },
    { key: 'usdtFrozen', title: '冻结余额 · USDT', today: fmtMoney(frozen.totalUsdt), total: '', unit: 'USDT', sub: '含待审提现冻结' }
  ]
})

const todoList = computed(() => {
  const pendingAmount = moneyOf('pendingWithdrawAmount')
  const defs = [
    {
      key: 'kyc',
      title: '待审核实名',
      desc: '尚未实名的会员',
      count: n(stats.value.pendingKyc),
      unit: '人',
      menuTitles: ['会员管理'],
      perms: ['biz:member:list'],
      query: { kycStatus: '0' },
      icon: User,
      color: '#F56C6C',
      bg: 'rgba(245, 108, 108, 0.12)',
      tagType: 'danger' as const
    },
    {
      key: 'withdrawCount',
      title: '待处理提现笔数',
      desc: '审核中 / 待打款',
      count: n(stats.value.pendingWithdraw),
      unit: '笔',
      menuTitles: ['提现审核'],
      perms: ['biz:withdraw:list', 'biz:withdraw:query', 'biz:withdraw:audit'],
      query: { status: '0' },
      icon: CreditCard,
      color: '#E6A23C',
      bg: 'rgba(230, 162, 60, 0.12)',
      tagType: 'warning' as const
    },
    {
      key: 'withdrawAmount',
      title: '待处理提现金额',
      desc: '审核中 / 待打款金额，CNY / USDT 分开',
      count: n(stats.value.pendingWithdraw),
      unit: '笔',
      amountText: `${fmtMoney(pendingAmount.totalCny)} CNY`,
      amountSub: `${fmtMoney(pendingAmount.totalUsdt)} USDT`,
      menuTitles: ['提现审核'],
      perms: ['biz:withdraw:list', 'biz:withdraw:query', 'biz:withdraw:audit'],
      query: { status: '0' },
      icon: Wallet,
      color: '#409EFF',
      bg: 'rgba(64, 158, 255, 0.12)',
      tagType: 'primary' as const
    }
  ]
  return defs
    .map((item) => ({ ...item, path: resolveMenuPath(...item.menuTitles) }))
    .filter((item) => canShow(item.path, item.perms))
})

const todoTotal = computed(() => n(stats.value.pendingKyc) + n(stats.value.pendingWithdraw))

const shortcutDefs = [
  { name: '新闻资讯', menuTitles: ['新闻资讯'], perms: ['biz:news:list', 'biz:news:query'], icon: Document, color: '#409EFF', bg: 'rgba(64,158,255,.1)' },
  { name: '视频轮播', menuTitles: ['视频轮播'], perms: ['biz:carousel:list', 'biz:carousel:query'], icon: Picture, color: '#67C23A', bg: 'rgba(103,194,58,.1)' },
  { name: '官方群聊', menuTitles: ['官方群聊'], perms: ['biz:group:list', 'biz:group:query'], icon: ChatDotRound, color: '#E6A23C', bg: 'rgba(230,162,60,.1)' },
  { name: '客服中心', menuTitles: ['客服中心'], perms: ['biz:service:list', 'biz:service:query'], icon: Headset, color: '#F56C6C', bg: 'rgba(245,108,108,.1)' },
  { name: '运行概览', menuTitles: ['运行概览'], perms: ['biz:overview:list', 'biz:overview:query'], icon: DataBoard, color: '#9B59B6', bg: 'rgba(155,89,182,.1)' },
  { name: '关于我们', menuTitles: ['关于我们'], perms: ['biz:about:list', 'biz:about:query'], icon: Stamp, color: '#607D8B', bg: 'rgba(96,125,139,.1)' }
]

const shortcuts = computed(() =>
  shortcutDefs
    .map((item) => ({
      ...item,
      path: resolveMenuPath(...item.menuTitles),
      menuIcon: resolveMenuIcon(...item.menuTitles)
    }))
    .filter((item) => canShow(item.path, item.perms))
)

const bizTypeMap: Record<string, { label: string; tagType: string }> = {
  RECHARGE: { label: '充值', tagType: 'success' },
  SUBSCRIBE: { label: '认购', tagType: 'warning' },
  REBATE: { label: '日返', tagType: 'primary' },
  COMMISSION: { label: '分佣', tagType: 'primary' },
  INVITE: { label: '邀请', tagType: 'success' },
  CHECKIN: { label: '签到', tagType: '' },
  KYC_REWARD: { label: '实名奖励', tagType: 'info' },
  LEVEL_REWARD: { label: '等级奖励', tagType: 'info' },
  WITHDRAW_FREEZE: { label: '提现', tagType: 'danger' },
  WITHDRAW_SUCCESS: { label: '提现成功', tagType: 'danger' },
  WITHDRAW_REJECT: { label: '提现退回', tagType: 'info' }
}

const recentActivities = computed(() => {
  const rows = Array.isArray(stats.value.recent) ? stats.value.recent : []
  return rows.map((row: any) => {
    const meta = bizTypeMap[row.bizType] || { label: row.bizType || '流水', tagType: 'info' }
    const amount = n(row.amount)
    return {
      time: parseTime(row.createTime) || '',
      type: meta.label,
      tagType: meta.tagType,
      user: row.phone || (row.memberId ? `ID ${row.memberId}` : '—'),
      content: row.remark || meta.label,
      amount: `${amount >= 0 ? '+' : ''}${fmtMoney(amount)} ${row.currency || ''}`.trim(),
      amountClass: amount >= 0 ? 'amt-plus' : 'amt-minus'
    }
  })
})

const trendType = ref<'register' | 'order' | 'fund'>('fund')
const trendChartRef = ref<HTMLElement | null>(null)
let chart: ECharts | null = null

function renderTrendChart(): void {
  if (!trendChartRef.value) return
  if (!chart) {
    chart = echarts.init(trendChartRef.value)
  }
  const dates = trend.value.dates || []
  const seriesMap: Record<string, { name: string; data: any[]; color: string }[]> = {
    register: [{ name: '新增注册', data: trend.value.register || [], color: '#409EFF' }],
    order: [
      { name: '认购笔数', data: trend.value.orderCount || [], color: '#E6A23C' },
      { name: '认购人数', data: trend.value.orderUsers || [], color: '#67C23A' }
    ],
    fund: [
      { name: '充值CNY', data: trend.value.rechargeCny || [], color: '#F56C6C' },
      { name: '提现CNY', data: trend.value.withdrawCny || [], color: '#909399' }
    ]
  }
  const series = seriesMap[trendType.value]
  chart.setOption({
    color: series.map((s) => s.color),
    tooltip: { trigger: 'axis' },
    legend: { data: series.map((s) => s.name), bottom: 0 },
    grid: { left: 40, right: 20, top: 24, bottom: 36 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
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

function go(path?: string, query?: Record<string, string>): void {
  if (!path) {
    ElMessage.warning('当前账号无对应菜单权限，或菜单尚未配置')
    return
  }
  router.push({ path, query }).catch(() => {
    ElMessage.warning('菜单路径跳转失败，请从侧边栏进入')
  })
}

function onResize(): void {
  chart?.resize()
}

async function onTabChange(name: string | number) {
  if (name !== 'dashboard') return
  await nextTick()
  renderTrendChart()
  chart?.resize()
}

async function reload() {
  loading.value = true
  try {
    const [statsRes, trendRes] = await Promise.all([
      getDashboardStats(queryDate.value),
      getDashboardTrend(queryDate.value)
    ])
    stats.value = (statsRes as any).data || {}
    trend.value = (trendRes as any).data || {}
    await nextTick()
    if (activeTab.value === 'dashboard') {
      renderTrendChart()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  reload()
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
    flex-wrap: wrap;
  }

  .update-time {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}

.hint-alert {
  margin-bottom: 16px;
}

.home-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
}

.section-title {
  margin: 4px 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.metric-row {
  margin-bottom: 8px;
}

.metric-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  padding: 14px 16px 12px;
  margin-bottom: 14px;
  min-height: 122px;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, transform 0.2s;

  &.tint {
    background: linear-gradient(180deg, #f7fbff 0%, var(--el-bg-color) 72%);
  }

  &.clickable {
    cursor: pointer;

    &:hover {
      box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
      transform: translateY(-1px);
    }
  }

  .metric-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
  }

  .metric-title {
    font-size: 13px;
    color: var(--el-text-color-regular);
    line-height: 1.3;
  }

  .metric-tag {
    flex-shrink: 0;
    font-size: 11px;
    padding: 1px 8px;
    border-radius: 999px;
    color: #3d7eff;
    background: #eaf2ff;

    &.stock {
      color: #606266;
      background: #eef0f3;
    }
  }

  .metric-value {
    margin-top: 10px;
    font-size: 22px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: 0.2px;
    line-height: 1.2;
    word-break: break-all;

    &.dual {
      display: flex;
      flex-direction: column;
      gap: 4px;
      font-size: 18px;
      line-height: 1.35;
    }

    .unit {
      margin-left: 4px;
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-secondary);
    }
  }

  .metric-sub {
    margin-top: 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .metric-foot {
    margin-top: auto;
    padding-top: 10px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    border-top: 1px dashed var(--el-border-color-lighter);

    .foot-dual {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 2px;
      color: var(--el-text-color-regular);
      font-weight: 500;
      text-align: right;
    }
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

  .todo-right {
    text-align: right;
    flex-shrink: 0;
  }

  .todo-amount {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .todo-amount-sub {
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

.bottom-row {
  margin-top: 0;
}
</style>
