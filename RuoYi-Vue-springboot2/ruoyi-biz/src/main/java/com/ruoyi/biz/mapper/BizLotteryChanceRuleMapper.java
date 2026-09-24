package com.ruoyi.biz.mapper;


import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizLotteryChanceRule;

@Mapper
public interface BizLotteryChanceRuleMapper
{
    BizLotteryChanceRule selectChanceRuleById(Long ruleId);

    List<BizLotteryChanceRule> selectChanceRuleList(BizLotteryChanceRule query);

    List<BizLotteryChanceRule> selectEnabledRulesByActivityId(Long activityId);

    int insertChanceRule(BizLotteryChanceRule rule);

    int updateChanceRule(BizLotteryChanceRule rule);

    int deleteChanceRuleByIds(Long[] ruleIds);
}
