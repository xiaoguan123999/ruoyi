<template>
  <div class="app-container ops-page">
    <el-form :inline="true" @submit.prevent="handleQuery">
      <el-form-item label="会员">
        <MemberSelect v-model="memberId" @change="onMemberChange" />
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
const memberId = ref<number | undefined>()
const loading = ref(false)
const rows = ref<any[]>([])

function formatAmount(v: any) {
  if (v == null || v === "") return "0.00"
  const n = Number(v)
  return Number.isNaN(n) ? v : n.toFixed(2)
}

function loadByKeyword(q: string) {
  loading.value = true
  listTeamRelation(q).then((res: any) => {
    rows.value = res.data || []
    if (!memberId.value) {
      for (const r of rows.value) {
        const hit = (r.peers || []).find((p: any) => p.current)
        if (hit?.memberId) {
          memberId.value = Number(hit.memberId)
          break
        }
        if (r.memberId) {
          memberId.value = Number(r.memberId)
          break
        }
      }
    }
  }).catch(() => {
    rows.value = []
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  if (!memberId.value) {
    proxy.$modal.msgWarning("请选择会员")
    return
  }
  loadByKeyword(String(memberId.value))
}

function onMemberChange(id?: number) {
  if (!id) {
    rows.value = []
    return
  }
  handleQuery()
}

function handleExport() {
  if (!memberId.value) {
    proxy.$modal.msgWarning("请先选择会员")
    return
  }
  proxy.download("biz/team/relation/export", { keyword: String(memberId.value) }, `relation_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  const q = String(route.query.keyword || "").trim()
  if (!q) return
  const asId = Number(q)
  if (Number.isFinite(asId) && String(asId) === q) {
    memberId.value = asId
    handleQuery()
    return
  }
  loadByKeyword(q)
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
