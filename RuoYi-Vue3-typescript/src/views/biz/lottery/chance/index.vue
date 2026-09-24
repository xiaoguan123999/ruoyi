<template>
  <div class="app-container ops-page">
    <el-alert
      title="可为任意会员手动增加/扣减抽奖次数（未有余额记录也会自动建账）。正数增加，负数扣减。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-tabs v-model="activeTab">
      <el-tab-pane label="用户余额" name="balance">
        <el-form :model="queryParams" ref="queryRef" :inline="true" class="mb8" v-show="showSearch">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="会员" prop="memberId">
            <MemberSelect v-model="queryParams.memberId" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="openGrant" v-hasPermi="['biz:lotteryChance:adjust']">添加次数</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </el-row>

        <el-table v-loading="loading" :data="dataList">
          <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
          <el-table-column label="手机号" align="center" prop="phone" width="130" />
          <el-table-column label="姓名" align="center" prop="realName" width="100" />
          <el-table-column label="活动" align="left" prop="activityTitle" min-width="120" show-overflow-tooltip />
          <el-table-column label="可用次数" align="center" prop="balance" width="90" />
          <el-table-column label="累计发放" align="center" prop="totalGranted" width="90" />
          <el-table-column label="累计消耗" align="center" prop="totalConsumed" width="90" />
          <el-table-column label="更新时间" align="center" prop="updateTime" width="170">
            <template #default="scope"><span>{{ parseTime(scope.row.updateTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="180" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="openAdjust(scope.row)" v-hasPermi="['biz:lotteryChance:adjust']">调整</el-button>
              <el-button link type="primary" @click="viewLogs(scope.row)">流水</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-tab-pane>

      <el-tab-pane label="变动流水" name="log">
        <el-form :model="logQuery" ref="logQueryRef" :inline="true" class="mb8">
          <el-form-item label="手机号">
            <el-input v-model="logQuery.phone" placeholder="手机号" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="会员">
            <MemberSelect v-model="logQuery.memberId" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="logQuery.changeType" clearable placeholder="类型" style="width: 150px">
              <el-option label="规则发放" value="RULE_GRANT" />
              <el-option label="后台增加" value="ADMIN_GRANT" />
              <el-option label="后台扣减" value="ADMIN_DEDUCT" />
              <el-option label="抽奖消耗" value="DRAW_CONSUME" />
              <el-option label="失败退回" value="DRAW_REFUND" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleLogQuery">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="logLoading" :data="logList">
          <el-table-column label="时间" align="center" prop="createTime" width="170">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
          <el-table-column label="手机号" align="center" prop="phone" width="130" />
          <el-table-column label="类型" align="center" width="100">
            <template #default="scope">{{ changeTypeLabel(scope.row.changeType) }}</template>
          </el-table-column>
          <el-table-column label="变动" align="center" prop="changeAmount" width="80" />
          <el-table-column label="余额" align="center" prop="balanceAfter" width="80" />
          <el-table-column label="规则" align="left" prop="ruleName" min-width="120" show-overflow-tooltip />
          <el-table-column label="备注" align="left" prop="remark" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作者" align="center" prop="createBy" width="100" />
        </el-table>
        <pagination v-show="logTotal > 0" :total="logTotal" v-model:page="logQuery.pageNum" v-model:limit="logQuery.pageSize" @pagination="getLogList" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="adjustForm.mode === 'grant' ? '添加抽奖次数' : '调整抽奖次数'" v-model="adjustOpen" width="480px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="会员" required>
          <MemberSelect
            v-if="adjustForm.mode === 'grant'"
            v-model="adjustForm.memberId"
            width="100%"
            placeholder="搜索手机号或会员ID"
          />
          <span v-else>{{ adjustForm.phone || adjustForm.memberId }}（ID {{ adjustForm.memberId }}）</span>
        </el-form-item>
        <el-form-item v-if="adjustForm.mode !== 'grant'" label="当前余额">
          <span>{{ adjustForm.balance }}</span>
        </el-form-item>
        <el-form-item :label="adjustForm.mode === 'grant' ? '增加次数' : '调整次数'" required>
          <el-input-number
            v-model="adjustForm.amount"
            :min="adjustForm.mode === 'grant' ? 1 : -999"
            :max="999"
            style="width: 160px"
          />
          <span class="field-tip">{{ adjustForm.mode === 'grant' ? '直接增加可用次数' : '正数增加，负数扣减' }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="2" maxlength="200" placeholder="选填，写入变动流水" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="submitting" @click="submitAdjust">确 定</el-button>
        <el-button @click="adjustOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizLotteryChance">
import { listLotteryChance, listLotteryChanceLog, adjustLotteryChance, getSoleLotteryActivity } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const activeTab = ref("balance")
const showSearch = ref(true)
const loading = ref(false)
const logLoading = ref(false)
const submitting = ref(false)
const dataList = ref<any[]>([])
const logList = ref<any[]>([])
const total = ref(0)
const logTotal = ref(0)
const adjustOpen = ref(false)
const soleActivityId = ref<number>()

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  activityId: undefined as number | undefined,
  phone: undefined as string | undefined,
  memberId: undefined as number | undefined
})
const logQuery = ref({
  pageNum: 1,
  pageSize: 10,
  activityId: undefined as number | undefined,
  phone: undefined as string | undefined,
  memberId: undefined as number | undefined,
  changeType: undefined as string | undefined
})
const adjustForm = ref<any>({})

function changeTypeLabel(type: string) {
  const map: Record<string, string> = {
    RULE_GRANT: "规则发放",
    ADMIN_GRANT: "后台增加",
    ADMIN_DEDUCT: "后台扣减",
    DRAW_CONSUME: "抽奖消耗",
    DRAW_REFUND: "失败退回"
  }
  return map[type] || type
}

function getList() {
  loading.value = true
  const q = { ...queryParams.value }
  if (q.memberId !== undefined && q.memberId !== null && (q.memberId as any) !== "") {
    q.memberId = Number(q.memberId)
  } else {
    q.memberId = undefined
  }
  listLotteryChance(q).then((res: any) => {
    dataList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function getLogList() {
  logLoading.value = true
  const q = { ...logQuery.value }
  if (q.memberId !== undefined && q.memberId !== null && (q.memberId as any) !== "") {
    q.memberId = Number(q.memberId)
  } else {
    q.memberId = undefined
  }
  listLotteryChanceLog(q).then((res: any) => {
    logList.value = res.rows || []
    logTotal.value = res.total || 0
  }).finally(() => { logLoading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.phone = undefined
  queryParams.value.memberId = undefined
  handleQuery()
}

function handleLogQuery() {
  logQuery.value.pageNum = 1
  getLogList()
}

function openGrant() {
  if (!soleActivityId.value) {
    proxy.$modal.msgWarning("请先配置并启用抽奖活动")
    return
  }
  adjustForm.value = {
    mode: "grant",
    activityId: soleActivityId.value,
    memberId: undefined,
    amount: 1,
    remark: "后台手动添加"
  }
  adjustOpen.value = true
}

function openAdjust(row: any) {
  adjustForm.value = {
    mode: "adjust",
    activityId: row.activityId || soleActivityId.value,
    memberId: row.memberId,
    phone: row.phone,
    balance: row.balance,
    amount: 1,
    remark: ""
  }
  adjustOpen.value = true
}

function submitAdjust() {
  if (!adjustForm.value.memberId) {
    proxy.$modal.msgWarning("请选择会员")
    return
  }
  if (!adjustForm.value.activityId) {
    proxy.$modal.msgWarning("缺少活动信息")
    return
  }
  const amount = Number(adjustForm.value.amount)
  if (!amount || amount === 0) {
    proxy.$modal.msgWarning(adjustForm.value.mode === "grant" ? "请填写增加次数" : "请填写非0调整次数")
    return
  }
  if (adjustForm.value.mode === "grant" && amount < 0) {
    proxy.$modal.msgWarning("添加次数须为正数")
    return
  }
  submitting.value = true
  adjustLotteryChance({
    activityId: adjustForm.value.activityId,
    memberId: Number(adjustForm.value.memberId),
    amount,
    remark: adjustForm.value.remark
  }).then(() => {
    proxy.$modal.msgSuccess(adjustForm.value.mode === "grant" ? "添加成功" : "调整成功")
    adjustOpen.value = false
    getList()
    getLogList()
  }).finally(() => {
    submitting.value = false
  })
}

function viewLogs(row: any) {
  activeTab.value = "log"
  logQuery.value.memberId = row.memberId
  logQuery.value.phone = undefined
  handleLogQuery()
}

getSoleLotteryActivity().then((res: any) => {
  soleActivityId.value = res.data?.activityId
  queryParams.value.activityId = soleActivityId.value
  logQuery.value.activityId = soleActivityId.value
  getList()
  getLogList()
}).catch(() => {
  getList()
  getLogList()
})
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.field-tip { margin-left: 8px; font-size: 12px; color: var(--el-text-color-secondary); }
</style>
