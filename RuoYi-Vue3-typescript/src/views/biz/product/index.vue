<template>
  <div class="app-container ops-page">
    <el-alert
      title="同一产品可同时配人民币和 USDT 价格。每人限购填累计可买份数，0 或不填表示不限制。一拖二：自己认购后，直属下级再认购同一产品达到设定份数，再等设定小时数才开始日返；两处填 0 表示关闭，认购即可出收益。"
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
      <el-table-column label="产品名称" align="center" prop="productName" min-width="120" />
      <el-table-column label="人民币价" align="center" prop="priceCny" width="100" />
      <el-table-column label="人民币日返" align="center" prop="dailyRebateCny" width="110" />
      <el-table-column label="USDT价" align="center" prop="priceUsdt" width="90" />
      <el-table-column label="USDT日返" align="center" prop="dailyRebateUsdt" width="100" />
      <el-table-column label="天数" align="center" prop="durationDays" width="70" />
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

    <el-drawer :title="title" v-model="open" size="620px" append-to-body destroy-on-close class="product-drawer">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="product-drawer-form">
        <div class="form-section-title">基本信息</div>
        <el-form-item label="所属系列" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择系列" style="width: 100%">
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="App 卡片主标题" />
        </el-form-item>
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="form.nameEn" placeholder="App 卡片副标题，可空" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上架状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">上架</el-radio>
                <el-radio value="1">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="开放认购" prop="onSale">
          <div class="switch-line">
            <el-switch v-model="form.onSale" active-value="1" inactive-value="0" />
            <span class="field-tip inline">关闭后点「立即参与」提示暂未开放，与上架/下架独立</span>
          </div>
        </el-form-item>
        <el-form-item label="封面">
          <image-upload v-model="form.coverUrl" :limit="1" />
        </el-form-item>

        <div class="form-section-title is-follow">价格与收益</div>
        <p class="section-tip">同一产品可同时配人民币和 USDT；至少填一种价格。返利天数必填。</p>
        <el-form-item label="认购价格">
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
        </el-form-item>
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

        <div class="form-section-title is-follow">认购规则</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="每人限购" prop="buyLimit">
              <el-input-number v-model="form.buyLimit" :min="0" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提现指定">
              <el-radio-group v-model="form.withdrawRequired">
                <el-radio value="1">是</el-radio>
                <el-radio value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <p class="section-tip">限购填 0 不限制，按会员已购该产品累计份数计；提现指定=是时，认购后才可提现。</p>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="一拖二份数" prop="unlockDirectQty">
              <el-input-number v-model="form.unlockDirectQty" :min="0" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等待小时" prop="unlockDelayHours">
              <el-input-number v-model="form.unlockDelayHours" :min="0" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <p class="section-tip">直属下级认购本产品累计份数达标后，再等设定小时开始日返。两处填 0 表示关闭，自己认购即可出收益。</p>

        <div class="form-section-title is-follow">App 展示</div>
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
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="App 认购页展示，如：直推下级认购2份相同价格产品"
          />
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

<script setup lang="ts" name="BizProduct">
import { listProduct, getProduct, addProduct, updateProduct, delProduct, listProductCategoryOptions, getWalletCreditByBiz, saveWalletCreditByBiz } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"

const { proxy } = getCurrentInstance() as any
const productList = ref<any[]>([])
const categoryOptions = ref<any[]>([])
const open = ref(false)
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
    durationDays: [{ required: true, message: "天数不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)
const creditLoading = ref(false)
const rebateWalletType = ref("PRODUCT")

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
    dailyRebateUsdt: undefined
  }
  proxy.resetForm("formRef")
}
function handleAdd() { reset(); open.value = true; title.value = "新增产品" }
function handleUpdate(row: any) {
  reset()
  getProduct(row.productId).then((res: any) => {
    form.value = res.data || {}
    if (form.value.onSale == null || form.value.onSale === "") {
      form.value.onSale = form.value.onSaleFlag === false ? "0" : "1"
    } else {
      form.value.onSale = form.value.onSale === "1" || form.value.onSale === 1 || form.value.onSale === true ? "1" : "0"
    }
    open.value = true
    title.value = "修改产品"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const cny = Number(form.value.priceCny || 0)
    const usdt = Number(form.value.priceUsdt || 0)
    if (cny <= 0 && usdt <= 0) {
      proxy.$modal.msgError("请至少配置人民币或USDT认购价格")
      return
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
  margin: -6px 0 14px;
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
