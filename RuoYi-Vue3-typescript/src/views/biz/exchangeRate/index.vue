<template>
  <div class="app-container ops-page">
    <el-alert
      title="全局 USDT 兑人民币汇率，用于等级奖励等业务的币种换算。未配置时默认 6.25。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <div class="ops-section-card mb8">
      <div class="ops-section-card__hd">当前汇率</div>
      <div class="ops-section-card__bd">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" v-loading="formLoading">
          <el-form-item label="USDT/CNY汇率" prop="usdtToCny">
            <el-input-number v-model="form.usdtToCny" :min="0.01" :precision="4" :step="0.01" controls-position="right" style="width: 220px" />
            <div class="field-tip">须大于 0，保存后立即生效</div>
          </el-form-item>
          <el-form-item label-width="0">
            <el-button type="primary" @click="save" v-hasPermi="['biz:levelReward:edit']">保存</el-button>
            <el-button @click="load">重新加载</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="ops-section-card">
      <div class="ops-section-card__hd">更改记录</div>
      <div class="ops-section-card__bd">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="操作人" prop="operator">
            <el-input v-model="queryParams.operator" placeholder="操作人" clearable style="width: 160px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="时间">
            <el-date-picker
              v-model="dateRange"
              value-format="YYYY-MM-DD"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-table v-loading="listLoading" :data="dataList" style="width: 100%">
          <el-table-column label="ID" align="center" prop="logId" width="80" />
          <el-table-column label="调整前" align="center" prop="oldRate" min-width="100" />
          <el-table-column label="调整后" align="center" prop="newRate" min-width="100" />
          <el-table-column label="操作人" align="center" prop="operator" width="120" show-overflow-tooltip />
          <el-table-column label="操作时间" align="center" prop="createTime" width="170">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="备注" align="center" prop="remark" min-width="140" show-overflow-tooltip />
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="BizExchangeRate">
import { getLevelRewardRule, saveLevelRewardRule, listLevelRewardFxLog } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const formLoading = ref(false)
const listLoading = ref(false)
const showSearch = ref(true)
const dataList = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const queryParams = ref({
  pageNum: 1,
  pageSize: 100,
  operator: undefined as string | undefined
})
const form = ref({
  usdtToCny: 6.25,
  enabled: true,
  mixedPayCurrency: "USDT",
  performanceSource: "SUBSCRIBE",
  includeSelf: false,
  validNeedKyc: true,
  validNeedOrder: true,
  ruleText: "",
  hint: ""
})
const rules = {
  usdtToCny: [
    { required: true, message: "汇率不能为空", trigger: "blur" },
    {
      validator: (_rule: any, value: number, callback: (err?: Error) => void) => {
        if (value == null || Number(value) <= 0) callback(new Error("汇率须大于 0"))
        else callback()
      },
      trigger: "blur"
    }
  ]
}

function load() {
  formLoading.value = true
  getLevelRewardRule().then((res: any) => {
    form.value = Object.assign(form.value, res.data || {})
    if (form.value.usdtToCny == null || Number(form.value.usdtToCny) <= 0) {
      form.value.usdtToCny = 6.25
    }
  }).finally(() => { formLoading.value = false })
}

function save() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    saveLevelRewardRule(form.value).then(() => {
      proxy.$modal.msgSuccess("保存成功")
      load()
      getList()
    })
  })
}

function getList() {
  listLoading.value = true
  listLevelRewardFxLog(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
  }).finally(() => { listLoading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

load()
getList()
</script>

<style scoped>
.field-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}
</style>
