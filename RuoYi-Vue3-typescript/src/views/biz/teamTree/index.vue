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
      </el-form-item>
    </el-form>
    <el-empty v-if="!root && !loading" description="输入手机号或会员ID，查看该会员及其下级结构" />
    <el-tree
      v-if="root"
      ref="treeRef"
      class="member-tree"
      :key="root.memberId"
      lazy
      highlight-current
      node-key="memberId"
      :load="loadNode"
      :props="treeProps"
      :expand-on-click-node="false"
    />
  </div>
</template>

<script setup lang="ts" name="BizTeamTree">
import { getTeamTree, listTeamChildren } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const route = useRoute()
const keyword = ref("")
const loading = ref(false)
const root = ref<any>(null)
const treeProps = { label: "label", isLeaf: "leaf", children: "children" }

function handleQuery() {
  const q = (keyword.value || "").trim()
  if (!q) {
    proxy.$modal.msgWarning("请输入手机号或会员ID")
    return
  }
  loading.value = true
  getTeamTree(q).then((res: any) => {
    root.value = res.data
  }).catch(() => {
    root.value = null
  }).finally(() => {
    loading.value = false
  })
}

function loadNode(node: any, resolve: (data: any[]) => void) {
  if (node.level === 0) {
    resolve(root.value ? [root.value] : [])
    return
  }
  listTeamChildren(node.data.memberId).then((res: any) => {
    resolve(res.data || [])
  }).catch(() => resolve([]))
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
.member-tree {
  max-width: 720px;
  padding: 8px 4px 24px;
}
.member-tree :deep(.el-tree-node__label) {
  font-size: 14px;
}
</style>
