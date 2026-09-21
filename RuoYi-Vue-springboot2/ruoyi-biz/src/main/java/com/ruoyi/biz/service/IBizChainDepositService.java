package com.ruoyi.biz.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.biz.api.AppChainDepositConfigData;
import com.ruoyi.biz.api.AppChainDepositOrderData;
import com.ruoyi.biz.chain.TronUsdtTransfer;
import com.ruoyi.biz.domain.BizChainDeposit;
import com.ruoyi.biz.domain.BizChainDepositConfig;

public interface IBizChainDepositService
{
    AppChainDepositConfigData getAppConfig(Long memberId);

    AppChainDepositOrderData create(Long memberId, BigDecimal amount, String network);

    AppChainDepositOrderData getOrder(Long memberId, String outTradeNo);

    List<BizChainDeposit> selectList(BizChainDeposit query);

    void scan();

    void expirePending();

    void creditByFingerprint(TronUsdtTransfer tx);

    void simulatePaid(String outTradeNo, String operator);

    BizChainDepositConfig getAdminConfig();

    void saveAdminConfig(BizChainDepositConfig config);
}
