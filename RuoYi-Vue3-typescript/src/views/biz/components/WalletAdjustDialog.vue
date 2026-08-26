<template>
  <el-dialog title="钱包调账" :model-value="modelValue" width="460px" append-to-body @close="onClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="会员" prop="memberId">
        <MemberSelect
          v-model="form.memberId"
          width="100%"
          placeholder="选择会员"
          :disabled="locked"
        />
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="form.currency" style="width: 100%">
          <el-option label="CNY" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="方向" prop="direction">
        <el-radio-group v-model="form.direction">
          <el-radio value="PLUS">增加</el-radio>
          <el-radio value="MINUS">减少</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number v-model="form.amount" :min="0.0001" :precision="4" :step="1" style="width: 100%" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="必填，会写入资金流水" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="submitting" @click="submit">确 定</el-button>
      <el-button @click="onClose">取 消</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts" name="WalletAdjustDialog">
import { adjustWallet } from "@/api/biz"

const props = defineProps<{
  modelValue: boolean
  memberId?: number
  phone?: string
}>()
const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void
  (e: "success"): void
}>()

const { proxy } = getCurrentInstance() as any
const formRef = ref()
const submitting = ref(false)
const locked = computed(() => props.memberId != null && props.memberId !== undefined)
const form = reactive({
  memberId: undefined as number | undefined,
  currency: "CNY",
  direction: "PLUS",
  amount: undefined as number | undefined,
  remark: ""
})
const rules = {
  memberId: [{ required: true, message: "请选择会员", trigger: "change" }],
  currency: [{ required: true, message: "请选择币种", trigger: "change" }],
  direction: [{ required: true, message: "请选择增加或减少", trigger: "change" }],
  amount: [{ required: true, message: "请填写金额", trigger: "blur" }],
  remark: [{ required: true, message: "请填写备注", trigger: "blur" }]
}

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    form.memberId = props.memberId
    form.currency = "CNY"
    form.direction = "PLUS"
    form.amount = undefined
    form.remark = ""
    nextTick(() => formRef.value?.clearValidate?.())
  }
)

function onClose() {
  emit("update:modelValue", false)
}

function submit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const amount = Number(form.amount)
    if (!Number.isFinite(amount) || amount <= 0) {
      proxy.$modal.msgError("金额必须大于 0")
      return
    }
    const dirLabel = form.direction === "MINUS" ? "减少" : "增加"
    const who = props.phone ? props.phone : ("会员 " + form.memberId)
    proxy.$modal.confirm("确认对 " + who + " " + dirLabel + " " + amount + " " + form.currency + "？将立即入账并记流水。").then(() => {
      submitting.value = true
      return adjustWallet({
        memberId: form.memberId as number,
        currency: form.currency,
        direction: form.direction,
        amount,
        remark: (form.remark || "").trim()
      })
    }).then(() => {
      proxy.$modal.msgSuccess("调账成功")
      emit("success")
      onClose()
    }).catch(() => {}).finally(() => {
      submitting.value = false
    })
  })
}
</script>
