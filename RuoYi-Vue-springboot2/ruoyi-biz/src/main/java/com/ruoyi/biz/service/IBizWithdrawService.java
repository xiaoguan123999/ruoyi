package com.ruoyi.biz.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.domain.BizWithdrawRule;

public interface IBizWithdrawService
{
    BizWithdraw selectWithdrawById(Long withdrawId);

    List<BizWithdraw> selectWithdrawList(BizWithdraw withdraw);

    BizWithdraw apply(Long memberId, String currency, BigDecimal amount, String accountInfo, String remark, String googleCode);

    BizWithdraw apply(Long memberId, String currency, BigDecimal amount, String accountInfo, String remark,
            String googleCode, String payMethod);

    void audit(Long withdrawId, String status, String auditBy, String auditRemark, String payProofUrl);

    /** 批量审核，逐单提交；返回成功/失败笔数说明 */
    String auditBatch(Long[] ids, String status, String auditBy, String auditRemark, String payProofUrl);

    BizWithdrawRule getRule();

    void saveRule(BizWithdrawRule rule);
}
