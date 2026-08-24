package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.api.AppWalletCard;
import com.ruoyi.biz.api.AppWalletLogItem;
import com.ruoyi.biz.api.AppWalletRow;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizWallet;
import com.ruoyi.biz.domain.BizWalletLog;
import com.ruoyi.biz.mapper.BizRebateLogMapper;
import com.ruoyi.biz.mapper.BizWalletLogMapper;
import com.ruoyi.biz.mapper.BizWalletMapper;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizWalletServiceImpl implements IBizWalletService
{
    @Autowired
    private BizWalletMapper walletMapper;

    @Autowired
    private BizWalletLogMapper walletLogMapper;

    @Autowired
    private BizRebateLogMapper rebateLogMapper;

    @Override
    public List<BizWallet> selectWalletsByMemberId(Long memberId)
    {
        return walletMapper.selectWalletsByMemberId(memberId);
    }

    @Override
    public BizWallet getWallet(Long memberId, String currency)
    {
        return walletMapper.selectWallet(memberId, currency);
    }

    @Override
    public void initWallets(Long memberId)
    {
        insertIfAbsent(memberId, BizConstants.CURRENCY_CNY);
        insertIfAbsent(memberId, BizConstants.CURRENCY_USDT);
    }

    private void insertIfAbsent(Long memberId, String currency)
    {
        if (walletMapper.selectWallet(memberId, currency) == null)
        {
            BizWallet wallet = new BizWallet();
            wallet.setMemberId(memberId);
            wallet.setCurrency(currency);
            wallet.setAvailable(BigDecimal.ZERO);
            wallet.setFrozen(BigDecimal.ZERO);
            walletMapper.insertWallet(wallet);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void credit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, currency, amount, BigDecimal.ZERO, bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void debit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, currency, amount.negate(), BigDecimal.ZERO, bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, currency, amount.negate(), amount, bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeSuccess(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, currency, BigDecimal.ZERO, amount.negate(), bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeReject(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, currency, amount, amount.negate(), bizType, bizId, remark);
    }

    @Override
    public List<BizWalletLog> selectWalletLogList(BizWalletLog log)
    {
        return walletLogMapper.selectWalletLogList(log);
    }

    @Override
    public List<AppWalletLogItem> selectAppWalletLogList(Long memberId, String currency, String bizType)
    {
        List<BizWalletLog> logs = walletLogMapper.selectAppWalletLogList(memberId, currency, bizType);
        List<AppWalletLogItem> rows = new ArrayList<AppWalletLogItem>();
        for (int i = 0; i < logs.size(); i++)
        {
            rows.add(toAppItem(logs.get(i)));
        }
        return rows;
    }

    @Override
    public AppWalletCard selectAppWalletCard(Long memberId)
    {
        initWallets(memberId);
        AppWalletRow cny = currencyRow(memberId, BizConstants.CURRENCY_CNY);
        AppWalletRow usdt = currencyRow(memberId, BizConstants.CURRENCY_USDT);
        List<AppWalletRow> wallets = new ArrayList<AppWalletRow>();
        wallets.add(cny);
        wallets.add(usdt);

        AppWalletCard data = new AppWalletCard();
        data.setCnyAvailable(cny.getAvailable());
        data.setCnyFrozen(cny.getFrozen());
        data.setCnyProductIncome(cny.getProductIncome());
        data.setCnyAssistValue(cny.getAssistValue());
        data.setUsdtAvailable(usdt.getAvailable());
        data.setUsdtFrozen(usdt.getFrozen());
        data.setUsdtProductIncome(usdt.getProductIncome());
        data.setUsdtAssistValue(usdt.getAssistValue());
        data.setCny(cny);
        data.setUsdt(usdt);
        data.setWallets(wallets);
        return data;
    }

    @Override
    public void fillAssetSummary(BizMember member)
    {
        if (member == null || member.getMemberId() == null)
        {
            return;
        }
        AppWalletCard card = selectAppWalletCard(member.getMemberId());
        member.setCnyAvailable(card.getCnyAvailable());
        member.setCnyFrozen(card.getCnyFrozen());
        member.setCnyProductIncome(card.getCnyProductIncome());
        member.setCnyAssistValue(card.getCnyAssistValue());
        member.setUsdtAvailable(card.getUsdtAvailable());
        member.setUsdtFrozen(card.getUsdtFrozen());
        member.setUsdtProductIncome(card.getUsdtProductIncome());
        member.setUsdtAssistValue(card.getUsdtAssistValue());
    }

    private AppWalletRow currencyRow(Long memberId, String currency)
    {
        BizWallet wallet = walletMapper.selectWallet(memberId, currency);
        AppWalletRow row = new AppWalletRow();
        row.setCurrency(currency);
        row.setAvailable(nvl(wallet == null ? null : wallet.getAvailable()));
        row.setFrozen(nvl(wallet == null ? null : wallet.getFrozen()));
        row.setProductIncome(nvl(rebateLogMapper.sumAmountByMemberAndCurrency(memberId, currency)));
        row.setAssistValue(BigDecimal.ZERO);
        return row;
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void change(Long memberId, String currency, BigDecimal availableDelta, BigDecimal frozenDelta,
            String bizType, Long bizId, String remark)
    {
        if (availableDelta.compareTo(BigDecimal.ZERO) == 0 && frozenDelta.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        BizWallet wallet = walletMapper.selectWalletForUpdate(memberId, currency);
        if (wallet == null)
        {
            throw new ServiceException("钱包不存在");
        }
        BigDecimal availableBefore = wallet.getAvailable();
        BigDecimal frozenBefore = wallet.getFrozen();
        BigDecimal availableAfter = availableBefore.add(availableDelta);
        BigDecimal frozenAfter = frozenBefore.add(frozenDelta);
        if (availableAfter.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("余额不足");
        }
        if (frozenAfter.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("冻结金额异常");
        }
        wallet.setAvailable(availableAfter);
        wallet.setFrozen(frozenAfter);
        walletMapper.updateWallet(wallet);

        BizWalletLog log = new BizWalletLog();
        log.setMemberId(memberId);
        log.setCurrency(currency);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setAmount(availableDelta.compareTo(BigDecimal.ZERO) != 0 ? availableDelta : frozenDelta);
        log.setAvailableBefore(availableBefore);
        log.setAvailableAfter(availableAfter);
        log.setFrozenBefore(frozenBefore);
        log.setFrozenAfter(frozenAfter);
        log.setRemark(remark);
        walletLogMapper.insertWalletLog(log);
    }

    private AppWalletLogItem toAppItem(BizWalletLog log)
    {
        String bizType = log.getBizType() == null ? "" : log.getBizType();
        String label = bizTypeLabel(bizType);
        String title = displayTitle(bizType, log.getRemark(), label);
        BigDecimal amount = log.getAmount() == null ? BigDecimal.ZERO : log.getAmount();
        AppWalletLogItem item = new AppWalletLogItem();
        item.setLogId(log.getLogId());
        item.setId(log.getLogId());
        item.setTitle(title);
        item.setName(title);
        item.setBizType(bizType);
        item.setBizTypeLabel(label);
        item.setTypeLabel(label);
        item.setAmount(amount);
        item.setCurrency(log.getCurrency());
        item.setDirection(amount.compareTo(BigDecimal.ZERO) < 0 ? "OUT" : "IN");
        item.setDate(formatDate(log.getCreateTime()));
        item.setCreateTime(log.getCreateTime());
        item.setRemark(log.getRemark() == null ? "" : log.getRemark());
        return item;
    }

    private String displayTitle(String bizType, String remark, String label)
    {
        if (BizConstants.BIZ_SUBSCRIBE.equals(bizType))
        {
            return stripPrefix(remark, "认购产品:", label);
        }
        if (BizConstants.BIZ_RECHARGE.equals(bizType))
        {
            return "充值";
        }
        if (BizConstants.BIZ_COMMISSION.equals(bizType))
        {
            return "推广奖金";
        }
        if (BizConstants.BIZ_CHECKIN.equals(bizType))
        {
            return "签到";
        }
        if (BizConstants.BIZ_REBATE.equals(bizType))
        {
            return "系统";
        }
        if (BizConstants.BIZ_LEVEL_REWARD.equals(bizType))
        {
            return stripPrefix(remark, "等级奖励:", "等级奖励");
        }
        if (BizConstants.BIZ_WITHDRAW_FREEZE.equals(bizType) || BizConstants.BIZ_WITHDRAW_SUCCESS.equals(bizType))
        {
            return "提现";
        }
        if (BizConstants.BIZ_WITHDRAW_REJECT.equals(bizType))
        {
            return "提现退回";
        }
        if (!StringUtils.isEmpty(remark))
        {
            return remark;
        }
        return label;
    }

    private String stripPrefix(String remark, String prefix, String fallback)
    {
        if (StringUtils.isEmpty(remark))
        {
            return fallback;
        }
        if (remark.startsWith(prefix))
        {
            String name = remark.substring(prefix.length()).trim();
            return name.length() == 0 ? fallback : name;
        }
        return remark;
    }

    private String bizTypeLabel(String bizType)
    {
        if (BizConstants.BIZ_SUBSCRIBE.equals(bizType))
        {
            return "认购";
        }
        if (BizConstants.BIZ_RECHARGE.equals(bizType))
        {
            return "充值";
        }
        if (BizConstants.BIZ_COMMISSION.equals(bizType))
        {
            return "推广奖金";
        }
        if (BizConstants.BIZ_CHECKIN.equals(bizType))
        {
            return "签到";
        }
        if (BizConstants.BIZ_REBATE.equals(bizType))
        {
            return "产品日返";
        }
        if (BizConstants.BIZ_LEVEL_REWARD.equals(bizType))
        {
            return "等级奖励";
        }
        if (BizConstants.BIZ_WITHDRAW_FREEZE.equals(bizType))
        {
            return "提现";
        }
        if (BizConstants.BIZ_WITHDRAW_SUCCESS.equals(bizType))
        {
            return "提现成功";
        }
        if (BizConstants.BIZ_WITHDRAW_REJECT.equals(bizType))
        {
            return "提现退回";
        }
        return StringUtils.isEmpty(bizType) ? "其他" : bizType;
    }

    private String formatDate(Date time)
    {
        if (time == null)
        {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(time);
    }
}
