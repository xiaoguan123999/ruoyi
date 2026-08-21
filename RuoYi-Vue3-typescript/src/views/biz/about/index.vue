<template>
  <div class="app-container">
    <el-alert
      title="这里维护 App「关于我们」页面内容。可新增多段，按排序展示。正文用富文本编辑，App 接口会转成纯文本。"
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:about:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="aboutId" width="70" />
      <el-table-column label="标题" align="center" prop="title" min-width="140" />
      <el-table-column label="副标题" align="center" prop="subtitle" min-width="180" show-overflow-tooltip />
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="显示" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:about:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:about:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="例如 星帆智联" />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="例如 连接星空 · 智联未来" />
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
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <editor v-model="form.content" :min-height="192" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizAbout">
import { listAbout, getAbout, addAbout, updateAbout, delAbout } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, status: undefined },
  rules: {
    title: [{ required: true, message: "请填写标题", trigger: "blur" }],
    content: [{ required: true, message: "请填写正文", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listAbout(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { status: "0", sort: 0, imageUrl: "", subtitle: "", content: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增关于我们" }
function handleUpdate(row: any) {
  getAbout(row.aboutId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改关于我们"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.aboutId ? updateAbout(form.value) : addAbout(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除"' + row.title + '"？').then(() => delAbout(row.aboutId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
