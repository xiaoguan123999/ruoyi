<template>
  <div class="app-container ops-page">
    <el-alert
      title="通道给 App 选支付方式。productId 是三方产品码（福旺 wayCode / 无忧 8000·8001 / 非凡 13·15）。下单和回调在后端，本页只改通道配置。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="服务商" prop="providerCode">
        <el-select v-model="queryParams.providerCode" placeholder="服务商" clearable style="width: 140px">
          <el-option v-for="p in providers" :key="p.providerCode" :label="p.providerName" :value="p.providerCode" />
        </el-select>
      </el-form-item>
      <el-form-item label="场景" prop="scene">
        <el-select v-model="queryParams.scene" placeholder="场景" clearable style="width: 140px">
          <el-option label="支付宝" value="alipay" />
          <el-option label="微信" value="wechat" />
          <el-option label="银联" value="union" />
          <el-option label="USDT" value="usdt" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="服务商" align="center" prop="providerName" width="90" />
      <el-table-column label="通道" align="center" prop="displayName" min-width="120">
        <template #default="scope">{{ scope.row.displayName || scope.row.channelName }}</template>
      </el-table-column>
      <el-table-column label="编码" align="center" prop="channelCode" min-width="140" show-overflow-tooltip />
      <el-table-column label="产品码" align="center" prop="productId" min-width="110" show-overflow-tooltip />
      <el-table-column label="场景" align="center" prop="scene" width="90" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="限额" align="center" min-width="140">
        <template #default="scope">{{ scope.row.minAmount }} ~ {{ scope.row.maxAmount || "不限" }}</template>
      </el-table-column>
      <el-table-column label="权重" align="center" prop="weight" width="70" />
      <el-table-column label="模拟" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.mockMode === '1' ? 'warning' : 'success'">{{ scope.row.mockMode === '1' ? '模拟' : '真实' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['biz:payChannel:edit']">改通道</el-button>
          <el-button link type="primary" @click="handleProvider(scope.row)" v-hasPermi="['biz:payProvider:edit']">改服务商</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="修改通道" v-model="open" width="480px" append-to-body>
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="展示名"><el-input v-model="form.displayName" /></el-form-item>
        <el-form-item label="产品码">
          <el-input v-model="form.productId" placeholder="三方产品码，如 ALI_QR / 8000 / 13" />
        </el-form-item>
        <el-form-item label="最小金额"><el-input v-model="form.minAmount" /></el-form-item>
        <el-form-item label="最大金额"><el-input v-model="form.maxAmount" placeholder="空表示不限" /></el-form-item>
        <el-form-item label="权重"><el-input v-model="form.weight" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitChannel">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="修改服务商" v-model="providerOpen" width="520px" append-to-body>
      <el-form :model="providerForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="providerForm.providerName" /></el-form-item>
        <el-form-item label="协议">
          <el-select v-model="providerForm.adapterFamily" placeholder="adapterFamily" style="width: 100%">
            <el-option label="jeepay（福旺）" value="jeepay" />
            <el-option label="wuyou（无忧）" value="wuyou" />
            <el-option label="monpay（非凡）" value="monpay" />
          </el-select>
        </el-form-item>
        <el-form-item label="网关"><el-input v-model="providerForm.gatewayUrl" placeholder="真实网关地址" /></el-form-item>
        <el-form-item label="商户号"><el-input v-model="providerForm.appId" /></el-form-item>
        <el-form-item label="密钥"><el-input v-model="providerForm.secretKey" placeholder="不改请留空" show-password /></el-form-item>
        <el-form-item label="回调 IP">
          <el-input v-model="providerForm.callbackIps" placeholder="逗号分隔，空则不校验来源 IP" />
        </el-form-item>
        <el-form-item label="模式">
          <el-radio-group v-model="providerForm.mockMode">
            <el-radio value="1">模拟</el-radio>
            <el-radio value="0">真实</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="providerForm.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitProvider">确 定</el-button>
        <el-button @click="providerOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizPayChannel">
import { listPayChannel, getPayChannel, updatePayChannel, listPayProvider, getPayProvider, updatePayProvider } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const providers = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const providerOpen = ref(false)
const form = ref<any>({})
const providerForm = ref<any>({})
const queryParams = ref({ pageNum: 1, pageSize: 100, providerCode: undefined, scene: undefined, status: undefined })

function getList() {
  loading.value = true
  listPayChannel(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleUpdate(row: any) {
  getPayChannel(row.channelId).then((res: any) => {
    form.value = res.data
    open.value = true
  })
}
function submitChannel() {
  updatePayChannel(form.value).then(() => {
    proxy.$modal.msgSuccess("已保存")
    open.value = false
    getList()
  })
}
function handleProvider(row: any) {
  const hit = providers.value.find((p: any) => p.providerCode === row.providerCode)
  if (hit?.providerId) {
    getPayProvider(hit.providerId).then((res: any) => {
      const data = res.data || {}
      providerForm.value = { ...data, secretKey: "" }
      providerOpen.value = true
    })
    return
  }
  providerForm.value = { ...(hit || {}), secretKey: "" }
  providerOpen.value = true
}
function submitProvider() {
  const payload = { ...providerForm.value }
  if (!payload.secretKey || payload.secretKey === "******") {
    payload.secretKey = ""
  }
  updatePayProvider(payload).then(() => {
    proxy.$modal.msgSuccess("已保存")
    providerOpen.value = false
    listPayProvider().then((res: any) => { providers.value = res.data || [] })
    getList()
  })
}
listPayProvider().then((res: any) => { providers.value = res.data || [] })
getList()
</script>
