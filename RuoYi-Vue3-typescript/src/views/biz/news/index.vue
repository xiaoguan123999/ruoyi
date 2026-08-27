<template>
  <div class="app-container ops-page">
    <el-alert
      title="这里维护 App 底部「新闻」页。封面可空（App 可用本地默认图）。正文用富文本，App 接口会转成纯文本。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题" clearable style="width: 220px" @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:news:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="newsId" width="70" />
      <el-table-column label="封面" align="center" width="90">
        <template #default="scope">
          <el-image
            v-if="scope.row.coverUrl"
            :src="imgSrc(scope.row.coverUrl)"
            :preview-src-list="[imgSrc(scope.row.coverUrl)]"
            preview-teleported
            fit="cover"
            style="width: 48px; height: 48px; border-radius: 4px"
          />
          <span v-else style="color: #909399">无</span>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="left" prop="title" min-width="220" show-overflow-tooltip />
      <el-table-column label="发布日期" align="center" prop="publishTime" width="120" />
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="显示" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:news:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:news:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="新闻标题" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="列表页摘要，不填则用标题" />
        </el-form-item>
        <el-form-item label="发布日期" prop="publishTime">
          <el-date-picker v-model="form.publishTime" type="date" value-format="YYYY-MM-DD" placeholder="发布日期" />
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
        <el-form-item label="封面">
          <image-upload v-model="form.coverUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <editor v-model="form.content" :min-height="220" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizNews">
import { listNews, getNews, addNews, updateNews, delNews } from "@/api/biz"
import { isExternal } from "@/utils/validate"

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
    title: [{ required: true, message: "请填写标题", trigger: "blur" }],
    content: [{ required: true, message: "请填写正文", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function imgSrc(url: string) {
  if (!url) return ""
  if (isExternal(url)) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function getList() {
  loading.value = true
  listNews(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { status: "0", sort: 0, coverUrl: "", summary: "", content: "", publishTime: undefined }
}
function handleAdd() { reset(); open.value = true; title.value = "新增新闻" }
function handleUpdate(row: any) {
  getNews(row.newsId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改新闻"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.newsId ? updateNews(form.value) : addNews(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除"' + row.title + '"？').then(() => delNews(row.newsId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
