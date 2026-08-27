<template>
  <div class="app-container ops-page">
    <el-alert
      title="同一产品可同时配人民币和 USDT 价格。每人限购填累计可买份数，0 或不填表示不限制。"
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
      <el-table-column label="提现指定" align="center" prop="withdrawRequired" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.withdrawRequired === '1' ? 'warning' : 'info'">{{ scope.row.withdrawRequired === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:product:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:product:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="所属系列" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择系列" style="width: 100%">
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="英文名" prop="nameEn">
          <el-input v-model="form.nameEn" placeholder="App 卡片副标题，可空" />
        </el-form-item>
        <el-form-item label="人民币价格" prop="priceCny">
          <el-input-number v-model="form.priceCny" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="人民币日返" prop="dailyRebateCny">
          <el-input-number v-model="form.dailyRebateCny" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="USDT价格" prop="priceUsdt">
          <el-input-number v-model="form.priceUsdt" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="USDT日返" prop="dailyRebateUsdt">
          <el-input-number v-model="form.dailyRebateUsdt" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="返利天数" prop="durationDays">
          <el-input-number v-model="form.durationDays" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每人限购" prop="buyLimit">
          <el-input-number v-model="form.buyLimit" :min="0" :step="1" style="width: 100%" />
          <div class="el-form-item-msg" style="color:#909399;font-size:12px;line-height:1.4">0 或不填表示不限制。按该会员已购该产品的累计份数计算。</div>
        </el-form-item>
        <el-form-item label="收益发放方式" prop="payoutMethod">
          <el-input v-model="form.payoutMethod" maxlength="100" placeholder="仅 App 展示，例如：每日发放" />
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-input v-model="form.riskLevel" maxlength="64" placeholder="仅 App 展示，例如：中" />
        </el-form-item>
        <el-form-item label="提现指定产品" prop="withdrawRequired">
          <el-radio-group v-model="form.withdrawRequired">
            <el-radio value="1">是</el-radio>
            <el-radio value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">上架</el-radio>
            <el-radio value="1">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="封面">
          <image-upload v-model="form.coverUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
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
  queryParams: { pageNum: 1, pageSize: 100, productName: undefined, currency: undefined, status: undefined, categoryId: undefined },
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
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = {
    status: "0",
    withdrawRequired: "0",
    buyLimit: 0,
    payoutMethod: "",
    riskLevel: "",
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
    form.value = res.data
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
.tip { margin-left: 12px; color: #909399; font-size: 13px; }
</style>
