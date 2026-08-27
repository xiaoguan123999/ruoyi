package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizWalletType;

public interface BizWalletTypeMapper
{
    BizWalletType selectWalletTypeById(Long typeId);

    BizWalletType selectWalletTypeByCode(String typeCode);

    List<BizWalletType> selectWalletTypeList(BizWalletType type);

    int insertWalletType(BizWalletType type);

    int updateWalletType(BizWalletType type);

    int deleteWalletTypeByIds(Long[] typeIds);
}
