<template>
  <div class="app-container ops-page">
    <el-alert
      title="配置如何获得抽奖次数。例：累计签到≥3天 且 直推实名≥3人 → 每达标一档发放指定次数。可设「仅一次」「按倍数不限档」「按倍数最多 N 档」。App 进入抽奖页/抽奖前会自动校验并发放。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="queryParams.ruleName" placeholder="规则名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:lotteryChanceRule:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="ruleId" width="70" />
      <el-table-column label="规则名称" align="left" prop="ruleName" min-width="160" show-overflow-tooltip />
      <el-table-column label="签到天数≥" align="center" prop="checkinDays" width="110" />
      <el-table-column label="直推实名≥" align="center" prop="inviteKycCount" width="110" />
      <el-table-column label="每档发放" align="center" prop="grantAmount" width="90" />
      <el-table-column label="发放模式" align="center" min-width="150">
        <template #default="scope">
          <span v-if="scope.row.onceOnly !== '0'">仅一次</span>
          <span v-else-if="!scope.row.maxRepeat || scope.row.maxRepeat <= 0">按倍数（不限）</span>
          <span v-else>按倍数（最多{{ scope.row.maxRepeat }}档）</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:lotteryChanceRule:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:lotteryChanceRule:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="如：签到3天+直推实名3人" maxlength="64" />
        </el-form-item>
        <el-form-item label="累计签到≥" prop="checkinDays">
          <el-input-number v-model="form.checkinDays" :min="0" :max="3650" style="width: 160px" />
          <span class="field-tip">天，0 表示不限制此项</span>
        </el-form-item>
        <el-form-item label="直推实名≥" prop="inviteKycCount">
          <el-input-number v-model="form.inviteKycCount" :min="0" :max="99999" style="width: 160px" />
          <span class="field-tip">人（KYC通过），0 表示不限制此项</span>
        </el-form-item>
        <el-form-item label="每档发放" prop="grantAmount">
          <el-input-number v-model="form.grantAmount" :min="1" :max="100" style="width: 160px" />
          <span class="field-tip">次抽奖机会</span>
        </el-form-item>
        <el-form-item label="发放模式" prop="grantMode">
          <el-radio-group v-model="form.grantMode" @change="onGrantModeChange">
            <el-radio value="ONCE">仅发放一次</el-radio>
            <el-radio value="LOOP">按倍数重复（不限档）</el-radio>
            <el-radio value="LOOP_CAP">按倍数重复（限档）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.grantMode === 'LOOP_CAP'" label="最多档次" prop="maxRepeat">
          <el-input-number v-model="form.maxRepeat" :min="1" :max="999" style="width: 160px" />
          <span class="field-tip">档（例：条件3、实际12 → 本来4档，限3则只发3档）</span>
        </el-form-item>
        <el-form-item v-if="form.grantMode === 'LOOP'" label="说明">
          <span class="field-tip">按条件整倍计档，不封顶。例：条件3、实际12 → 发第1～4档共4次</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizLotteryChanceRule">
import {
  listLotteryChanceRule,
  getLotteryChanceRule,
  addLotteryChanceRule,
  updateLotteryChanceRule,
  delLotteryChanceRule,
  getSoleLotteryActivity
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const soleActivityId = ref<number>()

const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 10, ruleName: undefined, status: undefined, activityId: undefined as number | undefined },
  rules: {
    ruleName: [{ required: true, message: "请填写规则名称", trigger: "blur" }],
    grantAmount: [{ required: true, message: "请填写每档发放次数", trigger: "blur" }],
    grantMode: [{ required: true, message: "请选择发放模式", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function toGrantMode(onceOnly: string, maxRepeat: number) {
  if (onceOnly !== "0") return "ONCE"
  if (!maxRepeat || maxRepeat <= 0) return "LOOP"
  return "LOOP_CAP"
}

function onGrantModeChange(mode: string) {
  if (mode === "ONCE") {
    form.value.maxRepeat = 1
  } else if (mode === "LOOP") {
    form.value.maxRepeat = 0
  } else if (mode === "LOOP_CAP" && (!form.value.maxRepeat || form.value.maxRepeat < 1)) {
    form.value.maxRepeat = 3
  }
}

function getList() {
  loading.value = true
  listLotteryChanceRule(queryParams.value).then((res: any) => {
    dataList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
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
    ruleId: undefined,
    activityId: soleActivityId.value,
    ruleName: "",
    checkinDays: 3,
    inviteKycCount: 3,
    grantAmount: 1,
    grantMode: "ONCE",
    onceOnly: "1",
    maxRepeat: 1,
    sort: 0,
    status: "0",
    remark: ""
  }
}

function handleAdd() {
  if (!soleActivityId.value) {
    proxy.$modal.msgWarning("请先初始化抽奖活动")
    return
  }
  reset()
  open.value = true
  title.value = "新增获次规则"
}

function handleUpdate(row: any) {
  getLotteryChanceRule(row.ruleId).then((res: any) => {
    const d = res.data || {}
    const onceOnly = d.onceOnly === "0" ? "0" : "1"
    const maxRepeat = d.maxRepeat == null ? (onceOnly === "0" ? 0 : 1) : Number(d.maxRepeat)
    form.value = {
      ...d,
      onceOnly,
      maxRepeat,
      grantMode: toGrantMode(onceOnly, maxRepeat)
    }
    open.value = true
    title.value = "修改获次规则"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    if ((form.value.checkinDays || 0) <= 0 && (form.value.inviteKycCount || 0) <= 0) {
      proxy.$modal.msgError("签到天数与直推实名人数至少配置一项")
      return
    }
    const mode = form.value.grantMode || "ONCE"
    let onceOnly = "1"
    let maxRepeat = 1
    if (mode === "LOOP") {
      onceOnly = "0"
      maxRepeat = 0
    } else if (mode === "LOOP_CAP") {
      onceOnly = "0"
      maxRepeat = Number(form.value.maxRepeat || 0)
      if (maxRepeat < 1) {
        proxy.$modal.msgError("限档模式下，最多档次数须≥1")
        return
      }
    }
    const payload = {
      ...form.value,
      activityId: form.value.activityId || soleActivityId.value,
      onceOnly,
      maxRepeat
    }
    delete payload.grantMode
    const req = payload.ruleId ? updateLotteryChanceRule(payload) : addLotteryChanceRule(payload)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除规则「' + row.ruleName + '」？').then(() => delLotteryChanceRule(row.ruleId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getSoleLotteryActivity().then((res: any) => {
  soleActivityId.value = res.data?.activityId
  queryParams.value.activityId = soleActivityId.value
  getList()
}).catch(() => getList())
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.field-tip { margin-left: 8px; font-size: 12px; color: var(--el-text-color-secondary); }
</style>
