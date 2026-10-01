package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.api.AppPayChannelItem;
import com.ruoyi.biz.api.AppRechargeMethodItem;
import com.ruoyi.biz.domain.BizRechargeMethod;

public interface IBizRechargeMethodService
{
    BizRechargeMethod selectRechargeMethodById(Long methodId);

    List<BizRechargeMethod> selectRechargeMethodList(BizRechargeMethod query);

    int insertRechargeMethod(BizRechargeMethod row);

    int updateRechargeMethod(BizRechargeMethod row);

    int deleteRechargeMethodByIds(Long[] methodIds);

    /** App：仅返回启用中的充值方式（不含通道） */
    List<AppRechargeMethodItem> listAppMethods();

    /** App：按充值方式查可用通道 */
    List<AppPayChannelItem> listAppChannelsByMethodCode(String methodCode);
}
