<template>
  <div class="app-container ops-page">
    <el-alert
      title="独立奖品池：维护名称、说明、图片与转盘扇区配色。抽奖活动「配置奖品」时从本池选择，再设置库存/概率/排序。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="prizeName">
        <el-input v-model="queryParams.prizeName" placeholder="奖品名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="prizeType">
        <el-select v-model="queryParams.prizeType" placeholder="类型" clearable style="width: 140px">
          <el-option label="实物" :value="1" />
          <el-option label="现金" :value="2" />
          <el-option label="虚拟" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:lotteryPrize:edit']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="poolId" width="70" />
      <el-table-column label="图片" align="center" width="80">
        <template #default="scope">
          <image-preview v-if="scope.row.imageUrl" :src="scope.row.imageUrl" :width="40" :height="40" />
          <span v-else style="color:#909399">—</span>
        </template>
      </el-table-column>
      <el-table-column label="奖品名称" align="left" prop="prizeName" min-width="120" show-overflow-tooltip />
      <el-table-column label="说明" align="left" prop="prizeDesc" min-width="140">
        <template #default="scope">
          <div class="prize-desc-cell">{{ scope.row.prizeDesc || "—" }}</div>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" width="80">
        <template #default="scope">{{ prizeTypeLabel(scope.row.prizeType) }}</template>
      </el-table-column>
      <el-table-column label="扇区色" align="center" width="100">
        <template #default="scope">
          <span class="color-dot" :style="{ background: scope.row.sectorBgColor || '#FFF8EC' }"></span>
          {{ scope.row.sectorBgColor || '—' }}
        </template>
      </el-table-column>
      <el-table-column label="虚拟配置" align="left" min-width="140">
        <template #default="scope">
          <span v-if="scope.row.prizeType === 3">助力值 {{ scope.row.assistAmount ?? 0 }} {{ scope.row.currency || "CNY" }}</span>
          <span v-else style="color: #909399">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:lotteryPrize:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:lotteryPrize:edit']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="奖品名称" prop="prizeName">
          <el-input v-model="form.prizeName" placeholder="如：华为手机" maxlength="64" />
        </el-form-item>
        <el-form-item label="奖品说明" prop="prizeDesc">
          <el-input
            v-model="form.prizeDesc"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="支持换行，如：&#10;价值10000元&#10;联系客服领取"
          />
          <div class="field-tip">多行说明会在 App 转盘扇区按行展示</div>
        </el-form-item>
        <el-form-item label="奖品图片">
          <image-upload v-model="form.imageUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="类型" prop="prizeType">
          <el-select v-model="form.prizeType" style="width: 180px" @change="onPrizeTypeChange">
            <el-option label="实物" :value="1" />
            <el-option label="现金" :value="2" />
            <el-option label="虚拟" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="扇区背景色" prop="sectorBgColor">
          <el-color-picker v-model="form.sectorBgColor" color-format="hex" />
          <el-input v-model="form.sectorBgColor" style="width: 140px; margin-left: 8px" placeholder="#FFF8EC" />
          <div class="field-tip">转盘该奖品扇区底色</div>
        </el-form-item>
        <el-form-item label="名称字体色" prop="nameColor">
          <el-color-picker v-model="form.nameColor" color-format="hex" />
          <el-input v-model="form.nameColor" style="width: 140px; margin-left: 8px" placeholder="#111827" />
          <div class="field-tip">奖品名称文字颜色（如「一等奖」）</div>
        </el-form-item>
        <el-form-item label="说明字体色" prop="descColor">
          <el-color-picker v-model="form.descColor" color-format="hex" />
          <el-input v-model="form.descColor" style="width: 140px; margin-left: 8px" placeholder="#374151" />
          <div class="field-tip">奖品说明文字颜色；保存后会同步到活动奖项</div>
        </el-form-item>
        <template v-if="form.prizeType === 3">
          <el-form-item label="助力值" prop="assistAmount">
            <el-input-number v-model="form.assistAmount" :min="0" :precision="2" controls-position="right" style="width: 180px" />
          </el-form-item>
          <el-form-item label="币种" prop="currency">
            <el-select v-model="form.currency" style="width: 180px">
              <el-option label="CNY" value="CNY" />
              <el-option label="USDT" value="USDT" />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizLotteryPrize">
import {
  listLotteryPrizePool,
  getLotteryPrizePool,
  addLotteryPrizePool,
  updateLotteryPrizePool,
  delLotteryPrizePool
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")

const data = reactive({
  form: {} as any,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    prizeName: undefined as string | undefined,
    prizeType: undefined as number | undefined,
    status: undefined as string | undefined
  },
  rules: {
    prizeName: [{ required: true, message: "请填写奖品名称", trigger: "blur" }],
    prizeType: [{ required: true, message: "请选择类型", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function prizeTypeLabel(type: number) {
  if (type === 1) return "实物"
  if (type === 2) return "现金"
  if (type === 3) return "虚拟"
  return "-"
}

function getList() {
  loading.value = true
  listLotteryPrizePool(queryParams.value).then((res: any) => {
    dataList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function reset() {
  form.value = {
    poolId: undefined,
    prizeName: "",
    prizeDesc: "",
    imageUrl: "",
    sectorBgColor: "#FFF8EC",
    nameColor: "#111827",
    descColor: "#374151",
    prizeType: 3,
    assistAmount: 1,
    currency: "CNY",
    sort: 0,
    status: "0",
    remark: ""
  }
}

function onPrizeTypeChange() {
  if (form.value.prizeType !== 3) {
    form.value.assistAmount = undefined
    form.value.currency = undefined
  } else {
    if (form.value.assistAmount == null) form.value.assistAmount = 1
    if (!form.value.currency) form.value.currency = "CNY"
  }
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增奖品"
}

function handleUpdate(row: any) {
  getLotteryPrizePool(row.poolId).then((res: any) => {
    form.value = {
      sectorBgColor: "#FFF8EC",
      nameColor: "#111827",
      descColor: "#374151",
      ...res.data
    }
    open.value = true
    title.value = "修改奖品"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const payload = { ...form.value }
    if (payload.prizeType !== 3) {
      payload.assistAmount = null
      payload.currency = null
    }
    const req = payload.poolId ? updateLotteryPrizePool(payload) : addLotteryPrizePool(payload)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row: any) {
  proxy.$modal
    .confirm('是否确认删除奖品「' + row.prizeName + "」？")
    .then(() => delLotteryPrizePool(row.poolId))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess("删除成功")
    })
    .catch(() => {})
}

getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 6px;
  vertical-align: middle;
  border: 1px solid var(--el-border-color);
}
.prize-desc-cell {
  white-space: pre-line;
  line-height: 1.4;
  word-break: break-word;
}
.field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
