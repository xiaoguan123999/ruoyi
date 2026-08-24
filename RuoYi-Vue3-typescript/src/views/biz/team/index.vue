<template>
  <div class="app-container">
    <el-alert
      title="可查看某会员 1～7 级下线。App「我的团队」也会按这七级返回注册/激活人数和认购、充值汇总。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员ID" prop="memberId">
        <el-input v-model="queryParams.memberId" placeholder="会员ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
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
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="姓名" align="center" prop="realName" />
      <el-table-column label="上级ID" align="center" prop="parentId" width="90" />
      <el-table-column label="祖级" align="center" prop="ancestors" />
      <el-table-column label="等级" align="center" prop="levelName" width="80" />
      <el-table-column label="团队人数" align="center" prop="teamCount" width="90" />
      <el-table-column label="注册时间" align="center" prop="createTime" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openTeam(scope.row)" v-hasPermi="['biz:team:list']">查看下线</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="teamTitle" v-model="teamOpen" width="860px" append-to-body>
      <el-radio-group v-model="teamLevel" @change="loadTeamMembers" class="mb8">
        <el-radio-button v-for="n in 7" :key="n" :value="n">{{ n }}级</el-radio-button>
      </el-radio-group>
      <el-table v-loading="teamLoading" :data="teamRows" max-height="420">
        <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
        <el-table-column label="手机号" align="center" prop="phone" width="130" />
        <el-table-column label="姓名" align="center" prop="realName" />
        <el-table-column label="等级" align="center" prop="levelName" width="90" />
        <el-table-column label="实名" align="center" prop="kycStatus" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.kycStatus === '1' ? 'success' : 'info'">{{ scope.row.kycStatus === '1' ? '已实名' : '未实名' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" align="center" prop="createTime" width="160">
          <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizTeam">
import { listTeam, listMemberTeam } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, memberId: undefined, phone: undefined })
const teamOpen = ref(false)
const teamTitle = ref("")
const teamLoading = ref(false)
const teamRows = ref<any[]>([])
const teamLevel = ref(1)
const currentMemberId = ref<number>()

function getList() {
  loading.value = true
  listTeam(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function openTeam(row: any) {
  currentMemberId.value = row.memberId
  teamLevel.value = 1
  teamTitle.value = "下线（" + (row.phone || row.memberId) + "）"
  teamOpen.value = true
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
