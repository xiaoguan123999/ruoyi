<template>
  <div class="app-container ops-page">
    <el-alert
      title="选择会员后可展开直属下级。点击 ID 可下钻为新根节点继续往下看；路径可点击回退。同一注册/登录 IP 会标红。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :inline="true" @submit.prevent="handleQuery">
      <el-form-item label="会员">
        <MemberSelect v-model="memberId" @change="onMemberChange" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!root && !loading" description="请选择会员，查看该会员及其下级结构" />

    <template v-if="root">
      <el-row :gutter="12" class="mb8 summary-row">
        <el-col :xs="12" :sm="8" :md="4" v-for="item in summaryCards" :key="item.label">
          <div class="summary-card">
            <div class="summary-label">{{ item.label }}</div>
            <div class="summary-value">{{ item.value }}</div>
          </div>
        </el-col>
      </el-row>

      <div v-if="pathStack.length" class="path-bar mb8">
        <span class="path-label">当前路径</span>
        <template v-for="(item, index) in pathStack" :key="item.memberId">
          <span v-if="index > 0" class="path-sep">/</span>
          <el-button
            link
            type="primary"
            class="path-item"
            :class="{ 'is-current': index === pathStack.length - 1 }"
            @click="jumpToPath(index)"
          >{{ item.memberId }}{{ item.phone ? `（${item.phone}）` : "" }}</el-button>
        </template>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        :key="root.memberId"
        row-key="memberId"
        border
        lazy
        :indent="0"
        :load="loadChildren"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        :row-class-name="rowClassName"
        class="team-tree-table"
      >
        <el-table-column label="会员" min-width="200" fixed>
          <template #default="scope">
            <div class="member-cell">
              <el-tag size="small" effect="plain" class="depth-tag" :class="'depth-tag-' + depthTone(scope.row)">L{{ rowDepth(scope.row) }}</el-tag>
              <el-button link type="primary" class="id-link" @click="drill(scope.row)">{{ scope.row.memberId }}</el-button>
              <span class="phone-text">{{ scope.row.phone || "—" }}</span>
            </div>
          </template>
        </el-table-column>
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
const memberId = ref<number | undefined>()
const loading = ref(false)
const root = ref<any>(null)
const summary = ref<any>(null)
const tableData = ref<any[]>([])
const loadedRows = ref<any[]>([])
const pathStack = ref<any[]>([])

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

function pickPathItem(row: any) {
  return {
    memberId: row.memberId,
    phone: row.phone || "",
    realName: row.realName || ""
  }
}

function remember(rows: any[]) {
  const byId = new Map(loadedRows.value.map((r: any) => [r.memberId, r]))
  rows.forEach((r: any) => byId.set(r.memberId, r))
  loadedRows.value = Array.from(byId.values())
}

function withDepth(rows: any[], depth: number) {
  return (rows || []).map((row: any) => ({ ...row, _depth: depth }))
}

function rowDepth(row: any) {
  const d = Number(row?._depth)
  return Number.isFinite(d) && d > 0 ? d : 1
}

/** 相邻层级高对比色，超过 10 层循环 */
const DEPTH_TONE_COUNT = 10
function depthTone(row: any) {
  return ((rowDepth(row) - 1) % DEPTH_TONE_COUNT) + 1
}

function rowClassName({ row }: { row: any }) {
  return `depth-row depth-${depthTone(row)}`
}

function clearView() {
  root.value = null
  summary.value = null
  tableData.value = []
  loadedRows.value = []
  pathStack.value = []
}

function syncPath(node: any, mode: "reset" | "drill") {
  const item = pickPathItem(node)
  if (mode === "reset") {
    pathStack.value = [item]
    return
  }
  const idx = pathStack.value.findIndex((p) => Number(p.memberId) === Number(item.memberId))
  if (idx >= 0) {
    pathStack.value = pathStack.value.slice(0, idx + 1)
  } else {
    pathStack.value = [...pathStack.value, item]
  }
}

function loadByKeyword(q: string, mode: "reset" | "drill" = "reset") {
  loading.value = true
  getTeamTree(q).then((res: any) => {
    const node = res.data ? { ...res.data, _depth: 1 } : null
    root.value = node
    summary.value = res.summary || res.data?.summary || null
    tableData.value = node ? [node] : []
    loadedRows.value = node ? [node] : []
    if (node?.memberId) {
      memberId.value = Number(node.memberId)
      syncPath(node, mode)
    } else {
      pathStack.value = []
    }
  }).catch(() => {
    clearView()
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  if (!memberId.value) {
    proxy.$modal.msgWarning("请选择会员")
    return
  }
  loadByKeyword(String(memberId.value), "reset")
}

function onMemberChange(id?: number) {
  if (!id) {
    clearView()
    return
  }
  handleQuery()
}

function loadChildren(row: any, _treeNode: any, resolve: (data: any[]) => void) {
  const depth = rowDepth(row) + 1
  listTeamChildren(row.memberId).then((res: any) => {
    const rows = withDepth(res.data || [], depth)
    remember(rows)
    resolve(rows)
  }).catch(() => resolve([]))
}

function drill(row: any) {
  if (!row?.memberId) return
  if (Number(row.memberId) === Number(root.value?.memberId)) return
  memberId.value = Number(row.memberId)
  loadByKeyword(String(row.memberId), "drill")
}

function drillById(id: number) {
  if (!id) return
  if (Number(id) === Number(root.value?.memberId)) return
  memberId.value = Number(id)
  loadByKeyword(String(id), "drill")
}

function jumpToPath(index: number) {
  const item = pathStack.value[index]
  if (!item?.memberId) return
  if (Number(item.memberId) === Number(root.value?.memberId)) return
  memberId.value = Number(item.memberId)
  pathStack.value = pathStack.value.slice(0, index + 1)
  loadByKeyword(String(item.memberId), "drill")
}

onMounted(() => {
  const q = String(route.query.keyword || "").trim()
  if (!q) return
  const asId = Number(q)
  if (Number.isFinite(asId) && String(asId) === q) {
    memberId.value = asId
    handleQuery()
    return
  }
  loadByKeyword(q, "reset")
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
.path-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 2px;
  padding: 10px 14px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}
.path-label {
  margin-right: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.path-sep {
  color: var(--el-text-color-placeholder);
  margin: 0 2px;
}
.path-item {
  padding: 0 4px;
}
.path-item.is-current {
  font-weight: 700;
  color: var(--el-text-color-primary) !important;
  cursor: default;
}
.ip-dup {
  color: var(--el-color-danger);
  font-weight: 600;
}
.member-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  vertical-align: middle;
}
.depth-tag {
  flex: none;
  min-width: 36px;
  justify-content: center;
  border-width: 1px;
}
.depth-tag-1 { color: #1677ff; background: #e6f4ff; border-color: #91caff; }
.depth-tag-2 { color: #d46b08; background: #fff7e6; border-color: #ffd591; }
.depth-tag-3 { color: #389e0d; background: #f6ffed; border-color: #b7eb8f; }
.depth-tag-4 { color: #cf1322; background: #fff1f0; border-color: #ffa39e; }
.depth-tag-5 { color: #531dab; background: #f9f0ff; border-color: #d3adf7; }
.depth-tag-6 { color: #08979c; background: #e6fffb; border-color: #87e8de; }
.depth-tag-7 { color: #c41d7f; background: #fff0f6; border-color: #ffadd2; }
.depth-tag-8 { color: #ad4e00; background: #fff7e6; border-color: #ffc069; }
.depth-tag-9 { color: #1d39c4; background: #f0f5ff; border-color: #adc6ff; }
.depth-tag-10 { color: #5b8c00; background: #fcffe6; border-color: #eaff8f; }
.id-link {
  flex: none;
  font-weight: 600;
}
.phone-text {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.team-tree-table :deep(.el-table__indent),
.team-tree-table :deep(.el-table__placeholder) {
  display: none !important;
  width: 0 !important;
  padding: 0 !important;
}
.team-tree-table :deep(.el-table__expand-icon) {
  margin-right: 6px;
}
.team-tree-table :deep(.depth-row > td:first-child) {
  box-shadow: inset 3px 0 0 var(--depth-color, transparent);
}
.team-tree-table :deep(.depth-1) { --depth-color: #1677ff; }
.team-tree-table :deep(.depth-2) { --depth-color: #d46b08; }
.team-tree-table :deep(.depth-3) { --depth-color: #389e0d; }
.team-tree-table :deep(.depth-4) { --depth-color: #cf1322; }
.team-tree-table :deep(.depth-5) { --depth-color: #531dab; }
.team-tree-table :deep(.depth-6) { --depth-color: #08979c; }
.team-tree-table :deep(.depth-7) { --depth-color: #c41d7f; }
.team-tree-table :deep(.depth-8) { --depth-color: #ad4e00; }
.team-tree-table :deep(.depth-9) { --depth-color: #1d39c4; }
.team-tree-table :deep(.depth-10) { --depth-color: #5b8c00; }
</style>
