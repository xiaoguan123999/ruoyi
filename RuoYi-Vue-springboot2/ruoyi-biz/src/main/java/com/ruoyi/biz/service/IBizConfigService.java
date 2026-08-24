package com.ruoyi.biz.service;

import java.math.BigDecimal;

public interface IBizConfigService
{
    BigDecimal getCheckinAmount();

    BigDecimal getWithdrawMinAmount();

    BigDecimal getWithdrawMinAmount(String currency);

    BigDecimal getTeamRate(int level);

    boolean isTeamCommissionEnabled();

    boolean isUsdtEnabled();

    void assertCurrencyEnabled(String currency);

    boolean isGoogleEnabled();

    boolean isGoogleRequiredForWithdraw();

    String getGoogleIssuer();
}

