<template>
  <div class="app-container ops-page">
    <el-alert
      title="星链伙伴成长激励金：先配全局规则和各等级金额，再把对应等级改为正常。启航到星域（前6级）一次自动发放；仅星链找客服领取，后台「等级奖励发放」手动下发。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <div class="ops-section-card">
      <div class="ops-section-card__hd">全局规则</div>
      <div class="ops-section-card__bd">
        <el-form ref="ruleRef" :model="rule" label-width="140px" v-loading="ruleLoading">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="成长激励金开关">
                <el-switch v-model="rule.enabled" />
                <span class="tip">关闭后不核算发放</span>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="混合业绩币种">
                <el-radio-group v-model="rule.mixedPayCurrency">
                  <el-radio value="USDT">USDT</el-radio>
                  <el-radio value="CNY">人民币</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="团队业绩口径">
                <el-select v-model="rule.performanceSource" style="width: 100%">
                  <el-option label="认购金额" value="SUBSCRIBE" />
                  <el-option label="已通过充值" value="RECHARGE" />
                  <el-option label="认购 + 充值" value="BOTH" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="团队业绩含本人">
                <el-switch v-model="rule.includeSelf" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="有效成员需实名">
                <el-switch v-model="rule.validNeedKyc" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="有效成员需认购">
                <el-switch v-model="rule.validNeedOrder" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="规则说明">
                <el-input v-model="rule.ruleText" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="App 右上角「规则说明」正文" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="页面注释">
                <el-input v-model="rule.hint" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="App 等级表上方的「注」" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label-width="0" class="form-actions">
            <el-button type="primary" @click="saveRule" v-hasPermi="['biz:levelReward:edit']">保存全局规则</el-button>
            <el-button type="warning" @click="runEvaluate" v-hasPermi="['biz:levelReward:edit']">立即核算全部会员</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="ops-section-card">
      <div class="ops-section-card__hd">各等级奖励</div>
      <div class="ops-section-card__bd">
        <el-form :model="queryParams" ref="queryRef" :inline="true">
          <el-form-item label="等级名称" prop="levelName">
            <el-input v-model="queryParams.levelName" placeholder="等级名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="dataList">
          <el-table-column label="等级" align="center" prop="levelName" width="100" />
          <el-table-column label="团队要求" align="center" prop="teamDepth" width="100" />
          <el-table-column label="有效成员" align="center" prop="minValidMembers" width="90" />
          <el-table-column label="充值金额CNY" align="center" prop="minTeamPerfCny" width="110" />
          <el-table-column label="充值金额USDT" align="center" prop="minTeamPerfUsdt" width="120" />
          <el-table-column label="奖励开关" align="center" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.rewardEnabled === '1' ? 'success' : 'info'">{{ scope.row.rewardEnabled === '1' ? '开' : '关' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="周期" align="center" width="100">
            <template #default="scope">{{ cycleLabel(scope.row.rewardCycle) }}</template>
          </el-table-column>
          <el-table-column label="发放" align="center" width="90">
            <template #default="scope">{{ scope.row.rewardMode === 'MANUAL' ? '客服' : '自动' }}</template>
          </el-table-column>
          <el-table-column label="团队奖励CNY" align="center" prop="rewardCny" width="110" />
          <el-table-column label="团队奖励USDT" align="center" prop="rewardUsdt" width="120" />
          <el-table-column label="等级状态" align="center" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="100" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:levelReward:edit']">配置</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="等级名称">
          <el-input v-model="form.levelName" disabled />
        </el-form-item>
        <el-form-item label="团队要求" prop="teamDepth">
          <el-input v-model="form.teamDepth" placeholder="例如 一级内" />
          <div class="tip">App 会员等级表「团队要求」列，对应接口 teamDepth</div>
        </el-form-item>
        <el-form-item label="有效成员人数" prop="minValidMembers">
          <el-input-number v-model="form.minValidMembers" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="本人充值CNY">
          <el-input-number v-model="form.minRechargeCny" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="本人充值USDT">
          <el-input-number v-model="form.minRechargeUsdt" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="充值金额CNY">
          <el-input-number v-model="form.minTeamPerfCny" :min="0" :precision="2" style="width: 100%" />
          <div class="tip">填 0 表示不限制</div>
        </el-form-item>
        <el-form-item label="充值金额USDT">
          <el-input-number v-model="form.minTeamPerfUsdt" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="启用该等级奖励">
          <el-switch v-model="form.rewardEnabled" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="奖励周期">
          <el-select v-model="form.rewardCycle" style="width: 100%">
            <el-option label="无奖励" value="NONE" />
            <el-option label="达成一次" value="ONCE" />
            <el-option label="每月一次" value="MONTHLY" />
            <el-option label="永久资格" value="PERMANENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放方式">
          <el-select v-model="form.rewardMode" style="width: 100%">
            <el-option label="自动入账" value="AUTO" />
            <el-option label="客服发放" value="MANUAL" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.rewardCycle === 'PERMANENT'" label="永久档领取">
          <el-select v-model="form.rewardRepeat" style="width: 100%">
            <el-option label="仅一次资格单" value="NONE" />
            <el-option label="按月待发放" value="MONTHLY" />
            <el-option label="不限次数（客服额外发放）" value="UNLIMITED" />
          </el-select>
        </el-form-item>
        <el-form-item label="团队奖励CNY">
          <el-input-number v-model="form.rewardCny" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="团队奖励USDT">
          <el-input-number v-model="form.rewardUsdt" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="等级状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizLevelReward">
import { getLevelRewardRule, saveLevelRewardRule, listLevelRewardLevel, updateLevelRewardLevel, evaluateLevelReward, getLevel } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const ruleLoading = ref(false)
const loading = ref(true)
const dataList = ref<any[]>([])
const total = ref(0)
const open = ref(false)
const title = ref("")
const rule = ref({
  enabled: true,
  mixedPayCurrency: "USDT",
  performanceSource: "SUBSCRIBE",
  includeSelf: false,
  validNeedKyc: true,
  validNeedOrder: true,
  ruleText: "",
  hint: ""
})
const queryParams = ref({ pageNum: 1, pageSize: 100, levelName: undefined as string | undefined })
const form = ref<any>({})
const rules = {
  minValidMembers: [{ required: true, message: "请填写有效成员人数", trigger: "blur" }]
}

function cycleLabel(v: string) {
  if (v === "ONCE") return "一次"
  if (v === "MONTHLY") return "每月"
  if (v === "PERMANENT") return "永久"
  return "无"
}

function loadRule() {
  ruleLoading.value = true
  getLevelRewardRule().then((res: any) => {
    rule.value = Object.assign(rule.value, res.data || {})
  }).finally(() => { ruleLoading.value = false })
}

function saveRule() {
  saveLevelRewardRule(rule.value).then(() => {
    proxy.$modal.msgSuccess("保存成功")
    loadRule()
  })
}

function runEvaluate() {
  proxy.$modal.confirm("将按当前规则核算全部会员等级和奖励，是否继续？").then(() => {
    return evaluateLevelReward()
  }).then((res: any) => {
    proxy.$modal.msgSuccess(res.msg || "核算完成")
    getList()
  }).catch(() => {})
}

function getList() {
  loading.value = true
  listLevelRewardLevel(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleUpdate(row: any) {
  getLevel(row.levelId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "配置 " + row.levelName
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    updateLevelRewardLevel(form.value).then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}

loadRule()
getList()
</script>

<style scoped>
.tip { margin-left: 8px; color: #909399; font-size: 12px; }
.form-actions {
  margin-bottom: 8px !important;
}
.form-actions :deep(.el-form-item__content) {
  margin-left: 0 !important;
}
</style>
