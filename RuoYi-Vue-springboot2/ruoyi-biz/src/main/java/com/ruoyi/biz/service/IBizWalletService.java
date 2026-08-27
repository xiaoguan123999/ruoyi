package com.ruoyi.biz.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.biz.api.AppWalletCard;
import com.ruoyi.biz.api.AppWalletLogItem;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizWallet;
import com.ruoyi.biz.domain.BizWalletLog;

public interface IBizWalletService
{
    List<BizWallet> selectWalletsByMemberId(Long memberId);

    BizWallet getWallet(Long memberId, String currency);

    BizWallet getWallet(Long memberId, String typeCode, String currency);

    void initWallets(Long memberId);

    void credit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void credit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark, String typeCode);

    void debit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void adjust(Long memberId, String typeCode, String currency, String direction, BigDecimal amount, String remark, String operator);

    void freeze(Long memberId, String typeCode, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void unfreezeSuccess(Long memberId, String typeCode, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    void unfreezeReject(Long memberId, String typeCode, String currency, BigDecimal amount, String bizType, Long bizId, String remark);

    List<BizWalletLog> selectWalletLogList(BizWalletLog log);

    List<AppWalletLogItem> selectAppWalletLogList(Long memberId, String currency, String bizType);

    AppWalletCard selectAppWalletCard(Long memberId);

    void fillAssetSummary(BizMember member);
}
