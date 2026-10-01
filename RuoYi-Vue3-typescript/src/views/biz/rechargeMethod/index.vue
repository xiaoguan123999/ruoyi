<template>
  <div class="app-container ops-page">
    <el-alert
      title="先维护支付方式（名称/图标/是否客服）。编码建议用 wechat/alipay/union/usdt，与原通道场景一致；支付通道页绑定支付方式即可。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="label">
        <el-input v-model="queryParams.label" placeholder="展示名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="是否客服" prop="isCs">
        <el-select v-model="queryParams.isCs" placeholder="全部" clearable style="width: 120px">
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:rechargeMethod:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!ids.length" @click="handleDelete()" v-hasPermi="['biz:rechargeMethod:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="图标" align="center" width="80">
        <template #default="scope">
          <el-image v-if="scope.row.iconUrl" :src="scope.row.iconUrl" fit="contain" style="width: 36px; height: 36px" />
          <span v-else style="color:#909399">—</span>
        </template>
      </el-table-column>
      <el-table-column label="展示名" align="center" prop="label" min-width="120" />
      <el-table-column label="编码" align="center" prop="methodCode" min-width="110" />
      <el-table-column label="是否客服" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.isCs === '1' ? 'warning' : 'info'">{{ scope.row.isCs === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortOrder" width="70" />
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:rechargeMethod:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:rechargeMethod:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="展示名" prop="label">
          <el-input v-model="form.label" placeholder="例如 微信支付" maxlength="64" />
        </el-form-item>
        <el-form-item label="编码" prop="methodCode">
          <el-input v-model="form.methodCode" placeholder="建议 wechat / alipay / union / usdt" maxlength="32" :disabled="!!form.methodId" />
        </el-form-item>
        <el-form-item label="图标">
          <image-upload v-model="form.iconUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="是否跳转客服">
          <el-switch v-model="form.isCs" active-value="1" inactive-value="0" />
          <span class="tip">开启后点充值进客服，不挂支付通道</span>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
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

<script setup lang="ts" name="BizRechargeMethod">
import {
  listRechargeMethod,
  getRechargeMethod,
  addRechargeMethod,
  updateRechargeMethod,
  delRechargeMethod
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const ids = ref<number[]>([])

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  label: undefined as string | undefined,
  isCs: undefined as string | undefined,
  status: undefined as string | undefined
})

const form = ref<any>({})
const rules = {
  label: [{ required: true, message: "请填写展示名", trigger: "blur" }],
  methodCode: [{ required: true, message: "请填写编码", trigger: "blur" }]
}

function resetForm() {
  form.value = {
    methodId: undefined,
    methodCode: "",
    label: "",
    iconUrl: "",
    isCs: "0",
    status: "0",
    sortOrder: 0,
    remark: ""
  }
  proxy.resetForm("formRef")
}

function getList() {
  loading.value = true
  listRechargeMethod(queryParams.value).then((res: any) => {
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
  handleQuery()
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item) => item.methodId)
}

function handleAdd() {
  resetForm()
  open.value = true
  title.value = "新增充值方式"
}

function handleUpdate(row: any) {
  resetForm()
  getRechargeMethod(row.methodId).then((res: any) => {
    form.value = { ...res.data }
    open.value = true
    title.value = "修改充值方式"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const payload = { ...form.value }
    const req = payload.methodId ? updateRechargeMethod(payload) : addRechargeMethod(payload)
    req.then(() => {
      proxy.$modal.msgSuccess(payload.methodId ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row?: any) {
  const methodIds = row?.methodId != null ? row.methodId : ids.value
  if (methodIds == null || (Array.isArray(methodIds) && !methodIds.length)) {
    proxy.$modal.msgWarning("请选择要删除的数据")
    return
  }
  proxy.$modal.confirm("确认删除所选充值方式？").then(() => {
    return delRechargeMethod(methodIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
