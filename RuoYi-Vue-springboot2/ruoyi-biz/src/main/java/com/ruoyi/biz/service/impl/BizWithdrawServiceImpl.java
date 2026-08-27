package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.domain.BizWithdrawRule;
import com.ruoyi.biz.mapper.BizWithdrawMapper;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.biz.service.IBizWalletCreditRuleService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.biz.service.IBizWalletTypeService;
import com.ruoyi.biz.service.IBizWithdrawService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizWithdrawServiceImpl implements IBizWithdrawService
{
    @Autowired
    private BizWithdrawMapper withdrawMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizConfigService configService;

    @Autowired
    private IBizWalletTypeService walletTypeService;

    @Autowired
    private IBizWalletCreditRuleService creditRuleService;

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
    public BizWithdrawRule getRule()
    {
        BizWithdrawRule rule = new BizWithdrawRule();
        rule.setMinCny(configService.getWithdrawMinAmount(BizConstants.CURRENCY_CNY));
        rule.setMaxCny(nvlMax(configService.getWithdrawMaxAmount(BizConstants.CURRENCY_CNY)));
        rule.setMinUsdt(configService.getWithdrawMinAmount(BizConstants.CURRENCY_USDT));
        rule.setMaxUsdt(nvlMax(configService.getWithdrawMaxAmount(BizConstants.CURRENCY_USDT)));
        rule.setUsdtEnabled(configService.isUsdtEnabled());
        rule.setFeeRate(configService.getWithdrawFeeRate());
        rule.setProductWalletType(creditRuleService.resolveTypeCode(BizConstants.BIZ_WITHDRAW_PRODUCT));
        rule.setPromoWalletType(creditRuleService.resolveTypeCode(BizConstants.BIZ_WITHDRAW_PROMO));
        return rule;
    }

    @Override
    public void saveRule(BizWithdrawRule rule)
    {
        if (rule == null)
        {
            throw new ServiceException("请填写提现规则");
        }
        BigDecimal minCny = requireMin(rule.getMinCny(), "人民币最低提现");
        BigDecimal maxCny = normalizeMax(rule.getMaxCny());
        BigDecimal minUsdt = requireMin(rule.getMinUsdt(), "USDT最低提现");
        BigDecimal maxUsdt = normalizeMax(rule.getMaxUsdt());
        assertMaxNotBelowMin(minCny, maxCny, "人民币");
        assertMaxNotBelowMin(minUsdt, maxUsdt, "USDT");
        boolean usdtEnabled = rule.getUsdtEnabled() == null ? true : rule.getUsdtEnabled().booleanValue();
        configService.saveConfig(BizConstants.CONFIG_WITHDRAW_MIN, "提现最低金额", fmt(minCny), "人民币最低提现金额");
        configService.saveConfig(BizConstants.CONFIG_WITHDRAW_MAX, "提现最高金额", fmt(maxCny), "人民币最高提现，0表示不限");
        configService.saveConfig(BizConstants.CONFIG_WITHDRAW_MIN_USDT, "USDT最低提现", fmt(minUsdt), "USDT最低提现金额");
        configService.saveConfig(BizConstants.CONFIG_WITHDRAW_MAX_USDT, "USDT最高提现", fmt(maxUsdt), "USDT最高提现，0表示不限");
        configService.saveConfig(BizConstants.CONFIG_USDT_ENABLED, "USDT业务开关", usdtEnabled ? "true" : "false", "false表示USDT充提暂未开放");
        configService.saveConfig(BizConstants.CONFIG_WITHDRAW_FEE_RATE, "提现手续费比例", fmt(normalizeFeeRate(rule.getFeeRate())), "百分数，3表示3%，0表示免手续费");
        String operator = SecurityUtils.getUsername();
        String productWallet = StringUtils.isEmpty(rule.getProductWalletType())
                ? BizConstants.WALLET_PRODUCT : rule.getProductWalletType();
        String promoWallet = StringUtils.isEmpty(rule.getPromoWalletType())
                ? BizConstants.WALLET_PROMO : rule.getPromoWalletType();
        creditRuleService.saveTypeCodeByBizType(BizConstants.BIZ_WITHDRAW_PRODUCT, productWallet, operator);
        creditRuleService.saveTypeCodeByBizType(BizConstants.BIZ_WITHDRAW_PROMO, promoWallet, operator);
        configService.refreshCache();
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
        if (StringUtils.isEmpty(accountInfo))
        {
            throw new ServiceException(BizConstants.CURRENCY_USDT.equals(payCurrency) ? "请填写USDT收款地址" : "请填写收款账户");
        }
        BigDecimal minAmount = configService.getWithdrawMinAmount(payCurrency);
        if (amount.compareTo(minAmount) < 0)
        {
            throw new ServiceException("最低提现金额为" + minAmount + " " + payCurrency);
        }
        BigDecimal maxAmount = configService.getWithdrawMaxAmount(payCurrency);
        if (maxAmount != null && amount.compareTo(maxAmount) > 0)
        {
            throw new ServiceException("最高提现金额为" + maxAmount + " " + payCurrency);
        }
        BigDecimal feeRate = configService.getWithdrawFeeRate();
        BigDecimal feeAmount = calcFee(amount, feeRate);
        BigDecimal arrivalAmount = amount.subtract(feeAmount);
        if (arrivalAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("手续费不能大于或等于提现金额");
        }
        String typeCode = resolveWalletTypeCode(remark);
        walletTypeService.assertCanWithdraw(typeCode, memberId, payCurrency);
        BizWithdraw withdraw = new BizWithdraw();
        withdraw.setMemberId(memberId);
        withdraw.setCurrency(payCurrency);
        withdraw.setWalletTypeCode(typeCode);
        withdraw.setAmount(amount);
        withdraw.setFeeAmount(feeAmount);
        withdraw.setArrivalAmount(arrivalAmount);
        withdraw.setAccountInfo(accountInfo.trim());
        withdraw.setPayMethod(BizConstants.CURRENCY_USDT.equals(payCurrency) ? BizConstants.PAY_USDT : BizConstants.PAY_ALIPAY);
        withdraw.setStatus(BizConstants.AUDIT_PENDING);
        withdraw.setRemark(remark);
        withdrawMapper.insertWithdraw(withdraw);
        walletService.freeze(memberId, typeCode, payCurrency, amount, BizConstants.BIZ_WITHDRAW_FREEZE,
                withdraw.getWithdrawId(), StringUtils.isEmpty(remark) ? "提现冻结" : remark);
        return withdraw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long withdrawId, String status, String auditBy, String auditRemark, String payProofUrl)
    {
        BizWithdraw withdraw = withdrawMapper.selectWithdrawById(withdrawId);
        if (withdraw == null)
        {
            throw new ServiceException("提现单不存在");
        }
        if (!BizConstants.AUDIT_PENDING.equals(withdraw.getStatus()))
        {
            throw new ServiceException("该提现单已处理");
        }
        if (!BizConstants.AUDIT_PASS.equals(status) && !BizConstants.AUDIT_REJECT.equals(status))
        {
            throw new ServiceException("审核状态不正确");
        }
        if (BizConstants.AUDIT_REJECT.equals(status) && StringUtils.isEmpty(auditRemark))
        {
            throw new ServiceException("请填写拒绝原因");
        }
        withdraw.setStatus(status);
        withdraw.setAuditBy(auditBy);
        withdraw.setAuditTime(new Date());
        withdraw.setAuditRemark(auditRemark);
        withdraw.setPayProofUrl(payProofUrl);
        if (withdrawMapper.updateWithdrawIfPending(withdraw) <= 0)
        {
            throw new ServiceException("该提现单已处理");
        }
        String typeCode = StringUtils.isEmpty(withdraw.getWalletTypeCode())
                ? resolveWalletTypeCode(withdraw.getRemark())
                : withdraw.getWalletTypeCode();
        if (BizConstants.AUDIT_PASS.equals(status))
        {
            walletService.unfreezeSuccess(withdraw.getMemberId(), typeCode, withdraw.getCurrency(), withdraw.getAmount(),
                    BizConstants.BIZ_WITHDRAW_SUCCESS, withdraw.getWithdrawId(), "提现已打款");
        }
        else
        {
            walletService.unfreezeReject(withdraw.getMemberId(), typeCode, withdraw.getCurrency(), withdraw.getAmount(),
                    BizConstants.BIZ_WITHDRAW_REJECT, withdraw.getWithdrawId(), "提现拒绝解冻");
        }
    }

    private String resolveWalletTypeCode(String remark)
    {
        if (StringUtils.isEmpty(remark))
        {
            return BizConstants.WALLET_PRODUCT;
        }
        String text = remark.trim();
        String upper = text.toUpperCase();
        if (text.startsWith("推广收益") || upper.startsWith("PROMO"))
        {
            return creditRuleService.resolveTypeCode(BizConstants.BIZ_WITHDRAW_PROMO);
        }
        if (text.startsWith("产品收益") || upper.startsWith("PRODUCT"))
        {
            return creditRuleService.resolveTypeCode(BizConstants.BIZ_WITHDRAW_PRODUCT);
        }
        if (text.startsWith("余额") || upper.startsWith("BALANCE"))
        {
            return BizConstants.WALLET_BALANCE;
        }
        if (text.startsWith("助力") || upper.startsWith("ASSIST"))
        {
            return creditRuleService.resolveTypeCode(BizConstants.BIZ_WITHDRAW_PROMO);
        }
        return creditRuleService.resolveTypeCode(BizConstants.BIZ_WITHDRAW_PRODUCT);
    }

    private BigDecimal calcFee(BigDecimal amount, BigDecimal feeRate)
    {
        if (amount == null || feeRate == null || feeRate.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return amount.multiply(feeRate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeFeeRate(BigDecimal feeRate)
    {
        BigDecimal rate = feeRate == null ? new BigDecimal("3") : feeRate;
        if (rate.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("手续费比例不能小于0");
        }
        if (rate.compareTo(new BigDecimal("100")) > 0)
        {
            throw new ServiceException("手续费比例不能超过100");
        }
        return rate;
    }

    private BigDecimal requireMin(BigDecimal amount, String label)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException(label + "必须大于0");
        }
        return amount;
    }

    private BigDecimal normalizeMax(BigDecimal amount)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        return amount;
    }

    private void assertMaxNotBelowMin(BigDecimal min, BigDecimal max, String label)
    {
        if (max.compareTo(BigDecimal.ZERO) > 0 && max.compareTo(min) < 0)
        {
            throw new ServiceException(label + "最高提现不能低于最低提现");
        }
    }

    private BigDecimal nvlMax(BigDecimal amount)
    {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String fmt(BigDecimal amount)
    {
        return amount.stripTrailingZeros().toPlainString();
    }
}
