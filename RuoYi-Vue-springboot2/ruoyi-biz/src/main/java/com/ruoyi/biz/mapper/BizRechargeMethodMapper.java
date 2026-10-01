package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizRechargeMethod;

public interface BizRechargeMethodMapper
{
    BizRechargeMethod selectRechargeMethodById(Long methodId);

    BizRechargeMethod selectByMethodCode(String methodCode);

    List<BizRechargeMethod> selectRechargeMethodList(BizRechargeMethod query);

    int insertRechargeMethod(BizRechargeMethod row);

    int updateRechargeMethod(BizRechargeMethod row);

    int deleteRechargeMethodByIds(Long[] methodIds);
}
