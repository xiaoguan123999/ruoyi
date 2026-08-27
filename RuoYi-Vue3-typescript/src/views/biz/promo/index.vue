<template>
  <div class="app-container ops-page" v-loading="ruleLoading">
    <el-alert
      title="改金额/比例后点「保存规则」立即生效。实名自领金额与「实名认证奖励」同步；发放明细在「推广奖励发放」，认购三级佣金在「推广佣金」。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form ref="ruleRef" :model="rule" :rules="rules" label-width="140px" class="ops-form-full">
      <el-divider content-position="left">总开关</el-divider>
      <el-form-item label="注册推广总开关" class="with-tip">
        <el-switch v-model="rule.enabled" />
        <div class="field-tip">关闭后：实名自领、邀请奖励都不发；三级认购返佣不受影响</div>
      </el-form-item>

      <el-divider content-position="left">一、实名注册奖励（用户自领）</el-divider>
      <el-form-item label="是否开启" class="with-tip">
        <el-switch v-model="rule.kycSelfEnabled" />
        <div class="field-tip">实名后在 App 任选一种币领一次</div>
      </el-form-item>
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="人民币金额" prop="kycRewardCny">
            <el-input-number v-model="rule.kycRewardCny" :min="0" :precision="2" :step="1" controls-position="right" style="width: 220px" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="USDT金额" prop="kycRewardUsdt" label-width="110px">
            <el-input-number v-model="rule.kycRewardUsdt" :min="0" :precision="2" :step="1" controls-position="right" style="width: 220px" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="到账钱包">
        <WalletTypeSelect v-model="rule.kycWalletTypeCode" />
        <div class="field-tip">实名自领入这个钱包</div>
      </el-form-item>

      <el-divider content-position="left">二、实名推广奖励（发给邀请人）</el-divider>
      <el-form-item label="是否开启" class="with-tip">
        <el-switch v-model="rule.inviteEnabled" />
        <div class="field-tip">被邀请人完成实名后自动入账</div>
      </el-form-item>
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="奖励金额" prop="inviteAmount">
            <el-input-number v-model="rule.inviteAmount" :min="0" :precision="2" :step="1" controls-position="right" style="width: 220px" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="奖励币种" label-width="110px">
            <el-radio-group v-model="rule.inviteCurrency">
              <el-radio value="CNY">人民币</el-radio>
              <el-radio value="USDT">USDT</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="到账钱包">
        <WalletTypeSelect v-model="rule.inviteWalletTypeCode" />
        <div class="field-tip">邀请奖励入这个钱包</div>
      </el-form-item>
      <el-form-item label="锁定上下级" class="with-tip">
        <el-switch v-model="rule.lockParent" />
        <div class="field-tip">注册绑定邀请码后不可改上级，请提醒用户核对邀请码</div>
      </el-form-item>

      <el-divider content-position="left">三、团队返佣（认购触发，充值不分佣）</el-divider>
      <el-form-item label="是否开启" class="with-tip">
        <el-switch v-model="rule.teamEnabled" />
        <div class="field-tip">关闭后下级认购不再给上级分佣</div>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="8" :md="6">
          <el-form-item label="一级(%)" prop="teamRateL1">
            <el-input-number v-model="rule.teamRateL1" :min="0" :max="100" :precision="2" :step="1" controls-position="right" style="width: 160px" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8" :md="6">
          <el-form-item label="二级(%)" prop="teamRateL2" label-width="90px">
            <el-input-number v-model="rule.teamRateL2" :min="0" :max="100" :precision="2" :step="1" controls-position="right" style="width: 160px" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8" :md="6">
          <el-form-item label="三级(%)" prop="teamRateL3" label-width="90px">
            <el-input-number v-model="rule.teamRateL3" :min="0" :max="100" :precision="2" :step="1" controls-position="right" style="width: 160px" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">App 规则说明</el-divider>
      <el-form-item label="展示全文" class="with-tip">
        <el-input
          v-model="rule.ruleText"
          type="textarea"
          :rows="8"
          maxlength="500"
          show-word-limit
          placeholder="展示在 App 邀请页 / 规则页"
        />
        <div class="field-tip">改完金额或比例后，可先点「按当前数值生成说明」，再保存</div>
      </el-form-item>
      <el-form-item label="到账钱包">
        <WalletTypeSelect v-model="rule.teamWalletTypeCode" />
        <span class="tip">下单返佣入这个钱包</span>
      </el-form-item>

      <el-form-item label-width="0" class="form-actions">
        <el-button type="primary" @click="saveRule" v-hasPermi="['biz:promo:edit']">保存规则</el-button>
        <el-button @click="fillRuleText">按当前数值生成说明</el-button>
        <el-button @click="loadRule">重新加载</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts" name="BizPromoRule">
import { getPromoRule, savePromoRule, getWalletCreditByBiz, saveWalletCreditByBiz } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"

const { proxy } = getCurrentInstance() as any
const ruleLoading = ref(false)
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
  kycWalletTypeCode: "PROMO",
  inviteWalletTypeCode: "PROMO",
  teamWalletTypeCode: "PROMO",
  ruleText: ""
})
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
    "下级成功认购产品后按认购金额返佣，充值到账不返佣。",
    `一级返佣 ${fmt(rule.value.teamRateL1)}%、二级返佣 ${fmt(rule.value.teamRateL2)}%、三级返佣 ${fmt(rule.value.teamRateL3)}%`,
    "",
    "奖励资格、返佣计算及发放结果以平台系统实际核算为准；如发现异常注册、批量账户或其他违规行为，平台有权取消相关奖励资格。"
  ].join("\n")
}

function fillRuleText() {
  rule.value.ruleText = buildRuleText()
  proxy.$modal.msgSuccess("已按当前数值生成说明，请确认后保存")
}

function loadRule() {
  ruleLoading.value = true
  Promise.all([
    getPromoRule(),
    getWalletCreditByBiz("KYC_REWARD"),
    getWalletCreditByBiz("INVITE"),
    getWalletCreditByBiz("COMMISSION")
  ]).then(([res, kyc, invite, team]: any[]) => {
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
      kycWalletTypeCode: kyc.data?.typeCode || "PROMO",
      inviteWalletTypeCode: invite.data?.typeCode || "PROMO",
      teamWalletTypeCode: team.data?.typeCode || "PROMO",
      ruleText: data.ruleText || ""
    }
  }).finally(() => { ruleLoading.value = false })
}

function saveRule() {
  proxy.$refs["ruleRef"].validate((valid: boolean) => {
    if (!valid) return
    savePromoRule(rule.value).then(() => Promise.all([
      saveWalletCreditByBiz("KYC_REWARD", rule.value.kycWalletTypeCode),
      saveWalletCreditByBiz("INVITE", rule.value.inviteWalletTypeCode),
      saveWalletCreditByBiz("COMMISSION", rule.value.teamWalletTypeCode)
    ])).then(() => {
      proxy.$modal.msgSuccess("规则已保存")
      loadRule()
    })
  })
}

loadRule()
</script>

<style scoped>
/* 说明跟在控件下方，与开关/输入框左边缘对齐 */
:deep(.with-tip .el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: normal;
}
.field-tip {
  margin: 6px 0 0;
  padding: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.4;
}
.form-actions {
  margin-bottom: 8px !important;
}
.form-actions :deep(.el-form-item__content) {
  margin-left: 0 !important;
}
</style>
