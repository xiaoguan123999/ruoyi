package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCommissionLog;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.domain.BizRecharge;
import com.ruoyi.biz.mapper.BizCommissionLogMapper;
import com.ruoyi.biz.service.IBizCommissionService;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.biz.service.IBizWalletService;

@Service
public class BizCommissionServiceImpl implements IBizCommissionService
{
    @Autowired
    private BizCommissionLogMapper commissionLogMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizConfigService configService;

    @Autowired
    private IBizMemberService memberService;

    @Override
    public List<BizCommissionLog> selectCommissionList(BizCommissionLog log)
    {
        return commissionLogMapper.selectCommissionList(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantForSubscribe(BizOrder order)
    {
        if (order == null || order.getMemberId() == null || order.getOrderId() == null)
        {
            return;
        }
        if (!configService.isTeamCommissionEnabled())
        {
            return;
        }
        grantUpchain(order.getMemberId(), order.getCurrency(), order.getPrice(),
                order.getOrderId(), null, "认购", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantForRecharge(BizRecharge recharge)
    {
        if (recharge == null || recharge.getMemberId() == null || recharge.getRechargeId() == null)
        {
            return;
        }
        if (!configService.isRechargeTeamCommissionEnabled())
        {
            return;
        }
        if (isManualRecharge(recharge) && !configService.isRechargeTeamManualCommissionEnabled())
        {
            return;
        }
        if (commissionLogMapper.countByRechargeId(recharge.getRechargeId()) > 0)
        {
            return;
        }
        grantUpchain(recharge.getMemberId(), recharge.getCurrency(), recharge.getAmount(),
                null, recharge.getRechargeId(), "充值", false);
    }

    /** 后台「+ 人工充值」及未标线上的旧单 */
    private boolean isManualRecharge(BizRecharge recharge)
    {
        String payMode = recharge.getPayMode();
        return payMode == null || payMode.length() == 0 || BizConstants.PAY_MODE_MANUAL.equals(payMode);
    }

    private void grantUpchain(Long fromMemberId, String currencyRaw, BigDecimal base,
            Long orderId, Long rechargeId, String scene, boolean subscribeRate)
    {
        if (base == null || base.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        String currency = currencyRaw == null ? BizConstants.CURRENCY_CNY : currencyRaw.toUpperCase();
        BizMember current = memberService.selectMemberById(fromMemberId);
        if (current == null || current.testAccount())
        {
            return;
        }
        Long parentId = current.getParentId();
        for (int level = 1; level <= 3 && parentId != null; level++)
        {
            BizMember parent = memberService.selectMemberById(parentId);
            if (parent == null || BizConstants.STATUS_DISABLE.equals(parent.getStatus()))
            {
                break;
            }
            if (parent.testAccount())
            {
                parentId = parent.getParentId();
                continue;
            }
            BigDecimal rate = subscribeRate ? configService.getTeamRate(level)
                    : configService.getRechargeTeamRate(level);
            if (rate.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal amount = base.multiply(rate).divide(new BigDecimal("100"), 4, RoundingMode.DOWN);
                if (amount.compareTo(BigDecimal.ZERO) > 0)
                {
                    Long bizId = orderId != null ? orderId : rechargeId;
                    walletService.credit(parent.getMemberId(), currency, amount,
                            BizConstants.BIZ_COMMISSION, bizId, scene + "团队" + level + "级分佣");
                    BizCommissionLog log = new BizCommissionLog();
                    log.setFromMemberId(fromMemberId);
                    log.setToMemberId(parent.getMemberId());
                    log.setTeamLevel(Integer.valueOf(level));
                    log.setCurrency(currency);
                    log.setBaseAmount(base);
                    log.setRate(rate);
                    log.setAmount(amount);
                    log.setOrderId(orderId);
                    log.setRechargeId(rechargeId);
                    commissionLogMapper.insertCommissionLog(log);
                    memberService.refreshLevel(parent.getMemberId());
                }
            }
            parentId = parent.getParentId();
        }
    }
}
