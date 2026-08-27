<template>
  <div class="app-container ops-page">
    <el-alert
      title="黑名单命中后会写一条记录。登录/注册看手机号，实名看身份证，绑卡看银行卡。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="动作" prop="action">
        <el-select v-model="queryParams.action" placeholder="动作" clearable style="width: 140px">
          <el-option label="登录" value="LOGIN" />
          <el-option label="注册" value="REGISTER" />
          <el-option label="实名" value="KYC" />
          <el-option label="绑卡" value="BANK" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="命中值" prop="hitValue">
        <el-input v-model="queryParams.hitValue" placeholder="手机/身份证/卡号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="logId" width="70" />
      <el-table-column label="动作" align="center" prop="action" width="90">
        <template #default="scope">{{ actionText(scope.row.action) }}</template>
      </el-table-column>
      <el-table-column label="命中类型" align="center" prop="hitType" width="110">
        <template #default="scope">{{ hitText(scope.row.hitType) }}</template>
      </el-table-column>
      <el-table-column label="命中值" align="center" prop="hitValue" min-width="180" show-overflow-tooltip />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="姓名" align="center" prop="realName" width="100" />
      <el-table-column label="时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:blacklistLog:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizBlacklistLog">
import { listBlacklistLog, delBlacklistLog } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 100, action: undefined, phone: undefined, hitValue: undefined })

function actionText(v: string) {
  if (v === "LOGIN") return "登录"
  if (v === "REGISTER") return "注册"
  if (v === "KYC") return "实名"
  if (v === "BANK") return "绑卡"
  return v || "-"
}
function hitText(v: string) {
  if (v === "PHONE") return "手机号"
  if (v === "ID_CARD") return "身份证"
  if (v === "BANK_CARD") return "银行卡"
  return v || "-"
}
function getList() {
  loading.value = true
  listBlacklistLog(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleDelete(row: any) {
  proxy.$modal.confirm("是否确认删除该记录？").then(() => delBlacklistLog(row.logId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
