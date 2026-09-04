<template>
  <div class="app-container ops-page">
    <div class="ops-section-card">
      <div class="ops-section-card__hd">充值入账</div>
      <div class="ops-section-card__bd">
        <el-form :inline="true" v-loading="creditLoading">
          <el-form-item label="到账钱包">
            <WalletTypeSelect v-model="rechargeWalletType" />
            <span class="tip">审核通过后的充值进这个钱包，默认余额且不能提现</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveCredit" v-hasPermi="['biz:recharge:audit']">保存</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
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
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['biz:recharge:list']">导出{{ selectedRows.length ? `（${selectedRows.length}）` : "" }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
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
          <el-button
            link
            type="primary"
            :disabled="scope.row.status !== '0'"
            @click="handleAudit(scope.row, '1')"
            v-hasPermi="['biz:recharge:audit']"
          >通过</el-button>
          <el-button
            link
            type="danger"
            :disabled="scope.row.status !== '0'"
            @click="handleAudit(scope.row, '2')"
            v-hasPermi="['biz:recharge:audit']"
          >拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="人工充值" v-model="open" width="420px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="会员" prop="memberId">
          <MemberSelect v-model="form.memberId" width="100%" placeholder="选择会员" />
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
import { listRecharge, addRecharge, auditRecharge, getWalletCreditByBiz, saveWalletCreditByBiz } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const dateRange = ref<string[]>([])
const selectedRows = ref<any[]>([])
const data = reactive({
  form: { memberId: undefined, currency: "CNY", amount: undefined, remark: undefined } as any,
  queryParams: { pageNum: 1, pageSize: 100, memberId: undefined, phone: undefined, currency: undefined, status: undefined },
  rules: {
    memberId: [{ required: true, message: "请选择会员", trigger: "change" }],
    amount: [{ required: true, message: "金额不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)
const creditLoading = ref(false)
const rechargeWalletType = ref("BALANCE")

function loadCredit() {
  creditLoading.value = true
  getWalletCreditByBiz("RECHARGE").then((res: any) => {
    rechargeWalletType.value = res.data?.typeCode || "BALANCE"
  }).finally(() => { creditLoading.value = false })
}
function saveCredit() {
  saveWalletCreditByBiz("RECHARGE", rechargeWalletType.value).then(() => {
    proxy.$modal.msgSuccess("保存成功")
    loadCredit()
  })
}

function getList() {
  loading.value = true
  listRecharge(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows || []
}
function handleExport() {
  const ids = selectedRows.value.map((r: any) => r.rechargeId).filter((id: any) => id != null)
  if (ids.length) {
    proxy.download("biz/recharge/export", { rechargeIds: ids.join(",") }, `recharge_${new Date().getTime()}.xlsx`)
    return
  }
  const { pageNum, pageSize, ...q } = queryParams.value
  proxy.download("biz/recharge/export", proxy.addDateRange(q, dateRange.value), `recharge_${new Date().getTime()}.xlsx`)
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
loadCredit()
</script>

<style scoped>
.tip { margin-left: 12px; color: #909399; font-size: 13px; }
</style>
