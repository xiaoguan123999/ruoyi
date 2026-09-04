<template>
  <div class="app-container ops-page">
    <el-alert
      title="搜索会员后可展开下级。同一注册/登录 IP 会标红，便于判断是否同一设备刷团队。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :inline="true" @submit.prevent="handleQuery">
      <el-form-item>
        <el-input
          v-model="keyword"
          placeholder="手机号 / 会员ID / 邀请码"
          clearable
          style="width: 280px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!root && !loading" description="输入手机号或会员ID，查看该会员及其下级结构" />

    <template v-if="root">
      <el-row :gutter="12" class="mb8 summary-row">
        <el-col :xs="12" :sm="8" :md="4" v-for="item in summaryCards" :key="item.label">
          <div class="summary-card">
            <div class="summary-label">{{ item.label }}</div>
            <div class="summary-value">{{ item.value }}</div>
          </div>
        </el-col>
      </el-row>

      <el-table
        v-loading="loading"
        :data="tableData"
        :key="root.memberId"
        row-key="memberId"
        border
        lazy
        :load="loadChildren"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      >
        <el-table-column label="ID" min-width="110" fixed>
          <template #default="scope">
            <el-button link type="primary" @click="drill(scope.row)">{{ scope.row.memberId }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="账号" align="center" prop="phone" min-width="120" />
        <el-table-column label="姓名" align="center" min-width="100">
          <template #default="scope">
            {{ scope.row.realName || "—" }}
            <el-tag v-if="scope.row.kycStatus === '1'" type="success" size="small" style="margin-left: 4px">实名</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="身份证" align="center" min-width="170" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.idCard || "—" }}</template>
        </el-table-column>
        <el-table-column label="父级ID" align="center" min-width="100">
          <template #default="scope">
            <el-button v-if="scope.row.parentId" link type="primary" @click="drillById(scope.row.parentId)">{{ scope.row.parentId }}</el-button>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="充值" align="right" min-width="130">
          <template #default="scope">{{ moneyPair(scope.row.rechargeCny, scope.row.rechargeUsdt) }}</template>
        </el-table-column>
        <el-table-column label="购买" align="right" min-width="130">
          <template #default="scope">{{ moneyPair(scope.row.subscribeCny, scope.row.subscribeUsdt) }}</template>
        </el-table-column>
        <el-table-column label="提现" align="right" min-width="130">
          <template #default="scope">{{ moneyPair(scope.row.withdrawCny, scope.row.withdrawUsdt) }}</template>
        </el-table-column>
        <el-table-column label="下级总人数" align="center" prop="teamCount" width="110" />
        <el-table-column label="最后登录IP" align="center" min-width="140">
          <template #default="scope">
            <span :class="{ 'ip-dup': isDupIp(scope.row.lastLoginIp) }">{{ scope.row.lastLoginIp || "—" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" align="center" width="170">
          <template #default="scope"><span>{{ parseTime(scope.row.lastLoginTime) || "—" }}</span></template>
        </el-table-column>
        <el-table-column label="注册IP" align="center" min-width="140">
          <template #default="scope">
            <span :class="{ 'ip-dup': isDupIp(scope.row.registerIp) }">{{ scope.row.registerIp || "—" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" align="center" width="170">
          <template #default="scope"><span>{{ parseTime(scope.row.createTime) || "—" }}</span></template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<script setup lang="ts" name="BizTeamTree">
import { getTeamTree, listTeamChildren } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const route = useRoute()
const keyword = ref("")
const loading = ref(false)
const root = ref<any>(null)
const summary = ref<any>(null)
const tableData = ref<any[]>([])
const loadedRows = ref<any[]>([])

const summaryCards = computed(() => {
  const s = summary.value || {}
  return [
    { label: "下级总人数", value: fmtInt(s.teamCount) },
    { label: "下一级激活人数", value: fmtInt(s.directActive) },
    { label: "下级实名人数", value: fmtInt(s.teamKyc) },
    { label: "下级购买总额", value: moneyPair(s.subscribeCny, s.subscribeUsdt) },
    { label: "下级充值总额", value: moneyPair(s.rechargeCny, s.rechargeUsdt) },
    { label: "下级提现总额", value: moneyPair(s.withdrawCny, s.withdrawUsdt) }
  ]
})

const dupIps = computed(() => {
  const map = new Map<string, Set<number>>()
  loadedRows.value.forEach((row: any) => {
    const ips = [row.registerIp, row.lastLoginIp].filter((ip: string) => !!ip)
    Array.from(new Set(ips)).forEach((ip: string) => {
      if (!map.has(ip)) map.set(ip, new Set())
      map.get(ip)!.add(row.memberId)
    })
  })
  const dups = new Set<string>()
  map.forEach((members, ip) => {
    if (members.size > 1) dups.add(ip)
  })
  return dups
})

function fmtInt(v: any) {
  const n = Number(v || 0)
  return Number.isFinite(n) ? n.toLocaleString() : "0"
}

function moneyPair(cny: any, usdt: any) {
  const c = Number(cny || 0)
  const u = Number(usdt || 0)
  const cs = c.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 })
  if (!u) return cs
  return cs + " / " + u.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 }) + " USDT"
}

function isDupIp(ip: string) {
  return !!ip && dupIps.value.has(ip)
}

function remember(rows: any[]) {
  const byId = new Map(loadedRows.value.map((r: any) => [r.memberId, r]))
  rows.forEach((r: any) => byId.set(r.memberId, r))
  loadedRows.value = Array.from(byId.values())
}

function handleQuery() {
  const q = (keyword.value || "").trim()
  if (!q) {
    proxy.$modal.msgWarning("请输入手机号或会员ID")
    return
  }
  loading.value = true
  getTeamTree(q).then((res: any) => {
    root.value = res.data
    summary.value = res.summary || res.data?.summary || null
    tableData.value = root.value ? [root.value] : []
    loadedRows.value = root.value ? [root.value] : []
  }).catch(() => {
    root.value = null
    summary.value = null
    tableData.value = []
    loadedRows.value = []
  }).finally(() => {
    loading.value = false
  })
}

function loadChildren(row: any, _treeNode: any, resolve: (data: any[]) => void) {
  listTeamChildren(row.memberId).then((res: any) => {
    const rows = res.data || []
    remember(rows)
    resolve(rows)
  }).catch(() => resolve([]))
}

function drill(row: any) {
  if (!row?.memberId) return
  keyword.value = String(row.phone || row.memberId)
  handleQuery()
}

function drillById(id: number) {
  keyword.value = String(id)
  handleQuery()
}

onMounted(() => {
  const q = (route.query.keyword as string) || ""
  if (q) {
    keyword.value = q
    handleQuery()
  }
})
</script>

<style scoped>
.summary-row {
  margin-bottom: 12px;
}
.summary-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 12px 14px;
  min-height: 74px;
}
.summary-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-bottom: 8px;
}
.summary-value {
  color: var(--el-color-primary);
  font-size: 20px;
  font-weight: 600;
  line-height: 1.2;
  word-break: break-all;
}
.ip-dup {
  color: var(--el-color-danger);
  font-weight: 600;
}
</style>
