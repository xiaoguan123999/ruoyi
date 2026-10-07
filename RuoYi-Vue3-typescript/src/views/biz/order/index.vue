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
      <el-table-column label="数量" align="center" prop="quantity" width="70" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="本金" align="center" prop="price" width="90" />
      <el-table-column label="日返" align="center" prop="dailyRebate" width="80" />
      <el-table-column label="累计金额" align="center" min-width="110">
        <template #default="scope">
          <span v-if="scope.row.incomeMode === 'ACCUMULATE' || scope.row.incomeMode === 'PROTECT'">
            {{ scope.row.accumulatedAmount ?? 0 }}
            <el-tag v-if="scope.row.incomeMode === 'ACCUMULATE' && scope.row.accumulatePaused === '1'" type="warning" size="small" style="margin-left: 4px">暂停</el-tag>
          </span>
          <span v-else style="color: #909399">—</span>
        </template>
      </el-table-column>
      <el-table-column label="累计进度" align="center" width="100">
        <template #default="scope">
          <span v-if="scope.row.incomeMode === 'ACCUMULATE'">
            {{ scope.row.accumulateDays || 0 }}/{{ scope.row.accumulateCycleDays || 0 }}
          </span>
          <span v-else style="color: #909399">—</span>
        </template>
      </el-table-column>
      <el-table-column label="进度" align="center" width="90">
        <template #default="scope">{{ progressText(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="到期日" align="center" width="120">
        <template #default="scope">{{ expireDate(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="激活" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.activateStatus === '1' ? 'success' : 'info'">{{ scope.row.activateStatus === '1' ? '已激活' : '未激活' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="一拖二" align="center" min-width="110">
        <template #default="scope">
          <span>{{ unlockProgress(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="orderStatusType(scope.row)">{{ orderStatusText(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收益开始" align="center" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.incomeStartTime) || "—" }}</span></template>
      </el-table-column>
      <el-table-column label="申购时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-drawer
      v-model="detailOpen"
      title="认购订单详情"
      size="720px"
      append-to-body
      destroy-on-close
      class="order-detail-drawer"
    >
      <div v-loading="detailLoading" class="drawer-body">
        <el-tabs v-model="detailTab" class="detail-tabs">
          <el-tab-pane label="订单信息" name="order">
            <div class="tab-scroll">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="订单号" :span="2">{{ detail.orderNo || "—" }}</el-descriptions-item>
                <el-descriptions-item label="会员ID">{{ detail.memberId ?? "—" }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ detail.phone || "—" }}</el-descriptions-item>
                <el-descriptions-item label="系列">{{ detail.categoryName || "—" }}</el-descriptions-item>
                <el-descriptions-item label="产品">{{ detail.productName || "—" }}</el-descriptions-item>
                <el-descriptions-item label="币种">{{ detail.currency || "—" }}</el-descriptions-item>
                <el-descriptions-item label="数量">{{ detail.quantity ?? "—" }}</el-descriptions-item>
                <el-descriptions-item label="本金">{{ detail.price ?? "—" }}</el-descriptions-item>
                <el-descriptions-item label="日返">{{ detail.dailyRebate ?? "—" }}</el-descriptions-item>
                <el-descriptions-item label="进度">{{ progressText(detail) }}</el-descriptions-item>
                <el-descriptions-item label="到期日">{{ expireDate(detail) }}</el-descriptions-item>
                <el-descriptions-item label="激活">
                  <el-tag :type="detail.activateStatus === '1' ? 'success' : 'info'" size="small">
                    {{ detail.activateStatus === '1' ? '已激活' : '未激活' }}
                    <template v-if="detail.activatedQty != null"> {{ detail.activatedQty }}/{{ detail.quantity || 1 }}</template>
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="一拖二">{{ unlockProgress(detail) }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="orderStatusType(detail)" size="small">{{ orderStatusText(detail) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="收益开始">{{ parseTime(detail.incomeStartTime) || "—" }}</el-descriptions-item>
                <el-descriptions-item label="申购时间" :span="2">{{ parseTime(detail.createTime) || "—" }}</el-descriptions-item>
                <el-descriptions-item v-if="showAccumulate(detail)" label="累计金额">
                  {{ detail.accumulatedAmount ?? 0 }}
                  <el-tag v-if="detail.incomeMode === 'ACCUMULATE' && detail.accumulatePaused === '1'" type="warning" size="small" style="margin-left: 4px">暂停</el-tag>
                </el-descriptions-item>
                <el-descriptions-item v-if="detail.incomeMode === 'ACCUMULATE'" label="累计进度">
                  {{ detail.accumulateDays || 0 }}/{{ detail.accumulateCycleDays || 0 }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>

          <el-tab-pane label="产品快照" name="snapshot">
            <div class="tab-scroll">
              <p class="ops-hint mb8">下单时写入订单，点「同步快照」后会按产品最新配置更新</p>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="入账方式" :span="2">{{ incomeModeText(detail.incomeMode) }}</el-descriptions-item>
                <el-descriptions-item label="保护天数">{{ detail.protectDays ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="累计周期">{{ detail.accumulateCycleDays ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="一拖二份数">{{ detail.unlockDirectQty ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="等待小时">{{ detail.unlockDelayHours ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="总天数">{{ detail.durationDays ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="剩余天数">{{ detail.remainingDays ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="提现指定">{{ yesNo(detail.withdrawRequired) }}</el-descriptions-item>
                <el-descriptions-item label="对档产品">{{ detail.relatedProductName || detail.relatedProductId || "无" }}</el-descriptions-item>
                <el-descriptions-item v-if="detail.bizMode === 'ASSIST'" label="助力值">{{ detail.assistValue ?? 0 }}</el-descriptions-item>
                <el-descriptions-item v-if="detail.bizMode === 'ASSIST'" label="本金返还天数">{{ detail.principalReturnDays ?? 0 }}</el-descriptions-item>
                <el-descriptions-item v-if="detail.bizMode === 'ASSIST'" label="返还时间" :span="2">{{ parseTime(detail.principalReturnAt) || "—" }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>

          <el-tab-pane name="rebate">
            <template #label>
              收益发放
              <el-badge v-if="rebateList.length" :value="rebateList.length" :max="999" class="rebate-badge" />
            </template>
            <div class="rebate-pane">
              <div class="rebate-summary">
                共 {{ rebateList.length }} 笔，合计 <b>{{ rebateSum }}</b> {{ detail.currency || "" }}
              </div>
              <el-table
                :data="rebatePageRows"
                size="small"
                border
                height="100%"
                class="rebate-table"
                empty-text="暂无发放记录"
              >
                <el-table-column label="发放日期" align="center" prop="rebateDate" width="120">
                  <template #default="scope">{{ formatRebateDate(scope.row) }}</template>
                </el-table-column>
                <el-table-column label="金额" align="center" prop="amount" width="120" />
                <el-table-column label="币种" align="center" prop="currency" width="80" />
                <el-table-column label="入账时间" align="center" prop="createTime" min-width="160">
                  <template #default="scope">{{ formatRebateTime(scope.row) }}</template>
                </el-table-column>
              </el-table>
              <div class="rebate-pager" v-if="rebateList.length > rebatePageSize">
                <el-pagination
                  v-model:current-page="rebatePage"
                  :page-size="rebatePageSize"
                  :total="rebateList.length"
                  layout="total, prev, pager, next"
                  small
                  background
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
          <el-button type="primary" :loading="detailLoading" @click="reloadDetail">刷 新</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="BizOrder">
import { getOrder, listOrder, listOrderRebates, listProductCategoryOptions } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref<string[]>([])
const categoryOptions = ref<any[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 100, orderNo: undefined, memberId: undefined, phone: undefined, categoryId: undefined, currency: undefined, status: undefined })

const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref<any>({})
const rebateList = ref<any[]>([])
const detailOrderId = ref<number | string | null>(null)
const detailTab = ref("order")
const rebatePage = ref(1)
const rebatePageSize = 20

const rebateSum = computed(() => {
  let sum = 0
  for (const row of rebateList.value) {
    sum += Number(row?.amount || 0)
  }
  return Number(sum.toFixed(4))
})

const rebatePageRows = computed(() => {
  const start = (rebatePage.value - 1) * rebatePageSize
  return rebateList.value.slice(start, start + rebatePageSize)
})

function progressText(row: any) {
  if (!row) return "—"
  const totalDays = Number(row.durationDays) || 0
  const remain = Number(row.remainingDays) || 0
  const done = Math.max(0, totalDays - remain)
  return done + "/" + totalDays
}
function unlockProgress(row: any) {
  if (!row) return "—"
  const need = Number(row.unlockDirectQty || 0)
  if (need <= 0) return "—"
  const have = Number(row.unlockDirectHave || 0)
  return have + "/" + need + "份"
}
function orderStatusText(row: any) {
  if (!row) return "—"
  if (row.status === "1") return "已完成"
  if (row.activateStatus === "1") return "收益中"
  return "未激活"
}
function orderStatusType(row: any) {
  if (!row) return "info"
  if (row.status === "1") return "success"
  if (row.activateStatus === "1") return "warning"
  return "info"
}
function expireDate(row: any) {
  if (!row) return "—"
  if (row.activateStatus !== "1") return "待激活"
  const start = row.incomeStartTime || row.createTime
  if (!start || !row.durationDays) return "—"
  const raw = String(start).replace(/-/g, "/")
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return "—"
  d.setDate(d.getDate() + Number(row.durationDays))
  return proxy.parseTime(d, "{y}-{m}-{d}")
}
function incomeModeText(mode: any) {
  const m = String(mode || "CREDIT").toUpperCase()
  if (m === "PROTECT") return "保护期 + 累计池"
  if (m === "ACCUMULATE") return "订单累计后结算"
  return "每天进产品收益"
}
function yesNo(v: any) {
  return String(v) === "1" ? "是" : "否"
}
function showAccumulate(row: any) {
  const m = String(row?.incomeMode || "").toUpperCase()
  return m === "ACCUMULATE" || m === "PROTECT"
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

function openDetail(row: any) {
  const orderId = row?.orderId
  if (!orderId) return
  detailOrderId.value = orderId
  detailTab.value = "order"
  rebatePage.value = 1
  detailOpen.value = true
  reloadDetail()
}

function reloadDetail() {
  const orderId = detailOrderId.value
  if (!orderId) return
  detailLoading.value = true
  Promise.all([getOrder(orderId), listOrderRebates(orderId)]).then(([orderRes, rebateRes]: any[]) => {
    detail.value = orderRes?.data || {}
    rebateList.value = Array.isArray(rebateRes?.data) ? rebateRes.data : []
    const maxPage = Math.max(1, Math.ceil(rebateList.value.length / rebatePageSize) || 1)
    if (rebatePage.value > maxPage) rebatePage.value = maxPage
  }).finally(() => {
    detailLoading.value = false
  })
}

listProductCategoryOptions().then((res: any) => {
  categoryOptions.value = res.data || []
})
getList()
</script>

<style scoped>
.order-detail-drawer :deep(.el-drawer__body) {
  padding: 0 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.drawer-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.detail-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}
.detail-tabs :deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.detail-tabs :deep(.el-tab-pane) {
  height: 100%;
}
.tab-scroll {
  height: calc(100vh - 220px);
  max-height: 640px;
  overflow: auto;
  padding-right: 4px;
}
.ops-hint {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}
.mb8 {
  margin-bottom: 8px;
}
.rebate-badge {
  margin-left: 4px;
}
.rebate-badge :deep(.el-badge__content) {
  transform: translateY(-2px);
}
.rebate-pane {
  height: calc(100vh - 220px);
  max-height: 640px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.rebate-summary {
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}
.rebate-table {
  flex: 1;
  min-height: 0;
}
.rebate-pager {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
