<template>
  <div class="app-container ops-page">
    <el-alert
      title="系统只维护一个抽奖活动。运营建议：先建标签人群包与奖品池，再在本页保存规则并配置奖品、必中策略。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-empty v-if="!loading && !form.activityId" description="尚未初始化抽奖活动">
      <el-button type="primary" v-hasPermi="['biz:lotteryActivity:add']" @click="handleInit">初始化抽奖活动</el-button>
    </el-empty>

    <template v-else-if="form.activityId">
      <div class="lottery-toolbar" v-loading="loading">
        <div class="lottery-toolbar__main">
          <div class="lottery-toolbar__title">{{ form.title || '抽奖活动' }}</div>
          <div class="lottery-toolbar__status">
            <span class="lottery-toolbar__label">活动开关</span>
            <el-switch
              v-model="form.status"
              active-value="0"
              inactive-value="1"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
            />
          </div>
        </div>
        <div class="lottery-toolbar__actions">
          <el-button icon="Refresh" @click="reloadAll">刷新</el-button>
          <el-button type="primary" icon="Check" @click="submitForm" v-hasPermi="['biz:lotteryActivity:edit']">保存活动</el-button>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="ops-form-stack" v-loading="loading">
        <div class="ops-section-card">
          <div class="ops-section-card__hd">活动设置</div>
          <div class="ops-section-card__bd">
            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-form-item label="活动名称" prop="title">
                  <div class="field-with-tip">
                    <el-input v-model="form.title" placeholder="如：幸运抽奖" maxlength="128" show-word-limit />
                    <p class="field-tip">后台识别用，也会作为 App 活动标题参考</p>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="频控间隔" prop="intervalHours">
                  <div class="field-with-tip">
                    <div class="interval-row">
                      <el-input-number v-model="form.intervalHours" :min="0" :max="720" controls-position="right" style="width: 160px" />
                      <span class="interval-unit">小时</span>
                    </div>
                    <p class="field-tip">两次抽奖最短间隔；填 0 表示关闭频控，可连续抽</p>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="开始时间" prop="startTime">
                  <div class="field-with-tip">
                    <el-date-picker
                      v-model="form.startTime"
                      type="datetime"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      placeholder="不填 = 立即生效"
                      clearable
                      style="width: 100%"
                    />
                    <p class="field-tip">到点后用户才能抽奖</p>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="结束时间" prop="endTime">
                  <div class="field-with-tip">
                    <el-date-picker
                      v-model="form.endTime"
                      type="datetime"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      placeholder="不填 = 长期有效"
                      clearable
                      style="width: 100%"
                    />
                    <p class="field-tip">到期后活动自动不可抽</p>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </div>

        <div class="ops-section-card">
          <div class="ops-section-card__hd">奖品与策略</div>
          <div class="ops-section-card__bd">
            <div class="config-cards" v-loading="overviewLoading">
              <div class="config-card" v-hasPermi="['biz:lotteryPrize:edit']" @click="openPrizeDialog()">
                <div class="config-card__head">
                  <div class="config-card__title">配置奖品</div>
                  <el-tag :type="prizeOverview.ready ? 'success' : 'warning'" size="small">
                    {{ prizeOverview.ready ? '已配置' : '待完善' }}
                  </el-tag>
                </div>
                <div class="config-card__stats">
                  <div><span class="stat-num">{{ prizeOverview.count }}</span><span class="stat-label">个奖项</span></div>
                  <div><span class="stat-num">{{ prizeOverview.probabilityText }}</span><span class="stat-label">概率合计</span></div>
                  <div><span class="stat-num">{{ prizeOverview.fallbackText }}</span><span class="stat-label">兜底奖</span></div>
                </div>
                <div class="config-card__echo" v-if="prizeOverview.names.length">
                  {{ prizeOverview.names.join('、') }}
                </div>
                <div class="config-card__echo is-muted" v-else>尚未配置奖品，点击进入抽屉设置</div>
                <div class="config-card__action">进入配置 →</div>
              </div>

              <div class="config-card" v-hasPermi="['biz:lotteryStrategy:edit']" @click="openStrategyDialog()">
                <div class="config-card__head">
                  <div class="config-card__title">必中策略</div>
                  <el-tag :type="strategyOverview.count ? 'success' : 'info'" size="small">
                    {{ strategyOverview.count ? '已配置' : '未配置' }}
                  </el-tag>
                </div>
                <div class="config-card__stats">
                  <div><span class="stat-num">{{ strategyOverview.count }}</span><span class="stat-label">条策略</span></div>
                  <div><span class="stat-num">{{ strategyOverview.enabled }}</span><span class="stat-label">启用中</span></div>
                  <div><span class="stat-num">{{ strategyOverview.disabled }}</span><span class="stat-label">已停用</span></div>
                </div>
                <div class="config-card__echo" v-if="strategyOverview.lines.length">
                  <div v-for="(line, idx) in strategyOverview.lines" :key="idx">{{ line }}</div>
                </div>
                <div class="config-card__echo is-muted" v-else>可不配；需要定向必中时再点此进入</div>
                <div class="config-card__action">进入配置 →</div>
              </div>
            </div>
          </div>
        </div>

        <div class="ops-section-card">
          <div class="ops-section-card__hd">App 规则文案</div>
          <div class="ops-section-card__bd">
            <el-form-item label="抽奖规则" prop="ruleText">
              <div class="field-with-tip">
                <el-input
                  v-model="form.ruleText"
                  type="textarea"
                  :rows="8"
                  maxlength="2000"
                  show-word-limit
                  placeholder="一行一条规则，保存后展示在 App 抽奖页"
                />
                <p class="field-tip">面向用户可见，请写清签到获得次数、频控、奖品发放等说明</p>
              </div>
            </el-form-item>
          </div>
        </div>
      </el-form>
    </template>

    <!-- 奖品配置：从奖品池选择 -->
    <el-drawer
      :title="'配置奖品 · ' + currentActivityTitle"
      v-model="prizeOpen"
      size="920px"
      append-to-body
      destroy-on-close
      class="lottery-config-drawer"
    >
      <div class="drawer-body">
        <el-alert
          title="从奖品池选择奖品，再设置排序/库存/概率/兜底。奖品名称与类型在「奖品管理」维护。须恰好 1 个虚拟兜底奖。"
          type="warning"
          :closable="false"
          show-icon
          class="mb8"
        />
        <div class="drawer-toolbar mb8">
          <el-button type="primary" plain icon="Plus" @click="handlePrizeAdd">新增奖项</el-button>
          <span class="field-tip">当前 {{ prizeList.length }} 个奖项；概率合计 {{ formatProbabilityPercent(probabilitySum) }}（{{ probabilitySum }} / 10000）</span>
        </div>
        <el-table :data="prizeList" border size="small" class="drawer-table">
          <el-table-column label="排序" align="center" prop="position" width="70" />
          <el-table-column label="奖品" align="left" min-width="140" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.prizeName || ('池#' + scope.row.poolId) }}</template>
          </el-table-column>
          <el-table-column label="类型" align="center" width="80">
            <template #default="scope">{{ prizeTypeLabel(scope.row.prizeType) }}</template>
          </el-table-column>
          <el-table-column label="普通库存" align="center" width="90">
            <template #default="scope">{{ formatStockCell(scope.row, 'public') }}</template>
          </el-table-column>
          <el-table-column label="必中库存" align="center" width="90">
            <template #default="scope">{{ formatStockCell(scope.row, 'exclusive') }}</template>
          </el-table-column>
          <el-table-column label="概率" align="center" width="100">
            <template #default="scope">{{ formatProbabilityPercent(scope.row.probability) }}</template>
          </el-table-column>
          <el-table-column label="兜底" align="center" width="70">
            <template #default="scope">
              <el-tag v-if="scope.row.isFallback === '1'" type="warning" size="small">是</el-tag>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="140" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handlePrizeEdit(scope.row, scope.$index)">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handlePrizeDelete(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="submitPrizes" v-hasPermi="['biz:lotteryPrize:edit']">保存奖项</el-button>
          <el-button @click="prizeOpen = false">取 消</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog :title="prizeFormTitle" v-model="prizeFormOpen" width="520px" append-to-body>
      <el-form ref="prizeFormRef" :model="prizeForm" :rules="prizeFormRules" label-width="110px">
        <el-form-item label="选择奖品" prop="poolId">
          <el-select v-model="prizeForm.poolId" filterable placeholder="从奖品池选择" style="width: 100%" @change="onPoolSelected">
            <el-option
              v-for="item in poolOptions"
              :key="item.poolId"
              :label="item.prizeName + '（' + prizeTypeLabel(item.prizeType) + '）'"
              :value="item.poolId"
              :disabled="isPoolUsed(item.poolId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="selectedPoolPreview" label="奖品预览">
          <div class="pool-preview" :style="{ background: selectedPoolPreview.sectorBgColor || '#FFF8EC' }">
            <image-preview v-if="selectedPoolPreview.imageUrl" :src="selectedPoolPreview.imageUrl" :width="40" :height="40" />
            <div>
              <div :style="{ color: selectedPoolPreview.nameColor || '#111827', fontWeight: 600 }">{{ selectedPoolPreview.prizeName }}</div>
              <div
                v-if="selectedPoolPreview.prizeDesc"
                class="prize-desc-preview"
                :style="{ color: selectedPoolPreview.descColor || '#374151', fontSize: '12px', marginTop: '4px' }"
              >{{ selectedPoolPreview.prizeDesc }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="position">
          <el-input-number v-model="prizeForm.position" :min="1" :max="36" controls-position="right" style="width: 160px" />
        </el-form-item>
        <el-form-item label="普通库存">
          <template v-if="prizeForm.prizeType === 3">
            <el-checkbox v-model="prizeForm.publicUnlimited">无限</el-checkbox>
            <el-input-number v-if="!prizeForm.publicUnlimited" v-model="prizeForm.publicStock" :min="0" controls-position="right" style="width: 140px; margin-left: 8px" />
          </template>
          <el-input-number v-else v-model="prizeForm.publicStock" :min="0" controls-position="right" style="width: 160px" />
          <div class="field-tip">面向全体用户，按概率抽奖时消耗</div>
        </el-form-item>
        <el-form-item label="必中库存">
          <template v-if="prizeForm.prizeType === 3">
            <el-checkbox v-model="prizeForm.exclusiveUnlimited">无限</el-checkbox>
            <el-input-number v-if="!prizeForm.exclusiveUnlimited" v-model="prizeForm.exclusiveStock" :min="0" controls-position="right" style="width: 140px; margin-left: 8px" />
          </template>
          <el-input-number v-else v-model="prizeForm.exclusiveStock" :min="0" controls-position="right" style="width: 160px" />
          <div class="field-tip">仅用于必中策略发放，与普通抽奖分开计数</div>
        </el-form-item>
        <el-form-item label="概率" prop="probability">
          <el-input-number v-model="prizeForm.probability" :min="0" :max="10000" controls-position="right" style="width: 160px" />
          <span class="field-tip">万分制，约 {{ formatProbabilityPercent(prizeForm.probability) }}</span>
        </el-form-item>
        <el-form-item label="兜底奖">
          <el-switch v-model="prizeForm.isFallback" active-value="1" inactive-value="0" @change="onPrizeFallbackChange" />
          <span class="field-tip">须为虚拟类型</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPrizeForm">确 定</el-button>
        <el-button @click="prizeFormOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 必中策略 -->
    <el-drawer
      :title="'必中策略 · ' + currentActivityTitle"
      v-model="strategyOpen"
      size="960px"
      append-to-body
      destroy-on-close
      class="lottery-config-drawer"
    >
      <div class="drawer-body">
        <el-alert
          title="触发模式：仅一次=累计第 N 次必中；每N次循环=第 N/2N/3N… 次必中。同次命中多条时「仅一次」优先。库存不足发兜底。普通库存=大家一起抽；必中库存=定向必中专用。"
          type="warning"
          :closable="false"
          show-icon
          class="mb8"
        />
        <div class="drawer-toolbar mb8">
          <el-button type="primary" plain icon="Plus" @click="addStrategy">添加策略</el-button>
          <span class="field-tip">当前 {{ strategyList.length }} 条策略</span>
        </div>
        <div v-if="!strategyList.length" class="strategy-empty">暂无策略，点击上方「添加策略」开始配置</div>
        <div v-for="(item, index) in strategyList" :key="index" class="strategy-row">
          <div class="strategy-row-head">
            <span>策略 {{ index + 1 }}</span>
            <el-button link type="danger" icon="Delete" @click="removeStrategy(index)">删除</el-button>
          </div>
          <el-form label-width="90px" class="strategy-form">
            <el-row :gutter="12">
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="目标范围">
                  <el-select v-model="item.targetType" style="width: 100%" @change="onTargetTypeChange(item)">
                    <el-option label="全员" :value="1" />
                    <el-option label="指定用户" :value="2" />
                    <el-option label="人群包" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col v-if="item.targetType === 2" :xs="24" :sm="24" :md="16">
                <el-form-item label="指定会员">
                  <MemberSelect
                    :model-value="Array.isArray(item.memberIds) ? item.memberIds : []"
                    multiple
                    width="100%"
                    placeholder="搜索手机号/姓名/会员ID，可多选"
                    @update:model-value="(ids) => { item.memberIds = Array.isArray(ids) ? ids : [] }"
                  />
                </el-form-item>
              </el-col>
              <el-col v-if="item.targetType === 3" :xs="24" :sm="12" :md="8">
                <el-form-item label="人群包">
                  <el-select v-model="item.packageId" placeholder="选择人群包" filterable style="width: 100%">
                    <el-option v-for="pkg in crowdOptions" :key="pkg.packageId" :label="pkg.packageName" :value="pkg.packageId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="必中奖品">
                  <el-select v-model="item.targetPrizeId" placeholder="选择奖品" style="width: 100%">
                    <el-option v-for="p in activityPrizes" :key="p.prizeId" :label="p.prizeName || ('扇区' + p.position)" :value="p.prizeId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="触发模式">
                  <el-select v-model="item.loopMode" style="width: 100%">
                    <el-option label="仅一次" value="ONCE" />
                    <el-option label="每N次循环" value="LOOP" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item :label="item.loopMode === 'LOOP' ? '每几次' : '第几次'">
                  <el-input-number v-model="item.triggerCount" :min="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="库存来源">
                  <el-select v-model="item.stockMode" style="width: 100%">
                    <el-option label="必中库存优先" value="AUTO" />
                    <el-option label="只用必中库存" value="EXCLUSIVE" />
                    <el-option label="只用普通库存" value="PUBLIC" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="状态">
                  <el-radio-group v-model="item.status">
                    <el-radio value="0">启用</el-radio>
                    <el-radio value="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="submitStrategies">保 存</el-button>
          <el-button @click="strategyOpen = false">取 消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="BizLotteryActivity">
import {
  getSoleLotteryActivity,
  getLotteryActivity,
  addLotteryActivity,
  updateLotteryActivity,
  saveLotteryPrizes,
  saveLotteryStrategies,
  listCrowdPackageOptions,
  listLotteryPrizePoolOptions
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const loading = ref(true)
const prizeOpen = ref(false)
const prizeFormOpen = ref(false)
const prizeFormTitle = ref("")
const prizeEditIndex = ref(-1)
const poolOptions = ref<any[]>([])
const prizeForm = ref<any>({})
const strategyOpen = ref(false)
const currentActivityId = ref<number>()
const currentActivityTitle = ref("")
const prizeList = ref<any[]>([])
const strategyList = ref<any[]>([])
const activityPrizes = ref<any[]>([])
const crowdOptions = ref<any[]>([])
const overviewLoading = ref(false)
const overviewPrizes = ref<any[]>([])
const overviewStrategies = ref<any[]>([])

const data = reactive({
  form: {} as any,
  rules: {
    title: [{ required: true, message: "请填写活动名称", trigger: "blur" }],
    intervalHours: [{ required: true, message: "请填写频控间隔", trigger: "blur" }]
  }
})
const { form, rules } = toRefs(data)

const prizeFormRules = {
  poolId: [{ required: true, message: "请选择奖品", trigger: "change" }],
  position: [{ required: true, message: "请填写排序", trigger: "blur" }],
  probability: [{ required: true, message: "请填写概率", trigger: "blur" }]
}

const probabilitySum = computed(() => {
  return prizeList.value.reduce((sum, p) => sum + (Number(p.probability) || 0), 0)
})


/** 万分制转百分比展示，如 5000 → 50% */
function formatProbabilityPercent(value: number | string | null | undefined) {
  const n = Number(value)
  if (!Number.isFinite(n) || n <= 0) return "0%"
  const pct = n / 100
  if (Number.isInteger(pct)) return pct + "%"
  const fixed = pct.toFixed(2).replace(/\.?0+$/, "")
  return fixed + "%"
}

const prizeOverview = computed(() => {
  const list = overviewPrizes.value || []
  const sum = list.reduce((s, p) => s + (Number(p.probability) || 0), 0)
  const fallback = list.find((p) => p.isFallback === "1" || p.isFallback === 1)
  const names = list
    .slice()
    .sort((a, b) => (Number(a.position) || 0) - (Number(b.position) || 0))
    .map((p) => p.prizeName || ("扇区" + (p.position || "?")))
  const fallbackCount = list.filter((p) => p.isFallback === "1" || p.isFallback === 1).length
  return {
    count: list.length,
    probabilityText: formatProbabilityPercent(sum),
    fallbackText: fallback ? (fallback.prizeName || "已设置") : "未设置",
    names: names.slice(0, 6),
    ready: list.length > 0 && fallbackCount === 1 && sum === 10000
  }
})

const strategyOverview = computed(() => {
  const list = overviewStrategies.value || []
  const enabled = list.filter((s) => String(s.status ?? "0") === "0").length
  const lines = list.slice(0, 4).map((s, idx) => {
    const target =
      s.targetType === 2 ? "指定用户" : s.targetType === 3 ? "人群包" : "全员"
    const mode = s.loopMode === "LOOP" ? ("每" + (s.triggerCount || 1) + "次") : ("第" + (s.triggerCount || 1) + "次")
    const prize = s.targetPrizeName || s.prizeName || ("奖品#" + (s.targetPrizeId || "-"))
    const on = String(s.status ?? "0") === "0" ? "启用" : "停用"
    return (idx + 1) + ". " + target + " · " + mode + " · " + prize + "（" + on + "）"
  })
  return {
    count: list.length,
    enabled,
    disabled: Math.max(0, list.length - enabled),
    lines
  }
})


const selectedPoolPreview = computed(() => {
  if (!prizeForm.value?.poolId) return null
  return poolOptions.value.find((p) => p.poolId === prizeForm.value.poolId) || null
})

const DEFAULT_RULE_TEXT = `1、签到3天并推广3人实名注册即可获得一次抽奖机会；
2、抽取一次后，每隔72小时后抽取下一次；
3、一等奖、二等奖、三等奖抽到请联系客服发放，参与奖则会自动发放至账户助力值余额；

规则如有调整将会提前通知，最终解释权归星帆智联所有`

function applyOverview(detail: any) {
  overviewPrizes.value = buildPrizeRowsFromApi(detail?.prizes || [])
  overviewStrategies.value = (detail?.strategies || []).map((s: any) => {
    const prize = (detail?.prizes || []).find((p: any) => p.prizeId === s.targetPrizeId)
    return {
      ...s,
      targetPrizeName: prize?.prizeName || s.targetPrizeName || s.prizeName
    }
  })
}

function loadOverview(activityId?: number) {
  if (!activityId) {
    overviewPrizes.value = []
    overviewStrategies.value = []
    return Promise.resolve()
  }
  overviewLoading.value = true
  return getLotteryActivity(activityId).then((res: any) => {
    applyOverview(res.data)
  }).catch(() => {
    overviewPrizes.value = []
    overviewStrategies.value = []
  }).finally(() => {
    overviewLoading.value = false
  })
}

function loadSole() {
  loading.value = true
  getSoleLotteryActivity().then((res: any) => {
    if (res.data?.activityId) {
      form.value = { ...res.data }
      currentActivityId.value = res.data.activityId
      currentActivityTitle.value = res.data.title || ""
      return loadOverview(res.data.activityId)
    }
    form.value = {}
    currentActivityId.value = undefined
    currentActivityTitle.value = ""
    overviewPrizes.value = []
    overviewStrategies.value = []
  }).finally(() => {
    loading.value = false
  })
}

function reloadAll() {
  loadSole()
}

function reset() {
  form.value = {
    title: "幸运抽奖",
    startTime: undefined,
    endTime: undefined,
    intervalHours: 72,
    ruleText: DEFAULT_RULE_TEXT,
    status: "1"
  }
}

function handleInit() {
  reset()
  const payload = {
    ...form.value,
    startTime: null,
    endTime: null
  }
  addLotteryActivity(payload).then(() => {
    proxy.$modal.msgSuccess("抽奖活动已初始化，请继续配置奖品与策略后再启用")
    loadSole()
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const payload = {
      ...form.value,
      startTime: form.value.startTime || null,
      endTime: form.value.endTime || null
    }
    updateLotteryActivity(payload).then(() => {
      proxy.$modal.msgSuccess("保存成功")
      loadSole()
    })
  })
}

function prizeTypeLabel(type: number) {
  if (type === 1) return "实物"
  if (type === 2) return "现金"
  if (type === 3) return "虚拟"
  return "-"
}

function formatStockCell(row: any, pool: "public" | "exclusive") {
  if (pool === "public") {
    if (row.prizeType === 3 && (row.publicUnlimited || row.publicStock === -1)) return "无限"
    return row.publicStock ?? 0
  }
  if (row.prizeType === 3 && (row.exclusiveUnlimited || row.exclusiveStock === -1)) return "无限"
  return row.exclusiveStock ?? 0
}

function nextPrizePosition() {
  if (!prizeList.value.length) return 1
  return Math.max(...prizeList.value.map((p) => Number(p.position) || 0)) + 1
}

function normalizePrizeRow(p: any) {
  return {
    ...p,
    poolId: p.poolId,
    publicUnlimited: p.publicStock === -1 || p.publicUnlimited === true,
    exclusiveUnlimited: p.exclusiveStock === -1 || p.exclusiveUnlimited === true,
    publicStock: p.publicStock === -1 ? 0 : (p.publicStock ?? 0),
    exclusiveStock: p.exclusiveStock === -1 ? 0 : (p.exclusiveStock ?? 0),
    isFallback: p.isFallback || "0"
  }
}

function buildPrizeRowsFromApi(prizes: any[]) {
  return (prizes || [])
    .slice()
    .sort((a, b) => (a.position || 0) - (b.position || 0))
    .map((p) => normalizePrizeRow(p))
}

function loadPoolOptions() {
  return listLotteryPrizePoolOptions().then((res: any) => {
    poolOptions.value = res.data || []
  }).catch(() => {
    poolOptions.value = []
  })
}

function isPoolUsed(poolId: number) {
  if (!poolId) return false
  return prizeList.value.some((p, idx) => p.poolId === poolId && idx !== prizeEditIndex.value)
}

function onPoolSelected(poolId: number) {
  const pool = poolOptions.value.find((p) => p.poolId === poolId)
  if (!pool) return
  prizeForm.value.prizeName = pool.prizeName
  prizeForm.value.prizeDesc = pool.prizeDesc
  prizeForm.value.imageUrl = pool.imageUrl
  prizeForm.value.sectorBgColor = pool.sectorBgColor
  prizeForm.value.nameColor = pool.nameColor
  prizeForm.value.descColor = pool.descColor
  prizeForm.value.prizeType = pool.prizeType
  prizeForm.value.assistAmount = pool.assistAmount
  prizeForm.value.currency = pool.currency
  if (pool.prizeType !== 3) {
    prizeForm.value.publicUnlimited = false
    prizeForm.value.exclusiveUnlimited = false
    if (prizeForm.value.isFallback === "1") prizeForm.value.isFallback = "0"
  }
}

function resetPrizeForm(asFallback = false) {
  prizeForm.value = {
    prizeId: undefined,
    poolId: undefined,
    prizeName: "",
    prizeType: 3,
    position: nextPrizePosition(),
    publicStock: 0,
    exclusiveStock: 0,
    probability: asFallback ? 10000 : 0,
    isFallback: asFallback ? "1" : "0",
    publicUnlimited: false,
    exclusiveUnlimited: asFallback,
    assistAmount: undefined,
    currency: undefined
  }
  prizeEditIndex.value = -1
}

function handlePrizeAdd() {
  if (!poolOptions.value.length) {
    proxy.$modal.msgWarning("请先在「奖品管理」中添加奖品")
    return
  }
  resetPrizeForm(prizeList.value.length === 0)
  prizeFormTitle.value = "新增奖项"
  prizeFormOpen.value = true
}

function handlePrizeEdit(row: any, index: number) {
  prizeForm.value = { ...normalizePrizeRow(row) }
  prizeEditIndex.value = index
  prizeFormTitle.value = "修改奖项"
  prizeFormOpen.value = true
}

function onPrizeFallbackChange() {
  if (prizeForm.value.isFallback === "1" && prizeForm.value.prizeType !== 3) {
    proxy.$modal.msgWarning("兜底奖须为虚拟类型")
    prizeForm.value.isFallback = "0"
  }
}

function submitPrizeForm() {
  ;(proxy.$refs["prizeFormRef"] as any).validate((valid: boolean) => {
    if (!valid) return
    if (!prizeForm.value.poolId) {
      proxy.$modal.msgWarning("请选择奖品")
      return
    }
    const row = normalizePrizeRow({ ...prizeForm.value })
    const next = prizeList.value.map((p) => ({ ...p }))
    if (prizeEditIndex.value >= 0) {
      next[prizeEditIndex.value] = row
    } else {
      next.push(row)
    }
    if (row.isFallback === "1") {
      next.forEach((p, idx) => {
        const self = prizeEditIndex.value >= 0 ? idx === prizeEditIndex.value : idx === next.length - 1
        if (!self) p.isFallback = "0"
      })
    }
    prizeList.value = next
    prizeFormOpen.value = false
  })
}

function handlePrizeDelete(index: number) {
  if (prizeList.value.length <= 1) {
    proxy.$modal.msgWarning("至少保留一个奖项，或保存前自行调整")
  }
  const removed = prizeList.value[index]
  prizeList.value.splice(index, 1)
  if (removed?.isFallback === "1" && prizeList.value.length) {
    const last = prizeList.value[prizeList.value.length - 1]
    if (last.prizeType === 3) {
      last.isFallback = "1"
      last.exclusiveUnlimited = true
    }
  }
}

function openPrizeDialog() {
  if (!form.value.activityId) {
    proxy.$modal.msgWarning("请先初始化抽奖活动")
    return
  }
  currentActivityId.value = form.value.activityId
  currentActivityTitle.value = form.value.title || ""
  Promise.all([
    loadPoolOptions(),
    getLotteryActivity(form.value.activityId)
  ]).then((results: any[]) => {
    const res = results[1]
    prizeList.value = buildPrizeRowsFromApi(res.data?.prizes)
    prizeOpen.value = true
  })
}

function buildPrizesPayload() {
  return prizeList.value.map((p) => {
    let publicStock = p.publicUnlimited ? -1 : (p.publicStock ?? 0)
    let exclusiveStock = p.exclusiveUnlimited ? -1 : (p.exclusiveStock ?? 0)
    if (p.prizeType !== 3) {
      publicStock = Math.max(0, publicStock === -1 ? 0 : publicStock)
      exclusiveStock = Math.max(0, exclusiveStock === -1 ? 0 : exclusiveStock)
    }
    return {
      prizeId: p.prizeId,
      poolId: p.poolId,
      publicStock,
      exclusiveStock,
      probability: p.probability ?? 0,
      position: p.position,
      isFallback: p.isFallback || "0"
    }
  })
}

function validatePrizesLocal(prizes: any[]): string | null {
  if (!prizes.length) return "请至少配置一个奖品"
  let fallbackCount = 0
  const posSet = new Set<number>()
  const poolSet = new Set<number>()
  for (let i = 0; i < prizes.length; i++) {
    const p = prizes[i]
    if (!p.poolId) return "第" + (i + 1) + "个奖项请选择奖品"
    if (!p.position || p.position < 1) return "第" + (i + 1) + "个奖品排序须从1起"
    if (posSet.has(p.position)) return "排序号" + p.position + "重复"
    posSet.add(p.position)
    if (poolSet.has(p.poolId)) return "同一奖品不能重复配置"
    poolSet.add(p.poolId)
    const type = prizeList.value.find((x) => x.poolId === p.poolId)?.prizeType
    if ((type === 1 || type === 2) && (p.publicStock === -1 || p.exclusiveStock === -1)) {
      return "实物/现金奖品库存不能设为无限"
    }
    if (p.isFallback === "1") {
      fallbackCount++
      if (type !== 3) return "兜底奖品须为虚拟资产类型"
    }
  }
  if (fallbackCount !== 1) return "须恰好配置 1 个兜底奖品"
  return null
}

function submitPrizes() {
  const prizes = buildPrizesPayload()
  const err = validatePrizesLocal(prizes)
  if (err) {
    proxy.$modal.msgError(err)
    return
  }
  saveLotteryPrizes(currentActivityId.value!, prizes).then(() => {
    proxy.$modal.msgSuccess("奖品配置已保存")
    prizeOpen.value = false
    loadSole()
  })
}

function defaultStrategy() {
  return {
    targetType: 1,
    userIds: "",
    memberIds: [] as number[],
    packageId: undefined as number | undefined,
    targetPrizeId: undefined as number | undefined,
    triggerCount: 1,
    loopMode: "ONCE",
    stockMode: "AUTO",
    status: "0"
  }
}

function onTargetTypeChange(item: any) {
  if (item.targetType === 1) {
    item.userIds = ""
    item.memberIds = []
    item.packageId = undefined
  } else if (item.targetType === 2) {
    item.packageId = undefined
    if (!Array.isArray(item.memberIds)) item.memberIds = parseMemberIds(item.userIds)
  } else if (item.targetType === 3) {
    item.userIds = ""
    item.memberIds = []
  }
}

function parseMemberIds(userIds?: string): number[] {
  if (!userIds) return []
  return String(userIds)
    .split(/[,，\s]+/)
    .map((s) => Number(s.trim()))
    .filter((n) => Number.isFinite(n) && n > 0)
}

function joinMemberIds(ids?: number[]): string {
  return (ids || []).filter((n) => Number.isFinite(n) && n > 0).join(",")
}

function addStrategy() {
  strategyList.value.push(defaultStrategy())
}

function removeStrategy(index: number) {
  strategyList.value.splice(index, 1)
}

function openStrategyDialog() {
  if (!form.value.activityId) {
    proxy.$modal.msgWarning("请先初始化抽奖活动")
    return
  }
  currentActivityId.value = form.value.activityId
  currentActivityTitle.value = form.value.title || ""
  listCrowdPackageOptions().then((res: any) => {
    crowdOptions.value = res.data || []
  }).catch(() => {
    crowdOptions.value = []
  })
  getLotteryActivity(form.value.activityId).then((res: any) => {
    activityPrizes.value = res.data?.prizes || []
    strategyList.value = (res.data?.strategies || []).map((s: any) => ({
      ...s,
      loopMode: s.loopMode || "ONCE",
      stockMode: s.stockMode || "AUTO",
      memberIds: parseMemberIds(s.userIds)
    }))
    if (strategyList.value.length === 0) {
      strategyList.value.push(defaultStrategy())
    }
    strategyOpen.value = true
  })
}

function validateStrategiesLocal(): string | null {
  for (let i = 0; i < strategyList.value.length; i++) {
    const s = strategyList.value[i]
    if (s.targetType === 2 && !(Array.isArray(s.memberIds) ? s.memberIds.length : parseMemberIds(s.userIds).length)) {
      return "策略" + (i + 1) + "须选择指定会员"
    }
    if (s.targetType === 3 && !s.packageId) {
      return "策略" + (i + 1) + "须选择人群包"
    }
    if (!s.targetPrizeId) {
      return "策略" + (i + 1) + "须选择必中奖品"
    }
    if (!s.triggerCount || s.triggerCount < 1) {
      return "策略" + (i + 1) + "触发次数须大于等于1"
    }
  }
  return null
}

function checkMeltdownWarning(): string | null {
  for (const s of strategyList.value) {
    if (!s.targetPrizeId) continue
    const mode = s.stockMode || "AUTO"
    const prize = activityPrizes.value.find((p) => p.prizeId === s.targetPrizeId)
    if (!prize) continue
    if (mode === "EXCLUSIVE" && prize.exclusiveStock === 0) {
      return "策略关联奖品「" + (prize.prizeName || "扇区" + prize.position) + "」必中库存为 0，且库存来源为「只用必中库存」，命中后将走兜底或熔断。是否仍要保存？"
    }
    if (mode === "PUBLIC" && prize.publicStock === 0) {
      return "策略关联奖品「" + (prize.prizeName || "扇区" + prize.position) + "」普通库存为 0，且库存来源为「只用普通库存」，命中后将走兜底或熔断。是否仍要保存？"
    }
    if (mode === "AUTO" && prize.exclusiveStock === 0 && prize.publicStock === 0) {
      return "策略关联奖品「" + (prize.prizeName || "扇区" + prize.position) + "」必中库存与普通库存均为 0，命中后将走兜底或熔断。是否仍要保存？"
    }
  }
  return null
}

function doSaveStrategies() {
  const payload = strategyList.value.map((s) => ({
    strategyId: s.strategyId,
    targetType: s.targetType,
    userIds: s.targetType === 2 ? joinMemberIds(s.memberIds?.length ? s.memberIds : parseMemberIds(s.userIds)) : undefined,
    packageId: s.targetType === 3 ? s.packageId : undefined,
    targetPrizeId: s.targetPrizeId,
    triggerCount: s.triggerCount,
    loopMode: s.loopMode || "ONCE",
    stockMode: s.stockMode || "AUTO",
    status: s.status || "0"
  }))
  saveLotteryStrategies(currentActivityId.value!, payload).then(() => {
    proxy.$modal.msgSuccess("必中策略已保存")
    strategyOpen.value = false
    loadOverview(currentActivityId.value)
  })
}

function submitStrategies() {
  const err = validateStrategiesLocal()
  if (err) {
    proxy.$modal.msgError(err)
    return
  }
  const warn = checkMeltdownWarning()
  if (warn) {
    proxy.$modal.confirm(warn).then(() => doSaveStrategies()).catch(() => {})
  } else {
    doSaveStrategies()
  }
}

loadSole()
</script>

<style scoped>
.lottery-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  padding: 12px 16px;
  background: var(--el-bg-color);
  border: var(--ops-border, 1px solid var(--el-border-color-lighter));
  border-radius: var(--ops-radius, 8px);
  box-shadow: var(--ops-shadow, none);
}
.lottery-toolbar__main {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}
.lottery-toolbar__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.lottery-toolbar__status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.lottery-toolbar__label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.lottery-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}
.field-with-tip {
  width: 100%;
}
.field-with-tip .field-tip {
  margin: 6px 0 0;
  margin-left: 0;
  line-height: 1.5;
}
.interval-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.interval-unit {
  font-size: 13px;
  color: var(--el-text-color-regular);
}
.config-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding-bottom: 10px;
}
.config-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 14px 16px;
  cursor: pointer;
  background: var(--el-bg-color);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}
.config-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
}
.config-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}
.config-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.config-card__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 10px;
}
.stat-num {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-right: 4px;
}
.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.config-card__echo {
  min-height: 40px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  word-break: break-word;
}
.config-card__echo.is-muted {
  color: var(--el-text-color-secondary);
}
.config-card__action {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: 500;
}
@media (max-width: 768px) {
  .config-cards {
    grid-template-columns: 1fr;
  }
  .lottery-toolbar__actions {
    width: 100%;
  }
}
.pool-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
}
.pool-preview-img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}
.field-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.prize-desc-preview {
  white-space: pre-line;
  line-height: 1.4;
  word-break: break-word;
}
.mt8 {
  margin-top: 8px;
}
.prize-virtual-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}
.strategy-row {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
}
.strategy-row-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 600;
}

.drawer-body {
  padding: 0 4px 12px;
}
.drawer-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.drawer-toolbar .field-tip {
  margin-left: 0;
}
.drawer-table {
  width: 100%;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.strategy-empty {
  padding: 28px 12px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  margin-bottom: 12px;
}
.strategy-form {
  width: 100%;
}
.strategy-form .el-form-item {
  margin-bottom: 12px;
}
:deep(.lottery-config-drawer .el-drawer__body) {
  padding: 12px 16px 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
}
:deep(.lottery-config-drawer .el-drawer__footer) {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-extra-light);
}
</style>
