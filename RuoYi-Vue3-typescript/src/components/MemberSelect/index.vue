<template>
  <el-select
    :model-value="innerValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    filterable
    remote
    reserve-keyword
    :remote-method="onRemoteSearch"
    :loading="loading"
    :multiple="multiple"
    :collapse-tags="multiple"
    :collapse-tags-tooltip="multiple"
    :style="styleObj"
    popper-class="member-select-dropdown"
    @update:model-value="onUpdate"
    @visible-change="onVisibleChange"
    @clear="onClear"
  >
    <el-option
      v-for="item in displayOptions"
      :key="'m-' + item.memberId"
      :label="formatLabel(item)"
      :value="Number(item.memberId)"
    >
      <span class="member-select-option">{{ formatLabel(item) }}</span>
    </el-option>
  </el-select>
</template>

<script setup lang="ts" name="MemberSelect">
import { listMember, getMember } from "@/api/biz"

const PAGE_SIZE = 20

const props = withDefaults(defineProps<{
  modelValue?: number | string | number[] | null
  placeholder?: string
  clearable?: boolean
  disabled?: boolean
  multiple?: boolean
  width?: string | number
}>(), {
  modelValue: undefined,
  placeholder: "邀请码 / 手机号 / 姓名 / 会员ID",
  clearable: true,
  disabled: false,
  multiple: false,
  width: 240
})

const emit = defineEmits<{
  (e: "update:modelValue", value: number | number[] | undefined): void
  (e: "change", value: number | number[] | undefined, member?: any): void
}>()

const options = ref<any[]>([])
/** 已选会员缓存，保证远程搜索清空列表后仍能回显标签 */
const selectedCache = ref<Record<number, any>>({})
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)
const keyword = ref("")
const dropdownWrap = ref<HTMLElement | null>(null)

const styleObj = computed(() => ({
  width: typeof props.width === "number" ? `${props.width}px` : props.width
}))

const innerValue = computed(() => {
  if (props.multiple) {
    return selectedIds()
  }
  const ids = selectedIds()
  return ids.length ? ids[0] : undefined
})

const displayOptions = computed(() => {
  const map = new Map<number, any>()
  Object.values(selectedCache.value).forEach((m: any) => {
    const id = Number(m?.memberId)
    if (id > 0) map.set(id, m)
  })
  options.value.forEach((m) => {
    const id = Number(m?.memberId)
    if (id > 0) map.set(id, m)
  })
  return [...map.values()]
})

function formatLabel(m: any) {
  if (!m) return ""
  const id = m.memberId ?? "—"
  const phone = m.phone || "—"
  const name = m.realName || "—"
  return `${id} / ${phone} / ${name}`
}

function selectedIds(): number[] {
  const v = props.modelValue
  if (v === undefined || v === null || v === "") return []
  if (Array.isArray(v)) {
    return v.map((x) => Number(x)).filter((n) => Number.isFinite(n) && n > 0)
  }
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? [n] : []
}

function rememberMembers(list: any[]) {
  list.forEach((m) => {
    const id = Number(m?.memberId)
    if (id > 0) selectedCache.value[id] = m
  })
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
  const list = (rows || []).map((r) => ({ ...r, memberId: Number(r.memberId) }))
  if (reset) {
    options.value = list
    return
  }
  const exist = new Set(options.value.map((i) => Number(i.memberId)))
  list.forEach((row) => {
    if (!exist.has(Number(row.memberId))) options.value.push(row)
  })
}

async function ensureSelectedOptions() {
  const ids = selectedIds()
  for (const numId of ids) {
    if (selectedCache.value[numId] || options.value.some((i) => Number(i.memberId) === numId)) {
      const hit = selectedCache.value[numId] || options.value.find((i) => Number(i.memberId) === numId)
      if (hit) selectedCache.value[numId] = hit
      continue
    }
    try {
      const res: any = await getMember(numId)
      if (res?.data) {
        const row = { ...res.data, memberId: Number(res.data.memberId) }
        selectedCache.value[numId] = row
        if (!options.value.some((i) => Number(i.memberId) === numId)) {
          options.value.unshift(row)
        }
      }
    } catch {
      /* ignore */
    }
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
    rememberMembers(rows)
    const loaded = options.value.length
    hasMore.value = loaded < total && rows.length > 0
    if (rows.length) pageNum.value = page + 1
    await ensureSelectedOptions()
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
  if (props.multiple) {
    const raw = Array.isArray(val) ? val : []
    const next = raw.map((x) => Number(x)).filter((n) => Number.isFinite(n) && n > 0)
    // 选中后立刻写入缓存，避免远程刷新丢标签
    next.forEach((id) => {
      const hit = displayOptions.value.find((m) => Number(m.memberId) === id)
      if (hit) selectedCache.value[id] = hit
    })
    emit("update:modelValue", next)
    emit("change", next)
    return
  }
  const next = val === undefined || val === null || val === "" ? undefined : Number(val)
  if (next) {
    const hit = displayOptions.value.find((m) => Number(m.memberId) === next)
    if (hit) selectedCache.value[next] = hit
  }
  emit("update:modelValue", next)
  emit("change", next, next ? selectedCache.value[next] : undefined)
}

function onClear() {
  if (props.multiple) {
    emit("update:modelValue", [])
    emit("change", [])
    return
  }
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
    else ensureSelectedOptions()
    bindScroll(true)
  } else {
    bindScroll(false)
  }
}

watch(
  () => props.modelValue,
  () => {
    ensureSelectedOptions()
  },
  { immediate: true, deep: true }
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
</style>
