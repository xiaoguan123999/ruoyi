package com.ruoyi.biz.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.biz.domain.BizRecharge;

public interface IBizRechargeService
{
    BizRecharge selectRechargeById(Long rechargeId);

    List<BizRecharge> selectRechargeList(BizRecharge recharge);

    BizRecharge apply(Long memberId, String currency, BigDecimal amount, String remark);

    BizRecharge applyOnline(Long memberId, String currency, BigDecimal amount, String remark,
            String channelCode, String outTradeNo);

    void audit(Long rechargeId, String status, String auditBy, String auditRemark);

    /**
     * 线上支付到账：待审直接通过；若因超时关单被拒，允许翻转为通过并入账。
     */
    void passOnlinePaid(Long rechargeId, String auditBy, String auditRemark);
}
