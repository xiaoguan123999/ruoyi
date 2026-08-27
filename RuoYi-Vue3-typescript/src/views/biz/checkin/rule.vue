<template>
  <div class="app-container ops-page">
    <el-alert
      title="配置每日签到奖励与连续签到抽奖档位。保存后 App 端立即按新规则生效。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" v-loading="loading" class="ops-form-full">
      <el-divider content-position="left">每日签到</el-divider>
      <el-form-item label="奖励金额(CNY)" prop="amount">
        <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="到账钱包" prop="walletTypeCode">
        <WalletTypeSelect v-model="form.walletTypeCode" />
        <span class="tip">签到奖励入这个钱包，和币种无关</span>
      </el-form-item>
      <el-form-item label="每日仅一次">
        <el-switch v-model="form.oncePerDay" disabled />
        <span class="el-form-item__label" style="margin-left: 12px; float: none">每个账户每天只能签到一次</span>
      </el-form-item>

      <el-divider content-position="left">连续签到抽奖（第一档）</el-divider>
      <el-form-item label="连续天数" prop="prize1Days">
        <el-input-number v-model="form.prize1Days" :min="1" :step="1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="奖品名称" prop="prize1Name">
        <el-input v-model="form.prize1Name" placeholder="例如：华为手机" style="width: 360px" />
      </el-form-item>
      <el-form-item label="中奖概率(%)" prop="prize1Rate">
        <el-input-number v-model="form.prize1Rate" :min="0" :max="100" :precision="2" :step="0.1" style="width: 240px" />
        <span style="margin-left: 12px; color: #909399">1 表示 1%，100 表示必中</span>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.prize1Enabled" />
      </el-form-item>

      <el-divider content-position="left">连续签到抽奖（第二档）</el-divider>
      <el-form-item label="连续天数" prop="prize2Days">
        <el-input-number v-model="form.prize2Days" :min="1" :step="1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="奖品名称" prop="prize2Name">
        <el-input v-model="form.prize2Name" placeholder="例如：华硕ROG笔记本电脑" style="width: 360px" />
      </el-form-item>
      <el-form-item label="中奖概率(%)" prop="prize2Rate">
        <el-input-number v-model="form.prize2Rate" :min="0" :max="100" :precision="2" :step="0.1" style="width: 240px" />
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.prize2Enabled" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" v-hasPermi="['biz:checkin:rule']">保存规则</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts" name="BizCheckinRule">
import { getCheckinRule, saveCheckinRule, getWalletCreditByBiz, saveWalletCreditByBiz } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"

const { proxy } = getCurrentInstance() as any
const loading = ref(false)
const form = ref({
  amount: 2,
  walletTypeCode: "PROMO",
  oncePerDay: true,
  prize1Days: 180,
  prize1Name: "华为手机",
  prize1Rate: 1,
  prize1Enabled: true,
  prize2Days: 365,
  prize2Name: "华硕ROG笔记本电脑",
  prize2Rate: 0.5,
  prize2Enabled: true
})
const rules = {
  amount: [{ required: true, message: "请填写每日签到金额", trigger: "blur" }],
  prize1Days: [{ required: true, message: "请填写第一档天数", trigger: "blur" }],
  prize1Name: [{ required: true, message: "请填写第一档奖品", trigger: "blur" }],
  prize1Rate: [{ required: true, message: "请填写第一档概率", trigger: "blur" }],
  prize2Days: [{ required: true, message: "请填写第二档天数", trigger: "blur" }],
  prize2Name: [{ required: true, message: "请填写第二档奖品", trigger: "blur" }],
  prize2Rate: [{ required: true, message: "请填写第二档概率", trigger: "blur" }]
}

function load() {
  loading.value = true
  Promise.all([getCheckinRule(), getWalletCreditByBiz("CHECKIN")]).then(([res, credit]: any[]) => {
    const data = res.data || {}
    const p1 = (data.prizes && data.prizes[0]) || {}
    const p2 = (data.prizes && data.prizes[1]) || {}
    form.value = {
      amount: Number(data.amount ?? 2),
      walletTypeCode: credit.data?.typeCode || "PROMO",
      oncePerDay: true,
      prize1Days: Number(p1.days ?? 180),
      prize1Name: p1.name || "华为手机",
      prize1Rate: Number(p1.rate ?? 1),
      prize1Enabled: p1.enabled !== false,
      prize2Days: Number(p2.days ?? 365),
      prize2Name: p2.name || "华硕ROG笔记本电脑",
      prize2Rate: Number(p2.rate ?? 0.5),
      prize2Enabled: p2.enabled !== false
    }
  }).finally(() => { loading.value = false })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const payload = {
      amount: form.value.amount,
      oncePerDay: true,
      prizes: [
        { days: form.value.prize1Days, name: form.value.prize1Name, rate: form.value.prize1Rate, enabled: form.value.prize1Enabled },
        { days: form.value.prize2Days, name: form.value.prize2Name, rate: form.value.prize2Rate, enabled: form.value.prize2Enabled }
      ]
    }
    saveCheckinRule(payload).then(() => saveWalletCreditByBiz("CHECKIN", form.value.walletTypeCode)).then(() => {
      proxy.$modal.msgSuccess("保存成功")
      load()
    })
  })
}

load()
</script>

<style scoped>
.tip { margin-left: 12px; color: #909399; font-size: 13px; }
</style>
