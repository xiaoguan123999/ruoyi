package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.mapper.BizOrderMapper;
import com.ruoyi.biz.mapper.BizWithdrawMapper;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.biz.service.IBizWithdrawService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class BizWithdrawServiceImpl implements IBizWithdrawService
{
    @Autowired
    private BizWithdrawMapper withdrawMapper;

    @Autowired
    private BizOrderMapper orderMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizConfigService configService;

    @Autowired
    private IBizGoogleAuthService googleAuthService;

    @Override
    public BizWithdraw selectWithdrawById(Long withdrawId)
    {
        return withdrawMapper.selectWithdrawById(withdrawId);
    }

    @Override
    public List<BizWithdraw> selectWithdrawList(BizWithdraw withdraw)
    {
        return withdrawMapper.selectWithdrawList(withdraw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizWithdraw apply(Long memberId, String currency, BigDecimal amount, String accountInfo, String remark, String googleCode)
    {
        googleAuthService.assertForWithdraw(memberId, googleCode);
        configService.assertCurrencyEnabled(currency);
        String payCurrency = currency.toUpperCase();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("提现金额必须大于0");
        }
        BigDecimal minAmount = configService.getWithdrawMinAmount(payCurrency);
        if (amount.compareTo(minAmount) < 0)
        {
            throw new ServiceException("最低提现金额为" + minAmount + " " + payCurrency);
        }
        if (orderMapper.countWithdrawRequiredOrders(memberId, payCurrency) <= 0)
        {
            throw new ServiceException("请先认购对应币种的指定产品后再提现");
        }
        walletService.freeze(memberId, payCurrency, amount, BizConstants.BIZ_WITHDRAW_FREEZE, null, "提现冻结");
        BizWithdraw withdraw = new BizWithdraw();
        withdraw.setMemberId(memberId);
        withdraw.setCurrency(payCurrency);
        withdraw.setAmount(amount);
        withdraw.setAccountInfo(accountInfo);
        withdraw.setStatus(BizConstants.AUDIT_PENDING);
        withdraw.setRemark(remark);
        withdrawMapper.insertWithdraw(withdraw);
        return withdraw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long withdrawId, String status, String auditBy, String auditRemark)
    {
        BizWithdraw withdraw = withdrawMapper.selectWithdrawById(withdrawId);
        if (withdraw == null)
        {
            throw new ServiceException("提现单不存在");
        }
        if (!BizConstants.AUDIT_PENDING.equals(withdraw.getStatus()))
        {
            throw new ServiceException("该提现单已审核");
        }
        if (!BizConstants.AUDIT_PASS.equals(status) && !BizConstants.AUDIT_REJECT.equals(status))
        {
            throw new ServiceException("审核状态不正确");
        }
        withdraw.setStatus(status);
        withdraw.setAuditBy(auditBy);
        withdraw.setAuditTime(new Date());
        withdraw.setAuditRemark(auditRemark);
        withdrawMapper.updateWithdraw(withdraw);
        if (BizConstants.AUDIT_PASS.equals(status))
        {
            walletService.unfreezeSuccess(withdraw.getMemberId(), withdraw.getCurrency(), withdraw.getAmount(),
                    BizConstants.BIZ_WITHDRAW_SUCCESS, withdraw.getWithdrawId(), "提现成功");
        }
        else
        {
            walletService.unfreezeReject(withdraw.getMemberId(), withdraw.getCurrency(), withdraw.getAmount(),
                    BizConstants.BIZ_WITHDRAW_REJECT, withdraw.getWithdrawId(), "提现拒绝解冻");
        }
    }
}
