<template>
  <div class="app-container ops-page">
    <el-alert
      title="这里维护 App 首页顶部视频轮播。显示中的视频按排序从小到大播放。一条都没有时 App 仍可用本地默认图。"
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:carousel:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="carouselId" width="70" />
      <el-table-column label="封面" align="center" width="120">
        <template #default="scope">
          <el-image
            v-if="scope.row.coverUrl"
            :src="mediaSrc(scope.row.coverUrl)"
            :preview-src-list="[mediaSrc(scope.row.coverUrl)]"
            preview-teleported
            fit="cover"
            style="width: 96px; height: 54px; border-radius: 4px"
          />
          <span v-else style="color: #909399">无</span>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="left" prop="title" min-width="160" show-overflow-tooltip />
      <el-table-column label="视频" align="left" min-width="220" show-overflow-tooltip>
        <template #default="scope">
          <a v-if="scope.row.videoUrl" :href="mediaSrc(scope.row.videoUrl)" target="_blank">预览</a>
          <span v-else style="color: #909399">无</span>
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
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:carousel:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:carousel:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="后台识别用，可不填" />
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
        <el-form-item label="视频" prop="videoUrl">
          <file-upload v-model="form.videoUrl" :limit="1" :file-size="100" :file-type="['mp4']" />
          <el-input v-model="form.videoUrl" placeholder="也可粘贴 mp4 地址" style="margin-top: 8px" />
        </el-form-item>
        <el-form-item label="封面">
          <image-upload v-model="form.coverUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizCarousel">
import { listCarousel, getCarousel, addCarousel, updateCarousel, delCarousel } from "@/api/biz"
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
    videoUrl: [{ required: true, message: "请上传或填写视频地址", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function mediaSrc(url: string) {
  if (!url) return ""
  if (isExternal(url)) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function getList() {
  loading.value = true
  listCarousel(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { status: "0", sort: 0, title: "", videoUrl: "", coverUrl: "", remark: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增视频轮播" }
function handleUpdate(row: any) {
  getCarousel(row.carouselId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改视频轮播"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.carouselId ? updateCarousel(form.value) : addCarousel(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  const name = row.title || row.carouselId
  proxy.$modal.confirm('是否确认删除"' + name + '"？').then(() => delCarousel(row.carouselId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
