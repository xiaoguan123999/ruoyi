<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员ID" prop="memberId">
        <el-input v-model="queryParams.memberId" placeholder="会员ID" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="币种" clearable style="width: 120px">
          <el-option label="CNY" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="待审" value="0" />
          <el-option label="通过" value="1" />
          <el-option label="拒绝" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:recharge:add']">人工充值</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="单号" align="center" prop="rechargeId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="金额" align="center" prop="amount" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="warning">待审</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">通过</el-tag>
          <el-tag v-else type="danger">拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请备注" align="center" prop="remark" min-width="140" show-overflow-tooltip />
      <el-table-column label="申请时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="auditBy" width="100" />
      <el-table-column label="审核时间" align="center" prop="auditTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.auditTime) || "—" }}</span></template>
      </el-table-column>
      <el-table-column label="审核备注" align="center" prop="auditRemark" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="primary" @click="handleAudit(scope.row, '1')" v-hasPermi="['biz:recharge:audit']">通过</el-button>
            <el-button link type="danger" @click="handleAudit(scope.row, '2')" v-hasPermi="['biz:recharge:audit']">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="人工充值" v-model="open" width="420px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="会员ID" prop="memberId">
          <el-input v-model="form.memberId" placeholder="会员ID/邀请码" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="form.currency" style="width: 100%">
            <el-option label="CNY" value="CNY" />
            <el-option label="USDT" value="USDT" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizRecharge">
import { listRecharge, addRecharge, auditRecharge } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const dateRange = ref<string[]>([])
const data = reactive({
  form: { memberId: undefined, currency: "CNY", amount: undefined, remark: undefined } as any,
  queryParams: { pageNum: 1, pageSize: 10, memberId: undefined, phone: undefined, currency: undefined, status: undefined },
  rules: {
    memberId: [{ required: true, message: "会员ID不能为空", trigger: "blur" }],
    amount: [{ required: true, message: "金额不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listRecharge(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() {
  form.value = { memberId: undefined, currency: "CNY", amount: undefined, remark: undefined }
  open.value = true
}
function submitAdd() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    addRecharge(form.value).then(() => {
      proxy.$modal.msgSuccess("已提交，请审核通过后入账")
      open.value = false
      getList()
    })
  })
}
function handleAudit(row: any, status: string) {
  const tip = status === "1" ? "通过并入账" : "拒绝"
  proxy.$modal.confirm("确认" + tip + "该充值单？").then(() => auditRecharge({ id: row.rechargeId, status })).then(() => {
    proxy.$modal.msgSuccess("操作成功")
    getList()
  }).catch(() => {})
}
getList()
</script>
