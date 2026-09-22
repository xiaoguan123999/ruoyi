<template>
  <div class="app-container ops-page">
    <el-alert
      title="维护 App 产品卡片布局模板。模板编码上线后勿随意改名（App 按编码渲染）。预留布局可先停用，发版后再启用。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="模板名称" prop="templateName">
        <el-input v-model="queryParams.templateName" placeholder="模板名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="编码" prop="templateCode">
        <el-input v-model="queryParams.templateCode" placeholder="如 CLASSIC" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:productCardTemplate:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="templateId" width="70" />
      <el-table-column label="预览" align="center" width="90">
        <template #default="scope">
          <el-image
            v-if="scope.row.previewUrl"
            :src="imgSrc(scope.row.previewUrl)"
            :preview-src-list="[imgSrc(scope.row.previewUrl)]"
            preview-teleported
            fit="cover"
            style="width: 48px; height: 48px; border-radius: 4px"
          />
          <span v-else style="color: #909399">无</span>
        </template>
      </el-table-column>
      <el-table-column label="名称" align="left" prop="templateName" min-width="140" show-overflow-tooltip />
      <el-table-column label="编码" align="center" prop="templateCode" width="130" />
      <el-table-column label="指标坑" align="center" prop="metricSlotCount" width="80" />
      <el-table-column label="主金额区" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.hasMainAmount === '1' ? 'success' : 'info'">{{ scope.row.hasMainAmount === '1' ? '有' : '无' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认按钮" align="center" prop="defaultCtaText" width="110" show-overflow-tooltip />
      <el-table-column label="排序" align="center" prop="sort" width="70" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="说明" align="left" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:productCardTemplate:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:productCardTemplate:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="运营可见名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="form.templateCode" placeholder="如 CLASSIC" :disabled="!!form.templateId" />
          <div class="field-tip">新增后不可改；须与 App 组件约定一致</div>
        </el-form-item>
        <el-form-item label="指标坑数量" prop="metricSlotCount">
          <el-input-number v-model="form.metricSlotCount" :min="0" :max="8" style="width: 160px" />
        </el-form-item>
        <el-form-item label="主金额区">
          <el-radio-group v-model="form.hasMainAmount">
            <el-radio value="1">有</el-radio>
            <el-radio value="0">无</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认按钮文案">
          <el-input v-model="form.defaultCtaText" maxlength="32" placeholder="立即参与" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预览图">
          <image-upload v-model="form.previewUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="适用场景">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="给运营看的选用说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizProductCardTemplate">
import {
  listProductCardTemplate,
  getProductCardTemplate,
  addProductCardTemplate,
  updateProductCardTemplate,
  delProductCardTemplate
} from "@/api/biz"
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
  queryParams: { pageNum: 1, pageSize: 100, templateName: undefined, templateCode: undefined, status: undefined },
  rules: {
    templateName: [{ required: true, message: "请填写模板名称", trigger: "blur" }],
    templateCode: [{ required: true, message: "请填写模板编码", trigger: "blur" }],
    metricSlotCount: [{ required: true, message: "请填写指标坑数量", trigger: "blur" }]
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
  listProductCardTemplate(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = {
    status: "0",
    sort: 0,
    metricSlotCount: 2,
    hasMainAmount: "0",
    defaultCtaText: "立即参与",
    previewUrl: "",
    templateCode: "",
    templateName: "",
    remark: ""
  }
}
function handleAdd() { reset(); open.value = true; title.value = "新增卡片模板" }
function handleUpdate(row: any) {
  getProductCardTemplate(row.templateId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改卡片模板"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    if (form.value.templateCode) {
      form.value.templateCode = String(form.value.templateCode).trim().toUpperCase()
    }
    const req = form.value.templateId ? updateProductCardTemplate(form.value) : addProductCardTemplate(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除模板「' + row.templateName + '」？已被产品引用时不能删。').then(() => delProductCardTemplate(row.templateId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>

<style scoped>
.field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
