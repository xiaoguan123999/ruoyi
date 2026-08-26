package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCommissionLog;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizRecharge;
import com.ruoyi.biz.mapper.BizCommissionLogMapper;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.mapper.BizRechargeMapper;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.biz.service.IBizRechargeService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class BizRechargeServiceImpl implements IBizRechargeService
{
    @Autowired
    private BizRechargeMapper rechargeMapper;

    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private BizCommissionLogMapper commissionLogMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizConfigService configService;

    @Autowired
    private IBizMemberService memberService;

    @Override
    public BizRecharge selectRechargeById(Long rechargeId)
    {
        return rechargeMapper.selectRechargeById(rechargeId);
    }

    @Override
    public List<BizRecharge> selectRechargeList(BizRecharge recharge)
    {
        return rechargeMapper.selectRechargeList(recharge);
    }

    @Override
    public BizRecharge apply(Long memberId, String currency, BigDecimal amount, String remark)
    {
        configService.assertCurrencyEnabled(currency);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("充值金额必须大于0");
        }
        BizRecharge recharge = new BizRecharge();
        recharge.setMemberId(memberId);
        recharge.setCurrency(currency.toUpperCase());
        recharge.setAmount(amount);
        recharge.setStatus(BizConstants.AUDIT_PENDING);
        recharge.setRemark(remark);
        recharge.setPayMode(BizConstants.PAY_MODE_MANUAL);
        rechargeMapper.insertRecharge(recharge);
        return recharge;
    }

    @Override
    public BizRecharge applyOnline(Long memberId, String currency, BigDecimal amount, String remark,
            String channelCode, String outTradeNo)
    {
        configService.assertCurrencyEnabled(currency);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("充值金额必须大于0");
        }
        BizRecharge recharge = new BizRecharge();
        recharge.setMemberId(memberId);
        recharge.setCurrency(currency.toUpperCase());
        recharge.setAmount(amount);
        recharge.setStatus(BizConstants.AUDIT_PENDING);
        recharge.setRemark(remark);
        recharge.setPayMode(BizConstants.PAY_MODE_ONLINE);
        recharge.setChannelCode(channelCode);
        recharge.setOutTradeNo(outTradeNo);
        rechargeMapper.insertRecharge(recharge);
        return recharge;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long rechargeId, String status, String auditBy, String auditRemark)
    {
        BizRecharge recharge = rechargeMapper.selectRechargeById(rechargeId);
        if (recharge == null)
        {
            throw new ServiceException("充值单不存在");
        }
        if (!BizConstants.AUDIT_PENDING.equals(recharge.getStatus()))
        {
            if (BizConstants.AUDIT_PASS.equals(recharge.getStatus()) && BizConstants.AUDIT_PASS.equals(status))
            {
                return;
            }
            throw new ServiceException("该充值单已审核");
        }
        if (!BizConstants.AUDIT_PASS.equals(status) && !BizConstants.AUDIT_REJECT.equals(status))
        {
            throw new ServiceException("审核状态不正确");
        }
        recharge.setStatus(status);
        recharge.setAuditBy(auditBy);
        recharge.setAuditTime(new Date());
        recharge.setAuditRemark(auditRemark);
        rechargeMapper.updateRecharge(recharge);
        if (BizConstants.AUDIT_PASS.equals(status))
        {
            walletService.credit(recharge.getMemberId(), recharge.getCurrency(), recharge.getAmount(),
                    BizConstants.BIZ_RECHARGE, recharge.getRechargeId(), "充值入账");
            grantCommission(recharge);
            memberService.refreshLevel(recharge.getMemberId());
        }
    }

    private void grantCommission(BizRecharge recharge)
    {
        if (!configService.isTeamCommissionEnabled())
        {
            return;
        }
        BizMember current = memberMapper.selectMemberById(recharge.getMemberId());
        if (current == null)
        {
            return;
        }
        Long parentId = current.getParentId();
        for (int level = 1; level <= 3 && parentId != null; level++)
        {
            BizMember parent = memberMapper.selectMemberById(parentId);
            if (parent == null || BizConstants.STATUS_DISABLE.equals(parent.getStatus()))
            {
                break;
            }
            BigDecimal rate = configService.getTeamRate(level);
            if (rate.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal amount = recharge.getAmount().multiply(rate).divide(new BigDecimal("100"), 4, RoundingMode.DOWN);
                if (amount.compareTo(BigDecimal.ZERO) > 0)
                {
                    walletService.credit(parent.getMemberId(), recharge.getCurrency(), amount,
                            BizConstants.BIZ_COMMISSION, recharge.getRechargeId(), "团队" + level + "级分佣");
                    BizCommissionLog log = new BizCommissionLog();
                    log.setFromMemberId(recharge.getMemberId());
                    log.setToMemberId(parent.getMemberId());
                    log.setTeamLevel(level);
                    log.setCurrency(recharge.getCurrency());
                    log.setBaseAmount(recharge.getAmount());
                    log.setRate(rate);
                    log.setAmount(amount);
                    log.setRechargeId(recharge.getRechargeId());
                    commissionLogMapper.insertCommissionLog(log);
                    memberService.refreshLevel(parent.getMemberId());
                }
            }
            parentId = parent.getParentId();
        }
    }
}
