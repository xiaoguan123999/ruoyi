<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="币种" clearable style="width: 140px">
          <el-option label="人民币" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="上架" value="0" />
          <el-option label="下架" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:product:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="productList">
      <el-table-column label="ID" align="center" prop="productId" width="80" />
      <el-table-column label="产品名称" align="center" prop="productName" />
      <el-table-column label="币种" align="center" prop="currency" width="90" />
      <el-table-column label="价格" align="center" prop="price" />
      <el-table-column label="日返" align="center" prop="dailyRebate" />
      <el-table-column label="天数" align="center" prop="durationDays" />
      <el-table-column label="提现指定" align="center" prop="withdrawRequired" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.withdrawRequired === '1' ? 'warning' : 'info'">{{ scope.row.withdrawRequired === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:product:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:product:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="结算币种" prop="currency">
          <el-select v-model="form.currency" style="width: 100%">
            <el-option label="人民币 CNY" value="CNY" />
            <el-option label="USDT" value="USDT" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每日返利" prop="dailyRebate">
          <el-input-number v-model="form.dailyRebate" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="返利天数" prop="durationDays">
          <el-input-number v-model="form.durationDays" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="提现指定产品" prop="withdrawRequired">
          <el-radio-group v-model="form.withdrawRequired">
            <el-radio value="1">是</el-radio>
            <el-radio value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">上架</el-radio>
            <el-radio value="1">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizProduct">
import { listProduct, getProduct, addProduct, updateProduct, delProduct } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const productList = ref<any[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 10, productName: undefined, currency: undefined, status: undefined },
  rules: {
    productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
    currency: [{ required: true, message: "请选择币种", trigger: "change" }],
    price: [{ required: true, message: "价格不能为空", trigger: "blur" }],
    dailyRebate: [{ required: true, message: "日返不能为空", trigger: "blur" }],
    durationDays: [{ required: true, message: "天数不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listProduct(queryParams.value).then((res: any) => {
    productList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { status: "0", withdrawRequired: "0", sort: 0, currency: "CNY" }
  proxy.resetForm("formRef")
}
function handleAdd() { reset(); open.value = true; title.value = "新增产品" }
function handleUpdate(row: any) {
  reset()
  getProduct(row.productId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改产品"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.productId ? updateProduct(form.value) : addProduct(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除产品编号为"' + row.productId + '"的数据项？').then(() => delProduct(row.productId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
