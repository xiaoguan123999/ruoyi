<template>
  <div class="app-container ops-page">
    <el-alert
      title="维护 App 各平台安装包版本。设为「最新」后，同平台其它版本会自动取消最新；App 通过 /app/version/latest 拉取。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="平台" prop="platform">
        <el-select v-model="queryParams.platform" placeholder="平台" clearable style="width: 140px">
          <el-option label="Android" value="android" />
          <el-option label="iOS" value="ios" />
        </el-select>
      </el-form-item>
      <el-form-item label="版本号" prop="version">
        <el-input v-model="queryParams.version" placeholder="版本号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="最新" prop="isLatest">
        <el-select v-model="queryParams.isLatest" placeholder="是否最新" clearable style="width: 120px">
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="启用" prop="isEnabled">
        <el-select v-model="queryParams.isEnabled" placeholder="是否启用" clearable style="width: 120px">
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:appVersion:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="versionId" width="70" />
      <el-table-column label="平台" align="center" width="100">
        <template #default="scope">{{ platformLabel(scope.row.platform) }}</template>
      </el-table-column>
      <el-table-column label="版本号" align="center" prop="version" min-width="110" />
      <el-table-column label="下载地址" align="left" prop="downloadUrl" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <a v-if="scope.row.downloadUrl" :href="scope.row.downloadUrl" target="_blank" rel="noopener">{{ scope.row.downloadUrl }}</a>
          <span v-else style="color: #909399">—</span>
        </template>
      </el-table-column>
      <el-table-column label="更新说明" align="left" prop="description" min-width="160" show-overflow-tooltip />
      <el-table-column label="最新" align="center" width="80">
        <template #default="scope">
          <el-switch
            :model-value="rowFlag(scope.row, 'isLatest', 'latestFlag')"
            @change="(val: boolean) => handleToggleLatest(scope.row, val)"
            v-hasPermi="['biz:appVersion:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="强更" align="center" width="80">
        <template #default="scope">
          <el-switch
            :model-value="rowFlag(scope.row, 'forceUpdate', 'forceUpdateFlag')"
            @change="(val: boolean) => handleToggleForceUpdate(scope.row, val)"
            v-hasPermi="['biz:appVersion:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="启用" align="center" width="80">
        <template #default="scope">
          <el-switch
            :model-value="rowFlag(scope.row, 'isEnabled', 'enabledFlag')"
            @change="(val: boolean) => handleToggleEnabled(scope.row, val)"
            v-hasPermi="['biz:appVersion:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortOrder" width="70" />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:appVersion:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:appVersion:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="640px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="平台" prop="platform">
          <el-select v-model="form.platform" placeholder="请选择" style="width: 100%">
            <el-option label="Android" value="android" />
            <el-option label="iOS" value="ios" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="例如 1.0.11" />
        </el-form-item>
        <el-form-item label="下载地址" prop="downloadUrl">
          <el-input v-model="form.downloadUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="更新说明">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="App 更新弹窗展示" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="设为最新">
              <el-switch v-model="form.isLatest" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="强制更新">
              <el-switch v-model="form.forceUpdate" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="启用">
              <el-switch v-model="form.isEnabled" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <p class="form-tip">设为最新时，同平台其它版本会自动取消最新。</p>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizAppVersion">
import {
  listAppVersion,
  getAppVersion,
  addAppVersion,
  updateAppVersion,
  delAppVersion,
  setAppVersionLatest,
  setAppVersionForceUpdate,
  setAppVersionEnabled
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    platform: undefined as string | undefined,
    version: undefined as string | undefined,
    isLatest: undefined as string | undefined,
    isEnabled: undefined as string | undefined
  },
  rules: {
    platform: [{ required: true, message: "请选择平台", trigger: "change" }],
    version: [{ required: true, message: "请填写版本号", trigger: "blur" }],
    downloadUrl: [{ required: true, message: "请填写下载地址", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function platformLabel(v: string) {
  if (v === "ios") return "iOS"
  if (v === "android") return "Android"
  return v || "—"
}

function rowId(row: any) {
  return row.versionId ?? row.id
}

function rowFlag(row: any, ...keys: string[]) {
  for (const key of keys) {
    const v = row[key]
    if (v !== undefined && v !== null) return v === true || v === "1" || v === 1
  }
  return false
}

function asSwitchStr(v: any) {
  return v === true || v === "1" || v === 1 ? "1" : "0"
}

function normalizeForm(data: any) {
  return {
    ...data,
    forceUpdate: asSwitchStr(data.forceUpdate ?? data.forceUpdateFlag),
    isLatest: asSwitchStr(data.isLatest ?? data.latestFlag),
    isEnabled: asSwitchStr(data.isEnabled ?? data.enabledFlag),
    sortOrder: Number(data.sortOrder ?? 0)
  }
}

function buildPayload(data: any) {
  return {
    versionId: data.versionId,
    platform: data.platform,
    version: data.version,
    downloadUrl: data.downloadUrl,
    description: data.description,
    forceUpdate: asSwitchStr(data.forceUpdate),
    isLatest: asSwitchStr(data.isLatest),
    isEnabled: asSwitchStr(data.isEnabled),
    sortOrder: Number(data.sortOrder ?? 0)
  }
}

function getList() {
  loading.value = true
  listAppVersion(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
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

function reset() {
  form.value = {
    platform: "android",
    version: "",
    downloadUrl: "",
    description: "",
    forceUpdate: "0",
    isLatest: "0",
    isEnabled: "1",
    sortOrder: 0
  }
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增版本"
}

function handleUpdate(row: any) {
  const id = rowId(row)
  getAppVersion(id).then((res: any) => {
    form.value = normalizeForm(res.data || {})
    open.value = true
    title.value = "修改版本"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const payload = buildPayload(form.value)
    const req = payload.versionId ? updateAppVersion(payload) : addAppVersion(payload)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row: any) {
  const id = rowId(row)
  const name = `${platformLabel(row.platform)} ${row.version}`
  proxy.$modal.confirm(`是否确认删除版本「${name}」？`).then(() => delAppVersion(id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleToggleLatest(row: any, val: boolean) {
  const id = rowId(row)
  setAppVersionLatest(id, val).then(() => {
    proxy.$modal.msgSuccess(val ? "已设为最新" : "已取消最新")
    getList()
  }).catch(() => getList())
}

function handleToggleForceUpdate(row: any, val: boolean) {
  const id = rowId(row)
  setAppVersionForceUpdate(id, val).then(() => {
    proxy.$modal.msgSuccess(val ? "已开启强制更新" : "已关闭强制更新")
    getList()
  }).catch(() => getList())
}

function handleToggleEnabled(row: any, val: boolean) {
  const id = rowId(row)
  setAppVersionEnabled(id, val).then(() => {
    proxy.$modal.msgSuccess(val ? "已启用" : "已停用")
    getList()
  }).catch(() => getList())
}

getList()
</script>

<style scoped>
.form-tip {
  margin: -4px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
</style>
