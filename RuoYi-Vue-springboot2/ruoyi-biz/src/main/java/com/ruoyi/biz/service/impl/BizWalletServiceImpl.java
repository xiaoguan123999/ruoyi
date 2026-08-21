package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.api.AppWalletCard;
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
}
