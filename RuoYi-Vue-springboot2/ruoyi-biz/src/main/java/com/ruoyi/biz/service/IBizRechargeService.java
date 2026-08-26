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
}
