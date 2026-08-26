<template>
  <div class="app-container ops-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="来源或收款手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="币种" clearable style="width: 120px">
          <el-option label="CNY" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="层级" prop="teamLevel">
        <el-select v-model="queryParams.teamLevel" placeholder="层级" clearable style="width: 120px">
          <el-option v-for="n in 3" :key="n" :label="n + '级'" :value="n" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
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
      <el-table-column label="编号" align="center" prop="commissionId" width="80" />
      <el-table-column label="收款会员" align="center" min-width="160">
        <template #default="scope">{{ scope.row.toMemberId }} / {{ scope.row.toPhone || "—" }}</template>
      </el-table-column>
      <el-table-column label="来源会员" align="center" min-width="160">
        <template #default="scope">{{ scope.row.fromMemberId }} / {{ scope.row.fromPhone || "—" }}</template>
      </el-table-column>
      <el-table-column label="层级" align="center" prop="teamLevel" width="70" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="本金" align="center" prop="baseAmount" min-width="110" />
      <el-table-column label="比例%" align="center" prop="rate" width="80" />
      <el-table-column label="佣金金额" align="center" prop="amount" min-width="120" />
      <el-table-column label="发放时间" align="center" prop="createTime" min-width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" align="center" min-width="220" show-overflow-tooltip>
        <template #default="scope">{{ commissionRemark(scope.row) }}</template>
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
const dateRange = ref<string[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 100, memberId: undefined, phone: undefined, currency: undefined, teamLevel: undefined })

function commissionRemark(row: any) {
  const phone = row.fromPhone || row.fromMemberId || "来源会员"
  const base = row.baseAmount ?? 0
  const level = row.teamLevel ?? ""
  const kind = row.orderId ? "认购" : (row.rechargeId ? "充值" : "认购")
  return phone + " " + kind + " " + base + " 的 " + level + " 级分佣"
}
function getList() {
  loading.value = true
  listCommission(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
getList()
</script>
