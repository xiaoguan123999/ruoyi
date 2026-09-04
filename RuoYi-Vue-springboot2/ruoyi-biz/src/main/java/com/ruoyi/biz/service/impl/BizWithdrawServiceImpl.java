package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.domain.BizWithdrawRule;
import com.ruoyi.biz.mapper.BizMemberMapper;
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
    @Lazy
    private IBizWithdrawService self;

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

    @Autowired
    private BizMemberMapper memberMapper;

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
        rule.setNeedKyc(Boolean.valueOf(configService.isWithdrawNeedKyc()));
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
        boolean needKyc = rule.getNeedKyc() == null ? configService.isWithdrawNeedKyc() : rule.getNeedKyc().booleanValue();
        configService.saveConfig(BizConstants.CONFIG_WITHDRAW_NEED_KYC, "提现需实名", needKyc ? "true" : "false",
                "true表示必须完成实名才能提现");
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
        return apply(memberId, currency, amount, accountInfo, remark, googleCode, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizWithdraw apply(Long memberId, String currency, BigDecimal amount, String accountInfo, String remark,
            String googleCode, String payMethod)
    {
        googleAuthService.assertForWithdraw(memberId, googleCode);
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        if (BizConstants.WITHDRAW_FORBID.equals(member.getWithdrawStatus()))
        {
            throw new ServiceException("您的账号已被禁止提现");
        }
        if (configService.isWithdrawNeedKyc() && !BizConstants.KYC_DONE.equals(member.getKycStatus()))
        {
            throw new ServiceException("请先完成实名认证");
        }
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
        withdraw.setPayMethod(resolvePayMethod(payCurrency, payMethod, accountInfo, remark));
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
        String fromStatus = withdraw.getStatus();
        boolean reviewing = BizConstants.AUDIT_PENDING.equals(fromStatus);
        boolean payPending = BizConstants.WD_PAY_PENDING.equals(fromStatus);
        if (!reviewing && !payPending)
        {
            throw new ServiceException("该提现单已处理");
        }
        if (BizConstants.WD_PAY_PENDING.equals(status))
        {
            if (!reviewing)
            {
                throw new ServiceException("只有审核中的提现可以改为待打款");
            }
        }
        else if (BizConstants.AUDIT_PASS.equals(status))
        {
            if (!payPending)
            {
                throw new ServiceException("请先改为待打款，再标记提现成功");
            }
        }
        else if (BizConstants.AUDIT_REJECT.equals(status))
        {
            if (StringUtils.isEmpty(auditRemark))
            {
                throw new ServiceException("请填写提现失败原因");
            }
        }
        else
        {
            throw new ServiceException("审核状态不正确");
        }
        withdraw.setStatus(status);
        withdraw.setAuditBy(auditBy);
        withdraw.setAuditTime(new Date());
        withdraw.setAuditRemark(auditRemark);
        withdraw.setPayProofUrl(payProofUrl);
        if (withdrawMapper.updateWithdrawIfStatus(withdraw, fromStatus) <= 0)
        {
            throw new ServiceException("该提现单已处理");
        }
        if (BizConstants.WD_PAY_PENDING.equals(status))
        {
            return;
        }
        String typeCode = StringUtils.isEmpty(withdraw.getWalletTypeCode())
                ? resolveWalletTypeCode(withdraw.getRemark())
                : withdraw.getWalletTypeCode();
        if (BizConstants.AUDIT_PASS.equals(status))
        {
            walletService.unfreezeSuccess(withdraw.getMemberId(), typeCode, withdraw.getCurrency(), withdraw.getAmount(),
                    BizConstants.BIZ_WITHDRAW_SUCCESS, withdraw.getWithdrawId(), "提现成功");
        }
        else
        {
            walletService.unfreezeReject(withdraw.getMemberId(), typeCode, withdraw.getCurrency(), withdraw.getAmount(),
                    BizConstants.BIZ_WITHDRAW_REJECT, withdraw.getWithdrawId(), "提现失败解冻");
        }
    }

    @Override
    public String auditBatch(Long[] ids, String status, String auditBy, String auditRemark, String payProofUrl)
    {
        if (ids == null || ids.length == 0)
        {
            throw new ServiceException("请选择提现单");
        }
        if (ids.length > 2000)
        {
            throw new ServiceException("单次最多处理 2000 笔，请缩小筛选范围");
        }
        int ok = 0;
        int fail = 0;
        StringBuilder errors = new StringBuilder();
        for (int i = 0; i < ids.length; i++)
        {
            Long id = ids[i];
            try
            {
                self.audit(id, status, auditBy, auditRemark, payProofUrl);
                ok++;
            }
            catch (Exception e)
            {
                fail++;
                if (errors.length() < 400)
                {
                    errors.append("单号").append(id).append("：").append(e.getMessage()).append("；");
                }
            }
        }
        if (ok == 0)
        {
            throw new ServiceException(fail > 0 ? errors.toString() : "没有可处理的提现单");
        }
        String msg = "成功 " + ok + " 笔";
        if (fail > 0)
        {
            msg = msg + "，失败 " + fail + " 笔。" + errors.toString();
        }
        return msg;
    }

    private String resolvePayMethod(String currency, String payMethod, String accountInfo, String remark)
    {
        if (BizConstants.CURRENCY_USDT.equals(currency))
        {
            return BizConstants.PAY_USDT;
        }
        String method = payMethod == null ? "" : payMethod.trim().toUpperCase();
        if (BizConstants.PAY_USDT.equals(method) || BizConstants.PAY_BANK.equals(method))
        {
            return method;
        }
        if (BizWithdraw.looksLikeBank(accountInfo) || BizWithdraw.looksLikeBank(remark))
        {
            return BizConstants.PAY_BANK;
        }
        if (BizConstants.PAY_ALIPAY.equals(method))
        {
            return method;
        }
        return BizConstants.PAY_ALIPAY;
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
