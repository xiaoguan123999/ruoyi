package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizLotteryChance;
import com.ruoyi.biz.domain.BizLotteryChanceLog;
import com.ruoyi.biz.domain.BizLotteryChanceRule;

public interface IBizLotteryChanceService
{
    List<BizLotteryChanceRule> selectChanceRuleList(BizLotteryChanceRule query);

    BizLotteryChanceRule selectChanceRuleById(Long ruleId);

    int insertChanceRule(BizLotteryChanceRule rule);

    int updateChanceRule(BizLotteryChanceRule rule);

    int deleteChanceRuleByIds(Long[] ruleIds);

    List<BizLotteryChance> selectChanceList(BizLotteryChance query);

    List<BizLotteryChanceLog> selectChanceLogList(BizLotteryChanceLog query);

    int getBalance(Long activityId, Long memberId);

    /** 按启用规则尝试发放次数（幂等） */
    int tryGrantByRules(Long activityId, Long memberId);

    /** 对当前进行中活动按获次规则尝试发放（签到/实名等事件主动触发） */
    int tryGrantForMember(Long memberId);

    /** 后台加减次数，amount 正增负减 */
    void adminAdjust(Long activityId, Long memberId, int amount, String remark, String operator);

    /** 抽奖消耗 1 次，返回是否成功 */
    boolean consumeForDraw(Long activityId, Long memberId, String bizNo);

    /** 抽奖失败退回 1 次 */
    void refundForDraw(Long activityId, Long memberId, String bizNo);
}
