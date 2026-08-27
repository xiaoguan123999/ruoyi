<template>
  <div class="app-container ops-page">
    <el-alert
      title="这里是会员保存的 USDT / 银行卡 / 支付宝收款账户，给 App「钱包管理」和提现选用。不是 CNY/USDT 余额钱包。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="accountType">
        <el-select v-model="queryParams.accountType" placeholder="类型" clearable style="width: 140px">
          <el-option label="USDT" value="USDT" />
          <el-option label="银行卡" value="BANK" />
          <el-option label="支付宝" value="ALIPAY" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:payAccount:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="accountId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="类型" align="center" prop="accountType" width="90">
        <template #default="scope">{{ typeLabel(scope.row.accountType) }}</template>
      </el-table-column>
      <el-table-column label="户名" align="center" prop="accountName" min-width="100" />
      <el-table-column label="账号/地址" align="center" prop="accountNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="银行" align="center" prop="bankName" min-width="120" />
      <el-table-column label="网络" align="center" prop="network" width="90" />
      <el-table-column label="默认" align="center" prop="isDefault" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.isDefault === '1' ? 'success' : 'info'">{{ scope.row.isDefault === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:payAccount:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:payAccount:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="!form.accountId" label="会员手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="必填，按手机号绑定会员" />
        </el-form-item>
        <el-form-item v-else label="会员手机号">
          <el-input v-model="form.phone" disabled />
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType">
          <el-select v-model="form.accountType" placeholder="类型" style="width: 100%">
            <el-option label="USDT" value="USDT" />
            <el-option label="银行卡" value="BANK" />
            <el-option label="支付宝" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="户名/实名" prop="accountName">
          <el-input v-model="form.accountName" placeholder="可选" />
        </el-form-item>
        <el-form-item :label="noLabel" prop="accountNo">
          <el-input v-model="form.accountNo" :placeholder="noPlaceholder" />
        </el-form-item>
        <el-form-item v-if="form.accountType === 'BANK'" label="银行名称" prop="bankName">
          <el-input v-model="form.bankName" placeholder="例如 中国银行" />
        </el-form-item>
        <el-form-item v-if="form.accountType === 'USDT'" label="网络" prop="network">
          <el-select v-model="form.network" placeholder="网络" style="width: 100%">
            <el-option label="TRC20" value="TRC20" />
            <el-option label="ERC20" value="ERC20" />
          </el-select>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-radio-group v-model="form.isDefault">
            <el-radio value="1">是</el-radio>
            <el-radio value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizPayAccount">
import { listPayAccount, getPayAccount, addPayAccount, updatePayAccount, delPayAccount } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, phone: undefined, accountType: undefined, status: undefined },
  rules: {
    phone: [{ required: true, message: "请填写会员手机号", trigger: "blur" }],
    accountType: [{ required: true, message: "请选择账户类型", trigger: "change" }],
    accountNo: [{ required: true, message: "请填写账号或地址", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const noLabel = computed(() => {
  if (form.value.accountType === "USDT") return "USDT地址"
  if (form.value.accountType === "BANK") return "银行卡号"
  return "支付宝账号"
})
const noPlaceholder = computed(() => {
  if (form.value.accountType === "USDT") return "TRC20/ERC20 地址"
  if (form.value.accountType === "BANK") return "银行卡号"
  return "支付宝账号"
})

function typeLabel(type: string) {
  if (type === "USDT") return "USDT"
  if (type === "BANK") return "银行卡"
  if (type === "ALIPAY") return "支付宝"
  return type || "—"
}

function getList() {
  loading.value = true
  listPayAccount(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { accountType: "USDT", isDefault: "0", status: "0", network: "TRC20", accountName: "", accountNo: "", bankName: "", phone: "", remark: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增收款账户" }
function handleUpdate(row: any) {
  getPayAccount(row.accountId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改收款账户"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.accountId ? updatePayAccount(form.value) : addPayAccount(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除该收款账户？').then(() => delPayAccount(row.accountId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
