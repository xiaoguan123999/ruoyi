<template>
  <div class="app-container ops-page">
    <el-alert
      title="到账钱包、发放币种、有效成员在「会员等级」里按每个等级配置。这里只配奖励周期和金额。启航到星域自动发放；星链到「等级奖励发放」手动下发。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

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
            <el-button type="warning" @click="runEvaluate" v-hasPermi="['biz:levelReward:edit']">立即核算全部会员</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="dataList" style="width: 100%">
          <el-table-column label="等级" align="center" prop="levelName" min-width="100" />
          <el-table-column label="团队要求" align="center" prop="teamDepth" min-width="100" />
          <el-table-column label="团队口径" align="center" min-width="110">
            <template #default="scope">{{ sourceLabel(scope.row.performanceSource) }}</template>
          </el-table-column>
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
          <el-table-column label="团队奖励CNY" align="center" prop="rewardCny" min-width="120" />
          <el-table-column label="团队奖励USDT" align="center" prop="rewardUsdt" min-width="130" />
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

    <el-drawer :title="title" v-model="open" size="560px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="160px">
        <el-form-item label="等级名称">
          <el-input v-model="form.levelName" disabled />
        </el-form-item>
        <el-form-item label="团队要求" prop="teamDepth">
          <el-select v-model="form.teamDepth" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in teamDepthOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <div class="tip">一级内=邀请关系第1层，二级内=第1+2层。下面团队累计按这个范围统计。</div>
        </el-form-item>
        <el-form-item label="团队业绩口径">
          <el-select v-model="form.performanceSource" style="width: 100%">
            <el-option label="认购金额" value="SUBSCRIBE" />
            <el-option label="已通过充值" value="RECHARGE" />
            <el-option label="认购 + 充值" value="BOTH" />
          </el-select>
          <div class="tip">只改这个等级的团队累计怎么算。本人累计充值始终看充值，不受此项影响。</div>
        </el-form-item>
        <el-form-item label="本人累计充值CNY">
          <el-input-number v-model="form.minRechargeCny" :min="0" :precision="2" style="width: 100%" />
          <div class="tip">只看这个会员自己、审核通过的充值，不是团队。两边都填则两种币都要达标；填 0 不限制。</div>
        </el-form-item>
        <el-form-item label="本人累计充值USDT">
          <el-input-number v-model="form.minRechargeUsdt" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="'团队累计' + sourceLabel(form.performanceSource) + 'CNY'">
          <el-input-number v-model="form.minTeamRechargeCny" :min="0" :precision="2" style="width: 100%" />
          <div class="tip">按上面口径和「团队要求」范围合计，不含本人。填 0 不限制。</div>
        </el-form-item>
        <el-form-item :label="'团队累计' + sourceLabel(form.performanceSource) + 'USDT'">
          <el-input-number v-model="form.minTeamRechargeUsdt" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发放币种">
          <el-radio-group v-model="form.mixedPayCurrency">
            <el-radio value="USDT">只发USDT</el-radio>
            <el-radio value="CNY">只发人民币</el-radio>
            <el-radio value="BOTH">人民币和USDT都发</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="到账钱包">
          <WalletTypeSelect v-model="form.walletTypeCode" width="100%" />
        </el-form-item>
        <el-form-item label="启用该等级奖励">
          <el-switch v-model="form.rewardEnabled" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="奖励周期" label-width="100px">
              <el-select v-model="form.rewardCycle" style="width: 100%">
                <el-option label="无奖励" value="NONE" />
                <el-option label="达成一次" value="ONCE" />
                <el-option label="每月一次" value="MONTHLY" />
                <el-option label="永久资格" value="PERMANENT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发放方式" label-width="90px">
              <el-select v-model="form.rewardMode" style="width: 100%">
                <el-option label="自动入账" value="AUTO" />
                <el-option label="客服发放" value="MANUAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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
        <div class="drawer-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="BizLevelReward">
import { listLevelRewardLevel, updateLevelRewardLevel, evaluateLevelReward, getLevel } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"

const { proxy } = getCurrentInstance() as any
const loading = ref(true)
const dataList = ref<any[]>([])
const total = ref(0)
const open = ref(false)
const title = ref("")
const queryParams = ref({ pageNum: 1, pageSize: 100, levelName: undefined as string | undefined })
const form = ref<any>({})
const rules = {}
const TEAM_DEPTH_PRESET = ["一级内", "二级内", "三级内", "四级内", "五级内", "六级内", "七级内"]
const teamDepthOptions = computed(() => {
  const cur = form.value?.teamDepth
  if (cur && !TEAM_DEPTH_PRESET.includes(cur)) return [cur, ...TEAM_DEPTH_PRESET]
  return TEAM_DEPTH_PRESET
})

function cycleLabel(v: string) {
  if (v === "ONCE") return "一次"
  if (v === "MONTHLY") return "每月"
  if (v === "PERMANENT") return "永久"
  return "无"
}

function sourceLabel(v: string) {
  if (v === "SUBSCRIBE") return "认购"
  if (v === "BOTH") return "认购+充值"
  return "充值"
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
    form.value = Object.assign({ performanceSource: "RECHARGE", mixedPayCurrency: "USDT", walletTypeCode: "PROMO" }, res.data || {})
    if (!form.value.performanceSource) form.value.performanceSource = "RECHARGE"
    if (!form.value.mixedPayCurrency) form.value.mixedPayCurrency = "USDT"
    if (!form.value.walletTypeCode) form.value.walletTypeCode = "PROMO"
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

getList()
</script>

<style scoped>
.tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
