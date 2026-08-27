package com.ruoyi.biz.service.impl;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizWalletCreditRule;
import com.ruoyi.biz.domain.BizWalletType;
import com.ruoyi.biz.mapper.BizWalletCreditRuleMapper;
import com.ruoyi.biz.mapper.BizWalletTypeMapper;
import com.ruoyi.biz.service.IBizWalletCreditRuleService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizWalletCreditRuleServiceImpl implements IBizWalletCreditRuleService
{
    private static final Pattern BIZ_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,31}$");

    @Autowired
    private BizWalletCreditRuleMapper creditRuleMapper;

    @Autowired
    private BizWalletTypeMapper walletTypeMapper;

    @Override
    public BizWalletCreditRule selectCreditRuleById(Long ruleId)
    {
        return creditRuleMapper.selectCreditRuleById(ruleId);
    }

    @Override
    public List<BizWalletCreditRule> selectCreditRuleList(BizWalletCreditRule rule)
    {
        return creditRuleMapper.selectCreditRuleList(rule);
    }

    @Override
    public int insertCreditRule(BizWalletCreditRule rule)
    {
        fillDefaults(rule);
        checkSave(rule, true);
        if (creditRuleMapper.selectCreditRuleByBizType(rule.getBizType()) != null)
        {
            throw new ServiceException("该业务类型已配置入账钱包");
        }
        return creditRuleMapper.insertCreditRule(rule);
    }

    @Override
    public int updateCreditRule(BizWalletCreditRule rule)
    {
        if (rule == null || rule.getRuleId() == null)
        {
            throw new ServiceException("请选择入账配置");
        }
        BizWalletCreditRule db = creditRuleMapper.selectCreditRuleById(rule.getRuleId());
        if (db == null)
        {
            throw new ServiceException("入账配置不存在");
        }
        fillDefaults(rule);
        rule.setBizType(db.getBizType());
        checkSave(rule, false);
        return creditRuleMapper.updateCreditRule(rule);
    }

    @Override
    public int deleteCreditRuleByIds(Long[] ruleIds)
    {
        if (ruleIds == null)
        {
            return 0;
        }
        for (int i = 0; i < ruleIds.length; i++)
        {
            BizWalletCreditRule db = creditRuleMapper.selectCreditRuleById(ruleIds[i]);
            if (db != null && "1".equals(db.getBuiltin()))
            {
                throw new ServiceException(db.getBizName() + "是内置入账项，不能删除");
            }
        }
        return creditRuleMapper.deleteCreditRuleByIds(ruleIds);
    }

    @Override
    public String resolveTypeCode(String bizType)
    {
        if (StringUtils.isEmpty(bizType))
        {
            return BizConstants.WALLET_BALANCE;
        }
        String key = bizType.trim().toUpperCase();
        BizWalletCreditRule rule = creditRuleMapper.selectCreditRuleByBizType(key);
        if (rule != null && !StringUtils.isEmpty(rule.getTypeCode()))
        {
            return rule.getTypeCode();
        }
        if (BizConstants.BIZ_REBATE.equals(key))
        {
            return BizConstants.WALLET_PRODUCT;
        }
        if (BizConstants.isPromoIncome(key))
        {
            return BizConstants.WALLET_PROMO;
        }
        if (BizConstants.BIZ_SUBSCRIBE.equals(key))
        {
            return BizConstants.WALLET_BALANCE;
        }
        if (BizConstants.BIZ_WITHDRAW_PRODUCT.equals(key))
        {
            return BizConstants.WALLET_PRODUCT;
        }
        if (BizConstants.BIZ_WITHDRAW_PROMO.equals(key))
        {
            return BizConstants.WALLET_PROMO;
        }
        return BizConstants.WALLET_BALANCE;
    }

    @Override
    public BizWalletCreditRule selectCreditRuleByBizType(String bizType)
    {
        if (StringUtils.isEmpty(bizType))
        {
            return null;
        }
        BizWalletCreditRule rule = creditRuleMapper.selectCreditRuleByBizType(bizType.trim().toUpperCase());
        if (rule != null)
        {
            return rule;
        }
        BizWalletCreditRule fallback = new BizWalletCreditRule();
        fallback.setBizType(bizType.trim().toUpperCase());
        fallback.setTypeCode(resolveTypeCode(bizType));
        return fallback;
    }

    @Override
    public void saveTypeCodeByBizType(String bizType, String typeCode, String operator)
    {
        if (StringUtils.isEmpty(bizType))
        {
            throw new ServiceException("业务类型不能为空");
        }
        if (StringUtils.isEmpty(typeCode))
        {
            throw new ServiceException("请选择到账钱包");
        }
        String key = bizType.trim().toUpperCase();
        String code = typeCode.trim().toUpperCase();
        BizWalletType type = walletTypeMapper.selectWalletTypeByCode(code);
        if (type == null)
        {
            throw new ServiceException("入账钱包不存在");
        }
        BizWalletCreditRule db = creditRuleMapper.selectCreditRuleByBizType(key);
        String op = StringUtils.isEmpty(operator) ? "admin" : operator;
        if (db == null)
        {
            BizWalletCreditRule created = new BizWalletCreditRule();
            created.setBizType(key);
            created.setBizName(withdrawBizName(key));
            created.setTypeCode(code);
            created.setBuiltin("0");
            created.setSort(Integer.valueOf(99));
            created.setCreateBy(op);
            insertCreditRule(created);
            return;
        }
        db.setTypeCode(code);
        db.setUpdateBy(op);
        checkSave(db, false);
        creditRuleMapper.updateCreditRule(db);
    }

    private String withdrawBizName(String key)
    {
        if (BizConstants.BIZ_WITHDRAW_PRODUCT.equals(key))
        {
            return "产品收益提现";
        }
        if (BizConstants.BIZ_WITHDRAW_PROMO.equals(key))
        {
            return "推广收益提现";
        }
        return key;
    }

    private void fillDefaults(BizWalletCreditRule rule)
    {
        if (rule.getBuiltin() == null || rule.getBuiltin().length() == 0)
        {
            rule.setBuiltin("0");
        }
        if (rule.getSort() == null)
        {
            rule.setSort(Integer.valueOf(0));
        }
        if (rule.getBizType() != null)
        {
            rule.setBizType(rule.getBizType().trim().toUpperCase());
        }
        if (rule.getBizName() != null)
        {
            rule.setBizName(rule.getBizName().trim());
        }
        if (rule.getTypeCode() != null)
        {
            rule.setTypeCode(rule.getTypeCode().trim().toUpperCase());
        }
    }

    private void checkSave(BizWalletCreditRule rule, boolean creating)
    {
        if (creating)
        {
            if (StringUtils.isEmpty(rule.getBizType()) || !BIZ_PATTERN.matcher(rule.getBizType()).matches())
            {
                throw new ServiceException("业务类型需为2-32位大写字母数字下划线，且以字母开头");
            }
        }
        if (StringUtils.isEmpty(rule.getBizName()))
        {
            throw new ServiceException("请填写业务名称");
        }
        if (StringUtils.isEmpty(rule.getTypeCode()))
        {
            throw new ServiceException("请选择入账钱包");
        }
        BizWalletType type = walletTypeMapper.selectWalletTypeByCode(rule.getTypeCode());
        if (type == null)
        {
            throw new ServiceException("入账钱包不存在");
        }
    }
}
