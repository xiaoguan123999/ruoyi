package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.api.AppInviteData;
import com.ruoyi.biz.api.AppPromoClaimData;
import com.ruoyi.biz.api.AppPromoData;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.AppPromoClaimBody;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizPromoGrant;
import com.ruoyi.biz.domain.BizPromoRule;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.mapper.BizPromoGrantMapper;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizPromoService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class BizPromoServiceImpl implements IBizPromoService
{
    private static final Logger log = LoggerFactory.getLogger(BizPromoServiceImpl.class);

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private BizPromoGrantMapper grantMapper;

    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizConfigService configService;

    @Override
    public BizPromoRule getRule()
    {
        BizPromoRule rule = new BizPromoRule();
        rule.setEnabled(Boolean.valueOf(boolVal(BizConstants.CONFIG_PROMO_ENABLED, true)));
        rule.setKycSelfEnabled(Boolean.valueOf(boolVal(BizConstants.CONFIG_PROMO_KYC_SELF_ENABLED, true)));
        rule.setKycRewardCny(decimal(BizConstants.CONFIG_PROMO_KYC_SELF_CNY, "14"));
        rule.setKycRewardUsdt(decimal(BizConstants.CONFIG_PROMO_KYC_SELF_USDT, "2"));
        rule.setInviteEnabled(Boolean.valueOf(boolVal(BizConstants.CONFIG_PROMO_INVITE_ENABLED, true)));
        rule.setInviteAmount(decimal(BizConstants.CONFIG_PROMO_INVITE_AMOUNT, "2"));
        rule.setInviteCurrency(strVal(BizConstants.CONFIG_PROMO_INVITE_CURRENCY, BizConstants.CURRENCY_CNY).toUpperCase());
        rule.setLockParent(Boolean.valueOf(boolVal(BizConstants.CONFIG_PROMO_LOCK_PARENT, true)));
        rule.setTeamEnabled(Boolean.valueOf(boolVal(BizConstants.CONFIG_TEAM_ENABLED, true)));
        rule.setTeamRateL1(configService.getTeamRate(1));
        rule.setTeamRateL2(configService.getTeamRate(2));
        rule.setTeamRateL3(configService.getTeamRate(3));
        rule.setRuleText(strVal(BizConstants.CONFIG_PROMO_RULE_TEXT, defaultRuleText(rule)));
        return rule;
    }

    @Override
    public void saveRule(BizPromoRule rule)
    {
        if (rule == null)
        {
            throw new ServiceException("请填写规则");
        }
        BigDecimal kycCny = nvl(rule.getKycRewardCny());
        BigDecimal kycUsdt = nvl(rule.getKycRewardUsdt());
        BigDecimal inviteAmount = nvl(rule.getInviteAmount());
        BigDecimal l1 = nvl(rule.getTeamRateL1());
        BigDecimal l2 = nvl(rule.getTeamRateL2());
        BigDecimal l3 = nvl(rule.getTeamRateL3());
        assertNonNegative(kycCny, "实名注册奖励人民币");
        assertNonNegative(kycUsdt, "实名注册奖励USDT");
        assertNonNegative(inviteAmount, "推广奖励金额");
        assertRate(l1, "一级返佣");
        assertRate(l2, "二级返佣");
        assertRate(l3, "三级返佣");
        String inviteCurrency = StringUtils.isEmpty(rule.getInviteCurrency()) ? BizConstants.CURRENCY_CNY
                : rule.getInviteCurrency().toUpperCase();
        if (!BizConstants.CURRENCY_CNY.equals(inviteCurrency) && !BizConstants.CURRENCY_USDT.equals(inviteCurrency))
        {
            throw new ServiceException("推广奖励币种只能是CNY或USDT");
        }
        rule.setKycRewardCny(kycCny);
        rule.setKycRewardUsdt(kycUsdt);
        rule.setInviteAmount(inviteAmount);
        rule.setInviteCurrency(inviteCurrency);
        rule.setTeamRateL1(l1);
        rule.setTeamRateL2(l2);
        rule.setTeamRateL3(l3);
        String ruleText = rule.getRuleText();
        if (StringUtils.isEmpty(ruleText))
        {
            ruleText = defaultRuleText(rule);
        }
        if (ruleText.length() > 500)
        {
            throw new ServiceException("规则说明不能超过500字");
        }
        saveConfig(BizConstants.CONFIG_PROMO_ENABLED, "推广规则总开关",
                boolStr(rule.getEnabled(), true), "false关闭实名自领和邀请奖励");
        saveConfig(BizConstants.CONFIG_PROMO_KYC_SELF_ENABLED, "实名注册奖励开关",
                boolStr(rule.getKycSelfEnabled(), true), "实名后可选CNY或USDT领一次");
        saveConfig(BizConstants.CONFIG_PROMO_KYC_SELF_CNY, "实名注册奖励CNY", fmt(kycCny), "实名注册奖励人民币金额");
        saveConfig(BizConstants.CONFIG_PROMO_KYC_SELF_USDT, "实名注册奖励USDT", fmt(kycUsdt), "实名注册奖励USDT金额");
        saveConfig(BizConstants.CONFIG_PROMO_INVITE_ENABLED, "实名推广奖励开关",
                boolStr(rule.getInviteEnabled(), true), "被邀请人实名后给邀请人发奖");
        saveConfig(BizConstants.CONFIG_PROMO_INVITE_AMOUNT, "实名推广奖励金额", fmt(inviteAmount), "每成功邀请1名实名用户的奖励");
        saveConfig(BizConstants.CONFIG_PROMO_INVITE_CURRENCY, "实名推广奖励币种", inviteCurrency, "邀请奖励币种 CNY或USDT");
        saveConfig(BizConstants.CONFIG_PROMO_LOCK_PARENT, "邀请后不可改上级",
                boolStr(rule.getLockParent(), true), "注册时绑定邀请码后不可转移");
        saveConfig(BizConstants.CONFIG_TEAM_ENABLED, "团队返佣开关",
                boolStr(rule.getTeamEnabled(), true), "false关闭充值三级返佣");
        saveConfig(BizConstants.CONFIG_RATE_L1, "团队一级分佣比例", fmt(l1), "充值一级分佣百分比");
        saveConfig(BizConstants.CONFIG_RATE_L2, "团队二级分佣比例", fmt(l2), "充值二级分佣百分比");
        saveConfig(BizConstants.CONFIG_RATE_L3, "团队三级分佣比例", fmt(l3), "充值三级分佣百分比");
        saveConfig(BizConstants.CONFIG_INVITE_REWARD, "邀请奖励金额", fmt(inviteAmount), "每成功邀请1名实名用户给邀请人的金额");
        saveConfig(BizConstants.CONFIG_PROMO_RULE_TEXT, "注册推广规则说明", ruleText, "App邀请/规则页展示全文");
    }

    @Override
    public AppPromoData getAppPromo(Long memberId)
    {
        BizPromoRule rule = getRule();
        AppPromoData data = new AppPromoData();
        data.setEnabled(rule.getEnabled());
        data.setKycSelfEnabled(rule.getKycSelfEnabled());
        data.setKycRewardCny(rule.getKycRewardCny());
        data.setKycRewardUsdt(rule.getKycRewardUsdt());
        data.setInviteEnabled(rule.getInviteEnabled());
        data.setInviteAmount(rule.getInviteAmount());
        data.setInviteCurrency(rule.getInviteCurrency());
        data.setLockParent(rule.getLockParent());
        data.setTeamEnabled(rule.getTeamEnabled());
        data.setTeamRateL1(rule.getTeamRateL1());
        data.setTeamRateL2(rule.getTeamRateL2());
        data.setTeamRateL3(rule.getTeamRateL3());
        data.setRuleText(rule.getRuleText());
        if (memberId == null)
        {
            return data;
        }
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            return data;
        }
        data.setKycStatus(member.getKycStatus());
        BizPromoGrant selfGrant = grantMapper.selectByTypeAndFrom(BizConstants.PROMO_KYC_SELF, memberId);
        boolean claimed = selfGrant != null;
        data.setKycRewardClaimed(Boolean.valueOf(claimed));
        if (claimed)
        {
            data.setClaimedCurrency(selfGrant.getCurrency());
            data.setClaimedAmount(selfGrant.getAmount());
        }
        data.setKycRewardClaimable(Boolean.valueOf(canClaimKyc(rule, member, claimed)));
        return data;
    }

    @Override
    public void fillInvite(AppInviteData data)
    {
        if (data == null)
        {
            return;
        }
        BizPromoRule rule = getRule();
        data.setInviteAmount(rule.getInviteAmount());
        data.setInviteCurrency(rule.getInviteCurrency());
        data.setKycRewardCny(rule.getKycRewardCny());
        data.setKycRewardUsdt(rule.getKycRewardUsdt());
        data.setTeamRateL1(rule.getTeamRateL1());
        data.setTeamRateL2(rule.getTeamRateL2());
        data.setTeamRateL3(rule.getTeamRateL3());
        data.setRuleText(rule.getRuleText());
        data.setLockParent(rule.getLockParent());
        boolean payInvite = bool(rule.getEnabled()) && bool(rule.getInviteEnabled())
                && nvl(rule.getInviteAmount()).compareTo(BigDecimal.ZERO) > 0;
        data.setReward(Integer.valueOf(payInvite ? nvl(rule.getInviteAmount()).intValue() : 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantInviteOnKyc(Long memberId)
    {
        if (memberId == null)
        {
            return;
        }
        BizPromoRule rule = getRule();
        if (!bool(rule.getEnabled()) || !bool(rule.getInviteEnabled()))
        {
            return;
        }
        BigDecimal amount = nvl(rule.getInviteAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        BizMember invitee = memberMapper.selectMemberById(memberId);
        if (invitee == null || invitee.getParentId() == null || !BizConstants.KYC_DONE.equals(invitee.getKycStatus()))
        {
            return;
        }
        BizMember parent = memberMapper.selectMemberById(invitee.getParentId());
        if (parent == null || BizConstants.STATUS_DISABLE.equals(parent.getStatus()))
        {
            return;
        }
        if (grantMapper.selectByTypeAndFrom(BizConstants.PROMO_INVITE, memberId) != null)
        {
            return;
        }
        String currency = rule.getInviteCurrency();
        try
        {
            configService.assertCurrencyEnabled(currency);
        }
        catch (ServiceException e)
        {
            log.warn("invite reward skip memberId={}: {}", memberId, e.getMessage());
            return;
        }
        BizPromoGrant grant = new BizPromoGrant();
        grant.setMemberId(parent.getMemberId());
        grant.setFromMemberId(memberId);
        grant.setGrantType(BizConstants.PROMO_INVITE);
        grant.setCurrency(currency);
        grant.setAmount(amount);
        grant.setStatus("1");
        grant.setRemark("推广奖励");
        try
        {
            grantMapper.insertGrant(grant);
        }
        catch (DuplicateKeyException e)
        {
            return;
        }
        walletService.credit(parent.getMemberId(), currency, amount, BizConstants.BIZ_INVITE, grant.getGrantId(),
                "推广奖励");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPromoClaimData claimKycReward(Long memberId, AppPromoClaimBody body)
    {
        if (body == null || StringUtils.isEmpty(body.getCurrency()))
        {
            throw new ServiceException("请选择领取币种");
        }
        String currency = body.getCurrency().toUpperCase();
        if (!BizConstants.CURRENCY_CNY.equals(currency) && !BizConstants.CURRENCY_USDT.equals(currency))
        {
            throw new ServiceException("只能选择人民币或USDT");
        }
        BizPromoRule rule = getRule();
        if (!bool(rule.getEnabled()) || !bool(rule.getKycSelfEnabled()))
        {
            throw new ServiceException("实名注册奖励暂未开放");
        }
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        if (!BizConstants.KYC_DONE.equals(member.getKycStatus()))
        {
            throw new ServiceException("请先完成实名认证");
        }
        if (grantMapper.selectByTypeAndFrom(BizConstants.PROMO_KYC_SELF, memberId) != null)
        {
            throw new ServiceException("已领取实名注册奖励");
        }
        BigDecimal amount = BizConstants.CURRENCY_USDT.equals(currency) ? nvl(rule.getKycRewardUsdt())
                : nvl(rule.getKycRewardCny());
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("该币种奖励金额为0，请选择另一种");
        }
        configService.assertCurrencyEnabled(currency);
        BizPromoGrant grant = new BizPromoGrant();
        grant.setMemberId(memberId);
        grant.setFromMemberId(memberId);
        grant.setGrantType(BizConstants.PROMO_KYC_SELF);
        grant.setCurrency(currency);
        grant.setAmount(amount);
        grant.setStatus("1");
        grant.setRemark("实名注册奖励");
        try
        {
            grantMapper.insertGrant(grant);
        }
        catch (DuplicateKeyException e)
        {
            throw new ServiceException("已领取实名注册奖励");
        }
        walletService.credit(memberId, currency, amount, BizConstants.BIZ_KYC_REWARD, grant.getGrantId(), "实名注册奖励");
        AppPromoClaimData data = new AppPromoClaimData();
        data.setCurrency(currency);
        data.setAmount(amount);
        return data;
    }

    @Override
    public List<BizPromoGrant> selectGrantList(BizPromoGrant grant)
    {
        return grantMapper.selectGrantList(grant);
    }

    private boolean canClaimKyc(BizPromoRule rule, BizMember member, boolean claimed)
    {
        if (claimed || member == null || !BizConstants.KYC_DONE.equals(member.getKycStatus()))
        {
            return false;
        }
        if (!bool(rule.getEnabled()) || !bool(rule.getKycSelfEnabled()))
        {
            return false;
        }
        return nvl(rule.getKycRewardCny()).compareTo(BigDecimal.ZERO) > 0
                || nvl(rule.getKycRewardUsdt()).compareTo(BigDecimal.ZERO) > 0;
    }

    private String defaultRuleText(BizPromoRule rule)
    {
        String inviteUnit = BizConstants.CURRENCY_USDT.equalsIgnoreCase(rule.getInviteCurrency()) ? " USDT" : " 元";
        StringBuilder sb = new StringBuilder();
        sb.append("用户注册与推广奖励规则：\n");
        sb.append("一、实名注册奖励\n");
        sb.append("新用户完成注册并通过实名认证后，可获得 ").append(fmt(nvl(rule.getKycRewardCny()))).append(" 元或 ")
                .append(fmt(nvl(rule.getKycRewardUsdt()))).append(" USDT 平台余额，两种奖励方式任选其一。\n");
        sb.append("二、实名推广奖励\n");
        sb.append("每成功邀请 1 名新用户完成实名注册，邀请人可获得 ").append(fmt(nvl(rule.getInviteAmount()))).append(inviteUnit)
                .append("推广奖励。上下级不可以转移，请核对好正确的邀请码再注册。\n");
        sb.append("三、团队返佣机制\n");
        sb.append("一级返佣 ").append(fmt(nvl(rule.getTeamRateL1()))).append("%、二级返佣 ")
                .append(fmt(nvl(rule.getTeamRateL2()))).append("%、三级返佣 ").append(fmt(nvl(rule.getTeamRateL3())))
                .append("%\n\n");
        sb.append("奖励资格、返佣计算及发放结果以平台系统实际核算为准；如发现异常注册、批量账户或其他违规行为，平台有权取消相关奖励资格。");
        return sb.toString();
    }

    private void saveConfig(String key, String name, String value, String remark)
    {
        SysConfig existing = sysConfigMapper.checkConfigKeyUnique(key);
        if (existing == null)
        {
            SysConfig config = new SysConfig();
            config.setConfigName(name);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("N");
            config.setRemark(remark);
            sysConfigService.insertConfig(config);
        }
        else
        {
            existing.setConfigName(name);
            existing.setConfigValue(value);
            existing.setRemark(remark);
            sysConfigService.updateConfig(existing);
        }
    }

    private boolean boolVal(String key, boolean defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private String strVal(String key, String defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    private BigDecimal decimal(String key, String defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            value = defaultValue;
        }
        try
        {
            return new BigDecimal(value);
        }
        catch (Exception e)
        {
            return new BigDecimal(defaultValue);
        }
    }

    private String boolStr(Boolean value, boolean defaultValue)
    {
        boolean v = value == null ? defaultValue : value.booleanValue();
        return v ? "true" : "false";
    }

    private boolean bool(Boolean value)
    {
        return value != null && value.booleanValue();
    }

    private BigDecimal nvl(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String fmt(BigDecimal v)
    {
        return nvl(v).stripTrailingZeros().toPlainString();
    }

    private void assertNonNegative(BigDecimal v, String name)
    {
        if (v.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException(name + "不能为负数");
        }
    }

    private void assertRate(BigDecimal v, String name)
    {
        if (v.compareTo(BigDecimal.ZERO) < 0 || v.compareTo(new BigDecimal("100")) > 0)
        {
            throw new ServiceException(name + "须在0到100之间");
        }
    }
}
