package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizWalletCreditRule;

public interface IBizWalletCreditRuleService
{
    BizWalletCreditRule selectCreditRuleById(Long ruleId);

    List<BizWalletCreditRule> selectCreditRuleList(BizWalletCreditRule rule);

    int insertCreditRule(BizWalletCreditRule rule);

    int updateCreditRule(BizWalletCreditRule rule);

    int deleteCreditRuleByIds(Long[] ruleIds);

    String resolveTypeCode(String bizType);

    BizWalletCreditRule selectCreditRuleByBizType(String bizType);

    void saveTypeCodeByBizType(String bizType, String typeCode, String operator);
}
