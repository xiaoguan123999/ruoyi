package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizLotteryChance;
import com.ruoyi.biz.domain.BizLotteryChanceLog;
import com.ruoyi.biz.domain.BizLotteryChanceRule;
import com.ruoyi.biz.mapper.BizCheckinMapper;
import com.ruoyi.biz.mapper.BizLotteryActivityMapper;
import com.ruoyi.biz.mapper.BizLotteryChanceLogMapper;
import com.ruoyi.biz.mapper.BizLotteryChanceMapper;
import com.ruoyi.biz.mapper.BizLotteryChanceRuleMapper;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.service.IBizLotteryChanceService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizLotteryChanceServiceImpl implements IBizLotteryChanceService
{
    @Autowired
    private BizLotteryChanceRuleMapper chanceRuleMapper;

    @Autowired
    private BizLotteryChanceMapper chanceMapper;

    @Autowired
    private BizLotteryChanceLogMapper chanceLogMapper;

    @Autowired
    private BizLotteryActivityMapper lotteryActivityMapper;

    @Autowired
    private BizCheckinMapper checkinMapper;

    @Autowired
    private BizMemberMapper memberMapper;

    @Override
    public List<BizLotteryChanceRule> selectChanceRuleList(BizLotteryChanceRule query)
    {
        return chanceRuleMapper.selectChanceRuleList(query);
    }

    @Override
    public BizLotteryChanceRule selectChanceRuleById(Long ruleId)
    {
        return chanceRuleMapper.selectChanceRuleById(ruleId);
    }

    @Override
    public int insertChanceRule(BizLotteryChanceRule rule)
    {
        validateRule(rule);
        fillRuleDefaults(rule, true);
        return chanceRuleMapper.insertChanceRule(rule);
    }

    @Override
    public int updateChanceRule(BizLotteryChanceRule rule)
    {
        if (rule.getRuleId() == null)
        {
            throw new ServiceException("规则ID不能为空");
        }
        validateRule(rule);
        fillRuleDefaults(rule, false);
        return chanceRuleMapper.updateChanceRule(rule);
    }

    @Override
    public int deleteChanceRuleByIds(Long[] ruleIds)
    {
        return chanceRuleMapper.deleteChanceRuleByIds(ruleIds);
    }

    @Override
    public List<BizLotteryChance> selectChanceList(BizLotteryChance query)
    {
        return chanceMapper.selectChanceList(query);
    }

    @Override
    public List<BizLotteryChanceLog> selectChanceLogList(BizLotteryChanceLog query)
    {
        return chanceLogMapper.selectChanceLogList(query);
    }

    @Override
    public int getBalance(Long activityId, Long memberId)
    {
        ensureAccount(activityId, memberId);
        BizLotteryChance row = chanceMapper.selectByActivityAndMember(activityId, memberId);
        return row != null && row.getBalance() != null ? row.getBalance().intValue() : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int tryGrantByRules(Long activityId, Long memberId)
    {
        if (activityId == null || memberId == null)
        {
            return 0;
        }
        ensureAccount(activityId, memberId);
        List<BizLotteryChanceRule> rules = chanceRuleMapper.selectEnabledRulesByActivityId(activityId);
        if (rules == null || rules.isEmpty())
        {
            return 0;
        }
        int checkinDays = checkinMapper.countByMemberId(memberId);
        int inviteKyc = memberMapper.countDirectKycMembers(memberId);
        int granted = 0;
        for (int i = 0; i < rules.size(); i++)
        {
            BizLotteryChanceRule rule = rules.get(i);
            int levels = qualifiedLevels(rule, checkinDays, inviteKyc);
            if (levels <= 0)
            {
                continue;
            }
            int maxTimes = resolveMaxGrantTimes(rule);
            int target = Math.min(levels, maxTimes);
            int amount = rule.getGrantAmount() != null && rule.getGrantAmount().intValue() > 0
                    ? rule.getGrantAmount().intValue() : 1;
            for (int seq = 1; seq <= target; seq++)
            {
                String bizNo = buildRuleGrantBizNo(activityId, memberId, rule.getRuleId(), seq);
                if (alreadyGranted(activityId, memberId, rule.getRuleId(), seq))
                {
                    continue;
                }
                applyChange(activityId, memberId, amount, BizConstants.LOTTERY_CHANCE_RULE_GRANT,
                        bizNo, rule.getRuleId(), "规则发放：" + rule.getRuleName() + "（第" + seq + "档）", "system");
                granted += amount;
            }
        }
        return granted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminAdjust(Long activityId, Long memberId, int amount, String remark, String operator)
    {
        if (activityId == null || memberId == null)
        {
            throw new ServiceException("活动与会员不能为空");
        }
        if (amount == 0)
        {
            throw new ServiceException("调整次数不能为0");
        }
        ensureAccount(activityId, memberId);
        if (amount < 0)
        {
            int bal = getBalance(activityId, memberId);
            if (bal + amount < 0)
            {
                throw new ServiceException("可用次数不足，当前余额=" + bal);
            }
        }
        String type = amount > 0 ? BizConstants.LOTTERY_CHANCE_ADMIN_GRANT : BizConstants.LOTTERY_CHANCE_ADMIN_DEDUCT;
        String bizNo = "ADMIN:" + activityId + ":" + memberId + ":" + System.currentTimeMillis() + ":" + amount;
        applyChange(activityId, memberId, amount, type, bizNo, null,
                StringUtils.isNotEmpty(remark) ? remark : "后台调整",
                StringUtils.isNotEmpty(operator) ? operator : "admin");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeForDraw(Long activityId, Long memberId, String bizNo)
    {
        ensureAccount(activityId, memberId);
        if (chanceLogMapper.selectByBizNo(bizNo) != null)
        {
            return true;
        }
        int rows = chanceMapper.consumeBalance(activityId, memberId, 1);
        if (rows <= 0)
        {
            return false;
        }
        BizLotteryChance row = chanceMapper.selectByActivityAndMember(activityId, memberId);
        BizLotteryChanceLog log = new BizLotteryChanceLog();
        log.setActivityId(activityId);
        log.setMemberId(memberId);
        log.setChangeType(BizConstants.LOTTERY_CHANCE_DRAW_CONSUME);
        log.setChangeAmount(Integer.valueOf(-1));
        log.setBalanceAfter(row.getBalance());
        log.setBizNo(bizNo);
        log.setRemark("抽奖消耗");
        log.setCreateBy("system");
        try
        {
            chanceLogMapper.insertChanceLog(log);
        }
        catch (DuplicateKeyException e)
        {
            return true;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundForDraw(Long activityId, Long memberId, String bizNo)
    {
        String refundNo = "REFUND:" + bizNo;
        if (chanceLogMapper.selectByBizNo(refundNo) != null)
        {
            return;
        }
        BizLotteryChanceLog consume = chanceLogMapper.selectByBizNo(bizNo);
        if (consume == null)
        {
            return;
        }
        ensureAccount(activityId, memberId);
        chanceMapper.refundBalance(activityId, memberId, 1);
        BizLotteryChance row = chanceMapper.selectByActivityAndMember(activityId, memberId);
        BizLotteryChanceLog log = new BizLotteryChanceLog();
        log.setActivityId(activityId);
        log.setMemberId(memberId);
        log.setChangeType(BizConstants.LOTTERY_CHANCE_DRAW_REFUND);
        log.setChangeAmount(Integer.valueOf(1));
        log.setBalanceAfter(row.getBalance());
        log.setBizNo(refundNo);
        log.setRemark("抽奖失败退回");
        log.setCreateBy("system");
        try
        {
            chanceLogMapper.insertChanceLog(log);
        }
        catch (DuplicateKeyException e)
        {
            // idempotent
        }
    }

    /**
     * 按条件倍数计算已达标档次。例：签到≥3 且 直推≥3，实际 6/6 → 2 档。
     */
    private int qualifiedLevels(BizLotteryChanceRule rule, int checkinDays, int inviteKyc)
    {
        int needCheckin = rule.getCheckinDays() != null ? rule.getCheckinDays().intValue() : 0;
        int needInvite = rule.getInviteKycCount() != null ? rule.getInviteKycCount().intValue() : 0;
        if (needCheckin <= 0 && needInvite <= 0)
        {
            return 0;
        }
        int levels = Integer.MAX_VALUE;
        if (needCheckin > 0)
        {
            levels = Math.min(levels, checkinDays / needCheckin);
        }
        if (needInvite > 0)
        {
            levels = Math.min(levels, inviteKyc / needInvite);
        }
        return Math.max(0, levels);
    }

    private boolean isOnceOnly(BizLotteryChanceRule rule)
    {
        return rule == null || !"0".equals(rule.getOnceOnly());
    }

    /**
     * 返回最多可发放档次数。仅一次=1；重复且 maxRepeat<=0 表示不封顶（按实际倍数发完）。
     */
    private int resolveMaxGrantTimes(BizLotteryChanceRule rule)
    {
        if (isOnceOnly(rule))
        {
            return 1;
        }
        int max = rule.getMaxRepeat() != null ? rule.getMaxRepeat().intValue() : 0;
        // 0 及以下：不限制档次，按合格倍数全部发放
        if (max <= 0)
        {
            return Integer.MAX_VALUE;
        }
        return max;
    }

    private String buildRuleGrantBizNo(Long activityId, Long memberId, Long ruleId, int seq)
    {
        if (seq <= 1)
        {
            // 兼容历史流水：RULE_GRANT:活动:会员:规则
            return "RULE_GRANT:" + activityId + ":" + memberId + ":" + ruleId;
        }
        return "RULE_GRANT:" + activityId + ":" + memberId + ":" + ruleId + ":#" + seq;
    }

    private boolean alreadyGranted(Long activityId, Long memberId, Long ruleId, int seq)
    {
        String bizNo = buildRuleGrantBizNo(activityId, memberId, ruleId, seq);
        if (chanceLogMapper.selectByBizNo(bizNo) != null)
        {
            return true;
        }
        if (seq == 1)
        {
            String alt = "RULE_GRANT:" + activityId + ":" + memberId + ":" + ruleId + ":#1";
            return chanceLogMapper.selectByBizNo(alt) != null;
        }
        return false;
    }

    private void applyChange(Long activityId, Long memberId, int amount, String changeType,
            String bizNo, Long ruleId, String remark, String operator)
    {
        if (amount > 0)
        {
            chanceMapper.addBalance(activityId, memberId, amount);
        }
        else if (amount < 0)
        {
            int rows = chanceMapper.consumeBalance(activityId, memberId, -amount);
            if (rows <= 0)
            {
                throw new ServiceException("可用次数不足");
            }
        }
        BizLotteryChance row = chanceMapper.selectByActivityAndMember(activityId, memberId);
        BizLotteryChanceLog log = new BizLotteryChanceLog();
        log.setActivityId(activityId);
        log.setMemberId(memberId);
        log.setChangeType(changeType);
        log.setChangeAmount(Integer.valueOf(amount));
        log.setBalanceAfter(row.getBalance());
        log.setBizNo(bizNo);
        log.setRuleId(ruleId);
        log.setRemark(remark);
        log.setCreateBy(operator);
        try
        {
            chanceLogMapper.insertChanceLog(log);
        }
        catch (DuplicateKeyException e)
        {
            // idempotent
        }
    }

    private void ensureAccount(Long activityId, Long memberId)
    {
        BizLotteryChance exist = chanceMapper.selectByActivityAndMember(activityId, memberId);
        if (exist != null)
        {
            return;
        }
        BizLotteryChance row = new BizLotteryChance();
        row.setActivityId(activityId);
        row.setMemberId(memberId);
        row.setBalance(Integer.valueOf(0));
        row.setTotalGranted(Integer.valueOf(0));
        row.setTotalConsumed(Integer.valueOf(0));
        try
        {
            chanceMapper.insertChance(row);
        }
        catch (DuplicateKeyException e)
        {
            // concurrent create
        }
    }

    private void validateRule(BizLotteryChanceRule rule)
    {
        if (rule.getActivityId() == null)
        {
            throw new ServiceException("请选择抽奖活动");
        }
        if (lotteryActivityMapper.selectLotteryActivityById(rule.getActivityId()) == null)
        {
            throw new ServiceException("抽奖活动不存在");
        }
        if (StringUtils.isEmpty(rule.getRuleName()))
        {
            throw new ServiceException("请填写规则名称");
        }
        int checkin = rule.getCheckinDays() != null ? rule.getCheckinDays().intValue() : 0;
        int invite = rule.getInviteKycCount() != null ? rule.getInviteKycCount().intValue() : 0;
        if (checkin < 0 || invite < 0)
        {
            throw new ServiceException("条件天数/人数不能为负");
        }
        if (checkin == 0 && invite == 0)
        {
            throw new ServiceException("签到天数与直推实名人数至少配置一项");
        }
        if (rule.getGrantAmount() == null || rule.getGrantAmount().intValue() < 1)
        {
            throw new ServiceException("发放次数须大于等于1");
        }
        if (isOnceOnly(rule))
        {
            rule.setOnceOnly("1");
            rule.setMaxRepeat(Integer.valueOf(1));
        }
        else
        {
            rule.setOnceOnly("0");
            int max = rule.getMaxRepeat() != null ? rule.getMaxRepeat().intValue() : 0;
            // 0 = 不限制档次；>0 = 最多 N 档
            if (max < 0)
            {
                throw new ServiceException("最多档次数不能为负（填 0 表示不限制）");
            }
            if (max > 999)
            {
                throw new ServiceException("最多档次数不能超过999");
            }
            rule.setMaxRepeat(Integer.valueOf(max));
        }
    }

    private void fillRuleDefaults(BizLotteryChanceRule rule, boolean create)
    {
        if (create && StringUtils.isEmpty(rule.getStatus()))
        {
            rule.setStatus(BizConstants.STATUS_OK);
        }
        if (StringUtils.isEmpty(rule.getOnceOnly()))
        {
            rule.setOnceOnly("1");
        }
        if (isOnceOnly(rule))
        {
            rule.setMaxRepeat(Integer.valueOf(1));
        }
        else if (rule.getMaxRepeat() == null || rule.getMaxRepeat().intValue() < 0)
        {
            // 重复模式默认不封顶
            rule.setMaxRepeat(Integer.valueOf(0));
        }
        if (rule.getSort() == null)
        {
            rule.setSort(Integer.valueOf(0));
        }
        if (rule.getCheckinDays() == null)
        {
            rule.setCheckinDays(Integer.valueOf(0));
        }
        if (rule.getInviteKycCount() == null)
        {
            rule.setInviteKycCount(Integer.valueOf(0));
        }
    }
}
