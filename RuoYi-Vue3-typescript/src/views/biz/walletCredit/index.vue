<template>
  <div class="app-container ops-page">
    <el-alert
      title="签到、实名奖励、邀请、分佣、等级奖励、产品日返等，入到哪个钱包在这里配。充值默认入余额；认购始终扣余额。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="业务名称" prop="bizName">
        <el-input v-model="queryParams.bizName" placeholder="业务名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="入账钱包" prop="typeCode">
        <el-select v-model="queryParams.typeCode" placeholder="钱包类型" clearable style="width: 180px">
          <el-option v-for="item in typeOptions" :key="item.typeCode" :label="item.typeName" :value="item.typeCode" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:walletCredit:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="ruleId" width="80" />
      <el-table-column label="业务类型" align="center" prop="bizType" width="150" />
      <el-table-column label="业务名称" align="center" prop="bizName" min-width="140" />
      <el-table-column label="入账钱包" align="center" min-width="140">
        <template #default="scope">{{ scope.row.typeName || scope.row.typeCode }}</template>
      </el-table-column>
      <el-table-column label="内置" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.builtin === '1' ? 'warning' : 'info'">{{ scope.row.builtin === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:walletCredit:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" :disabled="scope.row.builtin === '1'" @click="handleDelete(scope.row)" v-hasPermi="['biz:walletCredit:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="业务类型" prop="bizType">
          <el-input v-model="form.bizType" placeholder="如 LOTTERY" :disabled="!!form.ruleId" maxlength="32" />
        </el-form-item>
        <el-form-item label="业务名称" prop="bizName">
          <el-input v-model="form.bizName" placeholder="如 抽奖奖励" maxlength="32" />
        </el-form-item>
        <el-form-item label="入账钱包" prop="typeCode">
          <el-select v-model="form.typeCode" style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item.typeCode" :label="item.typeName + '（' + item.typeCode + '）'" :value="item.typeCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 160px" />
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

<script setup lang="ts" name="BizWalletCredit">
import { listWalletCredit, getWalletCredit, addWalletCredit, updateWalletCredit, delWalletCredit, listWalletTypeOptions } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const typeOptions = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, bizName: undefined, typeCode: undefined },
  rules: {
    bizType: [{ required: true, message: "请填写业务类型", trigger: "blur" }],
    bizName: [{ required: true, message: "请填写业务名称", trigger: "blur" }],
    typeCode: [{ required: true, message: "请选择入账钱包", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function loadTypes() {
  listWalletTypeOptions().then((res: any) => { typeOptions.value = res.data || [] })
}
function getList() {
  loading.value = true
  listWalletCredit(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { sort: 0, bizType: "", bizName: "", typeCode: "PROMO", remark: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增入账配置" }
function handleUpdate(row: any) {
  getWalletCredit(row.ruleId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改入账配置"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.ruleId ? updateWalletCredit(form.value) : addWalletCredit(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除入账配置"' + row.bizName + '"？').then(() => delWalletCredit(row.ruleId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
loadTypes()
getList()
</script>
