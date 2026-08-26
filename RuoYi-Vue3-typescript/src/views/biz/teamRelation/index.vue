<template>
  <div class="app-container">
    <el-form :inline="true" @submit.prevent="handleQuery">
      <el-form-item>
        <el-input
          v-model="keyword"
          placeholder="手机号 / 会员ID / 邀请码"
          clearable
          style="width: 280px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button type="success" icon="Download" @click="handleExport" v-hasPermi="['biz:team:export']">导出</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="用户ID" align="center" prop="memberId" width="100" />
      <el-table-column label="层级" align="center" prop="teamLevel" width="80" />
      <el-table-column label="余额" align="center" prop="balance" width="120">
        <template #default="scope">{{ formatAmount(scope.row.balance) }}</template>
      </el-table-column>
      <el-table-column label="签到天数" align="center" prop="checkinDays" width="100" />
      <el-table-column label="账号" align="center" prop="account" width="140" />
      <el-table-column label="列表" min-width="360">
        <template #default="scope">
          <span class="peer-list">
            <span
              v-for="(p, i) in (scope.row.peers || [])"
              :key="p.memberId"
              :class="{ 'is-path': p.current }"
            >{{ p.memberId }}:{{ p.phone }}{{ i < scope.row.peers.length - 1 ? " " : "" }}</span>
          </span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts" name="BizTeamRelation">
import { listTeamRelation } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const route = useRoute()
const keyword = ref("")
const loading = ref(false)
const rows = ref<any[]>([])

function formatAmount(v: any) {
  if (v == null || v === "") return "0.00"
  const n = Number(v)
  return Number.isNaN(n) ? v : n.toFixed(2)
}

function handleQuery() {
  const q = (keyword.value || "").trim()
  if (!q) {
    proxy.$modal.msgWarning("请输入手机号或会员ID")
    return
  }
  loading.value = true
  listTeamRelation(q).then((res: any) => {
    rows.value = res.data || []
  }).catch(() => {
    rows.value = []
  }).finally(() => {
    loading.value = false
  })
}

function handleExport() {
  const q = (keyword.value || "").trim()
  if (!q) {
    proxy.$modal.msgWarning("请先搜索会员")
    return
  }
  proxy.download("biz/team/relation/export", { keyword: q }, `relation_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  const q = (route.query.keyword as string) || ""
  if (q) {
    keyword.value = q
    handleQuery()
  }
})
</script>

<style scoped>
.peer-list {
  display: block;
  white-space: normal;
  word-break: break-all;
  line-height: 1.6;
}
.is-path {
  color: #f56c6c;
  font-weight: 600;
}
</style>
