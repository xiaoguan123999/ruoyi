<template>
  <div class="app-container ops-page">
    <el-alert
      title="纯页面设计预览：关于我们仅一条内容。选择文本或 PDF 模式后，下方展示对应编辑区。暂不对接接口。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-form :model="display" label-width="110px" class="ops-form-full">
      <el-divider content-position="left">展示模式</el-divider>
      <el-form-item label="启用模式">
        <el-radio-group v-model="display.mode">
          <el-radio value="TEXT">文本模式</el-radio>
          <el-radio value="PDF">PDF 模式</el-radio>
        </el-radio-group>
        <div class="field-tip">二选一，必选一种；切换后下方内容区随之变化</div>
      </el-form-item>
    </el-form>

    <!-- 文本模式：单条内容 -->
    <el-form v-if="display.mode === 'TEXT'" :model="textForm" label-width="110px" class="ops-form-full">
      <el-divider content-position="left">文本内容</el-divider>
      <el-form-item label="标题">
        <el-input v-model="textForm.title" placeholder="例如 星帆智联" style="max-width: 420px" />
      </el-form-item>
      <el-form-item label="副标题">
        <el-input v-model="textForm.subtitle" placeholder="例如 连接星空 · 智联未来" style="max-width: 420px" />
      </el-form-item>
      <el-form-item label="配图">
        <image-upload v-model="textForm.imageUrl" :limit="1" />
      </el-form-item>
      <el-form-item label="正文">
        <editor v-model="textForm.content" :min-height="220" />
      </el-form-item>
      <el-form-item label-width="0" class="form-actions">
        <el-button type="primary">保存文本内容</el-button>
        <el-button>重 置</el-button>
      </el-form-item>
    </el-form>

    <!-- PDF 模式：左上传，右预览 -->
    <div v-else class="ops-form-full pdf-panel">
      <el-divider content-position="left">PDF 内容</el-divider>
      <el-row :gutter="20" class="pdf-panel__row">
        <el-col :xs="24" :md="10" :lg="9">
          <el-form :model="display" label-width="90px">
            <el-form-item label="PDF 文件">
              <file-upload v-model="display.pdfUrl" :limit="1" :file-size="20" :file-type="['pdf']" />
              <div class="field-tip">仅支持 pdf，建议不超过 20MB。App 将按此文件逐页渲染</div>
            </el-form-item>
            <el-form-item label-width="0" class="form-actions">
              <el-button type="primary">保存 PDF 设置</el-button>
              <el-button>取 消</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :xs="24" :md="14" :lg="15">
          <div class="pdf-preview">
            <div class="pdf-preview__bar">
              <span>预览</span>
              <el-button v-if="pdfSrc" type="primary" link @click="previewOpen = true">放大阅览</el-button>
            </div>
            <iframe v-if="pdfSrc" :src="pdfSrc" class="pdf-preview__frame" title="PDF 预览" />
            <div v-else class="pdf-preview__empty">上传后可在此直接阅览</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-dialog
      title="PDF 阅览"
      v-model="previewOpen"
      width="90%"
      top="4vh"
      append-to-body
      destroy-on-close
      class="pdf-preview-dialog"
    >
      <iframe v-if="pdfSrc" :src="pdfSrc" class="pdf-preview__frame pdf-preview__frame--dialog" title="PDF 阅览" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizAbout">
import { isExternal } from "@/utils/validate"

const display = ref({
  mode: "TEXT" as "TEXT" | "PDF",
  pdfUrl: ""
})
const previewOpen = ref(false)
const textForm = ref({
  title: "星帆智联",
  subtitle: "连接星空 · 智联未来",
  imageUrl: "",
  content: "<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>"
})

const pdfSrc = computed(() => {
  const url = (display.value.pdfUrl || "").trim()
  if (!url) return ""
  // 外链或 R2 公网地址直接用；相对路径走管理端代理前缀
  if (isExternal(url)) return url
  const path = url.startsWith("/") ? url : `/${url}`
  return import.meta.env.VITE_APP_BASE_API + path
})
</script>

<style scoped>
.field-tip {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.4;
  width: 100%;
}
.form-actions {
  margin-bottom: 8px !important;
}
.form-actions :deep(.el-form-item__content) {
  margin-left: 0 !important;
}
.pdf-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  padding: 20px 24px 12px;
  margin-bottom: 12px;
}
.pdf-panel__row {
  margin-bottom: 8px;
}
.pdf-preview {
  width: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-fill-color-blank);
  min-height: 480px;
  display: flex;
  flex-direction: column;
}
.pdf-preview__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-extra-light);
}
.pdf-preview__frame {
  display: block;
  width: 100%;
  flex: 1;
  min-height: 440px;
  height: 560px;
  border: 0;
  background: #f5f5f5;
}
.pdf-preview__empty {
  flex: 1;
  min-height: 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  background: var(--el-fill-color-lighter);
}
.pdf-preview__frame--dialog {
  height: calc(90vh - 120px);
  min-height: 480px;
}
</style>
