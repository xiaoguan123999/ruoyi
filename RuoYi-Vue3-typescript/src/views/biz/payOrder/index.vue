<template>
  <div class="app-container">
    <el-alert
      title="线上代收单。模拟通道可点「模拟到账」，效果等同三方回调成功：充值自动审核入账。"
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
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
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
import { listPayOrder, simulatePayOrder } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, outTradeNo: undefined, phone: undefined, status: undefined })

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
function handleSimulate(row: any) {
  proxy.$modal.confirm("确认模拟支付成功并入账？").then(() => simulatePayOrder(row.outTradeNo)).then(() => {
    proxy.$modal.msgSuccess("已到账")
    getList()
  }).catch(() => {})
}
function openPay(url: string) {
  window.open(url, "_blank")
}
getList()
</script>
