<template>
  <div class="app-container ops-page">
    <el-alert
      title="两网络独立找上级团队地址，没有则用系统地址；改团队地址只影响新单。超时未到账不自动补。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="商户单号" prop="outTradeNo">
        <el-input v-model="queryParams.outTradeNo" placeholder="单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="网络" prop="network">
        <el-select v-model="queryParams.network" placeholder="全部" clearable style="width: 140px">
          <el-option label="TRC20" value="TRC20" />
          <el-option label="BEP20" value="BEP20" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="待转账" value="0" />
          <el-option label="已入账" value="1" />
          <el-option label="已过期" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="warning" plain v-hasPermi="['biz:chainDeposit:query']" @click="handleScan">立刻扫链</el-button>
        <el-button type="primary" plain icon="Setting" v-hasPermi="['biz:chainDeposit:list']" @click="openConfig">网络配置</el-button>
      </el-form-item>
    </el-form>

    <div class="config-summary mb8" v-hasPermi="['biz:chainDeposit:list']">
      <el-tag :type="config.tronEnabled ? 'success' : 'info'" size="small">TRC20 {{ config.tronEnabled ? "开" : "关" }}</el-tag>
      <el-tag :type="config.bscEnabled ? 'success' : 'info'" size="small">BEP20 {{ config.bscEnabled ? "开" : "关" }}</el-tag>
      <el-tag :type="config.mock ? 'warning' : 'info'" size="small">模拟 {{ config.mock ? "开" : "关" }}</el-tag>
      <span class="summary-text">有效期 {{ config.expireMinutes || 30 }} 分钟</span>
    </div>

    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="商户单号" align="center" prop="outTradeNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="网络" align="center" prop="network" width="90">
        <template #default="scope">{{ scope.row.network || "—" }}</template>
      </el-table-column>
      <el-table-column label="会员" align="center" prop="phone" width="120" />
      <el-table-column label="下单金额" align="center" width="110" prop="amount" />
      <el-table-column label="应付指纹金额" align="center" min-width="140">
        <template #default="scope">{{ scope.row.payAmountText || scope.row.payAmount }}</template>
      </el-table-column>
      <el-table-column label="收款地址" align="center" prop="address" min-width="180" show-overflow-tooltip />
      <el-table-column label="地址来源" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.addressSource === 'TEAM'" type="warning">团队</el-tag>
          <el-tag v-else-if="scope.row.addressSource === 'SYSTEM'" type="info">系统</el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="String(scope.row.status) === '0'" type="warning">待转账</el-tag>
          <el-tag v-else-if="String(scope.row.status) === '1'" type="success">已入账</el-tag>
          <el-tag v-else type="info">已过期</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="txHash" align="center" prop="txHash" min-width="160" show-overflow-tooltip />
      <el-table-column label="过期时间" align="center" prop="expireTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.expireTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <el-button
            v-if="String(scope.row.status) === '0'"
            link
            type="primary"
            v-hasPermi="['biz:chainDeposit:simulate']"
            @click="handleSimulate(scope.row)"
          >模拟到账</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-drawer
      v-model="configOpen"
      title="网络配置"
      size="540px"
      append-to-body
      destroy-on-close
      class="chain-config-drawer"
      @open="loadConfig"
    >
      <div v-loading="configLoading" class="drawer-body">
        <el-form :model="config" :rules="configRules" label-width="88px" class="drawer-form">
          <div class="ops-block">
            <div class="ops-block__hd">订单规则（两网络共用）</div>
            <el-row :gutter="10">
              <el-col :span="8">
                <el-form-item label="有效期" prop="expireMinutes" label-width="64px">
                  <el-input-number v-model="config.expireMinutes" :min="5" :max="1440" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最低" prop="minAmount" label-width="48px">
                  <el-input-number v-model="config.minAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最高" prop="maxAmount" label-width="48px">
                  <el-input-number v-model="config.maxAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="模拟模式">
              <el-switch v-model="config.mock" active-text="开（不扫链，只能模拟到账）" inactive-text="关" />
            </el-form-item>
            <div class="ops-hint">有效期单位分钟，最少 5；最高填 0 表示不限金额</div>
          </div>

          <div class="ops-block">
            <div class="ops-block__hd">系统收款地址</div>
            <el-tabs v-model="configTab" type="border-card" class="net-tabs">
              <el-tab-pane label="TRC20" name="tron">
                <el-form-item label="启用">
                  <el-switch v-model="config.tronEnabled" />
                  <span class="ops-hint inline">关闭后 App 不展示该网络</span>
                </el-form-item>
                <el-form-item label="收款地址" prop="tronAddress" :required="config.tronEnabled">
                  <el-input v-model="config.tronAddress" placeholder="T 开头 34 位" clearable maxlength="34" />
                </el-form-item>
                <el-form-item label="扫链 Key" prop="tronApiKey" :required="needScanFields && config.tronEnabled">
                  <el-input
                    v-model="config.tronApiKey"
                    type="password"
                    show-password
                    :placeholder="needScanFields ? '必填' : '模拟模式下可不填'"
                    :disabled="!needScanFields"
                    clearable
                  />
                </el-form-item>
              </el-tab-pane>
              <el-tab-pane label="BEP20" name="bsc">
                <el-form-item label="启用">
                  <el-switch v-model="config.bscEnabled" />
                  <span class="ops-hint inline">关闭后 App 不展示该网络</span>
                </el-form-item>
                <el-form-item label="收款地址" prop="bscAddress" :required="config.bscEnabled">
                  <el-input v-model="config.bscAddress" placeholder="0x + 40 位" clearable maxlength="42" />
                </el-form-item>
                <el-form-item label="扫链 Key" prop="bscApiKey" :required="needScanFields && config.bscEnabled">
                  <el-input
                    v-model="config.bscApiKey"
                    type="password"
                    show-password
                    :placeholder="needScanFields ? '必填' : '模拟模式下可不填'"
                    :disabled="!needScanFields"
                    clearable
                  />
                </el-form-item>
                <el-form-item label="扫链 URL" prop="bscApiUrl" :required="needScanFields && config.bscEnabled">
                  <el-input
                    v-model="config.bscApiUrl"
                    :placeholder="needScanFields ? '必填' : '模拟模式下可不填'"
                    :disabled="!needScanFields"
                    clearable
                  />
                </el-form-item>
              </el-tab-pane>
            </el-tabs>
          </div>

          <div class="ops-block">
            <div class="ops-block__hd">App 页底文案</div>
            <el-form-item prop="hint" label-width="0">
              <el-input v-model="config.hint" type="textarea" :rows="3" placeholder="展示在 App 充值页底部" maxlength="500" show-word-limit />
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="configOpen = false">取 消</el-button>
          <el-button @click="loadConfig">刷 新</el-button>
          <el-button type="primary" :loading="configSaving" @click="saveConfig">保存配置</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="BizChainDeposit">
import {
  listChainDeposit,
  getChainDepositConfig,
  saveChainDepositConfig,
  scanChainDeposit,
  simulateChainDeposit
} from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const configOpen = ref(false)
const configTab = ref("tron")
const configLoading = ref(false)
const configSaving = ref(false)
const config = ref(emptyConfig())
const queryParams = ref({
  pageNum: 1,
  pageSize: 100,
  outTradeNo: undefined as string | undefined,
  phone: undefined as string | undefined,
  network: undefined as string | undefined,
  status: undefined as string | undefined
})

const TRON_RE = /^T[1-9A-HJ-NP-Za-km-z]{33}$/
const BEP20_RE = /^0x[a-fA-F0-9]{40}$/

/** 非模拟才真正扫链，扫链 Key/URL 才必填 */
const needScanFields = computed(() => !config.value.mock)

const configRules = {
  tronAddress: [{
    validator: (_: any, value: any, callback: (e?: Error) => void) => {
      const v = String(value ?? "").trim()
      if (!config.value.tronEnabled) return callback()
      if (!v) return callback(new Error("请填写 TRC20 收款地址"))
      if (!TRON_RE.test(v)) return callback(new Error("TRC20 地址须为 T 开头共 34 位"))
      callback()
    },
    trigger: "blur"
  }],
  tronApiKey: [{
    validator: (_: any, value: any, callback: (e?: Error) => void) => {
      if (!needScanFields.value || !config.value.tronEnabled) return callback()
      if (!String(value ?? "").trim()) return callback(new Error("请填写 TRC20 扫链 Key"))
      callback()
    },
    trigger: "blur"
  }],
  bscAddress: [{
    validator: (_: any, value: any, callback: (e?: Error) => void) => {
      const v = String(value ?? "").trim()
      if (!config.value.bscEnabled) return callback()
      if (!v) return callback(new Error("请填写 BEP20 收款地址"))
      if (!BEP20_RE.test(v)) return callback(new Error("BEP20 地址须为 0x + 40 位十六进制"))
      callback()
    },
    trigger: "blur"
  }],
  bscApiKey: [{
    validator: (_: any, value: any, callback: (e?: Error) => void) => {
      if (!needScanFields.value || !config.value.bscEnabled) return callback()
      if (!String(value ?? "").trim()) return callback(new Error("请填写 BEP20 扫链 Key"))
      callback()
    },
    trigger: "blur"
  }],
  bscApiUrl: [{
    validator: (_: any, value: any, callback: (e?: Error) => void) => {
      if (!needScanFields.value || !config.value.bscEnabled) return callback()
      if (!String(value ?? "").trim()) return callback(new Error("请填写 BEP20 扫链 URL"))
      callback()
    },
    trigger: "blur"
  }],
  expireMinutes: [{
    validator: (_: any, value: any, callback: (e?: Error) => void) => {
      const n = Number(value)
      if (!Number.isFinite(n) || n < 5) return callback(new Error("订单有效期最少 5 分钟"))
      callback()
    },
    trigger: "change"
  }]
}

function emptyConfig() {
  return {
    tronEnabled: true,
    tronAddress: "",
    tronApiKey: "",
    bscEnabled: true,
    bscAddress: "",
    bscApiKey: "",
    bscApiUrl: "",
    expireMinutes: 30,
    minAmount: 10,
    maxAmount: 100000,
    mock: false,
    hint: ""
  }
}

function normalizeConfig(data: any) {
  if (!data || typeof data !== "object") return emptyConfig()
  return {
    tronEnabled: data.tronEnabled !== false,
    tronAddress: data.tronAddress == null ? "" : String(data.tronAddress).trim(),
    tronApiKey: data.tronApiKey == null ? "" : String(data.tronApiKey),
    bscEnabled: data.bscEnabled !== false,
    bscAddress: data.bscAddress == null ? "" : String(data.bscAddress).trim(),
    bscApiKey: data.bscApiKey == null ? "" : String(data.bscApiKey),
    bscApiUrl: data.bscApiUrl == null ? "" : String(data.bscApiUrl).trim(),
    expireMinutes: Number(data.expireMinutes) > 0 ? Number(data.expireMinutes) : 30,
    minAmount: data.minAmount == null || data.minAmount === "" ? 0 : Number(data.minAmount),
    maxAmount: data.maxAmount == null || data.maxAmount === "" ? 0 : Number(data.maxAmount),
    mock: data.mock === true || data.mock === "true" || data.mock === 1 || data.mock === "1",
    hint: data.hint == null ? "" : String(data.hint)
  }
}

function buildConfigPayload() {
  const c = config.value
  return {
    tronEnabled: !!c.tronEnabled,
    tronAddress: String(c.tronAddress || "").trim(),
    tronApiKey: String(c.tronApiKey || ""),
    bscEnabled: !!c.bscEnabled,
    bscAddress: String(c.bscAddress || "").trim(),
    bscApiKey: String(c.bscApiKey || ""),
    bscApiUrl: String(c.bscApiUrl || "").trim(),
    expireMinutes: Math.max(5, Number(c.expireMinutes) || 30),
    minAmount: Number(c.minAmount) || 0,
    maxAmount: Number(c.maxAmount) || 0,
    mock: !!c.mock,
    hint: String(c.hint || "")
  }
}

function openConfig() {
  configTab.value = "tron"
  configOpen.value = true
}

function loadConfig() {
  configLoading.value = true
  getChainDepositConfig().then((res: any) => {
    config.value = normalizeConfig(res.data)
  }).catch(() => {
    config.value = emptyConfig()
  }).finally(() => {
    configLoading.value = false
  })
}

function saveConfig() {
  const c = config.value
  const tron = String(c.tronAddress || "").trim()
  const tronKey = String(c.tronApiKey || "").trim()
  const bsc = String(c.bscAddress || "").trim()
  const bscKey = String(c.bscApiKey || "").trim()
  const bscUrl = String(c.bscApiUrl || "").trim()

  if (c.tronEnabled) {
    configTab.value = "tron"
    if (!tron) {
      proxy.$modal.msgWarning("TRC20 已启用，请填写收款地址")
      return
    }
    if (!TRON_RE.test(tron)) {
      proxy.$modal.msgWarning("TRC20 地址须为 T 开头共 34 位")
      return
    }
    if (!c.mock && !tronKey) {
      proxy.$modal.msgWarning("正式扫链需填写 TRC20 扫链 Key（或先开启模拟模式）")
      return
    }
  } else if (tron && !TRON_RE.test(tron)) {
    configTab.value = "tron"
    proxy.$modal.msgWarning("TRC20 地址须为 T 开头共 34 位")
    return
  }

  if (c.bscEnabled) {
    configTab.value = "bsc"
    if (!bsc) {
      proxy.$modal.msgWarning("BEP20 已启用，请填写收款地址")
      return
    }
    if (!BEP20_RE.test(bsc)) {
      proxy.$modal.msgWarning("BEP20 地址须为 0x + 40 位十六进制")
      return
    }
    if (!c.mock && !bscKey) {
      proxy.$modal.msgWarning("正式扫链需填写 BEP20 扫链 Key（或先开启模拟模式）")
      return
    }
    if (!c.mock && !bscUrl) {
      proxy.$modal.msgWarning("正式扫链需填写 BEP20 扫链 URL（或先开启模拟模式）")
      return
    }
  } else if (bsc && !BEP20_RE.test(bsc)) {
    configTab.value = "bsc"
    proxy.$modal.msgWarning("BEP20 地址须为 0x + 40 位十六进制")
    return
  }

  if (!Number.isFinite(Number(c.expireMinutes)) || Number(c.expireMinutes) < 5) {
    proxy.$modal.msgWarning("订单有效期最少 5 分钟")
    return
  }

  configSaving.value = true
  saveChainDepositConfig(buildConfigPayload()).then(() => {
    proxy.$modal.msgSuccess("网络配置已保存")
    loadConfig()
    configOpen.value = false
  }).finally(() => {
    configSaving.value = false
  })
}

function getList() {
  loading.value = true
  listChainDeposit(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => {
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

function handleScan() {
  if (config.value.mock) {
    proxy.$modal.msgWarning("当前为模拟模式，不会扫链；请用「模拟到账」")
    return
  }
  proxy.$modal.confirm("立刻扫链补单？").then(() => scanChainDeposit()).then(() => {
    proxy.$modal.msgSuccess("已触发扫链")
    getList()
  }).catch(() => {})
}

function handleSimulate(row: any) {
  if (!config.value.mock) {
    proxy.$modal.msgWarning("模拟到账仅在「模拟模式」开启时可用，请先打开网络配置")
    return
  }
  proxy.$modal.confirm("确认模拟到账？").then(() => simulateChainDeposit(row.outTradeNo)).then(() => {
    proxy.$modal.msgSuccess("已到账")
    getList()
  }).catch(() => {})
}

loadConfig()
getList()
</script>

<style scoped>
.config-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.summary-text {
  color: #909399;
  font-size: 13px;
}
.drawer-body {
  padding-bottom: 8px;
}
.drawer-form :deep(.el-form-item) {
  margin-bottom: 12px;
}
.ops-block + .ops-block {
  margin-top: 16px;
}
.ops-block__hd {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
}
.ops-hint {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
  margin: -2px 0 8px 88px;
}
.ops-hint.inline {
  margin: 0 0 0 10px;
}
.net-tabs {
  box-shadow: none;
}
.net-tabs :deep(.el-tabs__content) {
  padding: 12px 12px 4px;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
