<template>
  <div class="app-container ops-page">
    <el-alert
      title="实名自领与推广奖励的入账记录。规则请在「注册推广规则」配置；三级认购返佣请看「推广佣金」。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="收款会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="收款手机" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="收款人手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="grantType">
        <el-select v-model="queryParams.grantType" placeholder="全部" clearable style="width: 160px">
          <el-option label="实名自领" value="KYC_SELF" />
          <el-option label="推广奖励" value="INVITE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList" style="width: 100%">
      <el-table-column label="ID" align="center" prop="grantId" width="80" />
      <el-table-column label="收款会员" align="center" min-width="160">
        <template #default="scope">{{ scope.row.memberId }} / {{ scope.row.phone || "—" }}</template>
      </el-table-column>
      <el-table-column label="类型" align="center" width="110">
        <template #default="scope">{{ scope.row.grantType === "INVITE" ? "推广奖励" : "实名自领" }}</template>
      </el-table-column>
      <el-table-column label="来源会员" align="center" min-width="160">
        <template #default="scope">{{ scope.row.fromMemberId || "—" }} / {{ scope.row.fromPhone || "—" }}</template>
      </el-table-column>
      <el-table-column label="币种" align="center" prop="currency" width="80" />
      <el-table-column label="金额" align="center" prop="amount" min-width="100" />
      <el-table-column label="时间" align="center" prop="createTime" min-width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizPromoGrant">
import { listPromoGrant } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const loading = ref(false)
const showSearch = ref(true)
const dataList = ref<any[]>([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 100,
  memberId: undefined as number | undefined,
  phone: undefined as string | undefined,
  grantType: undefined as string | undefined
})

function getList() {
  loading.value = true
  listPromoGrant(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

getList()
</script>
