<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员ID" prop="memberId">
        <el-input v-model="queryParams.memberId" placeholder="会员ID" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="queryParams.remark" placeholder="备注模糊搜索" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="币种" clearable style="width: 120px">
          <el-option label="CNY" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="业务类型" clearable style="width: 150px">
          <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
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
      <el-table-column label="编号" align="center" prop="logId" width="90" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="账号" align="center" prop="phone" width="120" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="类型" align="center" width="130">
        <template #default="scope">{{ bizTypeLabel(scope.row.bizType) }}</template>
      </el-table-column>
      <el-table-column label="金额" align="center" width="120">
        <template #default="scope">
          <span :style="{ color: Number(scope.row.amount) >= 0 ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
            {{ formatAmount(scope.row.amount) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="余额" align="center" prop="availableAfter" width="120" />
      <el-table-column label="操作时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizWalletLog">
import { listWalletLog } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref<string[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 10, memberId: undefined, phone: undefined, remark: undefined, currency: undefined, bizType: undefined })
const bizTypeOptions = [
  { value: "RECHARGE", label: "充值" },
  { value: "SUBSCRIBE", label: "认购" },
  { value: "REBATE", label: "产品日返" },
  { value: "COMMISSION", label: "推广奖金" },
  { value: "INVITE", label: "推广奖励" },
  { value: "CHECKIN", label: "签到" },
  { value: "KYC_REWARD", label: "实名注册奖励" },
  { value: "LEVEL_REWARD", label: "等级奖励" },
  { value: "WITHDRAW_FREEZE", label: "提现" },
  { value: "WITHDRAW_SUCCESS", label: "提现成功" },
  { value: "WITHDRAW_REJECT", label: "提现退回" }
]
const bizTypeMap: Record<string, string> = Object.fromEntries(bizTypeOptions.map(item => [item.value, item.label]))

function bizTypeLabel(bizType: string) {
  return bizTypeMap[bizType] || bizType || "—"
}
function formatAmount(amount: any) {
  const n = Number(amount)
  if (!Number.isFinite(n)) return amount ?? "—"
  return (n >= 0 ? "+" : "") + n
}
function getList() {
  loading.value = true
  listWalletLog(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
getList()
</script>
