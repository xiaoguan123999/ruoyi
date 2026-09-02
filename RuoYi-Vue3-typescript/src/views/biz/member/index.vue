<template>
  <div class="app-container ops-page">
    <div class="ops-section-card">
      <div class="ops-section-card__hd">App 谷歌验证</div>
      <div class="ops-section-card__bd">
        <el-form :model="google" label-width="140px" v-loading="googleLoading" class="ops-form-full">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="谷歌验证开关">
                <el-switch v-model="google.enabled" />
                <span class="tip">关闭后 App 不能绑定谷歌验证器</span>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="10">
              <el-form-item label="验证器名称">
                <el-input v-model="google.issuer" placeholder="显示在谷歌验证器里的名称" maxlength="32" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label-width="0">
                <el-button type="primary" @click="saveGoogle" v-hasPermi="['biz:member:edit']">保存</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </div>
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="会员" prop="memberId">
        <MemberSelect v-model="queryParams.memberId" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="邀请码" prop="inviteCode">
        <el-input v-model="queryParams.inviteCode" placeholder="邀请码" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="实名" prop="kycStatus">
        <el-select v-model="queryParams.kycStatus" placeholder="实名状态" clearable style="width: 160px">
          <el-option label="未实名" value="0" />
          <el-option label="已实名" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="谷歌验证" prop="gaStatus">
        <el-select v-model="queryParams.gaStatus" placeholder="谷歌验证" clearable style="width: 160px">
          <el-option label="未绑定" value="0" />
          <el-option label="已绑定" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="账号类型" prop="testFlag">
        <el-select v-model="queryParams.testFlag" placeholder="账号类型" clearable style="width: 160px">
          <el-option label="正式" value="0" />
          <el-option label="测试" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:member:add']">新增顶级会员</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="memberList">
      <el-table-column label="ID" align="center" prop="memberId" width="90" />
      <el-table-column label="邀请码" align="center" prop="inviteCode" width="110" />
      <el-table-column label="手机号" align="center" prop="phone" width="120">
        <template #default="scope">
          <span>{{ scope.row.phone }}</span>
          <el-tag v-if="isTestMember(scope.row)" type="warning" size="small" style="margin-left: 4px">测试</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="姓名" align="center" prop="realName" min-width="100" />
      <el-table-column label="身份证" align="center" prop="idCard" width="180" />
      <el-table-column label="谷歌验证" align="center" prop="gaStatus" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.gaStatus === '1' ? 'success' : 'info'">{{ scope.row.gaStatus === '1' ? '已绑定' : '未绑定' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实名" align="center" prop="kycStatus" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.kycStatus === '1' ? 'success' : 'info'">{{ scope.row.kycStatus === '1' ? '已实名' : '未实名' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="等级" align="center" prop="levelName" width="90">
        <template #default="scope">{{ scope.row.levelName || "无等级" }}</template>
      </el-table-column>
      <el-table-column label="上级ID" align="center" prop="parentId" width="90" />
      <el-table-column label="余额CNY" align="center" prop="cnyAvailable" width="100" />
      <el-table-column label="产品收益CNY" align="center" prop="cnyProductIncome" width="120" />
      <el-table-column label="推广收益CNY" align="center" prop="cnyAssistValue" width="120" />
      <el-table-column label="CNY冻结" align="center" prop="cnyFrozen" width="100" />
      <el-table-column label="余额USDT" align="center" prop="usdtAvailable" width="110" />
      <el-table-column label="产品收益USDT" align="center" prop="usdtProductIncome" width="130" />
      <el-table-column label="推广收益USDT" align="center" prop="usdtAssistValue" width="130" />
      <el-table-column label="USDT冻结" align="center" prop="usdtFrozen" width="110" />
      <el-table-column label="团队人数" align="center" prop="teamCount" width="90" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="测试账号" align="center" width="90">
        <template #default="scope">
          <el-switch
            :model-value="isTestMember(scope.row)"
            @change="(val: boolean) => handleToggleTest(scope.row, val)"
            v-hasPermi="['biz:member:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="420" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:member:edit']">修改</el-button>
          <el-button link type="primary" icon="Wallet" @click="openAdjust(scope.row)" v-hasPermi="['biz:wallet:adjust']">调账</el-button>
          <el-button link type="primary" icon="Key" @click="handleResetPwd(scope.row)" v-hasPermi="['biz:member:resetPwd']">登录密码</el-button>
          <el-button link type="primary" icon="Lock" @click="handleResetPayPwd(scope.row)" v-hasPermi="['biz:member:resetPayPwd']">交易密码</el-button>
          <el-button v-if="scope.row.gaStatus === '1'" link type="primary" icon="Unlock" @click="handleResetGoogle(scope.row)" v-hasPermi="['biz:member:edit']">解绑谷歌</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="isAdd ? rules : {}" label-width="90px">
        <el-form-item v-if="isAdd" label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-else label="手机号">
          <el-input v-model="form.phone" disabled />
        </el-form-item>
        <template v-if="isAdd">
          <el-form-item label="登录密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入登录密码" show-password />
          </el-form-item>
          <el-alert title="开通后没有上级，系统生成7位不重复邀请码，发给后续用户填写。" type="info" :closable="false" show-icon style="margin-bottom: 12px" />
        </template>
        <template v-else>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="实名状态" prop="kycStatus">
            <el-radio-group v-model="form.kycStatus">
              <el-radio value="0">未实名</el-radio>
              <el-radio value="1">已实名</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="账号状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio value="0">正常</el-radio>
              <el-radio value="1">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="测试账号">
            <div class="switch-line">
              <el-switch v-model="form.testFlag" active-value="1" inactive-value="0" />
              <span class="tip inline">测试账号不计入看板、团队与奖励统计，一拖二仍生效</span>
            </div>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <WalletAdjustDialog v-model="adjustOpen" :member-id="adjustMemberId" :phone="adjustPhone" @success="getList" />
  </div>
</template>

<script setup lang="ts" name="BizMember">
import { listMember, getMember, addMember, updateMember, resetMemberGoogle, resetMemberPwd, resetMemberPayPwd, getMemberGoogleConfig, saveMemberGoogleConfig } from "@/api/biz"
import WalletAdjustDialog from "@/views/biz/components/WalletAdjustDialog.vue"

const { proxy } = getCurrentInstance() as any
const memberList = ref<any[]>([])
const open = ref(false)
const isAdd = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const adjustOpen = ref(false)
const adjustMemberId = ref<number | undefined>()
const adjustPhone = ref("")
const googleLoading = ref(false)
const google = ref({ enabled: true, issuer: "App" })

const data = reactive({
  form: {} as any,
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    memberId: undefined,
    phone: undefined,
    inviteCode: undefined,
    kycStatus: undefined,
    status: undefined,
    gaStatus: undefined,
    testFlag: undefined
  },
  rules: {
    phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
    password: [{ required: true, message: "请输入密码", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)
const route = useRoute()

function isTestMember(row: any) {
  if (row.testFlag !== undefined && row.testFlag !== null && row.testFlag !== "") {
    return row.testFlag === "1" || row.testFlag === 1 || row.testFlag === true
  }
  if (typeof row.testFlagFlag === "boolean") return row.testFlagFlag
  if (typeof row.testAccount === "boolean") return row.testAccount
  return false
}

function asTestFlagStr(value: any) {
  if (typeof value === "boolean") return value ? "1" : "0"
  return value === "1" || value === 1 || value === true ? "1" : "0"
}

function normalizeMemberForm(data: any) {
  return {
    ...data,
    testFlag: asTestFlagStr(data.testFlag ?? data.testFlagFlag ?? data.testAccount)
  }
}

function buildMemberUpdatePayload(data: any) {
  return {
    memberId: data.memberId,
    realName: data.realName,
    idCard: data.idCard,
    kycStatus: data.kycStatus,
    status: data.status,
    testFlag: asTestFlagStr(data.testFlag),
    remark: data.remark
  }
}

function applyRouteQuery() {
  const kyc = String(route.query.kycStatus || "")
  if (kyc === "0" || kyc === "1") {
    queryParams.value.kycStatus = kyc
  }
}

function loadGoogle() {
  googleLoading.value = true
  getMemberGoogleConfig().then((res: any) => {
    const data = res.data || {}
    google.value = {
      enabled: data.enabled !== false,
      issuer: data.issuer || "App"
    }
  }).finally(() => { googleLoading.value = false })
}
function saveGoogle() {
  saveMemberGoogleConfig(google.value).then(() => {
    proxy.$modal.msgSuccess("谷歌验证配置已保存")
    loadGoogle()
  })
}
function getList() {
  loading.value = true
  listMember(queryParams.value).then((res: any) => {
    memberList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}
function reset() {
  form.value = {}
  proxy.resetForm("formRef")
}
function handleAdd() {
  reset()
  isAdd.value = true
  open.value = true
  title.value = "新增顶级会员"
}
function handleUpdate(row: any) {
  reset()
  isAdd.value = false
  getMember(row.memberId).then((res: any) => {
    form.value = normalizeMemberForm(res.data || {})
    form.value.password = undefined
    open.value = true
    title.value = "修改会员"
  })
}
function handleToggleTest(row: any, val: boolean) {
  const next = val ? "1" : "0"
  const action = val ? "标记为测试账号" : "取消测试账号标记"
  const tip = val
    ? "标记后该账号不计入看板、团队与奖励统计，一拖二仍生效；历史已发佣金不会追回。"
    : "取消后该账号将按正式用户参与统计与奖励。"
  proxy.$modal.confirm(`确认对「${row.phone || row.memberId}」${action}？${tip}`).then(() => {
    return updateMember({ memberId: row.memberId, testFlag: next })
  }).then(() => {
    proxy.$modal.msgSuccess(val ? "已标记为测试账号" : "已取消测试账号标记")
    getList()
  }).catch(() => getList())
}
function openAdjust(row: any) {
  adjustMemberId.value = row.memberId
  adjustPhone.value = row.phone || ""
  adjustOpen.value = true
}
function handleResetGoogle(row: any) {
  proxy.$modal.confirm('确认解绑会员 ' + row.phone + ' 的谷歌验证器？解绑后需重新绑定。').then(() => {
    return resetMemberGoogle(row.memberId)
  }).then(() => {
    proxy.$modal.msgSuccess("已解绑")
    getList()
  }).catch(() => {})
}
function handleResetPwd(row: any) {
  proxy.$prompt("请输入「" + row.phone + "」的新登录密码", "重置登录密码", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputType: "password",
    inputValidator: (value: string) => {
      if (!value || value.length < 5 || value.length > 20) {
        return "登录密码长度必须为 5-20 位"
      }
    }
  }).then(({ value }: { value: string }) => {
    return resetMemberPwd(row.memberId, value).then(() => {
      proxy.$modal.msgSuccess("登录密码已重置，新密码是：" + value)
    })
  }).catch(() => {})
}
function handleResetPayPwd(row: any) {
  proxy.$prompt("请输入「" + row.phone + "」的新交易密码", "重置交易密码", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputType: "password",
    inputValidator: (value: string) => {
      if (!value || value.length < 4 || value.length > 20) {
        return "交易密码长度必须为 4-20 位"
      }
    }
  }).then(({ value }: { value: string }) => {
    return resetMemberPayPwd(row.memberId, value).then(() => {
      proxy.$modal.msgSuccess("交易密码已重置，新密码是：" + value)
    })
  }).catch(() => {})
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    if (isAdd.value) {
      addMember({ phone: form.value.phone, password: form.value.password }).then((res: any) => {
        proxy.$modal.msgSuccess("开通成功，邀请码 " + (res.inviteCode || res.data?.inviteCode || res.memberId))
        open.value = false
        getList()
      })
      return
    }
    updateMember(buildMemberUpdatePayload(form.value)).then(() => {
      proxy.$modal.msgSuccess("修改成功")
      open.value = false
      getList()
    })
  })
}
applyRouteQuery()
loadGoogle()
getList()
watch(
  () => String(route.query.kycStatus || ""),
  () => {
    applyRouteQuery()
    handleQuery()
  }
)
</script>

<style scoped>
.tip { margin-left: 12px; color: #909399; font-size: 13px; }
.tip.inline { margin-left: 10px; }
.switch-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  width: 100%;
}
</style>
