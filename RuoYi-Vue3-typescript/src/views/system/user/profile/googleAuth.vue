<template>
  <div>
    <el-alert
      title="绑定后，后台登录需额外填写谷歌验证器 6 位动态码。未绑定的账号仍可直接登录。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />
    <el-form label-width="120px" style="max-width: 520px">
      <el-form-item label="当前状态">
        <el-tag v-if="bound" type="success">已绑定</el-tag>
        <el-tag v-else type="info">未绑定</el-tag>
      </el-form-item>
      <template v-if="!bound">
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="startBind">生成绑定二维码</el-button>
        </el-form-item>
        <el-form-item v-if="otpauthUrl" label="扫码绑定">
          <div>
            <img :src="qrUrl" alt="QR" style="width: 180px; height: 180px; background: #fff; padding: 8px; border-radius: 8px" />
            <div style="margin-top: 8px; color: #909399; font-size: 12px">用 Google Authenticator / 微软 Authenticator 扫码</div>
            <div style="margin-top: 8px; word-break: break-all">密钥：{{ secret }}</div>
          </div>
        </el-form-item>
        <el-form-item v-if="otpauthUrl" label="验证码">
          <el-input v-model="googleCode" placeholder="请输入 6 位验证码" maxlength="6" style="width: 220px" />
          <el-button type="primary" style="margin-left: 8px" :loading="loading" @click="confirmBind">确认绑定</el-button>
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="验证码">
          <el-input v-model="googleCode" placeholder="请输入当前 6 位验证码" maxlength="6" style="width: 220px" />
          <el-button type="danger" style="margin-left: 8px" :loading="loading" @click="doUnbind">解绑</el-button>
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { confirmGoogleBind, getGoogleStatus, startGoogleBind, unbindGoogle } from "@/api/system/user"

const { proxy } = getCurrentInstance()
const loading = ref(false)
const bound = ref(false)
const secret = ref("")
const otpauthUrl = ref("")
const googleCode = ref("")

const qrUrl = computed(() => {
  if (!otpauthUrl.value) {
    return ""
  }
  return "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + encodeURIComponent(otpauthUrl.value)
})

function loadStatus() {
  getGoogleStatus().then((res: any) => {
    bound.value = !!(res.data && res.data.bound)
  })
}

function startBind() {
  loading.value = true
  startGoogleBind().then((res: any) => {
    secret.value = res.data.secret
    otpauthUrl.value = res.data.otpauthUrl
    googleCode.value = ""
  }).finally(() => {
    loading.value = false
  })
}

function confirmBind() {
  if (!googleCode.value || googleCode.value.length !== 6) {
    proxy.$modal.msgError("请输入 6 位验证码")
    return
  }
  loading.value = true
  confirmGoogleBind(googleCode.value).then(() => {
    proxy.$modal.msgSuccess("绑定成功，下次登录需要谷歌验证码")
    bound.value = true
    secret.value = ""
    otpauthUrl.value = ""
    googleCode.value = ""
  }).finally(() => {
    loading.value = false
  })
}

function doUnbind() {
  if (!googleCode.value || googleCode.value.length !== 6) {
    proxy.$modal.msgError("请输入 6 位验证码")
    return
  }
  loading.value = true
  unbindGoogle(googleCode.value).then(() => {
    proxy.$modal.msgSuccess("已解绑")
    bound.value = false
    googleCode.value = ""
  }).finally(() => {
    loading.value = false
  })
}

loadStatus()
</script>
