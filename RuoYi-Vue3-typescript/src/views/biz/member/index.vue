<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="邀请码" prop="inviteCode">
        <el-input v-model="queryParams.inviteCode" placeholder="会员ID/邀请码" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="实名" prop="kycStatus">
        <el-select v-model="queryParams.kycStatus" placeholder="实名状态" clearable style="width: 160px">
          <el-option label="未实名" value="0" />
          <el-option label="已实名" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
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
      <el-table-column label="ID/邀请码" align="center" prop="memberId" width="100" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="姓名" align="center" prop="realName" />
      <el-table-column label="身份证" align="center" prop="idCard" width="180" />
      <el-table-column label="实名" align="center" prop="kycStatus" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.kycStatus === '1' ? 'success' : 'info'">{{ scope.row.kycStatus === '1' ? '已实名' : '未实名' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="等级" align="center" prop="levelName" width="80" />
      <el-table-column label="上级ID" align="center" prop="parentId" width="90" />
      <el-table-column label="CNY可用" align="center" prop="cnyAvailable" width="100" />
      <el-table-column label="团队人数" align="center" prop="teamCount" width="90" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:member:edit']">修改</el-button>
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
          <el-alert title="开通后没有上级，邀请码等于新会员ID，可发给后续用户填写。" type="info" :closable="false" show-icon style="margin-bottom: 12px" />
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
          <el-form-item label="重置密码">
            <el-input v-model="form.password" type="password" placeholder="不修改请留空" show-password />
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
  </div>
</template>

<script setup lang="ts" name="BizMember">
import { listMember, getMember, addMember, updateMember } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const memberList = ref<any[]>([])
const open = ref(false)
const isAdd = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {} as any,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    phone: undefined,
    inviteCode: undefined,
    kycStatus: undefined,
    status: undefined
  },
  rules: {
    phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
    password: [{ required: true, message: "请输入密码", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

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
    form.value = res.data
    form.value.password = undefined
    open.value = true
    title.value = "修改会员"
  })
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
    updateMember(form.value).then(() => {
      proxy.$modal.msgSuccess("修改成功")
      open.value = false
      getList()
    })
  })
}
getList()
</script>
