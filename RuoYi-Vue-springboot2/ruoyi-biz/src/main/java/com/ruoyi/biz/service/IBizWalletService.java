package com.ruoyi.biz.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.biz.domain.BizWallet;
import com.ruoyi.biz.domain.BizWalletLog;

public interface IBizWalletService
{
    List<BizWallet> selectWalletsByMemberId(Long memberId);

    BizWallet getWallet(Long memberId, String currency);

    void initWallets(Long memberId);

    void credit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void debit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void freeze(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void unfreezeSuccess(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void unfreezeReject(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    List<BizWalletLog> selectWalletLogList(BizWalletLog log);
}
