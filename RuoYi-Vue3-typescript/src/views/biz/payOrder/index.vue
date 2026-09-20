<template>
  <div class="app-container ops-page">
    <div class="ops-section-card">
      <div class="ops-section-card__hd">待付超时</div>
      <div class="ops-section-card__bd">
        <el-form :inline="true" v-loading="expireLoading">
          <el-form-item label="超时分钟">
            <el-input-number v-model="expireMinutes" :min="1" :max="1440" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveExpire" v-hasPermi="['biz:payOrder:list']">保存</el-button>
          </el-form-item>
        </el-form>
        <div class="tip">拉起收银台未付，超过该时间自动关闭。定时任务每 5 分钟扫一次；</div>
      </div>
    </div>
    <el-alert
      title="线上代收单。未付超时自动关闭；待付可「查单补单」；超时关单后若用户仍付款，回调仍可到账。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="商户单号" prop="outTradeNo">
        <el-input v-model="queryParams.outTradeNo" placeholder="商户单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="待付" value="0" />
          <el-option label="成功" value="1" />
          <el-option label="失败" value="2" />
          <el-option label="关闭" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="商户单号" align="center" prop="outTradeNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="会员" align="center" prop="phone" width="120" />
      <el-table-column label="服务商" align="center" prop="providerName" width="90" />
      <el-table-column label="通道" align="center" prop="channelName" min-width="110" />
      <el-table-column label="金额" align="center" width="120">
        <template #default="scope">{{ scope.row.amount }} {{ scope.row.currency }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="warning">待付</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">成功</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="danger">失败</el-tag>
          <el-tag v-else>关闭</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="模拟" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.mockMode === '1' ? 'warning' : 'info'">{{ scope.row.mockMode === '1' ? '模拟' : '真实' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="primary"
            v-hasPermi="['biz:payOrder:query']"
            @click="handleSync(scope.row)"
          >查单补单</el-button>
          <el-button
            v-if="scope.row.status === '0' && scope.row.mockMode === '1'"
            link
            type="primary"
            v-hasPermi="['biz:payOrder:simulate']"
            @click="handleSimulate(scope.row)"
          >模拟到账</el-button>
          <el-button v-if="scope.row.payUrl" link type="primary" @click="openPay(scope.row.payUrl)">打开收银台</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizPayOrder">
import {
  listPayOrder,
  simulatePayOrder,
  syncPayOrder,
  getPayOrderExpireMinutes,
  savePayOrderExpireMinutes
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const expireLoading = ref(false)
const expireMinutes = ref(30)
const queryParams = ref({ pageNum: 1, pageSize: 100, outTradeNo: undefined, phone: undefined, status: undefined })

function loadExpire() {
  expireLoading.value = true
  getPayOrderExpireMinutes().then((res: any) => {
    expireMinutes.value = Number(res.data?.expireMinutes) || 30
  }).finally(() => { expireLoading.value = false })
}
function saveExpire() {
  savePayOrderExpireMinutes(expireMinutes.value).then(() => {
    proxy.$modal.msgSuccess("保存成功（仅对新拉单生效）")
    loadExpire()
  })
}

function getList() {
  loading.value = true
  listPayOrder(proxy.addDateRange(queryParams.value, [])).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function payStatusText(status?: string) {
  if (status === '1') return '成功'
  if (status === '2') return '失败'
  if (status === '3') return '关闭'
  return '待付'
}

function handleSync(row: any) {
  proxy.$modal.confirm("向三方查单并补单？").then(() => syncPayOrder(row.outTradeNo)).then((res: any) => {
    const order = res.data || {}
    const before = String(row.status ?? '')
    const after = String(order.status ?? '')
    let msg = ''
    let ok = false
    if (after === '1') {
      ok = true
      msg = before === '1' ? '查单完成：订单本来就是成功状态。' : '查单完成：三方已支付，已补单到账。'
    } else if (after === '3') {
      msg = before === '3'
        ? '查单完成：订单已是关闭状态，三方仍未支付。'
        : '查单完成：三方未支付且已超时，订单已关闭。'
    } else if (after === '2') {
      msg = '查单完成：三方返回失败，订单状态为失败。'
    } else {
      msg = '查单完成：三方仍为待付，未发生补单。'
    }
    msg += ` 商户单号 ${order.outTradeNo || row.outTradeNo}，状态 ${payStatusText(before)} → ${payStatusText(after)}`
    if (ok) {
      proxy.$modal.alertSuccess(msg)
    } else {
      proxy.$modal.alertWarning(msg)
    }
    getList()
  }).catch(() => {})
}
function handleSimulate(row: any) {
  proxy.$modal.confirm("确认模拟支付成功并入账？").then(() => simulatePayOrder(row.outTradeNo)).then(() => {
    proxy.$modal.msgSuccess("已到账")
    getList()
  }).catch(() => {})
}
function openPay(url: string) {
  window.open(url, "_blank")
}
loadExpire()
getList()
</script>

<style scoped>
.tip {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
</style>
