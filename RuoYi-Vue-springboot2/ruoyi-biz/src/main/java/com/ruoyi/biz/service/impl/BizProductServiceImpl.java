package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.domain.BizProductCardMetric;
import com.ruoyi.biz.domain.BizProductCardTemplate;
import com.ruoyi.biz.domain.BizProductCategory;
import com.ruoyi.biz.mapper.BizProductCardMetricMapper;
import com.ruoyi.biz.mapper.BizProductCardTemplateMapper;
import com.ruoyi.biz.mapper.BizProductCategoryMapper;
import com.ruoyi.biz.mapper.BizProductMapper;
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
        }
        else
        {
            product.setIncomeMode(BizConstants.INCOME_MODE_CREDIT);
            product.setAccumulateCycleDays(Integer.valueOf(0));
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
}
