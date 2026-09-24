package com.ruoyi.biz.lottery;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCrowdCondition;
import com.ruoyi.biz.mapper.BizCrowdConditionMapper;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.mapper.BizOrderMapper;
import com.ruoyi.biz.mapper.BizRechargeMapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * 营销人群包条件匹配（全部 AND）
 */
@Component
public class CrowdRuleMatcher
{
    private static final String FIELD_INVITE_COUNT = "invite_count";
    private static final String FIELD_CHARGE_AMOUNT = "charge_amount";
    private static final String FIELD_USER_TYPE = "user_type";
    private static final String USER_TYPE_NEW = "NEW_USER";
    private static final String USER_TYPE_OLD = "OLD_USER";

    @Autowired
    private BizCrowdConditionMapper crowdConditionMapper;

    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private BizRechargeMapper rechargeMapper;

    @Autowired
    private BizOrderMapper orderMapper;

    /**
     * 会员是否命中人群包全部条件
     */
    public boolean match(Long packageId, Long memberId)
    {
        if (packageId == null || memberId == null)
        {
            return false;
        }
        List<BizCrowdCondition> conditions = crowdConditionMapper.selectConditionListByPackageId(packageId);
        if (conditions == null || conditions.isEmpty())
        {
            return false;
        }
        for (int i = 0; i < conditions.size(); i++)
        {
            if (!matchCondition(conditions.get(i), memberId))
            {
                return false;
            }
        }
        return true;
    }

    private boolean matchCondition(BizCrowdCondition condition, Long memberId)
    {
        if (condition == null || StringUtils.isEmpty(condition.getLabelField()))
        {
            return false;
        }
        String field = condition.getLabelField().trim();
        String operator = condition.getOperatorType();
        String ruleValue = condition.getRuleValue();
        if (FIELD_INVITE_COUNT.equals(field))
        {
            int inviteCount = memberMapper.countDirectMembers(memberId);
            return compareNumber(new BigDecimal(inviteCount), operator, ruleValue);
        }
        if (FIELD_CHARGE_AMOUNT.equals(field))
        {
            BigDecimal amount = rechargeMapper.sumPassedRecharge(memberId, BizConstants.CURRENCY_CNY);
            if (amount == null)
            {
                amount = BigDecimal.ZERO;
            }
            return compareNumber(amount, operator, ruleValue);
        }
        if (FIELD_USER_TYPE.equals(field))
        {
            String actual = orderMapper.countMemberOrders(memberId) > 0 ? USER_TYPE_OLD : USER_TYPE_NEW;
            return compareText(actual, operator, ruleValue);
        }
        return false;
    }

    private boolean compareNumber(BigDecimal actual, String operator, String ruleValue)
    {
        if (StringUtils.isEmpty(operator) || StringUtils.isEmpty(ruleValue))
        {
            return false;
        }
        BigDecimal expected;
        try
        {
            expected = new BigDecimal(ruleValue.trim());
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        int cmp = actual.compareTo(expected);
        if (BizConstants.CROWD_OP_EQUALS.equals(operator))
        {
            return cmp == 0;
        }
        if (BizConstants.CROWD_OP_GREATER_THAN.equals(operator))
        {
            return cmp > 0;
        }
        if (BizConstants.CROWD_OP_LESS_THAN.equals(operator))
        {
            return cmp < 0;
        }
        return false;
    }

    private boolean compareText(String actual, String operator, String ruleValue)
    {
        if (StringUtils.isEmpty(operator) || StringUtils.isEmpty(ruleValue))
        {
            return false;
        }
        String expected = ruleValue.trim();
        if (BizConstants.CROWD_OP_EQUALS.equals(operator))
        {
            return expected.equalsIgnoreCase(actual);
        }
        return false;
    }
}
