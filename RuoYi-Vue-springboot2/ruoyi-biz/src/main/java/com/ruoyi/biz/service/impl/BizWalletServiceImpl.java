package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.api.AppTypedWallet;
import com.ruoyi.biz.api.AppWalletCard;
import com.ruoyi.biz.api.AppWalletLogItem;
import com.ruoyi.biz.api.AppWalletRow;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizWallet;
import com.ruoyi.biz.domain.BizWalletLog;
import com.ruoyi.biz.domain.BizWalletType;
import com.ruoyi.biz.mapper.BizWalletLogMapper;
import com.ruoyi.biz.mapper.BizWalletMapper;
import com.ruoyi.biz.mapper.BizWalletTypeMapper;
import com.ruoyi.biz.service.IBizWalletCreditRuleService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.biz.service.IBizWalletTypeService;
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
    private BizWalletTypeMapper walletTypeMapper;

    @Autowired
    private IBizWalletTypeService walletTypeService;

    @Autowired
    private IBizWalletCreditRuleService creditRuleService;

    @Override
    public List<BizWallet> selectWalletsByMemberId(Long memberId)
    {
        return walletMapper.selectWalletsByMemberId(memberId);
    }

    @Override
    public BizWallet getWallet(Long memberId, String currency)
    {
        return getWallet(memberId, BizConstants.WALLET_BALANCE, currency);
    }

    @Override
    public BizWallet getWallet(Long memberId, String typeCode, String currency)
    {
        return walletMapper.selectWallet(memberId, typeCode, currency);
    }

    @Override
    public void initWallets(Long memberId)
    {
        List<BizWalletType> types = walletTypeMapper.selectWalletTypeList(new BizWalletType());
        if (types == null || types.isEmpty())
        {
            insertIfAbsent(memberId, BizConstants.WALLET_BALANCE, BizConstants.CURRENCY_CNY);
            insertIfAbsent(memberId, BizConstants.WALLET_BALANCE, BizConstants.CURRENCY_USDT);
            insertIfAbsent(memberId, BizConstants.WALLET_PRODUCT, BizConstants.CURRENCY_CNY);
            insertIfAbsent(memberId, BizConstants.WALLET_PRODUCT, BizConstants.CURRENCY_USDT);
            insertIfAbsent(memberId, BizConstants.WALLET_PROMO, BizConstants.CURRENCY_CNY);
            insertIfAbsent(memberId, BizConstants.WALLET_PROMO, BizConstants.CURRENCY_USDT);
            insertIfAbsent(memberId, BizConstants.WALLET_ASSIST, BizConstants.CURRENCY_CNY);
            insertIfAbsent(memberId, BizConstants.WALLET_ASSIST, BizConstants.CURRENCY_USDT);
            return;
        }
        for (int i = 0; i < types.size(); i++)
        {
            String code = types.get(i).getTypeCode();
            insertIfAbsent(memberId, code, BizConstants.CURRENCY_CNY);
            insertIfAbsent(memberId, code, BizConstants.CURRENCY_USDT);
        }
    }

    private void insertIfAbsent(Long memberId, String typeCode, String currency)
    {
        if (walletMapper.selectWallet(memberId, typeCode, currency) == null)
        {
            BizWallet wallet = new BizWallet();
            wallet.setMemberId(memberId);
            wallet.setTypeCode(typeCode);
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
        credit(memberId, currency, amount, bizType, bizId, remark, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void credit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark, String typeCode)
    {
        String code = StringUtils.isEmpty(typeCode) ? creditRuleService.resolveTypeCode(bizType) : typeCode.trim().toUpperCase();
        change(memberId, code, currency, amount, BigDecimal.ZERO, bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void debit(Long memberId, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        String typeCode = BizConstants.BIZ_SUBSCRIBE.equals(bizType)
                ? BizConstants.WALLET_BALANCE
                : creditRuleService.resolveTypeCode(bizType);
        change(memberId, typeCode, currency, amount.negate(), BigDecimal.ZERO, bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(Long memberId, String typeCode, String currency, String direction, BigDecimal amount, String remark, String operator)
    {
        if (memberId == null)
        {
            throw new ServiceException("请选择会员");
        }
        String code = StringUtils.isEmpty(typeCode) ? BizConstants.WALLET_BALANCE : typeCode.trim().toUpperCase();
        String unit = currency == null ? "" : currency.trim().toUpperCase();
        if (!BizConstants.CURRENCY_CNY.equals(unit) && !BizConstants.CURRENCY_USDT.equals(unit))
        {
            throw new ServiceException("币种只能是 CNY 或 USDT");
        }
        String dir = direction == null ? "" : direction.trim().toUpperCase();
        if (!"PLUS".equals(dir) && !"MINUS".equals(dir))
        {
            throw new ServiceException("请选择增加或减少");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("金额必须大于 0");
        }
        if (amount.scale() > 4)
        {
            amount = amount.setScale(4, RoundingMode.DOWN);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("金额必须大于 0");
        }
        if (amount.compareTo(new BigDecimal("999999999999")) > 0)
        {
            throw new ServiceException("金额过大");
        }
        String note = remark == null ? "" : remark.trim();
        if (note.length() == 0)
        {
            throw new ServiceException("请填写备注");
        }
        if (note.length() > 200)
        {
            throw new ServiceException("备注不能超过200字");
        }
        String op = operator == null || operator.trim().length() == 0 ? "admin" : operator.trim();
        String stored = "调账 " + op + ": " + note;
        if (walletTypeService.selectWalletTypeByCode(code) == null)
        {
            throw new ServiceException("钱包类型不存在");
        }
        initWallets(memberId);
        if ("PLUS".equals(dir))
        {
            change(memberId, code, unit, amount, BigDecimal.ZERO, BizConstants.BIZ_ADJUST, null, stored);
        }
        else
        {
            change(memberId, code, unit, amount.negate(), BigDecimal.ZERO, BizConstants.BIZ_ADJUST, null, stored);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long memberId, String typeCode, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, typeCode, currency, amount.negate(), amount, bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeSuccess(Long memberId, String typeCode, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, typeCode, currency, BigDecimal.ZERO, amount.negate(), bizType, bizId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeReject(Long memberId, String typeCode, String currency, BigDecimal amount, String bizType, Long bizId, String remark)
    {
        change(memberId, typeCode, currency, amount, amount.negate(), bizType, bizId, remark);
    }

    @Override
    public List<BizWalletLog> selectWalletLogList(BizWalletLog log)
    {
        return walletLogMapper.selectWalletLogList(log);
    }

    @Override
    public List<AppWalletLogItem> selectAppWalletLogList(Long memberId, String currency, String bizType)
    {
        List<BizWalletLog> logs = walletLogMapper.selectAppWalletLogList(memberId, currency, resolveAppBizTypes(bizType));
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
        data.setTypedWallets(typedWallets(memberId));
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
        member.setCnyFrozen(sumFrozen(member.getMemberId(), BizConstants.CURRENCY_CNY));
        member.setCnyProductIncome(card.getCnyProductIncome());
        member.setCnyAssistValue(card.getCnyAssistValue());
        member.setUsdtAvailable(card.getUsdtAvailable());
        member.setUsdtFrozen(sumFrozen(member.getMemberId(), BizConstants.CURRENCY_USDT));
        member.setUsdtProductIncome(card.getUsdtProductIncome());
        member.setUsdtAssistValue(card.getUsdtAssistValue());
    }

    private List<AppTypedWallet> typedWallets(Long memberId)
    {
        List<BizWallet> rows = walletMapper.selectWalletsByMemberId(memberId);
        List<AppTypedWallet> list = new ArrayList<AppTypedWallet>();
        if (rows == null)
        {
            return list;
        }
        for (int i = 0; i < rows.size(); i++)
        {
            BizWallet wallet = rows.get(i);
            AppTypedWallet item = new AppTypedWallet();
            item.setTypeCode(wallet.getTypeCode());
            item.setTypeName(wallet.getTypeName());
            item.setCurrency(wallet.getCurrency());
            item.setAvailable(nvl(wallet.getAvailable()));
            item.setFrozen(nvl(wallet.getFrozen()));
            list.add(item);
        }
        return list;
    }

    private AppWalletRow currencyRow(Long memberId, String currency)
    {
        BizWallet balance = walletMapper.selectWallet(memberId, BizConstants.WALLET_BALANCE, currency);
        BizWallet product = walletMapper.selectWallet(memberId, BizConstants.WALLET_PRODUCT, currency);
        BizWallet promo = walletMapper.selectWallet(memberId, BizConstants.WALLET_PROMO, currency);
        AppWalletRow row = new AppWalletRow();
        row.setCurrency(currency);
        row.setAvailable(nvl(balance == null ? null : balance.getAvailable()));
        row.setFrozen(sumFrozen(memberId, currency));
        row.setProductIncome(nvl(product == null ? null : product.getAvailable()));
        row.setAssistValue(nvl(promo == null ? null : promo.getAvailable()));
        return row;
    }

    private BigDecimal sumFrozen(Long memberId, String currency)
    {
        List<BizWallet> rows = walletMapper.selectWalletsByMemberId(memberId);
        BigDecimal total = BigDecimal.ZERO;
        if (rows == null)
        {
            return total;
        }
        for (int i = 0; i < rows.size(); i++)
        {
            BizWallet wallet = rows.get(i);
            if (currency.equals(wallet.getCurrency()))
            {
                total = total.add(nvl(wallet.getFrozen()));
            }
        }
        return total;
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void change(Long memberId, String typeCode, String currency, BigDecimal availableDelta, BigDecimal frozenDelta,
            String bizType, Long bizId, String remark)
    {
        if (availableDelta.compareTo(BigDecimal.ZERO) == 0 && frozenDelta.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        String code = StringUtils.isEmpty(typeCode) ? BizConstants.WALLET_BALANCE : typeCode.trim().toUpperCase();
        String unit = currency == null ? "" : currency.trim().toUpperCase();
        initWallets(memberId);
        BizWallet wallet = walletMapper.selectWalletForUpdate(memberId, code, unit);
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
            if (BizConstants.WALLET_BALANCE.equals(code))
            {
                throw new ServiceException("充值余额不足，请先充值");
            }
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
        log.setTypeCode(code);
        log.setCurrency(unit);
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

    private List<String> resolveAppBizTypes(String bizType)
    {
        if (StringUtils.isEmpty(bizType))
        {
            return null;
        }
        Set<String> types = new LinkedHashSet<String>();
        String[] parts = bizType.split("[,|\\s]+");
        for (int i = 0; i < parts.length; i++)
        {
            String key = parts[i] == null ? "" : parts[i].trim();
            if (key.length() == 0)
            {
                continue;
            }
            key = key.toUpperCase();
            if ("WITHDRAW".equals(key) || "WD".equals(key) || "TX".equals(key) || "提现".equals(key))
            {
                types.add(BizConstants.BIZ_WITHDRAW_FREEZE);
                types.add(BizConstants.BIZ_WITHDRAW_SUCCESS);
                types.add(BizConstants.BIZ_WITHDRAW_REJECT);
            }
            else if ("RECHARGE".equals(key) || "CZ".equals(key) || "充值".equals(key))
            {
                types.add(BizConstants.BIZ_RECHARGE);
            }
            else if ("PROMO".equals(key) || "ASSIST".equals(key) || "推广收益".equals(parts[i].trim()))
            {
                BizConstants.addPromoIncomeTypes(types);
            }
            else if ("PRODUCT".equals(key) || "INCOME".equals(key) || "产品收益".equals(parts[i].trim()))
            {
                types.add(BizConstants.BIZ_REBATE);
            }
            else
            {
                types.add(key);
            }
        }
        return types.isEmpty() ? null : new ArrayList<String>(types);
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
        if (BizConstants.BIZ_KYC_REWARD.equals(bizType))
        {
            return "实名注册奖励";
        }
        if (BizConstants.BIZ_INVITE.equals(bizType))
        {
            return "推广奖励";
        }
        if (BizConstants.BIZ_ADJUST.equals(bizType))
        {
            return "后台调账";
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
