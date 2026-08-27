package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDictTypeService;

@Service
public class BizLevelRewardServiceImpl implements IBizLevelRewardService
{
    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private ISysDictTypeService dictTypeService;

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
                "启航、探索、开拓、星耀、领航、星域：达成条件后系统自动发放1次成长激励金。星链：达成条件后联系客服领取，由后台手动发放。团队同时有人民币和USDT业绩时发放USDT。最终以系统核算为准。"));
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
        if (!BizConstants.CURRENCY_CNY.equals(mixed) && !BizConstants.CURRENCY_USDT.equals(mixed)
                && !"BOTH".equals(mixed))
        {
            throw new ServiceException("混合业绩发放币种只能是CNY、USDT或BOTH");
        }
        String source = StringUtils.isEmpty(rule.getPerformanceSource()) ? "SUBSCRIBE" : rule.getPerformanceSource().toUpperCase();
        if (!"SUBSCRIBE".equals(source) && !"RECHARGE".equals(source) && !"BOTH".equals(source))
        {
            throw new ServiceException("团队业绩口径不正确");
        }
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_ENABLED, "等级奖励开关",
                boolStr(rule.getEnabled(), true), "false表示关闭成长激励金");
        saveConfig(BizConstants.CONFIG_LEVEL_REWARD_MIXED_PAY, "混合业绩发放币种", mixed,
                "CNY或USDT=两种业绩都有时发该币种；BOTH=人民币和USDT金额都发");
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
        String source = StringUtils.isEmpty(level.getPerformanceSource()) ? "RECHARGE"
                : level.getPerformanceSource().toUpperCase();
        if (!"SUBSCRIBE".equals(source) && !"RECHARGE".equals(source) && !"BOTH".equals(source))
        {
            throw new ServiceException("团队业绩口径只能是认购、充值或认购+充值");
        }
        level.setPerformanceSource(source);
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
        if (StringUtils.isEmpty(level.getWalletTypeCode()))
        {
            level.setWalletTypeCode("PROMO");
        }
        else
        {
            level.setWalletTypeCode(level.getWalletTypeCode().trim().toUpperCase());
        }
        String mixedPay = StringUtils.isEmpty(level.getMixedPayCurrency()) ? BizConstants.CURRENCY_USDT
                : level.getMixedPayCurrency().toUpperCase();
        if (!BizConstants.CURRENCY_CNY.equals(mixedPay) && !BizConstants.CURRENCY_USDT.equals(mixedPay)
                && !"BOTH".equals(mixedPay))
        {
            throw new ServiceException("发放币种只能是CNY、USDT或BOTH");
        }
        level.setMixedPayCurrency(mixedPay);
        if (StringUtils.isEmpty(level.getValidNeedKyc()))
        {
            level.setValidNeedKyc("1");
        }
        if (StringUtils.isEmpty(level.getValidNeedOrder()))
        {
            level.setValidNeedOrder("1");
        }
        normalizeTeamDepth(level);
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
        Map<String, Stats> cache = new HashMap<String, Stats>();
        int viewerDepth = viewerDepth(memberId);
        BizLevel query = new BizLevel();
        query.setStatus(BizConstants.STATUS_OK);
        List<BizLevel> levels = levelMapper.selectLevelList(query);
        BizLevel current = pickHighest(levels, memberId, viewerDepth, cache);
        for (int i = 0; i < levels.size(); i++)
        {
            BizLevel level = levels.get(i);
            Stats stats = statsOf(memberId, viewerDepth, level, cache);
            if (matches(stats, level) && rewardOn(level) && "ONCE".equalsIgnoreCase(level.getRewardCycle()))
            {
                tryGrant(member, level, stats, "ONCE");
            }
        }
        if (current == null || !rewardOn(current))
        {
            return;
        }
        Stats currentStats = statsOf(memberId, viewerDepth, current, cache);
        String cycle = current.getRewardCycle() == null ? "" : current.getRewardCycle().toUpperCase();
        if ("MONTHLY".equals(cycle))
        {
            tryGrant(member, current, currentStats, DateUtils.parseDateToStr(DateUtils.YYYY_MM, new Date()));
        }
        else if ("PERMANENT".equals(cycle))
        {
            String repeat = current.getRewardRepeat() == null ? "UNLIMITED" : current.getRewardRepeat().toUpperCase();
            if ("MONTHLY".equals(repeat))
            {
                tryGrant(member, current, currentStats, DateUtils.parseDateToStr(DateUtils.YYYY_MM, new Date()));
            }
            else
            {
                tryGrant(member, current, currentStats, "FIRST");
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
        BizLevel query = new BizLevel();
        query.setStatus(BizConstants.STATUS_OK);
        return pickHighest(levelMapper.selectLevelList(query), memberId, viewerDepth(memberId),
                new HashMap<String, Stats>());
    }

    private BizLevel pickHighest(List<BizLevel> levels, Long memberId, int viewerDepth, Map<String, Stats> cache)
    {
        BizLevel current = null;
        for (int i = 0; i < levels.size(); i++)
        {
            BizLevel level = levels.get(i);
            if (!matches(statsOf(memberId, viewerDepth, level, cache), level))
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
                BizConstants.BIZ_LEVEL_REWARD, grant.getGrantId(), "等级奖励:" + grant.getLevelName(),
                walletOf(levelMapper.selectLevelById(grant.getLevelId())));
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
        Stats stats = statsOf(member.getMemberId(), viewerDepth(member.getMemberId()), level,
                new HashMap<String, Stats>());
        if (!matches(stats, level))
        {
            throw new ServiceException("该会员当前未达成该等级条件");
        }
        String cycleKey = "PAY-" + DateUtils.dateTimeNow();
        List<String> currencies = payCurrencies(stats, level);
        if (currencies.isEmpty())
        {
            throw new ServiceException("奖励金额未配置或为0");
        }
        boolean both = currencies.size() > 1;
        for (int i = 0; i < currencies.size(); i++)
        {
            String currency = currencies.get(i);
            String key = both ? cycleKey + "-" + currency : cycleKey;
            BizLevelRewardGrant grant = buildGrant(member, level, key, currency);
            if (grant == null)
            {
                continue;
            }
            grant.setStatus(BizConstants.AUDIT_PASS);
            grant.setPayBy(operator);
            grant.setPayTime(new Date());
            grant.setRemark(body.getRemark());
            grantMapper.insertGrant(grant);
            walletService.credit(member.getMemberId(), grant.getCurrency(), grant.getAmount(),
                    BizConstants.BIZ_LEVEL_REWARD, grant.getGrantId(), "等级奖励:" + level.getLevelName(),
                    walletOf(level));
        }
    }

    private void tryGrant(BizMember member, BizLevel level, Stats stats, String cycleKey)
    {
        if ("ONCE".equalsIgnoreCase(level.getRewardCycle())
                && grantMapper.countActiveByMemberLevel(member.getMemberId(), level.getLevelId()) > 0)
        {
            return;
        }
        List<String> currencies = payCurrencies(stats, level);
        boolean both = currencies.size() > 1;
        for (int i = 0; i < currencies.size(); i++)
        {
            String currency = currencies.get(i);
            String key = both ? cycleKey + "-" + currency : cycleKey;
            BizLevelRewardGrant existing = grantMapper.selectByCycle(member.getMemberId(), level.getLevelId(), key);
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
                continue;
            }
            BizLevelRewardGrant grant = buildGrant(member, level, key, currency);
            if (grant == null)
            {
                continue;
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
                        BizConstants.BIZ_LEVEL_REWARD, grant.getGrantId(), "等级奖励:" + level.getLevelName(),
                        walletOf(level));
            }
            else
            {
                grant.setStatus(BizConstants.AUDIT_PENDING);
                grant.setRemark("待客服发放");
                grantMapper.insertGrant(grant);
            }
        }
    }

    private BizLevelRewardGrant buildGrant(BizMember member, BizLevel level, String cycleKey, String currency)
    {
        BigDecimal amount = amountOf(level, currency);
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

    private List<String> payCurrencies(Stats stats, BizLevel level)
    {
        String mixed = mixedOf(level);
        List<String> list = new ArrayList<String>();
        if ("BOTH".equals(mixed))
        {
            if (amountOf(level, BizConstants.CURRENCY_CNY).compareTo(BigDecimal.ZERO) > 0)
            {
                list.add(BizConstants.CURRENCY_CNY);
            }
            if (amountOf(level, BizConstants.CURRENCY_USDT).compareTo(BigDecimal.ZERO) > 0)
            {
                list.add(BizConstants.CURRENCY_USDT);
            }
            return list;
        }
        String currency = resolveCurrency(stats, level);
        BigDecimal amount = amountOf(level, currency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            currency = BizConstants.CURRENCY_USDT.equals(currency)
                    ? BizConstants.CURRENCY_CNY : BizConstants.CURRENCY_USDT;
        }
        if (amountOf(level, currency).compareTo(BigDecimal.ZERO) > 0)
        {
            list.add(currency);
        }
        return list;
    }

    private String resolveCurrency(Stats stats, BizLevel level)
    {
        BigDecimal teamCny = teamAmount(stats, level, true);
        BigDecimal teamUsdt = teamAmount(stats, level, false);
        boolean hasCny = teamCny.compareTo(BigDecimal.ZERO) > 0;
        boolean hasUsdt = teamUsdt.compareTo(BigDecimal.ZERO) > 0;
        if (hasCny && hasUsdt)
        {
            String mixed = mixedOf(level);
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
        BigDecimal teamCny = teamAmount(stats, level, true);
        BigDecimal teamUsdt = teamAmount(stats, level, false);
        if (!reach(teamCny, level.getMinTeamRechargeCny()))
        {
            return false;
        }
        if (!reach(teamUsdt, level.getMinTeamRechargeUsdt()))
        {
            return false;
        }
        if (!reach(teamCny, level.getMinTeamPerfCny()))
        {
            return false;
        }
        if (!reach(teamUsdt, level.getMinTeamPerfUsdt()))
        {
            return false;
        }
        return true;
    }

    private BigDecimal teamAmount(Stats stats, BizLevel level, boolean cny)
    {
        String source = sourceOf(level);
        BigDecimal order = cny ? stats.downlineOrderCny : stats.downlineOrderUsdt;
        BigDecimal rec = cny ? stats.downlineRechargeCny : stats.downlineRechargeUsdt;
        if ("RECHARGE".equals(source))
        {
            return rec;
        }
        if ("BOTH".equals(source))
        {
            return order.add(rec);
        }
        return order;
    }

    private String mixedOf(BizLevel level)
    {
        String mixed = level == null ? null : level.getMixedPayCurrency();
        if (StringUtils.isEmpty(mixed))
        {
            mixed = strVal(BizConstants.CONFIG_LEVEL_REWARD_MIXED_PAY, BizConstants.CURRENCY_USDT);
        }
        return mixed.toUpperCase();
    }

    private String walletOf(BizLevel level)
    {
        if (level != null && StringUtils.isNotEmpty(level.getWalletTypeCode()))
        {
            return level.getWalletTypeCode().trim().toUpperCase();
        }
        return null;
    }

    private boolean flagOn(String value, boolean defaultValue)
    {
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    private String sourceOf(BizLevel level)
    {
        String source = level == null ? null : level.getPerformanceSource();
        if (StringUtils.isEmpty(source))
        {
            source = strVal(BizConstants.CONFIG_LEVEL_REWARD_PERF_SOURCE, "SUBSCRIBE");
        }
        return source.toUpperCase();
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

    private Stats statsOf(Long memberId, int viewerDepth, BizLevel level, Map<String, Stats> cache)
    {
        Integer maxDepth = parseTeamDepthLimit(level == null ? null : level.getTeamDepth());
        boolean needKyc = flagOn(level == null ? null : level.getValidNeedKyc(),
                boolVal(BizConstants.CONFIG_LEVEL_REWARD_NEED_KYC, true));
        boolean needOrder = flagOn(level == null ? null : level.getValidNeedOrder(),
                boolVal(BizConstants.CONFIG_LEVEL_REWARD_NEED_ORDER, true));
        String key = (maxDepth == null ? "all" : maxDepth.toString()) + ":" + needKyc + ":" + needOrder;
        Stats cached = cache.get(key);
        if (cached != null)
        {
            return cached;
        }
        Stats stats = loadStats(memberId, maxDepth, viewerDepth, needKyc, needOrder);
        cache.put(key, stats);
        return stats;
    }

    private Integer parseTeamDepthLimit(String teamDepth)
    {
        if (StringUtils.isEmpty(teamDepth))
        {
            return null;
        }
        String text = teamDepth.trim();
        List<SysDictData> dicts = teamDepthDict();
        if (dicts != null)
        {
            for (int i = 0; i < dicts.size(); i++)
            {
                SysDictData row = dicts.get(i);
                if (row == null || StringUtils.isEmpty(row.getDictValue()))
                {
                    continue;
                }
                if ("1".equals(row.getStatus()))
                {
                    continue;
                }
                if (text.equals(row.getDictValue()) || text.equals(row.getDictLabel()))
                {
                    return parsePositiveInt(row.getDictValue());
                }
            }
        }
        Integer n = parsePositiveInt(text);
        if (n != null)
        {
            return n;
        }
        String[] labels = new String[] {"一级内", "二级内", "三级内", "四级内", "五级内", "六级内", "七级内"};
        for (int i = 0; i < labels.length; i++)
        {
            if (labels[i].equals(text) || text.startsWith(String.valueOf(i + 1)))
            {
                return Integer.valueOf(i + 1);
            }
        }
        return null;
    }

    @Override
    public void fillTeamDepthLabels(List<BizLevel> levels)
    {
        if (levels == null || levels.isEmpty())
        {
            return;
        }
        for (int i = 0; i < levels.size(); i++)
        {
            BizLevel level = levels.get(i);
            if (level == null || StringUtils.isEmpty(level.getTeamDepth()))
            {
                continue;
            }
            String raw = level.getTeamDepth().trim();
            String label = DictUtils.getDictLabel(BizConstants.DICT_TEAM_DEPTH, raw);
            if (StringUtils.isEmpty(label))
            {
                List<SysDictData> dicts = teamDepthDict();
                if (dicts != null)
                {
                    for (int j = 0; j < dicts.size(); j++)
                    {
                        SysDictData row = dicts.get(j);
                        if (row != null && raw.equals(row.getDictValue()))
                        {
                            label = row.getDictLabel();
                            break;
                        }
                    }
                }
            }
            if (StringUtils.isNotEmpty(label))
            {
                level.setTeamDepth(label);
            }
        }
    }

    @Override
    public void fillTeamDepthLabel(BizLevel level)
    {
        if (level == null)
        {
            return;
        }
        List<BizLevel> one = new ArrayList<BizLevel>(1);
        one.add(level);
        fillTeamDepthLabels(one);
    }

    @Override
    public void normalizeTeamDepth(BizLevel level)
    {
        if (level == null)
        {
            return;
        }
        if (StringUtils.isEmpty(level.getTeamDepth()))
        {
            level.setTeamDepth("");
            return;
        }
        String text = level.getTeamDepth().trim();
        List<SysDictData> dicts = teamDepthDict();
        if (dicts != null)
        {
            for (int i = 0; i < dicts.size(); i++)
            {
                SysDictData row = dicts.get(i);
                if (row == null || StringUtils.isEmpty(row.getDictValue()))
                {
                    continue;
                }
                if ("1".equals(row.getStatus()))
                {
                    continue;
                }
                if (text.equals(row.getDictValue()) || text.equals(row.getDictLabel()))
                {
                    level.setTeamDepth(row.getDictValue().trim());
                    return;
                }
            }
        }
        Integer n = parsePositiveInt(text);
        if (n != null)
        {
            level.setTeamDepth(String.valueOf(n));
            return;
        }
        String[] labels = new String[] {"一级内", "二级内", "三级内", "四级内", "五级内", "六级内", "七级内"};
        for (int i = 0; i < labels.length; i++)
        {
            if (labels[i].equals(text))
            {
                level.setTeamDepth(String.valueOf(i + 1));
                return;
            }
        }
        level.setTeamDepth(text);
    }

    private List<SysDictData> teamDepthDict()
    {
        List<SysDictData> cached = DictUtils.getDictCache(BizConstants.DICT_TEAM_DEPTH);
        if (cached != null && !cached.isEmpty())
        {
            return cached;
        }
        if (dictTypeService == null)
        {
            return cached;
        }
        return dictTypeService.selectDictDataByType(BizConstants.DICT_TEAM_DEPTH);
    }

    private Integer parsePositiveInt(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        for (int i = 0; i < text.length(); i++)
        {
            if (!Character.isDigit(text.charAt(i)))
            {
                return null;
            }
        }
        try
        {
            int n = Integer.parseInt(text);
            return n > 0 ? Integer.valueOf(n) : null;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private int viewerDepth(Long memberId)
    {
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null || StringUtils.isEmpty(member.getAncestors()) || "0".equals(member.getAncestors()))
        {
            return 0;
        }
        int n = 0;
        String ancestors = member.getAncestors();
        for (int i = 0; i < ancestors.length(); i++)
        {
            if (ancestors.charAt(i) == ',')
            {
                n++;
            }
        }
        return n;
    }

    private Stats loadStats(Long memberId, Integer maxDepth, int viewerDepth, boolean needKyc, boolean needOrder)
    {
        Stats stats = new Stats();
        stats.validMembers = memberMapper.countValidTeamMembersConfig(memberId, needKyc, needOrder, maxDepth,
                Integer.valueOf(viewerDepth));
        stats.rechargeCny = nvl(rechargeMapper.sumPassedRecharge(memberId, BizConstants.CURRENCY_CNY));
        stats.rechargeUsdt = nvl(rechargeMapper.sumPassedRecharge(memberId, BizConstants.CURRENCY_USDT));
        stats.downlineOrderCny = nvl(orderMapper.sumTeamOrderAmount(memberId, BizConstants.CURRENCY_CNY, false,
                maxDepth, Integer.valueOf(viewerDepth)));
        stats.downlineOrderUsdt = nvl(orderMapper.sumTeamOrderAmount(memberId, BizConstants.CURRENCY_USDT, false,
                maxDepth, Integer.valueOf(viewerDepth)));
        stats.downlineRechargeCny = nvl(rechargeMapper.sumTeamPassedRecharge(memberId, BizConstants.CURRENCY_CNY, false,
                maxDepth, Integer.valueOf(viewerDepth)));
        stats.downlineRechargeUsdt = nvl(rechargeMapper.sumTeamPassedRecharge(memberId, BizConstants.CURRENCY_USDT, false,
                maxDepth, Integer.valueOf(viewerDepth)));
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
        if (level.getMinTeamRechargeCny() == null)
        {
            level.setMinTeamRechargeCny(BigDecimal.ZERO);
        }
        if (level.getMinTeamRechargeUsdt() == null)
        {
            level.setMinTeamRechargeUsdt(BigDecimal.ZERO);
        }
        if (StringUtils.isEmpty(level.getPerformanceSource()))
        {
            level.setPerformanceSource("RECHARGE");
        }
        if (StringUtils.isEmpty(level.getWalletTypeCode()))
        {
            level.setWalletTypeCode("PROMO");
        }
        if (StringUtils.isEmpty(level.getMixedPayCurrency()))
        {
            level.setMixedPayCurrency(BizConstants.CURRENCY_USDT);
        }
        if (StringUtils.isEmpty(level.getValidNeedKyc()))
        {
            level.setValidNeedKyc("1");
        }
        if (StringUtils.isEmpty(level.getValidNeedOrder()))
        {
            level.setValidNeedOrder("1");
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
        private BigDecimal downlineRechargeCny = BigDecimal.ZERO;
        private BigDecimal downlineRechargeUsdt = BigDecimal.ZERO;
        private BigDecimal downlineOrderCny = BigDecimal.ZERO;
        private BigDecimal downlineOrderUsdt = BigDecimal.ZERO;
    }
}
