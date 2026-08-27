package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizWalletType;

public interface IBizWalletTypeService
{
    BizWalletType selectWalletTypeById(Long typeId);

    BizWalletType selectWalletTypeByCode(String typeCode);

    List<BizWalletType> selectWalletTypeList(BizWalletType type);

    int insertWalletType(BizWalletType type);

    int updateWalletType(BizWalletType type);

    int deleteWalletTypeByIds(Long[] typeIds);

    BizWalletType requireEnabled(String typeCode);

    void assertCanWithdraw(String typeCode, Long memberId, String currency);
}
