<template>
  <div class="app-container">
    <el-alert
      title="这里记的是 App 会员登录、注册、退出。系统监控里的登录日志只给后台管理员用。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员ID" prop="memberId">
        <el-input v-model="queryParams.memberId" placeholder="会员ID" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="登录地址" prop="ipaddr">
        <el-input v-model="queryParams.ipaddr" placeholder="IP" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="登录状态" clearable style="width: 140px">
          <el-option
            v-for="dict in sys_common_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="登录时间" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:memberLogin:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" @click="handleClean" v-hasPermi="['biz:memberLogin:remove']">清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['biz:memberLogin:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="访问编号" align="center" prop="infoId" width="90" />
      <el-table-column label="会员ID" align="center" prop="memberId" width="90" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="地址" align="center" prop="ipaddr" min-width="130" show-overflow-tooltip />
      <el-table-column label="登录地点" align="center" prop="loginLocation" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作系统" align="center" prop="os" min-width="120" show-overflow-tooltip />
      <el-table-column label="浏览器" align="center" prop="browser" min-width="110" show-overflow-tooltip />
      <el-table-column label="登录状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="sys_common_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="描述" align="center" prop="msg" min-width="140" show-overflow-tooltip />
      <el-table-column label="访问时间" align="center" prop="loginTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="BizMemberLogin">
import { listMemberLogin, delMemberLogin, cleanMemberLogin } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const { sys_common_status } = useDict("sys_common_status")

const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<number[]>([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref<string[]>([])
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  memberId: undefined as any,
  phone: undefined as any,
  ipaddr: undefined as any,
  status: undefined as any
})

function getList() {
  loading.value = true
  listMemberLogin(proxy.addDateRange(queryParams.value, dateRange.value)).then((res: any) => {
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
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(item => item.infoId)
  multiple.value = !selection.length
}
function handleDelete() {
  proxy.$modal.confirm('是否确认删除访问编号为"' + ids.value + '"的数据项?').then(() => delMemberLogin(ids.value)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有会员登录日志?").then(() => cleanMemberLogin()).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}
function handleExport() {
  proxy.download("biz/memberLogin/export", {
    ...queryParams.value,
  }, `member_login_${new Date().getTime()}.xlsx`)
}
getList()
</script>
