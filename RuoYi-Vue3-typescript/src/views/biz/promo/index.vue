<template>
  <div class="app-container ops-page">
    <el-alert
      title="实名后用户自行领取 14 元或 2 USDT（可改）；被邀请人实名后自动给邀请人发推广奖；充值通过后按三级比例返佣。金额、比例、开关和规则说明都在本页配置。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form ref="ruleRef" :model="rule" :rules="rules" label-width="170px" v-loading="ruleLoading" class="ops-form-full">
      <el-divider content-position="left">总开关</el-divider>
      <el-form-item label="注册推广总开关">
        <el-switch v-model="rule.enabled" />
        <span class="tip">关闭后实名自领和邀请奖励都不发，不影响三级返佣</span>
      </el-form-item>

      <el-divider content-position="left">一、实名注册奖励</el-divider>
      <el-form-item label="实名注册奖励">
        <el-switch v-model="rule.kycSelfEnabled" />
        <span class="tip">通过实名后，用户在 App 任选人民币或 USDT 领一次</span>
      </el-form-item>
      <el-form-item label="人民币金额" prop="kycRewardCny">
        <el-input-number v-model="rule.kycRewardCny" :min="0" :precision="2" :step="1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="USDT金额" prop="kycRewardUsdt">
        <el-input-number v-model="rule.kycRewardUsdt" :min="0" :precision="2" :step="1" style="width: 240px" />
      </el-form-item>

      <el-divider content-position="left">二、实名推广奖励</el-divider>
      <el-form-item label="实名推广奖励">
        <el-switch v-model="rule.inviteEnabled" />
        <span class="tip">被邀请人完成实名后，自动发给邀请人</span>
      </el-form-item>
      <el-form-item label="奖励金额" prop="inviteAmount">
        <el-input-number v-model="rule.inviteAmount" :min="0" :precision="2" :step="1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="奖励币种">
        <el-radio-group v-model="rule.inviteCurrency">
          <el-radio value="CNY">人民币</el-radio>
          <el-radio value="USDT">USDT</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="上下级不可转移">
        <el-switch v-model="rule.lockParent" />
        <span class="tip">注册时绑定邀请码，之后不能改上级。请提醒用户核对邀请码</span>
      </el-form-item>

      <el-divider content-position="left">三、团队返佣</el-divider>
      <el-form-item label="团队返佣开关">
        <el-switch v-model="rule.teamEnabled" />
        <span class="tip">关闭后充值审核通过不再给上级分佣</span>
      </el-form-item>
      <el-form-item label="一级返佣(%)" prop="teamRateL1">
        <el-input-number v-model="rule.teamRateL1" :min="0" :max="100" :precision="2" :step="1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="二级返佣(%)" prop="teamRateL2">
        <el-input-number v-model="rule.teamRateL2" :min="0" :max="100" :precision="2" :step="1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="三级返佣(%)" prop="teamRateL3">
        <el-input-number v-model="rule.teamRateL3" :min="0" :max="100" :precision="2" :step="1" style="width: 240px" />
      </el-form-item>

      <el-divider content-position="left">规则说明</el-divider>
      <el-form-item label="App展示全文">
        <el-input v-model="rule.ruleText" type="textarea" :rows="10" maxlength="500" show-word-limit placeholder="展示给 App 邀请页 / 规则页" />
        <div class="tip" style="margin-left: 0; margin-top: 6px">GET /app/promo 和 GET /app/invite 的 ruleText。改金额后可点「按当前数值生成」同步文案。</div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveRule" v-hasPermi="['biz:promo:edit']">保存规则</el-button>
        <el-button @click="fillRuleText">按当前数值生成说明</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">发放记录</el-divider>
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="收款手机" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="收款人手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="grantType">
        <el-select v-model="queryParams.grantType" placeholder="全部" clearable style="width: 160px">
          <el-option label="实名自领" value="KYC_SELF" />
          <el-option label="推广奖励" value="INVITE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="grantId" width="80" />
      <el-table-column label="收款会员" align="center" prop="memberId" width="100" />
      <el-table-column label="收款手机" align="center" prop="phone" width="130" />
      <el-table-column label="类型" align="center" width="110">
        <template #default="scope">{{ scope.row.grantType === 'INVITE' ? '推广奖励' : '实名自领' }}</template>
      </el-table-column>
      <el-table-column label="来源会员" align="center" prop="fromMemberId" width="100" />
      <el-table-column label="来源手机" align="center" prop="fromPhone" width="130" />
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="金额" align="center" prop="amount" width="100" />
      <el-table-column label="时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="120" />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizPromoRule">
import { getPromoRule, savePromoRule, listPromoGrant } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const ruleLoading = ref(false)
const loading = ref(false)
const dataList = ref<any[]>([])
const total = ref(0)
const rule = ref({
  enabled: true,
  kycSelfEnabled: true,
  kycRewardCny: 14,
  kycRewardUsdt: 2,
  inviteEnabled: true,
  inviteAmount: 2,
  inviteCurrency: "CNY",
  lockParent: true,
  teamEnabled: true,
  teamRateL1: 9,
  teamRateL2: 3,
  teamRateL3: 1,
  ruleText: ""
})
const queryParams = ref({ pageNum: 1, pageSize: 100, phone: undefined as string | undefined, grantType: undefined as string | undefined })
const rules = {
  kycRewardCny: [{ required: true, message: "请填写实名注册奖励人民币", trigger: "blur" }],
  kycRewardUsdt: [{ required: true, message: "请填写实名注册奖励USDT", trigger: "blur" }],
  inviteAmount: [{ required: true, message: "请填写推广奖励金额", trigger: "blur" }],
  teamRateL1: [{ required: true, message: "请填写一级返佣", trigger: "blur" }],
  teamRateL2: [{ required: true, message: "请填写二级返佣", trigger: "blur" }],
  teamRateL3: [{ required: true, message: "请填写三级返佣", trigger: "blur" }]
}

function fmt(v: any) {
  const n = Number(v ?? 0)
  if (!Number.isFinite(n)) return "0"
  return String(n)
}

function buildRuleText() {
  const unit = rule.value.inviteCurrency === "USDT" ? " USDT" : " 元"
  return [
    "用户注册与推广奖励规则：",
    "一、实名注册奖励",
    `新用户完成注册并通过实名认证后，可获得 ${fmt(rule.value.kycRewardCny)} 元或 ${fmt(rule.value.kycRewardUsdt)} USDT 平台余额，两种奖励方式任选其一。`,
    "二、实名推广奖励",
    `每成功邀请 1 名新用户完成实名注册，邀请人可获得 ${fmt(rule.value.inviteAmount)}${unit}推广奖励。上下级不可以转移，请核对好正确的邀请码再注册。`,
    "三、团队返佣机制",
    `一级返佣 ${fmt(rule.value.teamRateL1)}%、二级返佣 ${fmt(rule.value.teamRateL2)}%、三级返佣 ${fmt(rule.value.teamRateL3)}%`,
    "",
    "奖励资格、返佣计算及发放结果以平台系统实际核算为准；如发现异常注册、批量账户或其他违规行为，平台有权取消相关奖励资格。"
  ].join("\n")
}

function fillRuleText() {
  rule.value.ruleText = buildRuleText()
}

function loadRule() {
  ruleLoading.value = true
  getPromoRule().then((res: any) => {
    const data = res.data || {}
    rule.value = {
      enabled: data.enabled !== false,
      kycSelfEnabled: data.kycSelfEnabled !== false,
      kycRewardCny: Number(data.kycRewardCny ?? 14),
      kycRewardUsdt: Number(data.kycRewardUsdt ?? 2),
      inviteEnabled: data.inviteEnabled !== false,
      inviteAmount: Number(data.inviteAmount ?? 2),
      inviteCurrency: data.inviteCurrency || "CNY",
      lockParent: data.lockParent !== false,
      teamEnabled: data.teamEnabled !== false,
      teamRateL1: Number(data.teamRateL1 ?? 9),
      teamRateL2: Number(data.teamRateL2 ?? 3),
      teamRateL3: Number(data.teamRateL3 ?? 1),
      ruleText: data.ruleText || ""
    }
  }).finally(() => { ruleLoading.value = false })
}

function saveRule() {
  proxy.$refs["ruleRef"].validate((valid: boolean) => {
    if (!valid) return
    savePromoRule(rule.value).then(() => {
      proxy.$modal.msgSuccess("保存成功")
      loadRule()
    })
  })
}

function getList() {
  loading.value = true
  listPromoGrant(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

loadRule()
getList()
</script>

<style scoped>
.tip { margin-left: 12px; color: #909399; font-size: 13px; }
.mb8 { margin-bottom: 8px; }
</style>
