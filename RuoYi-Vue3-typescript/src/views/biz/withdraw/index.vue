<template>
  <div class="app-container">
    <el-alert
      title="会员提交提现后会冻结余额。请先线下打款，再点「确认打款」扣冻结；拒绝则解冻退回。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="单号" prop="withdrawId">
        <el-input v-model="queryParams.withdrawId" placeholder="提现单号" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
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
          <el-option label="待打款" value="0" />
          <el-option label="已打款" value="1" />
          <el-option label="已拒绝" value="2" />
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="单号" align="center" prop="withdrawId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="姓名" align="center" prop="realName" width="100">
        <template #default="scope">{{ scope.row.realName || "—" }}</template>
      </el-table-column>
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="金额" align="center" prop="amount" width="110" />
      <el-table-column label="收款方式" align="center" width="100">
        <template #default="scope">{{ scope.row.payMethodLabel || scope.row.payMethod || "—" }}</template>
      </el-table-column>
      <el-table-column label="收款信息" align="center" prop="accountInfo" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="warning">待打款</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已打款</el-tag>
          <el-tag v-else type="danger">已拒绝</el-tag>
        </template>
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
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="primary" @click="openAudit(scope.row, '1')" v-hasPermi="['biz:withdraw:audit']">确认打款</el-button>
            <el-button link type="danger" @click="openAudit(scope.row, '2')" v-hasPermi="['biz:withdraw:audit']">拒绝</el-button>
          </template>
          <span v-else>{{ scope.row.auditRemark || "—" }}</span>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="auditTitle" v-model="open" width="520px" append-to-body>
      <el-descriptions :column="1" border class="mb8">
        <el-descriptions-item label="会员">{{ current.realName || current.phone }}（ID {{ current.memberId }}）</el-descriptions-item>
        <el-descriptions-item label="金额">{{ current.amount }} {{ current.currency }}</el-descriptions-item>
        <el-descriptions-item label="收款方式">{{ current.payMethodLabel || current.payMethod }}</el-descriptions-item>
        <el-descriptions-item label="收款信息">{{ current.accountInfo }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="form.status === '1'" label="打款凭证">
          <image-upload v-model="form.payProofUrl" :limit="1" />
          <div class="el-upload__tip">建议上传转账截图，方便对账。</div>
        </el-form-item>
        <el-form-item :label="form.status === '1' ? '打款备注' : '拒绝原因'" prop="auditRemark">
          <el-input v-model="form.auditRemark" type="textarea" :rows="3" :placeholder="form.status === '1' ? '可选，例如流水号' : '请填写拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAudit">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizWithdraw">
import { listWithdraw, auditWithdraw } from "@/api/biz"
import { isExternal } from "@/utils/validate"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const current = ref<any>({})
const dateRange = ref<string[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 10, withdrawId: undefined, memberId: undefined, phone: undefined, currency: undefined, status: undefined })
const route = useRoute()
function applyRouteQuery() {
  const status = String(route.query.status || "")
  if (status === "0" || status === "1" || status === "2") {
    queryParams.value.status = status
  }
}
const form = ref({ id: undefined as number | undefined, status: "1", auditRemark: "", payProofUrl: "" })
const rules = {
  auditRemark: [{
    validator: (_: any, value: string, callback: (e?: Error) => void) => {
      if (form.value.status === "2" && !value) {
        callback(new Error("请填写拒绝原因"))
        return
      }
      callback()
    },
    trigger: "blur"
  }]
}
const auditTitle = computed(() => form.value.status === "1" ? "确认已打款" : "拒绝提现")

function imgSrc(url: string) {
  if (!url) return ""
  if (isExternal(url)) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function getList() {
  loading.value = true
  listWithdraw(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
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
      proxy.$modal.msgSuccess(form.value.status === "1" ? "已确认打款" : "已拒绝并解冻")
      open.value = false
      getList()
    })
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
