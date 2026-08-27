<template>
  <div class="app-container ops-page">
    <el-alert
      title="实名认证通过后，用户在 App 任选人民币或 USDT 领取一次。金额在「奖励配置」里改，领取后直接入账对应钱包。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="领取人手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Setting" @click="openRuleDialog" v-hasPermi="['biz:kycReward:edit']">奖励配置</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="grantId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="100" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="币种" align="center" prop="currency" width="90" />
      <el-table-column label="金额" align="center" prop="amount" width="100" />
      <el-table-column label="领取时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog
      title="奖励配置"
      v-model="ruleOpen"
      width="480px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="ruleRef" :model="rule" :rules="ruleRules" label-width="120px" v-loading="ruleLoading">
        <el-form-item label="实名认证奖励">
          <div class="switch-with-tip">
            <el-switch v-model="rule.kycSelfEnabled" />
            <div class="tip tip-block">关闭后 App 不能领取</div>
          </div>
        </el-form-item>
        <el-form-item label="人民币金额" prop="kycRewardCny">
          <el-input-number v-model="rule.kycRewardCny" :min="0" :precision="2" :step="1" style="width: 100%" />
          <div class="tip tip-block">对应弹窗「14元 / 人民币到账」</div>
        </el-form-item>
        <el-form-item label="USDT金额" prop="kycRewardUsdt">
          <el-input-number v-model="rule.kycRewardUsdt" :min="0" :precision="2" :step="1" style="width: 100%" />
          <div class="tip tip-block">对应弹窗「2U / USDT 到账」</div>
        </el-form-item>
        <el-form-item label="到账钱包">
          <WalletTypeSelect v-model="rule.walletTypeCode" />
          <div class="tip tip-block">实名奖励入这个钱包</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveRule" v-hasPermi="['biz:kycReward:edit']">保存配置</el-button>
        <el-button @click="ruleOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizKycReward">
import { getKycRewardConfig, saveKycRewardConfig, listKycRewardGrant, getWalletCreditByBiz, saveWalletCreditByBiz } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"

const { proxy } = getCurrentInstance() as any
const ruleOpen = ref(false)
const ruleLoading = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const dataList = ref<any[]>([])
const total = ref(0)
const rule = ref({
  kycSelfEnabled: true,
  kycRewardCny: 14,
  kycRewardUsdt: 2,
  walletTypeCode: "PROMO"
})
const queryParams = ref({
  pageNum: 1,
  pageSize: 100,
  memberId: undefined as number | undefined,
  phone: undefined as string | undefined
})
const ruleRules = {
  kycRewardCny: [{ required: true, message: "请填写人民币金额", trigger: "blur" }],
  kycRewardUsdt: [{ required: true, message: "请填写USDT金额", trigger: "blur" }]
}

function loadRule() {
  ruleLoading.value = true
  return Promise.all([getKycRewardConfig(), getWalletCreditByBiz("KYC_REWARD")]).then(([res, credit]: any[]) => {
    const data = res.data || {}
    rule.value = {
      kycSelfEnabled: data.kycSelfEnabled !== false,
      kycRewardCny: Number(data.kycRewardCny ?? 14),
      kycRewardUsdt: Number(data.kycRewardUsdt ?? 2),
      walletTypeCode: credit.data?.typeCode || "PROMO"
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
    saveKycRewardConfig(rule.value).then(() => saveWalletCreditByBiz("KYC_REWARD", rule.value.walletTypeCode)).then(() => {
      proxy.$modal.msgSuccess("奖励配置已保存")
      ruleOpen.value = false
      loadRule()
    })
  })
}

function getList() {
  loading.value = true
  listKycRewardGrant(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

getList()
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
