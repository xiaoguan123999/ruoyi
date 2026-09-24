<template>
  <div class="app-container ops-page">
    <el-alert
      title="查询大转盘抽奖流水。可按活动、会员、手机号及是否熔断筛选。「必中」=命中了必中策略；「熔断」=目标库存不足且兜底也失败。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="活动" prop="activityId">
        <el-select v-model="queryParams.activityId" placeholder="全部活动" clearable filterable style="width: 200px">
          <el-option v-for="item in activityOptions" :key="item.activityId" :label="item.title" :value="item.activityId" />
        </el-select>
      </el-form-item>
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="熔断" prop="isMeltdown">
        <el-select v-model="queryParams.isMeltdown" placeholder="全部" clearable style="width: 120px">
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
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
      <el-table-column label="流水ID" align="center" prop="recordId" width="90" />
      <el-table-column label="活动" align="left" min-width="140" show-overflow-tooltip>
        <template #default="scope">{{ activityTitle(scope.row.activityId) }}</template>
      </el-table-column>
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="次数" align="center" prop="drawCount" width="70" />
      <el-table-column label="奖品" align="left" prop="prizeName" min-width="120" show-overflow-tooltip />
      <el-table-column label="库存类型" align="center" prop="poolType" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.poolType === 'EXCLUSIVE' ? 'warning' : 'info'">{{ poolTypeLabel(scope.row.poolType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="必中" align="center" prop="isStrategy" width="70">
        <template #default="scope">
          <el-tag :type="scope.row.isStrategy === '1' ? 'success' : 'info'">{{ scope.row.isStrategy === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="熔断" align="center" prop="isMeltdown" width="70">
        <template #default="scope">
          <el-tag :type="scope.row.isMeltdown === '1' ? 'danger' : 'info'">{{ scope.row.isMeltdown === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发放状态" align="center" prop="grantStatus" width="100">
        <template #default="scope">{{ grantStatusLabel(scope.row.grantStatus) }}</template>
      </el-table-column>
      <el-table-column label="时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizLotteryRecord">
import { listLotteryRecord, listLotteryActivity } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activityOptions = ref<any[]>([])
const queryParams = ref({
  pageNum: 1,
  pageSize: 100,
  activityId: undefined as number | undefined,
  memberId: undefined as number | undefined,
  phone: undefined as string | undefined,
  isMeltdown: undefined as string | undefined
})

const activityMap = computed(() => {
  const map: Record<number, string> = {}
  activityOptions.value.forEach((a) => {
    map[a.activityId] = a.title
  })
  return map
})

function activityTitle(activityId: number) {
  return activityMap.value[activityId] || ("ID:" + activityId)
}

function poolTypeLabel(poolType: string) {
  if (poolType === "EXCLUSIVE") return "必中库存"
  if (poolType === "PUBLIC") return "普通库存"
  return poolType || "—"
}

function grantStatusLabel(status: string) {
  const map: Record<string, string> = {
    "0": "待处理",
    "1": "已入账",
    "2": "待领取",
    "3": "已关闭"
  }
  return map[status] || status || "—"
}

function getList() {
  loading.value = true
  listLotteryRecord(queryParams.value).then((res: any) => {
    dataList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function loadActivityOptions() {
  listLotteryActivity({ pageNum: 1, pageSize: 500 }).then((res: any) => {
    activityOptions.value = res.rows || []
  }).catch(() => {
    activityOptions.value = []
  })
}

loadActivityOptions()
getList()
</script>
