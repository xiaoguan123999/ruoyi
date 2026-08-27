package com.ruoyi.biz.service.impl;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizWalletType;
import com.ruoyi.biz.mapper.BizOrderMapper;
import com.ruoyi.biz.mapper.BizWalletCreditRuleMapper;
import com.ruoyi.biz.mapper.BizWalletMapper;
import com.ruoyi.biz.mapper.BizWalletTypeMapper;
import com.ruoyi.biz.service.IBizWalletTypeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizWalletTypeServiceImpl implements IBizWalletTypeService
{
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,31}$");

    @Autowired
    private BizWalletTypeMapper walletTypeMapper;

    @Autowired
    private BizWalletMapper walletMapper;

    @Autowired
    private BizWalletCreditRuleMapper creditRuleMapper;

    @Autowired
    private BizOrderMapper orderMapper;

    @Override
    public BizWalletType selectWalletTypeById(Long typeId)
    {
        return walletTypeMapper.selectWalletTypeById(typeId);
    }

    @Override
    public BizWalletType selectWalletTypeByCode(String typeCode)
    {
        if (StringUtils.isEmpty(typeCode))
        {
            return null;
        }
        return walletTypeMapper.selectWalletTypeByCode(typeCode.trim().toUpperCase());
    }

    @Override
    public List<BizWalletType> selectWalletTypeList(BizWalletType type)
    {
        return walletTypeMapper.selectWalletTypeList(type);
    }

    @Override
    public int insertWalletType(BizWalletType type)
    {
        fillDefaults(type);
        checkSave(type, true);
        if (walletTypeMapper.selectWalletTypeByCode(type.getTypeCode()) != null)
        {
            throw new ServiceException("钱包类型编码已存在");
        }
        return walletTypeMapper.insertWalletType(type);
    }

    @Override
    public int updateWalletType(BizWalletType type)
    {
        if (type == null || type.getTypeId() == null)
        {
            throw new ServiceException("请选择钱包类型");
        }
        BizWalletType db = walletTypeMapper.selectWalletTypeById(type.getTypeId());
        if (db == null)
        {
            throw new ServiceException("钱包类型不存在");
        }
        fillDefaults(type);
        checkSave(type, false);
        type.setTypeCode(db.getTypeCode());
        return walletTypeMapper.updateWalletType(type);
    }

    @Override
    public int deleteWalletTypeByIds(Long[] typeIds)
    {
        if (typeIds == null)
        {
            return 0;
        }
        for (int i = 0; i < typeIds.length; i++)
        {
            BizWalletType db = walletTypeMapper.selectWalletTypeById(typeIds[i]);
            if (db == null)
            {
                continue;
            }
            if ("1".equals(db.getBuiltin()))
            {
                throw new ServiceException(db.getTypeName() + "是内置钱包，不能删除");
            }
            if (walletMapper.countNonZeroByTypeCode(db.getTypeCode()) > 0)
            {
                throw new ServiceException(db.getTypeName() + "仍有余额或冻结，不能删除");
            }
            if (creditRuleMapper.countByTypeCode(db.getTypeCode()) > 0)
            {
                throw new ServiceException(db.getTypeName() + "仍被奖励入账使用，不能删除");
            }
            walletMapper.deleteWalletsByTypeCode(db.getTypeCode());
        }
        return walletTypeMapper.deleteWalletTypeByIds(typeIds);
    }

    @Override
    public BizWalletType requireEnabled(String typeCode)
    {
        BizWalletType type = selectWalletTypeByCode(typeCode);
        if (type == null)
        {
            throw new ServiceException("钱包类型不存在");
        }
        if (BizConstants.STATUS_DISABLE.equals(type.getStatus()))
        {
            throw new ServiceException(type.getTypeName() + "已停用");
        }
        return type;
    }

    @Override
    public void assertCanWithdraw(String typeCode, Long memberId, String currency)
    {
        BizWalletType type = requireEnabled(typeCode);
        String mode = type.getWithdrawMode() == null ? BizConstants.WALLET_WITHDRAW_NONE : type.getWithdrawMode();
        if (BizConstants.WALLET_WITHDRAW_NONE.equals(mode))
        {
            throw new ServiceException(type.getTypeName() + "不可提现");
        }
        if (BizConstants.WALLET_WITHDRAW_ANY_ORDER.equals(mode))
        {
            if (orderMapper.countMemberOrders(memberId) <= 0)
            {
                throw new ServiceException("请先购买产品后再提现" + type.getTypeName());
            }
            return;
        }
        if (BizConstants.WALLET_WITHDRAW_PRODUCT_REQUIRED.equals(mode))
        {
            if (orderMapper.countWithdrawRequiredOrders(memberId, currency) <= 0)
            {
                throw new ServiceException("请先认购对应币种的指定产品后再提现");
            }
        }
    }

    private void fillDefaults(BizWalletType type)
    {
        if (type.getStatus() == null || type.getStatus().length() == 0)
        {
            type.setStatus(BizConstants.STATUS_OK);
        }
        if (type.getSort() == null)
        {
            type.setSort(Integer.valueOf(0));
        }
        if (type.getBuiltin() == null || type.getBuiltin().length() == 0)
        {
            type.setBuiltin("0");
        }
        if (type.getWithdrawMode() == null || type.getWithdrawMode().length() == 0)
        {
            type.setWithdrawMode(BizConstants.WALLET_WITHDRAW_NONE);
        }
        if (type.getTypeCode() != null)
        {
            type.setTypeCode(type.getTypeCode().trim().toUpperCase());
        }
        if (type.getTypeName() != null)
        {
            type.setTypeName(type.getTypeName().trim());
        }
    }

    private void checkSave(BizWalletType type, boolean creating)
    {
        if (creating)
        {
            if (StringUtils.isEmpty(type.getTypeCode()) || !CODE_PATTERN.matcher(type.getTypeCode()).matches())
            {
                throw new ServiceException("编码需为2-32位大写字母数字下划线，且以字母开头");
            }
        }
        if (StringUtils.isEmpty(type.getTypeName()))
        {
            throw new ServiceException("请填写钱包名称");
        }
        if (type.getTypeName().length() > 32)
        {
            throw new ServiceException("钱包名称不能超过32字");
        }
        String mode = type.getWithdrawMode();
        if (!BizConstants.WALLET_WITHDRAW_NONE.equals(mode)
                && !BizConstants.WALLET_WITHDRAW_OPEN.equals(mode)
                && !BizConstants.WALLET_WITHDRAW_ANY_ORDER.equals(mode)
                && !BizConstants.WALLET_WITHDRAW_PRODUCT_REQUIRED.equals(mode))
        {
            throw new ServiceException("提现规则不正确");
        }
    }
}
