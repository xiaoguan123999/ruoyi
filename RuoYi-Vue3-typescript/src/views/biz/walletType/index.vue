<template>
  <div class="app-container ops-page">
    <el-alert
      title="钱包类型和币种是两层：余额、产品收益、推广收益、助力值是类型；CNY / USDT 是每个类型下的币种。后台可新增类型。内置类型不能删、不能改编码。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="typeName">
        <el-input v-model="queryParams.typeName" placeholder="钱包名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:walletType:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="typeId" width="80" />
      <el-table-column label="编码" align="center" prop="typeCode" width="130" />
      <el-table-column label="名称" align="center" prop="typeName" min-width="140" />
      <el-table-column label="提现规则" align="center" min-width="180">
        <template #default="scope">{{ withdrawModeLabel(scope.row.withdrawMode) }}</template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="内置" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.builtin === '1' ? 'warning' : 'info'">{{ scope.row.builtin === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:walletType:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" :disabled="scope.row.builtin === '1'" @click="handleDelete(scope.row)" v-hasPermi="['biz:walletType:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="编码" prop="typeCode">
          <el-input v-model="form.typeCode" placeholder="如 ACTIVITY" :disabled="!!form.typeId" maxlength="32" />
        </el-form-item>
        <el-form-item label="名称" prop="typeName">
          <el-input v-model="form.typeName" placeholder="如 活动钱包" maxlength="32" />
        </el-form-item>
        <el-form-item label="提现规则" prop="withdrawMode">
          <el-select v-model="form.withdrawMode" style="width: 100%">
            <el-option v-for="item in withdrawModes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizWalletType">
import { listWalletType, getWalletType, addWalletType, updateWalletType, delWalletType } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const withdrawModes = [
  { value: "NONE", label: "不可提现" },
  { value: "OPEN", label: "可提现" },
  { value: "ANY_ORDER", label: "认购任意产品后可提" },
  { value: "PRODUCT_REQUIRED", label: "认购指定产品后可提" }
]
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, typeName: undefined, status: undefined },
  rules: {
    typeCode: [{ required: true, message: "请填写编码", trigger: "blur" }],
    typeName: [{ required: true, message: "请填写名称", trigger: "blur" }],
    withdrawMode: [{ required: true, message: "请选择提现规则", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function withdrawModeLabel(mode: string) {
  return withdrawModes.find(item => item.value === mode)?.label || mode || "—"
}
function getList() {
  loading.value = true
  listWalletType(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { status: "0", sort: 0, withdrawMode: "NONE", typeCode: "", typeName: "", remark: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增钱包类型" }
function handleUpdate(row: any) {
  getWalletType(row.typeId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改钱包类型"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.typeId ? updateWalletType(form.value) : addWalletType(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除钱包类型"' + row.typeName + '"？').then(() => delWalletType(row.typeId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
