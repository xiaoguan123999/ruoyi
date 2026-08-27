package com.ruoyi.biz.service;

import java.math.BigDecimal;

public interface IBizConfigService
{
    BigDecimal getCheckinAmount();

    BigDecimal getWithdrawMinAmount();

    BigDecimal getWithdrawMinAmount(String currency);

    /** 最高提现，null 表示不限 */
    BigDecimal getWithdrawMaxAmount(String currency);

    BigDecimal getWithdrawFeeRate();

    BigDecimal getTeamRate(int level);

    boolean isTeamCommissionEnabled();

    boolean isUsdtEnabled();

    void assertCurrencyEnabled(String currency);

    boolean isGoogleEnabled();

    boolean isGoogleRequiredForWithdraw();

    String getGoogleIssuer();

    void saveConfig(String key, String name, String value, String remark);

    void refreshCache();
}
