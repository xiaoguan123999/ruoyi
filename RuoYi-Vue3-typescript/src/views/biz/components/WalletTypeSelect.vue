<template>
  <el-select :model-value="modelValue" :style="{ width: width }" :placeholder="placeholder" :disabled="disabled" @update:model-value="onChange">
    <el-option v-for="item in options" :key="item.typeCode" :label="item.typeName" :value="item.typeCode" />
  </el-select>
</template>

<script setup lang="ts" name="WalletTypeSelect">
import { listWalletTypeOptions } from "@/api/biz"

const props = withDefaults(defineProps<{
  modelValue?: string
  width?: string
  placeholder?: string
  disabled?: boolean
}>(), {
  modelValue: "",
  width: "240px",
  placeholder: "到账钱包",
  disabled: false
})
const emit = defineEmits<{
  (e: "update:modelValue", value: string): void
}>()

const options = ref<any[]>([])
listWalletTypeOptions().then((res: any) => { options.value = res.data || [] })

function onChange(value: string) {
  emit("update:modelValue", value)
}
</script>
