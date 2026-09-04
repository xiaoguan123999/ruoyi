<template>
  <div class="app-container ops-page">
    <el-alert
      title="会员提交后为「审核中」并冻结余额。确认要打款后改为「待打款」；线下出款完成再点「提现成功」扣冻结。「提现失败」则解冻退回。勾选后，批量操作和导出只处理勾选行；未勾选则处理当前搜索结果。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="单号" prop="withdrawId">
        <el-input v-model="queryParams.withdrawId" placeholder="提现单号" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
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
          <el-option label="审核中" value="0" />
          <el-option label="待打款" value="3" />
          <el-option label="提现成功" value="1" />
          <el-option label="提现失败" value="2" />
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
        <el-button type="primary" plain icon="Setting" @click="openRuleDialog" v-hasPermi="['biz:withdraw:audit']">提现规则</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Finished" :loading="batchLoading" @click="handleBatch('3')" v-hasPermi="['biz:withdraw:audit']">批量审核通过{{ selectionSuffix }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="CircleCheck" :loading="batchLoading" @click="handleBatch('1')" v-hasPermi="['biz:withdraw:audit']">批量确认打款{{ selectionSuffix }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="CircleClose" :loading="batchLoading" @click="handleBatch('2')" v-hasPermi="['biz:withdraw:audit']">批量审核驳回{{ selectionSuffix }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['biz:withdraw:list']">导出Excel{{ selectionSuffix }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" v-loading="loading" :data="dataList" row-key="withdrawId" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" reserve-selection />
      <el-table-column label="单号" align="center" prop="withdrawId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="姓名" align="center" prop="realName" width="100">
        <template #default="scope">{{ scope.row.realName || "—" }}</template>
      </el-table-column>
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="钱包" align="center" width="110">
        <template #default="scope">{{ scope.row.walletTypeName || scope.row.walletTypeCode || "—" }}</template>
      </el-table-column>
      <el-table-column label="金额" align="center" prop="amount" width="110" />
      <el-table-column label="手续费" align="center" prop="feeAmount" width="100">
        <template #default="scope">{{ scope.row.feeAmount ?? "0" }}</template>
      </el-table-column>
      <el-table-column label="到账" align="center" prop="arrivalAmount" width="110">
        <template #default="scope">{{ scope.row.arrivalAmount ?? scope.row.amount }}</template>
      </el-table-column>
      <el-table-column label="收款方式" align="center" width="100">
        <template #default="scope">{{ scope.row.payMethodLabel || scope.row.payMethod || "—" }}</template>
      </el-table-column>
      <el-table-column label="收款信息" align="center" prop="accountInfo" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="warning">审核中</el-tag>
          <el-tag v-else-if="scope.row.status === '3'" type="primary">待打款</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">提现成功</el-tag>
          <el-tag v-else type="danger">提现失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核备注" align="center" prop="auditRemark" min-width="160" :show-overflow-tooltip="true">
        <template #default="scope">{{ scope.row.auditRemark || "—" }}</template>
      </el-table-column>
      <el-table-column label="凭证" align="center" width="90">
        <template #default="scope">
          <el-image
            v-if="scope.row.payProofUrl"
            :src="imgSrc(scope.row.payProofUrl)"
            :preview-src-list="[imgSrc(scope.row.payProofUrl)]"
            preview-teleported
            fit="contain"
            style="width: 40px; height: 40px"
          />
          <span v-else style="color: #909399">—</span>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="审核时间" align="center" prop="auditTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.auditTime) || "—" }}</span></template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="auditBy" width="100" />
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="primary"
            @click="openAudit(scope.row, '3')"
            v-hasPermi="['biz:withdraw:audit']"
          >改为待打款</el-button>
          <el-button
            v-if="scope.row.status === '3'"
            link
            type="primary"
            @click="openAudit(scope.row, '1')"
            v-hasPermi="['biz:withdraw:audit']"
          >提现成功</el-button>
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '3'"
            link
            type="danger"
            @click="openAudit(scope.row, '2')"
            v-hasPermi="['biz:withdraw:audit']"
          >提现失败</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog
      title="提现规则"
      v-model="ruleOpen"
      width="520px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="ruleRef" :model="rule" :rules="ruleRules" label-width="120px" v-loading="ruleLoading">
        <el-form-item label="人民币最低" prop="minCny">
          <el-input-number v-model="rule.minCny" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="人民币最高" prop="maxCny">
          <el-input-number v-model="rule.maxCny" :min="0" :precision="2" :step="1" style="width: 100%" />
          <div class="tip tip-block">最高填 0 表示不限</div>
        </el-form-item>
        <el-form-item label="USDT最低" prop="minUsdt">
          <el-input-number v-model="rule.minUsdt" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="USDT最高" prop="maxUsdt">
          <el-input-number v-model="rule.maxUsdt" :min="0" :precision="2" :step="1" style="width: 100%" />
          <div class="tip tip-block">最高填 0 表示不限</div>
        </el-form-item>
        <el-form-item label="手续费(%)" prop="feeRate">
          <el-input-number v-model="rule.feeRate" :min="0" :max="100" :precision="2" :step="0.1" style="width: 100%" />
          <div class="tip tip-block">从申请金额扣，0 表示免手续费；确认打款时按「到账」金额转账</div>
        </el-form-item>
        <el-form-item label="提现需实名">
          <div class="switch-with-tip">
            <el-switch v-model="rule.needKyc" />
            <div class="tip tip-block">开启后，未通过实名认证的会员不能提交提现</div>
          </div>
        </el-form-item>
        <el-form-item label="开放USDT充提">
          <div class="switch-with-tip">
            <el-switch v-model="rule.usdtEnabled" />
            <div class="tip tip-block">关闭后 App 不能充值/提现 USDT</div>
          </div>
        </el-form-item>
        <el-form-item label="产品收益钱包">
          <WalletTypeSelect v-model="rule.productWalletType" placeholder="提现钱包" />
          <div class="tip tip-block">App「产品收益」从这里扣</div>
        </el-form-item>
        <el-form-item label="推广收益钱包">
          <WalletTypeSelect v-model="rule.promoWalletType" placeholder="提现钱包" />
          <div class="tip tip-block">App「推广收益」从这里扣</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveRule" v-hasPermi="['biz:withdraw:audit']">保存规则</el-button>
        <el-button @click="ruleOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="auditTitle" v-model="open" width="520px" append-to-body>
      <el-descriptions :column="1" border class="mb8">
        <el-descriptions-item label="会员">{{ current.realName || current.phone }}（ID {{ current.memberId }}）</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ current.amount }} {{ current.currency }}</el-descriptions-item>
        <el-descriptions-item label="手续费">{{ current.feeAmount ?? 0 }} {{ current.currency }}</el-descriptions-item>
        <el-descriptions-item label="到账金额">{{ current.arrivalAmount ?? current.amount }} {{ current.currency }}</el-descriptions-item>
        <el-descriptions-item label="钱包">{{ current.walletTypeName || current.walletTypeCode || "—" }}</el-descriptions-item>
        <el-descriptions-item label="收款方式">{{ current.payMethodLabel || current.payMethod }}</el-descriptions-item>
        <el-descriptions-item label="收款信息">{{ current.accountInfo }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="form.status === '1'" label="打款凭证">
          <image-upload v-model="form.payProofUrl" :limit="1" />
          <div class="el-upload__tip">建议上传转账截图，方便对账。</div>
        </el-form-item>
        <el-form-item :label="remarkLabel" prop="auditRemark">
          <el-input v-model="form.auditRemark" type="textarea" :rows="3" :placeholder="remarkPlaceholder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAudit">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="批量审核驳回" v-model="batchFailOpen" width="480px" append-to-body>
      <el-form label-width="100px">
        <div class="tip tip-block" style="margin-bottom: 12px">将把{{ batchScopeText() }}审核驳回并解冻退回。</div>
        <el-form-item label="驳回原因" required>
          <el-input v-model="batchFailRemark" type="textarea" :rows="3" placeholder="请填写驳回原因，将写入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="batchLoading" @click="submitBatchFail">确 定</el-button>
        <el-button @click="batchFailOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizWithdraw">
import { listWithdraw, auditWithdraw, auditWithdrawBatch, getWithdrawConfig, saveWithdrawConfig } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"
import { isExternal } from "@/utils/validate"

const { proxy } = getCurrentInstance() as any
const ruleOpen = ref(false)
const ruleLoading = ref(false)
const rule = ref({ minCny: 105, maxCny: 0, minUsdt: 105, maxUsdt: 0, feeRate: 3, needKyc: false, usdtEnabled: true, productWalletType: "PRODUCT", promoWalletType: "PROMO" })
const ruleRules = {
  minCny: [{ required: true, message: "请填写人民币最低提现", trigger: "blur" }],
  minUsdt: [{ required: true, message: "请填写USDT最低提现", trigger: "blur" }]
}
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const current = ref<any>({})
const dateRange = ref<string[]>([])
const tableRef = ref<any>()
const selectedRows = ref<any[]>([])
const batchLoading = ref(false)
const selectionSuffix = computed(() => selectedRows.value.length ? `（已选${selectedRows.value.length}）` : "（按筛选）")
const batchFailOpen = ref(false)
const batchFailRemark = ref("")
const queryParams = ref({ pageNum: 1, pageSize: 100, withdrawId: undefined, memberId: undefined, phone: undefined, currency: undefined, status: undefined })
const route = useRoute()
function applyRouteQuery() {
  const status = String(route.query.status || "")
  if (status === "0" || status === "1" || status === "2" || status === "3") {
    queryParams.value.status = status
  }
}
const form = ref({ id: undefined as number | undefined, status: "1", auditRemark: "", payProofUrl: "" })
const rules = {
  auditRemark: [{
    validator: (_: any, value: string, callback: (e?: Error) => void) => {
      if (form.value.status === "2" && !value) {
        callback(new Error("请填写提现失败原因"))
        return
      }
      callback()
    },
    trigger: "blur"
  }]
}
const auditTitle = computed(() => {
  if (form.value.status === "3") return "改为待打款"
  if (form.value.status === "1") return "提现成功"
  return "提现失败"
})
const remarkLabel = computed(() => {
  if (form.value.status === "3") return "审核备注"
  if (form.value.status === "1") return "打款备注"
  return "失败原因"
})
const remarkPlaceholder = computed(() => {
  if (form.value.status === "2") return "请填写提现失败原因"
  return "可选"
})

function imgSrc(url: string) {
  if (!url) return ""
  if (isExternal(url)) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function loadRule() {
  ruleLoading.value = true
  return getWithdrawConfig().then((res: any) => {
    const data = res.data || {}
    rule.value = {
      minCny: Number(data.minCny ?? 105),
      maxCny: Number(data.maxCny ?? 0),
      minUsdt: Number(data.minUsdt ?? 105),
      maxUsdt: Number(data.maxUsdt ?? 0),
      feeRate: Number(data.feeRate ?? 3),
      needKyc: data.needKyc === true,
      usdtEnabled: data.usdtEnabled !== false,
      productWalletType: data.productWalletType || "PRODUCT",
      promoWalletType: data.promoWalletType || "PROMO"
    }
  }).finally(() => { ruleLoading.value = false })
}
function openRuleDialog() {
  ruleOpen.value = true
  loadRule()
}
function saveRule() {
  proxy.$refs["ruleRef"].validate((valid: boolean) => {
    if (!valid) return
    saveWithdrawConfig(rule.value).then(() => {
      proxy.$modal.msgSuccess("提现规则已保存")
      ruleOpen.value = false
      loadRule()
    })
  })
}
function getList() {
  loading.value = true
  listWithdraw(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleExport() {
  const ids = selectedRows.value.map((r: any) => r.withdrawId).filter((id: any) => id != null)
  if (ids.length) {
    proxy.download("biz/withdraw/export", { withdrawIds: ids.join(",") }, `withdraw_${new Date().getTime()}.xlsx`)
    return
  }
  const { pageNum, pageSize, ...q } = queryParams.value
  proxy.download("biz/withdraw/export", proxy.addDateRange(q, dateRange.value), `withdraw_${new Date().getTime()}.xlsx`)
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows || []
}
function auditSuccessText(status: string) {
  if (status === "3") return "已改为待打款"
  if (status === "1") return "已标记提现成功"
  return "已标记提现失败并解冻"
}
function openAudit(row: any, status: string) {
  current.value = row
  form.value = { id: row.withdrawId, status, auditRemark: "", payProofUrl: "" }
  open.value = true
}
function submitAudit() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    auditWithdraw({
      id: form.value.id,
      status: form.value.status,
      auditRemark: form.value.auditRemark,
      payProofUrl: form.value.payProofUrl
    }).then(() => {
      proxy.$modal.msgSuccess(auditSuccessText(form.value.status))
      open.value = false
      getList()
    })
  })
}
function currentFilter() {
  const range = Array.isArray(dateRange.value) ? dateRange.value : []
  return {
    withdrawId: queryParams.value.withdrawId,
    memberId: queryParams.value.memberId,
    phone: queryParams.value.phone,
    currency: queryParams.value.currency,
    filterStatus: queryParams.value.status,
    beginTime: range[0],
    endTime: range[1]
  }
}
function batchScopeText() {
  if (selectedRows.value.length) {
    return `勾选的 ${selectedRows.value.length} 笔`
  }
  const n = total.value
  return n > 0 ? `当前筛选共 ${n} 笔` : "当前筛选结果"
}
function handleBatch(status: string) {
  if (status === "2") {
    batchFailRemark.value = ""
    batchFailOpen.value = true
    return
  }
  const name = status === "3" ? "审核通过（待打款）" : "确认打款（提现成功）"
  const extra = status === "3"
    ? "余额继续冻结，不会扣款。"
    : "将立即扣冻结。仅「待打款」状态会成功，审核中的单请先批量审核通过。"
  proxy.$modal.confirm(`确认对${batchScopeText()}执行「${name}」？${extra}`).then(() => {
    return runBatch(status, "")
  }).catch(() => {})
}
function submitBatchFail() {
  if (!batchFailRemark.value.trim()) {
    proxy.$modal.msgWarning("请填写驳回原因")
    return
  }
  runBatch("2", batchFailRemark.value.trim()).then(() => {
    batchFailOpen.value = false
  })
}
function runBatch(status: string, auditRemark: string) {
  const ids = selectedRows.value.map((r: any) => r.withdrawId).filter((id: any) => id != null)
  const data: any = {
    status,
    auditRemark,
    ...currentFilter()
  }
  if (ids.length) {
    data.ids = ids
  }
  batchLoading.value = true
  return auditWithdrawBatch(data).then((res: any) => {
    proxy.$modal.msgSuccess(res.msg || auditSuccessText(status))
    selectedRows.value = []
    tableRef.value?.clearSelection?.()
    getList()
  }).finally(() => {
    batchLoading.value = false
  })
}
applyRouteQuery()
getList()
watch(
  () => String(route.query.status || ""),
  () => {
    applyRouteQuery()
    handleQuery()
  }
)
</script>

<style scoped>
.tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.tip-block {
  display: block;
  margin-left: 0;
  margin-top: 6px;
  line-height: 1.4;
}
.switch-with-tip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
</style>
