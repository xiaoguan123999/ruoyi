package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class BizConfigServiceImpl implements IBizConfigService
{
    @Autowired
    private ISysConfigService configService;

    @Override
    public BigDecimal getCheckinAmount()
    {
        return decimal(BizConstants.CONFIG_CHECKIN_AMOUNT, "2");
    }

    @Override
    public BigDecimal getWithdrawMinAmount()
    {
        return getWithdrawMinAmount(BizConstants.CURRENCY_CNY);
    }

    @Override
    public BigDecimal getWithdrawMinAmount(String currency)
    {
        if (BizConstants.CURRENCY_USDT.equalsIgnoreCase(currency))
        {
            return decimal(BizConstants.CONFIG_WITHDRAW_MIN_USDT, "105");
        }
        return decimal(BizConstants.CONFIG_WITHDRAW_MIN, "105");
    }

    @Override
    public BigDecimal getTeamRate(int level)
    {
        if (level == 1)
        {
            return decimal(BizConstants.CONFIG_RATE_L1, "9");
        }
        if (level == 2)
        {
            return decimal(BizConstants.CONFIG_RATE_L2, "3");
        }
        if (level == 3)
        {
            return decimal(BizConstants.CONFIG_RATE_L3, "1");
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isTeamCommissionEnabled()
    {
        return bool(BizConstants.CONFIG_TEAM_ENABLED, true);
    }

    @Override
    public boolean isUsdtEnabled()
    {
        String value = configService.selectConfigByKey(BizConstants.CONFIG_USDT_ENABLED);
        return "true".equalsIgnoreCase(value);
    }

    @Override
    public void assertCurrencyEnabled(String currency)
    {
        if (BizConstants.CURRENCY_USDT.equalsIgnoreCase(currency) && !isUsdtEnabled())
        {
            throw new ServiceException("USDT暂未开放");
        }
        if (!BizConstants.CURRENCY_CNY.equalsIgnoreCase(currency)
                && !BizConstants.CURRENCY_USDT.equalsIgnoreCase(currency))
        {
            throw new ServiceException("不支持的币种");
        }
    }

    @Override
    public boolean isGoogleEnabled()
    {
        return bool(BizConstants.CONFIG_GOOGLE_ENABLED, true);
    }

    @Override
    public boolean isGoogleRequiredForWithdraw()
    {
        return false;
    }

    @Override
    public String getGoogleIssuer()
    {
        String value = configService.selectConfigByKey(BizConstants.CONFIG_GOOGLE_ISSUER);
        return StringUtils.isEmpty(value) ? "App" : value;
    }

    private boolean bool(String key, boolean defaultValue)
    {
        String value = configService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private BigDecimal decimal(String key, String defaultValue)
    {
        String value = configService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            value = defaultValue;
        }
        return new BigDecimal(value);
    }
}
