<template>
  <div class="app-container ops-page">
    <el-alert
      title="会员提交提现时可用已扣到冻结；确认打款只减冻结，可用不会再变。请看「可用前/后」和「冻结前/后」。"
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
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="queryParams.remark" placeholder="备注模糊搜索" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="钱包" prop="typeCode">
        <el-select v-model="queryParams.typeCode" placeholder="钱包类型" clearable style="width: 140px">
          <el-option v-for="item in typeOptions" :key="item.typeCode" :label="item.typeName" :value="item.typeCode" />
        </el-select>
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
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Wallet" @click="openAdjust" v-hasPermi="['biz:wallet:adjust']">调账</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="编号" align="center" prop="logId" width="90" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="账号" align="center" prop="phone" width="120" />
      <el-table-column label="钱包" align="center" width="110">
        <template #default="scope">{{ scope.row.typeName || scope.row.typeCode || "—" }}</template>
      </el-table-column>
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="类型" align="center" width="130">
        <template #default="scope">{{ bizTypeLabel(scope.row.bizType) }}</template>
      </el-table-column>
      <el-table-column label="金额" align="center" width="110">
        <template #default="scope">
          <span :style="{ color: Number(scope.row.amount) >= 0 ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
            {{ formatAmount(scope.row.amount) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="可用前" align="center" width="100">
        <template #default="scope">{{ formatMoney(scope.row.availableBefore) }}</template>
      </el-table-column>
      <el-table-column label="可用后" align="center" width="100">
        <template #default="scope">{{ formatMoney(scope.row.availableAfter) }}</template>
      </el-table-column>
      <el-table-column label="冻结前" align="center" width="100">
        <template #default="scope">{{ formatMoney(scope.row.frozenBefore) }}</template>
      </el-table-column>
      <el-table-column label="冻结后" align="center" width="100">
        <template #default="scope">{{ formatMoney(scope.row.frozenAfter) }}</template>
      </el-table-column>
      <el-table-column label="操作时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    <WalletAdjustDialog v-model="adjustOpen" @success="getList" />
  </div>
</template>

<script setup lang="ts" name="BizWalletLog">
import { listWalletLog, listWalletTypeOptions } from "@/api/biz"
import WalletAdjustDialog from "@/views/biz/components/WalletAdjustDialog.vue"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref<string[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 100, memberId: undefined, phone: undefined, remark: undefined, currency: undefined, bizType: undefined, typeCode: undefined })
const typeOptions = ref<any[]>([])
const adjustOpen = ref(false)
function openAdjust() { adjustOpen.value = true }
const bizTypeOptions = [
  { value: "RECHARGE", label: "充值" },
  { value: "SUBSCRIBE", label: "认购" },
  { value: "REBATE", label: "产品日返" },
  { value: "COMMISSION", label: "推广奖金" },
  { value: "INVITE", label: "推广奖励" },
  { value: "ADJUST", label: "后台调账" },
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
function formatMoney(value: any) {
  if (value === null || value === undefined || value === "") return "—"
  const n = Number(value)
  if (!Number.isFinite(n)) return value
  return n
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
listWalletTypeOptions().then((res: any) => { typeOptions.value = res.data || [] })
</script>
