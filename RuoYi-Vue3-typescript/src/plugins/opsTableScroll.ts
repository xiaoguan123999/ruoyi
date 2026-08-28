import { App, defineComponent, h, onActivated, onBeforeUnmount, onMounted, ref, nextTick, watch } from "vue"
import { ElTable } from "element-plus"
import {
  createOpsTableHeightBinder,
  shouldAutoBindOpsTable
} from "@/utils/opsTableHeight"

/**
 * 包装 ElTable：列表页自动按视口计算 max-height，表头固定、表体滚动。
 * 页面已手写 max-height / height 或在弹窗内的表格不受影响。
 */
export function patchOpsElTable(app: App) {
  const RawTable = ElTable

  app.component(
    "ElTable",
    defineComponent({
      name: "ElTable",
      inheritAttrs: false,
      props: (RawTable as any).props,
      setup(props, { attrs, slots, expose }) {
        const tableRef = ref<InstanceType<typeof ElTable>>()
        const autoMaxHeight = ref<number>()
        let unbind: (() => void) | undefined

        const bindAutoHeight = () => {
          unbind?.()
          unbind = undefined
          autoMaxHeight.value = undefined

          nextTick(() => {
            const root = tableRef.value?.$el as HTMLElement | undefined
            if (!shouldAutoBindOpsTable(root, props as Record<string, unknown>, attrs)) return
            unbind = createOpsTableHeightBinder(root!, (height) => {
              autoMaxHeight.value = height
            })
          })
        }

        onMounted(bindAutoHeight)
        onActivated(bindAutoHeight)
        onBeforeUnmount(() => unbind?.())

        watch(
          tableRef,
          (inst) => {
            if (inst) expose(inst as any)
          },
          { immediate: true }
        )

        return () =>
          h(
            RawTable,
            {
              ...attrs,
              ...props,
              ref: tableRef,
              maxHeight:
                (props as any).maxHeight ??
                (attrs as any).maxHeight ??
                autoMaxHeight.value
            },
            slots
          )
      }
    })
  )
}
