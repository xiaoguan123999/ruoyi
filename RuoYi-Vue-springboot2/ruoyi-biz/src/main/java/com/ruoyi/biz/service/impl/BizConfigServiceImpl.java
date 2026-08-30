package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class BizConfigServiceImpl implements IBizConfigService
{
    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SysConfigMapper sysConfigMapper;

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
    public BigDecimal getWithdrawMaxAmount(String currency)
    {
        String key = BizConstants.CURRENCY_USDT.equalsIgnoreCase(currency)
                ? BizConstants.CONFIG_WITHDRAW_MAX_USDT
                : BizConstants.CONFIG_WITHDRAW_MAX;
        BigDecimal value = optionalDecimal(key);
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
        {
            return null;
        }
        return value;
    }

    @Override
    public BigDecimal getWithdrawFeeRate()
    {
        BigDecimal rate;
        try
        {
            rate = decimal(BizConstants.CONFIG_WITHDRAW_FEE_RATE, "3");
        }
        catch (Exception e)
        {
            rate = new BigDecimal("3");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0)
        {
            return BigDecimal.ZERO;
        }
        if (rate.compareTo(new BigDecimal("100")) > 0)
        {
            return new BigDecimal("100");
        }
        return rate;
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
    public boolean isWithdrawNeedKyc()
    {
        return bool(BizConstants.CONFIG_WITHDRAW_NEED_KYC, false);
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

    @Override
    public BigDecimal getUsdtToCnyRate()
    {
        BigDecimal rate = decimal(BizConstants.CONFIG_FX_USDT_TO_CNY, BizConstants.FX_USDT_TO_CNY_DEFAULT);
        if (rate.compareTo(BigDecimal.ZERO) <= 0)
        {
            return new BigDecimal(BizConstants.FX_USDT_TO_CNY_DEFAULT);
        }
        return rate;
    }

    @Override
    public void saveConfig(String key, String name, String value, String remark)
    {
        SysConfig existing = sysConfigMapper.checkConfigKeyUnique(key);
        if (existing == null)
        {
            SysConfig config = new SysConfig();
            config.setConfigName(name);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("N");
            config.setRemark(remark);
            configService.insertConfig(config);
        }
        else
        {
            existing.setConfigName(name);
            existing.setConfigValue(value);
            existing.setRemark(remark);
            configService.updateConfig(existing);
        }
    }

    @Override
    public void refreshCache()
    {
        configService.resetConfigCache();
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
        return new BigDecimal(configValue(key, defaultValue));
    }

    private BigDecimal optionalDecimal(String key)
    {
        String value = configValue(key, null);
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        return new BigDecimal(value);
    }

    private String configValue(String key, String defaultValue)
    {
        SysConfig row = sysConfigMapper.checkConfigKeyUnique(key);
        if (row != null && StringUtils.isNotEmpty(row.getConfigValue()))
        {
            return row.getConfigValue();
        }
        String cached = configService.selectConfigByKey(key);
        if (StringUtils.isNotEmpty(cached))
        {
            return cached;
        }
        return defaultValue;
    }
}
