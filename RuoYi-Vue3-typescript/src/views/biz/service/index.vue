<template>
  <div class="app-container ops-page">
    <el-alert
      title="这里维护 App「客服中心」。登录页「联系客服」也会读同一套数据。和「官方群聊」不是同一页。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form ref="configRef" :model="config" label-width="110px" v-loading="configLoading" class="ops-form-full">
      <el-divider content-position="left">展示文案</el-divider>
      <el-row :gutter="16" class="config-basic-row">
        <el-col :xs="24" :sm="16" :md="16" :lg="16" :xl="16">
          <el-form-item label="标题">
            <el-input v-model="config.title" placeholder="例如 客服中心" class="title-input" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
          <el-form-item label="工作时间" class="work-time-item">
            <el-time-picker
              v-model="workTimeRange"
              is-range
              range-separator="-"
              start-placeholder="开始"
              end-placeholder="结束"
              format="HH:mm"
              value-format="HH:mm"
              clearable
              placement="bottom-start"
              popper-class="cs-work-time-popper"
              :popper-options="{
                modifiers: [
                  { name: 'flip', options: { fallbackPlacements: ['bottom-end', 'top-start', 'top-end'] } },
                  { name: 'preventOverflow', options: { padding: 12 } }
                ]
              }"
              class="work-time-picker"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="提示文案">
        <el-input v-model="config.hint" type="textarea" :rows="2" placeholder="例如 通道拥堵可联系在线客服" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveConfig" v-hasPermi="['biz:service:edit']">保存文案</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">联系渠道</el-divider>
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="channelType">
        <el-select v-model="queryParams.channelType" placeholder="类型" clearable style="width: 140px">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:service:add']">新增渠道</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="channelId" width="70" />
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
          <span v-else style="color: #909399">无</span>
        </template>
      </el-table-column>
      <el-table-column label="名称" align="center" prop="name" min-width="120" />
      <el-table-column label="类型" align="center" width="110">
        <template #default="scope">{{ typeLabel(scope.row.channelType) }}</template>
      </el-table-column>
      <el-table-column label="账号/号码" align="center" prop="value" min-width="140" show-overflow-tooltip />
      <el-table-column label="跳转链接" align="center" prop="linkUrl" min-width="160" show-overflow-tooltip />
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="显示" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:service:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:service:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例如 微信客服" />
        </el-form-item>
        <el-form-item label="类型" prop="channelType">
          <el-select v-model="form.channelType" placeholder="类型" style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号/号码" prop="value">
          <el-input v-model="form.value" placeholder="微信号、手机号、QQ 等" />
        </el-form-item>
        <el-form-item label="客服二维码" prop="qrUrl">
          <image-upload v-model="form.qrUrl" :limit="1" />
          <div class="el-upload__tip">微信/Telegram 等可上传二维码，App 直接展示。</div>
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="可选，例如 https://t.me/xxx" />
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

<script setup lang="ts" name="BizService">
import { listCsChannel, getCsChannel, addCsChannel, updateCsChannel, delCsChannel, getCsConfig, saveCsConfig } from "@/api/biz"
import { isExternal } from "@/utils/validate"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const configLoading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const typeOptions = [
  { label: "微信", value: "WECHAT" },
  { label: "电话", value: "PHONE" },
  { label: "Telegram", value: "TELEGRAM" },
  { label: "QQ", value: "QQ" },
  { label: "链接", value: "LINK" },
  { label: "二维码", value: "QR" }
]
const config = ref({ title: "客服中心", workTime: "09:00 - 21:00", hint: "" })

function normalizeTime(t: string) {
  const parts = (t || "").trim().split(":")
  if (parts.length < 2) return ""
  const h = String(Number(parts[0])).padStart(2, "0")
  const m = String(Number(parts[1])).padStart(2, "0")
  return `${h}:${m}`
}

function parseWorkTime(text: string): [string, string] | null {
  if (!text) return null
  const match = String(text).match(/(\d{1,2}:\d{2})\s*[-–—~至到]\s*(\d{1,2}:\d{2})/)
  if (!match) return null
  return [normalizeTime(match[1]), normalizeTime(match[2])]
}

const workTimeRange = computed({
  get(): [string, string] | null {
    return parseWorkTime(config.value.workTime)
  },
  set(val: [string, string] | null) {
    if (val && val.length === 2 && val[0] && val[1]) {
      config.value.workTime = `${val[0]} - ${val[1]}`
    } else {
      config.value.workTime = ""
    }
  }
})

const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, name: undefined, channelType: undefined, status: undefined },
  rules: {
    name: [{ required: true, message: "请填写名称", trigger: "blur" }],
    channelType: [{ required: true, message: "请选择类型", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function typeLabel(type: string) {
  const hit = typeOptions.find((item) => item.value === type)
  return hit ? hit.label : (type || "—")
}
function qrSrc(url: string) {
  if (!url) return ""
  if (isExternal(url)) return url
  return import.meta.env.VITE_APP_BASE_API + url
}
function loadConfig() {
  configLoading.value = true
  getCsConfig().then((res: any) => {
    const data = res.data || {}
    config.value = {
      title: data.title || "客服中心",
      workTime: data.workTime || "09:00 - 21:00",
      hint: data.hint || ""
    }
  }).finally(() => { configLoading.value = false })
}
function saveConfig() {
  saveCsConfig(config.value).then(() => proxy.$modal.msgSuccess("文案已保存"))
}
function getList() {
  loading.value = true
  listCsChannel(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = { name: "", channelType: "WECHAT", value: "", qrUrl: "", linkUrl: "", sort: 0, status: "0", remark: "" }
}
function handleAdd() { reset(); open.value = true; title.value = "新增客服渠道" }
function handleUpdate(row: any) {
  getCsChannel(row.channelId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改客服渠道"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const req = form.value.channelId ? updateCsChannel(form.value) : addCsChannel(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除"' + row.name + '"？').then(() => delCsChannel(row.channelId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
loadConfig()
getList()
</script>

<style scoped>
.config-basic-row :deep(.el-form-item__content) {
  min-width: 0;
}
.title-input {
  width: 100%;
  max-width: 640px;
}
.work-time-item :deep(.el-form-item__content) {
  justify-content: flex-start;
}
.work-time-picker {
  width: 100%;
  max-width: 280px;
}
.work-time-picker :deep(.el-input),
.work-time-picker :deep(.el-input__wrapper) {
  width: 100%;
}
@media (max-width: 767px) {
  .title-input,
  .work-time-picker {
    max-width: 100%;
  }
}
</style>

<style>
.cs-work-time-popper.el-picker__popper {
  max-width: min(280px, calc(100vw - 24px));
}
.cs-work-time-popper .el-time-range-picker {
  width: 260px;
  max-width: 100%;
}
.cs-work-time-popper .el-time-range-picker__cell {
  padding: 4px 2px 6px;
}
.cs-work-time-popper .el-time-panel {
  width: 100%;
}
.cs-work-time-popper .el-time-spinner__item {
  font-size: 12px;
  height: 28px;
  line-height: 28px;
}
</style>
