<template>
  <div class="app-container ops-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="系列" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="全部系列" clearable style="width: 180px">
          <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="币种" clearable style="width: 120px">
          <el-option label="CNY" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="持仓中" value="0" />
          <el-option label="已完成" value="1" />
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
      <el-table-column label="订单号" align="center" prop="orderNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="系列" align="center" prop="categoryName" min-width="120" show-overflow-tooltip />
      <el-table-column label="产品" align="center" prop="productName" min-width="120" show-overflow-tooltip />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="本金" align="center" prop="price" width="90" />
      <el-table-column label="日返" align="center" prop="dailyRebate" width="80" />
      <el-table-column label="进度" align="center" width="90">
        <template #default="scope">{{ progressText(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="到期日" align="center" width="120">
        <template #default="scope">{{ expireDate(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'warning' : 'success'">{{ scope.row.status === '0' ? '收益中' : '已完成' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申购时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizOrder">
import { listOrder, listProductCategoryOptions } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref<string[]>([])
const categoryOptions = ref<any[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 100, orderNo: undefined, memberId: undefined, phone: undefined, categoryId: undefined, currency: undefined, status: undefined })

function progressText(row: any) {
  const totalDays = Number(row.durationDays) || 0
  const remain = Number(row.remainingDays) || 0
  const done = Math.max(0, totalDays - remain)
  return done + "/" + totalDays
}
function expireDate(row: any) {
  if (!row.createTime || !row.durationDays) return "—"
  const raw = String(row.createTime).replace(/-/g, "/")
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return "—"
  d.setDate(d.getDate() + Number(row.durationDays))
  return proxy.parseTime(d, "{y}-{m}-{d}")
}
function getList() {
  loading.value = true
  listOrder(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
listProductCategoryOptions().then((res: any) => {
  categoryOptions.value = res.data || []
})
getList()
</script>
