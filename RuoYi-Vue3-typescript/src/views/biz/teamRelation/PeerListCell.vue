<template>
  <div class="peer-cell">
    <div
      ref="listRef"
      class="peer-list"
      :class="{ 'is-collapsed': !expanded }"
      v-html="peerHtml"
    />
    <el-button
      v-if="overflow"
      class="peer-toggle"
      link
      type="primary"
      @click="expanded = !expanded"
    >{{ expanded ? "收起" : "展开" }}</el-button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  peers?: Array<{ memberId?: number | string; phone?: string; current?: boolean }>
}>()

const listRef = ref<HTMLElement>()
const expanded = ref(false)
const overflow = ref(false)
let observer: ResizeObserver | undefined

function escapeHtml(s: string) {
  return s
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
}

const peerHtml = computed(() => {
  const list = [...(props.peers || [])]
  // 路径当前会员靠前，折叠时更容易看到
  list.sort((a, b) => Number(!!b.current) - Number(!!a.current))
  return list
    .map((p) => {
      const text = escapeHtml(`${p.memberId ?? ""}:${p.phone ?? ""}`)
      return p.current ? `<span class="is-path">${text}</span>` : text
    })
    .join(" ")
})

function measure() {
  const el = listRef.value
  if (!el) return
  // 展开时仍显示「收起」；折叠时用单行 clamp 判断是否真正溢出
  if (expanded.value) {
    overflow.value = true
    return
  }
  overflow.value = el.scrollHeight > el.clientHeight + 1
}

watch(peerHtml, async () => {
  expanded.value = false
  await nextTick()
  measure()
})

watch(expanded, async () => {
  await nextTick()
  measure()
})

onMounted(() => {
  measure()
  if (typeof ResizeObserver !== "undefined" && listRef.value) {
    observer = new ResizeObserver(() => measure())
    observer.observe(listRef.value)
  }
})

onBeforeUnmount(() => {
  observer?.disconnect()
})
</script>

<style scoped>
.peer-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  text-align: left;
  line-height: 1.5;
  width: 100%;
}
.peer-list {
  flex: 1;
  min-width: 0;
  white-space: normal;
  word-break: break-all;
  line-height: 1.6;
}
.peer-list.is-collapsed {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  overflow: hidden;
}
.peer-toggle {
  flex-shrink: 0;
  height: auto;
  padding: 0;
  line-height: 1.6;
}
.peer-list :deep(.is-path) {
  color: #f56c6c;
  font-weight: 600;
}
</style>
