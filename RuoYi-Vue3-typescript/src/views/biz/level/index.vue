<template>
  <div class="app-container ops-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="等级名称" prop="levelName">
        <el-input v-model="queryParams.levelName" placeholder="等级名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:level:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="levelId" width="80" />
      <el-table-column label="等级" align="center" prop="levelName" />
      <el-table-column label="团队要求" align="center" prop="teamDepth" min-width="100" />
      <el-table-column label="累计充值CNY" align="center" prop="minRechargeCny" />
      <el-table-column label="累计充值USDT" align="center" prop="minRechargeUsdt" />
      <el-table-column label="团队奖励CNY" align="center" prop="rewardCny" />
      <el-table-column label="团队奖励USDT" align="center" prop="rewardUsdt" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:level:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:level:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="等级名称" prop="levelName"><el-input v-model="form.levelName" /></el-form-item>
        <el-form-item label="团队要求" prop="teamDepth">
          <el-input v-model="form.teamDepth" placeholder="例如 一级内，对应 App 等级表该列" />
        </el-form-item>
        <el-form-item label="累计充值CNY" prop="minRechargeCny"><el-input-number v-model="form.minRechargeCny" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="累计充值USDT" prop="minRechargeUsdt"><el-input-number v-model="form.minRechargeUsdt" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="团队奖励CNY">
          <el-input-number v-model="form.rewardCny" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="团队奖励USDT">
          <el-input-number v-model="form.rewardUsdt" :min="0" :precision="2" style="width: 100%" />
          <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:6px">会员达到该等级后自动发给本人一次，对应 App 等级表「团队奖励」。已达标会员可到「等级奖励」点立即核算。</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizLevel">
import { listLevel, getLevel, addLevel, updateLevel, delLevel } from "@/api/biz"

const { proxy } = getCurrentInstance() as any
const dataList = ref<any[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, levelName: undefined },
  rules: { levelName: [{ required: true, message: "等级名称不能为空", trigger: "blur" }] }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listLevel(queryParams.value).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() { form.value = { status: "0", teamDepth: "", minValidMembers: 0, minRechargeCny: 0, minRechargeUsdt: 0, minTeamPerfCny: 0, minTeamPerfUsdt: 0, rewardCny: 0, rewardUsdt: 0, sort: 0 } }
function handleAdd() { reset(); open.value = true; title.value = "新增等级" }
function handleUpdate(row: any) {
  getLevel(row.levelId).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = "修改等级"
  })
}
function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean) => {
    if (!valid) return
    const data = form.value
    const req = data.levelId ? updateLevel(data) : addLevel(data)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除等级"' + row.levelName + '"？').then(() => delLevel(row.levelId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
getList()
</script>
