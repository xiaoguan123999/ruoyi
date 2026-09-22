package com.ruoyi.biz.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.domain.BizProductCardMetric;
import com.ruoyi.common.utils.StringUtils;

/**
 * 产品卡片指标：默认槽位 + display 文案组装。
 */
public final class ProductCardMetricSupport
{
    public static final String SOURCE_PRICE = "PRICE";
    public static final String SOURCE_DAILY_REBATE = "DAILY_REBATE";
    public static final String SOURCE_DURATION = "DURATION";
    public static final String SOURCE_BUY_LIMIT = "BUY_LIMIT";
    public static final String SOURCE_ASSIST_VALUE = "ASSIST_VALUE";
    public static final String SOURCE_PRINCIPAL_RETURN = "PRINCIPAL_RETURN";
    public static final String SOURCE_CUSTOM = "CUSTOM";

    public static final String CODE_CLASSIC = "CLASSIC";
    public static final String CODE_HERO = "HERO";
    public static final String CODE_SPLIT = "SPLIT";
    public static final String CODE_NUMBERED = "NUMBERED";
    public static final String CODE_ROW = "ROW";
    public static final String CODE_COMPACT = "COMPACT";
    public static final String CODE_BANNER = "BANNER";
    public static final String CODE_PRICE_FOCUS = "PRICE_FOCUS";
    public static final String CODE_MEDIA_LEFT = "MEDIA_LEFT";

    private ProductCardMetricSupport()
    {
    }

    public static List<BizProductCardMetric> defaultMetrics(String templateCode)
    {
        String code = normalizeCode(templateCode);
        List<BizProductCardMetric> list = new ArrayList<BizProductCardMetric>();
        if (CODE_SPLIT.equals(code))
        {
            list.add(slot(1, "金额", SOURCE_PRICE, ""));
        list.add(slot(2, "助力值", SOURCE_ASSIST_VALUE, ""));
        list.add(slot(3, "限购", SOURCE_BUY_LIMIT, ""));
        list.add(slot(4, "本金返还", SOURCE_PRINCIPAL_RETURN, ""));
        return list;
        }
        if (CODE_NUMBERED.equals(code) || CODE_COMPACT.equals(code))
        {
            list.add(slot(1, "金额", SOURCE_PRICE, ""));
            list.add(slot(2, "每日收益", SOURCE_DAILY_REBATE, ""));
            list.add(slot(3, "助力值", SOURCE_ASSIST_VALUE, ""));
            list.add(slot(4, "收益周期", SOURCE_DURATION, ""));
            return list;
        }
        if (CODE_ROW.equals(code))
        {
            list.add(slot(1, "价格", SOURCE_PRICE, ""));
            list.add(slot(2, "每日收益", SOURCE_DAILY_REBATE, ""));
            return list;
        }
        if (CODE_PRICE_FOCUS.equals(code))
        {
            list.add(slot(1, "收益周期", SOURCE_DURATION, ""));
            list.add(slot(2, "每日收益", SOURCE_DAILY_REBATE, ""));
            return list;
        }
        if (CODE_MEDIA_LEFT.equals(code))
        {
            list.add(slot(1, "价格", SOURCE_PRICE, ""));
            list.add(slot(2, "每日收益", SOURCE_DAILY_REBATE, ""));
            list.add(slot(3, "收益周期", SOURCE_DURATION, ""));
            return list;
        }
        if (CODE_BANNER.equals(code))
        {
            list.add(slot(1, "价格", SOURCE_PRICE, ""));
            list.add(slot(2, "每日收益", SOURCE_DAILY_REBATE, ""));
            return list;
        }
        // CLASSIC / HERO 默认
        list.add(slot(1, "每日收益", SOURCE_DAILY_REBATE, ""));
        list.add(slot(2, "收益周期", SOURCE_DURATION, ""));
        return list;
    }

    public static String normalizeCode(String templateCode)
    {
        if (StringUtils.isEmpty(templateCode))
        {
            return CODE_CLASSIC;
        }
        return templateCode.trim().toUpperCase();
    }

    public static String resolveDisplay(BizProduct product, BizProductCardMetric metric)
    {
        if (metric == null)
        {
            return "";
        }
        String source = metric.getSource() == null ? SOURCE_CUSTOM : metric.getSource().trim().toUpperCase();
        if (SOURCE_PRICE.equals(source))
        {
            return formatMoneyPair(product.getPriceCny(), product.getPriceUsdt());
        }
        if (SOURCE_DAILY_REBATE.equals(source))
        {
            return formatMoneyPair(product.getDailyRebateCny(), product.getDailyRebateUsdt());
        }
        if (SOURCE_DURATION.equals(source))
        {
            return formatDuration(product.getDurationDays());
        }
        if (SOURCE_BUY_LIMIT.equals(source))
        {
            Integer limit = product.getBuyLimit();
            if (limit == null || limit.intValue() <= 0)
            {
                return "不限";
            }
            return limit + " 份";
        }
        if (SOURCE_ASSIST_VALUE.equals(source))
        {
            BigDecimal cny = product.getAssistValueCny();
            BigDecimal usdt = product.getAssistValueUsdt();
            boolean hasCny = cny != null && cny.compareTo(BigDecimal.ZERO) > 0;
            boolean hasUsdt = usdt != null && usdt.compareTo(BigDecimal.ZERO) > 0;
            if (hasCny && hasUsdt)
            {
                if (cny.compareTo(usdt) == 0)
                {
                    return stripZeros(cny);
                }
                return stripZeros(cny) + " / " + stripZeros(usdt);
            }
            if (hasCny)
            {
                return stripZeros(cny);
            }
            if (hasUsdt)
            {
                return stripZeros(usdt);
            }
            if (StringUtils.isNotEmpty(metric.getCustomText()))
            {
                return metric.getCustomText();
            }
            return "--";
        }
        if (SOURCE_PRINCIPAL_RETURN.equals(source))
        {
            Integer days = product.getPrincipalReturnDays();
            if (days != null && days.intValue() > 0)
            {
                return days + " 天";
            }
            if (StringUtils.isNotEmpty(metric.getCustomText()))
            {
                return metric.getCustomText();
            }
            return "--";
        }
        if (StringUtils.isNotEmpty(metric.getCustomText()))
        {
            return metric.getCustomText();
        }
        return "--";
    }

    public static String formatMainAmount(BizProduct product)
    {
        if (BizProduct.hasPrice(product.getPriceUsdt()))
        {
            return stripZeros(product.getPriceUsdt()) + " USDT";
        }
        if (BizProduct.hasPrice(product.getPriceCny()))
        {
            return stripZeros(product.getPriceCny()) + " 元";
        }
        return "--";
    }

    public static String formatMoneyPair(BigDecimal cny, BigDecimal usdt)
    {
        boolean hasCny = BizProduct.hasPrice(cny);
        boolean hasUsdt = BizProduct.hasPrice(usdt);
        if (hasCny && hasUsdt)
        {
            return stripZeros(cny) + " 元 / " + stripZeros(usdt) + " USDT";
        }
        if (hasUsdt)
        {
            return stripZeros(usdt) + " USDT";
        }
        if (hasCny)
        {
            return stripZeros(cny) + " 元";
        }
        return "--";
    }

    public static String formatDuration(Integer days)
    {
        if (days == null || days.intValue() <= 0)
        {
            return "--";
        }
        int d = days.intValue();
        if (d % 365 == 0)
        {
            return (d / 365) + "年";
        }
        if (d % 30 == 0)
        {
            return (d / 30) + "个月";
        }
        return d + "天";
    }

    public static List<BizProductCardMetric> withDisplay(BizProduct product, List<BizProductCardMetric> metrics)
    {
        if (metrics == null || metrics.isEmpty())
        {
            return Collections.emptyList();
        }
        for (int i = 0; i < metrics.size(); i++)
        {
            BizProductCardMetric m = metrics.get(i);
            m.setDisplay(resolveDisplay(product, m));
        }
        return metrics;
    }

    private static BizProductCardMetric slot(int index, String label, String source, String custom)
    {
        BizProductCardMetric m = new BizProductCardMetric();
        m.setSlotIndex(Integer.valueOf(index));
        m.setLabel(label);
        m.setSource(source);
        m.setCustomText(custom == null ? "" : custom);
        return m;
    }

    private static String stripZeros(BigDecimal value)
    {
        if (value == null)
        {
            return "0";
        }
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
