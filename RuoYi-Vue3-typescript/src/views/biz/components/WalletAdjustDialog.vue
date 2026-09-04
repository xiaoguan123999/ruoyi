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
      <el-form-item label="钱包" prop="typeCode">
        <el-select v-model="form.typeCode" style="width: 100%">
          <el-option v-for="item in typeOptions" :key="item.typeCode" :label="item.typeName" :value="item.typeCode" />
        </el-select>
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="form.currency" style="width: 100%">
          <el-option label="CNY" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="当前余额">
        <div class="balance-line">
          <span>可用 <b>{{ balanceText }}</b></span>
          <span class="frozen">冻结 {{ frozenText }}</span>
        </div>
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
import { adjustWallet, getWalletBalance, listWalletTypeOptions } from "@/api/biz"

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
  typeCode: "BALANCE",
  currency: "CNY",
  direction: "PLUS",
  amount: undefined as number | undefined,
  remark: ""
})
const typeOptions = ref<any[]>([])
const available = ref<number | null>(null)
const frozen = ref<number | null>(null)
const balanceText = computed(() => formatMoney(available.value) + " " + form.currency)
const frozenText = computed(() => formatMoney(frozen.value) + " " + form.currency)

function formatMoney(val: number | null) {
  if (val == null || !Number.isFinite(Number(val))) return "--"
  return Number(val).toFixed(4)
}

function loadBalance() {
  if (!form.memberId || !form.currency) {
    available.value = null
    frozen.value = null
    return
  }
  getWalletBalance({
    memberId: form.memberId as number,
    typeCode: form.typeCode,
    currency: form.currency
  }).then((res: any) => {
    available.value = Number(res.available ?? res.data?.available ?? 0)
    frozen.value = Number(res.frozen ?? res.data?.frozen ?? 0)
  }).catch(() => {
    available.value = null
    frozen.value = null
  })
}
const rules = {
  memberId: [{ required: true, message: "请选择会员", trigger: "change" }],
  typeCode: [{ required: true, message: "请选择钱包", trigger: "change" }],
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
    form.typeCode = "BALANCE"
    form.currency = "CNY"
    form.direction = "PLUS"
    form.amount = undefined
    form.remark = ""
    nextTick(() => {
      formRef.value?.clearValidate?.()
      loadBalance()
    })
  }
)

watch(
  () => [form.memberId, form.typeCode, form.currency],
  () => {
    if (props.modelValue) loadBalance()
  }
)

function onClose() {
  emit("update:modelValue", false)
}

listWalletTypeOptions().then((res: any) => { typeOptions.value = res.data || [] })

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
    const typeName = typeOptions.value.find((item: any) => item.typeCode === form.typeCode)?.typeName || form.typeCode
    proxy.$modal.confirm("当前可用 " + formatMoney(available.value) + " " + form.currency + "。确认对 " + who + " 的" + typeName + " " + dirLabel + " " + amount + " " + form.currency + "？将立即入账并记流水。").then(() => {
      submitting.value = true
      return adjustWallet({
        memberId: form.memberId as number,
        typeCode: form.typeCode,
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

<style scoped>
.balance-line {
  display: flex;
  align-items: baseline;
  gap: 16px;
  line-height: 32px;
}
.balance-line b {
  font-size: 16px;
}
.frozen {
  color: var(--el-text-color-secondary);
}
</style>
