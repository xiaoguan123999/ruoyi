<template>
  <div class="app-container ops-page">
    <div class="ops-section-card mb8">
      <div class="ops-section-card__hd">App 等级页文案</div>
      <div class="ops-section-card__bd">
        <el-form :model="appCopy" label-width="110px" v-loading="copyLoading">
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="规则说明">
                <el-input v-model="appCopy.ruleText" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="App 右上角「规则说明」正文" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="页面注释">
                <el-input v-model="appCopy.hint" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="App 等级表上方的「注」" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label-width="0">
            <el-button type="primary" @click="saveAppCopy" v-hasPermi="['biz:levelReward:edit']">保存文案</el-button>
            <el-button type="warning" @click="runEvaluate" v-hasPermi="['biz:levelReward:edit']">立即核算全部会员</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="等级名称" prop="levelName">
        <el-input v-model="queryParams.levelName" placeholder="等级名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:level:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="levelId" width="80" />
      <el-table-column label="等级" align="center" prop="levelName" />
      <el-table-column label="团队要求" align="center" prop="teamDepth" min-width="100" />
      <el-table-column label="团队口径" align="center" min-width="110">
        <template #default="scope">{{ sourceLabel(scope.row.performanceSource) }}</template>
      </el-table-column>
      <el-table-column label="本人累计充值CNY" align="center" prop="minRechargeCny" min-width="140" />
      <el-table-column label="本人累计充值USDT" align="center" prop="minRechargeUsdt" min-width="150" />
      <el-table-column label="团队累计CNY" align="center" prop="minTeamRechargeCny" min-width="140" />
      <el-table-column label="团队累计USDT" align="center" prop="minTeamRechargeUsdt" min-width="150" />
      <el-table-column label="团队奖励CNY" align="center" prop="rewardCny" />
      <el-table-column label="团队奖励USDT" align="center" prop="rewardUsdt" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:level:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:level:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-drawer :title="title" v-model="open" size="560px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="等级名称" prop="levelName"><el-input v-model="form.levelName" /></el-form-item>
        <el-form-item label="团队要求" prop="teamDepth">
          <el-select v-model="form.teamDepth" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in teamDepthOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:6px">一级内=邀请关系第1层，二级内=第1+2层。团队累计按这个范围统计。不选则整条线都算。</div>
        </el-form-item>
        <el-form-item label="团队业绩口径">
          <el-select v-model="form.performanceSource" style="width: 100%">
            <el-option label="认购金额" value="SUBSCRIBE" />
            <el-option label="已通过充值" value="RECHARGE" />
            <el-option label="认购 + 充值" value="BOTH" />
          </el-select>
          <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:6px">只改这个等级的团队累计怎么算。本人累计充值始终看充值。</div>
        </el-form-item>
        <el-form-item label="本人累计充值CNY" prop="minRechargeCny">
          <el-input-number v-model="form.minRechargeCny" :min="0" :precision="2" style="width: 100%" />
          <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:6px">只看这个会员自己、审核通过的充值，不是团队累计。两边都填则两种币都要达标；填 0 不限制。</div>
        </el-form-item>
        <el-form-item label="本人累计充值USDT" prop="minRechargeUsdt"><el-input-number v-model="form.minRechargeUsdt" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item :label="'团队累计' + sourceLabel(form.performanceSource) + 'CNY'" prop="minTeamRechargeCny">
          <el-input-number v-model="form.minTeamRechargeCny" :min="0" :precision="2" style="width: 100%" />
          <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:6px">按上面口径和「团队要求」范围合计，不含本人。填 0 不限制。</div>
        </el-form-item>
        <el-form-item :label="'团队累计' + sourceLabel(form.performanceSource) + 'USDT'" prop="minTeamRechargeUsdt"><el-input-number v-model="form.minTeamRechargeUsdt" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="最低有效成员">
          <el-input-number v-model="form.minValidMembers" :min="0" :precision="0" style="width: 100%" />
          <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:6px">按「团队要求」范围统计。填 0 不限制人数。</div>
        </el-form-item>
        <el-form-item label="有效成员条件">
          <div class="switch-inline">
            <span class="switch-inline__item">
              <span>需实名</span>
              <el-switch v-model="form.validNeedKyc" active-value="1" inactive-value="0" />
            </span>
            <span class="switch-inline__item">
              <span>需认购</span>
              <el-switch v-model="form.validNeedOrder" active-value="1" inactive-value="0" />
            </span>
          </div>
        </el-form-item>
        <el-form-item label="发放币种">
          <el-radio-group v-model="form.mixedPayCurrency" class="currency-radio">
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
          <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:6px">会员达到该等级后自动发给本人一次，对应 App 等级表「团队奖励」。已达标会员可到「等级奖励」点立即核算。</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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

<script setup lang="ts" name="BizLevel">
import { listLevel, getLevel, addLevel, updateLevel, delLevel, getLevelRewardRule, saveLevelRewardRule, evaluateLevelReward } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, levelName: undefined },
  rules: { levelName: [{ required: true, message: "等级名称不能为空", trigger: "blur" }] }
})
const { queryParams, form, rules } = toRefs(data)
const TEAM_DEPTH_PRESET = ["一级内", "二级内", "三级内", "四级内", "五级内", "六级内", "七级内"]
const teamDepthOptions = computed(() => {
  const cur = form.value?.teamDepth
  if (cur && !TEAM_DEPTH_PRESET.includes(cur)) return [cur, ...TEAM_DEPTH_PRESET]
  return TEAM_DEPTH_PRESET
})

function getList() {
  loading.value = true
  listLevel(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function sourceLabel(v: string) {
  if (v === "SUBSCRIBE") return "认购"
  if (v === "BOTH") return "认购+充值"
  return "充值"
}
const copyLoading = ref(false)
const appCopy = ref({ ruleText: "", hint: "", enabled: true, mixedPayCurrency: "USDT", performanceSource: "SUBSCRIBE", includeSelf: false, validNeedKyc: true, validNeedOrder: true })
function loadAppCopy() {
  copyLoading.value = true
  getLevelRewardRule().then((res: any) => {
    appCopy.value = Object.assign(appCopy.value, res.data || {})
  }).finally(() => { copyLoading.value = false })
}
function saveAppCopy() {
  saveLevelRewardRule(appCopy.value).then(() => proxy.$modal.msgSuccess("保存成功"))
}
function runEvaluate() {
  proxy.$modal.confirm("将按当前各等级规则核算全部会员，是否继续？").then(() => evaluateLevelReward()).then((res: any) => {
    proxy.$modal.msgSuccess(res.msg || "核算完成")
    getList()
  }).catch(() => {})
}
function reset() {
  form.value = {
    status: "0",
    teamDepth: "",
    performanceSource: "RECHARGE",
    mixedPayCurrency: "USDT",
    walletTypeCode: "PROMO",
    validNeedKyc: "1",
    validNeedOrder: "1",
    rewardEnabled: "1",
    rewardCycle: "ONCE",
    rewardMode: "AUTO",
    rewardRepeat: "NONE",
    minValidMembers: 0,
    minRechargeCny: 0,
    minRechargeUsdt: 0,
    minTeamRechargeCny: 0,
    minTeamRechargeUsdt: 0,
    minTeamPerfCny: 0,
    minTeamPerfUsdt: 0,
    rewardCny: 0,
    rewardUsdt: 0,
    sort: 0
  }
}
function handleAdd() { reset(); open.value = true; title.value = "新增等级" }
function handleUpdate(row: any) {
  getLevel(row.levelId).then((res: any) => {
    form.value = Object.assign({
      performanceSource: "RECHARGE",
      mixedPayCurrency: "USDT",
      walletTypeCode: "PROMO",
      validNeedKyc: "1",
      validNeedOrder: "1",
      rewardEnabled: "1",
      rewardCycle: "ONCE",
      rewardMode: "AUTO",
      rewardRepeat: "NONE"
    }, res.data || {})
    if (!form.value.performanceSource) form.value.performanceSource = "RECHARGE"
    if (!form.value.mixedPayCurrency) form.value.mixedPayCurrency = "USDT"
    if (!form.value.walletTypeCode) form.value.walletTypeCode = "PROMO"
    if (!form.value.validNeedKyc) form.value.validNeedKyc = "1"
    if (!form.value.validNeedOrder) form.value.validNeedOrder = "1"
    if (!form.value.rewardEnabled) form.value.rewardEnabled = "1"
    if (!form.value.rewardCycle) form.value.rewardCycle = "ONCE"
    if (!form.value.rewardMode) form.value.rewardMode = "AUTO"
    if (!form.value.rewardRepeat) form.value.rewardRepeat = "NONE"
    open.value = true
    title.value = "修改等级"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const data = form.value
    const req = data.levelId ? updateLevel(data) : addLevel(data)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除等级"' + row.levelName + '"？').then(() => delLevel(row.levelId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
loadAppCopy()
getList()
</script>

<style scoped>
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.switch-inline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}
.switch-inline__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-regular);
  font-size: 14px;
}
.currency-radio {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  column-gap: 16px;
}
.currency-radio :deep(.el-radio) {
  margin-right: 0;
  white-space: nowrap;
  height: auto;
}
</style>
