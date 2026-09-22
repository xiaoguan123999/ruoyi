<template>
  <div class="app-container ops-page">
    <el-alert
      title="按会员查看各钱包类型（余额 / 产品收益 / 推广 / 助力）× 币种的可用与冻结。可从此页直接调账。"
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
      <el-form-item label="仅看有余额">
        <el-switch v-model="onlyNonZero" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Wallet" @click="openAdjust()" v-hasPermi="['biz:wallet:adjust']">调账</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="账号" align="center" prop="phone" width="130" />
      <el-table-column label="钱包" align="center" min-width="120">
        <template #default="scope">{{ scope.row.typeName || scope.row.typeCode || "—" }}</template>
      </el-table-column>
      <el-table-column label="类型编码" align="center" prop="typeCode" width="100" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="可用" align="center" width="120">
        <template #default="scope">
          <span style="font-weight: 600">{{ formatMoney(scope.row.available) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="冻结" align="center" width="120">
        <template #default="scope">{{ formatMoney(scope.row.frozen) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.updateTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Wallet"
            @click="openAdjust(scope.row)"
            v-hasPermi="['biz:wallet:adjust']"
          >调账</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    <WalletAdjustDialog
      v-model="adjustOpen"
      :member-id="adjustMemberId"
      :phone="adjustPhone"
      @success="getList"
    />
  </div>
</template>

<script setup lang="ts" name="BizWallet">
import { listWallet, listWalletTypeOptions } from "@/api/biz"
import WalletAdjustDialog from "@/views/biz/components/WalletAdjustDialog.vue"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const onlyNonZero = ref(false)
const typeOptions = ref<any[]>([])
const queryParams = ref({
  pageNum: 1,
  pageSize: 100,
  memberId: undefined as number | undefined,
  phone: undefined as string | undefined,
  typeCode: undefined as string | undefined,
  currency: undefined as string | undefined
})
const adjustOpen = ref(false)
const adjustMemberId = ref<number | undefined>()
const adjustPhone = ref("")

function formatMoney(val: any) {
  if (val == null || val === "") return "0"
  const n = Number(val)
  if (!Number.isFinite(n)) return String(val)
  return n.toFixed(4).replace(/\.?0+$/, "") || "0"
}

function getList() {
  loading.value = true
  const params: any = { ...queryParams.value }
  if (onlyNonZero.value) {
    params.params = { onlyNonZero: true }
  }
  listWallet(params).then((res: any) => {
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
  onlyNonZero.value = false
  handleQuery()
}

function openAdjust(row?: any) {
  if (row) {
    adjustMemberId.value = row.memberId
    adjustPhone.value = row.phone || ""
  } else {
    adjustMemberId.value = undefined
    adjustPhone.value = ""
  }
  adjustOpen.value = true
}

listWalletTypeOptions().then((res: any) => {
  typeOptions.value = res.data || []
}).catch(() => {
  typeOptions.value = []
})

const route = useRoute()
const mid = Number(route.query.memberId)
if (Number.isFinite(mid) && mid > 0) {
  queryParams.value.memberId = mid
}
getList()
</script>
