<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    :filterable="true"
    :remote="true"
    :remote-method="onRemoteSearch"
    :loading="loading"
    :style="styleObj"
    popper-class="member-select-dropdown"
    @update:model-value="onUpdate"
    @visible-change="onVisibleChange"
    @clear="onClear"
  >
    <el-option
      v-for="item in options"
      :key="item.memberId"
      :label="formatLabel(item)"
      :value="item.memberId"
    >
      <span class="member-select-option">{{ formatLabel(item) }}</span>
    </el-option>
    <el-option v-if="loadingMore" disabled :value="LOADING_VALUE">
      <span class="member-select-tip">加载中…</span>
    </el-option>
    <el-option v-else-if="!loading && !options.length" disabled :value="EMPTY_VALUE">
      <span class="member-select-tip">暂无会员</span>
    </el-option>
    <el-option v-else-if="!hasMore && options.length" disabled :value="END_VALUE">
      <span class="member-select-tip">没有更多了</span>
    </el-option>
  </el-select>
</template>

<script setup lang="ts" name="MemberSelect">
import { listMember, getMember } from "@/api/biz"

const LOADING_VALUE = "__member_loading__"
const EMPTY_VALUE = "__member_empty__"
const END_VALUE = "__member_end__"
const PAGE_SIZE = 20

const props = withDefaults(defineProps<{
  modelValue?: number | string | null
  placeholder?: string
  clearable?: boolean
  disabled?: boolean
  width?: string | number
}>(), {
  modelValue: undefined,
  placeholder: "手机号 / 会员ID / 姓名",
  clearable: true,
  disabled: false,
  width: 240
})

const emit = defineEmits<{
  (e: "update:modelValue", value: number | undefined): void
  (e: "change", value: number | undefined, member?: any): void
}>()

const options = ref<any[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)
const keyword = ref("")
const dropdownWrap = ref<HTMLElement | null>(null)

const styleObj = computed(() => ({
  width: typeof props.width === "number" ? `${props.width}px` : props.width
}))

function formatLabel(m: any) {
  if (!m) return ""
  const name = m.realName || "—"
  const phone = m.phone || "—"
  return `${m.memberId} / ${phone} / ${name}`
}

function buildQuery(page: number) {
  const q: Record<string, any> = { pageNum: page, pageSize: PAGE_SIZE }
  const k = (keyword.value || "").trim()
  if (!k) return q
  if (/^\d+$/.test(k) && k.length <= 8) {
    q.memberId = Number(k)
  } else if (/^\d+$/.test(k)) {
    q.phone = k
  } else if (/^[A-Za-z0-9_-]+$/.test(k)) {
    q.inviteCode = k
  } else {
    q.realName = k
  }
  return q
}

function mergeOptions(rows: any[], reset: boolean) {
  const list = rows || []
  if (reset) {
    options.value = list
    return
  }
  const exist = new Set(options.value.map((i) => i.memberId))
  list.forEach((row) => {
    if (!exist.has(row.memberId)) options.value.push(row)
  })
}

async function ensureSelectedOption() {
  const id = props.modelValue
  if (id === undefined || id === null || id === "") return
  const numId = Number(id)
  if (!numId || options.value.some((i) => Number(i.memberId) === numId)) return
  try {
    const res: any = await getMember(numId)
    if (res?.data) options.value.unshift(res.data)
  } catch {
    /* ignore */
  }
}

async function fetchPage(reset: boolean) {
  if (reset) {
    pageNum.value = 1
    hasMore.value = true
    loading.value = true
  } else {
    if (!hasMore.value || loadingMore.value || loading.value) return
    loadingMore.value = true
  }
  const page = pageNum.value
  try {
    const res: any = await listMember(buildQuery(page))
    const rows = res.rows || []
    const total = Number(res.total || 0)
    mergeOptions(rows, reset)
    const loaded = options.value.length
    hasMore.value = loaded < total && rows.length > 0
    if (rows.length) pageNum.value = page + 1
    await ensureSelectedOption()
  } catch {
    if (reset) options.value = []
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null
function onRemoteSearch(query: string) {
  keyword.value = query || ""
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchPage(true)
  }, 280)
}

function onUpdate(val: any) {
  if (val === LOADING_VALUE || val === EMPTY_VALUE || val === END_VALUE) return
  const next = val === undefined || val === null || val === "" ? undefined : Number(val)
  emit("update:modelValue", next)
  const member = options.value.find((i) => Number(i.memberId) === next)
  emit("change", next, member)
}

function onClear() {
  emit("update:modelValue", undefined)
  emit("change", undefined, undefined)
}

function onScroll(e: Event) {
  const el = e.target as HTMLElement
  if (!el || !hasMore.value || loadingMore.value || loading.value) return
  if (el.scrollHeight - el.scrollTop - el.clientHeight <= 32) {
    fetchPage(false)
  }
}

function bindScroll(bind: boolean) {
  nextTick(() => {
    const root = [...document.querySelectorAll(".member-select-dropdown")].pop() as HTMLElement | undefined
    const wrap = (root?.querySelector(".el-select-dropdown__wrap")
      || root?.querySelector(".el-scrollbar__wrap")) as HTMLElement | null
    if (dropdownWrap.value && dropdownWrap.value !== wrap) {
      dropdownWrap.value.removeEventListener("scroll", onScroll)
      dropdownWrap.value = null
    }
    if (bind && wrap) {
      wrap.addEventListener("scroll", onScroll, { passive: true })
      dropdownWrap.value = wrap
    } else if (!bind && dropdownWrap.value) {
      dropdownWrap.value.removeEventListener("scroll", onScroll)
      dropdownWrap.value = null
    }
  })
}

function onVisibleChange(visible: boolean) {
  if (visible) {
    if (!options.value.length) fetchPage(true)
    else ensureSelectedOption()
    bindScroll(true)
  } else {
    bindScroll(false)
  }
}

watch(
  () => props.modelValue,
  () => {
    ensureSelectedOption()
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer)
  bindScroll(false)
})
</script>

<style scoped>
.member-select-option {
  font-size: 13px;
}
.member-select-tip {
  display: block;
  text-align: center;
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}
</style>
