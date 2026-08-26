<template>
  <div class="app-container ops-page">
    <el-alert
      title="团队查询：按手机号或会员ID查找后，可看 1～7 级下线汇总。结构图看下级树，推荐关系图看从顶点到该会员的路径及同级列表。"
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

    <el-card v-if="summaryMember" shadow="never" class="mb8">
      <el-descriptions :column="4" border size="small" class="mb8">
        <el-descriptions-item label="账号">{{ summaryMember.phone }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ summaryMember.realName || "—" }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ summaryMember.status === "1" ? "停用" : "正常" }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ summaryMember.levelName || "—" }}</el-descriptions-item>
        <el-descriptions-item label="CNY可用">{{ summaryMember.cnyAvailable ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="USDT可用">{{ summaryMember.usdtAvailable ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="推广收益CNY">{{ summaryMember.cnyAssistValue ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="推广收益USDT">{{ summaryMember.usdtAssistValue ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="团队人数">{{ summaryMember.teamCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="实名">{{ summaryMember.kycStatus === "1" ? "已实名" : "未实名" }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ parseTime(summaryMember.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <div class="mb8">
        <el-button type="primary" plain @click="openTree(summaryMember)" v-hasPermi="['biz:team:tree']">会员结构图</el-button>
        <el-button type="primary" plain @click="openRelation(summaryMember)" v-hasPermi="['biz:team:relation']">推荐关系图</el-button>
      </div>
      <el-table :data="summaryRows" size="small" border>
        <el-table-column label="层级" align="center" prop="teamLevel" width="80">
          <template #default="scope">{{ scope.row.teamLevel }}级</template>
        </el-table-column>
        <el-table-column label="注册人数" align="center" prop="register" />
        <el-table-column label="实名人数" align="center" prop="active" />
        <el-table-column label="认购CNY" align="center" prop="subscribeCny" />
        <el-table-column label="认购USDT" align="center" prop="subscribeUsdt" />
        <el-table-column label="充值CNY" align="center" prop="rechargeCny" />
        <el-table-column label="充值USDT" align="center" prop="rechargeUsdt" />
      </el-table>
    </el-card>

    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="姓名" align="center" prop="realName" />
      <el-table-column label="上级ID" align="center" prop="parentId" width="90" />
      <el-table-column label="祖级" align="center" prop="ancestors" min-width="140" show-overflow-tooltip />
      <el-table-column label="等级" align="center" prop="levelName" width="80" />
      <el-table-column label="CNY可用" align="center" prop="cnyAvailable" width="100" />
      <el-table-column label="USDT可用" align="center" prop="usdtAvailable" width="100" />
      <el-table-column label="团队人数" align="center" prop="teamCount" width="90" />
      <el-table-column label="注册时间" align="center" prop="createTime" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
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

    <el-dialog :title="teamTitle" v-model="teamOpen" width="960px" append-to-body>
      <el-radio-group v-model="teamLevel" @change="loadTeamMembers" class="mb8">
        <el-radio-button v-for="n in 7" :key="n" :value="n">{{ n }}级</el-radio-button>
      </el-radio-group>
      <el-table v-loading="teamLoading" :data="teamRows" max-height="420">
        <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
        <el-table-column label="层级" align="center" width="70">
          <template #default>{{ teamLevel }}</template>
        </el-table-column>
        <el-table-column label="手机号" align="center" prop="phone" min-width="130" />
        <el-table-column label="姓名" align="center" prop="realName" min-width="100" />
        <el-table-column label="等级" align="center" prop="levelName" width="90" />
        <el-table-column label="实名" align="center" prop="kycStatus" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.kycStatus === '1' ? 'success' : 'info'">{{ scope.row.kycStatus === '1' ? '已实名' : '未实名' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="CNY可用" align="center" prop="cnyAvailable" width="100" />
        <el-table-column label="USDT可用" align="center" prop="usdtAvailable" width="110" />
        <el-table-column label="团队人数" align="center" prop="teamCount" width="90" />
        <el-table-column label="注册时间" align="center" prop="createTime" width="160">
          <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="90">
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

function levelStats(summary: any, n: number) {
  return summary?.["level" + n] || { teamLevel: n, register: 0, active: 0, subscribeCny: 0, subscribeUsdt: 0, rechargeCny: 0, rechargeUsdt: 0 }
}

function loadSummary(memberId: number) {
  getTeamSummary(memberId).then((res: any) => {
    summaryMember.value = res.member || res.data?.member || null
    const summary = res.summary || res.data?.summary
    summaryRows.value = summary ? [1, 2, 3, 4, 5, 6, 7].map(n => levelStats(summary, n)) : []
  }).catch(() => {
    summaryMember.value = null
    summaryRows.value = []
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
    } else if (res.rows && res.rows.length > 0) {
      loadSummary(res.rows[0].memberId)
    } else {
      summaryMember.value = null
      summaryRows.value = []
    }
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { summaryMember.value = null; summaryRows.value = []; proxy.resetForm("queryRef"); handleQuery() }
function openTeam(row: any) {
  currentMemberId.value = row.memberId
  teamLevel.value = 1
  teamTitle.value = "下线（" + (row.phone || row.memberId) + "）"
  teamOpen.value = true
  loadSummary(row.memberId)
  loadTeamMembers()
}
function openTree(row: any) {
  const q = row.phone || String(row.memberId)
  proxy.$router.push({ path: "/biz/teamTree", query: { keyword: q } })
}
function openRelation(row: any) {
  const q = row.phone || String(row.memberId)
  proxy.$router.push({ path: "/biz/teamRelation", query: { keyword: q } })
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
