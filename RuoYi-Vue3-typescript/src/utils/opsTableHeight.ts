const PAGINATION_GAP = 16
const PAGE_BOTTOM_GAP = 20
const MIN_TABLE_HEIGHT = 240
const DEFAULT_PAGINATION_HEIGHT = 52

/** 表格下方分页占位（含间距） */
function getPaginationReserve(tableEl: HTMLElement): number {
  let next = tableEl.nextElementSibling
  if (next?.classList.contains("pagination-container")) {
    return next.getBoundingClientRect().height + PAGINATION_GAP
  }

  const parent = tableEl.parentElement
  if (parent) {
    const pagination = parent.querySelector(":scope > .pagination-container")
    if (pagination) {
      return pagination.getBoundingClientRect().height + PAGINATION_GAP
    }
  }

  const pageRoot = tableEl.closest(".ops-page, .app-container, .tree-sidebar-content")
  if (pageRoot) {
    const pagination = pageRoot.querySelector(".pagination-container")
    if (pagination) {
      return pagination.getBoundingClientRect().height + PAGINATION_GAP
    }
  }

  return DEFAULT_PAGINATION_HEIGHT + PAGINATION_GAP
}

/** 根据表格在视口中的位置，计算可用最大高度 */
export function calcOpsTableMaxHeight(tableEl: HTMLElement, offset = 0): number {
  const top = tableEl.getBoundingClientRect().top
  const reserve = getPaginationReserve(tableEl) + PAGE_BOTTOM_GAP + offset
  const height = window.innerHeight - top - reserve
  return Math.max(MIN_TABLE_HEIGHT, Math.floor(height))
}

export function shouldAutoBindOpsTable(
  tableEl: HTMLElement | undefined,
  props: Record<string, unknown>,
  attrs: Record<string, unknown>
): boolean {
  if (!tableEl) return false
  if (props.maxHeight != null || props.height != null) return false
  if (attrs.maxHeight != null || attrs.height != null) return false
  if (tableEl.closest(".el-dialog, .el-drawer, .el-popover")) return false
  if (tableEl.dataset.opsTableScroll === "off") return false
  return tableEl.closest(".ops-page, .app-container, .tree-sidebar-content") != null
}

export function createOpsTableHeightBinder(
  tableEl: HTMLElement,
  setHeight: (height: number) => void,
  options?: { offset?: number }
): () => void {
  const offset = options?.offset ?? 0
  const update = () => {
    setHeight(calcOpsTableMaxHeight(tableEl, offset))
  }

  update()
  window.addEventListener("resize", update)

  const resizeObserver = new ResizeObserver(update)
  const observeTarget =
    tableEl.closest(".ops-page, .app-container, .tree-sidebar-content, .app-main") ||
    tableEl.parentElement
  if (observeTarget) {
    resizeObserver.observe(observeTarget)
  }

  return () => {
    window.removeEventListener("resize", update)
    resizeObserver.disconnect()
  }
}
