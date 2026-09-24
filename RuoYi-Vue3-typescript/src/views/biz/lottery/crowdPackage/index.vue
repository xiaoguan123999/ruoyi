<template>
  <div class="app-container ops-page">
    <el-alert
      title="先在此创建标签人群包，再在「抽奖活动 → 必中策略」里引用。人群包独立维护，服务于系统唯一的抽奖活动。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="packageName">
        <el-input v-model="queryParams.packageName" placeholder="人群包名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:crowdPackage:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="packageId" width="70" />
      <el-table-column label="名称" align="left" prop="packageName" min-width="160" show-overflow-tooltip />
      <el-table-column label="条件数" align="center" prop="conditionCount" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.updateTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:crowdPackage:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:crowdPackage:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="人群包名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="如：黄金品质新客包" maxlength="64" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="匹配条件" required>
          <div class="condition-panel">
            <div v-if="form.conditions.length === 0" class="condition-empty">暂无条件，请添加至少一条</div>
            <div v-for="(item, index) in form.conditions" :key="index" class="condition-row">
              <el-select v-model="item.labelField" placeholder="标签维度" style="width: 220px" @change="onFieldChange(item)">
                <el-option v-for="opt in fieldOptions" :key="opt.field" :label="opt.label" :value="opt.field" />
              </el-select>
              <template v-if="item.labelField === 'user_type'">
                <el-select v-model="item.operatorType" disabled style="width: 100px">
                  <el-option label="等于" value="EQUALS" />
                </el-select>
                <el-select v-model="item.ruleValue" placeholder="用户类型" style="width: 140px">
                  <el-option label="新用户" value="NEW_USER" />
                  <el-option label="老用户" value="OLD_USER" />
                </el-select>
              </template>
              <template v-else-if="item.labelField">
                <el-select v-model="item.operatorType" placeholder="运算符" style="width: 100px">
                  <el-option label="等于" value="EQUALS" />
                  <el-option label="大于" value="GREATER_THAN" />
                  <el-option label="小于" value="LESS_THAN" />
                </el-select>
                <el-input-number v-model="item.ruleValueNum" :min="0" :precision="item.labelField === 'charge_amount' ? 2 : 0" style="width: 140px" />
              </template>
              <el-button link type="danger" icon="Delete" @click="removeCondition(index)">删除</el-button>
            </div>
            <el-button type="primary" plain icon="Plus" @click="addCondition">添加条件</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizCrowdPackage">
import {
  listCrowdPackage,
  getCrowdPackage,
  addCrowdPackage,
  updateCrowdPackage,
  delCrowdPackage
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")

const fieldOptions = [
  { label: "用户画像-新老用户", field: "user_type", type: "IMAGE" },
  { label: "资产-充值金额", field: "charge_amount", type: "TRANSACTION" },
  { label: "行为-邀请人数", field: "invite_count", type: "ACTION" }
]

interface ConditionRow {
  labelType: string
  labelField: string
  operatorType: string
  ruleValue: string
  ruleValueNum?: number
}

const data = reactive({
  form: {
    packageId: undefined as number | undefined,
    packageName: "",
    status: "0",
    conditions: [] as ConditionRow[]
  },
  queryParams: { pageNum: 1, pageSize: 100, packageName: undefined, status: undefined },
  rules: {
    packageName: [{ required: true, message: "请填写人群包名称", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function fieldMeta(field: string) {
  return fieldOptions.find((o) => o.field === field)
}

function onFieldChange(item: ConditionRow) {
  const meta = fieldMeta(item.labelField)
  item.labelType = meta?.type || ""
  if (item.labelField === "user_type") {
    item.operatorType = "EQUALS"
    item.ruleValue = item.ruleValue || "NEW_USER"
    item.ruleValueNum = undefined
  } else {
    item.operatorType = item.operatorType || "EQUALS"
    item.ruleValueNum = item.ruleValueNum ?? 0
    item.ruleValue = ""
  }
}

function createCondition(): ConditionRow {
  return { labelType: "IMAGE", labelField: "user_type", operatorType: "EQUALS", ruleValue: "NEW_USER" }
}

function addCondition() {
  form.value.conditions.push(createCondition())
}

function removeCondition(index: number) {
  form.value.conditions.splice(index, 1)
}

function parseConditionsFromApi(conditions: any[]): ConditionRow[] {
  return (conditions || []).map((c) => {
    const row: ConditionRow = {
      labelType: c.labelType,
      labelField: c.labelField,
      operatorType: c.operatorType,
      ruleValue: c.ruleValue
    }
    if (c.labelField === "user_type") {
      row.ruleValue = c.ruleValue || "NEW_USER"
    } else {
      row.ruleValueNum = Number(c.ruleValue)
      if (!Number.isFinite(row.ruleValueNum)) row.ruleValueNum = 0
    }
    return row
  })
}

function buildConditionsPayload(conditions: ConditionRow[]) {
  return conditions.map((c) => {
    const meta = fieldMeta(c.labelField)
    let ruleValue = c.ruleValue
    if (c.labelField !== "user_type") {
      ruleValue = String(c.ruleValueNum ?? 0)
    }
    return {
      labelType: meta?.type || c.labelType,
      labelField: c.labelField,
      operatorType: c.operatorType,
      ruleValue
    }
  })
}

function getList() {
  loading.value = true
  listCrowdPackage(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
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

function reset() {
  form.value = {
    packageId: undefined,
    packageName: "",
    status: "0",
    conditions: [createCondition()]
  }
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增人群包"
}

function handleUpdate(row: any) {
  getCrowdPackage(row.packageId).then((res: any) => {
    const d = res.data
    form.value = {
      packageId: d.packageId,
      packageName: d.packageName,
      status: d.status || "0",
      conditions: parseConditionsFromApi(d.conditions)
    }
    if (form.value.conditions.length === 0) {
      form.value.conditions.push(createCondition())
    }
    open.value = true
    title.value = "修改人群包"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    if (!form.value.conditions.length) {
      proxy.$modal.msgError("请至少添加一条匹配条件")
      return
    }
    for (let i = 0; i < form.value.conditions.length; i++) {
      const c = form.value.conditions[i]
      if (!c.labelField) {
        proxy.$modal.msgError("第" + (i + 1) + "条条件请选择标签维度")
        return
      }
      if (c.labelField === "user_type" && !c.ruleValue) {
        proxy.$modal.msgError("第" + (i + 1) + "条条件请选择用户类型")
        return
      }
      if (c.labelField !== "user_type" && (c.ruleValueNum == null || c.ruleValueNum < 0)) {
        proxy.$modal.msgError("第" + (i + 1) + "条条件请填写有效数值")
        return
      }
    }
    const payload = {
      packageId: form.value.packageId,
      packageName: form.value.packageName,
      status: form.value.status,
      conditions: buildConditionsPayload(form.value.conditions)
    }
    const req = form.value.packageId ? updateCrowdPackage(payload) : addCrowdPackage(payload)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除人群包「' + row.packageName + '」？').then(() => delCrowdPackage(row.packageId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.condition-panel {
  width: 100%;
}
.condition-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.condition-empty {
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
