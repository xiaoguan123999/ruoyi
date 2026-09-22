<template>
  <div class="app-container ops-page">
    <el-alert
      title="同一产品可同时配人民币和 USDT。双币按钮由认购价格决定；「跳过二级页」独立控制列表直购。业务模式：日返=按天返利，可另配认购发助力；助力退本=无日返、发助力、到期退本。每人限购填累计可买份数，0 或不填表示不限制。一拖二仅日返模式有效。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <div class="ops-section-card">
      <div class="ops-section-card__hd">产品日返入账</div>
      <div class="ops-section-card__bd">
        <el-form :inline="true" v-loading="creditLoading">
          <el-form-item label="到账钱包">
            <WalletTypeSelect v-model="rebateWalletType" />
            <span class="tip">产品每日返利进这个钱包，默认产品收益</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveCredit" v-hasPermi="['biz:product:edit']">保存</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属系列" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="系列" clearable style="width: 200px">
          <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item label="支持币种" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="支持币种" clearable style="width: 140px">
          <el-option label="人民币" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="上架" value="0" />
          <el-option label="下架" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="在售" prop="onSale">
        <el-select v-model="queryParams.onSale" placeholder="在售" clearable style="width: 140px">
          <el-option label="在售" value="1" />
          <el-option label="不在售" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:product:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="productList">
      <el-table-column label="ID" align="center" prop="productId" width="70" />
      <el-table-column label="系列" align="center" prop="categoryName" min-width="140" show-overflow-tooltip />
      <el-table-column label="排序" align="center" prop="sort" width="70" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="120" />
      <el-table-column label="卡片模板" align="center" prop="templateName" min-width="110" show-overflow-tooltip />
      <el-table-column label="业务模式" align="center" prop="bizMode" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.bizMode === 'ASSIST' ? 'warning' : 'success'" size="small">
            {{ scope.row.bizMode === 'ASSIST' ? '助力退本' : '日返' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="人民币价" align="center" prop="priceCny" width="100" />
      <el-table-column label="人民币日返" align="center" prop="dailyRebateCny" width="110" />
      <el-table-column label="USDT价" align="center" prop="priceUsdt" width="90" />
      <el-table-column label="USDT日返" align="center" prop="dailyRebateUsdt" width="100" />
      <el-table-column label="天数" align="center" width="80">
        <template #default="scope">
          <span v-if="scope.row.bizMode === 'ASSIST'">退本{{ scope.row.principalReturnDays || 0 }}天</span>
          <span v-else>{{ scope.row.durationDays }}</span>
        </template>
      </el-table-column>
      <el-table-column label="限购" align="center" prop="buyLimit" width="80">
        <template #default="scope">
          <span>{{ scope.row.buyLimit > 0 ? scope.row.buyLimit + "份" : "不限" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="一拖二" align="center" min-width="120">
        <template #default="scope">
          <span>{{ unlockText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提现指定" align="center" prop="withdrawRequired" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.withdrawRequired === '1' ? 'warning' : 'info'">{{ scope.row.withdrawRequired === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在售" align="center" width="80">
        <template #default="scope">
          <el-tag :type="isOnSale(scope.row) ? 'success' : 'info'">{{ isOnSale(scope.row) ? '在售' : '不在售' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" fixed="right" class-name="product-ops-col">
        <template #default="scope">
          <div class="product-ops">
            <el-button
              link
              type="primary"
              :icon="isOnSale(scope.row) ? 'CircleClose' : 'CircleCheck'"
              @click="toggleOnSale(scope.row)"
              v-hasPermi="['biz:product:edit']"
            >{{ isOnSale(scope.row) ? '停售' : '开售' }}</el-button>
            <el-button
              link
              type="primary"
              :icon="scope.row.status === '0' ? 'Bottom' : 'Top'"
              @click="toggleStatus(scope.row)"
              v-hasPermi="['biz:product:edit']"
            >{{ scope.row.status === '0' ? '下架' : '上架' }}</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:product:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:product:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-drawer :title="title" v-model="open" size="720px" append-to-body destroy-on-close class="product-drawer">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="product-drawer-form">
        <div class="publish-bar">
          <div class="publish-bar__item">
            <span class="publish-bar__label">上架</span>
            <el-switch v-model="form.status" active-value="0" inactive-value="1" />
          </div>
          <div class="publish-bar__item">
            <span class="publish-bar__label">开放认购</span>
            <el-switch v-model="form.onSale" active-value="1" inactive-value="0" />
            <el-tooltip content="关闭后列表点认购提示暂未开放，与上架独立" placement="top">
              <el-icon class="publish-bar__help"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="publish-bar__item">
            <span class="publish-bar__label">跳过二级页</span>
            <el-switch v-model="form.skipDetail" active-value="1" inactive-value="0" />
            <el-tooltip content="开启后列表直接认购，不进详情页；与业务模式无关。双按钮只看是否配了 CNY/USDT 价格" placement="top">
              <el-icon class="publish-bar__help"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <div class="form-section-title">产品是谁</div>
        <el-form-item label="所属系列" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择系列" style="width: 100%" @change="onCategoryChange">
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="App 卡片主标题" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="英文名" prop="nameEn">
              <el-input v-model="form.nameEn" placeholder="卡片副标题，可空" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="业务模式" prop="bizMode">
          <el-radio-group v-model="form.bizMode">
            <el-radio value="REBATE">日返</el-radio>
            <el-radio value="ASSIST">助力退本</el-radio>
          </el-radio-group>
          <el-tooltip
            content="日返：扣款后按天发返利。助力退本：无日返，认购发助力值，到期退本。列表是否直购看上方「跳过二级页」。"
            placement="top"
          >
            <el-icon class="publish-bar__help" style="margin-left: 8px"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>

        <el-tabs v-model="drawerTab" class="product-tabs">
          <el-tab-pane label="业务" name="biz">
            <el-form-item label="认购价格">
              <div class="field-with-tip">
                <div class="dual-input">
                  <div class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.priceCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.priceUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
                <p class="field-tip">至少填一种；有 CNY/USDT 价才会出对应认购按钮</p>
              </div>
            </el-form-item>
            <template v-if="form.bizMode === 'ASSIST'">
              <el-form-item label="发放模式">
                <div class="assist-grant-mode">
                  <el-radio-group v-model="form.assistGrantMode" @change="onAssistGrantModeChange">
                    <el-radio value="CNY">固定送 CNY</el-radio>
                    <el-radio value="USDT">固定送 USDT</el-radio>
                    <el-radio value="MATCH">跟认购币种</el-radio>
                    <el-radio value="BOTH">双币都送</el-radio>
                  </el-radio-group>
                  <p class="field-tip">{{ assistGrantModeTip }}</p>
                </div>
              </el-form-item>
              <el-form-item label="助力值">
                <div class="dual-input">
                  <div v-if="showAssistCny" class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.assistValueCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div v-if="showAssistUsdt" class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.assistValueUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="本金返还" prop="principalReturnDays">
                <el-input-number v-model="form.principalReturnDays" :min="1" controls-position="right" style="width: 160px" />
                <span class="field-tip inline">天后回余额</span>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="每日返利">
                <div class="dual-input">
                  <div class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.dailyRebateCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.dailyRebateUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="返利天数" prop="durationDays">
                <el-input-number v-model="form.durationDays" :min="1" controls-position="right" style="width: 180px" />
              </el-form-item>
              <el-form-item label="入账方式">
                <div class="field-with-tip">
                  <el-radio-group v-model="form.incomeMode">
                    <el-radio value="CREDIT">每天进产品收益钱包</el-radio>
                    <el-radio value="ACCUMULATE">订单累计后结算</el-radio>
                  </el-radio-group>
                  <p class="field-tip">累计模式：日返先记在认购单，满周期且持有对档产品后结算进产品收益，再走现有提现</p>
                </div>
              </el-form-item>
              <template v-if="form.incomeMode === 'ACCUMULATE'">
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="累计周期" prop="accumulateCycleDays">
                      <el-input-number v-model="form.accumulateCycleDays" :min="1" controls-position="right" style="width: 100%" />
                      <span class="field-tip inline">天（如 60）</span>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="对档产品" prop="relatedProductId">
                      <el-select v-model="form.relatedProductId" filterable clearable placeholder="结算前须持有" style="width: 100%">
                        <el-option
                          v-for="item in relatedProductOptions"
                          :key="item.productId"
                          :label="item.productName"
                          :value="item.productId"
                          :disabled="item.productId === form.productId"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>
              <div class="form-section-title is-follow">认购发助力（选填）</div>
              <el-form-item label="发放模式">
                <div class="assist-grant-mode">
                  <el-radio-group v-model="form.assistGrantMode" @change="onAssistGrantModeChange">
                    <el-radio value="CNY">固定送 CNY</el-radio>
                    <el-radio value="USDT">固定送 USDT</el-radio>
                    <el-radio value="MATCH">跟认购币种</el-radio>
                    <el-radio value="BOTH">双币都送</el-radio>
                  </el-radio-group>
                  <p class="field-tip">深空等日返产品可额外发助力；不填助力值则不发放</p>
                </div>
              </el-form-item>
              <el-form-item label="助力值">
                <div class="dual-input">
                  <div v-if="showAssistCny" class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.assistValueCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div v-if="showAssistUsdt" class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.assistValueUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
              </el-form-item>
            </template>

            <div class="form-section-title is-follow">认购规则</div>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="每人限购" prop="buyLimit">
                  <div class="field-with-tip">
                    <el-input-number v-model="form.buyLimit" :min="0" :step="1" controls-position="right" style="width: 100%" />
                    <p class="field-tip">0 = 不限制</p>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="提现指定">
                  <div class="field-with-tip">
                    <el-radio-group v-model="form.withdrawRequired">
                      <el-radio value="1">是</el-radio>
                      <el-radio value="0">否</el-radio>
                    </el-radio-group>
                    <p class="field-tip">选「是」时，认购后才可提现</p>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
            <template v-if="form.bizMode !== 'ASSIST'">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="一拖二份数" prop="unlockDirectQty">
                    <div class="field-with-tip">
                      <el-input-number v-model="form.unlockDirectQty" :min="0" :step="1" controls-position="right" style="width: 100%" />
                      <p class="field-tip">直属下级累计认购达标份数；0 关闭</p>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="等待小时" prop="unlockDelayHours">
                    <div class="field-with-tip">
                      <el-input-number v-model="form.unlockDelayHours" :min="0" :step="1" controls-position="right" style="width: 100%" />
                      <p class="field-tip">达标后再等多少小时开始日返；0 关闭</p>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </template>
          </el-tab-pane>

          <el-tab-pane label="卡片" name="card">
            <p class="section-tip">选模板、封面和主题色即可；坑位指标按模板默认自动绑定业务字段，一般不用改。</p>
            <el-form-item label="卡片模板" prop="templateId">
              <el-select v-model="form.templateId" placeholder="请选择卡片模板" style="width: 100%" @change="onTemplateChange">
                <el-option
                  v-for="item in templateOptions"
                  :key="item.templateId"
                  :label="item.templateName + '（' + item.templateCode + '）'"
                  :value="item.templateId"
                >
                  <span>{{ item.templateName }}</span>
                  <span style="float: right; color: var(--el-text-color-secondary); font-size: 12px">{{ item.templateCode }} · {{ item.metricSlotCount }}坑</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="封面">
              <image-upload v-model="form.coverUrl" :limit="1" />
              <p class="section-tip" style="margin-top: 6px">
                深空序号卡封面铺满整卡；主题色用于按钮、描边和文字倾向。浅色偏深字，深色偏浅字。
              </p>
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="主题色" prop="theme">
                  <div class="field-with-tip">
                    <div class="theme-color-row">
                      <el-color-picker v-model="form.theme" color-format="hex" :predefine="themePresets" />
                      <el-input v-model="form.theme" maxlength="16" placeholder="#2F7BFF" style="width: 132px" />
                    </div>
                    <p class="field-tip">取色或填色值。常用：蓝 #2F7BFF、紫 #7B6BFF、金 #C9A227</p>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="卡片序号">
                  <el-input v-model="form.cardNo" maxlength="8" placeholder="如 01，NUMBERED 用" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="角标文案">
              <el-input v-model="form.badgeText" maxlength="64" placeholder="空则用系列名称" />
            </el-form-item>
            <el-form-item label="按钮文案">
              <el-input v-model="form.ctaText" maxlength="32" placeholder="空则用模板默认" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="文案" name="copy">
            <p class="section-tip">认购详情页展示用，均可空。</p>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="发放方式">
                  <el-input v-model="form.payoutMethod" maxlength="100" placeholder="例如：每日发放" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="风险等级">
                  <el-input v-model="form.riskLevel" maxlength="64" placeholder="例如：中" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="卡片说明文案，选填" />
            </el-form-item>
            <el-form-item label="激活条件">
              <el-input
                v-model="form.unlockRuleText"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="如：直推下级认购2份相同价格产品"
              />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
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

<script setup lang="ts" name="BizProduct">
import { listProduct, getProduct, addProduct, updateProduct, delProduct, listProductCategoryOptions, listProductCardTemplateOptions, getWalletCreditByBiz, saveWalletCreditByBiz } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"
import { QuestionFilled } from "@element-plus/icons-vue"

const { proxy } = getCurrentInstance() as any
const THEME_NAME_HEX: Record<string, string> = {
  blue: "#2F7BFF",
  purple: "#7B6BFF",
  gold: "#C9A227",
  cyan: "#1AA7A0",
  silver: "#8A94A6"
}
const themePresets = Object.values(THEME_NAME_HEX)

function normalizeThemeColor(value?: string) {
  const raw = String(value || "").trim()
  if (!raw) return THEME_NAME_HEX.blue
  const named = THEME_NAME_HEX[raw.toLowerCase()]
  if (named) return named
  if (/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(raw)) {
    if (raw.length === 4) {
      return `#${raw[1]}${raw[1]}${raw[2]}${raw[2]}${raw[3]}${raw[3]}`.toUpperCase()
    }
    return raw.toUpperCase()
  }
  return THEME_NAME_HEX.blue
}

const productList = ref<any[]>([])
const categoryOptions = ref<any[]>([])
const templateOptions = ref<any[]>([])
const relatedProductOptions = ref<any[]>([])
const open = ref(false)
const drawerTab = ref("biz")
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, productName: undefined, currency: undefined, status: undefined, onSale: undefined, categoryId: undefined },
  rules: {
    categoryId: [{ required: true, message: "请选择所属系列", trigger: "change" }],
    productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
    bizMode: [{ required: true, message: "请选择业务模式", trigger: "change" }],
    durationDays: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode === "ASSIST") return callback()
        if (value == null || value === "" || Number(value) <= 0) return callback(new Error("返利天数不能为空"))
        callback()
      },
      trigger: "blur"
    }],
    principalReturnDays: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode !== "ASSIST") return callback()
        if (value == null || value === "" || Number(value) <= 0) return callback(new Error("请填写本金返还天数"))
        callback()
      },
      trigger: "blur"
    }],
    accumulateCycleDays: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode === "ASSIST" || form.value.incomeMode !== "ACCUMULATE") return callback()
        if (value == null || value === "" || Number(value) <= 0) return callback(new Error("请填写累计周期天数"))
        callback()
      },
      trigger: "blur"
    }],
    relatedProductId: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode === "ASSIST" || form.value.incomeMode !== "ACCUMULATE") return callback()
        if (value == null || value === "") return callback(new Error("请选择对档产品"))
        callback()
      },
      trigger: "change"
    }],
    templateId: [{ required: true, message: "请选择卡片模板", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const showAssistCny = computed(() => {
  const m = String(form.value.assistGrantMode || "CNY").toUpperCase()
  return m === "CNY" || m === "MATCH" || m === "BOTH"
})
const showAssistUsdt = computed(() => {
  const m = String(form.value.assistGrantMode || "CNY").toUpperCase()
  return m === "USDT" || m === "MATCH" || m === "BOTH"
})
const assistGrantModeTip = computed(() => {
  const m = String(form.value.assistGrantMode || "CNY").toUpperCase()
  if (m === "USDT") return "任意币认购都只发 USDT 助力"
  if (m === "MATCH") return "买什么币就发什么币助力；双币认购时两侧都建议填"
  if (m === "BOTH") return "一次认购同时发 CNY + USDT 助力"
  return "默认：任意币认购都只发 CNY 助力（与人民币 1:1）"
})

function onAssistGrantModeChange(mode: string) {
  const m = String(mode || "CNY").toUpperCase()
  form.value.assistGrantMode = m
  if (m === "CNY") form.value.assistValueUsdt = 0
  if (m === "USDT") form.value.assistValueCny = 0
}
const creditLoading = ref(false)
const rebateWalletType = ref("PRODUCT")

const METRIC_DEFAULTS: Record<string, Array<{ label: string; source: string; customText?: string }>> = {
  CLASSIC: [
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "收益周期", source: "DURATION" }
  ],
  HERO: [
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "收益周期", source: "DURATION" }
  ],
  SPLIT: [
    { label: "金额", source: "PRICE" },
    { label: "助力值", source: "ASSIST_VALUE" },
    { label: "限购", source: "BUY_LIMIT" },
    { label: "本金返还", source: "PRINCIPAL_RETURN" }
  ],
  NUMBERED: [
    { label: "金额", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "助力值", source: "ASSIST_VALUE" },
    { label: "收益周期", source: "DURATION" }
  ],
  ROW: [
    { label: "价格", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" }
  ],
  COMPACT: [
    { label: "金额", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "助力值", source: "ASSIST_VALUE" },
    { label: "收益周期", source: "DURATION" }
  ],
  BANNER: [
    { label: "价格", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" }
  ],
  PRICE_FOCUS: [
    { label: "收益周期", source: "DURATION" },
    { label: "每日收益", source: "DAILY_REBATE" }
  ],
  MEDIA_LEFT: [
    { label: "价格", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "收益周期", source: "DURATION" }
  ]
}

function buildDefaultMetrics(templateCode?: string, slotCount?: number) {
  const code = String(templateCode || "CLASSIC").toUpperCase()
  const defs = METRIC_DEFAULTS[code] || METRIC_DEFAULTS.CLASSIC
  const count = slotCount != null ? Number(slotCount) : defs.length
  const rows: any[] = []
  for (let i = 0; i < count; i++) {
    const d = defs[i] || { label: "指标" + (i + 1), source: "CUSTOM", customText: "" }
    rows.push({
      slotIndex: i + 1,
      label: d.label,
      source: d.source,
      customText: d.customText || ""
    })
  }
  return rows
}

function findTemplate(id?: number) {
  return templateOptions.value.find((t: any) => t.templateId === id)
}

function onTemplateChange(templateId: number) {
  const tpl = findTemplate(templateId)
  if (!tpl) return
  const apply = () => {
    form.value.metrics = buildDefaultMetrics(tpl.templateCode, tpl.metricSlotCount)
  }
  if (form.value._skipTemplateConfirm) {
    form.value._skipTemplateConfirm = false
    apply()
    return
  }
  if (form.value.metrics && form.value.metrics.length) {
    proxy.$modal.confirm("切换模板将按该模板默认坑位重新绑定展示字段，是否继续？").then(apply).catch(() => {})
  } else {
    apply()
  }
}

function onCategoryChange(categoryId?: number) {
  if (form.value.productId) return
  applySeriesDefaultTemplate(categoryId)
}

function applySeriesDefaultTemplate(categoryId?: number) {
  if (!categoryId) return
  const cat = categoryOptions.value.find((c: any) => c.categoryId === categoryId)
  if (cat?.defaultTemplateId) {
    form.value.templateId = cat.defaultTemplateId
    const tpl = findTemplate(cat.defaultTemplateId)
    form.value.metrics = buildDefaultMetrics(tpl?.templateCode, tpl?.metricSlotCount)
  } else if (!form.value.templateId && templateOptions.value.length) {
    const classic = templateOptions.value.find((t: any) => t.templateCode === "CLASSIC") || templateOptions.value[0]
    form.value.templateId = classic.templateId
    form.value.metrics = buildDefaultMetrics(classic.templateCode, classic.metricSlotCount)
  }
}

function loadCredit() {
  creditLoading.value = true
  getWalletCreditByBiz("REBATE").then((res: any) => {
    rebateWalletType.value = res.data?.typeCode || "PRODUCT"
  }).finally(() => { creditLoading.value = false })
}
function saveCredit() {
  saveWalletCreditByBiz("REBATE", rebateWalletType.value).then(() => {
    proxy.$modal.msgSuccess("保存成功")
    loadCredit()
  })
}

function loadCategories() {
  listProductCategoryOptions().then((res: any) => {
    categoryOptions.value = res.data || []
  })
}
function loadRelatedProducts() {
  listProduct({ pageNum: 1, pageSize: 500 }).then((res: any) => {
    relatedProductOptions.value = res.rows || []
  }).catch(() => { relatedProductOptions.value = [] })
}
function loadTemplates() {
  listProductCardTemplateOptions().then((res: any) => {
    templateOptions.value = res.data || []
  }).catch(() => { templateOptions.value = [] })
}
function getList() {
  loading.value = true
  listProduct(queryParams.value).then((res: any) => {
    productList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function unlockText(row: any) {
  const qty = Number(row.unlockDirectQty || 0)
  const hours = Number(row.unlockDelayHours || 0)
  if (qty <= 0 && hours <= 0) return "关闭"
  const parts: string[] = []
  if (qty > 0) parts.push("直属" + qty + "份")
  if (hours > 0) parts.push(hours + "小时")
  return parts.join(" / ")
}
function isOnSale(row: any) {
  if (typeof row.onSaleFlag === "boolean") return row.onSaleFlag
  return row.onSale === "1" || row.onSale === 1 || row.onSale === true
}
function normalizeOnSale(data: any) {
  if (data.onSale == null || data.onSale === "") {
    return data.onSaleFlag === false ? "0" : "1"
  }
  return data.onSale === "1" || data.onSale === 1 || data.onSale === true ? "1" : "0"
}
/** 快捷调整：先取详情再整单提交，避免 PUT 缺参 */
function patchProduct(row: any, patch: Record<string, any>) {
  return getProduct(row.productId).then((res: any) => {
    const data = { ...(res.data || {}), ...patch }
    data.onSale = normalizeOnSale(data)
    data.buyLimit = Number(data.buyLimit || 0)
    data.unlockDirectQty = Number(data.unlockDirectQty || 0)
    data.unlockDelayHours = Number(data.unlockDelayHours || 0)
    return updateProduct(data)
  })
}
function toggleOnSale(row: any) {
  const next = isOnSale(row) ? "0" : "1"
  const text = next === "1" ? "开售" : "停售"
  proxy.$modal.confirm(`确认要「${text}」产品「${row.productName}」吗？`).then(() => {
    return patchProduct(row, { onSale: next })
  }).then(() => {
    row.onSale = next
    row.onSaleFlag = next === "1"
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {})
}
function toggleStatus(row: any) {
  const next = row.status === "0" ? "1" : "0"
  const text = next === "0" ? "上架" : "下架"
  proxy.$modal.confirm(`确认要「${text}」产品「${row.productName}」吗？`).then(() => {
    return patchProduct(row, { status: next })
  }).then(() => {
    row.status = next
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {})
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = {
    status: "0",
    onSale: "1",
    skipDetail: "0",
    bizMode: "REBATE",
    withdrawRequired: "0",
    buyLimit: 0,
    unlockDirectQty: 0,
    unlockDelayHours: 0,
    payoutMethod: "",
    riskLevel: "",
    unlockRuleText: "",
    sort: 0,
    categoryId: undefined,
    nameEn: "",
    coverUrl: "",
    priceCny: undefined,
    dailyRebateCny: undefined,
    priceUsdt: undefined,
    dailyRebateUsdt: undefined,
    assistValueCny: undefined,
    assistValueUsdt: undefined,
    assistGrantMode: "CNY",
    principalReturnDays: undefined,
    durationDays: undefined,
    incomeMode: "CREDIT",
    accumulateCycleDays: 0,
    relatedProductId: undefined,
    templateId: undefined,
    theme: THEME_NAME_HEX.blue,
    badgeText: "",
    cardNo: "",
    ctaText: "",
    metrics: []
  }
  proxy.resetForm("formRef")
}
function handleAdd() {
  reset()
  drawerTab.value = "biz"
  open.value = true
  title.value = "新增产品"
  nextTick(() => {
    if (categoryOptions.value.length === 1) {
      form.value.categoryId = categoryOptions.value[0].categoryId
      applySeriesDefaultTemplate(form.value.categoryId)
    } else if (templateOptions.value.length) {
      const classic = templateOptions.value.find((t: any) => t.templateCode === "CLASSIC") || templateOptions.value[0]
      form.value.templateId = classic.templateId
      form.value.metrics = buildDefaultMetrics(classic.templateCode, classic.metricSlotCount)
    }
  })
}
function handleUpdate(row: any) {
  reset()
  drawerTab.value = "biz"
  getProduct(row.productId).then((res: any) => {
    form.value = res.data || {}
    if (form.value.onSale == null || form.value.onSale === "") {
      form.value.onSale = form.value.onSaleFlag === false ? "0" : "1"
    } else {
      form.value.onSale = form.value.onSale === "1" || form.value.onSale === 1 || form.value.onSale === true ? "1" : "0"
    }
    form.value.theme = normalizeThemeColor(form.value.theme)
    if (!form.value.bizMode) form.value.bizMode = "REBATE"
    if (!form.value.assistGrantMode) form.value.assistGrantMode = "CNY"
    if (!form.value.incomeMode) form.value.incomeMode = "CREDIT"
    if (form.value.accumulateCycleDays == null) form.value.accumulateCycleDays = 0
    if (form.value.skipDetail == null || form.value.skipDetail === "") {
      form.value.skipDetail = form.value.skipDetailFlag === true ? "1" : "0"
    } else {
      form.value.skipDetail = form.value.skipDetail === "1" || form.value.skipDetail === 1 || form.value.skipDetail === true ? "1" : "0"
    }
    if (!form.value.metrics || !form.value.metrics.length) {
      const tpl = findTemplate(form.value.templateId)
      form.value.metrics = buildDefaultMetrics(tpl?.templateCode || form.value.templateCode, tpl?.metricSlotCount)
    }
    open.value = true
    title.value = "修改产品"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean, fields?: Record<string, any>) => {
    if (!valid) {
      const keys = Object.keys(fields || {})
      if (keys.some((k) => ["durationDays", "principalReturnDays", "buyLimit"].includes(k))) {
        drawerTab.value = "biz"
      } else if (keys.includes("templateId")) {
        drawerTab.value = "card"
      }
      return
    }
    const cny = Number(form.value.priceCny || 0)
    const usdt = Number(form.value.priceUsdt || 0)
    if (cny <= 0 && usdt <= 0) {
      proxy.$modal.msgError("请至少配置人民币或USDT认购价格")
      return
    }
    form.value.theme = normalizeThemeColor(form.value.theme)
    form.value.bizMode = form.value.bizMode === "ASSIST" ? "ASSIST" : "REBATE"
    form.value.skipDetail = form.value.skipDetail === "1" || form.value.skipDetail === true ? "1" : "0"
    if (form.value.bizMode === "ASSIST") {
      const mode = String(form.value.assistGrantMode || "CNY").toUpperCase()
      form.value.assistGrantMode = ["CNY", "USDT", "MATCH", "BOTH"].includes(mode) ? mode : "CNY"
      const assistCny = Number(form.value.assistValueCny || 0)
      const assistUsdt = Number(form.value.assistValueUsdt || 0)
      if (form.value.assistGrantMode === "CNY" && assistCny <= 0) {
        proxy.$modal.msgError("固定送 CNY 时请填写 CNY 助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "USDT" && assistUsdt <= 0) {
        proxy.$modal.msgError("固定送 USDT 时请填写 USDT 助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "BOTH" && (assistCny <= 0 || assistUsdt <= 0)) {
        proxy.$modal.msgError("双币都送时请同时填写 CNY 与 USDT 助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "MATCH" && assistCny <= 0 && assistUsdt <= 0) {
        proxy.$modal.msgError("跟认购币种时请至少填写一种币种助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "CNY") form.value.assistValueUsdt = 0
      if (form.value.assistGrantMode === "USDT") form.value.assistValueCny = 0
      if (!form.value.principalReturnDays || Number(form.value.principalReturnDays) <= 0) {
        proxy.$modal.msgError("请填写本金返还天数")
        drawerTab.value = "biz"
        return
      }
      form.value.dailyRebateCny = 0
      form.value.dailyRebateUsdt = 0
      form.value.durationDays = Number(form.value.principalReturnDays)
      form.value.unlockDirectQty = 0
      form.value.unlockDelayHours = 0
      form.value.incomeMode = "CREDIT"
      form.value.accumulateCycleDays = 0
      form.value.relatedProductId = undefined
    } else {
      form.value.incomeMode = form.value.incomeMode === "ACCUMULATE" ? "ACCUMULATE" : "CREDIT"
      if (form.value.incomeMode !== "ACCUMULATE") {
        form.value.accumulateCycleDays = 0
        form.value.relatedProductId = undefined
      }
      const mode = String(form.value.assistGrantMode || "CNY").toUpperCase()
      form.value.assistGrantMode = ["CNY", "USDT", "MATCH", "BOTH"].includes(mode) ? mode : "CNY"
      const assistCny = Number(form.value.assistValueCny || 0)
      const assistUsdt = Number(form.value.assistValueUsdt || 0)
      if (assistCny > 0 || assistUsdt > 0) {
        if (form.value.assistGrantMode === "CNY" && assistCny <= 0) {
          proxy.$modal.msgError("固定送 CNY 时请填写 CNY 助力值")
          drawerTab.value = "biz"
          return
        }
        if (form.value.assistGrantMode === "USDT" && assistUsdt <= 0) {
          proxy.$modal.msgError("固定送 USDT 时请填写 USDT 助力值")
          drawerTab.value = "biz"
          return
        }
        if (form.value.assistGrantMode === "BOTH" && (assistCny <= 0 || assistUsdt <= 0)) {
          proxy.$modal.msgError("双币都送时请同时填写 CNY 与 USDT 助力值")
          drawerTab.value = "biz"
          return
        }
        if (form.value.assistGrantMode === "CNY") form.value.assistValueUsdt = 0
        if (form.value.assistGrantMode === "USDT") form.value.assistValueCny = 0
      } else {
        form.value.assistValueCny = 0
        form.value.assistValueUsdt = 0
      }
    }
    form.value.buyLimit = Number(form.value.buyLimit || 0)
    form.value.unlockDirectQty = Number(form.value.unlockDirectQty || 0)
    form.value.unlockDelayHours = Number(form.value.unlockDelayHours || 0)
    form.value.onSale = form.value.onSale === "1" || form.value.onSale === true ? "1" : "0"
    const req = form.value.productId ? updateProduct(form.value) : addProduct(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除产品编号为"' + row.productId + '"的数据项？').then(() => delProduct(row.productId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
loadCategories()
loadRelatedProducts()
loadTemplates()
getList()
loadCredit()
</script>

<style scoped>
.tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.product-drawer-form {
  padding: 0 2px 8px;
}
.publish-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
  align-items: center;
  margin: 0 0 16px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}
.publish-bar__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.publish-bar__label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}
.publish-bar__help {
  color: var(--el-text-color-secondary);
  cursor: help;
  font-size: 14px;
}
.product-tabs {
  margin-top: 4px;
}
.product-tabs :deep(.el-tabs__header) {
  margin-bottom: 14px;
}
.product-tabs :deep(.el-tab-pane) {
  padding-bottom: 4px;
}
.form-section-title {
  display: flex;
  align-items: center;
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.form-section-title.is-follow {
  margin-top: 18px;
}
.form-section-title::after {
  content: "";
  flex: 1;
  height: 1px;
  margin-left: 12px;
  background: var(--el-border-color-lighter);
}
.product-drawer-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.field-tip,
.section-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
.section-tip {
  margin: 0 0 14px;
}
.field-tip.inline {
  margin: 0 0 0 10px;
}
.switch-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  width: 100%;
}
.dual-input {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  width: 100%;
}
.dual-input__item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.dual-input__tag {
  flex: 0 0 40px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  text-align: right;
}
.dual-input__item :deep(.el-input-number) {
  flex: 1;
  width: 100%;
}
.assist-grant-mode {
  width: 100%;
}
.assist-grant-mode .field-tip,
.field-with-tip .field-tip {
  margin-top: 8px;
  margin-bottom: 0;
}
.field-with-tip {
  width: 100%;
}
.theme-color-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.product-ops {
  display: inline-flex;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
  gap: 0;
}
.product-ops :deep(.el-button) {
  margin-left: 0;
  padding: 4px 6px;
}
</style>
