<template>
  <div class="app-container ops-page">
    <el-alert
      title="三方拉单与回调原文。正常流程：拉单（待付）→ 支付完成回调；App 查单补状态不落库。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="类型" prop="logType">
        <el-select v-model="queryParams.logType" placeholder="类型" clearable style="width: 120px">
          <el-option label="调用" value="CALL" />
          <el-option label="回调" value="CALLBACK" />
        </el-select>
      </el-form-item>
      <el-form-item label="动作" prop="action">
        <el-select v-model="queryParams.action" placeholder="动作" clearable style="width: 120px">
          <el-option label="拉单" value="create" />
          <el-option label="回调" value="notify" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务商" prop="providerCode">
        <el-select v-model="queryParams.providerCode" placeholder="服务商" clearable style="width: 160px">
          <el-option
            v-for="p in providers"
            :key="p.providerCode"
            :label="p.providerName || p.providerCode"
            :value="p.providerCode"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="商户单号" prop="outTradeNo">
        <el-input v-model="queryParams.outTradeNo" placeholder="商户单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="结果" prop="success">
        <el-select v-model="queryParams.success" placeholder="结果" clearable style="width: 110px">
          <el-option label="成功" value="1" />
          <el-option label="失败" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="logId" width="70" />
      <el-table-column label="类型" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.logType === 'CALLBACK'" type="warning">回调</el-tag>
          <el-tag v-else type="info">调用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="动作" align="center" width="80">
        <template #default="scope">{{ actionLabel(scope.row.action) }}</template>
      </el-table-column>
      <el-table-column label="服务商" align="center" min-width="100" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.providerName || scope.row.providerCode }}</template>
      </el-table-column>
      <el-table-column label="通道" align="center" min-width="120" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.channelName || scope.row.channelCode || '-' }}</template>
      </el-table-column>
      <el-table-column label="会员" align="center" width="130" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.phone || (scope.row.memberId ? '#' + scope.row.memberId : '-') }}</template>
      </el-table-column>
      <el-table-column label="商户单号" align="center" prop="outTradeNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="结果" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.success === '1' ? 'success' : 'danger'">{{ scope.row.success === '1' ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="HTTP" align="center" prop="httpStatus" width="70" />
      <el-table-column label="耗时ms" align="center" prop="costMs" width="80" />
      <el-table-column label="错误" align="center" prop="errorMsg" min-width="160" show-overflow-tooltip />
      <el-table-column label="时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template #default="scope">
          <el-button link type="primary" v-hasPermi="['biz:payGatewayLog:query']" @click="openDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-drawer v-model="detailOpen" title="支付日志详情" size="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="类型">{{ detail.logType === 'CALLBACK' ? '回调' : '调用' }} / {{ actionLabel(detail.action) }}</el-descriptions-item>
        <el-descriptions-item label="服务商">{{ detail.providerName || detail.providerCode }}</el-descriptions-item>
        <el-descriptions-item label="通道">{{ detail.channelName || detail.channelCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会员">{{ detail.phone || (detail.memberId ? '#' + detail.memberId : '-') }}</el-descriptions-item>
        <el-descriptions-item label="商户单号">{{ detail.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detail.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="来源IP">{{ detail.clientIp }}</el-descriptions-item>
        <el-descriptions-item label="HTTP">{{ detail.httpStatus }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costMs }} ms</el-descriptions-item>
        <el-descriptions-item label="错误">{{ detail.errorMsg }}</el-descriptions-item>
      </el-descriptions>
      <div class="mt12"><strong>请求体</strong></div>
      <el-input type="textarea" :rows="8" readonly :model-value="detail.requestBody || ''" />
      <div class="mt12"><strong>响应/回调</strong></div>
      <el-input type="textarea" :rows="8" readonly :model-value="detail.responseBody || ''" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="BizPayGatewayLog">
import { listPayGatewayLog, getPayGatewayLog, listPayProvider } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const providers = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const detail = ref<any>({})
const queryParams = ref({
  pageNum: 1,
  pageSize: 50,
  logType: undefined,
  action: undefined,
  providerCode: undefined,
  memberId: undefined as number | undefined,
  outTradeNo: undefined,
  success: undefined
})

function actionLabel(action?: string) {
  if (action === 'create') return '拉单'
  if (action === 'query') return '查单'
  if (action === 'notify') return '回调'
  return action || '-'
}

function getList() {
  loading.value = true
  listPayGatewayLog(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function openDetail(row: any) {
  getPayGatewayLog(row.logId).then((res: any) => {
    detail.value = res.data || row
    detailOpen.value = true
  })
}
listPayProvider().then((res: any) => { providers.value = res.data || [] })
getList()
</script>

<style scoped>
.mt12 { margin-top: 12px; margin-bottom: 6px; }
</style>
