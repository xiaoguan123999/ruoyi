<template>
  <div class="app-container">
    <el-alert
      title="客服发放页：领航/星域每月达标后会出现待发放记录；星链永久资格达标后会出现首笔待发放，之后可用「额外发放」。自动档不会出现在待发放里。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="待发放" value="0" />
          <el-option label="已发放" value="1" />
          <el-option label="已拒绝" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="周期" prop="grantCycle">
        <el-select v-model="queryParams.grantCycle" placeholder="周期" clearable style="width: 140px">
          <el-option label="一次" value="ONCE" />
          <el-option label="每月" value="MONTHLY" />
          <el-option label="永久" value="PERMANENT" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="openExtra" v-hasPermi="['biz:levelReward:pay']">星链额外发放</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="grantId" width="80" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="等级" align="center" prop="levelName" width="100" />
      <el-table-column label="周期键" align="center" prop="cycleKey" width="140" />
      <el-table-column label="周期" align="center" prop="grantCycle" width="90" />
      <el-table-column label="方式" align="center" width="80">
        <template #default="scope">{{ scope.row.grantMode === 'MANUAL' ? '客服' : '自动' }}</template>
      </el-table-column>
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="金额" align="center" prop="amount" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="warning">待发放</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已发放</el-tag>
          <el-tag v-else type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" align="center" prop="createTime" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="payBy" width="100" />
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="primary" @click="openPay(scope.row)" v-hasPermi="['biz:levelReward:pay']">确认发放</el-button>
            <el-button link type="danger" @click="openReject(scope.row)" v-hasPermi="['biz:levelReward:reject']">拒绝</el-button>
          </template>
          <span v-else>{{ scope.row.remark || '—' }}</span>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="payTitle" v-model="payOpen" width="480px" append-to-body>
      <el-form ref="payRef" :model="payForm" label-width="90px">
        <el-form-item label="备注">
          <el-input v-model="payForm.remark" type="textarea" :rows="3" :placeholder="payForm.reject ? '请填写拒绝原因' : '可选'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPay">确 定</el-button>
        <el-button @click="payOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="星链额外发放" v-model="extraOpen" width="480px" append-to-body>
      <el-form ref="extraRef" :model="extraForm" :rules="extraRules" label-width="90px">
        <el-form-item label="会员ID" prop="memberId">
          <el-input-number v-model="extraForm.memberId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="等级ID" prop="levelId">
          <el-input-number v-model="extraForm.levelId" :min="1" style="width: 100%" />
          <div class="tip">请填已启用的永久档等级ID，例如星链</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="extraForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitExtra">确 定</el-button>
        <el-button @click="extraOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizLevelRewardGrant">
import { listLevelRewardGrant, payLevelRewardGrant, rejectLevelRewardGrant, extraPayLevelReward } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const payOpen = ref(false)
const extraOpen = ref(false)
const payTitle = ref("")
const queryParams = ref({ pageNum: 1, pageSize: 10, phone: undefined as string | undefined, status: "0", grantCycle: undefined as string | undefined })
const payForm = ref({ grantId: 0, remark: "", reject: false })
const extraForm = ref({ memberId: undefined as number | undefined, levelId: undefined as number | undefined, remark: "" })
const extraRules = {
  memberId: [{ required: true, message: "请填写会员ID", trigger: "blur" }],
  levelId: [{ required: true, message: "请填写等级ID", trigger: "blur" }]
}

function getList() {
  loading.value = true
  listLevelRewardGrant(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); queryParams.value.status = "0"; handleQuery() }
function openPay(row: any) {
  payForm.value = { grantId: row.grantId, remark: "", reject: false }
  payTitle.value = "确认发放 " + (row.amount || "") + " " + (row.currency || "")
  payOpen.value = true
}
function openReject(row: any) {
  payForm.value = { grantId: row.grantId, remark: "", reject: true }
  payTitle.value = "拒绝发放"
  payOpen.value = true
}
function submitPay() {
  const req = payForm.value.reject
    ? rejectLevelRewardGrant(payForm.value.grantId, { remark: payForm.value.remark })
    : payLevelRewardGrant(payForm.value.grantId, { remark: payForm.value.remark })
  req.then(() => {
    proxy.$modal.msgSuccess("操作成功")
    payOpen.value = false
    getList()
  })
}
function openExtra() {
  extraForm.value = { memberId: undefined, levelId: undefined, remark: "" }
  extraOpen.value = true
}
function submitExtra() {
  proxy.$refs["extraRef"].validate((valid: boolean) => {
    if (!valid) return
    extraPayLevelReward(extraForm.value).then(() => {
      proxy.$modal.msgSuccess("发放成功")
      extraOpen.value = false
      getList()
    })
  })
}
getList()
</script>

<style scoped>
.tip { color: #909399; font-size: 12px; line-height: 1.4; }
</style>
