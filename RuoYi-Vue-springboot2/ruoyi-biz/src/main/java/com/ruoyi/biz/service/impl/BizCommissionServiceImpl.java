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
        BigDecimal base = order.getPrice();
        if (base == null || base.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        String currency = order.getCurrency() == null ? BizConstants.CURRENCY_CNY : order.getCurrency().toUpperCase();
        BizMember current = memberService.selectMemberById(order.getMemberId());
        if (current == null)
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
            BigDecimal rate = configService.getTeamRate(level);
            if (rate.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal amount = base.multiply(rate).divide(new BigDecimal("100"), 4, RoundingMode.DOWN);
                if (amount.compareTo(BigDecimal.ZERO) > 0)
                {
                    walletService.credit(parent.getMemberId(), currency, amount,
                            BizConstants.BIZ_COMMISSION, order.getOrderId(), "认购团队" + level + "级分佣");
                    BizCommissionLog log = new BizCommissionLog();
                    log.setFromMemberId(order.getMemberId());
                    log.setToMemberId(parent.getMemberId());
                    log.setTeamLevel(level);
                    log.setCurrency(currency);
                    log.setBaseAmount(base);
                    log.setRate(rate);
                    log.setAmount(amount);
                    log.setOrderId(order.getOrderId());
                    commissionLogMapper.insertCommissionLog(log);
                    memberService.refreshLevel(parent.getMemberId());
                }
            }
            parentId = parent.getParentId();
        }
    }
}
