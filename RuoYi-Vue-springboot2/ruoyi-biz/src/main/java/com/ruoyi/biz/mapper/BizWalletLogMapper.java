package com.ruoyi.biz.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizWalletLog;

public interface BizWalletLogMapper
{
    List<BizWalletLog> selectWalletLogList(BizWalletLog log);

    List<BizWalletLog> selectAppWalletLogList(@Param("memberId") Long memberId, @Param("currency") String currency,
            @Param("bizTypes") List<String> bizTypes, @Param("walletTypeCode") String walletTypeCode);

    BigDecimal sumPromoIncome(@Param("memberId") Long memberId, @Param("currency") String currency);

    int insertWalletLog(BizWalletLog log);

    /** 某业务单已入账净额（按会员/钱包/币种） */
    List<BizWalletLog> selectNetCreditsByBizId(@Param("bizId") Long bizId, @Param("bizTypes") List<String> bizTypes);
}
