package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizLevelRewardGrant;
import com.ruoyi.biz.domain.BizLevelRewardPayBody;
import com.ruoyi.biz.domain.BizLevelRewardRule;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.mapper.BizLevelMapper;
import com.ruoyi.biz.mapper.BizLevelRewardGrantMapper;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.mapper.BizOrderMapper;
import com.ruoyi.biz.mapper.BizRechargeMapper;
import com.ruoyi.biz.service.IBizLevelRewardService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class BizLevelRewardServiceImpl implements IBizLevelRewardService
{
    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private BizLevelMapper levelMapper;

    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private BizOrderMapper orderMapper;

    @Autowired
    private BizRechargeMapper rechargeMapper;

    @Autowired
    private BizLevelRewardGrantMapper grantMapper;

    @Autowired
    private IBizWalletService walletService;

    @Override
    public BizLevelRewardRule getRule()
    {
        BizLevelRewardRule rule = new BizLevelRewardRule();
        rule.setEnabled(Boolean.valueOf(boolVal(BizConstants.CONFIG_LEVEL_REWARD_ENABLED, true)));
        rule.setMixedPayCurrency(strVal(BizConstants.CONFIG_LEVEL_REWARD_MIXED_PAY, BizConstants.CURRENCY_USDT));
        rule.setPerformanceSource(strVal(BizConstants.CONFIG_LEVEL_REWARD_PERF_SOURCE, "SUBSCRIBE"));
        rule.setIncludeSelf(Boolean.valueOf(boolVal(BizConstants.CONFIG_LEVEL_REWARD_INCLUDE_SELF, false)));
        rule.setValidNeedKyc(Boolean.valueOf(boolVal(BizConstants.CONFIG_LEVEL_REWARD_NEED_KYC, true)));
        rule.setValidNeedOrder(Boolean.valueOf(boolVal(BizConstants.CONFIG_LEVEL_REWARD_NEED_ORDER, true)));
        rule.setRuleText(strVal(BizConstants.CONFIG_LEVEL_REWARD_TEXT,
                "启航、探索、开拓、星耀：达成条件后自动获得1次等级奖励。领航、星域：每月达成条件后联系客服领取1次。星链：达成条件后获永久领取资格，联系客服领取。团队同时有人民币和USDT业绩时发放USDT。最终以系统核算为准。"));
        rule.setHint(strVal(BizConstants.CONFIG_LEVEL_REWARD_HINT,
                "注：成员个人累计认购金额达到 ¥10,000 或 1,429 USDT 后，方可计入团队等级考核。请遵循平台规则，严禁作弊行为，一经发现将取消奖励资格。"));
        return rule;
    }

    @Override
    public void saveRule(BizLevelRewardRule rule)
    {
        if (rule == null)
        {
            throw new ServiceException("请填写规则");
        }
        String mixed = StringUtils.isEmpty(rule.getMixedPayCurrency()) ? BizConstants.CURRENCY_USDT
                : rule.getMixedPayCurrency().toUpperCase();
        if (!BizConstants.CURRENCY_CNY.equals(mixed) && !BizConstants.CURRENCY_USDT.equals(mixed))
        {
            throw new ServiceException("混合业绩发放币种只能是CNY或USDT");
        }
        String source = StringUtils.isEmpty(rule.getPerformanceSource()) ? "SUBSCRIBE" : rule.getPerformanceSource().toUpperCase();
        if (!"SUBSCRIBE".equals(source) && !"RECHARGE".equals(source) && !"BOTH".equals(source))
        {
            throw new ServiceException("团队业绩口径不正确");
        }
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_ENABLED, "等级奖励开关",
                boolStr(rule.getEnabled(), true), "false表示关闭成长激励金");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_MIXED_PAY, "混合业绩发放币种", mixed,
                "团队同时有人民币和USDT业绩时发这个币种");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_PERF_SOURCE, "团队业绩口径", source,
                "SUBSCRIBE认购 RECHARGE充值 BOTH两者相加");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_INCLUDE_SELF, "团队业绩含本人",
                boolStr(rule.getIncludeSelf(), false), "true表示本人业绩计入团队");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_NEED_KYC, "有效成员需实名",
                boolStr(rule.getValidNeedKyc(), true), "有效成员是否必须已实名");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_NEED_ORDER, "有效成员需认购",
                boolStr(rule.getValidNeedOrder(), true), "有效成员是否必须有认购订单");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_TEXT, "等级奖励规则说明",
                rule.getRuleText() == null ? "" : rule.getRuleText(), "App右上角规则说明");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_HINT, "等级页注释",
                rule.getHint() == null ? "" : rule.getHint(), "App会员等级页表格上方注释");
    }

    @Override
    public int updateLevelReward(BizLevel level)
    {
        if (level == null || level.getLevelId() == null)
        {
            throw new ServiceException("请选择等级");
        }
        fillRewardDefaults(level);
        String cycle = level.getRewardCycle() == null ? "NONE" : level.getRewardCycle().toUpperCase();
        if (!"NONE".equals(cycle) && !"ONCE".equals(cycle) && !"MONTHLY".equals(cycle) && !"PERMANENT".equals(cycle))
        {
            throw new ServiceException("奖励周期只能是 NONE/ONCE/MONTHLY/PERMANENT");
        }
        level.setRewardCycle(cycle);
        String mode = level.getRewardMode() == null ? "AUTO" : level.getRewardMode().toUpperCase();
        if (!"AUTO".equals(mode) && !"MANUAL".equals(mode))
        {
            throw new ServiceException("发放方式只能是 AUTO 或 MANUAL");
        }
        level.setRewardMode(mode);
        String repeat = level.getRewardRepeat() == null ? "NONE" : level.getRewardRepeat().toUpperCase();
        if (!"NONE".equals(repeat) && !"MONTHLY".equals(repeat) && !"UNLIMITED".equals(repeat))
        {
            throw new ServiceException("永久档领取方式只能是 NONE/MONTHLY/UNLIMITED");
        }
        level.setRewardRepeat(repeat);
        return levelMapper.updateLevel(level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(Long memberId)
    {
        if (memberId == null || !boolVal(BizConstants.CONFIG_LEVEL_REWARD_ENABLED, true))
        {
            return;
        }
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null || BizConstants.STATUS_DISABLE.equals(member.getStatus()))
        {
            return;
        }
        Stats stats = loadStats(memberId);
        BizLevel query = new BizLevel();
        query.setStatus(BizConstants.STATUS_OK);
        List<BizLevel> levels = levelMapper.selectLevelList(query);
        BizLevel current = pickHighest(levels, stats);
        for (int i = 0; i < levels.size(); i++)
        {
            BizLevel level = levels.get(i);
            if (matches(stats, level) && rewardOn(level) && "ONCE".equalsIgnoreCase(level.getRewardCycle()))
            {
                tryGrant(member, level, stats, "ONCE");
            }
        }
        if (current == null || !rewardOn(current))
        {
            return;
        }
        String cycle = current.getRewardCycle() == null ? "" : current.getRewardCycle().toUpperCase();
        if ("MONTHLY".equals(cycle))
        {
            tryGrant(member, current, stats, DateUtils.parseDateToStr(DateUtils.YYYY_MM, new Date()));
        }
        else if ("PERMANENT".equals(cycle))
        {
            String repeat = current.getRewardRepeat() == null ? "UNLIMITED" : current.getRewardRepeat().toUpperCase();
            if ("MONTHLY".equals(repeat))
            {
                tryGrant(member, current, stats, DateUtils.parseDateToStr(DateUtils.YYYY_MM, new Date()));
            }
            else
            {
                tryGrant(member, current, stats, "FIRST");
            }
        }
    }

    @Override
    public BizLevel matchLevel(Long memberId)
    {
        if (memberId == null)
        {
            return null;
        }
        Stats stats = loadStats(memberId);
        BizLevel query = new BizLevel();
        query.setStatus(BizConstants.STATUS_OK);
        return pickHighest(levelMapper.selectLevelList(query), stats);
    }

    private BizLevel pickHighest(List<BizLevel> levels, Stats stats)
    {
        BizLevel current = null;
        for (int i = 0; i < levels.size(); i++)
        {
            BizLevel level = levels.get(i);
            if (!matches(stats, level))
            {
                continue;
            }
            if (current == null || nvl(level.getSort()) > nvl(current.getSort()))
            {
                current = level;
            }
        }
        return current;
    }

    @Override
    public int evaluateAll()
    {
        List<BizMember> members = memberMapper.selectMemberList(new BizMember());
        int count = 0;
        for (int i = 0; i < members.size(); i++)
        {
            evaluate(members.get(i).getMemberId());
            count++;
        }
        return count;
    }

    @Override
    public List<BizLevelRewardGrant> selectGrantList(BizLevelRewardGrant grant)
    {
        return grantMapper.selectGrantList(grant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payGrant(Long grantId, String operator, String remark)
    {
        BizLevelRewardGrant grant = grantMapper.selectGrantById(grantId);
        if (grant == null)
        {
            throw new ServiceException("发放记录不存在");
        }
        if (!BizConstants.AUDIT_PENDING.equals(grant.getStatus()))
        {
            throw new ServiceException("该记录不是待发放");
        }
        if (grant.getAmount() == null || grant.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("发放金额必须大于0");
        }
        walletService.credit(grant.getMemberId(), grant.getCurrency(), grant.getAmount(),
                BizConstants.BIZ_LEVEL_REWARD, grant.getGrantId(), "等级奖励:" + grant.getLevelName());
        BizLevelRewardGrant update = new BizLevelRewardGrant();
        update.setGrantId(grantId);
        update.setStatus(BizConstants.AUDIT_PASS);
        update.setPayBy(operator);
        update.setPayTime(new Date());
        update.setRemark(remark);
        grantMapper.updateGrant(update);
    }

    @Override
    public void rejectGrant(Long grantId, String operator, String remark)
    {
        BizLevelRewardGrant grant = grantMapper.selectGrantById(grantId);
        if (grant == null)
        {
            throw new ServiceException("发放记录不存在");
        }
        if (!BizConstants.AUDIT_PENDING.equals(grant.getStatus()))
        {
            throw new ServiceException("该记录不是待发放");
        }
        BizLevelRewardGrant update = new BizLevelRewardGrant();
        update.setGrantId(grantId);
        update.setStatus(BizConstants.AUDIT_REJECT);
        update.setPayBy(operator);
        update.setPayTime(new Date());
        update.setRemark(StringUtils.isEmpty(remark) ? "客服拒绝发放" : remark);
        grantMapper.updateGrant(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extraPay(BizLevelRewardPayBody body, String operator)
    {
        if (body == null || body.getMemberId() == null || body.getLevelId() == null)
        {
            throw new ServiceException("请选择会员和等级");
        }
        BizMember member = memberMapper.selectMemberById(body.getMemberId());
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        BizLevel level = levelMapper.selectLevelById(body.getLevelId());
        if (level == null || !rewardOn(level))
        {
            throw new ServiceException("该等级未启用奖励");
        }
        if (!"PERMANENT".equalsIgnoreCase(level.getRewardCycle()))
        {
            throw new ServiceException("仅永久档可额外发放");
        }
        Stats stats = loadStats(member.getMemberId());
        if (!matches(stats, level))
        {
            throw new ServiceException("该会员当前未达成该等级条件");
        }
        String cycleKey = "PAY-" + DateUtils.dateTimeNow();
        BizLevelRewardGrant grant = buildGrant(member, level, stats, cycleKey);
        if (grant == null)
        {
            throw new ServiceException("奖励金额未配置或为0");
        }
        grant.setStatus(BizConstants.AUDIT_PASS);
        grant.setPayBy(operator);
        grant.setPayTime(new Date());
        grant.setRemark(body.getRemark());
        grantMapper.insertGrant(grant);
        walletService.credit(member.getMemberId(), grant.getCurrency(), grant.getAmount(),
                BizConstants.BIZ_LEVEL_REWARD, grant.getGrantId(), "等级奖励:" + level.getLevelName());
    }

    private void tryGrant(BizMember member, BizLevel level, Stats stats, String cycleKey)
    {
        BizLevelRewardGrant existing = grantMapper.selectByCycle(member.getMemberId(), level.getLevelId(), cycleKey);
        if (existing != null)
        {
            if (BizConstants.AUDIT_REJECT.equals(existing.getStatus())
                    && "MANUAL".equalsIgnoreCase(level.getRewardMode()))
            {
                BizLevelRewardGrant reopen = new BizLevelRewardGrant();
                reopen.setGrantId(existing.getGrantId());
                reopen.setStatus(BizConstants.AUDIT_PENDING);
                reopen.setPayBy("");
                reopen.setRemark("待客服发放");
                grantMapper.updateGrant(reopen);
            }
            return;
        }
        BizLevelRewardGrant grant = buildGrant(member, level, stats, cycleKey);
        if (grant == null)
        {
            return;
        }
        boolean auto = "AUTO".equalsIgnoreCase(level.getRewardMode());
        if (auto)
        {
            grant.setStatus(BizConstants.AUDIT_PASS);
            grant.setPayBy("system");
            grant.setPayTime(new Date());
            grant.setRemark("系统自动发放");
            grantMapper.insertGrant(grant);
            walletService.credit(member.getMemberId(), grant.getCurrency(), grant.getAmount(),
                    BizConstants.BIZ_LEVEL_REWARD, grant.getGrantId(), "等级奖励:" + level.getLevelName());
        }
        else
        {
            grant.setStatus(BizConstants.AUDIT_PENDING);
            grant.setRemark("待客服发放");
            grantMapper.insertGrant(grant);
        }
    }

    private BizLevelRewardGrant buildGrant(BizMember member, BizLevel level, Stats stats, String cycleKey)
    {
        String currency = resolveCurrency(stats);
        BigDecimal amount = amountOf(level, currency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            String other = BizConstants.CURRENCY_USDT.equals(currency) ? BizConstants.CURRENCY_CNY : BizConstants.CURRENCY_USDT;
            amount = amountOf(level, other);
            currency = other;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return null;
        }
        BizLevelRewardGrant grant = new BizLevelRewardGrant();
        grant.setMemberId(member.getMemberId());
        grant.setLevelId(level.getLevelId());
        grant.setLevelName(level.getLevelName());
        grant.setCycleKey(cycleKey);
        grant.setGrantCycle(level.getRewardCycle());
        grant.setGrantMode(level.getRewardMode());
        grant.setCurrency(currency);
        grant.setAmount(amount);
        return grant;
    }

    private String resolveCurrency(Stats stats)
    {
        boolean hasCny = stats.teamCny.compareTo(BigDecimal.ZERO) > 0;
        boolean hasUsdt = stats.teamUsdt.compareTo(BigDecimal.ZERO) > 0;
        if (hasCny && hasUsdt)
        {
            String mixed = strVal(BizConstants.CONFIG_LEVEL_REWARD_MIXED_PAY, BizConstants.CURRENCY_USDT).toUpperCase();
            return BizConstants.CURRENCY_CNY.equals(mixed) ? BizConstants.CURRENCY_CNY : BizConstants.CURRENCY_USDT;
        }
        if (hasUsdt)
        {
            return BizConstants.CURRENCY_USDT;
        }
        return BizConstants.CURRENCY_CNY;
    }

    private BigDecimal amountOf(BizLevel level, String currency)
    {
        BigDecimal amount = BizConstants.CURRENCY_USDT.equals(currency) ? level.getRewardUsdt() : level.getRewardCny();
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private boolean matches(Stats stats, BizLevel level)
    {
        if (nvl(stats.validMembers) < nvl(level.getMinValidMembers()))
        {
            return false;
        }
        if (!reach(stats.rechargeCny, level.getMinRechargeCny()))
        {
            return false;
        }
        if (!reach(stats.rechargeUsdt, level.getMinRechargeUsdt()))
        {
            return false;
        }
        if (!reach(stats.teamCny, level.getMinTeamPerfCny()))
        {
            return false;
        }
        if (!reach(stats.teamUsdt, level.getMinTeamPerfUsdt()))
        {
            return false;
        }
        return true;
    }

    private boolean reach(BigDecimal actual, BigDecimal min)
    {
        if (min == null || min.compareTo(BigDecimal.ZERO) <= 0)
        {
            return true;
        }
        return actual.compareTo(min) >= 0;
    }

    private boolean rewardOn(BizLevel level)
    {
        return level != null && "1".equals(level.getRewardEnabled())
                && level.getRewardCycle() != null && !"NONE".equalsIgnoreCase(level.getRewardCycle());
    }

    private Stats loadStats(Long memberId)
    {
        BizLevelRewardRule rule = getRule();
        boolean needKyc = rule.getValidNeedKyc() == null || rule.getValidNeedKyc().booleanValue();
        boolean needOrder = rule.getValidNeedOrder() == null || rule.getValidNeedOrder().booleanValue();
        boolean includeSelf = rule.getIncludeSelf() != null && rule.getIncludeSelf().booleanValue();
        String source = rule.getPerformanceSource() == null ? "SUBSCRIBE" : rule.getPerformanceSource().toUpperCase();
        Stats stats = new Stats();
        stats.validMembers = memberMapper.countValidTeamMembersConfig(memberId, needKyc, needOrder);
        stats.rechargeCny = nvl(rechargeMapper.sumPassedRecharge(memberId, BizConstants.CURRENCY_CNY));
        stats.rechargeUsdt = nvl(rechargeMapper.sumPassedRecharge(memberId, BizConstants.CURRENCY_USDT));
        BigDecimal orderCny = nvl(orderMapper.sumTeamOrderAmount(memberId, BizConstants.CURRENCY_CNY, includeSelf));
        BigDecimal orderUsdt = nvl(orderMapper.sumTeamOrderAmount(memberId, BizConstants.CURRENCY_USDT, includeSelf));
        BigDecimal recCny = nvl(rechargeMapper.sumTeamPassedRecharge(memberId, BizConstants.CURRENCY_CNY, includeSelf));
        BigDecimal recUsdt = nvl(rechargeMapper.sumTeamPassedRecharge(memberId, BizConstants.CURRENCY_USDT, includeSelf));
        if ("RECHARGE".equals(source))
        {
            stats.teamCny = recCny;
            stats.teamUsdt = recUsdt;
        }
        else if ("BOTH".equals(source))
        {
            stats.teamCny = orderCny.add(recCny);
            stats.teamUsdt = orderUsdt.add(recUsdt);
        }
        else
        {
            stats.teamCny = orderCny;
            stats.teamUsdt = orderUsdt;
        }
        return stats;
    }

    private void fillRewardDefaults(BizLevel level)
    {
        if (StringUtils.isEmpty(level.getRewardEnabled()))
        {
            level.setRewardEnabled("0");
        }
        if (StringUtils.isEmpty(level.getRewardCycle()))
        {
            level.setRewardCycle("NONE");
        }
        if (StringUtils.isEmpty(level.getRewardMode()))
        {
            level.setRewardMode("AUTO");
        }
        if (StringUtils.isEmpty(level.getRewardRepeat()))
        {
            level.setRewardRepeat("NONE");
        }
        if (level.getRewardCny() == null)
        {
            level.setRewardCny(BigDecimal.ZERO);
        }
        if (level.getRewardUsdt() == null)
        {
            level.setRewardUsdt(BigDecimal.ZERO);
        }
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

    private String boolStr(Boolean value, boolean defaultValue)
    {
        boolean v = value == null ? defaultValue : value.booleanValue();
        return v ? "true" : "false";
    }

    private int nvl(Integer v)
    {
        return v == null ? 0 : v.intValue();
    }

    private BigDecimal nvl(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static class Stats
    {
        private int validMembers;
        private BigDecimal rechargeCny = BigDecimal.ZERO;
        private BigDecimal rechargeUsdt = BigDecimal.ZERO;
        private BigDecimal teamCny = BigDecimal.ZERO;
        private BigDecimal teamUsdt = BigDecimal.ZERO;
    }
}
