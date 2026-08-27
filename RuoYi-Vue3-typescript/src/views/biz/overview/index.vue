<template>
  <div class="app-container ops-page">
    <el-alert
      title="这里只改 App 首页「运行概览」展示数字，没有真实业务统计。卡片配图默认用 App 本地图（satellite / coverage / terminal），也可上传覆盖。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="显示" value="0" />
          <el-option label="隐藏" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:overview:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="itemId" width="70" />
      <el-table-column label="标识" align="center" prop="itemKey" width="120" />
      <el-table-column label="标题" align="center" prop="title" min-width="140" />
      <el-table-column label="展示数值" align="center" prop="displayValue" min-width="140" />
      <el-table-column label="状态文案" align="center" prop="statusText" min-width="120" />
      <el-table-column label="颜色" align="center" prop="statusColor" width="120">
        <template #default="scope">
          <span class="color-dot" :style="{ background: scope.row.statusColor }"></span>
          {{ scope.row.statusColor }}
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="显示" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:overview:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:overview:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="卡片标识" prop="itemKey">
          <el-input v-model="form.itemKey" placeholder="例如 satellite / coverage / terminal" :disabled="!!form.itemId" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="例如 在轨卫星" />
        </el-form-item>
        <el-form-item label="展示数值" prop="displayValue">
          <el-input v-model="form.displayValue" placeholder="例如 320 颗" />
        </el-form-item>
        <el-form-item label="状态文案" prop="statusText">
          <el-input v-model="form.statusText" placeholder="例如 正常运行" />
        </el-form-item>
        <el-form-item label="状态颜色" prop="statusColor">
          <el-color-picker v-model="form.statusColor" />
          <el-input v-model="form.statusColor" style="width: 160px; margin-left: 12px" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 160px" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-radio-group v-model="form.status">
            <el-radio value="0">显示</el-radio>
            <el-radio value="1">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="配图">
          <image-upload v-model="form.imageUrl" :limit="1" />
          <div class="el-form-item__tip">可不传。空则 App 按标识用本地 3D 图。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizOverview">
import { listOverview, getOverview, addOverview, updateOverview, delOverview } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, title: undefined, status: undefined },
  rules: {
    itemKey: [{ required: true, message: "请填写卡片标识", trigger: "blur" }],
    title: [{ required: true, message: "请填写标题", trigger: "blur" }],
    displayValue: [{ required: true, message: "请填写展示数值", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listOverview(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { status: "0", statusColor: "#4DA3FF", sort: 0, imageUrl: "", statusText: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增概览卡片" }
function handleUpdate(row: any) {
  getOverview(row.itemId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改概览卡片"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.itemId ? updateOverview(form.value) : addOverview(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除"' + row.title + '"？').then(() => delOverview(row.itemId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>

<style scoped>
.color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
</style>
