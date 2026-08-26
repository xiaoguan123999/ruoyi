<template>
  <div class="app-container">
    <el-alert
      title="供应商即代收服务商（百付/宝利/牛付/沙付/宝利U）。当前全部是模拟占位，不要把其他项目的真实网关和密钥填进来。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="编码" prop="providerCode">
        <el-input v-model="queryParams.providerCode" placeholder="编码" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="名称" prop="providerName">
        <el-input v-model="queryParams.providerName" placeholder="名称" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="模式" prop="mockMode">
        <el-select v-model="queryParams.mockMode" placeholder="模式" clearable style="width: 120px">
          <el-option label="模拟" value="1" />
          <el-option label="真实" value="0" />
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
      <el-table-column label="编码" align="center" prop="providerCode" width="110" />
      <el-table-column label="名称" align="center" prop="providerName" width="110" />
      <el-table-column label="网关" align="center" prop="gatewayUrl" min-width="220" show-overflow-tooltip />
      <el-table-column label="商户号" align="center" prop="appId" min-width="140" show-overflow-tooltip />
      <el-table-column label="密钥" align="center" prop="secretKey" width="100" />
      <el-table-column label="模式" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.mockMode === '1' ? 'warning' : 'success'">{{ scope.row.mockMode === '1' ? '模拟' : '真实' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortOrder" width="70" />
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['biz:payProvider:edit']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="修改供应商" v-model="open" width="520px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="编码"><el-input v-model="form.providerCode" disabled /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.providerName" /></el-form-item>
        <el-form-item label="网关"><el-input v-model="form.gatewayUrl" placeholder="模拟态用 mock.pay.local，真实接入后再改" /></el-form-item>
        <el-form-item label="商户号"><el-input v-model="form.appId" /></el-form-item>
        <el-form-item label="密钥"><el-input v-model="form.secretKey" placeholder="不改请留空" show-password /></el-form-item>
        <el-form-item label="模式">
          <el-radio-group v-model="form.mockMode">
            <el-radio value="1">模拟</el-radio>
            <el-radio value="0">真实（尚未接线）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序"><el-input v-model="form.sortOrder" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submit">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizPayProvider">
import { listPayProviderPage, getPayProvider, updatePayProvider } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const form = ref<any>({})
const queryParams = ref({ pageNum: 1, pageSize: 10, providerCode: undefined, providerName: undefined, mockMode: undefined, status: undefined })

function getList() {
  loading.value = true
  listPayProviderPage(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleUpdate(row: any) {
  getPayProvider(row.providerId).then((res: any) => {
    const data = res.data || {}
    form.value = { ...data, secretKey: "" }
    open.value = true
  })
}
function submit() {
  const payload = { ...form.value }
  if (!payload.secretKey || payload.secretKey === "******") {
    payload.secretKey = ""
  }
  updatePayProvider(payload).then(() => {
    proxy.$modal.msgSuccess("已保存")
    open.value = false
    getList()
  })
}
getList()
</script>
