<template>
  <div class="app-container">
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
      <el-table-column label="ID" align="center" prop="commissionId" width="80" />
      <el-table-column label="来源会员" align="center" prop="fromMemberId" width="90" />
      <el-table-column label="来源手机" align="center" prop="fromPhone" width="120" />
      <el-table-column label="获得会员" align="center" prop="toMemberId" width="90" />
      <el-table-column label="获得手机" align="center" prop="toPhone" width="120" />
      <el-table-column label="层级" align="center" prop="teamLevel" width="70" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="本金" align="center" prop="baseAmount" width="90" />
      <el-table-column label="比例%" align="center" prop="rate" width="80" />
      <el-table-column label="分佣" align="center" prop="amount" width="90" />
      <el-table-column label="时间" align="center" prop="createTime" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizCommission">
import { listCommission } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, memberId: undefined, phone: undefined })

function getList() {
  loading.value = true
  listCommission(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
getList()
</script>
