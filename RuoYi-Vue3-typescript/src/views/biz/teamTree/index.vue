<template>
  <div class="app-container ops-page">
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
    >
      <template #default="{ node }">
        <span class="tree-node">
          <el-icon class="node-icon">
            <FolderOpened v-if="!node.isLeaf && node.expanded" />
            <Folder v-else-if="!node.isLeaf" />
            <Document v-else />
          </el-icon>
          <span class="node-label">{{ node.label }}</span>
        </span>
      </template>
    </el-tree>
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
.member-tree :deep(.el-tree-node__content) {
  height: 34px;
  border-radius: 4px;
}
.member-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #e6f0fd;
}
.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}
.tree-node .node-icon {
  font-size: 16px;
  color: #e6a23c;
  flex-shrink: 0;
}
.tree-node .node-label {
  line-height: 1.4;
}
</style>
