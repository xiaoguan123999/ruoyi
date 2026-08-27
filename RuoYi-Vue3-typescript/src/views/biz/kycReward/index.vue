<template>
  <div class="app-container ops-page">
    <el-alert
      title="实名认证通过后，用户在 App 任选人民币或 USDT 领取一次，金额以下方配置为准。领取后直接入账对应钱包。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form ref="ruleRef" :model="rule" :rules="rules" label-width="160px" v-loading="ruleLoading" class="ops-form-full">
      <el-form-item label="实名认证奖励">
        <el-switch v-model="rule.kycSelfEnabled" />
        <span class="tip">关闭后 App 不能领取</span>
      </el-form-item>
      <el-form-item label="人民币金额" prop="kycRewardCny">
        <el-input-number v-model="rule.kycRewardCny" :min="0" :precision="2" :step="1" style="width: 240px" />
        <span class="tip">对应弹窗「14元 / 人民币到账」</span>
      </el-form-item>
      <el-form-item label="USDT金额" prop="kycRewardUsdt">
        <el-input-number v-model="rule.kycRewardUsdt" :min="0" :precision="2" :step="1" style="width: 240px" />
        <span class="tip">对应弹窗「2U / USDT 到账」</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveRule" v-hasPermi="['biz:kycReward:edit']">保存配置</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">领取记录</el-divider>
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="领取人手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="grantId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="100" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="币种" align="center" prop="currency" width="90" />
      <el-table-column label="金额" align="center" prop="amount" width="100" />
      <el-table-column label="领取时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="120" />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizKycReward">
import { getKycRewardConfig, saveKycRewardConfig, listKycRewardGrant } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const ruleLoading = ref(false)
const loading = ref(false)
const dataList = ref<any[]>([])
const total = ref(0)
const rule = ref({
  kycSelfEnabled: true,
  kycRewardCny: 14,
  kycRewardUsdt: 2
})
const queryParams = ref({ pageNum: 1, pageSize: 10, phone: undefined as string | undefined })
const rules = {
  kycRewardCny: [{ required: true, message: "请填写人民币金额", trigger: "blur" }],
  kycRewardUsdt: [{ required: true, message: "请填写USDT金额", trigger: "blur" }]
}

function loadRule() {
  ruleLoading.value = true
  getKycRewardConfig().then((res: any) => {
    const data = res.data || {}
    rule.value = {
      kycSelfEnabled: data.kycSelfEnabled !== false,
      kycRewardCny: Number(data.kycRewardCny ?? 14),
      kycRewardUsdt: Number(data.kycRewardUsdt ?? 2)
    }
  }).finally(() => { ruleLoading.value = false })
}

function saveRule() {
  proxy.$refs["ruleRef"].validate((valid: boolean) => {
    if (!valid) return
    saveKycRewardConfig(rule.value).then(() => {
      proxy.$modal.msgSuccess("保存成功")
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

loadRule()
getList()
</script>

<style scoped>
.tip { margin-left: 12px; color: #909399; font-size: 13px; }
.mb8 { margin-bottom: 8px; }
</style>
