<template>
  <div class="app-container ops-page">
    <el-alert
      title="同一产品可同时配人民币和 USDT。双币按钮由认购价格决定；「跳过二级页」独立控制列表直购。业务模式：日返=按天返利，可另配认购发助力；助力退本=无日返、发助力、到期退本。每人限购填累计可买份数，0 或不填表示不限制。一拖二仅日返模式有效。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <div class="ops-section-card">
      <div class="ops-section-card__hd">产品日返入账</div>
      <div class="ops-section-card__bd">
        <el-form :inline="true" v-loading="creditLoading">
          <el-form-item label="到账钱包">
            <WalletTypeSelect v-model="rebateWalletType" />
            <span class="tip">产品每日返利进这个钱包，默认产品收益</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveCredit" v-hasPermi="['biz:product:edit']">保存</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属系列" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="系列" clearable style="width: 200px">
          <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item label="支持币种" prop="currency">
        <el-select v-model="queryParams.currency" placeholder="支持币种" clearable style="width: 140px">
          <el-option label="人民币" value="CNY" />
          <el-option label="USDT" value="USDT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="上架" value="0" />
          <el-option label="下架" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="在售" prop="onSale">
        <el-select v-model="queryParams.onSale" placeholder="在售" clearable style="width: 140px">
          <el-option label="在售" value="1" />
          <el-option label="停售" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:product:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="productList">
      <el-table-column label="ID" align="center" prop="productId" width="70" />
      <el-table-column label="系列" align="center" prop="categoryName" min-width="140" show-overflow-tooltip />
      <el-table-column label="排序" align="center" prop="sort" width="70" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="120" />
      <el-table-column label="卡片模板" align="center" prop="templateName" min-width="110" show-overflow-tooltip />
      <el-table-column label="业务模式" align="center" prop="bizMode" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.bizMode === 'ASSIST' ? 'warning' : 'success'" size="small">
            {{ scope.row.bizMode === 'ASSIST' ? '助力退本' : '日返' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="人民币价" align="center" prop="priceCny" width="100" />
      <el-table-column label="人民币日返" align="center" prop="dailyRebateCny" width="110" />
      <el-table-column label="USDT价" align="center" prop="priceUsdt" width="90" />
      <el-table-column label="USDT日返" align="center" prop="dailyRebateUsdt" width="100" />
      <el-table-column label="天数" align="center" width="80">
        <template #default="scope">
          <span v-if="scope.row.bizMode === 'ASSIST'">退本{{ scope.row.principalReturnDays || 0 }}天</span>
          <span v-else>{{ scope.row.durationDays }}</span>
        </template>
      </el-table-column>
      <el-table-column label="限购" align="center" prop="buyLimit" width="80">
        <template #default="scope">
          <span>{{ scope.row.buyLimit > 0 ? scope.row.buyLimit + "份" : "不限" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="一拖二" align="center" min-width="120">
        <template #default="scope">
          <span>{{ unlockText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提现指定" align="center" prop="withdrawRequired" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.withdrawRequired === '1' ? 'warning' : 'info'">{{ scope.row.withdrawRequired === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="跳过二级页" align="center" width="110">
        <template #default="scope">
          <el-tag :type="isSkipDetail(scope.row) ? 'warning' : 'info'">{{ isSkipDetail(scope.row) ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在售" align="center" width="80">
        <template #default="scope">
          <el-tag :type="isOnSale(scope.row) ? 'success' : 'info'">{{ isOnSale(scope.row) ? '在售' : '停售' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" fixed="right" class-name="product-ops-col">
        <template #default="scope">
          <div class="product-ops">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:product:edit']">修改</el-button>
            <el-button
              link
              type="primary"
              :icon="isOnSale(scope.row) ? 'CircleClose' : 'CircleCheck'"
              @click="toggleOnSale(scope.row)"
              v-hasPermi="['biz:product:edit']"
            >{{ isOnSale(scope.row) ? '停售' : '开售' }}</el-button>
            <el-button
              link
              type="primary"
              :icon="scope.row.status === '0' ? 'Bottom' : 'Top'"
              @click="toggleStatus(scope.row)"
              v-hasPermi="['biz:product:edit']"
            >{{ scope.row.status === '0' ? '下架' : '上架' }}</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleProductMore(cmd, scope.row)">
              <el-button link type="primary">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    command="sync"
                    :disabled="syncPreviewLoading && syncTargetId === scope.row.productId"
                    v-hasPermi="['biz:product:edit']"
                  >同步快照</el-dropdown-item>
                  <el-dropdown-item command="delete" divided v-hasPermi="['biz:product:remove']">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-drawer :title="title" v-model="open" size="720px" append-to-body destroy-on-close class="product-drawer">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="product-drawer-form">
        <div class="publish-bar">
          <div class="publish-bar__item">
            <span class="publish-bar__label">上架</span>
            <el-switch v-model="form.status" active-value="0" inactive-value="1" />
          </div>
          <div class="publish-bar__item">
            <span class="publish-bar__label">在售</span>
            <el-switch v-model="form.onSale" active-value="1" inactive-value="0" />
            <el-tooltip content="关闭后列表点认购提示暂未开放（同列表「在售」），与上架独立" placement="top">
              <el-icon class="publish-bar__help"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="publish-bar__item">
            <span class="publish-bar__label">跳过二级页</span>
            <el-switch v-model="form.skipDetail" active-value="1" inactive-value="0" />
            <el-tooltip content="开启后列表直接认购，不进详情页；与业务模式无关。双按钮只看是否配了 CNY/USDT 价格" placement="top">
              <el-icon class="publish-bar__help"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <div class="form-section-title">产品是谁</div>
        <el-form-item label="所属系列" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择系列" style="width: 100%" @change="onCategoryChange">
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <div class="text-with-color">
            <el-input v-model="form.productName" placeholder="App 卡片主标题" class="text-with-color__input" />
            <div class="text-with-color__color" title="名称颜色，可空则跟主题色">
              <span class="text-with-color__label">颜色</span>
              <el-color-picker v-model="form.titleColor" color-format="hex" :predefine="themePresets" />
              <el-input v-model="form.titleColor" maxlength="16" clearable placeholder="可空" style="width: 100px" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="英文名" prop="nameEn">
          <div class="text-with-color">
            <el-input v-model="form.nameEn" placeholder="卡片英文名，可空" class="text-with-color__input" />
            <div class="text-with-color__color" title="序号与英文名共用，可空则跟主题色">
              <span class="text-with-color__label">颜色</span>
              <el-color-picker v-model="form.accentColor" color-format="hex" :predefine="themePresets" />
              <el-input v-model="form.accentColor" maxlength="16" clearable placeholder="可空" style="width: 100px" />
            </div>
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="业务模式" prop="bizMode">
              <el-radio-group v-model="form.bizMode">
                <el-radio value="REBATE">日返</el-radio>
                <el-radio value="ASSIST">助力退本</el-radio>
              </el-radio-group>
              <el-tooltip
                content="日返：扣款后按天发返利。助力退本：无日返，认购发助力值，到期退本。列表是否直购看上方「跳过二级页」。"
                placement="top"
              >
                <el-icon class="publish-bar__help" style="margin-left: 8px"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-tabs v-model="drawerTab" class="product-tabs">
          <el-tab-pane label="业务" name="biz">
            <el-form-item label="认购价格">
              <div class="field-with-tip">
                <div class="dual-input">
                  <div class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.priceCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.priceUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
                <p class="field-tip">至少填一种；有 CNY/USDT 价才会出对应认购按钮</p>
              </div>
            </el-form-item>
            <template v-if="form.bizMode === 'ASSIST'">
              <el-form-item label="发放模式">
                <div class="assist-grant-mode">
                  <el-radio-group v-model="form.assistGrantMode" @change="onAssistGrantModeChange">
                    <el-radio value="CNY">固定送 CNY</el-radio>
                    <el-radio value="USDT">固定送 USDT</el-radio>
                    <el-radio value="MATCH">跟认购币种</el-radio>
                    <el-radio value="BOTH">双币都送</el-radio>
                  </el-radio-group>
                  <p class="field-tip">{{ assistGrantModeTip }}</p>
                </div>
              </el-form-item>
              <el-form-item label="助力值">
                <div class="dual-input">
                  <div v-if="showAssistCny" class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.assistValueCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div v-if="showAssistUsdt" class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.assistValueUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="本金返还" prop="principalReturnDays">
                <el-input-number v-model="form.principalReturnDays" :min="1" controls-position="right" style="width: 160px" />
                <span class="field-tip inline">天后回余额</span>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="每日返利">
                <div class="dual-input">
                  <div class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.dailyRebateCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.dailyRebateUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="返利天数" prop="durationDays">
                <el-input-number v-model="form.durationDays" :min="1" controls-position="right" style="width: 180px" />
              </el-form-item>
              <el-form-item label="入账方式">
                <div class="field-with-tip">
                  <el-radio-group v-model="form.incomeMode">
                    <el-radio value="CREDIT">每天进产品收益钱包</el-radio>
                    <el-radio value="ACCUMULATE">订单累计后结算</el-radio>
                    <el-radio value="PROTECT">保护期 + 累计池</el-radio>
                  </el-radio-group>
                  <p class="field-tip">{{ incomeModeTip }}</p>
                </div>
              </el-form-item>
              <template v-if="form.incomeMode === 'ACCUMULATE'">
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="累计周期" prop="accumulateCycleDays" :required="true">
                      <div class="field-with-tip">
                        <div>
                          <el-input-number v-model="form.accumulateCycleDays" :min="1" controls-position="right" style="width: 160px" />
                          <span class="field-tip inline">天</span>
                        </div>
                        <p class="field-tip">如 60</p>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="对档产品" prop="relatedProductId" :required="true">
                      <el-select v-model="form.relatedProductId" filterable clearable placeholder="结算前须持有" style="width: 100%">
                        <el-option
                          v-for="item in relatedProductOptions"
                          :key="item.productId"
                          :label="item.productName"
                          :value="item.productId"
                          :disabled="item.productId === form.productId"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>
              <template v-if="form.incomeMode === 'PROTECT'">
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="保护天数" prop="protectDays" :required="true">
                      <div class="field-with-tip">
                        <div>
                          <el-input-number v-model="form.protectDays" :min="1" controls-position="right" style="width: 160px" />
                          <span class="field-tip inline">天</span>
                        </div>
                        <p class="field-tip">N，如 60。前 N 天日返进产品收益，期内不展示累计</p>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="对档产品" prop="relatedProductId">
                      <div class="field-with-tip">
                        <el-select v-model="form.relatedProductId" filterable clearable placeholder="选填" style="width: 100%">
                          <el-option
                            v-for="item in relatedProductOptions"
                            :key="item.productId"
                            :label="item.productName"
                            :value="item.productId"
                            :disabled="item.productId === form.productId"
                          />
                        </el-select>
                        <p class="field-tip">选填，需要时再配对档产品</p>
                      </div>
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>
              <div class="form-section-title is-follow">认购发助力（选填）</div>
              <el-form-item label="发放模式">
                <div class="assist-grant-mode">
                  <el-radio-group v-model="form.assistGrantMode" @change="onAssistGrantModeChange">
                    <el-radio value="CNY">固定送 CNY</el-radio>
                    <el-radio value="USDT">固定送 USDT</el-radio>
                    <el-radio value="MATCH">跟认购币种</el-radio>
                    <el-radio value="BOTH">双币都送</el-radio>
                  </el-radio-group>
                  <p class="field-tip">深空等日返产品可额外发助力；不填助力值则不发放</p>
                </div>
              </el-form-item>
              <el-form-item label="助力值">
                <div class="dual-input">
                  <div v-if="showAssistCny" class="dual-input__item">
                    <span class="dual-input__tag">CNY</span>
                    <el-input-number v-model="form.assistValueCny" :min="0" :precision="2" controls-position="right" />
                  </div>
                  <div v-if="showAssistUsdt" class="dual-input__item">
                    <span class="dual-input__tag">USDT</span>
                    <el-input-number v-model="form.assistValueUsdt" :min="0" :precision="2" controls-position="right" />
                  </div>
                </div>
              </el-form-item>
            </template>

            <div class="form-section-title is-follow">认购规则</div>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="每人限购" prop="buyLimit">
                  <div class="field-with-tip">
                    <el-input-number v-model="form.buyLimit" :min="0" :step="1" controls-position="right" style="width: 100%" />
                    <p class="field-tip">0 = 不限制</p>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="提现指定">
                  <div class="field-with-tip">
                    <el-radio-group v-model="form.withdrawRequired">
                      <el-radio value="1">是</el-radio>
                      <el-radio value="0">否</el-radio>
                    </el-radio-group>
                    <p class="field-tip">选「是」时，认购后才可提现</p>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
            <template v-if="form.bizMode !== 'ASSIST'">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="一拖二份数" prop="unlockDirectQty">
                    <div class="field-with-tip">
                      <el-input-number v-model="form.unlockDirectQty" :min="0" :step="1" controls-position="right" style="width: 100%" />
                      <p class="field-tip">直推同一产品几份激活上级 1 份；2 = 一拖二。只算直推、同一产品。0 关闭</p>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="等待小时" prop="unlockDelayHours">
                    <div class="field-with-tip">
                      <el-input-number v-model="form.unlockDelayHours" :min="0" :step="1" controls-position="right" style="width: 100%" />
                      <p class="field-tip">达标后再等多少小时开始日返；0 关闭</p>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </template>
          </el-tab-pane>

          <el-tab-pane label="卡片" name="card">
            <p class="section-tip">选模板、封面即可；主题色与单项颜色均可空，单项留空则跟主题色推导。英文名旁的颜色同时作用于序号。</p>
            <el-form-item label="卡片模板" prop="templateId">
              <el-select v-model="form.templateId" placeholder="请选择卡片模板" style="width: 100%" @change="onTemplateChange">
                <el-option
                  v-for="item in templateOptions"
                  :key="item.templateId"
                  :label="item.templateName + '（' + item.templateCode + '）'"
                  :value="item.templateId"
                >
                  <span>{{ item.templateName }}</span>
                  <span style="float: right; color: var(--el-text-color-secondary); font-size: 12px">{{ item.templateCode }} · {{ item.metricSlotCount }}坑</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="封面">
              <image-upload v-model="form.coverUrl" :limit="1" />
              <p class="section-tip" style="margin-top: 6px">
                深空序号卡封面铺满整卡；主题色可空，有值时用于描边和未单独配色时的文字/按钮倾向。
              </p>
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="主题色" prop="theme">
                  <div class="field-with-tip">
                    <div class="theme-color-row">
                      <el-color-picker
                        :model-value="form.theme || null"
                        color-format="hex"
                        clearable
                        :predefine="themePresets"
                        @update:model-value="onThemePick"
                      />
                      <el-input v-model="form.theme" maxlength="16" clearable placeholder="不设置请留空" style="width: 148px" />
                    </div>
                    <p class="field-tip">默认不设置，需要配色再点选</p>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="卡片序号">
                  <el-input v-model="form.cardNo" maxlength="8" placeholder="如 01；颜色同英文名旁" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="角标文案">
              <el-input v-model="form.badgeText" maxlength="64" placeholder="空则用系列名称" />
            </el-form-item>
            <el-form-item label="按钮文案">
              <div class="text-with-color">
                <el-input v-model="form.ctaText" maxlength="32" placeholder="空则用模板默认" class="text-with-color__input" />
                <div class="text-with-color__color" title="按钮背景色">
                  <span class="text-with-color__label">底色</span>
                  <el-color-picker v-model="form.btnColor" color-format="hex" :predefine="themePresets" />
                  <el-input v-model="form.btnColor" maxlength="16" clearable placeholder="可空" style="width: 88px" />
                </div>
                <div class="text-with-color__color" title="按钮文字色，空则白色">
                  <span class="text-with-color__label">字色</span>
                  <el-color-picker v-model="form.btnTextColor" color-format="hex" :predefine="['#FFFFFF', '#0B3A6E', '#1A2030']" />
                  <el-input v-model="form.btnTextColor" maxlength="16" clearable placeholder="可空=白" style="width: 88px" />
                </div>
              </div>
            </el-form-item>
            <el-form-item label="指标区颜色">
              <div class="metric-color-row">
                <div class="text-with-color__color" title="金额/每日收益等标题">
                  <span class="text-with-color__label">标题</span>
                  <el-color-picker v-model="form.labelColor" color-format="hex" :predefine="themePresets" />
                  <el-input v-model="form.labelColor" maxlength="16" clearable placeholder="可空" style="width: 88px" />
                </div>
                <div class="text-with-color__color" title="数值颜色">
                  <span class="text-with-color__label">数值</span>
                  <el-color-picker v-model="form.valueColor" color-format="hex" :predefine="themePresets" />
                  <el-input v-model="form.valueColor" maxlength="16" clearable placeholder="可空" style="width: 88px" />
                </div>
                <div class="text-with-color__color" title="元 / USDT 单位色">
                  <span class="text-with-color__label">单位</span>
                  <el-color-picker v-model="form.unitColor" color-format="hex" :predefine="themePresets" />
                  <el-input v-model="form.unitColor" maxlength="16" clearable placeholder="可空" style="width: 88px" />
                </div>
              </div>
              <p class="field-tip">对应卡片上「金额 / 每日收益」等标签与数字；可空跟主题色</p>
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="文案" name="copy">
            <p class="section-tip">认购详情页展示用，均可空。</p>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="发放方式">
                  <el-input v-model="form.payoutMethod" maxlength="100" placeholder="例如：每日发放" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="风险等级">
                  <el-input v-model="form.riskLevel" maxlength="64" placeholder="例如：中" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注">
              <div class="text-with-color text-with-color--top">
                <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="卡片口号/说明，选填" class="text-with-color__input" />
                <div class="text-with-color__color" title="备注/口号颜色，可空则跟主题色">
                  <span class="text-with-color__label">颜色</span>
                  <el-color-picker v-model="form.remarkColor" color-format="hex" :predefine="themePresets" />
                  <el-input v-model="form.remarkColor" maxlength="16" clearable placeholder="可空" style="width: 100px" />
                </div>
              </div>
            </el-form-item>
            <el-form-item label="激活条件">
              <el-input
                v-model="form.unlockRuleText"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="如：直推下级认购2份相同价格产品"
              />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog
      title="同步持仓快照"
      v-model="syncDialogOpen"
      width="720px"
      append-to-body
      destroy-on-close
      class="sync-snapshot-dialog"
    >
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        class="mb8"
        title="只刷该产品持仓中订单（已完成不碰）。日返：同步后重算激活；保护期延长会纠偏累计池。助力：只同步提现指定/本金返还/助力值展示，不改价格、不补扣助力余额。"
      />
      <div class="sync-summary">
        预计同步 <b>{{ syncPreview.wouldSync ?? 0 }}</b> 单
        <template v-if="syncPreview.holdingCount != null">（持仓 {{ syncPreview.holdingCount }}）</template>
        <template v-if="syncPreview.skippedAssist != null">，跳过助力 {{ syncPreview.skippedAssist }}</template>
        <template v-if="syncPreview.skippedCompleted != null">，跳过已完成 {{ syncPreview.skippedCompleted }}</template>
      </div>
      <el-alert
        v-for="(w, i) in (syncPreview.warnings || [])"
        :key="'w' + i"
        :title="String(w)"
        type="info"
        :closable="false"
        show-icon
        class="mb8"
      />
      <div v-if="syncDiffRows.length" class="sync-diff-wrap">
        <div class="sync-diff-title">抽样差异（最多 {{ syncDiffRows.length }} 条字段）</div>
        <el-table :data="syncDiffRows" size="small" max-height="360" border>
          <el-table-column label="订单" prop="orderKey" min-width="120" show-overflow-tooltip />
          <el-table-column label="字段" prop="field" width="140" show-overflow-tooltip />
          <el-table-column label="当前" prop="from" min-width="120" show-overflow-tooltip />
          <el-table-column label="同步为" prop="to" min-width="120" show-overflow-tooltip />
        </el-table>
      </div>
      <p v-else class="field-tip">暂无抽样差异明细，确认后仍会同步上方预计单数</p>
      <template #footer>
        <el-button @click="syncDialogOpen = false">取 消</el-button>
        <el-button type="primary" :loading="syncSubmitting" @click="confirmSyncOrderSnapshot">确认同步</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="BizProduct">
import { listProduct, getProduct, addProduct, updateProduct, delProduct, previewProductOrderSnapshot, syncProductOrderSnapshot, listProductCategoryOptions, listProductCardTemplateOptions, getWalletCreditByBiz, saveWalletCreditByBiz } from "@/api/biz"
import WalletTypeSelect from "@/views/biz/components/WalletTypeSelect.vue"
import { QuestionFilled } from "@element-plus/icons-vue"

const { proxy } = getCurrentInstance() as any
/** 仅用于色板快捷色，不作为表单默认值 */
const themePresets = ["#2F7BFF", "#7B6BFF", "#C9A227", "#1AA7A0", "#8A94A6"]
/** 旧数据可能存过命名色，转成 hex；未识别则清空 */
const THEME_NAME_HEX: Record<string, string> = {
  purple: "#7B6BFF",
  gold: "#C9A227",
  cyan: "#1AA7A0",
  silver: "#8A94A6"
}

/** 主题色默认为空；仅保留合法 hex，其它（含旧命名 blue）一律清空 */
function normalizeThemeColor(value?: string | null) {
  if (value == null) return ""
  const raw = String(value).trim()
  if (!raw) return ""
  const named = THEME_NAME_HEX[raw.toLowerCase()]
  if (named) return named
  if (/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(raw)) {
    if (raw.length === 4) {
      return `#${raw[1]}${raw[1]}${raw[2]}${raw[2]}${raw[3]}${raw[3]}`.toUpperCase()
    }
    return raw.toUpperCase()
  }
  return ""
}

function onThemePick(val?: string | null) {
  form.value.theme = val ? normalizeThemeColor(val) : ""
}

/** 可选颜色：非法或空一律存空串，App 走 theme 默认 */
function normalizeOptionalColor(value?: string | null) {
  if (value == null) return ""
  const raw = String(value).trim()
  if (!raw) return ""
  if (/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(raw)) {
    if (raw.length === 4) {
      return `#${raw[1]}${raw[1]}${raw[2]}${raw[2]}${raw[3]}${raw[3]}`.toUpperCase()
    }
    return raw.toUpperCase()
  }
  return ""
}

const CARD_COLOR_KEYS = [
  "accentColor",
  "titleColor",
  "remarkColor",
  "labelColor",
  "valueColor",
  "unitColor",
  "btnColor",
  "btnTextColor"
] as const

function normalizeCardColors(target: Record<string, any>) {
  CARD_COLOR_KEYS.forEach((key) => {
    target[key] = normalizeOptionalColor(target[key])
  })
}

const productList = ref<any[]>([])
const categoryOptions = ref<any[]>([])
const relatedProductOptions = ref<any[]>([])
const templateOptions = ref<any[]>([])
const open = ref(false)
const drawerTab = ref("biz")
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const syncDialogOpen = ref(false)
const syncPreviewLoading = ref(false)
const syncSubmitting = ref(false)
const syncTargetId = ref<number | undefined>()
const syncPreview = ref<any>({})
const syncDiffRows = ref<any[]>([])
const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 100, productName: undefined, currency: undefined, status: undefined, onSale: undefined, categoryId: undefined },
  rules: {
    categoryId: [{ required: true, message: "请选择所属系列", trigger: "change" }],
    productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
    bizMode: [{ required: true, message: "请选择业务模式", trigger: "change" }],
    durationDays: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode === "ASSIST") return callback()
        if (value == null || value === "" || Number(value) <= 0) return callback(new Error("返利天数不能为空"))
        callback()
      },
      trigger: "blur"
    }],
    principalReturnDays: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode !== "ASSIST") return callback()
        if (value == null || value === "" || Number(value) <= 0) return callback(new Error("请填写本金返还天数"))
        callback()
      },
      trigger: "blur"
    }],
    accumulateCycleDays: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode === "ASSIST" || form.value.incomeMode !== "ACCUMULATE") return callback()
        if (value == null || value === "" || Number(value) <= 0) return callback(new Error("请填写累计周期天数"))
        callback()
      },
      trigger: "blur"
    }],
    relatedProductId: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode === "ASSIST" || form.value.incomeMode !== "ACCUMULATE") return callback()
        if (value == null || value === "") return callback(new Error("请选择对档产品"))
        callback()
      },
      trigger: "change"
    }],
    protectDays: [{
      validator: (_: any, value: any, callback: any) => {
        if (form.value.bizMode === "ASSIST" || form.value.incomeMode !== "PROTECT") return callback()
        if (value == null || value === "" || Number(value) <= 0) return callback(new Error("请填写保护天数 N（须大于 0）"))
        callback()
      },
      trigger: "blur"
    }],
    templateId: [{ required: true, message: "请选择卡片模板", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const showAssistCny = computed(() => {
  const m = String(form.value.assistGrantMode || "CNY").toUpperCase()
  return m === "CNY" || m === "MATCH" || m === "BOTH"
})
const showAssistUsdt = computed(() => {
  const m = String(form.value.assistGrantMode || "CNY").toUpperCase()
  return m === "USDT" || m === "MATCH" || m === "BOTH"
})
const incomeModeTip = computed(() => {
  const m = String(form.value.incomeMode || "CREDIT").toUpperCase()
  if (m === "ACCUMULATE") return "日返先记在订单上，满累计周期后按对档产品已激活份数手动结算进产品收益"
  if (m === "PROTECT") return "前 N 天日返进产品收益；第 N+1～D 天已激活进产品收益、未激活进累计池；凑齐一拖二并对档条件满足后累计自动转入产品收益"
  return "日返始终进入产品收益钱包"
})
const assistGrantModeTip = computed(() => {
  const m = String(form.value.assistGrantMode || "CNY").toUpperCase()
  if (m === "USDT") return "任意币认购都只发 USDT 助力"
  if (m === "MATCH") return "买什么币就发什么币助力；双币认购时两侧都建议填"
  if (m === "BOTH") return "一次认购同时发 CNY + USDT 助力"
  return "默认：任意币认购都只发 CNY 助力（与人民币 1:1）"
})

function onAssistGrantModeChange(mode: string) {
  const m = String(mode || "CNY").toUpperCase()
  form.value.assistGrantMode = m
  if (m === "CNY") form.value.assistValueUsdt = 0
  if (m === "USDT") form.value.assistValueCny = 0
}
const creditLoading = ref(false)
const rebateWalletType = ref("PRODUCT")

const METRIC_DEFAULTS: Record<string, Array<{ label: string; source: string; customText?: string }>> = {
  CLASSIC: [
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "收益周期", source: "DURATION" }
  ],
  HERO: [
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "收益周期", source: "DURATION" }
  ],
  SPLIT: [
    { label: "金额", source: "PRICE" },
    { label: "助力值", source: "ASSIST_VALUE" },
    { label: "限购", source: "BUY_LIMIT" },
    { label: "本金返还", source: "PRINCIPAL_RETURN" }
  ],
  NUMBERED: [
    { label: "金额", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "助力值", source: "ASSIST_VALUE" },
    { label: "收益周期", source: "DURATION" }
  ],
  ROW: [
    { label: "价格", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" }
  ],
  COMPACT: [
    { label: "金额", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "助力值", source: "ASSIST_VALUE" },
    { label: "收益周期", source: "DURATION" }
  ],
  BANNER: [
    { label: "价格", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" }
  ],
  PRICE_FOCUS: [
    { label: "收益周期", source: "DURATION" },
    { label: "每日收益", source: "DAILY_REBATE" }
  ],
  MEDIA_LEFT: [
    { label: "价格", source: "PRICE" },
    { label: "每日收益", source: "DAILY_REBATE" },
    { label: "收益周期", source: "DURATION" }
  ]
}

function buildDefaultMetrics(templateCode?: string, slotCount?: number) {
  const code = String(templateCode || "CLASSIC").toUpperCase()
  const defs = METRIC_DEFAULTS[code] || METRIC_DEFAULTS.CLASSIC
  const count = slotCount != null ? Number(slotCount) : defs.length
  const rows: any[] = []
  for (let i = 0; i < count; i++) {
    const d = defs[i] || { label: "指标" + (i + 1), source: "CUSTOM", customText: "" }
    rows.push({
      slotIndex: i + 1,
      label: d.label,
      source: d.source,
      customText: d.customText || ""
    })
  }
  return rows
}

function findTemplate(id?: number) {
  return templateOptions.value.find((t: any) => t.templateId === id)
}

function onTemplateChange(templateId: number) {
  const tpl = findTemplate(templateId)
  if (!tpl) return
  const apply = () => {
    form.value.metrics = buildDefaultMetrics(tpl.templateCode, tpl.metricSlotCount)
  }
  if (form.value._skipTemplateConfirm) {
    form.value._skipTemplateConfirm = false
    apply()
    return
  }
  if (form.value.metrics && form.value.metrics.length) {
    proxy.$modal.confirm("切换模板将按该模板默认坑位重新绑定展示字段，是否继续？").then(apply).catch(() => {})
  } else {
    apply()
  }
}

function onCategoryChange(categoryId?: number) {
  if (form.value.productId) return
  applySeriesDefaultTemplate(categoryId)
}

function applySeriesDefaultTemplate(categoryId?: number) {
  if (!categoryId) return
  const cat = categoryOptions.value.find((c: any) => c.categoryId === categoryId)
  if (cat?.defaultTemplateId) {
    form.value.templateId = cat.defaultTemplateId
    const tpl = findTemplate(cat.defaultTemplateId)
    form.value.metrics = buildDefaultMetrics(tpl?.templateCode, tpl?.metricSlotCount)
  } else if (!form.value.templateId && templateOptions.value.length) {
    const classic = templateOptions.value.find((t: any) => t.templateCode === "CLASSIC") || templateOptions.value[0]
    form.value.templateId = classic.templateId
    form.value.metrics = buildDefaultMetrics(classic.templateCode, classic.metricSlotCount)
  }
}

function loadCredit() {
  creditLoading.value = true
  getWalletCreditByBiz("REBATE").then((res: any) => {
    rebateWalletType.value = res.data?.typeCode || "PRODUCT"
  }).finally(() => { creditLoading.value = false })
}
function saveCredit() {
  saveWalletCreditByBiz("REBATE", rebateWalletType.value).then(() => {
    proxy.$modal.msgSuccess("保存成功")
    loadCredit()
  })
}

function loadCategories() {
  listProductCategoryOptions().then((res: any) => {
    categoryOptions.value = res.data || []
  })
}
function loadRelatedProducts() {
  listProduct({ pageNum: 1, pageSize: 500 }).then((res: any) => {
    relatedProductOptions.value = res.rows || []
  }).catch(() => { relatedProductOptions.value = [] })
}
function loadTemplates() {
  listProductCardTemplateOptions().then((res: any) => {
    templateOptions.value = res.data || []
  }).catch(() => { templateOptions.value = [] })
}
function getList() {
  loading.value = true
  listProduct(queryParams.value).then((res: any) => {
    productList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function unlockText(row: any) {
  const qty = Number(row.unlockDirectQty || 0)
  const hours = Number(row.unlockDelayHours || 0)
  if (qty <= 0 && hours <= 0) return "关闭"
  const parts: string[] = []
  if (qty > 0) parts.push("直属" + qty + "份")
  if (hours > 0) parts.push(hours + "小时")
  return parts.join(" / ")
}
function isOnSale(row: any) {
  if (typeof row.onSaleFlag === "boolean") return row.onSaleFlag
  return row.onSale === "1" || row.onSale === 1 || row.onSale === true
}
function isSkipDetail(row: any) {
  if (typeof row.skipDetailFlag === "boolean") return row.skipDetailFlag
  return row.skipDetail === "1" || row.skipDetail === 1 || row.skipDetail === true
}
function normalizeOnSale(data: any) {
  if (data.onSale == null || data.onSale === "") {
    return data.onSaleFlag === false ? "0" : "1"
  }
  return data.onSale === "1" || data.onSale === 1 || data.onSale === true ? "1" : "0"
}
/** 快捷调整：先取详情再整单提交，避免 PUT 缺参 */
function patchProduct(row: any, patch: Record<string, any>) {
  return getProduct(row.productId).then((res: any) => {
    const data = { ...(res.data || {}), ...patch }
    data.onSale = normalizeOnSale(data)
    data.buyLimit = Number(data.buyLimit || 0)
    data.unlockDirectQty = Number(data.unlockDirectQty || 0)
    data.unlockDelayHours = Number(data.unlockDelayHours || 0)
    return updateProduct(data)
  })
}
function toggleOnSale(row: any) {
  const next = isOnSale(row) ? "0" : "1"
  const text = next === "1" ? "开售" : "停售"
  proxy.$modal.confirm(`确认要「${text}」产品「${row.productName}」吗？`).then(() => {
    return patchProduct(row, { onSale: next })
  }).then(() => {
    row.onSale = next
    row.onSaleFlag = next === "1"
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {})
}
function toggleStatus(row: any) {
  const next = row.status === "0" ? "1" : "0"
  const text = next === "0" ? "上架" : "下架"
  proxy.$modal.confirm(`确认要「${text}」产品「${row.productName}」吗？`).then(() => {
    return patchProduct(row, { status: next })
  }).then(() => {
    row.status = next
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {})
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function reset() {
  form.value = {
    status: "0",
    onSale: "1",
    skipDetail: "0",
    bizMode: "REBATE",
    withdrawRequired: "0",
    buyLimit: 0,
    unlockDirectQty: 0,
    unlockDelayHours: 0,
    payoutMethod: "",
    riskLevel: "",
    unlockRuleText: "",
    sort: 0,
    categoryId: undefined,
    nameEn: "",
    coverUrl: "",
    priceCny: undefined,
    dailyRebateCny: undefined,
    priceUsdt: undefined,
    dailyRebateUsdt: undefined,
    assistValueCny: undefined,
    assistValueUsdt: undefined,
    assistGrantMode: "CNY",
    principalReturnDays: undefined,
    durationDays: undefined,
    incomeMode: "CREDIT",
    protectDays: undefined,
    accumulateCycleDays: 0,
    relatedProductId: undefined,
    templateId: undefined,
    theme: "",
    badgeText: "",
    cardNo: "",
    ctaText: "",
    accentColor: "",
    titleColor: "",
    remarkColor: "",
    labelColor: "",
    valueColor: "",
    unitColor: "",
    btnColor: "",
    btnTextColor: "",
    metrics: []
  }
  proxy.resetForm("formRef")
}
function handleAdd() {
  reset()
  drawerTab.value = "biz"
  open.value = true
  title.value = "新增产品"
  nextTick(() => {
    if (categoryOptions.value.length === 1) {
      form.value.categoryId = categoryOptions.value[0].categoryId
      applySeriesDefaultTemplate(form.value.categoryId)
    } else if (templateOptions.value.length) {
      const classic = templateOptions.value.find((t: any) => t.templateCode === "CLASSIC") || templateOptions.value[0]
      form.value.templateId = classic.templateId
      form.value.metrics = buildDefaultMetrics(classic.templateCode, classic.metricSlotCount)
    }
  })
}
function handleUpdate(row: any) {
  reset()
  drawerTab.value = "biz"
  getProduct(row.productId).then((res: any) => {
    form.value = res.data || {}
    if (form.value.onSale == null || form.value.onSale === "") {
      form.value.onSale = form.value.onSaleFlag === false ? "0" : "1"
    } else {
      form.value.onSale = form.value.onSale === "1" || form.value.onSale === 1 || form.value.onSale === true ? "1" : "0"
    }
    form.value.theme = normalizeThemeColor(form.value.theme)
    normalizeCardColors(form.value)
    if (!form.value.bizMode) form.value.bizMode = "REBATE"
    if (!form.value.assistGrantMode) form.value.assistGrantMode = "CNY"
    if (!form.value.incomeMode) form.value.incomeMode = "CREDIT"
    if (form.value.accumulateCycleDays == null) form.value.accumulateCycleDays = 0
    if (form.value.protectDays == null) form.value.protectDays = undefined
    if (form.value.skipDetail == null || form.value.skipDetail === "") {
      form.value.skipDetail = form.value.skipDetailFlag === true ? "1" : "0"
    } else {
      form.value.skipDetail = form.value.skipDetail === "1" || form.value.skipDetail === 1 || form.value.skipDetail === true ? "1" : "0"
    }
    if (!form.value.metrics || !form.value.metrics.length) {
      const tpl = findTemplate(form.value.templateId)
      form.value.metrics = buildDefaultMetrics(tpl?.templateCode || form.value.templateCode, tpl?.metricSlotCount)
    }
    open.value = true
    title.value = "修改产品"
  })
}

const FIELD_LABELS: Record<string, string> = {
  productName: "产品名称",
  dailyRebate: "日返",
  dailyRebateCny: "日返CNY",
  dailyRebateUsdt: "日返USDT",
  durationDays: "总天数",
  withdrawRequired: "提现指定",
  withdrawRequireHold: "提现指定",
  unlockDirectQty: "一拖二",
  unlockDirectNeed: "一拖二",
  unlockDelayHours: "等待小时",
  incomeMode: "入账方式",
  accumulateCycleDays: "累计周期",
  protectDays: "保护天数",
  relatedProductId: "对档产品",
  assistValue: "助力值",
  principalReturnDays: "本金返还天数",
  principalReturnAt: "本金返还时间"
}

const INCOME_MODE_LABELS: Record<string, string> = {
  CREDIT: "每天进产品收益",
  ACCUMULATE: "订单累计后结算",
  PROTECT: "保护期 + 累计池"
}

const YES_NO_LABELS: Record<string, string> = {
  "0": "否",
  "1": "是",
  false: "否",
  true: "是"
}

function formatDiffVal(v: any, field?: string) {
  if (v === null || v === undefined || v === "") return "—"
  if (typeof v === "object") return JSON.stringify(v)
  const s = String(v)
  const f = String(field || "")
  if (f === "incomeMode") {
    const key = s.toUpperCase()
    return INCOME_MODE_LABELS[key] || s
  }
  if (f === "withdrawRequired" || f === "withdrawRequireHold") {
    return YES_NO_LABELS[s] || s
  }
  if ((f === "relatedProductId" || f === "protectDays" || f === "accumulateCycleDays"
      || f === "unlockDirectQty" || f === "unlockDelayHours") && s === "0") {
    return f === "relatedProductId" ? "无" : "0"
  }
  return s
}

function flattenFieldDiffs(preview: any) {
  const rows: any[] = []
  const samples = preview?.sample || preview?.fieldDiffs || preview?.samples || preview?.sampleDiffs || []
  if (!Array.isArray(samples)) return rows
  for (const sample of samples) {
    if (!sample || typeof sample !== "object") continue
    const orderKey = sample.orderNo || sample.orderId || sample.memberId || "—"
    const nested = sample.diffs || sample.fieldDiffs || sample.changes || sample.fields
    if (Array.isArray(nested)) {
      for (const d of nested) {
        if (!d) continue
        const field = d.field || d.name || d.key || "—"
        rows.push({
          orderKey: String(orderKey),
          field: FIELD_LABELS[field] || field,
          from: formatDiffVal(d.from ?? d.oldValue ?? d.before, field),
          to: formatDiffVal(d.to ?? d.newValue ?? d.after, field)
        })
      }
      continue
    }
    if (nested && typeof nested === "object" && !Array.isArray(nested)) {
      for (const [field, diff] of Object.entries(nested as Record<string, any>)) {
        const d: any = diff && typeof diff === "object" ? diff : { from: undefined, to: diff }
        rows.push({
          orderKey: String(orderKey),
          field: FIELD_LABELS[field] || field,
          from: formatDiffVal(d.from ?? d.oldValue ?? d.before ?? (Array.isArray(diff) ? diff[0] : undefined), field),
          to: formatDiffVal(d.to ?? d.newValue ?? d.after ?? (Array.isArray(diff) ? diff[1] : undefined), field)
        })
      }
      continue
    }
    if (sample.field) {
      rows.push({
        orderKey: String(orderKey),
        field: FIELD_LABELS[sample.field] || sample.field,
        from: formatDiffVal(sample.from ?? sample.oldValue ?? sample.before, sample.field),
        to: formatDiffVal(sample.to ?? sample.newValue ?? sample.after, sample.field)
      })
    }
  }
  return rows
}

function handleProductMore(command: string, row: any) {
  if (command === "sync") handleSyncOrderSnapshot(row)
  else if (command === "delete") handleDelete(row)
}

function handleSyncOrderSnapshot(row: any) {
  const productId = row?.productId
  if (!productId) return
  syncTargetId.value = productId
  syncPreviewLoading.value = true
  previewProductOrderSnapshot(productId, 20).then((res: any) => {
    const data = res?.data || {}
    const wouldSync = Number(data.wouldSync ?? 0)
    if (wouldSync <= 0) {
      proxy.$modal.msgSuccess("持仓快照已与产品配置一致，无需同步")
      return
    }
    syncPreview.value = data
    syncDiffRows.value = flattenFieldDiffs(data)
    syncDialogOpen.value = true
  }).finally(() => {
    syncPreviewLoading.value = false
  })
}

function confirmSyncOrderSnapshot() {
  const productId = syncTargetId.value
  if (!productId) return
  syncSubmitting.value = true
  syncProductOrderSnapshot(productId).then((res: any) => {
    const data = res?.data || {}
    const n = data.synced ?? data.updated ?? data.wouldSync ?? syncPreview.value.wouldSync
    proxy.$modal.msgSuccess(res?.msg || ("已同步 " + (n != null ? n : "") + " 单持仓快照"))
    syncDialogOpen.value = false
  }).finally(() => {
    syncSubmitting.value = false
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid: boolean, fields?: Record<string, any>) => {
    if (!valid) {
      const keys = Object.keys(fields || {})
      if (keys.some((k) => ["durationDays", "principalReturnDays", "buyLimit"].includes(k))) {
        drawerTab.value = "biz"
      } else if (keys.includes("templateId")) {
        drawerTab.value = "card"
      }
      return
    }
    const cny = Number(form.value.priceCny || 0)
    const usdt = Number(form.value.priceUsdt || 0)
    if (cny <= 0 && usdt <= 0) {
      proxy.$modal.msgError("请至少配置人民币或USDT认购价格")
      return
    }
    form.value.theme = normalizeThemeColor(form.value.theme)
    normalizeCardColors(form.value)
    form.value.bizMode = form.value.bizMode === "ASSIST" ? "ASSIST" : "REBATE"
    form.value.skipDetail = form.value.skipDetail === "1" || form.value.skipDetail === true ? "1" : "0"
    if (form.value.bizMode === "ASSIST") {
      const mode = String(form.value.assistGrantMode || "CNY").toUpperCase()
      form.value.assistGrantMode = ["CNY", "USDT", "MATCH", "BOTH"].includes(mode) ? mode : "CNY"
      const assistCny = Number(form.value.assistValueCny || 0)
      const assistUsdt = Number(form.value.assistValueUsdt || 0)
      if (form.value.assistGrantMode === "CNY" && assistCny <= 0) {
        proxy.$modal.msgError("固定送 CNY 时请填写 CNY 助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "USDT" && assistUsdt <= 0) {
        proxy.$modal.msgError("固定送 USDT 时请填写 USDT 助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "BOTH" && (assistCny <= 0 || assistUsdt <= 0)) {
        proxy.$modal.msgError("双币都送时请同时填写 CNY 与 USDT 助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "MATCH" && assistCny <= 0 && assistUsdt <= 0) {
        proxy.$modal.msgError("跟认购币种时请至少填写一种币种助力值")
        drawerTab.value = "biz"
        return
      }
      if (form.value.assistGrantMode === "CNY") form.value.assistValueUsdt = 0
      if (form.value.assistGrantMode === "USDT") form.value.assistValueCny = 0
      if (!form.value.principalReturnDays || Number(form.value.principalReturnDays) <= 0) {
        proxy.$modal.msgError("请填写本金返还天数")
        drawerTab.value = "biz"
        return
      }
      form.value.dailyRebateCny = 0
      form.value.dailyRebateUsdt = 0
      form.value.durationDays = Number(form.value.principalReturnDays)
      form.value.unlockDirectQty = 0
      form.value.unlockDelayHours = 0
      form.value.incomeMode = "CREDIT"
      form.value.protectDays = undefined
      form.value.relatedProductId = undefined
    } else {
      const incomeMode = String(form.value.incomeMode || "CREDIT").toUpperCase()
      if (incomeMode === "PROTECT") {
        form.value.incomeMode = "PROTECT"
        form.value.protectDays = Number(form.value.protectDays || 0)
      } else if (incomeMode === "ACCUMULATE") {
        form.value.incomeMode = "ACCUMULATE"
        form.value.protectDays = undefined
        form.value.accumulateCycleDays = Number(form.value.accumulateCycleDays || 0)
      } else {
        form.value.incomeMode = "CREDIT"
        form.value.protectDays = undefined
        form.value.relatedProductId = undefined
      }
      const mode = String(form.value.assistGrantMode || "CNY").toUpperCase()
      form.value.assistGrantMode = ["CNY", "USDT", "MATCH", "BOTH"].includes(mode) ? mode : "CNY"
      const assistCny = Number(form.value.assistValueCny || 0)
      const assistUsdt = Number(form.value.assistValueUsdt || 0)
      if (assistCny > 0 || assistUsdt > 0) {
        if (form.value.assistGrantMode === "CNY" && assistCny <= 0) {
          proxy.$modal.msgError("固定送 CNY 时请填写 CNY 助力值")
          drawerTab.value = "biz"
          return
        }
        if (form.value.assistGrantMode === "USDT" && assistUsdt <= 0) {
          proxy.$modal.msgError("固定送 USDT 时请填写 USDT 助力值")
          drawerTab.value = "biz"
          return
        }
        if (form.value.assistGrantMode === "BOTH" && (assistCny <= 0 || assistUsdt <= 0)) {
          proxy.$modal.msgError("双币都送时请同时填写 CNY 与 USDT 助力值")
          drawerTab.value = "biz"
          return
        }
        if (form.value.assistGrantMode === "CNY") form.value.assistValueUsdt = 0
        if (form.value.assistGrantMode === "USDT") form.value.assistValueCny = 0
      } else {
        form.value.assistValueCny = 0
        form.value.assistValueUsdt = 0
      }
    }
    form.value.buyLimit = Number(form.value.buyLimit || 0)
    form.value.unlockDirectQty = Number(form.value.unlockDirectQty || 0)
    form.value.unlockDelayHours = Number(form.value.unlockDelayHours || 0)
    form.value.onSale = form.value.onSale === "1" || form.value.onSale === true ? "1" : "0"
    const req = form.value.productId ? updateProduct(form.value) : addProduct(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除产品编号为"' + row.productId + '"的数据项？').then(() => delProduct(row.productId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
loadCategories()
loadRelatedProducts()
loadTemplates()
getList()
loadCredit()
</script>

<style scoped>
.tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.product-drawer-form {
  padding: 0 2px 8px;
}
.publish-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
  align-items: center;
  margin: 0 0 16px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}
.publish-bar__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.publish-bar__label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}
.publish-bar__help {
  color: var(--el-text-color-secondary);
  cursor: help;
  font-size: 14px;
}
.product-tabs {
  margin-top: 4px;
}
.product-tabs :deep(.el-tabs__header) {
  margin-bottom: 14px;
}
.product-tabs :deep(.el-tab-pane) {
  padding-bottom: 4px;
}
.form-section-title {
  display: flex;
  align-items: center;
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.form-section-title.is-follow {
  margin-top: 18px;
}
.form-section-title::after {
  content: "";
  flex: 1;
  height: 1px;
  margin-left: 12px;
  background: var(--el-border-color-lighter);
}
.product-drawer-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.field-tip,
.section-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
.section-tip {
  margin: 0 0 14px;
}
.field-tip.inline {
  margin: 0 0 0 10px;
}
.switch-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  width: 100%;
}
.dual-input {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  width: 100%;
}
.dual-input__item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.dual-input__tag {
  flex: 0 0 40px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  text-align: right;
}
.dual-input__item :deep(.el-input-number) {
  flex: 1;
  width: 100%;
}
.assist-grant-mode {
  width: 100%;
}
.assist-grant-mode .field-tip,
.field-with-tip .field-tip {
  margin-top: 8px;
  margin-bottom: 0;
}
.field-with-tip {
  width: 100%;
}
.theme-color-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.text-with-color {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-width: 0;
}
.text-with-color--top {
  align-items: flex-start;
}
.text-with-color__input {
  flex: 1;
  min-width: 0;
}
.text-with-color__color {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}
.text-with-color__label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.metric-color-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
  width: 100%;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.sync-summary {
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--el-text-color-primary);
}
.sync-diff-wrap {
  margin-top: 8px;
}
.sync-diff-title {
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}
.product-ops {
  display: inline-flex;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
  gap: 0;
}
.product-ops :deep(.el-button) {
  margin-left: 0;
  padding: 4px 6px;
}
</style>
