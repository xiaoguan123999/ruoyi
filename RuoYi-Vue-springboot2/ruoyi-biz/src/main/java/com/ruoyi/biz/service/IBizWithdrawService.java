package com.ruoyi.biz.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.biz.domain.BizWithdraw;

public interface IBizWithdrawService
{
    BizWithdraw selectWithdrawById(Long withdrawId);

    List<BizWithdraw> selectWithdrawList(BizWithdraw withdraw);

    BizWithdraw apply(Long memberId, String currency, BigDecimal amount, String accountInfo, String remark, String googleCode);

    void audit(Long withdrawId, String status, String auditBy, String auditRemark, String payProofUrl);
}
