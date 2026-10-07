package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.domain.BizOrderSnapshotSyncResult;
import com.ruoyi.biz.domain.BizOrderSnapshotSyncResult.BizOrderSnapshotFieldDiff;
import com.ruoyi.biz.domain.BizOrderSnapshotSyncResult.BizOrderSnapshotPreview;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.domain.BizProductCardMetric;
import com.ruoyi.biz.domain.BizProductCardTemplate;
import com.ruoyi.biz.domain.BizProductCategory;
import com.ruoyi.biz.mapper.BizOrderMapper;
import com.ruoyi.biz.mapper.BizProductCardMetricMapper;
import com.ruoyi.biz.mapper.BizProductCardTemplateMapper;
import com.ruoyi.biz.mapper.BizProductCategoryMapper;
import com.ruoyi.biz.mapper.BizProductMapper;
import com.ruoyi.biz.service.IBizOrderService;
import com.ruoyi.biz.service.IBizProductService;
import com.ruoyi.biz.support.ProductCardMetricSupport;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizProductServiceImpl implements IBizProductService
{
    @Autowired
    private BizProductMapper productMapper;

    @Autowired
    private BizProductCardMetricMapper metricMapper;

    @Autowired
    private BizProductCardTemplateMapper templateMapper;

    @Autowired
    private BizProductCategoryMapper categoryMapper;

    @Autowired
    private BizOrderMapper orderMapper;

    @Autowired
    private IBizOrderService orderService;

    @Override
    public BizProduct selectProductById(Long productId)
    {
        BizProduct product = productMapper.selectProductById(productId);
        if (product != null)
        {
            product.setMetrics(metricMapper.selectByProductId(productId));
            fillLayoutFields(product, false);
        }
        return product;
    }

    @Override
    public List<BizProduct> selectProductList(BizProduct product)
    {
        return productMapper.selectProductList(product);
    }

    @Override
    public void enrichForApp(List<BizProduct> products)
    {
        if (products == null || products.isEmpty())
        {
            return;
        }
        List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < products.size(); i++)
        {
            BizProduct p = products.get(i);
            if (p.getProductId() != null)
            {
                ids.add(p.getProductId());
            }
        }
        Map<Long, List<BizProductCardMetric>> metricMap = new HashMap<Long, List<BizProductCardMetric>>();
        if (!ids.isEmpty())
        {
            List<BizProductCardMetric> all = metricMapper.selectByProductIds(ids);
            for (int i = 0; i < all.size(); i++)
            {
                BizProductCardMetric m = all.get(i);
                List<BizProductCardMetric> bucket = metricMap.get(m.getProductId());
                if (bucket == null)
                {
                    bucket = new ArrayList<BizProductCardMetric>();
                    metricMap.put(m.getProductId(), bucket);
                }
                bucket.add(m);
            }
        }
        for (int i = 0; i < products.size(); i++)
        {
            BizProduct p = products.get(i);
            List<BizProductCardMetric> metrics = metricMap.get(p.getProductId());
            if (metrics == null || metrics.isEmpty())
            {
                metrics = ProductCardMetricSupport.defaultMetrics(p.getTemplateCode());
            }
            p.setMetrics(metrics);
            fillLayoutFields(p, true);
        }
    }

    @Override
    public void enrichForApp(BizProduct product)
    {
        if (product == null)
        {
            return;
        }
        List<BizProductCardMetric> metrics = metricMapper.selectByProductId(product.getProductId());
        if (metrics == null || metrics.isEmpty())
        {
            metrics = ProductCardMetricSupport.defaultMetrics(product.getTemplateCode());
        }
        product.setMetrics(metrics);
        fillLayoutFields(product, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProduct(BizProduct product)
    {
        if (product.getStatus() == null)
        {
            product.setStatus(BizConstants.STATUS_OK);
        }
        if (product.getWithdrawRequired() == null)
        {
            product.setWithdrawRequired("0");
        }
        if (product.getNameEn() == null)
        {
            product.setNameEn("");
        }
        if (product.getCoverUrl() == null)
        {
            product.setCoverUrl("");
        }
        if (product.getBuyLimit() == null || product.getBuyLimit().intValue() < 0)
        {
            product.setBuyLimit(Integer.valueOf(0));
        }
        if (product.getUnlockDirectQty() == null || product.getUnlockDirectQty().intValue() < 0)
        {
            product.setUnlockDirectQty(Integer.valueOf(0));
        }
        if (product.getUnlockDelayHours() == null || product.getUnlockDelayHours().intValue() < 0)
        {
            product.setUnlockDelayHours(Integer.valueOf(0));
        }
        if (product.getUnlockRuleText() == null)
        {
            product.setUnlockRuleText("");
        }
        if (!"0".equals(product.getOnSale()))
        {
            product.setOnSale("1");
        }
        if (!"1".equals(product.getSkipDetail()))
        {
            product.setSkipDetail("0");
        }
        fillCardDefaults(product);
        normalizeIncomeFields(product);
        normalizeAssistGrantFields(product);
        fillDualPrices(product);
        int rows = productMapper.insertProduct(product);
        saveMetrics(product);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProduct(BizProduct product)
    {
        if (product.getUnlockDirectQty() != null && product.getUnlockDirectQty().intValue() < 0)
        {
            product.setUnlockDirectQty(Integer.valueOf(0));
        }
        if (product.getUnlockDelayHours() != null && product.getUnlockDelayHours().intValue() < 0)
        {
            product.setUnlockDelayHours(Integer.valueOf(0));
        }
        if (product.getOnSale() != null && !"0".equals(product.getOnSale()) && !"1".equals(product.getOnSale()))
        {
            product.setOnSale("1");
        }
        if (product.getSkipDetail() != null && !"0".equals(product.getSkipDetail()) && !"1".equals(product.getSkipDetail()))
        {
            product.setSkipDetail("0");
        }
        fillCardDefaults(product);
        normalizeIncomeFields(product);
        normalizeAssistGrantFields(product);
        fillDualPrices(product);
        if (product.getRelatedProductId() == null)
        {
            if (product.getParams() == null)
            {
                product.setParams(new java.util.HashMap<String, Object>());
            }
            product.getParams().put("clearRelatedProduct", Boolean.TRUE);
        }
        int rows = productMapper.updateProduct(product);
        if (product.getMetrics() != null)
        {
            saveMetrics(product);
        }
        return rows;
    }

    private void normalizeIncomeFields(BizProduct product)
    {
        if (product.assistMode())
        {
            product.setIncomeMode(BizConstants.INCOME_MODE_CREDIT);
            product.setAccumulateCycleDays(Integer.valueOf(0));
            product.setProtectDays(Integer.valueOf(0));
            product.setRelatedProductId(null);
            return;
        }
        String mode = product.getIncomeMode();
        if (mode == null || mode.trim().isEmpty())
        {
            mode = BizConstants.INCOME_MODE_CREDIT;
        }
        mode = mode.trim().toUpperCase();
        if (BizConstants.INCOME_MODE_ACCUMULATE.equals(mode))
        {
            int cycle = product.getAccumulateCycleDays() == null ? 0 : product.getAccumulateCycleDays().intValue();
            if (cycle <= 0)
            {
                throw new ServiceException("订单累计模式请填写累计周期天数");
            }
            if (product.getRelatedProductId() == null)
            {
                throw new ServiceException("订单累计模式请选择对档产品");
            }
            if (product.getProductId() != null && product.getRelatedProductId().equals(product.getProductId()))
            {
                throw new ServiceException("对档产品不能是自身");
            }
            product.setIncomeMode(BizConstants.INCOME_MODE_ACCUMULATE);
            product.setAccumulateCycleDays(Integer.valueOf(cycle));
            product.setProtectDays(Integer.valueOf(0));
        }
        else if (BizConstants.INCOME_MODE_PROTECT.equals(mode))
        {
            int protect = product.getProtectDays() == null ? 0 : product.getProtectDays().intValue();
            if (protect <= 0)
            {
                throw new ServiceException("保护期+累计池请填写保护天数（前N天日返进产品收益）");
            }
            product.setIncomeMode(BizConstants.INCOME_MODE_PROTECT);
            product.setProtectDays(Integer.valueOf(protect));
            product.setAccumulateCycleDays(Integer.valueOf(0));
            product.setRelatedProductId(null);
        }
        else
        {
            product.setIncomeMode(BizConstants.INCOME_MODE_CREDIT);
            product.setAccumulateCycleDays(Integer.valueOf(0));
            product.setProtectDays(Integer.valueOf(0));
            product.setRelatedProductId(null);
        }
    }

    /**
     * ASSIST：助力值必填；REBATE：选填，填了认购时额外发助力（深空等）。
     */
    private void normalizeAssistGrantFields(BizProduct product)
    {
        boolean assistMode = product.assistMode();
        boolean hasCny = product.getAssistValueCny() != null && product.getAssistValueCny().compareTo(BigDecimal.ZERO) > 0;
        boolean hasUsdt = product.getAssistValueUsdt() != null && product.getAssistValueUsdt().compareTo(BigDecimal.ZERO) > 0;
        if (assistMode)
        {
            if (product.getPrincipalReturnDays() == null || product.getPrincipalReturnDays().intValue() <= 0)
            {
                throw new ServiceException("助力模式请填写本金返还天数");
            }
            validateAssistGrantAmounts(product, true, hasCny, hasUsdt);
            if (product.getDailyRebateCny() == null)
            {
                product.setDailyRebateCny(BigDecimal.ZERO);
            }
            if (product.getDailyRebateUsdt() == null)
            {
                product.setDailyRebateUsdt(BigDecimal.ZERO);
            }
            if (product.getDurationDays() == null || product.getDurationDays().intValue() <= 0)
            {
                product.setDurationDays(product.getPrincipalReturnDays());
            }
            product.setUnlockDirectQty(Integer.valueOf(0));
            product.setUnlockDelayHours(Integer.valueOf(0));
            return;
        }
        // 日返：未配助力则清零；配了则按发放模式校验
        if (!hasCny && !hasUsdt)
        {
            product.setAssistValueCny(BigDecimal.ZERO);
            product.setAssistValueUsdt(BigDecimal.ZERO);
            if (product.getAssistGrantMode() == null || product.getAssistGrantMode().trim().isEmpty())
            {
                product.setAssistGrantMode(BizConstants.ASSIST_GRANT_CNY);
            }
            return;
        }
        validateAssistGrantAmounts(product, false, hasCny, hasUsdt);
    }

    private void validateAssistGrantAmounts(BizProduct product, boolean required, boolean hasCny, boolean hasUsdt)
    {
        String grantMode = product.resolveAssistGrantMode();
        product.setAssistGrantMode(grantMode);
        if ("CNY".equals(grantMode) && !hasCny)
        {
            throw new ServiceException(required ? "发放模式为固定CNY时请填写CNY助力值" : "已选固定送CNY，请填写CNY助力值或清空发放配置");
        }
        if ("USDT".equals(grantMode) && !hasUsdt)
        {
            throw new ServiceException(required ? "发放模式为固定USDT时请填写USDT助力值" : "已选固定送USDT，请填写USDT助力值或清空");
        }
        if ("BOTH".equals(grantMode) && (!hasCny || !hasUsdt))
        {
            throw new ServiceException("发放模式为双币都送时请同时填写CNY与USDT助力值");
        }
        if ("MATCH".equals(grantMode) && !hasCny && !hasUsdt)
        {
            throw new ServiceException("发放模式为跟认购币时请至少填写一种币种助力值");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductByIds(Long[] productIds)
    {
        if (productIds != null)
        {
            for (int i = 0; i < productIds.length; i++)
            {
                metricMapper.deleteByProductId(productIds[i]);
            }
        }
        return productMapper.deleteProductByIds(productIds);
    }

    private void fillCardDefaults(BizProduct product)
    {
        if (product.getTemplateId() == null)
        {
            Long fromCategory = null;
            if (product.getCategoryId() != null)
            {
                BizProductCategory category = categoryMapper.selectCategoryById(product.getCategoryId());
                if (category != null)
                {
                    fromCategory = category.getDefaultTemplateId();
                }
            }
            if (fromCategory != null)
            {
                product.setTemplateId(fromCategory);
            }
            else
            {
                BizProductCardTemplate classic = templateMapper.selectTemplateByCode(ProductCardMetricSupport.CODE_CLASSIC);
                if (classic != null)
                {
                    product.setTemplateId(classic.getTemplateId());
                }
            }
        }
        if (product.getTemplateId() != null)
        {
            BizProductCardTemplate template = templateMapper.selectTemplateById(product.getTemplateId());
            if (template == null)
            {
                throw new ServiceException("卡片模板不存在");
            }
            product.setTemplateCode(template.getTemplateCode());
            product.setTemplateName(template.getTemplateName());
            product.setHasMainAmount(template.getHasMainAmount());
        }
        if (StringUtils.isEmpty(product.getTheme()))
        {
            product.setTheme("blue");
        }
        if (StringUtils.isEmpty(product.getSkipDetail()) || (!"0".equals(product.getSkipDetail()) && !"1".equals(product.getSkipDetail())))
        {
            product.setSkipDetail("0");
        }
        if (StringUtils.isEmpty(product.getBizMode()))
        {
            product.setBizMode(BizConstants.BIZ_MODE_REBATE);
        }
        else
        {
            product.setBizMode(product.getBizMode().trim().toUpperCase());
        }
        if (product.getAssistValueCny() == null)
        {
            product.setAssistValueCny(BigDecimal.ZERO);
        }
        if (product.getAssistValueUsdt() == null)
        {
            product.setAssistValueUsdt(BigDecimal.ZERO);
        }
        if (product.getPrincipalReturnDays() == null || product.getPrincipalReturnDays().intValue() < 0)
        {
            product.setPrincipalReturnDays(Integer.valueOf(0));
        }
        if (product.getBadgeText() == null)
        {
            product.setBadgeText("");
        }
        if (product.getCardNo() == null)
        {
            product.setCardNo("");
        }
        if (product.getCtaText() == null)
        {
            product.setCtaText("");
        }
        if (product.getMetrics() == null || product.getMetrics().isEmpty())
        {
            product.setMetrics(ProductCardMetricSupport.defaultMetrics(product.getTemplateCode()));
        }
    }

    private void saveMetrics(BizProduct product)
    {
        if (product.getProductId() == null)
        {
            return;
        }
        metricMapper.deleteByProductId(product.getProductId());
        List<BizProductCardMetric> metrics = product.getMetrics();
        if (metrics == null || metrics.isEmpty())
        {
            metrics = ProductCardMetricSupport.defaultMetrics(product.getTemplateCode());
        }
        List<BizProductCardMetric> batch = new ArrayList<BizProductCardMetric>();
        for (int i = 0; i < metrics.size(); i++)
        {
            BizProductCardMetric m = metrics.get(i);
            if (m == null)
            {
                continue;
            }
            BizProductCardMetric row = new BizProductCardMetric();
            row.setProductId(product.getProductId());
            row.setSlotIndex(m.getSlotIndex() != null ? m.getSlotIndex() : Integer.valueOf(i + 1));
            row.setLabel(StringUtils.isEmpty(m.getLabel()) ? "" : m.getLabel());
            row.setSource(StringUtils.isEmpty(m.getSource()) ? ProductCardMetricSupport.SOURCE_CUSTOM : m.getSource().trim().toUpperCase());
            row.setCustomText(m.getCustomText() == null ? "" : m.getCustomText());
            if (ProductCardMetricSupport.SOURCE_CUSTOM.equals(row.getSource()) && StringUtils.isEmpty(row.getCustomText()))
            {
                // 允许空自定义，App 侧显示 --
            }
            batch.add(row);
        }
        if (!batch.isEmpty())
        {
            metricMapper.insertMetricBatch(batch);
        }
    }

    private void fillLayoutFields(BizProduct product, boolean forApp)
    {
        String code = ProductCardMetricSupport.normalizeCode(product.getTemplateCode());
        product.setTemplateCode(code);
        product.setLayoutType(code);
        if (StringUtils.isEmpty(product.getTheme()))
        {
            product.setTheme("blue");
        }
        if (forApp)
        {
            String badge = product.getBadgeText();
            if (StringUtils.isEmpty(badge))
            {
                badge = product.getCategoryName();
            }
            product.setBadgeText(badge == null ? "" : badge);
            String cta = product.getCtaText();
            if (StringUtils.isEmpty(cta))
            {
                cta = StringUtils.isNotEmpty(product.getTemplateDefaultCta())
                    ? product.getTemplateDefaultCta() : "立即参与";
            }
            product.setCtaText(cta);
            product.setMainAmountDisplay(ProductCardMetricSupport.formatMainAmount(product));
            product.setMetrics(ProductCardMetricSupport.withDisplay(product, product.getMetrics()));
        }
    }

    private void fillDualPrices(BizProduct product)
    {
        boolean cny = BizProduct.hasPrice(product.getPriceCny());
        boolean usdt = BizProduct.hasPrice(product.getPriceUsdt());
        if (!cny && !usdt)
        {
            throw new ServiceException("请至少配置人民币或USDT认购价格");
        }
        if (cny && product.getDailyRebateCny() == null)
        {
            product.setDailyRebateCny(BigDecimal.ZERO);
        }
        if (usdt && product.getDailyRebateUsdt() == null)
        {
            product.setDailyRebateUsdt(BigDecimal.ZERO);
        }
        if (cny)
        {
            product.setPrice(product.getPriceCny());
            product.setDailyRebate(product.getDailyRebateCny());
            product.setCurrency(BizConstants.CURRENCY_CNY);
        }
        else
        {
            product.setPrice(product.getPriceUsdt());
            product.setDailyRebate(product.getDailyRebateUsdt());
            product.setCurrency(BizConstants.CURRENCY_USDT);
        }
    }

    @Override
    public BizOrderSnapshotSyncResult previewOrderSnapshot(Long productId, Integer sampleLimit)
    {
        return buildSnapshotSync(productId, sampleLimit, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizOrderSnapshotSyncResult syncOrderSnapshot(Long productId, Boolean confirm)
    {
        if (!Boolean.TRUE.equals(confirm))
        {
            throw new ServiceException("请确认后同步持仓快照");
        }
        return buildSnapshotSync(productId, Integer.valueOf(20), true);
    }


    private BizOrderSnapshotSyncResult buildSnapshotSync(Long productId, Integer sampleLimit, boolean apply)
    {
        BizProduct product = selectProductById(productId);
        if (product == null)
        {
            throw new ServiceException("产品不存在");
        }
        int limit = sampleLimit == null ? 20 : sampleLimit.intValue();
        if (limit < 1)
        {
            limit = 20;
        }
        if (limit > 50)
        {
            limit = 50;
        }
        BizOrder query = new BizOrder();
        query.setProductId(productId);
        query.setStatus(BizConstants.ORDER_HOLDING);
        List<BizOrder> list = orderMapper.selectOrderList(query);
        if (list == null)
        {
            list = new ArrayList<BizOrder>();
        }

        BizOrderSnapshotSyncResult result = new BizOrderSnapshotSyncResult();
        result.setProductId(product.getProductId());
        result.setProductName(product.getProductName());
        result.setStatusFilter(BizConstants.ORDER_HOLDING);
        result.setSampleLimit(Integer.valueOf(limit));
        if (product.assistMode())
        {
            result.getWarnings().add("助力产品：只同步提现指定、本金返还天数、助力值展示快照；不改价格、不补扣已发助力");
            result.getWarnings().add("每人限购、发放模式改产品即可，不对持仓单做快照同步");
            result.getWarnings().add("未退本订单会按「下单时间+新返还天数」重算到期时间；已退本不动");
        }
        else
        {
            result.getWarnings().add("日返持仓：同步规则快照后会按新一拖二重算激活（含撤销不再达标的已激活份；已发日返不回扣）");
            result.getWarnings().add("保护期延长时，累计池中本应进保护期的金额会转入产品收益；缩短保护期不回扣");
            result.getWarnings().add("不改剩余天数、已发日返日期；日返金额变更只影响之后发放");
        }

        int skipAssist = 0;
        int skipSame = 0;
        int wouldSync = 0;
        int synced = 0;
        Set<String> unlockKeys = new HashSet<String>();
        for (int i = 0; i < list.size(); i++)
        {
            BizOrder order = list.get(i);
            BizOrderSnapshotPreview row;
            if (product.assistMode())
            {
                row = diffAssist(product, order);
            }
            else if (order.assistMode())
            {
                row = new BizOrderSnapshotPreview();
                row.setOrderId(order.getOrderId());
                row.setOrderNo(order.getOrderNo());
                row.setMemberId(order.getMemberId());
                row.setPhone(order.getPhone());
                row.setChanged(Boolean.FALSE);
                row.setAction("skip_assist");
                row.getWarnings().add("日返产品下的助力单不参与本次同步");
                skipAssist++;
                if (result.getSample().size() < limit)
                {
                    result.getSample().add(row);
                }
                continue;
            }
            else
            {
                row = diffRebate(product, order);
            }
            if (Boolean.TRUE.equals(row.getChanged()))
            {
                wouldSync++;
                if (apply)
                {
                    if (product.assistMode())
                    {
                        applyAssistSnapshot(product, order);
                    }
                    else
                    {
                        int oldProtect = nz(order.getProtectDays());
                        SnapshotTarget target = rebateTargetOf(product, order);
                        applyRebateSnapshot(product, order, target);
                        if (BizConstants.INCOME_MODE_PROTECT.equals(target.incomeMode)
                                && target.protectDays > oldProtect)
                        {
                            BigDecimal moved = orderService.correctProtectPoolAfterExtend(
                                    order.getOrderId(), oldProtect, target.protectDays);
                            if (moved != null && moved.compareTo(BigDecimal.ZERO) > 0)
                            {
                                row.getWarnings().add("保护期延长纠偏：累计池转入产品收益 " + moved.toPlainString());
                            }
                        }
                        if (order.getMemberId() != null && order.getProductId() != null)
                        {
                            unlockKeys.add(order.getMemberId() + ":" + order.getProductId());
                        }
                    }
                    row.setAction("synced");
                    synced++;
                }
            }
            else
            {
                skipSame++;
            }
            if (result.getSample().size() < limit)
            {
                result.getSample().add(row);
            }
        }
        if (apply && !unlockKeys.isEmpty())
        {
            for (String key : unlockKeys)
            {
                int p = key.indexOf(':');
                Long memberId = Long.valueOf(key.substring(0, p));
                Long pid = Long.valueOf(key.substring(p + 1));
                orderService.refreshUnlockForSnapshot(memberId, pid);
            }
        }
        result.setTotalMatched(Integer.valueOf(list.size()));
        result.setWouldSync(Integer.valueOf(wouldSync));
        result.setSkipSame(Integer.valueOf(skipSame));
        result.setSkipAssist(Integer.valueOf(skipAssist));
        if (apply)
        {
            result.setSynced(Integer.valueOf(synced));
            result.setAction(synced > 0 ? "synced" : "skip_same");
        }
        else
        {
            result.setAction("preview");
        }
        return result;
    }

    private BizOrderSnapshotPreview diffRebate(BizProduct product, BizOrder order)
    {
        BizOrderSnapshotPreview row = new BizOrderSnapshotPreview();
        row.setOrderId(order.getOrderId());
        row.setOrderNo(order.getOrderNo());
        row.setMemberId(order.getMemberId());
        row.setPhone(order.getPhone());
        SnapshotTarget target = rebateTargetOf(product, order);
        addDiff(row, "productName", text(order.getProductName()), text(target.productName), false);
        addDiff(row, "dailyRebate", dec(order.getDailyRebate()), dec(target.dailyRebate), true);
        addDiff(row, "durationDays", num(order.getDurationDays()), num(target.durationDays), true);
        addDiff(row, "withdrawRequired", text(order.getWithdrawRequired()), text(target.withdrawRequired), false);
        addDiff(row, "unlockDirectQty", num(order.getUnlockDirectQty()), num(target.unlockDirectQty), true);
        addDiff(row, "unlockDelayHours", num(order.getUnlockDelayHours()), num(target.unlockDelayHours), true);
        addDiff(row, "incomeMode", text(normMode(order.getIncomeMode())), text(target.incomeMode), true);
        addDiff(row, "accumulateCycleDays", num(order.getAccumulateCycleDays()), num(target.accumulateCycleDays), false);
        addDiff(row, "protectDays", num(order.getProtectDays()), num(target.protectDays), true);
        addDiff(row, "relatedProductId", id(order.getRelatedProductId()), id(target.relatedProductId), true);
        int oldProtect = nz(order.getProtectDays());
        if (BizConstants.INCOME_MODE_PROTECT.equals(target.incomeMode) && target.protectDays > oldProtect)
        {
            BigDecimal excess = estimateProtectPoolExcess(order, target.protectDays);
            if (excess.compareTo(BigDecimal.ZERO) > 0)
            {
                row.getWarnings().add("保护期延长后预计累计池转入产品收益 " + excess.toPlainString());
            }
        }
        if (nz(order.getUnlockDirectQty()) != target.unlockDirectQty
                || nz(order.getUnlockDelayHours()) != target.unlockDelayHours)
        {
            row.getWarnings().add("同步后将按新一拖二/等待小时重算激活（不达标的已激活份会撤销；已发日返不回扣）");
        }
        boolean changed = !row.getFieldDiffs().isEmpty();
        row.setChanged(Boolean.valueOf(changed));
        row.setAction(changed ? "would_sync" : "skip_same");
        return row;
    }

    private BizOrderSnapshotPreview diffAssist(BizProduct product, BizOrder order)
    {
        BizOrderSnapshotPreview row = new BizOrderSnapshotPreview();
        row.setOrderId(order.getOrderId());
        row.setOrderNo(order.getOrderNo());
        row.setMemberId(order.getMemberId());
        row.setPhone(order.getPhone());
        if (!order.assistMode())
        {
            row.setChanged(Boolean.FALSE);
            row.setAction("skip_same");
            row.getWarnings().add("非助力单，跳过");
            return row;
        }
        AssistSnapshotTarget target = assistTargetOf(product, order);
        addDiff(row, "productName", text(order.getProductName()), text(target.productName), false);
        addDiff(row, "withdrawRequired", text(nzWithdraw(order.getWithdrawRequired())), text(target.withdrawRequired), false);
        addDiff(row, "assistValue", dec(order.getAssistValue()), dec(target.assistValue), true);
        addDiff(row, "principalReturnDays", num(order.getPrincipalReturnDays()), num(target.principalReturnDays), true);
        if (!"1".equals(order.getPrincipalReturned()))
        {
            addDiff(row, "principalReturnAt", text(formatTime(order.getPrincipalReturnAt())),
                    text(formatTime(target.principalReturnAt)), true);
        }
        else
        {
            row.getWarnings().add("已退本，不改到期时间");
        }
        row.getWarnings().add("不同步价格；助力值只改展示快照，不补发不扣回");
        boolean changed = !row.getFieldDiffs().isEmpty();
        row.setChanged(Boolean.valueOf(changed));
        row.setAction(changed ? "would_sync" : "skip_same");
        return row;
    }

    private void applyRebateSnapshot(BizProduct product, BizOrder order, SnapshotTarget target)
    {
        BizOrder patch = new BizOrder();
        patch.setOrderId(order.getOrderId());
        patch.setProductName(target.productName);
        patch.setDailyRebate(target.dailyRebate);
        patch.setDurationDays(Integer.valueOf(target.durationDays));
        patch.setWithdrawRequired(target.withdrawRequired);
        patch.setUnlockDirectQty(Integer.valueOf(target.unlockDirectQty));
        patch.setUnlockDelayHours(Integer.valueOf(target.unlockDelayHours));
        patch.setIncomeMode(target.incomeMode);
        patch.setAccumulateCycleDays(Integer.valueOf(target.accumulateCycleDays));
        patch.setProtectDays(Integer.valueOf(target.protectDays));
        patch.setRelatedProductId(target.relatedProductId);
        orderMapper.updateOrderSnapshot(patch);
        order.setProtectDays(Integer.valueOf(target.protectDays));
        order.setIncomeMode(target.incomeMode);
        order.setUnlockDirectQty(Integer.valueOf(target.unlockDirectQty));
        order.setUnlockDelayHours(Integer.valueOf(target.unlockDelayHours));
    }

    private void applyAssistSnapshot(BizProduct product, BizOrder order)
    {
        AssistSnapshotTarget target = assistTargetOf(product, order);
        BizOrder patch = new BizOrder();
        patch.setOrderId(order.getOrderId());
        patch.setProductName(target.productName);
        patch.setWithdrawRequired(target.withdrawRequired);
        patch.setAssistValue(target.assistValue);
        patch.setPrincipalReturnDays(Integer.valueOf(target.principalReturnDays));
        if ("1".equals(order.getPrincipalReturned()))
        {
            patch.setPrincipalReturnAt(order.getPrincipalReturnAt());
        }
        else
        {
            patch.setPrincipalReturnAt(target.principalReturnAt);
        }
        orderMapper.updateAssistOrderSnapshot(patch);
    }

    private SnapshotTarget rebateTargetOf(BizProduct product, BizOrder order)
    {
        SnapshotTarget target = new SnapshotTarget();
        target.productName = product.getProductName();
        int qty = order.getQuantity() == null || order.getQuantity().intValue() <= 0
                ? 1 : order.getQuantity().intValue();
        BigDecimal unit = product.rebateOf(order.getCurrency());
        if (unit == null)
        {
            unit = BigDecimal.ZERO;
        }
        target.dailyRebate = unit.multiply(new BigDecimal(qty));
        target.durationDays = nz(product.getDurationDays());
        target.withdrawRequired = nzWithdraw(product.getWithdrawRequired());
        target.unlockDirectQty = nz(product.getUnlockDirectQty());
        target.unlockDelayHours = nz(product.getUnlockDelayHours());
        if (product.accumulateIncome())
        {
            target.incomeMode = BizConstants.INCOME_MODE_ACCUMULATE;
            target.accumulateCycleDays = nz(product.getAccumulateCycleDays());
            target.protectDays = 0;
            target.relatedProductId = product.getRelatedProductId();
        }
        else if (product.protectIncome())
        {
            target.incomeMode = BizConstants.INCOME_MODE_PROTECT;
            target.accumulateCycleDays = 0;
            target.protectDays = nz(product.getProtectDays());
            target.relatedProductId = null;
        }
        else
        {
            target.incomeMode = BizConstants.INCOME_MODE_CREDIT;
            target.accumulateCycleDays = 0;
            target.protectDays = 0;
            target.relatedProductId = null;
        }
        return target;
    }

    private AssistSnapshotTarget assistTargetOf(BizProduct product, BizOrder order)
    {
        AssistSnapshotTarget target = new AssistSnapshotTarget();
        target.productName = product.getProductName();
        target.withdrawRequired = nzWithdraw(product.getWithdrawRequired());
        int qty = order.getQuantity() == null || order.getQuantity().intValue() <= 0
                ? 1 : order.getQuantity().intValue();
        BigDecimal unit = product.assistValueOf(order.getCurrency());
        if (unit == null)
        {
            unit = BigDecimal.ZERO;
        }
        target.assistValue = unit.multiply(new BigDecimal(qty));
        int returnDays = nz(product.getPrincipalReturnDays());
        if (returnDays <= 0)
        {
            returnDays = nz(order.getPrincipalReturnDays());
        }
        target.principalReturnDays = returnDays;
        Date base = order.getCreateTime() != null ? order.getCreateTime() : new Date();
        target.principalReturnAt = plusDays(base, returnDays);
        return target;
    }

    private BigDecimal estimateProtectPoolExcess(BizOrder order, int newProtectDays)
    {
        BigDecimal pool = order.getAccumulatedAmount() == null ? BigDecimal.ZERO : order.getAccumulatedAmount();
        if (pool.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        int duration = order.getDurationDays() == null ? 0 : order.getDurationDays().intValue();
        int remain = nz(order.getRemainingDays());
        int paidCount = duration > 0 ? Math.max(0, duration - remain) : 0;
        int idealDays = Math.max(0, paidCount - newProtectDays);
        BigDecimal daily = order.getDailyRebate() == null ? BigDecimal.ZERO : order.getDailyRebate();
        // 未激活近似：整单日返 * 理想累计天数；已有激活时仍按池超额估算，执行时以订单服务为准
        BigDecimal idealPool = daily.multiply(new BigDecimal(idealDays));
        BigDecimal excess = pool.subtract(idealPool);
        if (excess.compareTo(BigDecimal.ZERO) < 0)
        {
            return BigDecimal.ZERO;
        }
        if (excess.compareTo(pool) > 0)
        {
            return pool;
        }
        return excess;
    }

    private void addDiff(BizOrderSnapshotPreview row, String field, String before, String after, boolean warn)
    {
        if (before == null)
        {
            before = "";
        }
        if (after == null)
        {
            after = "";
        }
        if (before.equals(after))
        {
            return;
        }
        row.getFieldDiffs().add(new BizOrderSnapshotFieldDiff(field, before, after));
        if (!warn)
        {
            return;
        }
        if ("dailyRebate".equals(field))
        {
            row.getWarnings().add("日返金额变更只影响之后发放，不补发不冲正");
        }
        else if ("durationDays".equals(field))
        {
            row.getWarnings().add("总天数快照会改，剩余天数不改");
        }
        else if ("unlockDirectQty".equals(field))
        {
            row.getWarnings().add("一拖二比例变更后按新规则重算激活，不达标的已激活份会撤销；已发日返不回扣");
        }
        else if ("unlockDelayHours".equals(field))
        {
            row.getWarnings().add("等待小时只影响尚未写入开始返利时间的份额");
        }
        else if ("incomeMode".equals(field))
        {
            row.getWarnings().add("入账方式变更不自动清理历史累计；保护期延长会纠偏超额累计池");
        }
        else if ("protectDays".equals(field))
        {
            row.getWarnings().add("保护期延长将纠偏累计池；缩短只影响之后发放");
        }
        else if ("relatedProductId".equals(field))
        {
            row.getWarnings().add("对档产品变更不影响本单已消耗的对档份额");
        }
        else if ("assistValue".equals(field))
        {
            row.getWarnings().add("助力值只改订单展示，不补发不扣回钱包");
        }
        else if ("principalReturnDays".equals(field) || "principalReturnAt".equals(field))
        {
            row.getWarnings().add("未退本将重算到期时间；已退本不动");
        }
    }

    private String normMode(String mode)
    {
        if (StringUtils.isEmpty(mode))
        {
            return BizConstants.INCOME_MODE_CREDIT;
        }
        return mode.trim().toUpperCase();
    }

    private String nzWithdraw(String value)
    {
        return StringUtils.isEmpty(value) ? "0" : value;
    }

    private String text(String value)
    {
        return value == null ? "" : value;
    }

    private String num(Integer value)
    {
        return String.valueOf(nz(value));
    }

    private String id(Long value)
    {
        return value == null ? "" : String.valueOf(value.longValue());
    }

    private String dec(BigDecimal value)
    {
        return (value == null ? BigDecimal.ZERO : value).stripTrailingZeros().toPlainString();
    }

    private String formatTime(Date time)
    {
        if (time == null)
        {
            return "";
        }
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fmt.format(time);
    }

    private Date plusDays(Date time, int days)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time == null ? new Date() : time);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private int nz(Integer value)
    {
        return value == null ? 0 : value.intValue();
    }

    private static final class SnapshotTarget
    {
        private String productName;
        private BigDecimal dailyRebate;
        private int durationDays;
        private String withdrawRequired;
        private int unlockDirectQty;
        private int unlockDelayHours;
        private String incomeMode;
        private int accumulateCycleDays;
        private int protectDays;
        private Long relatedProductId;
    }

    private static final class AssistSnapshotTarget
    {
        private String productName;
        private String withdrawRequired;
        private BigDecimal assistValue;
        private int principalReturnDays;
        private Date principalReturnAt;
    }

}
