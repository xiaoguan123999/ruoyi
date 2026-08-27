<template>
  <div class="app-container ops-page">
    <el-alert
      title="这里维护 App「官方群聊」页。请上传群二维码图片，App 会原样展示。可新增多个（例如微信群、QQ群）。"
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:group:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="groupId" width="70" />
      <el-table-column label="二维码" align="center" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.qrUrl"
            :src="qrSrc(scope.row.qrUrl)"
            :preview-src-list="[qrSrc(scope.row.qrUrl)]"
            preview-teleported
            fit="contain"
            style="width: 56px; height: 56px; background: #fff"
          />
          <span v-else style="color: #909399">未上传</span>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" min-width="140" />
      <el-table-column label="提示文案" align="center" prop="hint" min-width="120" />
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="显示" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:group:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:group:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="例如 官方群聊" />
        </el-form-item>
        <el-form-item label="提示文案" prop="hint">
          <el-input v-model="form.hint" placeholder="例如 扫码进群" />
        </el-form-item>
        <el-form-item label="群聊二维码" prop="qrUrl">
          <image-upload v-model="form.qrUrl" :limit="1" />
          <div class="el-upload__tip">请上传清晰二维码图片，App 会按这个图展示。</div>
        </el-form-item>
        <el-form-item label="补充说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选，例如进群后请修改备注" />
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
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizGroupChat">
import { listGroupChat, getGroupChat, addGroupChat, updateGroupChat, delGroupChat } from "@/api/biz"
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
    qrUrl: [{ required: true, message: "请上传群聊二维码", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function qrSrc(url: string) {
  if (!url) return ""
  if (isExternal(url)) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function getList() {
  loading.value = true
  listGroupChat(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { status: "0", sort: 0, qrUrl: "", hint: "扫码进群", title: "官方群聊", remark: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增官方群聊" }
function handleUpdate(row: any) {
  getGroupChat(row.groupId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改官方群聊"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.groupId ? updateGroupChat(form.value) : addGroupChat(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除"' + row.title + '"？').then(() => delGroupChat(row.groupId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
