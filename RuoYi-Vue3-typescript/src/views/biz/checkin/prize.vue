<template>
  <div class="app-container ops-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="奖品" prop="prizeName">
        <el-input v-model="queryParams.prizeName" placeholder="奖品名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="是否中奖" prop="won">
        <el-select v-model="queryParams.won" placeholder="全部" clearable style="width: 140px">
          <el-option label="已中奖" value="1" />
          <el-option label="未中奖" value="0" />
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
      <el-table-column label="ID" align="center" prop="prizeLogId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="签到ID" align="center" prop="checkinId" width="90" />
      <el-table-column label="连续天数" align="center" prop="streakDays" width="100" />
      <el-table-column label="奖品" align="center" prop="prizeName" />
      <el-table-column label="结果" align="center" prop="won" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.won === '1' ? 'success' : 'info'">{{ scope.row.won === '1' ? '已中奖' : '未中奖' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizCheckinPrize">
import { listCheckinPrize } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 100, memberId: undefined, phone: undefined, prizeName: undefined, won: undefined })

function getList() {
  loading.value = true
  listCheckinPrize(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
getList()
</script>
