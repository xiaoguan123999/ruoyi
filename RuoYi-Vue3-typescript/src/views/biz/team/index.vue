<template>
  <div class="app-container ops-page">
    <el-alert
      title="团队查询：按手机号或会员ID查找后，可看全部下线汇总（不限 7 级）。结构图看下级树，推荐关系图看从顶点到该会员的路径及同级列表。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-card v-if="summaryMember" shadow="never" class="summary-card">
      <div class="summary-hd">
        <div class="summary-hd__main">
          <span class="summary-phone">{{ summaryMember.phone }}</span>
          <span class="summary-name">{{ summaryMember.realName || "未实名姓名" }}</span>
          <el-tag size="small" :type="summaryMember.status === '1' ? 'danger' : 'success'">
            {{ summaryMember.status === "1" ? "停用" : "正常" }}
          </el-tag>
          <el-tag size="small" :type="summaryMember.kycStatus === '1' ? 'success' : 'info'">
            {{ summaryMember.kycStatus === "1" ? "已实名" : "未实名" }}
          </el-tag>
          <el-tag size="small" type="warning">{{ summaryMember.levelName || "无等级" }}</el-tag>
          <span class="summary-meta">团队 {{ summaryMember.teamCount ?? 0 }} 人 · 注册 {{ parseTime(summaryMember.createTime) }}</span>
        </div>
        <div class="summary-hd__actions">
          <el-button type="primary" plain @click="openTeam(summaryMember)" v-hasPermi="['biz:team:list']">查看下线</el-button>
          <el-button type="primary" plain @click="openTree(summaryMember)" v-hasPermi="['biz:team:tree']">结构图</el-button>
          <el-button type="primary" plain @click="openRelation(summaryMember)" v-hasPermi="['biz:team:relation']">关系图</el-button>
        </div>
      </div>

      <el-descriptions :column="4" border size="small" class="summary-desc">
        <el-descriptions-item label="会员ID">{{ summaryMember.memberId }}</el-descriptions-item>
        <el-descriptions-item label="上级ID">{{ summaryMember.parentId || "—" }}</el-descriptions-item>
        <el-descriptions-item label="邀请码">{{ summaryMember.inviteCode || "—" }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ summaryMember.lastLoginTime ? parseTime(summaryMember.lastLoginTime) : "—" }}</el-descriptions-item>
        <el-descriptions-item label="CNY可用">{{ summaryMember.cnyAvailable ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="CNY冻结">{{ summaryMember.cnyFrozen ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="USDT可用">{{ summaryMember.usdtAvailable ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="USDT冻结">{{ summaryMember.usdtFrozen ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="推广收益CNY">{{ summaryMember.cnyAssistValue ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="推广收益USDT">{{ summaryMember.usdtAssistValue ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="注册IP">{{ summaryMember.registerIp || "—" }}</el-descriptions-item>
        <el-descriptions-item label="登录IP">{{ summaryMember.lastLoginIp || "—" }}</el-descriptions-item>
        <el-descriptions-item label="今日注册">{{ teamOverview.registerToday ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="总注册">{{ teamOverview.registerTotal ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日实名">{{ teamOverview.kycToday ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="总实名">{{ teamOverview.kycTotal ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日激活">{{ teamOverview.activeToday ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="总激活">{{ teamOverview.activeTotal ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日签到">{{ teamOverview.checkinToday ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="昨日签到">{{ teamOverview.checkinYesterday ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日充值CNY">{{ teamOverview.rechargeTodayCny ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="总充值CNY">{{ teamOverview.rechargeTotalCny ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日充值USDT">{{ teamOverview.rechargeTodayUsdt ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="总充值USDT">{{ teamOverview.rechargeTotalUsdt ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日认购CNY">{{ teamOverview.subscribeTodayCny ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="总认购CNY">{{ teamOverview.subscribeTotalCny ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日认购USDT">{{ teamOverview.subscribeTodayUsdt ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="总认购USDT">{{ teamOverview.subscribeTotalUsdt ?? 0 }}</el-descriptions-item>
      </el-descriptions>

      <template v-if="summaryRows.length">
        <div class="section-title">层级明细</div>
        <el-table :data="summaryRows" size="small" border>
          <el-table-column label="层级" align="center" prop="teamLevel" width="80">
            <template #default="scope">{{ scope.row.teamLevel }}级</template>
          </el-table-column>
          <el-table-column label="今日新注册" align="center" prop="registerToday" />
          <el-table-column label="注册人数" align="center" prop="register" />
          <el-table-column label="激活人数" align="center" prop="active" />
          <el-table-column label="认购CNY" align="center" prop="subscribeCny" />
          <el-table-column label="认购USDT" align="center" prop="subscribeUsdt" />
          <el-table-column label="充值CNY" align="center" prop="rechargeCny" />
          <el-table-column label="充值USDT" align="center" prop="rechargeUsdt" />
        </el-table>
      </template>
    </el-card>

    <template v-if="showMemberList">
      <el-table v-loading="loading" :data="dataList">
        <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
        <el-table-column label="手机号" align="center" prop="phone" width="120" />
        <el-table-column label="姓名" align="center" prop="realName" />
        <el-table-column label="上级ID" align="center" prop="parentId" width="90" />
        <el-table-column label="祖级" align="center" prop="ancestors" min-width="140" show-overflow-tooltip />
        <el-table-column label="等级" align="center" min-width="90"><template #default="scope">{{ scope.row.levelName || "无等级" }}</template></el-table-column>
        <el-table-column label="CNY可用" align="center" prop="cnyAvailable" width="100" />
        <el-table-column label="USDT可用" align="center" prop="usdtAvailable" width="100" />
        <el-table-column label="签到总次数" align="center" prop="checkinCount" width="100">
          <template #default="scope">{{ scope.row.checkinCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="团队人数" align="center" prop="teamCount" width="90" />
        <el-table-column label="注册时间" align="center" prop="createTime" width="160">
          <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
        </el-table-column>
        <el-table-column label="最后登录时间" align="center" width="160">
          <template #default="scope"><span>{{ scope.row.lastLoginTime ? parseTime(scope.row.lastLoginTime) : "--" }}</span></template>
        </el-table-column>
        <el-table-column label="注册IP" align="center" prop="registerIp" width="130">
          <template #default="scope">{{ scope.row.registerIp || "--" }}</template>
        </el-table-column>
        <el-table-column label="最后登录IP" align="center" prop="lastLoginIp" width="130">
          <template #default="scope">{{ scope.row.lastLoginIp || "--" }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="260" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openTeam(scope.row)" v-hasPermi="['biz:team:list']">查看下线</el-button>
            <el-button link type="primary" @click="openTree(scope.row)" v-hasPermi="['biz:team:tree']">结构图</el-button>
            <el-button link type="primary" @click="openRelation(scope.row)" v-hasPermi="['biz:team:relation']">关系图</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </template>
    <el-dialog :title="teamTitle" v-model="teamOpen" width="960px" append-to-body>
      <el-radio-group v-model="teamLevel" @change="loadTeamMembers" class="mb8 team-level-group">
        <el-radio-button v-for="n in teamLevelOptions" :key="n" :value="n">{{ n }}级</el-radio-button>
      </el-radio-group>
      <el-table v-loading="teamLoading" :data="teamRows" max-height="420">
        <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
        <el-table-column label="层级" align="center" width="70">
          <template #default>{{ teamLevel }}</template>
        </el-table-column>
        <el-table-column label="手机号" align="center" prop="phone" min-width="130" />
        <el-table-column label="姓名" align="center" prop="realName" min-width="100" />
        <el-table-column label="等级" align="center" min-width="90"><template #default="scope">{{ scope.row.levelName || "无等级" }}</template></el-table-column>
        <el-table-column label="实名" align="center" prop="kycStatus" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.kycStatus === '1' ? 'success' : 'info'">{{ scope.row.kycStatus === '1' ? '已实名' : '未实名' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="CNY可用" align="center" prop="cnyAvailable" width="100" />
        <el-table-column label="USDT可用" align="center" prop="usdtAvailable" width="110" />
        <el-table-column label="签到总次数" align="center" prop="checkinCount" width="100">
          <template #default="scope">{{ scope.row.checkinCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="团队人数" align="center" prop="teamCount" width="90" />
        <el-table-column label="注册时间" align="center" prop="createTime" width="160">
          <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
        </el-table-column>
        <el-table-column label="最后登录时间" align="center" width="160">
          <template #default="scope"><span>{{ scope.row.lastLoginTime ? parseTime(scope.row.lastLoginTime) : "--" }}</span></template>
        </el-table-column>
        <el-table-column label="注册IP" align="center" width="130">
          <template #default="scope">{{ scope.row.registerIp || "--" }}</template>
        </el-table-column>
        <el-table-column label="最后登录IP" align="center" width="130">
          <template #default="scope">{{ scope.row.lastLoginIp || "--" }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="90" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="drillDown(scope.row)">下级</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizTeam">
import { listTeam, listMemberTeam, getTeamSummary } from "@/api/biz"
import { resolveMenuPath } from "@/utils/menu"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 100, memberId: undefined, phone: undefined })
const teamOpen = ref(false)
const teamTitle = ref("")
const teamLoading = ref(false)
const teamRows = ref<any[]>([])
const teamLevel = ref(1)
const currentMemberId = ref<number>()
const summaryMember = ref<any>(null)
const summaryRows = ref<any[]>([])
const teamOverview = ref<any>({})

const teamLevelOptions = computed(() => {
  const max = summaryRows.value.reduce((m: number, r: any) => Math.max(m, Number(r.teamLevel) || 0), 0)
  return Array.from({ length: Math.max(max, 1) }, (_, i) => i + 1)
})

/** 精确查到单人并已展示汇总时，下方列表与汇总重复，隐藏 */
const showMemberList = computed(() => !(summaryMember.value && dataList.value.length === 1 && total.value <= 1))

function emptyLevel(n: number) {
  return { teamLevel: n, registerToday: 0, register: 0, active: 0, subscribeCny: 0, subscribeUsdt: 0, rechargeCny: 0, rechargeUsdt: 0 }
}

function loadSummary(memberId: number) {
  getTeamSummary(memberId).then((res: any) => {
    summaryMember.value = res.member || res.data?.member || null
    teamOverview.value = res.overview || res.data?.overview || {}
    const levels = res.levels || res.data?.levels
    if (Array.isArray(levels) && levels.length) {
      const max = levels.reduce((m: number, r: any) => Math.max(m, Number(r.teamLevel) || 0), 0)
      const byLevel = new Map(levels.map((r: any) => [Number(r.teamLevel), r]))
      summaryRows.value = Array.from({ length: max }, (_, i) => byLevel.get(i + 1) || emptyLevel(i + 1))
      return
    }
    summaryRows.value = []
  }).catch(() => {
    summaryMember.value = null
    summaryRows.value = []
    teamOverview.value = {}
  })
}

function getList() {
  loading.value = true
  listTeam(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
    const focused = queryParams.value.memberId || queryParams.value.phone
    if (focused && res.rows && res.rows.length === 1) {
      loadSummary(res.rows[0].memberId)
    } else if (!focused) {
      summaryMember.value = null
      summaryRows.value = []
      teamOverview.value = {}
    } else if (res.rows && res.rows.length > 0) {
      loadSummary(res.rows[0].memberId)
    } else {
      summaryMember.value = null
      summaryRows.value = []
      teamOverview.value = {}
    }
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { summaryMember.value = null; summaryRows.value = []; teamOverview.value = {}; proxy.resetForm("queryRef"); handleQuery() }
function openTeam(row: any) {
  currentMemberId.value = row.memberId
  teamLevel.value = 1
  teamTitle.value = "下线（" + (row.phone || row.memberId) + "）"
  teamOpen.value = true
  loadSummary(row.memberId)
  loadTeamMembers()
}
function openBizMenu(titles: string[], keyword: string) {
  const path = resolveMenuPath(...titles)
  if (!path) {
    proxy.$modal.msgWarning(`未找到菜单「${titles[0]}」，请先在系统管理 → 菜单管理中配置该页面`)
    return
  }
  proxy.$router.push({ path, query: { keyword } })
}
function openTree(row: any) {
  openBizMenu(["会员结构图"], String(row.memberId))
}
function openRelation(row: any) {
  openBizMenu(["推荐关系图"], String(row.memberId))
}
function drillDown(row: any) {
  currentMemberId.value = row.memberId
  teamLevel.value = 1
  teamTitle.value = "下线（" + (row.phone || row.memberId) + "）"
  loadSummary(row.memberId)
  loadTeamMembers()
}
function loadTeamMembers() {
  if (!currentMemberId.value) return
  teamLoading.value = true
  listMemberTeam(currentMemberId.value, teamLevel.value).then((res: any) => {
    teamRows.value = res.data || []
  }).finally(() => { teamLoading.value = false })
}
getList()
</script>

<style scoped>
.team-level-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 0;
}
.summary-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.summary-hd__main {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
}
.summary-phone {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
.summary-name {
  font-size: 14px;
  color: var(--el-text-color-regular);
}
.summary-meta {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.summary-hd__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.section-title {
  margin: 4px 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.summary-desc :deep(.el-descriptions__label) {
  width: 108px;
  min-width: 108px;
  max-width: 108px;
  padding: 8px 10px !important;
  white-space: nowrap;
}
.summary-desc :deep(.el-descriptions__content) {
  min-width: 0;
  padding: 8px 12px !important;
  word-break: break-all;
}
</style>
