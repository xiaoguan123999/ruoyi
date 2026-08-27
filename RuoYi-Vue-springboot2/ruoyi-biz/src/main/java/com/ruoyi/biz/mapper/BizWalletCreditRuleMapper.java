package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizWalletCreditRule;

public interface BizWalletCreditRuleMapper
{
    BizWalletCreditRule selectCreditRuleById(Long ruleId);

    BizWalletCreditRule selectCreditRuleByBizType(String bizType);

    List<BizWalletCreditRule> selectCreditRuleList(BizWalletCreditRule rule);

    int insertCreditRule(BizWalletCreditRule rule);

    int updateCreditRule(BizWalletCreditRule rule);

    int deleteCreditRuleByIds(Long[] ruleIds);

    int countByTypeCode(String typeCode);
}
